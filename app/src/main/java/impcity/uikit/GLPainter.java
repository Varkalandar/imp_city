package impcity.uikit;

import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glVertex2i;

/**
 * GL based UI helper drawing functions.
 * @author Hj. Malthaner
 */
public class GLPainter 
{
    public static void fillRect(int x, int y, int w, int h, RGBA color)
    {
        glBindTexture(GL_TEXTURE_2D, 0);

        glBegin(GL_QUADS);

        GL11.glColor4f(color.red, color.green, color.blue, color.alpha);

        glVertex2i(x, y);
        glVertex2i(x+w, y);
        glVertex2i(x+w, y+h);
        glVertex2i(x, y+h);

        glEnd();
    }
    
    
    public static void dddBox(int xpos, int ypos, int width, int height, RGBA light, RGBA area, RGBA dark)
    {
        fillRect(xpos+1, ypos+1, width-2, height-2, area);

        dddFrame(xpos, ypos, width, height, light, dark);
    }

    public static void dddFrame(int xpos, int ypos, int width, int height, RGBA light, RGBA dark)
    {
        fillRect(xpos, ypos, width, 1, dark);
        fillRect(xpos, ypos, 1, height, light);
        fillRect(xpos, ypos+height-1, width, 1, light);
        fillRect(xpos+width-1, ypos, 1, height, dark);
    }
}
