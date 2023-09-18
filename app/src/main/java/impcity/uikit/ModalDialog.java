package impcity.uikit;

import impcity.ui.PixFont;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

/**
 *
 * @author Hj. Malthaner
 */
public class ModalDialog extends Dialog
{
    public ModalDialog(PixFont font)
    {
        super(font);
    }
    
    @Override
    public void display(int xpos, int ypos)
    {
        boolean done = false;
        boolean lastButtonState = Mouse.getEventButtonState();
        while(!done)
        {
            if(Mouse.next())
            {
                int button = Mouse.getEventButton();
                boolean buttonState = Mouse.getEventButtonState();
                int buttonPressed = 0;
                int buttonReleased = 0;
                
                if(buttonState != lastButtonState)
                {
                    if(buttonState)
                    {
                        buttonPressed = button + 1;
                    }
                    else
                    {
                        buttonReleased = button + 1;
                    }

                    lastButtonState = buttonState;
                }


                MouseEvent event = new MouseEvent(Mouse.getX() - xpos,
                                        Mouse.getY() - ypos, 
                                        buttonPressed, 
                                        buttonReleased);

                processMouseEvent(event);
            }
            
            if(Keyboard.next())
            {
                if(Keyboard.getEventKeyState() == true)
                {
                    if(Keyboard.isKeyDown(Keyboard.KEY_RETURN))
                    {
                        done = true;
                    }
                    if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
                    {
                        done = true;
                    }

                    KeyEvent event = new KeyEvent(Keyboard.getEventCharacter(),
                                                  Keyboard.getEventKey());
                    processKeyEvent(event);
                }
            }
            
            super.display(xpos, ypos);

            Display.update();
            Display.sync(60);
        }
    }

}
