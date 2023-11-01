package impcity.game.map;

/**
 *
 * @author Hj. Malthaner
 */
public class ClearItems implements LocationCallback
{
    private final Map map;
    
    public ClearItems(Map map)
    {
        this.map = map;
    }

    @Override
    public boolean visit(int x, int y)
    {
        map.setItem(x, y, 0);

        return false;
    }
    
}
