package impcity.game;

import impcity.game.particles.ParticleDriver;

/**
 *
 * @author Hj. Malthaner
 */
public class HealingWellSplashEffect implements ParticleDriver.ParticleEndEffect
{

    @Override
    public void execute(ParticleDriver driver, int [] particles, int base) 
    {
        if(particles[base+ParticleDriver.TEXID] == Features.P_SPLASH_EFFECT_1)
        {
            driver.addParticle(
                    (particles[base+ParticleDriver.XPOS] >> 16) - 1,
                    (particles[base+ParticleDriver.YPOS] >> 16),
                    0, 
                    0, 
                    5, 
                    Features.P_SPLASH_EFFECT_1 + 1, 
                    0x77FFFFFF);
        }
        else if(particles[base+ParticleDriver.TEXID] == Features.P_SPLASH_EFFECT_1 + 1)
        {
            driver.addParticle(
                    (particles[base+ParticleDriver.XPOS] >> 16) - 3,
                    (particles[base+ParticleDriver.YPOS] >> 16) + 1,
                    0, 
                    0, 
                    5, 
                    Features.P_SPLASH_EFFECT_1 + 2, 
                    0x77FFFFFF);
        }
        else if(particles[base+ParticleDriver.TEXID] > Features.P_SPLASH_EFFECT_1 + 2)
        {
            driver.addParticle(
                    (particles[base+ParticleDriver.XPOS] >> 16) - 6,
                    (particles[base+ParticleDriver.YPOS] >> 16) + 2,
                    0, 
                    0, 
                    5, 
                    Features.P_SPLASH_EFFECT_1, 
                    0x77FFFFFF);
        }
    }
}
