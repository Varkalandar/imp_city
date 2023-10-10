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
import impcity.ui.PixFont;
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
    private final PixFont font;
    private final Party party;
    private final ArrayList <Entry> creatureDisplayList;
    
    private Quest quest;
    
    
    public CreatureOverview(ImpCity game, GameDisplay gameDisplay, IsoDisplay display, PixFont font)
    {
        super(display.textureCache, 1140, 620);
        this.game = game;
        this.gameDisplay = gameDisplay;
        this.display = display;
        this.font = font;
        
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
        super.display(x, y);
        
        int gold = Colors.BRIGHT_GOLD_INK;
        int silver = Colors.BRIGHT_SILVER_INK;
        
        creatureDisplayList.clear();
        
        gameDisplay.drawShadowText("Select Creatures",
                              gold, x+400, y+540, 0.5);
        
        Registry <Mob> mobs = game.world.mobs;
        
        Set <Cardinal> keys = mobs.keySet();
        
        int row = 480;
        
        for(Cardinal key : keys)
        {
            Mob mob = mobs.get(key.intValue());
            
            if(mob.getKey() != game.getPlayerKey())
            {
                int species = mob.getSpecies();
                if(species > 0 && species != Species.IMPS_BASE)
                {
                    SpeciesDescription desc = Species.speciesTable.get(species);
                    Texture tex = display.textureCache.species[species+1];

                    if(party.members.contains(key.intValue()))
                    {
                        IsoDisplay.fillRect(x + 50, y + row-2, 260, 38, 0x77000000);
                        IsoDisplay.fillRect(x + 50+1, y + row - 1, 258, 36, 0x77FFFFFF);
                    }
                    
                    IsoDisplay.drawTileStanding(tex, x + 80, y + row);

                    gameDisplay.drawShadowText("Level 1 " + desc.name, 
                                          silver, x + 105, y + row, 0.28);
                    
                    creatureDisplayList.add(new Entry(mob.getKey(), x + 50, y + row - 4));

                    row -= 40;
                }
            }
        }
        
        int xoff = 780;
        int yoff = 480;
        int col2 = 160;
        int yspace = 36;
        
        gameDisplay.drawMenuText("Your Party Stats", gold, x+xoff, y+yoff, 0.6);
        yoff -= 44;
        
        gameDisplay.drawMenuText("Intelligence:", silver, x+xoff, y+yoff, 0.6);
        gameDisplay.drawMenuText("" + party.intelligence, silver, x+xoff + col2, y+yoff, 0.6);
        yoff -= yspace;
        
        gameDisplay.drawMenuText("Stealth:", silver, x+xoff, y+yoff, 0.6);
        gameDisplay.drawMenuText("" + party.stealth, silver, x+xoff + col2, y+yoff, 0.6);
        yoff -= yspace;
        
        gameDisplay.drawMenuText("Combat:", silver, x+xoff, y+yoff, 0.6);
        gameDisplay.drawMenuText("" + party.combat, silver, x+xoff + col2, y+yoff, 0.6);
        yoff -= yspace;

        gameDisplay.drawMenuText("Carry:", silver, x+xoff, y+yoff, 0.6);
        gameDisplay.drawMenuText("" + party.carry, silver, x+xoff + col2, y+yoff, 0.6);
        yoff -= yspace;

        gameDisplay.drawMenuText("Speed:", silver, x+xoff, y+yoff, 0.6);
        gameDisplay.drawMenuText("" + party.speed, silver, x+xoff + col2, y+yoff, 0.6);
        yoff -= yspace;

        
        gameDisplay.drawMenuText("[ Ready ]", gold, x+540, y+40, 0.6);
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
                       mouseX < entry.x + 260 && mouseY < entry.y + 40)
                    {
                        if(party.members.contains(entry.key))
                        {
                            party.members.remove(entry.key);
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
