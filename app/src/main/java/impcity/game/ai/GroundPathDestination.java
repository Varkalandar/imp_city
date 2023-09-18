package impcity.game.ai;

import java.util.HashSet;
import java.util.Set;
import impcity.game.map.*;
import rlgamekit.pathfinding.PathDestination;

/**
 *
 * @author Hj. Malthaner
 */
public class GroundPathDestination implements PathDestination
{
    private final int ground;
    private final Map map;
    private final int size;
    
    public GroundPathDestination(Map map, int ground, int size)
    {
        this.map = map;
        this.ground = ground;
        this.size = size;
    }


    @Override
    public boolean isDestinationReached(int x, int y) 
    {
        boolean ok = true;
        
            for(int dy = -size; dy <= size && ok; dy++)
            {
                for(int dx = -size; dx <= size && ok; dx++)
                {
                    int xpos = dx + x;
                    int ypos = dy + y;

                    int rasterI = xpos/Map.SUB*Map.SUB;
                    int rasterJ = ypos/Map.SUB*Map.SUB;
                    int n = map.getFloor(rasterI, rasterJ);
                    ok &= (n >= ground) && (n < ground + 3);
                }
            }
        
        return ok;
    }
    
    /**
     * Give an estimation of the remaining path length. It is better to 
     * underestimate and 0 is an allowed value. Negative values are not
     * allowed results.
     * 
     * @param cx Current x coordinate
     * @param cy Current y coordinate 
     * @return 
     */
    @Override
    public int estimateRest(int cx, int cy) 
    {
        return 0;
    }
    
}
