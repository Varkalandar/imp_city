package impcity.game.jobs;

import impcity.game.Features;
import impcity.game.ImpCity;
import impcity.game.Sounds;
import java.awt.Point;
import impcity.game.mobs.Mob;
import impcity.game.map.Map;

/**
 * Excavate a tile.
 * 
 * @author Hj. Malthaner
 */
public class JobExcavate extends AbstractJob
{
    private final ImpCity game;
    
    public JobExcavate(ImpCity game, int x, int y)
    {
        super(new Point(x, y));
        this.game = game;
    }

    @Override
    public boolean isValid(Mob worker)
    {
        int rasterI = location.x/Map.SUB*Map.SUB;
        int rasterJ = location.y/Map.SUB*Map.SUB;
     
        Map map = worker.gameMap;
        int mark = map.getItem(rasterI+Map.SUB/2, rasterJ+Map.SUB/2);
        
        return mark == Features.MINING_MARK;
    }
    
    @Override
    public void execute(Mob worker)
    {
        int rasterI = location.x/Map.SUB*Map.SUB;
        int rasterJ = location.y/Map.SUB*Map.SUB;
     
        Map map = worker.gameMap;
        int mark = map.getItem(rasterI+Map.SUB/2, rasterJ+Map.SUB/2);
        
        // Hajo: see if the square is still marked for digging
        if(mark == Features.MINING_MARK)
        {
            map.setItem(rasterI+Map.O_BLOCK, rasterJ+Map.O_BLOCK, 0);      // remove wall block
            map.setItem(rasterI+Map.SUB/2, rasterJ+Map.SUB/2, 0);  // remove mining symbol

            game.jobQueue.add(new JobClaimGround(game, location.x, location.y), JobQueue.PRI_NORM);
            
            game.soundPlayer.play(Sounds.DIG_SQUARE, 0.6f, 1.0f);
        }        

    }

    @Override
    public String toString()
    {
        return "Job: digging at " + location.x + ", " + location.y;
    }
}
