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
        int n = map.getItem(x, y);
        
        if(n > 0 && n < Map.F_DECO)
        {
            map.setItem(x, y, 0);
        }
        
        return false;
    }
    
}
