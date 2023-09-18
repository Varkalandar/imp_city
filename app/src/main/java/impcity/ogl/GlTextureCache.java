package impcity.ogl;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import impcity.game.Texture;
import impcity.game.TextureCache;
import org.lwjgl.opengl.Display;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_MODULATE;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_ENV;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_ENV_MODE;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glTexEnvf;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.util.glu.GLU.gluErrorString;

/**
 * Texture loading and texture management.
 * 
 * @author Hj. Malthaner
 */
public class GlTextureCache extends TextureCache
{
    private static final Logger logger = Logger.getLogger(TextureCache.class.getName());
  
   
    public static Texture loadTexture(Class owner, final String filename, final int textureUnit, final int hasAlpha) throws IOException
    {
        BufferedImage img;
        int tWidth;
        int tHeight;
        
        InputStream in = owner.getResourceAsStream(filename);
        img = ImageIO.read(in);
        tWidth = img.getWidth();
        tHeight = img.getHeight();
        in.close();

        // Create a new texture object in memory and bind it
        int texId = glGenTextures();
        glActiveTexture(textureUnit);
        glBindTexture(GL_TEXTURE_2D, texId);

        // All RGB bytes are aligned to each other and each component is 1 byte
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        
        final ByteBuffer buf;
        if(hasAlpha == GL_RGBA)
        {
            buf = convertTextureToRGBA(img);

            // Upload the texture data
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, tWidth, tHeight, 0,
                        GL_RGBA, GL_UNSIGNED_BYTE, buf);
        }
        else
        {
            buf = convertTextureToRGB(img);
            
            // Upload the texture data
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, tWidth, tHeight, 0,
                        GL_RGB, GL_UNSIGNED_BYTE, buf);
        }
        

        exitOnGLError("Texture loading");

        // Setup the ST coordinate system
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        
        // Setup what to do when the texture has to be scaled
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);        
        // glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_DECAL);        
        
        exitOnGLError("loadPNGTexture");

        return new Texture(texId, img);
    }
    
    public static ByteBuffer convertTextureToRGB(BufferedImage img)
    {
        int width = img.getWidth();
        int height = img.getHeight();
        
        ByteBuffer buf = ByteBuffer.allocateDirect(Math.max(width*height*4, 65536));

        int [] pixels = img.getRGB(0, 0, width, height, null, 0, width);
        
        for(int i=0; i<pixels.length; i++)
        {
            int argb = pixels[i];
            buf.put((byte)(argb >> 16));
            buf.put((byte)(argb >> 8));
            buf.put((byte)(argb));
        }
        
        buf.position(0);

        return buf;
    }

    public static ByteBuffer convertTextureToRGBA(BufferedImage img)
    {
        int width = img.getWidth();
        int height = img.getHeight();
        
        ByteBuffer buf = ByteBuffer.allocateDirect(Math.max(width*height*4, 65536));

        int [] pixels = img.getRGB(0, 0, width, height, null, 0, width);
        
        for(int i=0; i<pixels.length; i++)
        {
            int argb = pixels[i];
            int rgba = (argb << 8) | (argb >>> 24);
            buf.putInt(rgba);
        }
        
        buf.position(0);

        return buf;
    }
    
    private static void exitOnGLError(String errorMessage)
    {
        int errorValue = glGetError();

        if (errorValue != GL_NO_ERROR)
        {
            String errorString = gluErrorString(errorValue);
            System.err.println("ERROR - " + errorMessage + ": " + errorString);

            if (Display.isCreated())
            {
                Display.destroy();
            }
            
            System.exit(-1);
        }
    }

    public Texture loadTexture(String filename, boolean hasAlpha) throws IOException
    {
        return loadTexture(this.getClass(), filename, GL_TEXTURE0, hasAlpha ? GL_RGBA : GL_RGB);
    }

    private static int rgbDiff(int a, int b)
    {
        int diff;
        
        diff = Math.abs(((a >> 16) & 0xFF) - ((b >> 16) & 0xFF))
               + Math.abs(((a >> 8) & 0xFF) - ((b >> 8) & 0xFF))
               + Math.abs(((a) & 0xFF) - ((b) & 0xFF));
        
        return diff;
    }
}
