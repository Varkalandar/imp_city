package impcity.uikit;

import java.awt.Rectangle;
import impcity.ui.PixFont;

/**
 *
 * @author Hj. Malthaner
 */
public class DisplayElement 
{
    public PixFont font;
    public final Rectangle area;
    public String value = "";
    
    public String key;

    public DisplayElement()
    {
        area = new Rectangle();
    }
    
    void display(int xpos, int ypos) 
    {
    }

    void handleMouseEvent(MouseEvent event)
    {
    }
    
    void handleKeyEvent(KeyEvent event)
    {
    }
}
