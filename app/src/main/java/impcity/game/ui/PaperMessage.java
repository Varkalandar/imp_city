package impcity.game.ui;

import java.io.IOException;
import impcity.game.TextureCache;
import impcity.ui.PixFont;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author Hj. Malthaner
 */
public abstract class PaperMessage extends UiDialog
{
    protected final PixFont fontText;
    protected final PixFont fontUi;
    protected String message;
    protected String leftButton;
    protected String rightButton;
    protected String headline;
    private int linespace;
    
    protected int messageYOffset = 0;
    
    public PaperMessage(GameDisplay gameDisplay, TextureCache textureCache, int width, int height,
            String headline,
            String message, String leftButton, String rightButton)
    {
        super(textureCache, width, height);
        // this.font = display.font;
        this.fontUi = gameDisplay.getFontLow();
        this.fontText = gameDisplay.getFontHigh();
        this.linespace = this.fontText.getLinespace() * 85 / 100;
        this.headline = headline;
        this.message = message;
        this.leftButton = leftButton;
        this.rightButton = rightButton;
    }
    
    @Override
    public void display(int x, int y)
    {
        super.display(x, y);
        // int colorUi = 0x404040;
        int colorUi = 0x304060;
        int colorText = Colors.BLUE_INK;
        
        int headlineWidth = (int)(fontText.getStringWidth(headline) * 0.5); // * 0.8);
        fontText.drawStringScaled(headline, colorUi, x + (width - headlineWidth) / 2, y + height - 90, 0.5); // 0.8);
        
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x+40, y+110, width-80, height-200);

        fontText.drawText(message, colorText, x+40, y+height - 128 + messageYOffset, width - 80, linespace, 0.25); // 0.6);
        
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        
        fontText.drawStringScaled(leftButton, colorUi, x + 40, y + 40, 0.3); // 0.6);
        
        int rwidth = (int)(fontText.getStringWidth(rightButton) * 0.3);
        fontText.drawStringScaled(rightButton, colorUi, x + width - 40 - rwidth, y + 40, 0.3); // 0.6);
    }
}
