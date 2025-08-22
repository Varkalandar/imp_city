package impcity.game.ai;

import impcity.game.Features;
import impcity.game.species.Species;
import impcity.game.species.SpeciesDescription;
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

    /** 
     * The registry key of the intruder to hunt 
     */
    protected int alarmKey;
    
    
    /**
     * This is called if the AI is called to fight
     * the given intruder.
     * @param mobKey The registry key of the intruder
     */
    @Override
    public void alarm(int mobKey)
    {
        // we only take one alarm at a time
        if (alarmKey == 0) 
        {
            alarmKey = mobKey;
        }
        
        if (mobKey == 0)
        {
            // cancel alarm
            alarmKey = 0;
        }
    }

    
    @Override
    public boolean isLair(Mob mob, int x, int y)
    {
    	int lair = -1;
    	
    	// ghosts have special lairs, so we must test two cases
    	if(mob.isGhost())
    	{
    		// this is a ghost - it should have a grave with flowers
    		lair = Features.I_GRAVE_FLOWERS;
    	}
    	else
    	{
    		// this is a normal mob
            int species = mob.getSpecies();
            SpeciesDescription desc = Species.speciesTable.get(species);
            lair = desc.lair;
    	}
    
        int n = mob.gameMap.getItem(x, y);
        return n == lair;
    }
    
    
    public void placeLair(final Mob mob, int x, int y)
    {
        int species = mob.getSpecies();
        SpeciesDescription desc = Species.speciesTable.get(species);
        int n = desc.lair;
        mob.gameMap.setItem(x, y, n);
        mob.visuals.setBubble(Features.BUBBLE_SLEEPING);
        
        RectArea area = new RectArea(x - desc.lairSize, y - desc.lairSize, desc.lairSize*2, desc.lairSize*2);
        
        area.traverseWithoutCorners((int x1, int y1) -> {
            mob.gameMap.setPlacementBlocked(x1, y1, true);
            // mob.gameMap.setItem(x, y, 9);
            return false;
        });        
    }
    
    
    public static void removeLair(final Mob mob, int x, int y)
    {
        int species = mob.getSpecies();
        SpeciesDescription desc = Species.speciesTable.get(species);
        mob.gameMap.setItem(x, y, 0);
        
        RectArea area = new RectArea(x - desc.lairSize, y - desc.lairSize, desc.lairSize*2, desc.lairSize*2);
        
        area.traverseWithoutCorners((int x1, int y1) -> {
            mob.gameMap.setPlacementBlocked(x1, y1, false);
            // mob.gameMap.setItem(x, y, 9);
            return false;
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
    

    @Override
    public Point getHome()
    {
        return home;
    }    
}
