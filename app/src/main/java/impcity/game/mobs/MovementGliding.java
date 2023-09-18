package impcity.game.mobs;

/**
 *
 * @author Hj. Malthaner
 */
public class MovementGliding implements MovementPattern
{
    @Override
    public void calculateMove(Mob mob, int deltaT) 
    {
        mob.zOff = 0;
    }
    
}
