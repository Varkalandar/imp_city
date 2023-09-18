package impcity.game.ai;

import impcity.game.Features;
import impcity.game.map.Map;
import rlgamekit.pathfinding.PathSource;

/**
 *
 * @author Hj. Malthaner
 */
public class WayPathSource implements PathSource
{
    private final Map map;
    private final int size;
    
    /**
     * Search a path, wide enough for a creature.
     * 
     * @param map The map to use.
     * @param size Creature size (radius).
     */
    public WayPathSource(Map map, int size)
    {
        this.map = map;
        this.size = size;
    }

    @Override
    public boolean isMoveAllowed(int sx, int sy, int dx, int dy) 
    {
        for(int y = dy-size; y <= dy+size; y++)
        {
            for(int x = dx-size; x <= dx+size; x++)
            {
                int ground = map.getFloor(x - (x % Map.SUB), y - (y % Map.SUB));
                
                if((ground >= Features.GROUND_IMPASSABLE && ground < Features.GROUND_IMPASSABLE+3) ||
                   (ground >= Features.GROUND_LIGHT_SOIL && ground < Features.GROUND_LIGHT_SOIL+3) ||     
                    map.isMovementBlocked(x, y))
                {
                    return false;
                }
            }
        }
        
        return true;
    }
    
}
