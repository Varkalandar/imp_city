package impcity.game.ai;

import impcity.game.Features;
import impcity.game.map.Map;
import impcity.game.mobs.Mob;
import rlgamekit.pathfinding.PathSource;

/**
 *
 * @author Hj. Malthaner
 */
public class ImpPathSource implements PathSource
{
    private final Map map;
    private final int size;
    private int startI, startJ;
    /**
     * Search a path, wide enough for a creature.
     * 
     * @param map The map to use.
     * @param size Creature size (radius).
     */
    public ImpPathSource(Mob mob, int size)
    {
        this.map = mob.gameMap;
        this.size = size;
        
        startI = mob.location.x;
        startJ = mob.location.y;
        
    }

    @Override
    public boolean isMoveAllowed(int sx, int sy, int dx, int dy) 
    {
        // Hajo: on the starting square we allow all moves ...
        // somehow the imp got there, so we need to allow it to
        // move away again

        if(Math.abs(sx - startI) < Map.SUB && Math.abs(sy - startJ) < Map.SUB)
        {
            return true;
        }

        for(int y = dy-size; y <= dy+size; y++)
        {
            for(int x = dx-size; x <= dx+size; x++)
            {
                int rasterI = x - (x % Map.SUB);
                int rasterJ = y - (y % Map.SUB);
                
                
                int ground = map.getFloor(rasterI, rasterJ);
                int item = map.getItem(rasterI, rasterJ) & 0xFFFF;
                
                if((ground >= Features.GROUND_IMPASSABLE && ground < Features.GROUND_IMPASSABLE+3) ||
                    map.isMovementBlocked(x, y) ||
                    ((item >= Features.I_STEEP_EARTH_BLOCK && item < Features.I_STEEP_EARTH_BLOCK+3) ||
                        item == Features.I_GOLD_MOUND || item == Features.I_COPPER_ORE_MOUND || item == Features.I_TIN_ORE_MOUND)
                   )
                {
                    return false;
                }
            }
        }
        
        return true;
    }
    
}
