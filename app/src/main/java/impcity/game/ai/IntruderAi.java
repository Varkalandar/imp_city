package impcity.game.ai;

import impcity.game.Clock;
import impcity.game.ImpCity;
import impcity.game.map.Map;
import impcity.game.map.LocationPathDestination;
import impcity.game.mobs.Mob;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Logger;
import rlgamekit.objects.Registry;
import rlgamekit.pathfinding.Path;

/**
 *
 * @author hjm
 */
public class IntruderAi extends AiBase 
{
    private static final Logger LOG = Logger.getLogger(CreatureAi.class.getName());
    private static final int THINK_COOLDOWN = 300;

    public enum Goal
    {
        FIND_CORE, GO_CORE, FIGHT,
    }

    private Goal goal;
    private long thinkTime;
    private long lastThinkTime;
    private long pathTime;
    private final ImpCity game;

    public IntruderAi(ImpCity game)
    {
        this.game = game;
        this.goal = Goal.FIND_CORE;
        this.thinkTime = Clock.time() + (int)(Math.random() * 4000);
        this.pathTime = Clock.time() + (int)(Math.random() * 4000);

        this.lastThinkTime = Clock.time();
    }
    
    
    /**
     * Delay the next thinking by at least this many milliseconds
     * @param milliseconds The desired delay
     */
    public void delayThinking(int milliseconds)
    {
        thinkTime = Clock.time() + milliseconds;        
    }


    @Override
    public void think(Mob mob, Registry<Mob> mobs) {
        // Overrun? restore
        mob.gameMap.setMob(mob.location.x, mob.location.y, mob.getKey());
        
        // Is it time to think yet?
        if(thinkTime >= Clock.time())
        {
            // Not yet, keep a cool head, erm CPU
            // System.err.println("Mob=" + mob.getKey() + " AI skips thinking.");
            return;
        }
        else
        {
            // System.err.println("Mob=" + mob.getKey() + " AI thinks.");
            delayThinking(THINK_COOLDOWN);
        }
    }

    
    @Override
    public void findNewPath(Mob mob, Registry<Mob> mobs) 
    {
        if(goal == Goal.FIND_CORE)
        {
            Map map = mob.gameMap;
            
            Point p = Map.randomCirclePoint(game.coreLocation.x, game.coreLocation.y, Map.SUB * 3 / 4);
            
            Path path = new Path();
            boolean ok = 
                path.findPath(new WayPathSource(map, 3, false),
                              new LocationPathDestination(p.x, p.y, 0),
                              mob.location.x, mob.location.y);
            
            if(ok)
            {
                mob.setPath(path);
                goal = Goal.GO_CORE;
            }
            else
            {
                LOG.info("Intruder #" + mob.getKey() + " cannot find a path to the dungeon core");
            }
        }
    }

    
    @Override
    public void thinkAfterStep(Mob mob, Registry<Mob> mobs) 
    {
    }

    
    @Override
    public void write(Writer writer) throws IOException 
    {
        writer.write("goal=" + goal + "\n");
    }

    
    @Override
    public void read(BufferedReader reader) throws IOException {
        String line;
        line = reader.readLine();
        goal = Goal.valueOf(line.substring(5));
        
        this.lastThinkTime = Clock.time();
    }    
}
