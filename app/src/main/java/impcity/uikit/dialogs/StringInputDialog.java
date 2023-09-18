package impcity.uikit.dialogs;

import impcity.ui.PixFont;
import impcity.uikit.DisplayLabel;
import impcity.uikit.GLPainter;
import impcity.uikit.ModalDialog;
import impcity.uikit.RGBA;
import impcity.uikit.TextField;

/**
 *
 * @author Hj. Malthaner
 */
public class StringInputDialog extends ModalDialog
{
    private final DisplayLabel label;
    private final TextField input;
    public int width, height;
    
    public StringInputDialog(PixFont font, String message, String value, double scale) 
    {
        super(font);
        width = 480;
        height = 180;
        
        label = new DisplayLabel(font, 0xFF000000, scale);
        label.area.x = 24;
        label.area.y = height - 56;
        label.value = message;
        label.key = "label";
        
        input = new TextField(font, 0xFFFFFFFF, scale);
        input.area.x = 24;
        input.area.y = height - 120;
        input.area.width = width - 48;
        input.area.height = 40;
        input.keyBuffer.append(value);
        input.key = "input";
        
        displayElements.put(label.key, label);
        displayElements.put(input.key, input);
    }
    
    @Override
    protected void displayBackground(int xpos, int ypos) 
    {
        GLPainter.dddBox(xpos, ypos, width, height, RGBA.WHITE, RGBA.LIGHT_GRAY, RGBA.DARK_GRAY);
        
        GLPainter.dddFrame(xpos+7, ypos+7, width-14, height-14, RGBA.DARK_GRAY, RGBA.WHITE);
        GLPainter.dddFrame(xpos+8, ypos+8, width-16, height-16, RGBA.WHITE, RGBA.DARK_GRAY);
    }

    public String getInput() 
    {
        return input.keyBuffer.toString();
    }

}
