package impcity.ogl;

import java.util.HashMap;

/**
 * This is not thread safe!
 * 
 * @author Hj. Malthaner
 */
public class DrawableVerticalList
{
    private final HashMap<Integer, DrawableLink> map = new HashMap<>();
    
    /** available nodes */
    private DrawableLink freeList;
    
    /** used nodes */
    private DrawableLink usedList;
    
    
    public DrawableVerticalList()
    {
    }
    
    public void clear()
    {
        while(usedList != null)
        {
            DrawableLink node = usedList;
            usedList = node.nextInList;
            
            node.nextInList = freeList;
            freeList = node;
        }
        
        
        map.clear();
    }

    /**
     *
     * @param drawable The thing to draw
     * @param x x coordinate on map
     * @param y y coordinate on map
     * @param id Element id
     */
    public void addDrawable(Drawable drawable, int x, int y, int id)
    {
        int key = (y << 15) + x;
        
        // System.err.println("x=" + x + " y=" + y + " key=" + key);
        DrawableLink link = map.get(key);
        
        if(link == null)
        {
            link = allocateDrawableLink();
            link.drawable = drawable;
            link.id = id;
            link.next = null;
            map.put(key, link);
        }
        else
        {
            DrawableLink newLink = allocateDrawableLink();
            newLink.drawable = drawable;
            newLink.id = id;
            newLink.next = link.next;
            link.next = newLink;
        }
    }

    public void display(IsoDisplay display, int x, int y)
    {
        int key = (y << 15) + x;
        DrawableLink link = map.get(key);
        
        while(link != null)
        {
            link.drawable.display(display, x, y);
            link = link.next;
        }        
    }

    public DrawableLink get(int x, int y)
    {
        int key = (y << 15) + x;
        return map.get(key);
    }

    private DrawableLink allocateDrawableLink() 
    {
        if(freeList == null)
        {
            freeList = new DrawableLink();
        }
        
        DrawableLink result = freeList;
        freeList = result.nextInList;
        
        result.nextInList = usedList;
        usedList = result;
        
        return result;
    }
    
    
    public static class DrawableLink
    {
        public Drawable drawable;
        public int id;
        public DrawableLink next;
        public DrawableLink nextInList;
    }
}
