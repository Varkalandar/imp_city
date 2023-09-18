package impcity.game.map;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import rlgamekit.map.data.LayeredMap;
import rlgamekit.map.generators.MapUtils;

/**
 *
 * @author Hj. Malthaner
 */
public class SubdivisionGenerator 
{

    private static final int MIN_SIZE = 6;
    private ArrayList<Rectangle> areas = new ArrayList<Rectangle>();

    void subdivide(Rectangle a) 
    {
        if (a.width > MIN_SIZE && a.height > MIN_SIZE) 
        {

            final int wr = a.width - MIN_SIZE + 1;
            final int hr = a.height - MIN_SIZE + 1;

            Random rng = new Random();

            final int wc = (MIN_SIZE + 1)/2 + (rng.nextInt(wr) + rng.nextInt(wr)) / 2;
            final int hc = (MIN_SIZE + 1)/2 + (rng.nextInt(hr) + rng.nextInt(hr)) / 2;

            Rectangle a2 = new Rectangle();

            // new top left area
            a2.x = a.x;
            a2.y = a.y;
            a2.width = wc;
            a2.height = hc;
            subdivide(a2);

            // new top right area
            a2.x = a.x + wc + 1;
            a2.y = a.y;
            a2.width = a.width - wc - 1;
            a2.height = hc;
            subdivide(a2);

            // new bottom left area
            a2.x = a.x;
            a2.y = a.y + hc + 1;
            a2.width = wc;
            a2.height = a.height - hc - 1;
            subdivide(a2);

            // new bottom right area
            a2.x = a.x + wc + 1;
            a2.y = a.y + hc + 1;
            a2.width = a.width - wc - 1;
            a2.height = a.height - hc -1;
            subdivide(a2);

        }
        else 
        {
            // Hajo: reached a leaf node -> add room
            areas.add(new Rectangle(a));
        }
    }

    public LayeredMap create(Properties props) 
    {
        int width = 40;
        int height = 40;

        LayeredMap map = new LayeredMap(2, width, height);

        Rectangle a = new Rectangle(0, 0, width, height);

        subdivide(a);

        // Hajo: make rooms

        MapUtils mapUtils = new MapUtils();

        int[] walls = new int[1];
        int[] rooms = new int[1];
        int[] floors = new int[1];
        walls[0] = '#';
        rooms[0] = 'a';
        floors[0] = '.';

        mapUtils.fillArea(map, 0, 0, 0, map.getWidth(), map.getHeight(), floors);
        // mapUtils.fillArea(map, 0, 0, 0, map.getWidth(), map.getHeight(), walls);
        // mapUtils.fillArea(map, 0, 1, 1, map.getWidth()-2, map.getHeight()-2, floors);
        
        for (Rectangle room : areas) 
        {
            // todo

            mapUtils.fillArea(map, 0, room.x, room.y, room.width, room.height, walls);
            mapUtils.fillArea(map, 0, room.x+1, room.y+1, room.width-2, room.height-2, rooms);
            mapUtils.fillArea(map, 0, room.x+1, room.y, 1, 1, rooms);
        }

        spawn:
        for(int j=0; j<height; j++)
        {
            for(int i=0; i<width; i++)
            {
                if(map.get(0, i, j) == '.')
                {
                    map.setSpawnX(i);
                    map.setSpawnY(j);
                    break spawn;
                }
            }
        }

        return map;
    }

    public static void main(String [] args)
    {
        SubdivisionGenerator gen = new SubdivisionGenerator();
        
        LayeredMap map = gen.create(new Properties());
        
        int w = map.getWidth();
        int h = map.getHeight();
                
        // Hajo: convert floors
        for(int j=0; j<h; j++)
        {
            for(int i=0; i<w; i++)
            {
                int n = map.get(0, i, j);
                
                char c = (char)(n & 0xFFFF);

                System.err.print(c);
            }
            System.err.println();
        }
        
    }
}
