package impcity.game.ui;

import impcity.game.ImpCity;
import impcity.game.quests.QuestResult;
import impcity.ogl.IsoDisplay;
import org.lwjgl.input.Mouse;

/**
 *
 * @author Hj. Malthaner
 */
public class QuestResultMessage extends PaperMessage
{
    private final GameDisplay gameDisplay;
    private final IsoDisplay display;

    private final QuestResult questResult;

    
    public QuestResultMessage(ImpCity game, GameDisplay gameDisplay, IsoDisplay display, int width, int height,
                              QuestResult result, String leftButton)
    {
        super(gameDisplay, width, height, "Quest Summary", result.summary, leftButton, "[ Show Details ]");
        this.display = display;
        this.gameDisplay = gameDisplay;
        this.questResult = result;
        
        // Hajo: clear wheeling buffer ...
        Mouse.getDWheel();
    }

    @Override
    public void display(int x, int y) 
    {
        int mouseY = Mouse.getY();
        
        if(Mouse.isButtonDown(0))
        {
            if(mouseY < display.displayHeight/2)
            {
                messageYOffset -= 2;
            }
            else
            {
                messageYOffset += 2;                
            }
        }
        
        messageYOffset -= Mouse.getDWheel() /2;
        
        if(messageYOffset < 0) messageYOffset = 0;

        super.display(x, y);
    }

    
    
    @Override
    public void mouseEvent(int buttonPressed, int buttonReleased, int mouseX, int mouseY) 
    {
        if(buttonReleased == 1)
        {
            if(mouseY < 180)
            {
                if(mouseX < display.displayWidth/2)
                {
                    gameDisplay.showDialog(null);
                }
                else
                {
                    if(message == questResult.story)
                    {
                        message = questResult.summary;
                        headline = "Quest Summary";
                        rightButton = "[ Show Details ]";
                        messageYOffset = 0;
                        setScaleFactor(0.2);
                    }
                    else
                    {
                        message = questResult.story;
                        headline = "Quest Details";
                        rightButton = "[ Show Summary ]";
                        messageYOffset = 0;
                        setScaleFactor(0.18);
                    }
                }
            }
        }
    }
}
