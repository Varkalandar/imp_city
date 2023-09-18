package impcity.game.mobs;

/**
 *
 * @author Hj. Malthaner
 */
public class MovementJumping implements MovementPattern
{
    @Override
    public void calculateMove(Mob mob, int deltaT) 
    {
        final int gravity = 2 * (1 << 10);
        
        if(mob.zOff <= 0)
        {
            // new jump
            mob.zOff = 0;
            mob.zSpeed = 240 * (1 << 10);
        }
        
        mob.zSpeed -= gravity * deltaT;
        mob.zOff += mob.zSpeed;
        
        // System.err.println("Mob #" + key + " zOff=" + zOff);
    }
    
}
