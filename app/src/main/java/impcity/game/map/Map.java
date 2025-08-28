package impcity.game.map;

import impcity.game.Features;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import impcity.game.Texture;
import impcity.ogl.Drawable;
import impcity.ogl.Light;
import impcity.ui.Colors;
import rlgamekit.map.data.LayeredMap;

/**
 * Ground layer data usage:
 * 
 * (0,0) - Ground (floor) tile 
 * (0,1) - Left wall
 * (1,0) - Right wall
 * (1,1) - Main feature
 * (0,2) - Left wall deco
 * (2,0) - Right wall deco
 * (2,2) - Way overlay
 * (1,2) - Way-like item
 * (2,1) - unused
 * (3,0) - temperature
 * (3,3) - brightness/color
 * 
 * @author Hj. Malthaner
 */
public class Map implements Serializable
{
    private static final int LAYER_GROUNDS = 0;
    private static final int LAYER_ITEMS = 1;
    private static final int LAYER_MOBS = 2;

    // The item layer of the map bears item numbers in the lower 24 bits
    // and special flags in the upper 8 bits.

    public static final int MASK_ITEM = 0xFFFFFF;
    public static final int MASK_FLAGS = 0xFF000000;
    public static final int FLAG_MOVEMENT_BLOCKED = 0x80000000;
    public static final int FLAG_PLACEMENT_BLOCKED = 0x40000000;

    private LayeredMap map;
    // public static final int SUB = 14;
    public static final int SUB = 12;

    // Offset for full tile blocks (earth, rock, resources)
    public static final int O_BLOCK = Map.SUB/2-1;

    public static final int F_ITEM = 0x10000;
    public static final int F_FLOOR_DECO = 0x20000;
    public static final int F_IDENT_MASK = 0xFFFF;

    public final ArrayList <Light> lights;
    private final SparseMapLayer<Drawable> effects;
    public int darkness;


    public static Point randomCirclePoint(int centerX, int centerY, int radius)
    {
        double angle = Math.random() * Math.PI * 2.0;
        int x = (int)(Math.cos(angle) * radius);
        int y = (int)(Math.cos(angle) * radius);
        return new Point(centerX + x, centerY + y);
    }
    
    
    public Map(int width, int height)
    {
        this.lights = new ArrayList <Light> ();
        this.effects = new SparseMapLayer<Drawable>(width*SUB, height*SUB);
        newMap(width, height);
    }


    public int getFloor(int w, int h)
    {
        return map.get(LAYER_GROUNDS, w, h);
    }


    public void setFloor(int w, int h, int floor)
    {
        map.set(LAYER_GROUNDS, w, h, floor);
    }


    public int getWay(int w, int h)
    {
        return map.get(LAYER_GROUNDS, w+2, h+2);
    }


    public void setWay(int w, int h, int floor)
    {
        map.set(LAYER_GROUNDS, w+2, h+2, floor);
    }


    public int getWayLikeItem(int w, int h)
    {
        return map.get(LAYER_GROUNDS, w+1, h+2);
    }


    public void setWayLikeItem(int w, int h, int floor)
    {
        map.set(LAYER_GROUNDS, w+1, h+2, floor);
    }


    public int getLeftWall(int w, int h)
    {
        return map.get(LAYER_GROUNDS, w, h+1);
    }


    public void setLeftWall(int w, int h, int wall)
    {
        map.set(LAYER_GROUNDS, w, h+1, wall);
    }


    public int getRightWall(int w, int h)
    {
        return map.get(LAYER_GROUNDS, w+1, h);
    }


    public void setRightWall(int w, int h, int wall)
    {
        map.set(LAYER_GROUNDS, w+1, h, wall);
    }


    public int getColor(int w, int h)
    {
        return map.get(LAYER_GROUNDS, w+3, h+3);
    }


    public void setColor(int w, int h, int argb)
    {
        map.set(LAYER_GROUNDS, w+3, h+3, argb);
    }


    public int getItem(int x, int y)
    {
        return map.get(LAYER_ITEMS, x, y) & MASK_ITEM;
    }


    /**
     * Setting an item keeps all special flags (e.g. movement) for the spot
     * @param x item x locations
     * @param y item y location
     * @param item item number (key id)
     */
    public void setItem(int x, int y, int item)
    {
        int v = map.get(LAYER_ITEMS, x, y);
        v = (v & MASK_FLAGS) | item;
        map.set(LAYER_ITEMS, x, y, v);
    }


    public boolean isMovementBlocked(int x, int y)
    {
        return (map.get(LAYER_ITEMS, x, y) & FLAG_MOVEMENT_BLOCKED) != 0;
    }


    public boolean isMovementBlockedRadius(int x, int y, int r)
    {
        RectArea area = new RectArea(x, y, 0, 0);
        
        LocationCallback callback = 
            new LocationCallback() 
            {
                @Override
                public boolean visit(int x, int y) 
                {
                    return isMovementBlocked(x, y);
                }
            };        
                
        return area.spirallyTraverse(callback, r);
    }


