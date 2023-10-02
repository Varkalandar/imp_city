package impcity.ogl;

import java.util.ArrayList;
import java.util.Arrays;
import impcity.ui.PixFont;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;


/**
 *
 * @author Hj. Malthaner
 */
public class HotspotMap 
{
    private final Hotspot [] hotspots = new Hotspot[1024];
    private int maxIndex;
    
    private final ArrayList <ArrayList <Hotspot> > rows;
    
    
    public HotspotMap()
    {
        for(int i=0; i<hotspots.length; i++)
        {
            hotspots[i] = new Hotspot();
        }
        
        rows = new ArrayList <ArrayList <Hotspot> > ();
        
        for(int i=0; i<50; i++)
        {
            ArrayList <Hotspot> row = new ArrayList<Hotspot>();
            rows.add(row);
        }
    }
    
    public void clear()
    {
        maxIndex = 0;
    }
    
    public void addHotspot(int mapI, int mapJ, int screenX, int screenY, String label)
    {
        hotspots[maxIndex].mapI = mapI;
        hotspots[maxIndex].mapJ = mapJ;
        hotspots[maxIndex].screenX = screenX;
        hotspots[maxIndex].screenY = screenY;
        hotspots[maxIndex].label = label;
        maxIndex ++;
    }
    
    public void layout(IsoDisplay display)
    {
        if(maxIndex > 0)
        {
            PixFont font = display.font;
            int lineSpacing = 22;

            // Hajo: center on item position
            for(int i=0; i<maxIndex; i++)
            {
                int width = 10 + (int)(font.getStringWidth(hotspots[i].label) * 0.6);
                int height = 20;

                hotspots[i].screenX -= width/2;
                hotspots[i].screenW = width;
                hotspots[i].screenH = height;
            }

            // Hajo: sort hotspots by screen Y
            Arrays.sort(hotspots, 0, maxIndex);


            // Hajo: fill label rows

            int anchorY = hotspots[0].screenY;

            for(ArrayList<Hotspot> row : rows)
            {
                row.clear();
            }

            for(int i=0; i<maxIndex; i++)
            {
                int rowIndex = (hotspots[i].screenY - anchorY) / lineSpacing;
                int yDiff = hotspots[i].screenY - anchorY;
                int yRest = yDiff % lineSpacing; 
                hotspots[i].screenY -= yRest;

                ArrayList <Hotspot> row = rows.get(rowIndex);

                if(row.isEmpty())
                {
                    row.add(hotspots[i]);
                }
                else
                {
                    // Hajo: sort row by screenX
                    int insert = 0;
                    for(int ri=0; ri<row.size(); ri++)
                    {
                        Hotspot other = row.get(ri);
                        if(other.screenX > hotspots[i].screenX)
                        {
                            insert = ri;
                            break;
                        }
                    }
                    row.add(insert, hotspots[i]);
                }
            }        

            // Hajo: adjust horizontal positions in each row

            int hGap = 4;

            for(ArrayList<Hotspot> row : rows)
            {
                if(!row.isEmpty())
                {
                    Hotspot leftmost = row.get(0);
                    int startX = leftmost.screenX;
                    int totalWidth = 0;
                    for(Hotspot hotspot : row)
                    {
                        totalWidth += hotspot.screenW + hGap;
                    }

                    startX -= totalWidth / 2;
                    int x = startX;
                    for(Hotspot hotspot : row)
                    {
                        hotspot.screenX = x;
                        x += hotspot.screenW + hGap;
                    }
                }
            }        
        }
    }
    
    public void display(IsoDisplay display)
    {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        PixFont font = display.font;
        
        for(int i=0; i<maxIndex; i++)
        {
            String name = hotspots[i].label;
            int x = hotspots[i].screenX;
            int y = hotspots[i].screenY;
            int width = hotspots[i].screenW;
            int height = hotspots[i].screenH;

            IsoDisplay.fillRect(x, y+11, width, height, 0x80000000);
            font.drawStringScaled(name, 0xFFFFFFFF, x+5, y+12, 0.6);
        }
    }
    
    public Hotspot findHotspot(int x, int y)
    {
        for(int i=0; i<maxIndex; i++)
        {
            if(x >= hotspots[i].screenX && 
               y >= hotspots[i].screenY &&
               x < hotspots[i].screenX + hotspots[i].screenW && 
               y < hotspots[i].screenY + hotspots[i].screenH)
            {
                return hotspots[i];
            }
        }
        
        return null;
    }
    
    
    public static class Hotspot implements Comparable<Hotspot>
    {
        public int mapI;
        public int mapJ;
        public int screenX;
        public int screenY;
        public int screenW;
        public int screenH;
        public String label;
        
        
        @Override
        public int compareTo(Hotspot other)
        {
            return (other.screenY > screenY) ? -1 : (other.screenY < screenY) ? 1 : 0;
        }
    }
}
