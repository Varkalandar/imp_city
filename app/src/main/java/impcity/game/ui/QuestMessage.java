package impcity.game.ui;

import impcity.game.ImpCity;
import impcity.game.quests.Quest;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import impcity.ogl.IsoDisplay;


/**
 *
 * @author Hj. Malthaner
 */
public class QuestMessage extends PaperMessage
{
    private final ImpCity game;
    private final IsoDisplay display;
    private final GameDisplay gameDisplay;
    private final Quest quest;
    
    public QuestMessage(ImpCity game, GameDisplay gameDisplay, IsoDisplay display, int width, int height,
            Quest quest, String title, String leftButton, String rightButton)
    {
        super(gameDisplay, display.textureCache, width, height, title, quest.story, leftButton, rightButton);
        this.game = game;
        this.display = display;
        this.gameDisplay = gameDisplay;
        this.quest = quest;
    }

    @Override
    public void mouseEvent(int buttonPressed, int buttonReleased, int mouseX, int mouseY) 
    {
        if(buttonReleased == 1)
        {
            gameDisplay.showDialog(null);
            if(mouseX < display.displayWidth / 2)
            {
                try
                {
                    CreatureOverview creatureOverview = new CreatureOverview(game, gameDisplay, display, fontText);
                    creatureOverview.setQuest(quest);
                    gameDisplay.showDialog(creatureOverview);
                }
                catch (IOException ex) {
                    Logger.getLogger(QuestMessage.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else
            {
                gameDisplay.openQuestBook(quest);
            }
        }
    }
}
