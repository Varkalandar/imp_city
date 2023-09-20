package impcity.game.ui;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import impcity.game.ImpCity;
import impcity.game.Texture;
import impcity.game.TextureCache;
import impcity.ogl.IsoDisplay;

/**
 *
 * @author Hj. Malthaner
 */
public abstract class UiDialog
{
    private static final Logger logger = Logger.getLogger(UiDialog.class.getName());

    private static Texture messagePaperBg;
    
    public final int width;
    public final int height;
    
    public UiDialog(TextureCache textureCache, int width, int height)
    {
        this.width = width;
        this.height = height;

        if(messagePaperBg == null)
        {
            try {
                messagePaperBg = textureCache.loadTexture("/ui/paper_bg.png", true);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Could not load: /ui/paper_bg.png");
            }
        }
    }
    
    public void display(int x, int y)
    {
        // IsoDisplay.drawTile(messagePaperBg, x, y, width, height, 0xFFFFFFFF);
        IsoDisplay.drawTile(messagePaperBg, x, y, width, height, 0xFFDDDDDD);
    }

    public abstract void mouseEvent(int buttonPressed, int buttonReleased, int mouseX, int mouseY);
}
