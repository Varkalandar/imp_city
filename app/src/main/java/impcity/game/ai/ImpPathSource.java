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
     * @param mob The mob to use for pathfinding.
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
        for(int y = dy-size; y <= dy+size; y++)
        {
            for(int x = dx-size; x <= dx+size; x++)
            {
                int rasterI = x - (x % Map.SUB);
                int rasterJ = y - (y % Map.SUB);

                int ground = map.getFloor(rasterI, rasterJ);
                int item = map.getItem(rasterI+Map.O_BLOCK, rasterJ+Map.O_BLOCK) & 0xFFFF;
                
                if((ground >= Features.GROUND_IMPASSABLE && ground < Features.GROUND_IMPASSABLE+3) ||
                    map.isMovementBlocked(x, y) ||
                    ((item >= Features.I_STEEP_EARTH_BLOCK && item < Features.I_STEEP_EARTH_BLOCK+3) ||
                    item == Features.I_GRAPHITE_BLOCK ||
                    item == Features.I_IRON_ORE_BLOCK ||
                    item == Features.I_MINERAL_BLOCK ||
                    item == Features.I_COPPER_ORE_MOUND ||
                    item == Features.I_TIN_ORE_MOUND)
                   )
                {
                    return false;
                }
            }
        }
        
        return true;
    }
    
}
