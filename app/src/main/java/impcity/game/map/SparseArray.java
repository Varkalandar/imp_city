package impcity.game.map;

import java.util.NoSuchElementException;

/**
 *
 * @author Hj. Malthaner
 */
public class SparseArray <E> 
{
    private final int[] keys;
    private final Object[] values;
    private final int capacity;
    
    public SparseArray(int capacity)
    {
        keys = new int[capacity];
        values = new Object[capacity];
        this.capacity = capacity;
        
        for(int i=0; i<capacity; i++)
        {
            keys[i] = Integer.MIN_VALUE;
        }
    }

    public void put(int index, E value)
    {
        int p = index % capacity;
        
        // find a free slot
        while(keys[p] != Integer.MIN_VALUE && keys[p] != index) 
        {
            System.out.println("Collision in set. i=" + index + " p=" + p);
            p = (p + 1) % capacity;
        }    
        
        keys[p] = index;
        values[p] = value;

        // System.out.println("SparseArray fill rate=" + getFillRate());
    }

    public E get(int index)
    {
        int p = findByIndex(index);
        return (E)values[p];
    }

    public void remove(int index)
    {
        int p = findByIndex(index);
        keys[p] = Integer.MIN_VALUE;
        values[p] = null;        
    }

    private int findByIndex(int index)
    {
        int p = index % capacity;
        int flip = 0;
        while(keys[p] != Integer.MIN_VALUE && keys[p] != index)
        {
            p++;

            if(p > capacity)
            {
                p = 0;
                flip ++;
                if(flip > 1)
                {
                    throw new NoSuchElementException("No such element, index=" + index);
                }
            }
        }

        return p;
    }


    private double getFillRate()
    {
        int count = 0;
        for(int i=0; i<capacity; i++)
        {
            if(keys[i] != Integer.MIN_VALUE)
            {
                count ++;
            }
        }

        return (double)count/(double) capacity;
    }

}
