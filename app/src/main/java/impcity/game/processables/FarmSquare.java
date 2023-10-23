package impcity.game.processables;

import impcity.game.Features;
import impcity.game.Clock;
import impcity.game.map.Map;

/**
 *
 * @author Hj. Malthaner
 */
public class FarmSquare implements Processable
{
    public int x, y;
    public long time;

    public FarmSquare(int rasterI, int rasterJ, long time) 
    {
        this.x = rasterI;
        this.y = rasterJ;
        this.time = time;
    }

    @Override
    public boolean equals(Object other)
    {
        boolean ok = false;
        
        if(other instanceof FarmSquare)
        {
            FarmSquare fs = (FarmSquare)other;
            ok = fs.x == x && fs.y == y;
        }
        return ok;
    }

    @Override
    public int hashCode() 
    {
        int hash = 5;
        hash = 97 * hash + this.x;
        hash = 97 * hash + this.y;
        return hash;
    }
    
    @Override
    public void process(Map map) 
    {
        if(time < Clock.time())
        {
            int xr = x + (int)(Math.random() * Map.SUB);
            int yr = y + (int)(Math.random() * Map.SUB);

            // must be a reachable location ...
            int size = 5;
            boolean ok = true;
            for(int j=-size; j<=size && ok; j++)
            {
                for(int i=-size; i<=size && ok; i++)
                {           
                    int xpos = xr + i;
                    int ypos = yr + j;

                    int ground = map.getFloor(xpos - (xpos % Map.SUB), ypos - (ypos % Map.SUB));
                    ok &= (ground >= Features.GROUND_GRASS_DARK && ground < Features.GROUND_GRASS_DARK + 3);
                }
            }

            int n = map.getItem(xr, yr);
            
            if(ok)
            {
                if(n >= Features.PLANTS_FIRST && n < Features.PLANTS_FIRST + Features.PLANTS_STRIDE*6)
                {
                    // Hajo: a 5-stage plant. 7 types
                    map.setItem(xr, yr, n + Features.PLANTS_STRIDE);
                }
                else if(n >= Features.PLANTS_FIRST && n <= Features.PLANTS_LAST)
                {
                    // too old plant ...
                    map.setItem(xr, yr, 0);

                }
                else if(n == Features.I_MUSHROOM)
                {
                    // old mushroom
                    map.setItem(xr, yr, 0);
                }
            }
            else
            {
                // unreachable location, place random shrubs
                // but only if the location is clear
                if(n == 0)
                {
                    double choice = Math.random();
                
                    if(choice > 0.7)
                    {
                        map.setItem(xr, yr, Features.I_SMALL_MOSSY_PATCH);
                    }
                    else if(choice > 0.4)
                    {
                        map.setItem(xr, yr, Features.I_TINY_SHRUBS);
                    }
                    else
                    {
                        map.setItem(xr, yr, Features.I_WET_AREA);
                    }
                }
            }
            time = Clock.time() + 2500;
        }
    }
}