    public void setMovementBlocked(int x, int y, boolean yesno)
    {
        int v = map.get(LAYER_ITEMS, x, y);
        if(yesno)
        {
            v = (v & ~FLAG_MOVEMENT_BLOCKED) | FLAG_MOVEMENT_BLOCKED;
        }
        else
        {
            v = (v & ~FLAG_MOVEMENT_BLOCKED);
        }
        map.set(LAYER_ITEMS, x, y, v);
    }


    public void setMovementBlockedRadius(int x, int y, int r, final boolean yesno)
    {
        RectArea area = new RectArea(x, y, 0, 0);
        
        LocationCallback callback = 
            new LocationCallback() 
            {
                @Override
                public boolean visit(int x, int y) 
                {
                    setMovementBlocked(x, y, yesno);
                    return false; // continue iterating
                }
            };        
                
        area.spirallyTraverse(callback, r);
    }


    public boolean isPlacementBlocked(int x, int y)
    {
        return (map.get(LAYER_ITEMS, x, y) & FLAG_PLACEMENT_BLOCKED) != 0;
    }


    public boolean isPlacementBlockedRadius(int x, int y, int r)
    {
        RectArea area = new RectArea(x, y, 0, 0);
        
        LocationCallback callback = 
            new LocationCallback() 
            {
                @Override
                public boolean visit(int x, int y) 
                {
                    return isPlacementBlocked(x, y);
                }
            };        
                
        return area.spirallyTraverse(callback, r);
    }


    public void setPlacementBlocked(int x, int y, boolean yesno)
    {
        int v = map.get(LAYER_ITEMS, x, y);
        if(yesno)
        {
            v = (v & ~FLAG_PLACEMENT_BLOCKED) | FLAG_PLACEMENT_BLOCKED;
        }
        else
        {
            v = (v & ~FLAG_PLACEMENT_BLOCKED);
        }
        map.set(LAYER_ITEMS, x, y, v);
    }


    public void setPlacementBlockedRadius(int x, int y, int r, final boolean yesno)
    {
        RectArea area = new RectArea(x, y, 0, 0);
        
        LocationCallback callback = 
            new LocationCallback() 
            {
                @Override
                public boolean visit(int x, int y) 
                {
                    setPlacementBlocked(x, y, yesno);
                    return false; // continue iterating
                }
            };        
                
        area.spirallyTraverse(callback, r);
    }


    public int getMob(int x, int y)
    {
        return map.get(LAYER_MOBS, x, y);
    }


    public void setMob(int x, int y, int mob)
    {
        map.set(LAYER_MOBS, x, y, mob);
    }


    public Drawable getEffect(int x, int y)
    {
        return effects.get(x, y);
    }


    public void setEffect(int x, int y, Drawable effect)
    {
        if(effect == null)
        {
            effects.remove(x, y);
        }
        else
        {
            effects.set(x, y, effect);
        }
    }


    public int getWidth() 
    {
        return map.getWidth();
    }


    public int getHeight() 
    {
        return map.getHeight();
    }


    public void recalculateBlockedAreas(Texture [] textures)
    {
        Rectangle allMap = new Rectangle(getWidth(), getHeight());
        setAreaMovementBlocked(0, 0, allMap, false);
        
        for(int j=0; j<getHeight(); j+=SUB)
        {
            for(int i=0; i<getWidth(); i+=SUB)
            {
                int n;
                
                n = getRightWall(i, j);
                if(n > 0)
                {
                    Texture tex = textures[n];
                    setAreaMovementBlocked(i, j, tex.area, true);
                }
                n = getLeftWall(i, j);
                if(n > 0)
                {
                    Texture tex = textures[n];
                    setAreaMovementBlocked(i, j, tex.area, true);
                }
            }
        }
    }


    public void save(File file) throws IOException
    {
        FileOutputStream fos = new FileOutputStream(file);
        save(fos);
    }


    public void save(OutputStream out) throws IOException    
    {
        GZIPOutputStream gos = new GZIPOutputStream(out);
        
        OutputStreamWriter writer = new OutputStreamWriter(gos);
        map.write(writer);
        writer.close();
    }


    public void load(File file) throws IOException
    {
        FileInputStream fis = new FileInputStream(file);
        load(fis);
    }


    public void load(InputStream is) throws IOException
    {
        GZIPInputStream gis = new GZIPInputStream(is);
        
        InputStreamReader fr = new InputStreamReader(gis);
        BufferedReader reader = new BufferedReader(fr);
        LayeredMap newMap = new LayeredMap(3, 16, 16);
        newMap.read(reader);
        reader.close();
        
        map = newMap;
        darkness = 0;
        lights.clear();
        effects.resize(map.getWidth(), map.getHeight());
    }


