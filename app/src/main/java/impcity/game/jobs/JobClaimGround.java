package impcity.game.jobs;

import impcity.game.Features;
import impcity.game.ImpCity;
import impcity.game.Sounds;
import java.awt.Point;
import impcity.game.mobs.Mob;
import impcity.game.map.Map;


/**
 * Claim a tile for the player. At the moment this means to set the ground to
 * poly tiles.
 * 
 * @author Hj. Malthaner
 */
public class JobClaimGround extends AbstractJob
{
    private final ImpCity game;
    

    public JobClaimGround(ImpCity game, int x, int y)
    {
        super(new Point(x, y));
        this.game = game;
    }

    
    @Override
    public void execute(Mob worker)
    {
        int rasterI = location.x/Map.SUB*Map.SUB;
        int rasterJ = location.y/Map.SUB*Map.SUB;
        
        Map map = worker.gameMap;
        map.setFloor(rasterI, rasterJ, Features.GROUND_POLY_TILES + (int)(Math.random() * 3));
        
        game.addClaimedSquare(map, rasterI, rasterJ);
        
        game.soundPlayer.play(Sounds.CLAIM_SQUARE, 0.5f, 1.0f);
    }

    
    /** 
     * Square must be free of earth or ore blocks
     * 
     * @param worker The worker intended to execute this job
     * @return True if this square can be claimed, false otherwise 
     */
    @Override
    public boolean isValid(Mob worker)
    {
        int rasterI = location.x/Map.SUB*Map.SUB;
        int rasterJ = location.y/Map.SUB*Map.SUB;
        Map map = worker.gameMap;
        int item = map.getItem(rasterI+Map.O_BLOCK, rasterJ+Map.O_BLOCK); 
        int floor = map.getFloor(rasterI, rasterJ);
        
        return item == 0 && (floor >= Features.GROUND_LIGHT_SOIL && floor < Features.GROUND_LIGHT_SOIL + 3);
    }

    
    @Override
    public String toString()
    {
        return "Job: flooring at " + location.x + ", " + location.y;
    }
}
