package impcity.game.animation;

import impcity.game.Clock;
import impcity.game.combat.Attack;
import impcity.game.mobs.Mob;

/**
 *
 * @author Hj. Malthaner
 */
public class MeleeAttackAnimation implements Animation
{
    private final long startTime;
    private boolean finished;
    private final Mob player;
    private final Attack attack;
    private final Mob defender;
    private final int startDirection;
    
    public MeleeAttackAnimation(Mob player, Attack attack, Mob defender) 
    {
        this.player = player;
        this.attack = attack;
        this.defender = defender;
        
        this.startTime = Clock.time();
        this.finished = false;
        this.startDirection = player.visuals.getDisplayCode() - player.getSpecies();
    }
    
    @Override
    public boolean isFinished()
    {
        return finished;
    }
    
    @Override
    public void play()
    {
        int step = (int)(Clock.time() - startTime) >> 5;
        
        if(step < 8)
        {
            int dir = (startDirection + step) & 7;
            int frame = player.getSpecies() + dir;
            player.visuals.setDisplayCode(frame);
        }
        else
        {
            player.visuals.setDisplayCode(player.getSpecies() + startDirection);
            finished = true;
            attack.hit(defender);
        }
    }
}
