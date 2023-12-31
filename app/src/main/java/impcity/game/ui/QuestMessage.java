package impcity.game.ui;

import impcity.game.ImpCity;
import impcity.game.quests.Quest;
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
        super(gameDisplay, width, height, title, quest.story, leftButton, rightButton);
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
                playClickSound();
                game.quests.add(quest);
                gameDisplay.openQuestBook();
            }
        }
    }
}
