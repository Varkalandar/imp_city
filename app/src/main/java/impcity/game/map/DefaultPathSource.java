package impcity.game.map;

import rlgamekit.pathfinding.PathSource;

/**
 *
 * @author Hj. Maltaner
 */
public class DefaultPathSource implements PathSource
{
    private final Map map;
    
    public DefaultPathSource(Map map)
    {
        this.map = map;
    }
    
    @Override
    public boolean isMoveAllowed(int sx, int sy, int dx, int dy) 
    {
        boolean ok = 
                map.getFloor(dx - (dx % Map.SUB), dy - (dy % Map.SUB)) != 0
                && map.isMovementBlocked(dx, dy) == false
                && map.getMob(dx, dy) == 0;

        // System.err.println("Checking " + dx + ", " + dy + " -> " + ok);
        
        return ok;
    }
}
