package impcity.game.ai;

import impcity.game.species.SpeciesDescription;
import impcity.game.map.*;
import rlgamekit.pathfinding.PathDestination;

/**
 *
 * @author Hj. Malthaner
 */
public class LairPathDestination implements PathDestination
{
    private final int ground;
    private final Map map;
    private final SpeciesDescription desc;
    
    public LairPathDestination(Map map, SpeciesDescription desc, int ground)
    {
        this.map = map;
        this.ground = ground;
        this.desc = desc;
    }

    @Override
    public boolean isDestinationReached(int x, int y) 
    {
        boolean ok = true;

        for(int dy = -desc.lairSize; dy <= desc.lairSize && ok; dy++)
        {
            for(int dx = -desc.lairSize; dx <= desc.lairSize && ok; dx++)
            {
                int xpos = dx + x;
                int ypos = dy + y;

                // Hajo: lair anchor points are shifted up to get 
                // visibility right 
                int n = map.getItem(xpos - desc.lairSize + 1, ypos - desc.lairSize + 1);
                ok &= (n == 0) && !map.isPlacementBlocked(xpos, ypos);

                if(ok)
                {
                    int rasterI = xpos/Map.SUB*Map.SUB;
                    int rasterJ = ypos/Map.SUB*Map.SUB;
                    n = map.getFloor(rasterI, rasterJ);
                    ok &= (n >= ground) && (n < ground + 3);
                }
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
