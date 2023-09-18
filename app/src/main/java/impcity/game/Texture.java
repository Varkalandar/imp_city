package impcity.game;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Texture wrapper to link Java BufferedImages and OpenGL textures.
 * 
 * @author Hj. Malthaner
 */
public class Texture 
{
    public final int id;
    public final BufferedImage image;
    public final String name;
    public final String tags;
    public final Rectangle area;
    public final int footX;
    public final int footY;
    public final int uiOrder;
    public final int stackRun;
    public final int cacheIndex;
    
    public Texture(int id, BufferedImage image)
    {
        this(id, image, "", "", new Rectangle(), -1, -1, 1, 0, 0);
    }
    
    public Texture(int id, BufferedImage image, String name, String tags, Rectangle area, int footX, int footY,
                   int stackRun, int uiOrder, int cacheIndex)
    {
        this.id = id;
        this.image = image;
        this.name = name;
        this.tags = tags;
        this.area = area;
        this.footX = footX;
        this.footY = footY;
        this.stackRun = stackRun;
        this.uiOrder = uiOrder;
        this.cacheIndex = cacheIndex;
    }
}
