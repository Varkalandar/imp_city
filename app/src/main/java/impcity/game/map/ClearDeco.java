package impcity.game.map;

/**
 *
 * @author Hj. Malthaner
 */
public class ClearDeco implements LocationCallback
{
    private final Map map;
    
    public ClearDeco(Map map)
    {
        this.map = map;
    }

    @Override
    public boolean visit(int x, int y)
    {
        int n = map.getItem(x, y);
        
        if((n & Map.F_DECO) != 0)
        {
            map.setItem(x, y, 0);
        }
        
        return false;
    }
    
}
