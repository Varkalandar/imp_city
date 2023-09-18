package impcity.game.jobs;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import impcity.game.mobs.Mob;

/**
 *
 * @author hjm
 */
public interface Job
{
    public void execute(Mob worker);

    public void write(Writer writer) throws IOException;
    
    public void read(BufferedReader reader) throws IOException;

    public Point getLocation();

    public boolean isValid(Mob worker);
}
