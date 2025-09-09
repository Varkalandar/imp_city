package impcity.game.map;

/**
 *
 * @author Hj. Malthaner
 */
public interface LocationCallback 
{
    /**
     * @param x current x position
     * @param y current y position
     * @return false to keep traversing, true to stop traversing
     */
    public boolean visit(int x, int y);
}
