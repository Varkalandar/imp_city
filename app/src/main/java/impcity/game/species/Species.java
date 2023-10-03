package impcity.game.species;

import impcity.game.Sounds;
import impcity.game.ai.JobPreference;
import impcity.game.mobs.*;
import java.util.HashMap;

/**
 * Constants for all imp city species
 * 
 * @author Hj. Malthaner
 */
public class Species
{
    public static final int CONIANS_BASE = 25;
    public static final int GLOBOS_BASE = 49;
    public static final int IMPS_BASE = 1;
    public static final int POWERSNAILS_BASE = 57;
    public static final int KILLERBEETLES_BASE = 65;
    public static final int WYVERNS_BASE = 73;
    public static final int BOOKWORMS_BASE = 81;
    public static final int MOSQUITOES_BASE = 89;
    public static final int HAT_MAGE_BASE = 129;

    public static final HashMap<Integer, SpeciesDescription> speciesTable = new HashMap<Integer, SpeciesDescription>();

    static
    {
        SpeciesDescription globos = 
                new SpeciesDescription("Globo", 
                                       3,                 // size
                                       GLOBOS_BASE,       // base
                                       398,               // lair type
                                       2,                 // lair size
                                       new MovementJumping(),
                                       15,                // speed
                                       -1,                // starting sound
                                       new RoomRequirements(
                                            999,   // lairs 
                                            999,   // farms
                                            999,   // treasury
                                            999,   // libraries
                                            999,
                                               999// forges
                                        ),
                                       3,                 // intelligence
                                       1,                 // combat
                                       1,                 // stealth
                                       1,                 // carrying capacity
                                       JobPreference.LAZY
                );
        
        SpeciesDescription conians = 
                new SpeciesDescription("Conian", 
                                       3, 
                                       CONIANS_BASE,
                                       398,               // lair type
                                       2,                 // lair size
                                       new MovementJumping(),
                                       15,                // speed
                                       -1,                // starting sound
                                       new RoomRequirements(
                                            4,   // lairs 
                                            4,   // farms
                                            0,   // treasury
                                            0,   // libraries
                                            1,    // forges
                                            0
                                        ),
                                       3,                 // intelligence
                                       2,                 // combat
                                       1,                 // stealth
                                       2,                 // carrying capacity
                                       JobPreference.FORGE
                );
        
        SpeciesDescription imps = 
                new SpeciesDescription("Imp", 
                                       2,
                                       IMPS_BASE, 
                                       397,               // lair type
                                       2,                 // lair size
                                       new MovementJumping(),
                                       16,                // speed
                                       -1,                // starting sound
                                       new RoomRequirements(
                                            999,   // lairs 
                                            999,   // farms
                                            999,   // treasury
                                            999,   // libraries
                                            999,    // forges
                                            0
                                        ),
                                       3,                 // intelligence
                                       1,                 // combat
                                       1,                 // stealth
                                       1,                 // carrying capacity
                                       JobPreference.LAZY
                );
        
        SpeciesDescription powersnails = 
                new SpeciesDescription("Powersnail", 
                                       3, 
                                       POWERSNAILS_BASE, 
                                       396,               // lair type
                                       3,                 // lair size
                                       new MovementGliding(),
                                       5,                // speed
                                       Sounds.SNAIL_START,  // starting sound
                                       // Sounds.FLY_START,  // starting sound
                                       new RoomRequirements(
                                            4,   // lairs 
                                            1,   // farms
                                            0,   // treasury
                                            0,   // libraries
                                            0,    // forges
                                            0
                                        ),
                                       3,                 // intelligence
                                       1,                 // combat
                                       2,                 // stealth
                                       3,                 // carrying capacity
                                       JobPreference.FARM
                );
        
        SpeciesDescription killerbeetles = 
                new SpeciesDescription("Giant Beetle", 
                                       4,                 // size 
                                       KILLERBEETLES_BASE,
                                       395,               // lair type
                                       4,                 // lair size
                                       new MovementJitter(7, 1),
                                       7,                // speed
                                       -1,                // starting sound
                                       new RoomRequirements(
                                            9,   // lairs 
                                            9,   // farms
                                            1,   // treasury
                                            0,   // libraries
                                            1,    // forges
                                            0
                                        ),
                                       1,                 // intelligence
                                       5,                 // combat
                                       0,                 // stealth
                                       5,                 // carrying capacity
                                       JobPreference.LAZY
                );

        SpeciesDescription wyverns = 
                new SpeciesDescription("Wyvern", 
                                       3,                 // size 
                                       WYVERNS_BASE,
                                       391,               // lair type
                                       3,                 // lair size
                                       new MovementJitter(7, 2 << 16),
                                       20,                // speed
                                       -1,                // starting sound
                                       new RoomRequirements(
                                            4,   // lairs 
                                            4,   // farms
                                            4,   // treasury
                                            0,   // libraries
                                            0,    // forges
                                            0
                                        ),
                                       5,                 // intelligence
                                       1,                 // combat
                                       5,                 // stealth
                                       1,                 // carrying capacity
                                       JobPreference.LAZY
                );
        
        SpeciesDescription bookworms = 
                new SpeciesDescription("Bookworm", 
                                       3,                 // size 
                                       BOOKWORMS_BASE,
                                       393,               // lair type
                                       3,                 // lair size
                                       new MovementJitter(8, 1 << 15),
                                       4,                // speed
                                       -1,                // starting sound
                                       new RoomRequirements(
                                            4,   // lairs 
                                            4,   // farms
                                            0,   // treasury
                                            4,   // libraries
                                            0,    // forges
                                            0   
                                        ),
                                       5,                 // intelligence
                                       0,                 // combat
                                       3,                 // stealth
                                       1,                 // carrying capacity
                                       JobPreference.LIBRARY
                );
        
        SpeciesDescription mosquitoes = 
                new SpeciesDescription("Mosquito", 
                                       3,                 // size 
                                       MOSQUITOES_BASE,
                                       394,               // lair type
                                       3,                 // lair size
                                       new MovementJitter(6, 1 << 16),
                                       8,                 // speed
                                       Sounds.FLY_START,  // starting sound
                                       new RoomRequirements(
                                            4,   // lairs 
                                            4,   // farms
                                            0,   // treasury
                                            0,   // libraries
                                            0,    // forges
                                            0
                                        ),
                                       4,                 // intelligence
                                       2,                 // combat
                                       3,                 // stealth
                                       1,                 // carrying capacity
                                       JobPreference.LIBRARY
                );

        SpeciesDescription hatMages = 
                new SpeciesDescription("Hat Mage", 
                                       2,                 // size
                                       HAT_MAGE_BASE,
                                       397,               // lair type
                                       3,                 // lair size
                                       new MovementJumping(),
                                       12,                 // speed
                                       -1,  // starting sound
                                       new RoomRequirements(
                                            4,   // lairs 
                                            4,   // farms
                                            0,   // treasury
                                            0,   // libraries
                                            0,    // forges
                                            4
                                        ),
                                       4,                 // intelligence
                                       1,                 // combat
                                       2,                 // stealth
                                       2,                 // carrying capacity
                                       JobPreference.LABORATORY
                );

        speciesTable.put(GLOBOS_BASE, globos);
        speciesTable.put(CONIANS_BASE, conians);
        speciesTable.put(IMPS_BASE, imps);
        speciesTable.put(POWERSNAILS_BASE, powersnails);
        speciesTable.put(KILLERBEETLES_BASE, killerbeetles);
        speciesTable.put(WYVERNS_BASE, wyverns);
        speciesTable.put(BOOKWORMS_BASE, bookworms);
        speciesTable.put(MOSQUITOES_BASE, mosquitoes);
        speciesTable.put(HAT_MAGE_BASE, hatMages);
    }
}
