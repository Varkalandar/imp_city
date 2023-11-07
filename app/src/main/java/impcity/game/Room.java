package impcity.game;

import impcity.game.map.LocationCallback;
import impcity.game.map.Map;
import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Hj. Malthaner
 */
public class Room
{
    public final Set <Point> squares;
    public final HashMap <Point, Integer> distances;
    
    
    public Room()
    {
        squares = new HashSet<>();
        distances = new HashMap<>();
    }

    
    /**
     * Calculates the distance from the squares root point to the nearest wall.
     * The squares root point is top-left
     * @param map The map to scan
     * @param floor The room floor type
     */
    void calculateBorderDistances(Map map, int floor) 
    {
        // System.err.println("-----");
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


    public void forAllInnerPoints(LocationCallback visitor)
    {
        for(Point p : squares)
        {
            if(distances.get(p) > 0)
            {
                visitor.visit(p.x, p.y);
            }
        }
    }
}
