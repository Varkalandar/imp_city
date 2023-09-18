package impcity.game.map;

import rlgamekit.pathfinding.PathDestination;

/**
 *
 * @author Hj. Malthaner
 */
public class LocationPathDestination implements PathDestination
{
    private final int x, y;
    private final int range;
    private final Map map;
    
    public LocationPathDestination(Map map, int x, int y, int range)
    {
        this.map = map;
        this.x = x;
        this.y = y;
        this.range = range;
    }

    @Override
    public boolean isDestinationReached(int x, int y) 
    {
        // Hajo: close enough?
        return Math.abs(this.x - x) + Math.abs(this.y - y) <= range;
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
        int xd = Math.abs(cx - x);
        int yd = Math.abs(cy - y);
        
        return Math.max(xd, yd);
    }
}
