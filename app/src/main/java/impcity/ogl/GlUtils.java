package impcity.ogl;

import static org.lwjgl.opengl.GL11.*;

/**
 * OpenGL utilities.
 * 
 * @author Hj. Malthaner
 */
public class GlUtils
{
    private static final float [] rgbFloats = new float [256];
    
    static
    {
        for(int i=0; i<256; i++)
        {
            rgbFloats[i] = i/255.0f;
        }
    }
    
    public static void colorRgb(int rgb)
    {
        glColor3f(rgbFloats[(rgb >> 16) & 0xFF],
                  rgbFloats[(rgb >> 8) & 0xFF],
                  rgbFloats[(rgb) & 0xFF]);
    }

    public static void colorArgb(int argb)
    {
        glColor4f(rgbFloats[(argb >> 16) & 0xFF],
                  rgbFloats[(argb >> 8) & 0xFF],
                  rgbFloats[argb & 0xFF],
                  rgbFloats[(argb >> 24) & 0xFF]);
    }
}
