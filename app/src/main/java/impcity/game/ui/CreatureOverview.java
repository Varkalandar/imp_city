package impcity.game.ui;

import impcity.game.ImpCity;
import impcity.game.Party;
import impcity.game.quests.QuestProcessor;
import impcity.game.species.Species;
import impcity.game.species.SpeciesDescription;
import impcity.game.quests.Quest;
import java.util.ArrayList;
import java.util.Set;
import impcity.game.Texture;
import impcity.game.mobs.Mob;
import impcity.ogl.IsoDisplay;
import rlgamekit.objects.Cardinal;
import rlgamekit.objects.Registry;

/**
 * A selectable list of all creatures of a player.
 * 
 * @author Hj. Malthaner
 */
public class CreatureOverview extends UiDialog
{
    private final ImpCity game;
    private final IsoDisplay display;
    private final GameDisplay gameDisplay;
    private final Party party;
    private final ArrayList <Entry> creatureDisplayList;
    
    private Quest quest;
    
    
    public CreatureOverview(ImpCity game, GameDisplay gameDisplay, IsoDisplay display)
    {
        super(display.textureCache, 800, 600);
        this.game = game;
        this.gameDisplay = gameDisplay;
        this.display = display;
        
        this.party = new Party();
        this.creatureDisplayList = new ArrayList<Entry>(64);
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
        
        gameDisplay.drawShadowText("Your Creatures",
                              gold, x+28, y+height-80, 0.4);
        
        gameDisplay.drawShadowText("Your Party",
                              gold, x+width/2+28, y+height-80, 0.4);

        Registry <Mob> mobs = game.world.mobs;
        
        Set <Cardinal> keys = mobs.keySet();
        
        int row = 500;
        int col = 20;
        for(Cardinal key : keys)
        {
            Mob mob = mobs.get(key.intValue());
            
            if(mob.getKey() != game.getPlayerKey())
            {
                int species = mob.getSpecies();
                if(species > 0 && species != Species.IMPS_BASE)
                {
                    SpeciesDescription desc = Species.speciesTable.get(species);

                    drawCreatureTile(x + col, row, desc, party.members.contains(key.intValue()));
                    
                    creatureDisplayList.add(new Entry(mob.getKey(), x + col, row));

                    col += 170;
                    if(col > width/2 - 169)
                    {
                        col = 20;
                        row -= 88;
                    }
                }
            }
        }
        
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
        yoff -= yspace;
        
        gameDisplay.drawMenuText("> Start Expedition", gold, x + width/2 + 28, y + 46, 0.6);
        gameDisplay.drawMenuText("> Cancel", gold, x + width/2 + 28, y + 22, 0.6);
    }    

    private void drawCreatureTile(int x, int y, SpeciesDescription desc, boolean selected)
    {
        int tw = 160;
        int th = 80;
        
        if(selected)
        {
            IsoDisplay.fillRect(x, y, tw, th, 0x77000000);
            IsoDisplay.fillRect(x+1, y + 1, tw-2, th-2, 0x33FFCC99);
        }

        Texture tex = display.textureCache.species[desc.baseImage+1];

        IsoDisplay.drawTileStanding(tex, x + tw/2, y + 32);

        String text = "Level 1 " + desc.name;
        int w = (int)(gameDisplay.getFontLow().getStringWidth(text) * 0.18);
        gameDisplay.drawShadowText(text, 
                              Colors.BRIGHT_SILVER_INK, x + (tw - w) / 2, y + 4, 0.18);
    }

    @Override
    public void mouseEvent(int buttonPressed, int buttonReleased, int mouseX, int mouseY) 
    {
        if(buttonReleased == 1)
        {
            if(mouseY < 180)
            {
                QuestProcessor processor = new QuestProcessor();
                /*
                // Hajo: this calculates the quest durarion
                quest.party = party;
                processor.createLog(game.world, quest);
                quest.eta = Clock.days() + quest.duration;

                game.quests.add(quest);
                */
                gameDisplay.showDialog(null);
                
                
                // QuestResult result = processor.createLog(game.world, quest, party);
                // System.out.println(result.story);
                // QuestResultMessage qrm = new QuestResultMessage(game, display, font, 600, height, result, "[ Ok ]");
                // display.showDialog(qrm);
            }
            else
            {
                for(Entry entry : creatureDisplayList)
                {
                    if(mouseX >= entry.x && mouseY >= entry.y &&
                       mouseX < entry.x + 160 && mouseY < entry.y + 80)
                    {
                        if(party.members.contains(entry.key))
                        {
                            party.members.remove((Object)entry.key);
                        }
                        else
                        {
                            party.members.add(entry.key);
                        }
                        
                        party.calculateStats(game.world.mobs);
                    }
                }
            }
        }
    }

    
    private class Entry
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
