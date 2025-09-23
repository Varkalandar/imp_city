package impcity.game.room;

import impcity.game.Features;
import impcity.game.ImpCity;
import impcity.game.map.LocationCallback;
import impcity.game.map.LocationVisitor;
import impcity.game.map.Map;
import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.IntPredicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Hj. Malthaner
 */
public class Room
{
    private static final Logger LOG = Logger.getLogger(Room.class.getName());

    public final Set <Point> squares;
    public final HashMap <Point, Integer> distances;
    public Product product;
    
    
    public Room()
    {
        squares = new HashSet<>();
        distances = new HashMap<>();
        product = Product.COPPER_COINS;
    }

    
    /**
     * Calculates the distance from the squares root point to the nearest wall.
     * The squares root point is top-left
     * @param map The map to scan
     * @param floor The room floor type
     */
    public void calculateBorderDistances(Map map, int floor)
    {
        distances.clear();
        int dmax = 1;
        
        for(Point p : squares)
        {
            boolean open = true;
            
            for(int i=-dmax; i<+dmax; i++)
            {
                for(int j=-dmax; j<+dmax; j++)
                {
                    int mf = map.getFloor(p.x + i*Map.SUB, p.y + j*Map.SUB);
                    // System.err.println("mf=" + mf + " -> " + floor);
                    open &= (mf >= floor && mf < floor + 3);
                }                
            }
            
            distances.put(p, open ? 1 : 0);
            
            // System.err.println("" + p + " -> " + distances.get(p));
        }
    }


    public void forAllPoints(LocationVisitor visitor)
    {
        for(Point p : squares)
        {
            visitor.visit(p.x, p.y);
        }
    }


    public void forAllInnerPoints(LocationVisitor visitor)
    {
        for(Point p : squares)
        {
            if(distances.get(p) > 0)
            {
                visitor.visit(p.x, p.y);
            }
        }
    }

    
    public Point scanForResources(Map map, IntPredicate resources)
    {
        for(Point p : squares)
        {
            for(int j=0; j<Map.SUB; j++)
            {
                for(int i=0; i<Map.SUB; i++)
                {
                    int n = map.getItem(p.x + i, p.y + j) & Map.F_IDENT_MASK;

                     // todo: check for correct resource
                    if(resources.test(n))
                    {
                        return new Point(p.x + i, p.y + j);
                    }
                }
            }
        }
        
        return null;
    }


    public void refurnish(ImpCity game, Map map, int floor, Furnisher action, boolean allPoints)
    {
        LOG.log(Level.INFO, "Room has now " + squares.size() + " squares, furnishing all points=" + allPoints);

        calculateBorderDistances(map, floor);
        forAllPoints((x, y) -> {game.clearItems(map, x, y, Map.SUB, Features.keepTreasureFilter);});
        if(allPoints)
        {
        	forAllPoints((x, y) -> {action.furnish(map, x, y);});
        }
        else
        {
        	forAllInnerPoints((x, y) -> {action.furnish(map, x, y);});
        }
        forAllPoints((x, y) -> {game.refreshPillars(x, y);});
    }
}
