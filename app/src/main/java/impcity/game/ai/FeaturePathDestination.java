package impcity.game.ai;

import java.util.HashSet;
import java.util.Set;
import impcity.game.map.*;
import rlgamekit.pathfinding.PathDestination;

/**
 *
 * @author Hj. Malthaner
 */
public class FeaturePathDestination implements PathDestination
{
    private final Set<Integer> features;
    private final int ground;
    private final Map map;
    private final int featureSize;
    private final int groundSize;
    
    public FeaturePathDestination(Map map, int feature, int featureSize, int ground, int groundSize)
    {
        this(map, makeSet(feature), feature, ground, groundSize);
    }

    FeaturePathDestination(Map map, Set<Integer> features, int featureSize, int ground, int groundSize)
    {
        this.map = map;
        this.ground = ground;
        this.groundSize = groundSize;
        this.features = features;
        this.featureSize = featureSize;
    }

    @Override
    public boolean isDestinationReached(int x, int y) 
    {
        boolean ok = true;
        
        if(ok && features.size() > 0)
        {
            for(int dy = -featureSize; dy<=featureSize && ok; dy++)
            {
                for(int dx = -featureSize; dx<=featureSize && ok; dx++)
                {
                    int xpos = dx + x;
                    int ypos = dy + y;
                    int n = map.getItem(xpos, ypos);
                    ok &= features.contains(n);
                }
            }
            for(int dy = -groundSize; dy<=groundSize && ok; dy++)
            {
                for(int dx = -groundSize; dx<=groundSize && ok; dx++)
                {
                    int xpos = dx + x;
                    int ypos = dy + y;

                    int rasterI = xpos/Map.SUB*Map.SUB;
                    int rasterJ = ypos/Map.SUB*Map.SUB;
                    int n = map.getFloor(rasterI, rasterJ);
                    ok &= (n >= ground) && (n < ground + 3);
                }
            }
        }
        
        
        return ok;
    }
    
    private static Set <Integer> makeSet(int feature)
    {
        HashSet<Integer> set = new HashSet<Integer>();
        set.add(feature);
        return set;
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
