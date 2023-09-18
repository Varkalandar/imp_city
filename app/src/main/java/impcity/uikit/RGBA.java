package impcity.uikit;

import java.awt.Color;

/**
 * Float precision color class.
 * 
 * @author Hj. Malthaner
 */
public class RGBA 
{
    public static RGBA WHITE = new RGBA(Color.WHITE);
    public static RGBA BLACK = new RGBA(Color.BLACK);
    public static RGBA LIGHT_GRAY = new RGBA(Color.LIGHT_GRAY);
    public static RGBA GRAY = new RGBA(Color.GRAY);
    public static RGBA DARK_GRAY = new RGBA(Color.DARK_GRAY);
    
    
    float red, green, blue, alpha;
    
    public RGBA(float red, float green, float blue, float alpha)
    {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public RGBA(double red, double green, double blue, double alpha)
    {
        this.red = (float)red;
        this.green = (float)green;
        this.blue = (float)blue;
        this.alpha = (float)alpha;
    }

    public RGBA(Color color)
    {
        this.red = color.getRed() / 255f;
        this.green = color.getGreen() / 255f;
        this.blue = color.getBlue() / 255f;
        this.alpha = color.getAlpha() / 255f;
    }
}
