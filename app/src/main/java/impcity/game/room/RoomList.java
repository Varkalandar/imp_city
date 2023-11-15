package impcity.game.room;

import impcity.game.map.LocationCallback;
import impcity.game.map.Map;
import impcity.utils.Pair;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.logging.Level;
import java.util.logging.Logger;


public class RoomList
{
    private static final Logger LOG = Logger.getLogger(RoomList.class.getName());

    private ArrayList<Room> rooms = new ArrayList<>(32);


    public void clear()
    {
        rooms.clear();
    }


    public void add(Room room)
    {
        rooms.add(room);
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
     * see if this is a new room or if it is
     * an extension of a room
     */
    public Room addNewSquare(Point p)
    {
        Pair <Room, Integer> best = findClosestRoom(p);

        Room bestRoom = best.v1;
        int dmax = best.v2;

        Room result;

        if(dmax > Map.SUB)
        {
            // Hajo: this is a new room
            Room room = new Room();
            room.squares.add(p);
            rooms.add(room);
            result = room;
        }
        else if(bestRoom != null)
        {
            bestRoom.squares.add(p);
            result = bestRoom;
        }
        else
        {
            LOG.log(Level.SEVERE, "Algorithm error!");
            result = null;
        }

        return result;
    }


    /**
     * Remove a square from a room in the list. This operation can split
     * the room into several.
     * @param p The coordinates
     * @param action Called for each part of the remaining room
     */
    public void removeSquare(Point p, LocationCallback action)
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
