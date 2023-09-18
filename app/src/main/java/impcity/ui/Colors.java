package impcity.ui;

import java.awt.Color;

/**
 * Color constants
 * 
 * @author Hj. Malthaner
 */
public class Colors
{
    public static final int TEXT_WARM = 0xFFFFEEDD;
    public static final int TEXT_COLD = 0xFFDDEEFF;

    public static final int INVENTORY_HIGHLIGHT = 0x50553300;
    public static final int INVENTORY_BACKGROUND = 0x50000077;
    
    // Rainbow color hues
    // chosen to have about equivalent visible "distances" and
    // about the same brightness throughout all hues
    
    public static final Color RED = new Color(0xCA0216);
    public static final Color ORANGE = new Color(0xB66200);
    public static final Color YELLOW = new Color(0xB39E00);
    public static final Color GREEN1 = new Color(0x7B8D00);
    public static final Color GREEN2 = new Color(0x149E0C);
    public static final Color CYAN = new Color(0x009898);
    public static final Color BLUE = new Color(0x3655FF);
    public static final Color PURPLE = new Color(0x9422FF);
    public static final Color MAGENTA = new Color(0xC60C9A);
    
    public static final Color WHITE = new Color(0xFFFFFF);
    public static final Color GRAY = new Color(0x888888);
    public static final Color DARK = new Color(0x444444);
    public static final Color BLACK = new Color(0);
    
    public static final Color BROWN = new Color(0x593000);
    public static final Color DARK_RED = new Color(0x701000);
    public static final Color DARK_GREEN = new Color(0x0B5906);
    public static final Color DARK_BLUE = new Color(0x182672);
    public static final Color FULL_YELLOW = new Color(0xFFE600);
    
    public static final Color PURE_RED = new Color(0xD00000);
    public static final Color PURE_GREEN = new Color(0x00B000);
    public static final Color PURE_BLUE = new Color(0x0000FF);


    public static final Color HALFBRIGHT = new Color(0x808080);
    
    
    public static int mix(Color c1, Color c2)
    {
        int rgb1 = c1.getRGB() & 0xFEFEFE;
        int rgb2 = c2.getRGB() & 0xFEFEFE;
        
        return 0xFF000000 | ((rgb1 + rgb2) >> 1);
    }

    public static int mix(Color c1, Color c2, int alpha)
    {
        return mix(c1.getRGB(), c2.getRGB(), alpha);
    }

    public static int mix(int rgb1, int rgb2, int alpha)
    {
        int beta = 256 - alpha;
        
        int R1 = (rgb1 >> 16) & 0xFF;
        int G1 = (rgb1 >> 8) & 0xFF;
        int B1 = (rgb1) & 0xFF;

        int R2 = (rgb2 >> 16) & 0xFF;
        int G2 = (rgb2 >> 8) & 0xFF;
        int B2 = (rgb2) & 0xFF;
        
        int R = alpha * R1 + beta * R2;
        int G = alpha * G1 + beta * G2;
        int B = alpha * B1 + beta * B2;
        
        return 0xFF000000 | ((R << 8) & 0xFF0000) | ((G) & 0xFF00) | ((B >> 8) & 0xFF);
    }


    public static int randomGray(int brightness, int var)
    {
        int v = (int)(Math.random() * var);
        return  0xFF000000 |
                ((brightness + v) << 16) |
                ((brightness + v) << 8) |
                ((brightness + v));
    }
            
    public static int randomColor(int brightness, int rv, int gv, int bv)
    {
        return  0xFF000000 |
                ((brightness + (int)(Math.random() * rv)) << 16) |
                ((brightness + (int)(Math.random() * gv)) << 8) |
                ((brightness + (int)(Math.random() * bv)));
    }
}
