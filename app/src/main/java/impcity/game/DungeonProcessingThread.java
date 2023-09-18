package impcity.game;

import impcity.game.ai.MobStats;
import impcity.game.processables.Processable;
import java.awt.Point;
import static java.lang.Thread.sleep;
import java.util.logging.Level;
import java.util.logging.Logger;
import impcity.game.map.Map;
import impcity.game.particles.ParticleGravity;
import impcity.game.particles.ParticleLinear;
import impcity.game.particles.ParticleMovement;
import impcity.game.mobs.Mob;
import impcity.game.ui.Colors;

/**
 * Background thread for processing non-ai dungeon activities.
 * 
 * @author Hj. Malthaner
 */
public class DungeonProcessingThread extends Thread
{
    private static final Logger logger = Logger.getLogger(DungeonProcessingThread.class.getName());
    private int pass;
    private int passMask = 1023;
    
    private final ImpCity game;
    
    private final ParticleMovement linear = new ParticleLinear();
    private final ParticleMovement gravity = new ParticleGravity(3 << 10);
    
    private final HealingWellSplashEffect splash = new HealingWellSplashEffect();
    
    public DungeonProcessingThread(ImpCity game)
    {
        setDaemon(true);
        this.game = game;
        pass = 0;
        setName("DungeonProcessingThread");
    }
    
    @Override
    public void run()
    {
        while(true)
        {
            safeSleep(50);
            game.processorActive = true;
            
            try
            {
                if(!game.processorLock)
                {
                    process();
                }
            }
            catch(Exception ex)
            {
                logger.log(Level.SEVERE, null, ex);
            }
            
            game.processorActive = false;
        }
    }
    
    private void process() 
    {
        Mob player = game.world.mobs.get(game.getPlayerKey());
        Map map = player.gameMap;

        if(map != null)
        {        
            for(Processable p : game.getFarmland())
            {
                p.process(map);
            }
            for(Processable p : game.getPortals())
            {
                p.process(map);
            }
            for(Mob generator : game.generators)
            {
                generateParticles(generator);
            }

            // Hajo: Testing: fill dungeon slowly with small shrubs and dust.
            // Moving creatures will clean this up again.
            int n = 0;
            for(Point p : game.getClaimedSquares())
            {
                if((n & passMask) == pass)
                {
                    int x = p.x + (int)(Math.random()*Map.SUB);
                    int y = p.y + (int)(Math.random()*Map.SUB);

                    if(map.getItem(x, y) == 0 && !map.isPlacementBlocked(x, y))
                    {
                        // add dust
                        int randomDust = Features.DUSTS[(int)(Features.DUSTS.length * Math.random())];

                        map.setItem(x, y, randomDust);
                    }
                }
                n++;
            }
        }
        
        pass = (pass + 1) & passMask;
    }

    private void safeSleep(int millis)
    {
        try
        {
            sleep(millis);
        } 
        catch (InterruptedException ex)
        {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private void generateParticles(Mob generator)
    {
        int type = generator.stats.getCurrent(MobStats.GENERATOR);
        switch(type)
        {
            case MobStats.G_VOLCANO:
                driveVolcano(generator);
                break;
            case MobStats.G_HEALING_WELL:
                driveHealingWell(generator);
                break;
                
            default:
                logger.log(Level.WARNING, "Unknown generator type: {0}", type);
        }
        
    }

    private void driveVolcano(Mob generator)
    {
        double speed = 0.2;
        // generator.visuals.backParticles.setMovement(linear);
        generator.visuals.backParticles.addParticle(4 - (int)(Math.random() * 9),
                                                    0, 
                                                    speed * Math.random() * 2.0 - speed, 
                                                    0.5 + speed * Math.random(), 
                                                    40,
                                                    Features.P_ORANGE_SPARK_1 + (int)(Math.random()*3),
                                                    0xFFFFFFFF);
    }

    private void driveHealingWell(Mob generator) 
    {
        /*
        int r = 21 + (int)(Math.random() * 4);
        int xpos = (int)(Math.cos(Math.random() * Math.PI) * r * 2);
        int ypos = (int)(Math.sin(Math.random() * Math.PI) * r);

        double speed = 0.1;
        generator.visuals.backParticles.addParticle(xpos,
                                                    ypos, 
                                                    0, 
                                                    // 0.5 + speed * Math.random(), 
                                                    2.5 + speed * Math.random(), 
                                                    20 + (int)(Math.random()*80),
                                                    Features.P_BLUE_SPARK_1 + (int)(Math.random()*8),
                                                    0xA0000000 | (Colors.randomColor(160, 95, 95, 95) & 0xFFFFFF));

        xpos = (int)(Math.cos(Math.random() * Math.PI) * r * 2);
        ypos = -(int)(Math.sin(Math.random() * Math.PI) * r);

        generator.visuals.frontParticles.addParticle(xpos,
                                                    ypos, 
                                                    0, 
                                                    // 0.5 + speed * Math.random(), 
                                                    2.5 + speed * Math.random(), 
                                                    20 + (int)(Math.random()*80),
                                                    Features.P_BLUE_SPARK_1 + (int)(Math.random()*8),
                                                    0xA0000000 | (Colors.randomColor(160, 95, 95, 95) & 0xFFFFFF));

*/ 
        generator.visuals.backParticles.setEndEffect(splash);
        generator.visuals.frontParticles.setEndEffect(splash);
        generator.visuals.backParticles.setMovement(gravity);
        generator.visuals.frontParticles.setMovement(gravity);
        
        int r = 1 + (int)(Math.random() * 4);
        int xpos = (int)(Math.cos(Math.random() * Math.PI) * r * 2);
        int ypos = (int)(Math.sin(Math.random() * Math.PI) * r);

        
        double speed = 0.1;
        double xspeed = (Math.random() - 0.5) * 0.7;
        
        generator.visuals.backParticles.addParticle(xpos,
                                                    ypos, 
                                                    xspeed, 
                                                    1.6 + speed * Math.random(), 
                                                    69 - Math.abs((int)(xspeed * 7)),
                                                    Features.P_SILVER_SPARK_1 + (int)(Math.random()*5),
                                                    0xA0000000 | (Colors.randomColor(200, 55, 55, 55) & 0xFFFFFF));

        generator.visuals.backParticles.addParticle(xpos,
                                                    ypos, 
                                                    Math.random() - 0.5, 
                                                    1.5 + speed * Math.random(), 
                                                    75 - Math.abs((int)(xspeed * 7)),
                                                    Features.P_SILVER_SPARK_1 + (int)(Math.random()*5),
                                                    0xA0000000 | (Colors.randomColor(200, 55, 55, 55) & 0xFFFFFF));

 }
}
