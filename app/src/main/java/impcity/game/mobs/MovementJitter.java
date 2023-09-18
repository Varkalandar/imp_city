
package impcity.game.mobs;

import impcity.game.Clock;

/**
 *
 * @author Hj. Malthaner
 */
public class MovementJitter implements MovementPattern
{
    private final int amount;
    private final int timeShift;
    
    public MovementJitter(int timeShift, int amount)
    {
        this.timeShift = timeShift;
        this.amount = amount;
    }
    
    @Override
    public void calculateMove(Mob mob, int deltaT) 
    {
        int t = (int)(Clock.time()) >> timeShift;
        
        int jitter = (t & 1) * 2 - 1;
        
        mob.zOff = jitter * amount;
    }
    
}
