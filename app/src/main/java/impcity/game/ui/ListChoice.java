package impcity.game.ui;

import java.util.List;
import impcity.game.TextureCache;
import impcity.ogl.IsoDisplay;
import java.util.function.IntConsumer;
import java.util.logging.Logger;

/**
 *
 * @author hjm
 */
public class ListChoice extends UiDialog
{
    private static final Logger logger = Logger.getLogger(ListChoice.class.getName());
    private final GameDisplay gameDisplay;    
    private final String title;
    private final List<String> choices;
    private final IntConsumer callback;
    private int topCached;
    private int selection = 0;
    
    public ListChoice(TextureCache textureCache, GameDisplay gameDisplay,
                      int width, int height,
                      String title, List<String> choices,
                      IntConsumer callback)
    {
        super(textureCache, width, height);
        this.gameDisplay = gameDisplay;
        this.title = title;
        this.choices = choices;
        this.callback = callback;
    }
    

    @Override
    public void display(int x, int y)
    {
        super.display(x, y);
    
        int top = y + height - 80;
        int left = x + 40;
        topCached = top;
        
        gameDisplay.drawShadowText(title, Colors.BRIGHT_GOLD_INK, left + 10, top, 0.4);
        
        top -= 30;

        if (selection >= 0 && selection < choices.size())
        {
            IsoDisplay.fillRect(left, top - selection * 35 - 4, width - 80, 35, 0x77000000);
            IsoDisplay.fillRect(left+1, top - selection * 35 - 3, width - 82, 33, 0x33FFCC99);
        }
        
        for(String choice : choices)
        {
            gameDisplay.drawShadowText(choice, Colors.BRIGHT_SILVER_INK, left + 10, top, 0.2);
            top -= 35;
        }
    }

    
    @Override
    public void mouseEvent(int buttonPressed, int buttonReleased, int mouseX, int mouseY) 
    {
        int top = topCached;
        int dy = top - mouseY;
        int n = dy / 35;
        
        selection = n;
        logger.info("Choice #" + selection + " was hovered");

        if(buttonReleased != 0)
        {
            callback.accept(n);
        }
    }
}
