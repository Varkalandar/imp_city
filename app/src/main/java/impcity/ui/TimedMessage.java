package impcity.ui;

/**
 *
 * @author Hj. Malthaner
 */
public class TimedMessage 
{
    public String message;
    public int color;
    public int x, y;
    public long time; 
    public double factor;
    
    public TimedMessage(String message, int color, int x, int y, long time, double factor)
    {
        this.message = message;
        this.color = color;
        this.x = x;
        this.y = y;
        this.time = time;
        this.factor = factor;
    }
}
