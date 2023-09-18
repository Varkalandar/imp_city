package impcity.game.mobs;

/**
 *
 * @author Hj. Malthaner
 */
public interface MovementPattern
{
    public void calculateMove(Mob mob, int deltaT);
}
