package impcity.game.combat.magic;

import impcity.game.World;
import impcity.game.combat.Attack;
import impcity.game.mobs.Mob;
import impcity.game.particles.ParticleDriver;
import impcity.ogl.IsoDisplay;
import static org.lwjgl.opengl.GL11.GL_DST_ALPHA;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;

/**
 *
 * @author Hj. Malthaner
 */
public class Spell 
{
    protected final World world;
    protected final Mob attacker;
    protected final Attack attack;
    
    public final ParticleDriver backParticles = new ParticleDriver(1024);
    public final ParticleDriver frontParticles = new ParticleDriver(1024);
    
    
    public Spell(World world, Mob attacker, Attack attack)
    {
        this.world = world;
        this.attacker = attacker;
        this.attack = attack;
    }
    
    public void drive()
    {
        backParticles.driveParticles();
        frontParticles.driveParticles();
    }
    
    
    public void displayFront(IsoDisplay display, int x, int y)
    {
        glBlendFunc(GL_SRC_ALPHA, GL_DST_ALPHA);
        frontParticles.drawParticlesAt(display, x, y);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }
    
    public void displayBack(IsoDisplay display, int x, int y)
    {
        glBlendFunc(GL_SRC_ALPHA, GL_DST_ALPHA);
        backParticles.drawParticlesAt(display, x, y);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }
    
    
    public boolean isExpired()
    {
        return !backParticles.hasParticles() && !frontParticles.hasParticles();
        // return false;
    }

    /**
     * Casting cost in mana point
     * @return The amount of mana needed for this spell
     */
    public int cost()
    {
        return 1;
    }
    /*
    private class MovementLinear implements ParticleMovement
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
                
                int x, y;
                
                x = particles[base + XPOS] >> 16;
                y = particles[base + YPOS] >> 16;
                
                attacker.gameMap.setEffect(x, y, null);
                
                particles[base + XPOS] += particles[base + XSPEED];
                particles[base + YPOS] += particles[base + YSPEED];
                return false;
            }
        }
    }
    */

    public void end() 
    {
    }
    
}
