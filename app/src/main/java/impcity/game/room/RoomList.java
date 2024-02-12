package impcity.game.room;

import impcity.game.ImpCity;
import impcity.game.map.Map;
import impcity.utils.Pair;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.logging.Level;
import java.util.logging.Logger;


public class RoomList
{
    private static final Logger LOG = Logger.getLogger(RoomList.class.getName());

    private final ArrayList<Room> rooms = new ArrayList<>(32);


    public void clear()
    {
        rooms.clear();
    }


    public void add(Room room)
    {
        rooms.add(room);
    }
    
    
    public int size()
    {
        return rooms.size();
    }


    public Point scanForResources(Map map,
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


    /**
     * See if this is a new room or if it is
     * an extension of a room, make a new room if needed,
     * then add the square.
     * 
     * @param p The square to add.
     * @return The existing or newly made room with the new square
     */
    public Room addNewSquare(Point p)
    {
        ArrayList <Room> neighbors = new ArrayList<>();
        
        for(Room room : rooms)
        {            
            for(Point rp : room.squares)
            {
                int d = Math.abs(rp.x - p.x) + Math.abs(rp.y - p.y);

                if(d < 2 * Map.SUB && !neighbors.contains(room)) 
                {
                    LOG.log(Level.INFO, "Found a neighboring room");
                    neighbors.add(room);
                }
            }            
        }
        
        
        Room result;
        
        if(neighbors.isEmpty())
        {
            LOG.log(Level.INFO, "No neighboring rooms, creating a new one");
            result = new Room();
            rooms.add(result);
        }
        else
        {
            result = neighbors.get(0);

            // if there are more neighbors, we need to merge them
            for(int i=1; i<neighbors.size(); i++)
            {
                Room neighbor = neighbors.get(i);
                LOG.log(Level.INFO, "Merging neighboring room");
                result.squares.addAll(neighbor.squares);
                rooms.remove(neighbor);
            }
        }

        result.squares.add(p);

        return result;
    }


    /**
     * Remove a square from a room in the list. This operation can split
     * the room into several.
     * @param p The coordinates
     * @param action Called for each part of the remaining room
     */
    public void removeSquareAndRebuild(ImpCity game, Map map, Point p, List<Point> squares, int floor, Furnisher action, boolean allPoints)
    {
        // we rebuild the room list by adding square by square
        rooms.clear();

        // except this one
        squares.remove(p);

        // ready to go
        for(Point square : squares)
        {
            addNewSquare(square);
        }

        LOG.log(Level.INFO, "Rebuild results in " + rooms.size() + " rooms. Now furnishing them.");

        // now we have new rooms. We need to furnish them again
        for(Room room : rooms)
        {
            room.refurnish(game, map, floor, action, allPoints);
        }
    }


    public Pair<Room, Integer> findClosestRoom(Point p)
    {
        int dmax = 999;
        Room bestRoom = null;
        for(Room room : rooms)
        {
            for(Point rp : room.squares)
            {
                int d = Math.abs(rp.x - p.x) + Math.abs(rp.y - p.y);

                if(d < dmax)
                {
                    dmax = d;
                    bestRoom = room;
                }
            }
        }

        return new Pair(bestRoom, dmax);
    }
}
