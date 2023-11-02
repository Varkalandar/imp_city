package impcity.game.quests;

import java.util.Random;

/**
 * This class generates random integers in a given range,
 * with the exception of the n previously generated values.
 * 
 * @author hjm
 */
public class NonRepetitiveRng 
{
    private final Random rng;
    private final int [] buffer;
    private int counter;
    
    public NonRepetitiveRng(int memory, long seed)
    {
        this.rng = new Random(seed);
        this.buffer = new int[memory];
        this.counter = 0;
        
        // init the buffer
        for(int i=0; i<buffer.length; i++)
        {
            buffer[i] = -1;
        }
    }
    
    
    public int random(int bound)
    {
        boolean ok;
        int n;
        
        do
        {
            n = rng.nextInt(bound);
            ok = isAllowed(n);
            
            if(ok) 
            {
                // block the number
                buffer[counter % buffer.length] = n;
                counter = (counter + 1) & 0xFFFF;
            }
        }
        while(!ok);
        
        return n;
    }
    
    private boolean isAllowed(int n)
    {
        boolean ok = true;
        for(int i=0; i<buffer.length && ok; i++)
        {
            if(buffer[i] == n) ok = false;
        }

        return ok;
    }
}
