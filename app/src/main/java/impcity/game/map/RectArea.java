package impcity.game.map;

import java.awt.Point;
import java.awt.Rectangle;

/**
 *
 * @author Hj. Malthaner
 */
public class RectArea 
{
    public final Rectangle area;
    
    public RectArea(int x, int y, int w, int h)
    {
        area = new Rectangle(x, y, w, h);
    }

    
    public Point spirallyTraverse(LocationCallback callback, int maxRadius)
    {
        int cx = area.x + area.width/2;
        int cy = area.y + area.height/2;
        
        for(int radius = 0; radius < maxRadius; radius ++)
        {
            for(int j=-radius; j<=radius; j++)
            {
                for(int i=-radius; i<=radius; i++)
                {
                    boolean ok = callback.visit(cx+i, cy+j);
                    if(ok) {
                        return new Point(cx+i, cy+j);
                    }
                }
            }
        }
        
        return null;
    }
    

    public boolean traverseWithoutCorners(LocationCallback callback)
    {
        for(int j=area.y+1; j<area.y + area.height-1; j++)
        {
            for(int i=area.x; i<area.x + area.width; i++)
            {
                boolean ok = callback.visit(i, j);
                if(ok) return true;
            }
        }
        
        for(int i=area.x+1; i<area.x + area.width-1; i++)
        {
            boolean ok = callback.visit(i, area.y);
            if(ok) return true;
            ok = callback.visit(i, area.y+area.height-1);
            if(ok) return true;
        }
        
        return false;
    }
}
