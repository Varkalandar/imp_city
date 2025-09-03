package impcity.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

/**
 *
 * @author Hj. Malthaner
 */
public interface MouseHandler
{
    public void processMouse();

    public void read(BufferedReader reader) throws IOException;
    public void write(Writer writer) throws IOException;
}
