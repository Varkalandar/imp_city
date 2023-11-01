package impcity.game;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.logging.Logger;
import impcity.game.map.ClearItems;
import impcity.game.map.Map;
import impcity.game.map.MapTransitions;
import impcity.game.map.RectArea;
import impcity.game.mobs.Mob;
import impcity.ogl.Light;
import rlgamekit.map.data.LayeredMap;
import rlgamekit.map.generators.MapUtils;
import rlgamekit.objects.ArrayRegistry;
import rlgamekit.objects.Registry;

/**
 * The game world.
 * 
 * @author Hj. Malthaner
 */
public class World
{
    private static final Logger logger = Logger.getLogger(World.class.getName());
    
    private static final int MAP_LAYERS = 3;

    private static final MapTransitions mapTransitions = new MapTransitions();

    public final Registry<Mob> mobs;
    public final Registry<Item> items;


    public World()
    {
        mobs = new ArrayRegistry<Mob> (1 << 12);
        items = new ArrayRegistry<Item> (1 << 10);
    }


    private void makeRoomLine(LayeredMap lmap, ArrayList <Rectangle> rooms, int dx, int dy, int x, int y, int minLength, int lengthRand, int count)
    {
        MapUtils mapUtils = new MapUtils();
        
        for(int i=0; i < count; i++)
        {
            // Hajo: First, a corridor
            int length = minLength + (int)(Math.random() * lengthRand);

            fillArea(mapUtils, lmap, x, y, x + length*dx, y + length*dy, new int [] {';'});
            
            x += length*dx;
            y += length*dy;
            
            // Hajo: then, a room

            int size = 1 + (int)(Math.random() * 2);
            
            int centerX = x + dx * size;
            int centerY = y + dy * size;
            int x1 = centerX - (dx + dy) * size;
            int x2 = centerX + (dx + dy) * size;
            
            int y1 = centerY - (dx + dy) * size;
            int y2 = centerY + (dx + dy) * size;

            if(x1 > x2)
            {
                int t = x2;
                x2 = x1;
                x1 = t;
            }
            
            if(y1 > y2)
            {
                int t = y2;
                y2 = y1;
                y1 = t;
            }
        
            Rectangle room = new Rectangle(x1, y1, x2-x1+1, y2-y1+1);
            mapUtils.fillArea(lmap, 0, room.x, room.y, room.width, room.height, new int [] {':'});
            rooms.add(room);
        
            x += dx * (2*size+1);
            y += dy * (2*size+1);
        }
    }
    
    private void fillArea(MapUtils mapUtils, LayeredMap lmap, int x1, int y1, int x2, int y2, int [] values)
    {
        if(x1 > x2)
        {
            int t = x2;
            x2 = x1;
            x1 = t;
        }

        if(y1 > y2)
        {
            int t = y2;
            y2 = y1;
            y1 = t;
        }
        
        mapUtils.fillArea(lmap, 0, x1, y1, x2-x1+1, y2-y1+1, values);
    }
    
    
    public void step()
    {
        
    }


    private void setRightWall(Map map, int i, int j, int wallType, double lightChance)
    {
        map.setRightWall(i, j, wallType);
        if(Math.random() < lightChance)
        {
            Light light = new Light(i, j + Map.SUB/2 - 1, 60, 3, 0xDDFFCC99, 0.7);
            map.lights.add(light);
            
            map.setItem(i, j + Map.SUB/2, 5);
        }
    }

    private void setLeftWall(Map map, int i, int j, int wallType, double lightChance)
    {
        map.setLeftWall(i, j, wallType);
        if(Math.random() < lightChance)
        {
            Light light = new Light(i + Map.SUB/2 - 1, j, 60, 3, 0xDDFFCC99, 0.7);
            map.lights.add(light);

            map.setItem(i + Map.SUB/2, j, 4);
        }
    }
    
    private void addWallEndPieces(Map map, int wallSet) 
    {
        int h = map.getHeight();
        int w = map.getWidth();

        // left back, right back, left front,right front,
        // left half pillar, left full pillar, right full pillar, right half pillar
        
        for(int j=0; j<h; j+=Map.SUB)
        {
            for(int i=0; i<w; i+=Map.SUB)
            {
                int n = map.getLeftWall(i, j);
                
                if(n == wallSet)
                {
                    // Hajo: There is a wall here, is it an wall end?
                    int n1 = map.getLeftWall(i+Map.SUB, j);
                    if(n1 != n)
                    {
                        // down end, check for incoming wall
                        n1 = map.getLeftWall(i + Map.SUB, j - Map.SUB);
                        if(n1 == 0)
                        {
                            map.setItem(i + Map.SUB-1, j, wallSet + 5);
                        }
                    }
                    
                    n1 = map.getLeftWall(i-Map.SUB, j);
                    if(n1 != n)
                    {
                        // up end - check for right wall incoming
                        n1 = map.getRightWall(i, j);
                        if(n1 == 0)
                        {
                            // no wall, must be end
                            map.setItem(i, j, wallSet + 7);
                        }
                    }
                }

                n = map.getRightWall(i, j);
                if(n == wallSet + 1)
                {
                    // Hajo: There is a wall here, is it an wall end?
                    int n1 = map.getRightWall(i, j+Map.SUB);
                    if(n1 != n)
                    {
                        // down end, check for incoming wall
                        n1 = map.getLeftWall(i - Map.SUB, j + Map.SUB);
                        if(n1 == 0)
                        {
                            map.setItem(i, j + Map.SUB-1, wallSet + 6);
                        }
                    }
                    n1 = map.getRightWall(i, j-Map.SUB);
                    if(n1 != n)
                    {
                        // up end - check for left wall incoming
                        n1 = map.getLeftWall(i, j);
                        if(n1 == 0)
                        {
                            // no wall, must be end
                            map.setItem(i, j, wallSet + 4);
                        }
                    }
                }
            }
        }
    }

    public boolean isArtifact(int key)
    {
        boolean yesno = false;

        if((key & Map.F_ITEM) != 0)
        {
            Item item = items.get(key & Map.F_ITEM_MASK);
            yesno = (item.texId >= Features.ARTIFACTS_FIRST && item.texId <= Features.ARTIFACTS_LAST);
        }

        return yesno;
    }
}

