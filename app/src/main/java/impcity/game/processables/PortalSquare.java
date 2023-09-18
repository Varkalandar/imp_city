package impcity.game.processables;

import impcity.game.ImpCity;
import impcity.game.Sounds;
import impcity.game.species.Species;
import impcity.game.species.SpeciesDescription;
import impcity.game.ai.CreatureAi;
import impcity.game.ai.MobStats;
import impcity.game.species.RoomRequirements;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import impcity.game.Clock;
import impcity.game.map.Map;
import impcity.game.mobs.Mob;
import rlgamekit.pathfinding.Path;

/**
 * Creatures enter the dungeon through portals.
 * 
 * @author Hj. Malthaner
 */
public class PortalSquare implements Processable
{
    public static final Logger logger = Logger.getLogger(PortalSquare.class.getName());
    
    private final int x, y;
    private final ImpCity game;

    private long time;
    
    public PortalSquare(ImpCity game, int x, int y, long time) 
    {
        this.game = game;
        this.x = x;
        this.y = y;
        this.time = time;
    }

    @Override
    public void process(Map map) 
    {
        if(time < Clock.time() && game.world.mobs.keySet().size() < game.getLairs().size() * 4)
        {
            // int species = selectRandom();
            int species = selectFiltered();
            
            // Hajo: special case - first creature should always be a powersnail.
            // -> we guess that by time. So likely the second one will be a powersnail, too.
            if(time < 80000)
            {
                species = Species.POWERSNAILS_BASE;
            }
            
            // Hajo: got something that matches?
            if(species > 0)
            {
                int sx = x + Map.SUB/3;
                int sy = y + Map.SUB/2 + (int)(Math.random() * 2);

                SpeciesDescription desc = Species.speciesTable.get(species);
                CreatureAi monsterAi = new CreatureAi(game);
                
                // Hajo: check if there is enough space for a lair
                Path path = new Path();
                boolean ok = monsterAi.findLair(map, desc, path, new Point(sx, sy));
                
                if(ok)
                {
                    game.soundPlayer.playFromPosition(Sounds.CREATURE_ARRIVAL, 0.8f, 1.0f,
                                                      new Point(16, 355), game.getViewPosition());
                    
                    Mob mob = new Mob(game.world, sx, sy, species, map, monsterAi, desc.speed, desc.move);
                    int key = game.world.mobs.nextFreeKey();
                    game.world.mobs.put(key, mob);
                    mob.setKey(key);

                    // Hajo: make creature look south-east
                    mob.visuals.setDisplayCode(species+3);
                    // Hajo: they arrive at full health
                    mob.stats.setCurrent(MobStats.INJURIES, 0);
                    // Hajo: give them slightly random shadaes
                    mob.visuals.color =
                            0xFF000000 + 
                            ((210 + (int)(36 * Math.random())) << 16) + 
                            ((210 + (int)(36 * Math.random())) << 8) + 
                            ((210 + (int)(36 * Math.random()))); 
                }
                else
                {
                    logger.log(Level.INFO, "Found no lair space for a {0}", desc.name);
                }
            }
            
            // Hajo: spawn next creature in 45 seconds.
            time = Clock.time() + 45000;
        }
    }

    private int selectRandom()
    {
        int species;
        
        double select = Math.random();
        if(select > 0.9)
        {
            species = Species.WYVERNS_BASE;
        }
        else if(select > 0.8)
        {
            species = Species.KILLERBEETLES_BASE;
        }
        else if(select > 0.7) 
        {
            species = Species.BOOKWORMS_BASE;
        }
        else if(select > 0.35) 
        {
            species = Species.CONIANS_BASE;
        }
        else
        {
            species = Species.POWERSNAILS_BASE;
        }
        return species;
    }

    private int selectFiltered()
    {
        ArrayList <SpeciesDescription> filteredList = new ArrayList<SpeciesDescription>();
        
        Set<Integer> keys = Species.speciesTable.keySet();
        
        for(Integer key : keys)
        {
            SpeciesDescription description = Species.speciesTable.get(key);
            
            RoomRequirements req = description.roomRequirements;
            
            boolean ok = true;
            
            ok &= req.farms <= game.getFarmland().size();
            ok &= req.lairs <= game.getLairs().size();
            ok &= req.libraries <= game.getLibraries().size();
            ok &= req.treasury <= game.getTreasuries().size();
            ok &= req.forges <= game.getForges().size();
            
            if(ok)
            {
                filteredList.add(description);
            }
        }
        int species = 0;
        
        if(!filteredList.isEmpty())
        {
            species = filteredList.get((int)(filteredList.size() * Math.random())).baseImage;
        }
        
        return species;
    }
}
