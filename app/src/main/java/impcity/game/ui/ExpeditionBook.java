package impcity.game.ui;

import impcity.game.Clock;
import impcity.game.ImpCity;
import impcity.game.Party;
import impcity.game.Texture;
import impcity.game.mobs.Mob;
import impcity.game.quests.Quest;
import impcity.game.species.Species;
import impcity.game.species.SpeciesDescription;
import impcity.ogl.IsoDisplay;

import java.util.List;

/**
 *
 * @author hjm
 */
public class ExpeditionBook extends UiDialog
{
    private final GameDisplay gameDisplay;
    private final ImpCity game;
    private final IsoDisplay display;
    private int selection = 0;
    private int currentPage = 0;
    private int maxPages = 0;

    
    public ExpeditionBook(IsoDisplay display, GameDisplay gameDisplay, ImpCity game)
    {
        super(display.textureCache, 800, 600);
        this.display = display;
        this.gameDisplay = gameDisplay;
        this.game = game;
    }
    

    @Override
    public void display(int x, int y)
    {
        int gold = Colors.BRIGHT_GOLD_INK;
        int silver = Colors.BRIGHT_SILVER_INK;
        int count = game.quests.size();
        maxPages = count / 3;
        
        int leftX = x;
        int rightX = x + 400;
        int pageWidth = 400;
        int pageHeight = 600;
        int textTop = y + pageHeight - 75;
        int textLeft = 35;
        int boxWidth = pageWidth - textLeft * 2;
        int linespace = 24;
        
        super.displayDoublePage(x, y);

        displayLeftPage(gold, silver, leftX, rightX, textTop, textLeft, boxWidth, linespace);
        displayRightPage(gold, silver, leftX, rightX, textTop, textLeft, boxWidth, linespace);
    }

    private void displayLeftPage(int gold, int silver, int leftX, int rightX, int textTop, int textLeft, int boxWidth, int linespace)
    {
        List <Quest> quests = game.quests;

        // headline
        gameDisplay.drawShadowText("Ongoing Expeditions", gold, leftX + textLeft-10, textTop, 0.40);
        gameDisplay.drawMenuText("< Page " +  (currentPage + 1) + " of " + (maxPages + 1) + " >", gold, leftX + textLeft, textTop - 500, 0.6);

        textTop -= 40;

        
        int i = 0;
        for(Quest quest : game.quests)
        {
            if(quest.party != null)
            {
                int lines;
                String headline = "Find " + quest.locationName.toLowerCase();
                
                if(i == selection)
                {
                    // hack: find height without actually showing the text - draw invisible
                    lines = gameDisplay.drawBoxedShadowText(headline, 0, leftX + textLeft, textTop, boxWidth, linespace *4, 0.25);

                    int boxH = 90 + lines * linespace;
                    IsoDisplay.fillRect(leftX + textLeft - 10, textTop - boxH + linespace + 12, boxWidth + 20, boxH, 0x10FFCC99);
                    // IsoDisplay.fillRect(leftX + textLeft - 10, textTop, boxWidth + 20, 1, 0x77FFFFFF);
                }

                lines = gameDisplay.drawBoxedShadowText(headline, Colors.WHITE, leftX + textLeft, textTop, boxWidth, linespace *4, 0.25);

                textTop -= 30 + lines * linespace;
                gameDisplay.drawShadowText("Expected return: " +  (quest.travelTime * 2 / quest.party.speed - (Clock.days() - quest.startTime)) + " days", silver, leftX + textLeft, textTop, 0.20);

                textTop -= 60;
                i++;
            }
        }
    }

    private void displayRightPage(int gold, int silver, int leftX, int rightX, int textTop, int textLeft, int boxWidth, int linespace)
    {
        gameDisplay.drawMenuText("[X]", gold, rightX + 350, textTop + 36, 0.6);

        gameDisplay.drawShadowText("Party Members", gold, rightX + textLeft, textTop, 0.40);
        
        textTop -= 60;

        int i = 0;
        for(Quest quest : game.quests)
        {
            Party party = quest.party;
            
            if(party != null)
            {
                if(i == selection)
                {
                    // show members
                    for(int key : party.members)
                    {
                        Mob mob = game.world.mobs.get(key);
                        SpeciesDescription desc = Species.speciesTable.get(mob.getSpecies());
                        
                        Texture tex = display.textureCache.species[desc.baseImage+1];
                        IsoDisplay.drawTileStanding(tex, rightX + textLeft + 20, textTop + 4);

                        gameDisplay.drawShadowText(desc.name, silver, rightX + textLeft + 50, textTop, 0.20);
                        
                        // debug - show vitality
                        // gameDisplay.drawShadowText(desc.name + " " + mob.stats.getCurrent(MobStats.VITALITY), silver, rightX + textLeft + 50, textTop, 0.20);
                        textTop -= 48;
                    }
                }

                i++;                
            }
        }
    }

        @Override
    public void mouseEvent(int buttonPressed, int buttonReleased, int mouseX, int mouseY) 
    {
        if(buttonReleased == 1)
        {
            if(mouseX < display.displayWidth / 2 && mouseY > display.displayHeight / 2 - 230)
            {
                // selection
                int n = mouseY - (display.displayHeight / 2) + 100;
                n = n / 140;  // 140 = Quest box
                n = 2 - n;  // count upside down

                // System.err.println("selection=" + n);
                selection = currentPage * 3 + Math.max(0, Math.min(n, 2));
                playClickSound();
            }
            else if(mouseX < display.displayWidth / 2 - 280)
            {
                // pagination clicked?
                if(currentPage > 0) currentPage --;
                playClickSound();
            }
            else if(mouseX < display.displayWidth / 2 - 100)
            {
                if(currentPage < maxPages) currentPage ++;
                playClickSound();
            }
            else if(mouseX > display.displayWidth / 2 && mouseY > display.displayHeight / 2 + 200)
            {
                // close button
                gameDisplay.showDialog(null);
                playClickSound();
            }
            else if(game.quests.size() > selection && mouseX > display.displayWidth / 2)
            {
            // start quest clicked?
                Quest quest = game.quests.get(selection);

                CreatureOverview creatureOverview = new CreatureOverview(game, gameDisplay, display);
                creatureOverview.setQuest(quest);
                gameDisplay.showDialog(creatureOverview);
                playClickSound();
            }
        }
    }

    private String difficulty(int actual, int max)
    {
        final String [] words = {"Impossible to miss", "Easy to find", "Well described", "Not obvious", "Difficult to find", "Obscure"};
        return words[actual * words.length / max];
    }

    private String protection(int actual, int max)
    {
        final String [] words = {"Unguarded", "Few or no guards", "Some guards", "Well protected", "Strongly protected"};
        return words[actual * words.length / max];
    }

}
