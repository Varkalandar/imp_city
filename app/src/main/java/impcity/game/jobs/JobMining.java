package impcity.game.jobs;

import impcity.game.Features;
import impcity.game.ImpCity;
import impcity.game.ai.MobStats;
import java.awt.Point;
import impcity.game.mobs.Mob;
import impcity.game.map.Map;

/**
 * Dig gold from a tile.
 * 
 * @author Hj. Malthaner
 */
public class JobMining extends AbstractJob
{
    private final ImpCity game;
    
    public JobMining(ImpCity game, int x, int y)
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
        int block = map.getItem(rasterI + Map.O_BLOCK, rasterJ + Map.O_BLOCK) - Map.F_DECO;
        int ground = map.getFloor(rasterI, rasterJ);
        
        return Features.canBeMined(ground, block);
    }
    
    @Override
    public void execute(Mob worker)
    {
        int rasterI = location.x/Map.SUB*Map.SUB;
        int rasterJ = location.y/Map.SUB*Map.SUB;
     
        Map map = worker.gameMap;
        
        // Hajo: see if the square is still marked for digging
        if(isValid(worker))
        {
            worker.stats.setCurrent(MobStats.GOLD, 0);                
            worker.stats.setCurrent(MobStats.CARRY, 0);
            
            // Hajo: what did we mine actually?
            int block = map.getItem(rasterI + Map.O_BLOCK, rasterJ + Map.O_BLOCK) - Map.F_DECO;
            if(block >=  Features.I_GOLD_MOUND && block <  + Features.I_GOLD_MOUND + 3)
            {
                // worker.stats.setCurrent(MobStats.GOLD, 100);                
            }
            else if(block >= Features.I_COPPER_ORE_MOUND && block <  + Features.I_COPPER_ORE_MOUND + 3)
            {
                worker.stats.setCurrent(MobStats.CARRY, Features.I_COPPER_ORE);                
            }            
            else if(block >= Features.I_TIN_ORE_MOUND && block <  + Features.I_TIN_ORE_MOUND + 3)
            {
                worker.stats.setCurrent(MobStats.CARRY, Features.I_TIN_ORE); 
            }
            else if(block == Features.I_MINERAL_BLOCK)
            {
                worker.stats.setCurrent(MobStats.CARRY, Features.I_MINERAL);
            }
        }
    }
    
    @Override
    public String toString()
    {
        return "Job: Mining at " + location.x + ", " + location.y;
    }
}
