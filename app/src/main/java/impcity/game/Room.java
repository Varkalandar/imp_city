package impcity.game;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Hj. Malthaner
 */
public class Room
{
    public final Set <Point> squares;
    
    
    public Room()
    {
        squares = new HashSet<Point>();
    }
}
