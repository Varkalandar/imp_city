package impcity.game.ui;

import impcity.game.Texture;
import java.util.List;
import impcity.game.TextureCache;
import impcity.ogl.IsoDisplay;
import java.util.HashSet;
import java.util.function.IntConsumer;
import java.util.logging.Logger;


/**
 *
 * @author hjm
 */
public class ImageChoice extends UiDialog
{
    private static final Logger LOG = Logger.getLogger(ImageChoice.class.getName());
    private final GameDisplay gameDisplay;
    private final TextureCache textureCache;
    private final String title;
    private final int [] choices;
    private final String [] labels;
    private final IntConsumer callback;
    private final int topOffset;
    private final int leftOffset;
    private final int cellXSpacing;
    private int leftCached;
    private int selection = 0;

    
    public ImageChoice(TextureCache textureCache, GameDisplay gameDisplay,
                      int width, int height,
                      String title, 
                      int [] choices, String [] labels,
                      int topOffset, int leftOffset, int cellCSpacing,
                      IntConsumer callback)
    {
        super(textureCache, width, height);
        this.gameDisplay = gameDisplay;
        this.textureCache = textureCache;
        this.title = title;
        this.choices = choices;
        this.labels = labels;
        this.callback = callback;
        this.topOffset = topOffset;
        this.leftOffset = leftOffset;
        this.cellXSpacing = cellCSpacing;
    }
    

    @Override
    public void display(int x, int y)
    {
        super.display(x, y);
    
        int top = y + height - 80;
        int left = x + 50;
        leftCached = left;
        
        gameDisplay.drawShadowText(title, Colors.BRIGHT_GOLD_INK, left, top, 0.4);
        
        top -= topOffset;
/*
        if (selection >= 0 && selection < choices.size())
        {
            IsoDisplay.fillRect(left, top - selection * 35 - 4, width - 80, 35, 0x77000000);
            IsoDisplay.fillRect(left+1, top - selection * 35 - 3, width - 82, 33, 0x33FFCC99);
        }
*/        
        int col = left + leftOffset;
        int row = top;
        for(int i = 0; i < choices.length; i++)
        {
            int choice = choices[i];
            String label = labels[i];
            Texture tex = textureCache.textures[choice];
            
            IsoDisplay.drawTileStanding(tex, col, row);
            int tw = gameDisplay.drawShadowText(label, 0x00000000, col, row - 20 , 0.2);
            gameDisplay.drawShadowText(label, Colors.BRIGHT_SILVER_INK, col - tw/2, row - 24 , 0.2);
            
            col += cellXSpacing;
            
            if(col > width) {
                col = left + leftOffset;
                row -= 64;
            }
        }
    }

    
    @Override
    public void mouseEvent(int buttonPressed, int buttonReleased, int mouseX, int mouseY) 
    {
        // int top = topCached;
        // int dy = top - mouseY;
        int n = (mouseX - leftCached) / cellXSpacing;
        
        selection = n;
        // LOG.info("Choice #" + selection + " was hovered");

        if(buttonReleased != 0)
        {
            playClickSound();
            callback.accept((Integer)choices[n]);
        }
    }
}
