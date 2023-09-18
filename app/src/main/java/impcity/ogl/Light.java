package impcity.ogl;

/**
 *
 * @author Hj. Malthaner
 */
public class Light 
{
    public final int x;
    public final int y;
    public final int yoff;   // lights on walls are "up" from the floor
    public final int type;
    public final int argb;
    public final double size;
    
    public Light(int x, int y, int yoff, int type, int argb, double size)
    {
        this.x = x;
        this.y = y;
        this.yoff = yoff;
        this.type = type;
        this.argb = argb;
        this.size = size;
    }

    @Override
    public int hashCode() 
    {
        int hash = 5;
        hash = 97 * hash + this.x;
        hash = 97 * hash + this.y;
        hash = 97 * hash + this.type;
        hash = 97 * hash + this.argb;
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.size) ^ (Double.doubleToLongBits(this.size) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object other)
    {
        boolean result = false;
        if(other instanceof Light)
        {
            Light otherLight = (Light)other;
            result = (x == otherLight.x) && (y == otherLight.y);
        }
        
        return result;
    }
}
