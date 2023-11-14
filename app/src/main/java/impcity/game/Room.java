package impcity.game;

import impcity.game.map.LocationCallback;
import impcity.game.map.Map;
import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntPredicate;

/**
 *
 * @author Hj. Malthaner
 */
public class Room
{
    /**
     * Remove a square from a room in the list. This operation can split
     * the room into several.
     * @param p The coordinates
     * @param rooms The list of room to work on
     * @param action Called for each part of the remaining room
     */
    public static void removeSquare(Point p, List<Room> rooms,
            LocationCallback action) 
    {
        Room roomKill = null;
        
        for(Room room : rooms)
        {
            if(room.squares.contains(p))
            {
                room.squares.remove(p);
                room.distances.remove(p);
                
                if(room.squares.isEmpty())
                {
                    // totally deleted
                    roomKill = room;
                }
                else
                {
                    // Todo: handle room split
                    // -> each new room must be furnished. Call the action
                    
                    Point anchor = room.squares.iterator().next();
                    action.visit(anchor.x, anchor.y);
                }
            }            
        }
        
        if(roomKill != null)
        {
            rooms.remove(roomKill);
        }
    }


    public static Point scanRoomsForResources(List<Room> rooms, Map map,
                                              IntPredicate resources,
                                              int rasterI, int rasterJ)
    {
        Point rasterP = new Point(rasterI, rasterJ);
        Point p = null;

        // scan forge rooms
        for(Room room : rooms)
        {
            if(room.squares.contains(rasterP))
            {
                // this is the room we are in -> scan it
                p = room.scanForResources(map, resources);
            }
        }

        return p;
    }

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


    public void forAllPoints(LocationCallback visitor)
    {
        for(Point p : squares)
        {
            visitor.visit(p.x, p.y);
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
}
