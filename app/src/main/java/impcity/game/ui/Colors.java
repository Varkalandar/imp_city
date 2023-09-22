package impcity.game.ui;

import java.awt.Color;

/**
 * Color constants
 * 
 * @author Hj. Malthaner
 */
public class Colors 
{
    public static final int BRIGHT_GOLD_INK = 0xFFFFDD99;
    public static final int BRIGHT_SILVER_INK = 0xFFDDDDDD;
    public static final int BLUE_INK = 0xFF405090;
    public static final int DARK_BLUE_INK = 0xFF304060;

    public static final int DARK_RED_INK = 0xFF403020;


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
