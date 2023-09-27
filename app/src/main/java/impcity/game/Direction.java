package impcity.game;

import java.awt.Point;

/**
 * Conversion methods between clock-directions and vector based
 * directions.
 * 
 * @author Hj. Malthaner
 */
public class Direction 
{
    public static final Point [] vectors;
    
    static
    {
        vectors = new Point [8];
        
        vectors[0] = new Point(-1, 1);
        vectors[1] = new Point( 0, 1);
        vectors[2] = new Point( 1, 1);
        vectors[3] = new Point( 1, 0);
        vectors[4] = new Point( 1,-1);
        vectors[5] = new Point( 0,-1);
        vectors[6] = new Point(-1,-1);
        vectors[7] = new Point(-1, 0);
    }
    
    public static int dirFromVector(int dx, int dy) 
    {
        int direction = 3;
        
        if(dx <= -1 && dy >= 1)
        {
            direction = 0;
        }
        else if(dx == 0 && dy >= 1)
        {
            direction = 1;
        }
        else if(dx >= 1 && dy >= 1)
        {
            direction = 2;
        }
        else if(dx >= 1 && dy == 0)
        {
            direction = 3;
        }
        else if(dx >= 1 && dy <= -1)
        {
            direction = 4;
        }
        else if(dx == 0 && dy <= -1)
        {
            direction = 5;
        }
        else if(dx <= -1 && dy <= -1)
        {
            direction = 6;
        }
        else if(dx <= -1 && dy == 0)
        {
            direction = 7;
        }
        
        return direction;
    }
    
}
