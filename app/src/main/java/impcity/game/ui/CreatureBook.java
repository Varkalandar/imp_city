package impcity.game.ui;

import impcity.game.*;
import impcity.game.ai.Ai;
import impcity.game.species.Species;
import impcity.game.species.SpeciesDescription;

import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.function.IntPredicate;

import impcity.game.mobs.Mob;
import impcity.ogl.IsoDisplay;
import java.util.HashSet;
import rlgamekit.objects.Cardinal;
import rlgamekit.objects.Registry;


/**
 * A selectable list of all creatures of a player.
 * 
 * @author Hj. Malthaner
 */
public class CreatureBook extends UiDialog
{
    private static final Logger logger = Logger.getLogger(CreatureBook.class.getName());
    private final ArrayList <Entry> creatureDisplayList = new ArrayList<>(64);

    private final ImpCity game;
    private final IsoDisplay display;
    private final GameDisplay gameDisplay;
    
    private int maxPages;
    private int currentPage;
    private int selectedCreatureKey;
    
    
    public CreatureBook(ImpCity game, GameDisplay gameDisplay, IsoDisplay display)
    {
        super(display.textureCache, 800, 600);
        this.game = game;
        this.gameDisplay = gameDisplay;
        this.display = display;
    }
    
    
    @Override
    public void display(int x, int y)
    {
        super.displayDoublePage(x, y);
        maxPages = displayCreaturePage(x, y, width, height, currentPage, game, gameDisplay, display,
                                       (int key) -> {return key == selectedCreatureKey;},
                                       creatureDisplayList);   
        
        gameDisplay.drawShadowText("Creature Stats",
                                   Colors.BRIGHT_GOLD_INK, x+width/2+28, y+height-80, 0.4);
        
        gameDisplay.drawMenuText("[X]", Colors.BRIGHT_GOLD_INK, x + width/2 + 350, y + 570, 0.6);


        int silver = Colors.BRIGHT_SILVER_INK;
        int xoff = width/2 + 28;
        int yoff = 460;
        int col2 = 160;
        int yspace = 36;

        Mob mob = game.world.mobs.get(selectedCreatureKey);
        if (mob != null)
        {
            int species = mob.getSpecies();
            double factor = 1 + mob.getLevel() * 0.3;
            SpeciesDescription desc = Species.speciesTable.get(species);

            int intelligence = (int)(desc.intelligence * factor);
            int combat = (int)(desc.combat * factor);
            int stealth = (int)(desc.stealth * factor);
            int speed = (int)(desc.speed * factor);
            int scouting = (int)((desc.speed + desc.intelligence) * 0.5 * factor);
            int carry = mob.isGhost() ? 0 : 1;

            gameDisplay.drawShadowText("Intelligence:", silver, x+xoff, y+yoff, 0.25);
            gameDisplay.drawShadowText("" + intelligence, silver, x+xoff + col2, y+yoff, 0.25);
            yoff -= yspace;

            gameDisplay.drawShadowText("Stealth:", silver, x+xoff, y+yoff, 0.25);
            gameDisplay.drawShadowText("" + stealth, silver, x+xoff + col2, y+yoff, 0.25);
            yoff -= yspace;

            gameDisplay.drawShadowText("Combat:", silver, x+xoff, y+yoff, 0.25);
            gameDisplay.drawShadowText("" + combat, silver, x+xoff + col2, y+yoff, 0.25);
            yoff -= yspace;

            gameDisplay.drawShadowText("Scouting:", silver, x+xoff, y+yoff, 0.25);
            gameDisplay.drawShadowText("" + scouting, silver, x+xoff + col2, y+yoff, 0.25);
            yoff -= yspace;

            gameDisplay.drawShadowText("Carry:", silver, x+xoff, y+yoff, 0.25);
            gameDisplay.drawShadowText("" + carry, silver, x+xoff + col2, y+yoff, 0.25);
            yoff -= yspace;

            gameDisplay.drawShadowText("Speed:", silver, x+xoff, y+yoff, 0.25);
            gameDisplay.drawShadowText("" + speed, silver, x+xoff + col2, y+yoff, 0.25);
        }
    }
    
    
    public static int displayCreaturePage(int x, int y, int width, int height, int currentPage, 
                                          ImpCity game, GameDisplay gameDisplay, IsoDisplay display,
                                          IntPredicate selector,
                                          ArrayList <Entry> creatureDisplayList)
    {
        int gold = Colors.BRIGHT_GOLD_INK;
        
        creatureDisplayList.clear();
        
        gameDisplay.drawShadowText("Your Creatures",
                              gold, x+28, y+height-80, 0.4);
        
        int row = 500;
        int col = 20;
        int count = 0;

        // work on copy, the original set might change during the loop
        Registry <Mob> mobs = game.world.mobs;
        Set <Cardinal> keys = new HashSet<>(mobs.keySet());

        for(Cardinal key : keys)
        {
            Mob mob = mobs.get(key.intValue());
            Ai ai = mob.getAi();

            if(mob.getKey() != game.getPlayerKey() &&
               ai != null &&
               ai.isLair(mob, ai.getHome().x, ai.getHome().y))
            {
                int species = mob.getSpecies();
                if(species > 0 && species != Species.IMPS_BASE)
                {
                    if(count >= currentPage * 10 && count < currentPage * 10 + 10)
                    {
                        drawCreatureTile(x + col, row, gameDisplay, display, mob, selector.test(key.intValue()));
                        creatureDisplayList.add(new Entry(mob.getKey(), x + col, row));

                        col += 170;
                        if (col > width / 2 - 169) {
                            col = 20;
                            row -= 88;
                        }
                    }

                    count ++;
                }
            }
        }

        // We list 10 creatures per page
        int maxPages = creatureDisplayList.size() / 10;


        gameDisplay.drawMenuText("< Page " +  (currentPage + 1) + " of " + (maxPages + 1) + " >", gold, x + 28, y + 22, 0.6);
    
        return maxPages;
    }
    

