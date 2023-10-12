package impcity.game.ai;

import impcity.game.Features;
import impcity.game.species.Species;
import impcity.game.species.SpeciesDescription;
import impcity.game.map.LocationCallback;
import impcity.game.map.RectArea;
import impcity.game.mobs.Mob;

import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hjm
 */
public abstract class AiBase implements Ai
{
    private static final Logger logger = Logger.getLogger(AiBase.class.getName());

    /**
     * The creatures home location. Homeless creatures have (-1, -1)
     */
    protected final Point home = new Point(-1, -1);

    protected int workStep;
    
    public boolean isLair(Mob mob, int x, int y)
    {
        int species = mob.getSpecies();
        SpeciesDescription desc = Species.speciesTable.get(species);
        
        int n = mob.gameMap.getItem(x, y);
        return n == desc.lair;
    }
    
    public void placeLair(final Mob mob, int x, int y)
    {
        int species = mob.getSpecies();
        SpeciesDescription desc = Species.speciesTable.get(species);
        int n = desc.lair;
        mob.gameMap.setItem(x, y, n);
        mob.visuals.setBubble(Features.BUBBLE_SLEEPING);
        
        RectArea area = new RectArea(x - desc.lairSize, y - desc.lairSize, desc.lairSize*2, desc.lairSize*2);
        
        area.traverseWithoutCorners(new LocationCallback() {

            @Override
            public boolean visit(int x, int y)
            {
                mob.gameMap.setPlacementBlocked(x, y, true);
                // mob.gameMap.setItem(x, y, 9);
                
                return false;
            }
        });
        
    }
    
    public boolean checkLairSpace(Mob mob, int x, int y)
    {
        SpeciesDescription desc = Species.speciesTable.get(mob.getSpecies());
        boolean ok = true;
        
        for(int j=-desc.lairSize; j<=desc.lairSize && ok; j++)
        {
            for(int i=-desc.lairSize; i<=desc.lairSize && ok; i++)
            {                
                // Hajo: lair anchor points are shifted up to get 
                // visibility right 
                ok &= mob.gameMap.getItem(x + i - desc.lairSize + 1, 
                                          y + j - desc.lairSize + 1) == 0;
                
                ok &= !mob.gameMap.isPlacementBlocked(x + i, y + j);
            }
        }
        
        return ok;
    }

    public Point getHome()
    {
        return home;
    }

    public void teleportMob(Mob mob, Point destination)
    {
        logger.log(Level.INFO, "Creature #{0} at {1}, {2} will be teleported to {3}, {4}.",
                new Object[]{mob.getKey(), mob.location.x, mob.location.y, destination.x, destination.y});
        mob.gameMap.setMob(mob.location.x, mob.location.y, 0);
        mob.location.x = destination.x;
        mob.location.y = destination.y;
        mob.gameMap.setMob(destination.x, destination.y, mob.getKey());
    }
}
