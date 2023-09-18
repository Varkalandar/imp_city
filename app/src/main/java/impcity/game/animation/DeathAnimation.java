package impcity.game.animation;

import impcity.game.Clock;
import impcity.game.World;
import impcity.game.mobs.Mob;


/**
 * Famous last words should got here ...
 * 
 * @author Hj. Malthaner
 */
public class DeathAnimation implements Animation
{
    private final long startTime;
    private boolean finished;
    private final Mob defender;
    private final int startDirection;
    private final World world;
    
    public DeathAnimation(World world, Mob defender) 
    {
        this.world = world;
        this.defender = defender;
        
        this.startTime = Clock.time();
        this.finished = false;
        this.startDirection = defender.visuals.getDisplayCode() - defender.getSpecies();
    }
    
    @Override
    public boolean isFinished()
    {
        return finished;
    }
    
    @Override
    public void play()
    {
        int step = (int)(Clock.time() - startTime) >> 3;
        
        if(step < 128)
        {
            int dir = (startDirection + step) & 7;
            int frame = defender.getSpecies() + dir;
            defender.visuals.setDisplayCode(frame);
            
            int bright = 256 - step * 2;
            defender.visuals.color = 
                    0xFF000000 | (bright << 16) | (bright << 8) | (bright);
                    
        }
        else
        {
            finished = true;
        
            defender.gameMap.setMob(defender.location.x, defender.location.y, 0);
            world.mobs.remove(defender.getKey());
            System.err.println("Mob #" + defender.getKey() + " was killed.");
        }
    }
}
