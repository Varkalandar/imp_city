package impcity.game.ai;

import impcity.game.mobs.Mob;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

/**
 *
 * @author Hj. Malthaner
 */
public interface Ai
{
    /**
     * This method is called each frame while the 
     * creature has no path (path == null).
     * 
     * @param mob The creature which needs to think
     */
    public void think(Mob mob);
    
    
    /**
     * This is called if the AI is called to fight
     * ar the given location.
     * @param p the location of the alarm
     */
    public void alarm(Point p);
    
    
    /**
     * This method is called each frame while the 
     * creature has no path (path == null). It is called
     * right after the think() method.
     * 
     * @param mob The creature which needs to think
     */
    public void findNewPath(Mob mob);

    /**
     * This method is called after each step on the path
     * 
     * @param mob The creature which needs to think
     */
    public void thinkAfterStep(Mob mob);

    public void write(Writer writer) throws IOException;
    
    public void read(BufferedReader reader) throws IOException;

    public Point getHome();

    public boolean isLair(Mob mob, int x, int y);
}
