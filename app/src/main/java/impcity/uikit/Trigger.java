package impcity.uikit;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Buttons or anything that can trigger an action in the UI.
 * 
 * @author Hj. Malthaner
 */
public class Trigger extends DisplayElement
{
    private static final Logger logger = Logger.getLogger(Trigger.class.getName());
    
    public boolean activated;
    
    private final ArrayList <ActionCallback> callbacks;
    
    
    public Trigger()
    {
        callbacks = new ArrayList <ActionCallback> ();
        activated = false;
    }
    
    @Override
    void handleMouseEvent(MouseEvent event)
    {
        if(!activated && event.buttonPressed >= 1)
        {
            if(area.contains(event.mouseX, event.mouseY))
            {
                activated = true;
             
                activateAllCallbacks(event);
            }
        }

        if(activated && event.buttonReleased >= 1)
        {
            activated = false;
        }
    }
    
    @Override
    void handleKeyEvent(KeyEvent event)
    {
    }

    public void setArea(int x, int y, int w, int h)
    {
        area.x = x;
        area.y = y;
        area.width = w;
        area.height = h;
    }

    private void activateAllCallbacks(MouseEvent event) 
    {
        for(ActionCallback callback : callbacks)
        {
            try
            {
                callback.activate(event);
            }
            catch(Exception e)
            {
                logger.log(Level.SEVERE, "Exception in handling trigger callbacks: " + e.getMessage(), e);
            }
        }
    }
}
