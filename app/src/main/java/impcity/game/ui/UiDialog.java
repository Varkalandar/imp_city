package impcity.game.ui;

import java.io.IOException;
import impcity.game.Texture;
import impcity.game.TextureCache;
import impcity.ogl.IsoDisplay;

/**
 *
 * @author Hj. Malthaner
 */
public abstract class UiDialog
{
    private static Texture messagePaperBg;
    
    public final int width;
    public final int height;
    
    public UiDialog(TextureCache textureCache, int width, int height) throws IOException
    {
        this.width = width;
        this.height = height;

        if(messagePaperBg == null)
        {
            messagePaperBg = textureCache.loadTexture("/impcity/resources/ui/paper_bg.png", true);
        }
    }
    
    public void display(int x, int y)
    {
        // IsoDisplay.drawTile(messagePaperBg, x, y, width, height, 0xFFFFFFFF);
        IsoDisplay.drawTile(messagePaperBg, x, y, width, height, 0xFFDDDDDD);
    }

    public abstract void mouseEvent(int buttonPressed, int buttonReleased, int mouseX, int mouseY);
}
