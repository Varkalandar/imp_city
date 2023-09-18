package impcity.game.jobs;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

/**
 * Abstract job base class.
 * 
 * @author Hj. Malthaner
 */
public abstract class AbstractJob implements Job
{
    protected final Point location;

    protected AbstractJob(Point location)
    {
        this.location = location;
    }
    
    @Override
    public Point getLocation()
    {
        return location;
    }
    
    @Override
    public void write(Writer writer) throws IOException
    {
        writer.write("jobX=" + location.x + "\n");
        writer.write("jobY=" + location.y + "\n");        
    }
    
    @Override
    public void read(BufferedReader reader) throws IOException
    {
        String line;
        line = reader.readLine();
        location.x = Integer.parseInt(line.substring(5));
        line = reader.readLine();
        location.y = Integer.parseInt(line.substring(5));
        
    }
}
