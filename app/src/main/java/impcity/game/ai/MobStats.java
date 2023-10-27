package impcity.game.ai;

/**
 *
 * @author Hj. Malthaner
 * @see impcity.game.KeeperStats
 */
public class MobStats 
{
    public final static int GOLD = 0;
    public final static int CARRY = 1;
    public final static int WORK_STEP = 2;
    public final static int INJURIES = 3;
    public final static int VITALITY = 4;
    public final static int EXPERIENCE = 5;

    public final static int GENERATOR = 9;
    
    // hack, hack ... generator types ...
    public final static int G_VOLCANO = 1;
    public final static int G_HEALING_WELL = 2;
    public final static int G_DISTILL = 3;


    /** Mobs start with this amount of experience */
    public final static int BEGINNER_EXPERIENCE = 200;
}
