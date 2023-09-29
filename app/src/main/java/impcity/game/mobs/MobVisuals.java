package impcity.game.mobs;

import impcity.game.Features;
import java.util.ArrayList;
import impcity.game.Clock;
import impcity.game.particles.ParticleDriver;
import impcity.game.Texture;
import impcity.game.animation.Animation;
import impcity.game.combat.magic.Spell;
import impcity.ogl.Drawable;
import impcity.ogl.IsoDisplay;
import impcity.ui.TimedMessage;

/**
 *
 * @author Hj. Malthaner
 */
public class MobVisuals implements Drawable
{
    public static final int OVERLAY_EYES = 0;
    public static final int OVERLAY_BACKPACK = 1;
    public static final int OVERLAY_WEAPON = 2;
    public static final int OVERLAY_SHIELD = 3;

    public Animation animation;
    
    private int displayCode;
    private int bubble;
    
    private int tempOverlayId;
    private int tempOverlaySize;
    private long showOverlayUntil;

    private final TimedMessage timedMessage = new TimedMessage("", 0,0,0,0);
    
    public int color = 0xFFFFFFFF;

    public final ParticleDriver backParticles = new ParticleDriver(1024);
    public final ParticleDriver frontParticles = new ParticleDriver(1024);
    
    public final ArrayList <Spell> spells = new ArrayList<Spell>();
    public int lastScreenX;
    public int lastScreenY;
    
    public final int [] equipmentOverlays = new int [12];
    public final int [] equipmentOverlaysColors = new int [12];
    
    public String name;
    
    public MobVisuals()
    {
        for(int i=0; i<equipmentOverlaysColors.length; i++)
        {
            equipmentOverlaysColors[i] = 0xFFFFFFFF;
        }
    }
    
    public int getDisplayCode()
    {
        return displayCode;
    }

    public void setDisplayCode(int displayCode)
    {
        this.displayCode = displayCode;
    }
    
    public void setBubble(int bubble)
    {
        this.bubble = bubble;
    }
    
    public void setMessage(String message, int color)
    {
        timedMessage.time = Clock.time();
        timedMessage.message = message;
        timedMessage.color = color;
    }
    
    public void setTempOverlay(int id, int size, int millis)
    {
        this.tempOverlayId = id;
        this.tempOverlaySize = size;
        this.showOverlayUntil = Clock.time() + millis;
    }
    
    @Override
    public void display(IsoDisplay display, int x, int y)
    {
        lastScreenX = x;
        lastScreenY = y;
        
        // Hajo: draw the mob's shadow
        Texture tex = display.textureCache.textures[Features.SHADOW_BASE];
        int w = 20;  // Mob size?
        int h = w / 2;
        IsoDisplay.drawTile(tex, x-w/2, y-h/4, w, h, 0.4f, 0f, 0f, 0f);
        
        
        if(animation != null)
        {
            animation.play();
            if(animation.isFinished()) 
            {
                animation = null;
            }
        }
        
        backParticles.drawParticlesAt(display, x, y);
        
        for(Spell spell : spells)
        {
            spell.drive();
            spell.displayBack(display, x, y);
        }
        
        // Hajo: draw the mob itself
        tex = display.textureCache.species[displayCode];
        int left, right, center;
        
        // debug
        if(tex == null)
        {
            // System.err.println("No species texture for displayCode=" + displayCode);
            center = 0;
                    
            // right = left = 0;
        }
        else
        {
            left = x-tex.footX;
            right = left + tex.image.getWidth();
            center = (left + right) / 2;
            
            IsoDisplay.drawTile(tex, left, y - tex.image.getHeight() + tex.footY, color);
            
            if(bubble > 0)
            {
                IsoDisplay.drawTile(display.textureCache.textures[bubble], center, y+tex.image.getHeight() + 2);
            }

            drawEquipmentOverlays(display, tex, left, y);
            
            if(name != null)
            {
                int nw = display.font.getStringWidth(name);
                display.font.drawStringScaled(name, 0xFFFFFFFF, center-nw/4, y+35, 0.5);
            }
        }
        
        

        if(Clock.time() < showOverlayUntil)
        {
            Texture oTex = display.textureCache.textures[tempOverlayId];
            
            IsoDisplay.drawTile(oTex, 
                                center - tempOverlaySize / 2, y+2, 
                                tempOverlaySize, tempOverlaySize, 0xFFFFFFFF);
        }

        frontParticles.drawParticlesAt(display, x, y);

        if(timedMessage.message != null)
        {
            int yoff = 8 + (((int)(Clock.time() - timedMessage.time)) >> 3);
            display.drawString(timedMessage.message, timedMessage.color, x-2, y + yoff, 1.0 + yoff/120.0);

            if(yoff > 100)
            {
                timedMessage.message = null;
                timedMessage.time = 0;
            }
        }
        
        ArrayList<Spell> killList = new ArrayList<Spell>();
        for(Spell spell : spells)
        {
            spell.displayFront(display, x, y);
            if(spell.isExpired()) 
            {
                spell.end();
                killList.add(spell);
            }
        }
        for(Spell spell : killList)
        {
            spells.remove(spell);
        }
    }

    private void drawEquipmentOverlays(IsoDisplay display, Texture tex, int left, int y) 
    {
        for(int i=0; i<equipmentOverlays.length; i++)
        {
            int base = equipmentOverlays[i];
            if(base > 0)
            {
                // mob drawing
                // IsoDisplay.drawTile(tex, left, y - tex.image.getHeight() + tex.footY, color);

                int offset = (displayCode - 1) & 7;
                int bot = y - tex.image.getHeight() + tex.footY;

                // System.err.println("offset=" + offset);

                // shield
                Texture ovl = display.textureCache.species[base + offset];
                int ydiff = tex.image.getHeight() - ovl.image.getHeight();

                IsoDisplay.drawTile(ovl, left, bot + ydiff, equipmentOverlaysColors[i]);
            }
        }
    }
}