    public void setAreaMovementBlocked(Rectangle area, boolean yesno)
    {
        setAreaMovementBlocked(0, 0, area, yesno);
    }


    public void setAreaMovementBlocked(int i, int j, Rectangle area, boolean yesno)
    {
        for(int jj=0; jj<area.width; jj++)
        {
            for(int ii=0; ii<area.height; ii++)
            {
                setMovementBlocked(i+ii+area.x, j+jj+area.y, yesno);
            }
        }
    }


    public void setAreaPlacementBlocked(Rectangle area, boolean yesno)
    {
        setAreaPlacementBlocked(0, 0, area, yesno);
    }


    public void setAreaPlacementBlocked(int i, int j, Rectangle area, boolean yesno)
    {
        for(int jj=0; jj<area.width; jj++)
        {
            for(int ii=0; ii<area.height; ii++)
            {
                setPlacementBlocked(i+ii+area.x, j+jj+area.y, yesno);
            }
        }
    }


    public void setPlayerSpawnPosition(int i, int j)
    {
        map.setSpawnX(i);
        map.setSpawnY(j);
    }


    public Point getPlayerSpawnPosition()
    {
        return new Point(map.getSpawnX(), map.getSpawnY());
    }
            
    
    public final void newMap(int w, int h) 
    {
        map = new LayeredMap(3, w, h);
        darkness = 0;
        
        int brightness = 128;
        
        for(int j=0; j<getHeight(); j+=SUB)
        {
            for(int i=0; i<getWidth(); i+=SUB)
            {
                int rgb = Colors.randomGray(brightness, 32);
                setColor(i, j, rgb);
            }
        }
    }


    public boolean dropItem(int x, int y, final int itemKey, LocationVisitor visitor)
    {
        RectArea area = new RectArea(x, y, 0, 0);
        
        LocationCallback callback = 
            new LocationCallback() 
            {
                @Override
                public boolean visit(int x, int y) 
                {
                    int rasterI = x - x % Map.SUB;
                    int rasterJ = y - y % Map.SUB;
                    
                    if(getItem(x, y) == 0 && 
                            !isPlacementBlocked(x, y) &&
                            !isMovementBlockedRadius(x, y, 2) && // Imps must be able to reach it  
                            !Features.isImpassable(getFloor(rasterI, rasterJ)))
                    {
                        setItem(x, y, itemKey);
                        visitor.visit(x, y);
                        return true;
                    }
                    return false;
                }
            };        
                
        return area.spirallyTraverse(callback, Map.SUB/2);
    }


    public int getTemperature(int i, int j)
    {
        return map.get(LAYER_GROUNDS, i+3, j+0);
    }


    public void setTemperature(int mouseI, int mouseJ, int temp)
    {
        int i, j;
        double d;
        
        i = (mouseI / Map.SUB) * Map.SUB;
        j = (mouseJ / Map.SUB) * Map.SUB;

        d = Math.sqrt(distance2(i, j, mouseI, mouseJ));
        setColor(i, j, Colors.mix(0x909090, temp, (int)(d*10))); 
        
        d = Math.sqrt(distance2(i+SUB, j, mouseI, mouseJ));
        setColor(i+SUB, j, Colors.mix(0x909090, temp, (int)(d*10))); 
        
        d = Math.sqrt(distance2(i, j+SUB, mouseI, mouseJ));
        setColor(i, j+SUB, Colors.mix(0x909090, temp, (int)(d*10))); 
        
        d = Math.sqrt(distance2(i+SUB, j+SUB, mouseI, mouseJ));
        setColor(i+SUB, j+SUB, Colors.mix(0x909090, temp, (int)(d*10))); 
    
        map.set(LAYER_GROUNDS, i+3, j+0, temp);
    }


    public void traverseArea(int x, int y, int w, int h,
                             LocationVisitor visitor)
    {
        for(int j=0; j<h; j++)
        {
            for(int i=0; i<w; i++)
            {
                visitor.visit(x + i, y + j);
            }
        }        
    }
    
    
    public void copyFrom(Map other)
    {
        int width = other.map.getWidth();
        int height = other.map.getHeight();
        
        newMap(width, height);
        effects.resize(width, height);
        
        map.insert(other.map, 0, 0, width, height, 0, 0);
        lights.clear();
        lights.addAll(other.lights);
    }


    public static int distance2(int i1, int j1, int i2, int j2)
    {
        int xd = i1 - i2;
        int yd = j1 - j2;
        
        return xd * xd + yd * yd;
    }


    public void removeLight(int x, int y) 
    {
        for(int i=lights.size()-1; i>=0; i--)
        {
            Light light = lights.get(i);
            if(light.x == x && light.y == y)
            {
                lights.remove(i);
            }
        }
    }
}
