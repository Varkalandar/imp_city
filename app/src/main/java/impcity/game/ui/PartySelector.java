package impcity.game.ui;

import impcity.game.*;
import impcity.game.ai.Ai;
import impcity.game.ai.AiBase;
import impcity.game.quests.QuestProcessor;
import impcity.game.species.Species;
import impcity.game.species.SpeciesDescription;
import impcity.game.quests.Quest;
import impcity.game.ui.CreatureBook.Entry;

import java.awt.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import impcity.game.mobs.Mob;
import impcity.ogl.IsoDisplay;



/**
 * A selectable list of all creatures of a player.
 * 
 * @author Hj. Malthaner
 */
public class PartySelector extends UiDialog
{
    private static final Logger logger = Logger.getLogger(PartySelector.class.getName());

    private final ImpCity game;
    private final IsoDisplay display;
    private final GameDisplay gameDisplay;
    private final Party party;
    private final ArrayList <Entry> creatureDisplayList;
    
    private Quest quest;
    private int currentPage;
    private int maxPages;


    public PartySelector(ImpCity game, GameDisplay gameDisplay, IsoDisplay display)
    {
        super(display.textureCache, 800, 600);
        this.game = game;
        this.gameDisplay = gameDisplay;
        this.display = display;
        
        this.party = new Party();
        this.creatureDisplayList = new ArrayList<>(64);
    }
    
    
    public void setQuest(Quest quest)
    {
        this.quest = quest;
    }
    
    
    @Override
    public void display(int x, int y)
    {
        super.displayDoublePage(x, y);
        
        int gold = Colors.BRIGHT_GOLD_INK;
        int silver = Colors.BRIGHT_SILVER_INK;
        
        creatureDisplayList.clear();

        maxPages = CreatureBook.displayCreaturePage(x, y, width, height, currentPage, game, gameDisplay, display,
                                                    (int key) -> {return party.members.contains(key);},
                                                    creatureDisplayList);
        
        // gameDisplay.drawShadowText("Your Creatures",
        //                       gold, x+28, y+height-80, 0.4);
        
        gameDisplay.drawShadowText("Your Party",
                              gold, x+width/2+28, y+height-80, 0.4);

        int xoff = width/2 + 28;
        int yoff = 460;
        int col2 = 160;
        int yspace = 36;
        
        gameDisplay.drawShadowText("Combined Stats", gold, x+xoff, y+yoff, 0.3);
        yoff -= 44;
        
        gameDisplay.drawShadowText("Intelligence:", silver, x+xoff, y+yoff, 0.25);
        gameDisplay.drawShadowText("" + party.intelligence, silver, x+xoff + col2, y+yoff, 0.25);
        yoff -= yspace;
        
        gameDisplay.drawShadowText("Stealth:", silver, x+xoff, y+yoff, 0.25);
        gameDisplay.drawShadowText("" + party.stealth, silver, x+xoff + col2, y+yoff, 0.25);
        yoff -= yspace;
        
        gameDisplay.drawShadowText("Combat:", silver, x+xoff, y+yoff, 0.25);
        gameDisplay.drawShadowText("" + party.combat, silver, x+xoff + col2, y+yoff, 0.25);
        yoff -= yspace;

        gameDisplay.drawShadowText("Scouting:", silver, x+xoff, y+yoff, 0.25);
        gameDisplay.drawShadowText("" + party.scouting, silver, x+xoff + col2, y+yoff, 0.25);
        yoff -= yspace;

        gameDisplay.drawShadowText("Carry:", silver, x+xoff, y+yoff, 0.25);
        gameDisplay.drawShadowText("" + party.carry, silver, x+xoff + col2, y+yoff, 0.25);
        yoff -= yspace;

        gameDisplay.drawShadowText("Speed:", silver, x+xoff, y+yoff, 0.25);
        gameDisplay.drawShadowText("" + party.speed, silver, x+xoff + col2, y+yoff, 0.25);
        yoff -= yspace * 1.5;

        gameDisplay.drawShadowText("Estimated travel time", gold, x+xoff, y+yoff, 0.3);
        yoff -= 44;

        if(party.speed > 0)
        {
            gameDisplay.drawShadowText("About " + quest.travelTime * 2 / party.speed + " days", 
                    silver, x+xoff, y+yoff, 0.25);
        }
        
        // gameDisplay.drawMenuText("< Page " +  (currentPage + 1) + " of " + (maxPages + 1) + " >", gold, x + 28, y + 22, 0.6);

        int color = party.members.isEmpty() ? Colors.DIM_GOLD_INK : gold;
        gameDisplay.drawMenuText("> Start Expedition", color, x + width/2 + 28, y + 22, 0.6);

        gameDisplay.drawMenuText("[X]", gold, x + width/2 + 350, y + 570, 0.6);
    }
    

    private void drawCreatureTile(int x, int y, Mob mob, boolean selected)
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
                if (mouseY < 180)
                {
                    if(!party.members.isEmpty())
                    {
                        playClickSound();
                        sendParty();
                    }
                }
                else if(mouseY > display.displayHeight / 2 + 200)
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
                if (party.members.contains(entry.key))
                {
                    int i = party.members.indexOf(entry.key);
                    party.members.remove(i);
                }
                else
                {
                    party.members.add(entry.key);
                }

                party.calculateStats(game.world.mobs);
            }
        }
    }
    

    private void sendParty()
    {
        // Preparations inside the dungeon

        for (int mobId : party.members)
        {
            // first, bring the creatures to their lairs
            Mob mob = game.world.mobs.get(mobId);
            Ai ai = mob.getAi();
            Point p = ai.getHome();
            mob.teleportTo(p);

            if (!mob.location.equals(p))
            {
                logger.log(Level.SEVERE, "Mob was not properly sent home.");
            }
            if (!((AiBase)ai).isLair(mob, mob.location.x, mob.location.y))
            {
                logger.log(Level.SEVERE, "Mob was not properly sent to its lair.");
            }

            // then, make them inactive
            mob.setPath(null);
            mob.setAi(null);

            // show coat of arms as indicator
            mob.visuals.setBubble(0);
            mob.visuals.setDisplayCode(Features.I_EXPEDITION_BANNER);
        }

        quest.startTime = Clock.days();
        QuestProcessor processor = new QuestProcessor();

        // Hajo: this calculates the quest duration
        quest.party = party;
        processor.createLog(game.world, quest);
        quest.eta = Clock.days() + quest.duration;

        gameDisplay.showDialog(null);
        
        // Just send one more
        quest.expeditions ++;
        
        // debug - show result immediately
        /*
        QuestResult result = processor.createLog(game.world, quest);
        System.out.println(result.story);
        QuestResultMessage qrm = new QuestResultMessage(game, gameDisplay, display, 600, height, result, "[ Ok ]");
        gameDisplay.showDialog(qrm);
        */
    }
}
