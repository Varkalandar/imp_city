package impcity.ui;

/**
 *
 * @author Hj. Malthaner
 */
public class TimedMessage 
{
    public String message;
    public int color;
    public long time; 
    public int x, y;

    public TimedMessage(String message, int color, int x, int y, long time)
    {
        this.message = message;
        this.color = color;
        this.x = x;
        this.y = y;
        this.time = time;
    }
}
