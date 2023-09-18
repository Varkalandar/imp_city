package impcity.uikit;

import impcity.ui.PixFont;

/**
 *
 * @author Hj. Malthaner
 */
public class DisplayLabel extends DisplayElement
{
    double fontScale = 1.0;
    int color = 0xFFFFFF;

    public DisplayLabel() 
    {
    }

    public DisplayLabel(PixFont font, int color, double scale) 
    {
        this.font = font;
        this.color = color;
        this.fontScale = scale;
    }
    
    @Override
    void display(int xpos, int ypos) 
    {
        font.drawStringScaled(value, color , xpos + area.x, ypos + area.y, fontScale);
    }
}
