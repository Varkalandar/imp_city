package impcity.game.particles;

import impcity.game.HealingWellSplashEffect;
import impcity.game.Texture;
import impcity.ogl.IsoDisplay;

/**
 * High performance particle driver.
 * 
 * @author Hj. Malthaner
 */
public class ParticleDriver 
{
    public static final int STRIDE = 8;
    
    public static final int LIFE = 0;
    public static final int MAXLIFE = 1;
    public static final int XPOS = 2;
    public static final int YPOS = 3;
    public static final int XSPEED = 4;
    public static final int YSPEED = 5;
    public static final int TEXID = 6;
    public static final int COLOR = 7;
    
    private final int count;
    private int startSearchMark = 0;
    private int lastParticleMark = 0;
    
    /** We fake some sort of structs in this array */
    private int [] particles; 
    
    private ParticleMovement movement;
    private ParticleEndEffect particleEndEffect;
    
    public ParticleDriver(int count)
    {
        this.particles = new int [STRIDE * count];
        this.count = count;
        this.startSearchMark = 0;
        this.lastParticleMark = 0;
        this.movement = new ParticleLinear();
    }
    
    public void setMovement(ParticleMovement movement)
    {
        this.movement = movement;
    }
    
    public boolean addParticle(int x, int y, double xSpeed, double ySpeed, int lifetime, int texId, int color)
    {
        // System.err.println("adding particle type=" + texId + " startSearchMark=" + startSearchMark + " lastParticleMark=" + lastParticleMark);
        
        for(int base=startSearchMark; base<count*STRIDE; base+=STRIDE)
        {
            if(particles[base] == 0)
            {
                // found a free entry
                particles[base + LIFE] = 1;               // now allocated
                particles[base + MAXLIFE] = lifetime + 1;  // max age
                particles[base + XPOS] = x << 16;
                particles[base + YPOS] = y << 16;
                particles[base + XSPEED] = (int)(xSpeed * (1 << 16));
                particles[base + YSPEED] = (int)(ySpeed * (1 << 16));
                particles[base + TEXID] = texId;
                particles[base + COLOR] = color;
                
                if(base > lastParticleMark) lastParticleMark = base;
                if(base > startSearchMark) startSearchMark = base + STRIDE;
                
                return true;
            }
        }
        
        return false;
    }
    
    public void driveParticles()
    {
        int lastActiveParticle = -STRIDE;
        
        for(int base=0; base<lastParticleMark; base+=STRIDE)
        {
            if(particles[base + LIFE] > 0)
            {
                lastActiveParticle = base;
                
                // found an active particle, drive it
                boolean tooOld = movement.drive(particles, base);
                if(tooOld)
                {
                    if(particleEndEffect != null)
                    {
                        particleEndEffect.execute(this, particles, base);
                    }
                    particles[base + LIFE] = 0;
                }
            }
            else
            {
                if(base < startSearchMark) startSearchMark = base;
            }
        }
        
        lastParticleMark = lastActiveParticle + STRIDE;
    }    
    
    public void drawParticles(IsoDisplay display)
    {
        for(int base=0; base<lastParticleMark; base+=STRIDE)
        {
            if(particles[base] > 0)
            {
                // found an active particle, draw it
            
                Texture tex = display.textureCache.textures[particles[base + TEXID]];
                IsoDisplay.drawTile(tex, particles[base + XPOS], particles[base + YPOS], particles[base + COLOR]);
            }
        }
    }
    
    public void drawParticlesAt(IsoDisplay display, int xpos, int ypos)
    {
        for(int base=0; base<lastParticleMark; base+=STRIDE)
        {
            if(particles[base] > 0)
            {
                // found an active particle, draw it
            
                Texture tex = display.textureCache.textures[particles[base + TEXID]];
                IsoDisplay.drawTile(tex, 
                                    xpos + (particles[base + XPOS] >> 16), 
                                    ypos + (particles[base + YPOS] >> 16),
                                    particles[base + COLOR]);
                
                // System.err.println("ping! " + (xpos + (particles[base + XPOS] >> 16)) + " " + (base + TEXID));
            }
        }
    }

    public void clear() 
    {
        for(int base=0; base<lastParticleMark; base+=STRIDE)
        {
            particles[base] = 0;
        }
        startSearchMark = 0;
        lastParticleMark = 0;
    }

    public void setEndEffect(HealingWellSplashEffect particleEndEffect) 
    {
        this.particleEndEffect = particleEndEffect;
    }

    public boolean hasParticles() 
    {
        return lastParticleMark > 0;
    }


    public static interface ParticleEndEffect
    {
        public void execute(ParticleDriver driver, int [] particles, int base);
    }
}
