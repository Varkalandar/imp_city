package impcity.game.ui;



/**
 *
 * @author Hj. Malthaner
 */
public class GenericMessage extends PaperMessage
{
    private final GameDisplay gameDisplay;


    public GenericMessage(GameDisplay gameDisplay, int width, int height,
                          String title, String message, String leftButton, String rightButton)
    {
        super(gameDisplay, width, height, title, message, leftButton, rightButton);
        this.gameDisplay = gameDisplay;
    }

    @Override
    public void mouseEvent(int buttonPressed, int buttonReleased, int mouseX, int mouseY) 
    {
        if(buttonReleased == 1)
        {
            gameDisplay.showDialog(null);
            if(mouseX < gameDisplay.getDisplay().displayWidth / 2)
            {
                // Todo: Buttons?
                playClickSound();
            }
        }
    }
}
