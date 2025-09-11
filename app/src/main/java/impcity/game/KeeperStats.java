package impcity.game;

/**
 * These are indices into the player stats array.
 * 
 * @author Hj. Malthaner
 */
public class KeeperStats 
{
    /**
     * The keeper reputation is the factor that determines the average level
     * of joining creatures. The higher the reputation, the more powerful 
     * creatures will join.
     */
    public static final int REPUTATION = 1;

    /**
     * Bookkeeping for the treasuries.
     */
    public static final int COINS = 2;
    
    // Stats 3 to 5 are used in Mob update
    // for mana and vitality
    public static final int LIFE = 4;
    
    // Bitfield, see RESEARCH_XXX values
    public static final int RESEARCH = 6;
    public static final int RESEARCH_QUEST = 7;

    public static final int RESEARCH_NONE = 0;
    public static final int RESEARCH_FORGES = 1;
    public static final int RESEARCH_LABS = 2;
    public static final int RESEARCH_HEALING = 4;
    public static final int RESEARCH_GHOSTYARDS = 8;

    /**
     * The metallurgy level indicates the ability of creatures
     * to create and use metals.
     *
     * 0 = no metal working skills
     * 1 = bronze working
     * 2 = iron working
     * 3 = steel working
     * 4 = advanced steel alloys
     * 5 = mithril working (low magic metal)
     * 6 = adamantite working (highly magic metal)
     */
    public static final int METALLURGY = 7;

    public static final int METALLURGY_NONE = 0;
    public static final int METALLURGY_BRONZE = 1;
    public static final int METALLURGY_IRON = 2;
    public static final int METALLURGY_STEEL = 3;
    public static final int METALLURGY_ALLOYS = 4;

    
    public static final int MANA = 8;
    
    
    // Life constants
    public static final int LIFE_START = 1000;
    public static final int LIFE_BASE_MAX = 1000;
    public static final int LIFE_BASE_GROWTH = 1;

    // Mana constants
    public static final int MANA_START = 400;
    public static final int MANA_BASE_MAX = 1000;
    public static final int MANA_BASE_GROWTH = 60;
    public static final int MANA_CREATURE_GROWTH = 4;
    
    // Room upkeept costs
    public static final int MANA_FARMLAND_COST = 1;
    public static final int MANA_PORTAL_COST = 10;
    public static final int MANA_LAIR_COST = 1;
    public static final int MANA_TREASURY_COST = 2;
    public static final int MANA_LIBRARY_COST = 2;
    public static final int MANA_FORGE_COST = 3;
    public static final int MANA_LABORATORY_COST = 4;
    public static final int MANA_HOSPITAL_COST = 5;
    public static final int MANA_GHOSTYARD_COST = 6;
    public static final int MANA_CLAIMED_SQUARE_COST = 1;
}
