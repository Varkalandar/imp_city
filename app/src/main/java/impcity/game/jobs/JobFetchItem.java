package impcity.game.jobs;

import impcity.game.ImpCity;
import impcity.game.ai.MobStats;
import java.awt.Point;
import impcity.game.mobs.Mob;

/**
 * Transport something.
 * 
 * @author Hj. Malthaner
 */
public class JobFetchItem extends AbstractJob
{
    private final ImpCity game;
    private final int item;
    
    public JobFetchItem(ImpCity game, int x, int y, int item)
    {
        super(new Point(x, y));
        this.game = game;
        this.item = item;
    }

    @Override
    public boolean isValid(Mob worker)
    {
        return worker.gameMap.getItem(location.x, location.y) == item;
    }
    
    @Override
    public void execute(Mob worker)
    {
        if(worker.gameMap.getItem(location.x, location.y) == item)
        {
            worker.stats.setCurrent(MobStats.CARRY, item);
            worker.visuals.setBubble(item);
            worker.gameMap.setItem(location.x, location.y, 0);
        }
        else
        {
            // Hajo: the expected item was not here anymore, but what now?
            // -> AI must take care of the case.
        }
    }

    @Override
    public String toString()
    {
        return "Job: fetch item from " + location.x + ", " + location.y;
    }
}
