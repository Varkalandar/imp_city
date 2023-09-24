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
    public static final int GOLD = 2;
    
    // Stats 3 to 5 are used in Mob update
    // for mana and vitality

    // Bitfield, see RESEARCH_XXX values
    public static final int RESEARCH = 6;

    public static final int RESEARCH_FORGES = 1;
    public static final int RESEARCH_LABS = 2;
    public static final int RESEARCH_HEALING = 4;

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
}
