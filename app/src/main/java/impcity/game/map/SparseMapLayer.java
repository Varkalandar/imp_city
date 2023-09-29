package impcity.game.map;

import java.util.HashMap;

/**
 *
 * @author Hj. Malthaner
 * @param <E> Type of the objects in this map layer
 */
public class SparseMapLayer <E>
{
    // private final HashMap <Integer, E> map = new HashMap<>();
    private final SparseArray <E> map = new SparseArray<>(2000);

    public SparseMapLayer(int width, int height)
    {
    }

    public E get(int x, int y)
    {
        int key = (y << 15) + x;
        return map.get(key);
    }

    public void remove(int x, int y)
    {
        int key = (y << 15) + x;
        map.remove(key);
    }
    
    public void set(int x, int y, E value)
    {
        int key = (y << 15) + x;
        map.put(key, value);
    }

    void resize(int width, int height) 
    {
        // ???
    }
}
