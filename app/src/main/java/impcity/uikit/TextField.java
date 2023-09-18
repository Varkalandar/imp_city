package impcity.uikit;

import impcity.ui.PixFont;
import org.lwjgl.input.Keyboard;

/**
 *
 * @author Hj. Malthaner
 */
public class TextField extends DisplayLabel
{
    public final StringBuilder keyBuffer;

    public TextField(PixFont font, int color, double scale)
    {
        super(font, color, scale);

        this.keyBuffer = new StringBuilder();
    }
    
    @Override
    void display(int xpos, int ypos) 
    {
        int x = xpos + area.x;
        int y = ypos + area.y;
        // GLPainter.dddBox(x, y, area.width, area.height, RGBA.WHITE, RGBA.LIGHT_GRAY, RGBA.DARK_GRAY);
        // GLPainter.dddBox(x+2, y+2, area.width-4, area.height-4, RGBA.DARK_GRAY, RGBA.LIGHT_GRAY, RGBA.WHITE);
        
        GLPainter.dddBox(x, y, area.width, area.height, RGBA.DARK_GRAY, RGBA.GRAY, RGBA.WHITE);

        value = keyBuffer.toString();
        int w = font.drawStringScaled(value, color , x + 8, y + 10, fontScale);
        
        GLPainter.fillRect(x + w + 10, y+7, 1, 26, (System.currentTimeMillis() & 1024 ) != 0 ? RGBA.BLACK : RGBA.LIGHT_GRAY);
    }

    @Override
    void handleKeyEvent(KeyEvent event) 
    {
        if(event.key >= 32)
        {
            keyBuffer.append(event.key);
        }

        if(event.code == Keyboard.KEY_BACK)
        {
            keyBuffer.deleteCharAt(keyBuffer.length()-1);
        }
    }
}
