package impcity.game.combat;

import impcity.game.World;
import impcity.game.animation.DeathAnimation;
import impcity.game.mobs.Mob;

/**
 * Models an attack in combat.
 * 
 * @author Hj. Malthaner
 */
public class DamageAttack extends Attack
{
    private final Damage damage;
    
    public DamageAttack(World world, Mob attacker, Damage damage)
    {
        super(world, attacker);
        this.damage = damage;
    }
    
    @Override
    public void hit(Mob defender)
    {
        // Hajo: can the defender block the attack?
        double blocking = defender.calculateTotalBlock() / 100.0;
        if(Math.random() > blocking)
        {
            // Hajo: blocking failed
            // Can the defender dodge this attack?
            int dodge = defender.calculateTotalDodge();
            
            // now see if the armor can protect
            
            int def = defender.calculateTotalDef();
            int ar = attacker.calculateTotalAttackRating();
            
            if(ar > Math.random() * (def + dodge))
            {
                // Hajo: this was a successful hit

                int life = damage.apply(defender);

                if(life < 0)
                {
                    if(defender.getAi() != null)
                    {
                        attacker.visuals.setMessage("Victory!", 0xFFFFFFFF);
                        killCreature(defender);
                    }
                    else
                    {
                        killPlayer(defender);
                    }
                }        
            }
            else
            {
                attacker.visuals.setMessage("Missed", 0xFF888888);
            }
        }
        else
        {
            defender.visuals.setMessage("Blocked", 0xFF888888);
        }
    }

    private void killCreature(Mob defender) 
    {
        if(!defender.isDying)
        {
            defender.isDying = true;
            
            DeathAnimation deathAnimation = new DeathAnimation(world, defender);
            defender.visuals.animation = deathAnimation;

            // Todo: calculate "real" drop count 
        }
    }
    
    private void killPlayer(Mob defender) 
    {
        // Todo
        // defender.gameMap.setMob(defender.location.x, defender.location.y, 0);
        // world.mobs.remove(defender.getKey());
        // System.err.println("Mob #" + defender.getKey() + " was killed.");
    }
}
