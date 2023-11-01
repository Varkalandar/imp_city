package impcity.game.jobs;

import impcity.game.ImpCity;
import impcity.game.ai.MobStats;
import java.awt.Point;

import impcity.game.map.Map;
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
            worker.gameMap.setItem(location.x, location.y, 0);

            // if this was a real item, the bubble must be set to the item
            // texture instead of the key

            int bubble = item;
            if((item & Map.F_ITEM) != 0)
            {
                // real item
                bubble = game.world.items.get(item & Map.F_ITEM_MASK).texId;
            }

            worker.visuals.setBubble(bubble);
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
