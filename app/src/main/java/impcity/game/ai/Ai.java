package impcity.game.ai;

import impcity.game.mobs.Mob;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import rlgamekit.objects.Registry;

/**
 *
 * @author Hj. Malthaner
 */
public interface Ai
{
    /**
     * This is called if the AI is called to fight
     * the given intruder.
     * @param mobKey The registry key of the intruder
     */
    public void alarm(int mobKey);


    /**
     * This method is called each frame while the 
     * creature has no path (path == null).
     * 
     * @param mob The creature which needs to think
     * @param mobs Registry of all creatures in the game.
     */
    public void think(Mob mob, Registry<Mob> mobs);
    
    /**
     * Delay the next thinking by at least this many milliseconds
     * @param milliseconds The desired delay
     */
    public void delayThinking(int milliseconds);
    
    /**
     * This method is called each frame while the 
     * creature has no path (path == null). It is called
     * right after the think() method.
     * 
     * @param mob The creature which needs to think
     * @param mobs Registry of all creatures in the game.
     */
    public void findNewPath(Mob mob, Registry<Mob> mobs);

    /**
     * This method is called after each step on the path
     * 
     * @param mob The creature which needs to think
     * @param mobs Registry of all creatures in the game.
     */
    public void thinkAfterStep(Mob mob, Registry<Mob> mobs);

    public void write(Writer writer) throws IOException;
    
    public void read(BufferedReader reader) throws IOException;

    public Point getHome();

    public boolean isLair(Mob mob, int x, int y);
}
