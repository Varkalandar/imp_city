package impcity.game.processables;

import impcity.game.map.Map;

import java.awt.*;

/**
 *
 * @author Hj. Malthaner
 */
public interface Processable 
{
    public void process(Map map);    

    public Point getLocation();
}
