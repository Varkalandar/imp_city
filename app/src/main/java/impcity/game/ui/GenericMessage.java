package impcity.game.ui;

import impcity.game.ImpCity;
import impcity.ogl.IsoDisplay;



/**
 *
 * @author Hj. Malthaner
 */
public class GenericMessage extends PaperMessage
{
    private final ImpCity game;
    private final IsoDisplay display;
    private final GameDisplay gameDisplay;


    public GenericMessage(ImpCity game, GameDisplay gameDisplay, IsoDisplay display, int width, int height,
                          String title, String message, String leftButton, String rightButton)
    {
        super(gameDisplay, display.textureCache, width, height, title, message, leftButton, rightButton);
        this.game = game;
        this.display = display;
        this.gameDisplay = gameDisplay;
    }

    @Override
    public void mouseEvent(int buttonPressed, int buttonReleased, int mouseX, int mouseY) 
    {
        if(buttonReleased == 1)
        {
            gameDisplay.showDialog(null);
            if(mouseX < display.displayWidth / 2)
            {
                // Todo: Buttons?
            }
        }
    }
}
