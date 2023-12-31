package impcity.game.ui;

import impcity.game.ImpCity;
import impcity.game.Texture;
import impcity.game.quests.Quest;
import impcity.ogl.IsoDisplay;
import impcity.utils.StringUtils;
import java.io.IOException;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author hjm
 */
public class QuestBook extends UiDialog
{
    public static final Logger LOG = Logger.getLogger(QuestBook.class.getName());
    
    private final GameDisplay gameDisplay;
    private final ImpCity game;
    private final IsoDisplay display;
    private int selection = 0;
    private int currentPage = 0;
    private int maxPages = 0;
    private static Texture castle;
    private static Texture riverscape;

    
    public QuestBook(IsoDisplay display, GameDisplay gameDisplay, ImpCity game)
    {
        super(display.textureCache, 800, 600);
        this.display = display;
        this.gameDisplay = gameDisplay;
        this.game = game;

        if(castle == null)
        {
            try 
            {
                castle = display.textureCache.loadTexture("/ui/castle.png", true);
                riverscape = display.textureCache.loadTexture("/ui/river_house.png", true);
            }
            catch (IOException e) 
            {
                LOG.log(Level.SEVERE, "IOException while loading textures.", e);
            }
        }        
    }
    

    @Override
    public void display(int x, int y)
    {
        int gold = Colors.BRIGHT_GOLD_INK;
        int silver = Colors.BRIGHT_SILVER_INK;
        int count = game.quests.size();
        maxPages = count / 3;

        int rightX = x + 400;
        int pageWidth = 400;
        int pageHeight = 600;
        int textTop = y + pageHeight - 75;
        int textLeft = 35;
        int boxWidth = pageWidth - textLeft * 2;
        int linespace = 24;
        
        super.displayDoublePage(x, y);

        displayLeftPage(gold, silver, x, textTop, textLeft, boxWidth, linespace);
        displayRightPage(gold, silver, rightX, textTop, textLeft, boxWidth, linespace);
    }


    private void displayLeftPage(int gold, int silver, int leftX, int textTop, int textLeft, int boxWidth, int linespace)
    {
        List <Quest> quests = game.quests;

        // headline
        gameDisplay.drawShadowText("Fabled Locations", gold, leftX + textLeft, textTop, 0.40);
        gameDisplay.drawMenuText("< Page " +  (currentPage + 1) + " of " + (maxPages + 1) + " >", gold, leftX + textLeft, textTop - 500, 0.6);

        textTop -= 40;

        // left page
        for(int i=currentPage*3; i<Math.min(currentPage * 3 + 3, quests.size()); i++)
        {
            Quest quest = quests.get(i);
            String headline = quest.treasureType == Quest.TT_ARTIFACT ? 
                    StringUtils.upperCaseFirst(quest.treasureName) : quest.locationName;
            int lines;

            if(i == selection)
            {
                // hack: find height without actually showing the text - draw invisible
                lines = gameDisplay.drawBoxedShadowText(headline, 0, leftX + textLeft, textTop, boxWidth, linespace *4, 0.25);

                int boxH = 120 + lines * linespace;
                IsoDisplay.fillRect(leftX + textLeft - 10, textTop - boxH + linespace + 12, boxWidth + 20, boxH, 0x10FFCC99);
                // IsoDisplay.fillRect(leftX + textLeft - 10, textTop, boxWidth + 20, 1, 0x77FFFFFF);
            }

            lines = gameDisplay.drawBoxedShadowText(headline, Colors.WHITE, leftX + textLeft, textTop, boxWidth, linespace *4, 0.25);

            textTop -= 30 + lines * linespace;
            gameDisplay.drawShadowText("Location: " + difficulty(quest.findingDifficulty, 17), silver, leftX + textLeft, textTop, 0.20);
            gameDisplay.drawShadowText("Guarded: " + protection(quest.guardHardness, 16), silver, leftX + textLeft, textTop - linespace, 0.20);
            gameDisplay.drawShadowText("Expeditions: " + quest.expeditions, silver, leftX + textLeft, textTop - linespace*2, 0.20);

            gameDisplay.drawShadowText("Found: " + ((quest.status & Quest.SF_FOUND) != 0 ? "Yes" : "Not yet"), 
                                 silver, leftX + textLeft + 150, textTop - linespace*2, 0.20);

            if((quest.status & Quest.SF_PLUNDERED) != 0)
            {
                gameDisplay.drawShadowText("PLUNDERED", Colors.DARK_RED_INK, 
                                           leftX + textLeft - 6, textTop - linespace, 0.60);
            }

            textTop -= 100;
            
            
        }
    }


    private void displayRightPage(int gold, int silver, int rightX, int textTop, int textLeft, int boxWidth, int linespace)
    {
        gameDisplay.drawMenuText("[X]", gold, rightX + 350, textTop + 36, 0.6);

        gameDisplay.drawMenuText("> Assemble Party", gold,
                    rightX + textLeft, textTop - 500, 0.6);
        
        // right page starts with selected quest story
        if(game.quests.size() > selection)
        {
            Quest quest = game.quests.get(selection);

            if(quest.locationIsBuilding)
            {
                IsoDisplay.drawTile(castle, rightX, textTop-castle.image.getHeight() + 50, 0xFFAAAAAA);
            }
            else
            {
                IsoDisplay.drawTile(riverscape, rightX, textTop-riverscape.image.getHeight() + 60, 0xFFFFFFFF);
            }
            
            textTop -= 220;

            gameDisplay.drawBoxedShadowText(quest.story, silver,
                    rightX + textLeft, textTop + 10, boxWidth,
                    linespace * 5, 0.20);
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
                int n = mouseY - (display.displayHeight / 2) + 165;
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

                if ((quest.status & Quest.SF_PLUNDERED) == 0)
                {
                    PartySelector partySelector = new PartySelector(game, gameDisplay, display);
                    partySelector.setQuest(quest);
                    gameDisplay.showDialog(partySelector);
                    playClickSound();
                }
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
