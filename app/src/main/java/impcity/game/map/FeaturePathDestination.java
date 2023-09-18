package impcity.game.map;

import rlgamekit.pathfinding.PathDestination;

/**
 *
 * @author Hj. Malthaner
 */
public class FeaturePathDestination implements PathDestination
{
    private final int feature;
    private final Map map;
    
    public FeaturePathDestination(Map map, int feature)
    {
        this.map = map;
        this.feature = feature + Map.F_DECO;
    }

    @Override
    public boolean isDestinationReached(int x, int y) 
    {
        int n = map.getItem(x, y);
        
        return (n == feature);
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
