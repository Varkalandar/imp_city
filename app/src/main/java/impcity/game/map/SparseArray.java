package impcity.game.map;

/**
 *
 * @author hjm
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
            p++;            
        }    
        
        keys[p] = index;
        values[p] = value;
    }
    
    public E get(int index)
    {
        int p = index % capacity;
        
        while(keys[p] != Integer.MIN_VALUE && keys[p] != index) p++;
        
        return (E)values[p];
    }

    public void remove(int index)
    {
        int p = index % capacity;

        while(keys[p] != Integer.MIN_VALUE && keys[p] != index) p++;
        
        keys[p] = Integer.MIN_VALUE;
        values[p] = null;        
    }
}
