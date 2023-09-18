package impcity.game.particles;

import static impcity.game.particles.ParticleDriver.LIFE;
import static impcity.game.particles.ParticleDriver.MAXLIFE;
import static impcity.game.particles.ParticleDriver.XPOS;
import static impcity.game.particles.ParticleDriver.XSPEED;
import static impcity.game.particles.ParticleDriver.YPOS;
import static impcity.game.particles.ParticleDriver.YSPEED;

/**
 *
 * @author Hj. Malthaner
 */
public class ParticleLinear implements ParticleMovement
{

    @Override
    public boolean drive(int[] particles, int base) 
    {
        // end of life reached?
        if(particles[base + LIFE] > particles[base+MAXLIFE])
        {
            return true;
        }
        else
        {
            particles[base + LIFE] ++;
            particles[base + XPOS] += particles[base + XSPEED];
            particles[base + YPOS] += particles[base + YSPEED];
            return false;
        }
    }
}
