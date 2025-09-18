package impcity.game;

import java.awt.Point;

/**
 *
 * @author hjm
 */
public class ResourceNode 
{
    public static enum Type
    {
        COPPER_ORE,
    };
    
    public final Type type;
    public final Point location;
    
    public ResourceNode(Type type, Point location)
    {
        this.type = type;
        this.location = location;
    }
}