    private static void drawCreatureTile(int x, int y, GameDisplay gameDisplay, IsoDisplay display, Mob mob, boolean selected)
    {
        SpeciesDescription desc = Species.speciesTable.get(mob.getSpecies());

        int tw = 160;
        int th = 80;
        
        if(selected)
        {
            IsoDisplay.fillRect(x, y, tw, th, 0x77000000);
            IsoDisplay.fillRect(x+1, y + 1, tw-2, th-2, 0x33FFCC99);
        }

        Texture tex = display.textureCache.species[desc.baseImage+1];

        IsoDisplay.drawTileStanding(tex, x + tw/2, y + 32);

        String text = "Level " + mob.getLevel() + " " + desc.name;
        int w = (int)(gameDisplay.getFontLow().getStringWidth(text) * 0.18);
        gameDisplay.drawShadowText(text, 
                              Colors.BRIGHT_SILVER_INK, x + (tw - w) / 2, y + 4, 0.18);
    }
    

    @Override
    public void mouseEvent(int buttonPressed, int buttonReleased, int mouseX, int mouseY) 
    {
        if(buttonReleased == 1)
        {
            // left or right page?

            if(mouseX < display.displayWidth / 2)
            {
                if (mouseY < 180)
                {
                    // pagination clicked?
                    if(mouseX < display.displayWidth / 2 - 280)
                    {
                        playClickSound();
                        if(currentPage > 0) currentPage --;
                    }
                    else if(mouseX < display.displayWidth / 2 - 100)
                    {
                        playClickSound();
                        if(currentPage < maxPages) currentPage ++;
                    }
                }
                else
                {
                    playClickSound();
                    updateCreatureSelection(mouseX, mouseY);
                }
            }
            else
            {
                if(mouseY > display.displayHeight / 2 + 200)
                {
                    // close button
                    gameDisplay.showDialog(null);
                    playClickSound();
                }
            }
        }
    }
    

    private void updateCreatureSelection(int mouseX, int mouseY)
    {
        for (Entry entry : creatureDisplayList)
        {
            if (mouseX >= entry.x && mouseY >= entry.y &&
                mouseX < entry.x + 160 && mouseY < entry.y + 80)
            {
                if (selectedCreatureKey == entry.key)
                {
                    selectedCreatureKey = 0;
                }
                else
                {
                    selectedCreatureKey = entry.key;
                }
            }
        }
    }


    public static class Entry
    {
        public final int key, x, y;
        
        public Entry(int key, int x, int y)
        {
            this.key = key;
            this.x = x;
            this.y = y;
        }
    }
}
