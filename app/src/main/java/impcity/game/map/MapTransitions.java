package impcity.game.map;

/**
 *
 * @author Hj. Malthaner
 */
public class MapTransitions
{
    int manhattenDistance(int sx, int sy, int dx, int dy)
    {
        int xd = Math.abs(dx - sx);
        int yd = Math.abs(dy - sy);
        
        return Math.max(xd, yd);
    }
}
