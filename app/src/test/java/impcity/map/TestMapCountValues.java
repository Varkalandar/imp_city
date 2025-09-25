package impcity.map;

import impcity.game.map.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author hjm
 */
public class TestMapCountValues 
{
    @Test
    public void testMapCountValues()
    {
        int width = 100 * Map.SUB;
        int height = 100 * Map.SUB;
        
        Map map = new Map(width, height);
        
        for(int i=0; i<100000; i++)
        {
            int x = (int)(Math.random() * width);
            int y = (int)(Math.random() * height);
            int v = (int)(Math.random() * 255);
            
            map.setCount(x, y, v);
            
            int r = map.getCount(x, y);
            
            if(r != v)
            {
                System.out.println("Count value mismatch at " + x + ", " + y +
                                   " stored=" + v + " read=" + r);
                Assertions.assertEquals(v, r);
            }
        }
    }
}
