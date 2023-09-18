package impcity.game.ui;

/**
 *
 * @author Hj. Malthaner
 */
public class MessageHook 
{
    /** Messaged drop in from the top */
    
    public int yoff;
    public final int icon;
    private final PaperMessage message;
    
    public MessageHook(int icon, PaperMessage message)
    {
        this.icon = icon;
        this.message = message;
    }
    
    /**
     * User wants to see the hooked message.
     */
    public void activate(GameDisplay gameDisplay)
    {
        gameDisplay.showDialog(message);
    }
}
