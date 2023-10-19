package impcity.game.ui;

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
    private final GameDisplay gameDisplay;
    protected String message;
    protected String leftButton;
    protected String rightButton;
    protected String headline;
    private final int linespace;
    private double scaleFactor = 0.2;
    
    protected int messageYOffset = 0;
    
    public PaperMessage(GameDisplay gameDisplay, int width, int height,
            String headline,
            String message, String leftButton, String rightButton)
    {
        super(gameDisplay.getDisplay().textureCache, width, height);
        this.gameDisplay = gameDisplay;
        this.fontUi = gameDisplay.getUiFont();
        this.fontText = gameDisplay.getFontLow();
        this.linespace = this.fontText.getLinespace() * 85 / 100;
        this.headline = headline;
        this.message = message;
        this.leftButton = leftButton;
        this.rightButton = rightButton;
    }

    public void setScaleFactor(double factor)
    {
        this.scaleFactor = factor;
    }

    @Override
    public void display(int x, int y)
    {
        super.display(x, y);
        int colorUi = Colors.BRIGHT_GOLD_INK;
        int colorText = Colors.BRIGHT_SILVER_INK;

        // headline

        int headlineWidth = (int)(fontText.getStringWidth(headline) * 0.5); // * 0.8);
        // fontUi.drawStringScaled(headline, colorUi, x + (width - headlineWidth) / 2, y + height - 90, 1.0); // 0.8);
        gameDisplay.drawShadowText(headline, colorUi, x + (width - headlineWidth) / 2, y + height - 80, 0.5); // 0.8);

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x+40, y+110, width-80, height-200);

        gameDisplay.drawBoxedShadowText(message, colorText, x+40, y+height - 128 + messageYOffset, width - 80, linespace, scaleFactor);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // buttons
        if(rightButton == null)
        {
            int bwidth = (int)(fontUi.getStringWidth(leftButton) * 0.6);
            gameDisplay.drawMenuText(leftButton, colorUi, x + (width - bwidth) / 2, y + 40, 0.6);
        }
        else
        {
            gameDisplay.drawMenuText(leftButton, colorUi, x + 40, y + 40, 0.6); // 0.6);

            int rwidth = (int)(fontUi.getStringWidth(rightButton) * 0.6);
            gameDisplay.drawMenuText(rightButton, colorUi, x + width - 40 - rwidth, y + 40, 0.6);
        }
    }
}