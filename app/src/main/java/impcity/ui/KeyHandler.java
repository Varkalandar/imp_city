package impcity.ui;

/**
 *
 * @author Hj. Malthaner
 */
public interface KeyHandler
{
    public void processKeyboard();
    
    /**
     * Add characters to the string buffer according to keyboard input.
     * To be used asynchroneously, call until it return true, that means
     * that the user pressed return and the input is done.
     * 
     * @param buffer Text input buffer
     * @return true if ENTER/RETURN was pressed. 
     */
    public boolean collectString(StringBuilder buffer);
}
