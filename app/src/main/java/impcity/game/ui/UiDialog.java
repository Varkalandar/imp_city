package impcity.game.ui;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import impcity.game.Texture;
import impcity.game.TextureCache;
import impcity.ogl.IsoDisplay;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author Hj. Malthaner
 */
public abstract class UiDialog
{
    private static final Logger logger = Logger.getLogger(UiDialog.class.getName());

    protected static Texture messagePaperBg;
    
    public final int width;
    public final int height;
    
    public UiDialog(TextureCache textureCache, int width, int height)
    {
        this.width = width;
        this.height = height;

        if(messagePaperBg == null)
        {
            try {
                // messagePaperBg = textureCache.loadTexture("/ui/fire_paper.png", true);
                messagePaperBg = textureCache.loadTexture("/ui/paper_bg.png", true);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Could not load: /ui/paper_bg.png");
            }
        }
    }
    
    public void display(int x, int y)
    {
        // IsoDisplay.drawTile(messagePaperBg, x, y, width, height, 0xFFCCCCCC);
        // IsoDisplay.drawTile(messagePaperBg, x, y, width, height, 0xFF776655);
        IsoDisplay.drawTile(messagePaperBg, x, y, width, height, 0xFF55504B);
    }

    public abstract void mouseEvent(int buttonPressed, int buttonReleased, int mouseX, int mouseY);

    void displayDoublePage(int x, int y) 
    {
        // Overlap the pages in the middle
        int hw = width/2;
        
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x, y, hw, height);
        IsoDisplay.drawTile(messagePaperBg, x, y, hw+5, height, 0xFF55504B);

        GL11.glScissor(x+hw, y, hw, height);
        IsoDisplay.drawTile(messagePaperBg, x+hw-5, y, hw+5, height, 0xFF55504B);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }
}
