package impcity.game.combat;

import impcity.game.Features;
import impcity.game.mobs.Mob;
import impcity.game.particles.ParticleDriver;


/**
 *
 * @author Hj. Malthaner
 */
public class Damage 
{
    public final Mob source;
    
    public final int physicalL;
    public final int physicalH;
    public final int fireL;
    public final int fireH;
    public final int iceL;
    public final int iceH;
    public final int lightningL;
    public final int lightningH;
    public final int undefL;
    public final int undefH;
    public final int fear;
    public final int confusion;
    
    public Damage(Mob source,
                  int physicalL,
                  int physicalH,
                  int fireL,
                  int fireH,
                  int iceL,
                  int iceH,
                  int lightningL,
                  int lightningH,
                  int undefL,
                  int undefH,
                  int fear,
                  int confusion)
    {
        this.source = source;
        
        this.physicalL = physicalL;
        this.physicalH = physicalH;
        this.fireL = fireL;
        this.fireH = fireH;
        this.iceL = iceL;
        this.iceH = iceH;
        this.lightningL = lightningL;
        this.lightningH = lightningH;
        this.undefL = undefL;
        this.undefH = undefH;
        this.fear = fear;
        this.confusion = confusion;
        
    }
    
    /**
     * 
     * @param target The mob which was hit
     * @return The remaining amount of life
     */
    public int apply(Mob target)
    {
        int physical =  physicalL + (int)(Math.random() * (physicalH - physicalL + 1));
        int fire =  fireL + (int)(Math.random() * (fireH - fireL + 1));
        int ice =  iceL + (int)(Math.random() * (iceH - iceL + 1));
        int light =  lightningL + (int)(Math.random() * (lightningH - lightningL + 1));
        int chaos = 0;

        // Hajo: absolute physical damage reduction
        // todo
        
        // Hajo: absolute elemental damage reduction
        // todo

        
        // Hajo: reduce damage by resistence
        // f = 1 - res
        // d = d * f;
        // d = d * (100 - res);

        int [] resistances = target.calculateTotalResistances();
        
        physical = (physical << 8) * (100 - resistances[0]) / 100;
        fire  = (fire << 8)  * (100 - resistances[1]) / 100;
        ice   = (ice << 8)   * (100 - resistances[2]) / 100;
        light = (light << 8) * (100 - resistances[3]) / 100;
        chaos = (chaos << 8) * (100 - resistances[6]) / 100;
        
        int total = 
                physical +
                fire + 
                ice +
                light +
                chaos;

        
        int life = target.stats.getCurrent(Mob.I_VIT);
        
        if(total > 255) // Hajo: total >= 1.0
        {
            // Hajo: visualize an hit
            // 399 = orange star shape
            target.visuals.setTempOverlay(399, 16 + (total >> 7), 100);

            addGore(source, target);
            
            life -= total;

            target.stats.setCurrent(Mob.I_VIT, life);
            target.visuals.setMessage("" + (total >> 8), 0xFF0000);
            target.setPath(null); // Todo: do some stun/shock delay
        }
        
        return life;
    }

    private void addGore(Mob source, Mob target) 
    {
        int sx = source.visuals.lastScreenX;
        int sy = source.visuals.lastScreenY;

        int tx = target.visuals.lastScreenX;
        int ty = target.visuals.lastScreenY;
        
        int dx = sx - tx;
        int dy = sy - ty;
        
        ParticleDriver driver;
        
        if(dy > 0)
        {
            driver = target.visuals.backParticles;
        }
        else
        {
            driver = target.visuals.frontParticles;
        }
        
        int count = 25;
        double spread = 1.0;
        for(int i=0; i<count; i++)
        {
            double speedX = dx * 0.01 + (Math.random() * spread * 2 - spread);
            double speedY = dy * 0.01 + (Math.random() * spread * 2 - spread); 
            
            driver.addParticle((int)(speedX * 2), (int)(speedY * 2) + 15, 
                               speedX, speedY,
                               12 + (int)(Math.random() * 7), Features.P_ORANGE_SPARK_1, 0xCCFFAA66);
        }
        
    }
}
