package impcity.game.combat;

import impcity.game.World;
import impcity.game.mobs.Mob;

/**
 * Models an attack in combat.
 * 
 * @author Hj. Malthaner
 */
public abstract class Attack 
{
    protected final World world;
    protected final Mob attacker;
    
    public Attack(World world, Mob attacker)
    {
        this.world = world;
        this.attacker = attacker;
    }

    /**
     * The attack hits the defender. Implementations of this
     * method are supposed to handle all effects.
     * @param defender The defnind mob, i.e. the one which was hit.
     */
    abstract public void hit(Mob defender);
}
