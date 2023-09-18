package impcity.uikit;

/**
 * For in-game UI event handling.
 * 
 * @author Hj. Malthaner
 */
public class MouseEvent 
{
    public int mouseX;
    public int mouseY;
    public int buttonPressed;
    public int buttonReleased;
    
    private boolean consumed;
    
    public MouseEvent(int mx, int my, int bp, int br)
    {
        mouseX = mx;
        mouseY = my;
        buttonPressed = bp;
        buttonReleased = br;
    }

    public boolean isConsumed()
    {
        return consumed;
    }
    
    public void consume()
    {
        consumed = true;
    }
}
