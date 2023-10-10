package impcity.game.ui;

import impcity.game.ImpCity;
import impcity.game.quests.Quest;
import impcity.ogl.IsoDisplay;

import java.util.List;

/**
 *
 * @author hjm
 */
public class QuestBook extends UiDialog
{
    private final GameDisplay gameDisplay;
    private final ImpCity game;
    private final IsoDisplay display;
    private int selection = 0;
    private int currentPage = 0;
    private int maxPages = 0;
    
    public QuestBook(IsoDisplay display, GameDisplay gameDisplay, ImpCity game)
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
        maxPages = (count + 2) / 3;

        
        int leftX = x;
        int rightX = x + 400;
        int pageWidth = 400;
        int pageHeight = 600;
        int textTop = y + pageHeight - 75;
        int textLeft = 35;
        int boxWidth = pageWidth - textLeft * 2;
        int linespace = 24;
        
        // Overlap the pages in the middle
        IsoDisplay.drawTile(messagePaperBg, leftX, y, pageWidth+5, pageHeight, 0xFF55504B);
        IsoDisplay.drawTile(messagePaperBg, rightX-5, y, pageWidth+5, pageHeight, 0xFF55504B);

        displayLeftPage(gold, silver, leftX, rightX, textTop, textLeft, boxWidth, linespace);
        displayRightPage(gold, silver, leftX, rightX, textTop, textLeft, boxWidth, linespace);
    }

    private void displayLeftPage(int gold, int silver, int leftX, int rightX, int textTop, int textLeft, int boxWidth, int linespace)
    {
        List <Quest> quests = game.quests;

        // headline
        gameDisplay.drawShadowText("Fabled Locations", gold, leftX + textLeft, textTop, 0.40);
        gameDisplay.drawMenuText("<< Page " +  (currentPage + 1) + " of " + (maxPages + 1) + " >>", gold, leftX + textLeft, textTop - 500, 0.6);

        textTop -= 40;

        // left page
        for(int i=currentPage*3; i<Math.min(currentPage * 3 + 3, quests.size()); i++)
        {
            Quest quest = quests.get(i);
            int lines;

            if(i == selection)
            {
                // hack: find height without actually showing the text - draw invisible
                lines = gameDisplay.drawBoxedShadowText(quest.locationName, 0, leftX + textLeft, textTop, boxWidth, linespace *4, 0.25);

                int boxH = 120 + lines * linespace;
                IsoDisplay.fillRect(leftX + textLeft - 10, textTop - boxH + linespace + 12, boxWidth + 20, boxH, 0x10FFCC99);
                // IsoDisplay.fillRect(leftX + textLeft - 10, textTop, boxWidth + 20, 1, 0x77FFFFFF);
            }

            lines = gameDisplay.drawBoxedShadowText(quest.locationName, silver, leftX + textLeft, textTop, boxWidth, linespace *4, 0.25);

            textTop -= 30 + lines * linespace;
            gameDisplay.drawShadowText("Location: " +  difficulty(quest.findingDifficulty, 17), silver, leftX + textLeft, textTop, 0.20);
            gameDisplay.drawShadowText("Guards: " +  protection(quest.guardHardness, 16), silver, leftX + textLeft, textTop - linespace, 0.20);
            gameDisplay.drawShadowText("Expeditions: " +  0, silver, leftX + textLeft, textTop - linespace*2, 0.20);

            textTop -= 100;
        }
    }

    private void displayRightPage(int gold, int silver, int leftX, int rightX, int textTop, int textLeft, int boxWidth, int linespace)
    {
        gameDisplay.drawMenuText("[X]", gold, rightX + 350, textTop + 36, 0.6);

        // right page starts with selected quest story
        if(game.quests.size() > selection)
        {
            Quest quest = game.quests.get(selection);
            int lines =
                    gameDisplay.drawBoxedShadowText(quest.story, silver,
                            rightX + textLeft, textTop + 10, boxWidth,
                            linespace * 5, 0.20);

            gameDisplay.drawMenuText("> Assemble Party", gold,
                    rightX + textLeft, textTop - lines * linespace - linespace, 0.6);
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
            }
            else if(mouseX < display.displayWidth / 2 - 280)
            {
                // pagination clicked?
                if(currentPage > 0) currentPage --;
            }
            else if(mouseX < display.displayWidth / 2 - 100)
            {
                if(currentPage < maxPages) currentPage ++;
            }
            else if(mouseX > display.displayWidth / 2 && mouseY > display.displayHeight / 2 + 200)
            {
                // close button
                gameDisplay.showDialog(null);
            }
            else if(game.quests.size() > selection && mouseX > display.displayWidth / 2)
            {
            // start quest clicked?
                Quest quest = game.quests.get(selection);

                CreatureOverview creatureOverview = new CreatureOverview(game, gameDisplay, display, gameDisplay.getFontLow());
                creatureOverview.setQuest(quest);
                gameDisplay.showDialog(creatureOverview);
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
