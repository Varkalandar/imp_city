package impcity.game.ui;

import impcity.game.TextureCache;
import impcity.game.quests.Quest;
import impcity.ogl.IsoDisplay;
import impcity.ui.PixFont;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author hjm
 */
public class QuestBook extends UiDialog
{
    private final ArrayList<Quest> quests = new ArrayList<>();
    private final GameDisplay gameDisplay;
    private int selection = 0;
    
    public QuestBook(IsoDisplay display, GameDisplay gameDisplay)
    {
        super(display.textureCache, 800, 600);
        this.gameDisplay = gameDisplay;
    }
    
    
    public void addQuest(Quest quest)
    {
        quests.add(quest);
    }

    public void display(int x, int y)
    {
        int gold = Colors.BRIGHT_GOLD_INK;
        int silver = Colors.BRIGHT_SILVER_INK;

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
        
        // headline
        gameDisplay.drawShadowText("Fabled Locations", gold, leftX + textLeft, textTop, 0.40);

        // right page starts with first quest story
        gameDisplay.drawBoxedShadowText(quests.get(0).story, silver, rightX + textLeft, textTop + 10, boxWidth, linespace*5, 0.20);

        textTop -= 40;
        
        // left page
        for(int i=0; i<quests.size(); i++)
        {
            Quest quest = quests.get(i);
            int lines;
            
            if(i == selection)
            {
                // hack: find height without actually showing the text - draw off screen            
                lines = gameDisplay.drawBoxedShadowText(quest.locationName, 0, leftX + textLeft, textTop, boxWidth, linespace*4, 0.25);

                int boxH = 70 + 30 + lines * linespace;
                IsoDisplay.fillRect(leftX + textLeft - 10, textTop-65, boxWidth + 20, boxH, 0x33000000);
            }
            
            lines = gameDisplay.drawBoxedShadowText(quest.locationName, silver, leftX + textLeft, textTop, boxWidth, linespace*4, 0.25);            
            
            textTop -= 30 + lines * linespace;
            gameDisplay.drawShadowText("Certainty: " +  percent(17 - quest.findingDifficulty, 17), silver, leftX + textLeft, textTop, 0.20);
            gameDisplay.drawShadowText("Expeditions: " +  0, silver, leftX + textLeft, textTop-linespace, 0.20);

            textTop -= 80;
            
            
            // silver = Colors.SILVER_INK;
        }
    }
    
    @Override
    public void mouseEvent(int buttonPressed, int buttonReleased, int mouseX, int mouseY) 
    {
        if(buttonReleased == 1)
        {
            gameDisplay.showDialog(null);
        }
    }

    private String percent(int actual, int max) 
    {
        int percent = actual * 100 / max;
        return "" + percent + "%";
    }
}
