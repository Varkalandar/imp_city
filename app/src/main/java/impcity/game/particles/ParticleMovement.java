package impcity.game.particles;

/**
 *
 * @author Hj. Malthaner
 */
public interface ParticleMovement 
{
    /**
     * Move the particle.
     * @param particles The particle array
     * @param base The particle index
     * @return true if the particle reached end of life
     */
    public boolean drive(int [] particles, int base);
}
