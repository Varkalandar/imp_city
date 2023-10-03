package impcity.game.jobs;

import impcity.game.Features;
import impcity.game.ImpCity;
import java.awt.Point;
import impcity.game.mobs.Mob;
import impcity.game.map.Map;
import impcity.ogl.Light;

/**
 * Excavate a tile.
 * 
 * @author Hj. Malthaner
 */
public class JobFrame extends AbstractJob
{
    private final ImpCity game;
    
    public static void placePillars(Map map, int rasterI, int rasterJ)
    {
        int t = map.getFloor(rasterI, rasterJ-Map.SUB);
        int tr = map.getFloor(rasterI+Map.SUB, rasterJ-Map.SUB);
        int r = map.getFloor(rasterI+Map.SUB, rasterJ);
        int br = map.getFloor(rasterI+Map.SUB, rasterJ+Map.SUB);

        int b = map.getFloor(rasterI, rasterJ+Map.SUB);
        int bl = map.getFloor(rasterI-Map.SUB, rasterJ+Map.SUB);
        int l = map.getFloor(rasterI-Map.SUB, rasterJ);
        int tl = map.getFloor(rasterI-Map.SUB, rasterJ-Map.SUB);

        // Hajo: clear old pillars
        clearPillar(map, rasterI, rasterJ);
        clearPillar(map, rasterI+Map.SUB-1, rasterJ);
        clearPillar(map, rasterI, rasterJ+Map.SUB-1);
        clearPillar(map, rasterI+Map.SUB-1, rasterJ+Map.SUB-1);

        if(Features.isImpassable(t) || Features.isImpassable(tl) || Features.isImpassable(l))
        {
            map.setItem(rasterI, rasterJ, Features.I_FRAME_TOP);
        }

        if(Features.isImpassable(t) || Features.isImpassable(tr) || Features.isImpassable(r))
        {
            map.setItem(rasterI+Map.SUB-1, rasterJ, Features.I_FRAME_LEFT);
        }

        if(Features.isImpassable(b) || Features.isImpassable(bl) || Features.isImpassable(l))
        {
            map.setItem(rasterI, rasterJ+Map.SUB-1, Features.I_FRAME_RIGHT);
        }

        if(Features.isImpassable(b) || Features.isImpassable(br) || Features.isImpassable(r))
        {
            map.setItem(rasterI+Map.SUB-1, rasterJ+Map.SUB-1, Features.I_FRAME_BOT);
        }
        
        
        /*
        
        // left wall light
        if(Features.isImpassable(l))
        {        
            int i = rasterI;
            int j = rasterJ + Map.SUB/2 - 1;
            map.removeLight(i, j);
            Light light1 = new Light(i, j, 20, 3, 0xBBFFCC99, 0.7);
            map.lights.add(light1);
        }
        
        
        // right wall light
        if(Features.isImpassable(t))
        {        
            int i = rasterI + Map.SUB/2 - 1;
            int j = rasterJ;
            
            map.removeLight(i, j);
            Light light = new Light(i, j, 20, 3, 0xBBFFCC99, 0.7);
            map.lights.add(light);
        }

        */
    }
    
    private static void clearPillar(Map map, int x, int y)
    {
        int n = map.getItem(x, y);
        
        if(n >= Features.I_FRAME_RIGHT && n <= Features.I_FRAME_TOP)
        {
            map.setItem(x, y, 0);
        }
    }
    
    public JobFrame(ImpCity game, int x, int y)
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
        int ground = map.getFloor(rasterI, rasterJ);
        
        return !(ground >= Features.GROUND_IMPASSABLE && ground < Features.GROUND_IMPASSABLE + 3) &&
               !(ground >= Features.GROUND_LIGHT_SOIL && ground < Features.GROUND_LIGHT_SOIL + 3)
                ;
    }
    
    @Override
    public void execute(Mob worker)
    {
        if(isValid(worker))
        {
            Map map = worker.gameMap;
            int rasterI = location.x/Map.SUB*Map.SUB;
            int rasterJ = location.y/Map.SUB*Map.SUB;
            
            placePillars(map, rasterI, rasterJ);
        }
    }

    
    
    @Override
    public String toString()
    {
        return "Job: framing at " + location.x + ", " + location.y;
    }
}
