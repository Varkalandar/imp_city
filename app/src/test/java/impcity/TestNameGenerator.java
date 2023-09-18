package impcity;

import impcity.game.quests.NameGenerator;

/**
 *
 * @author Hj. Malthaner
 */
public class TestNameGenerator
{
    public static void main(String [] args)
    {
        for(int i=0; i<50; i++)
        {
            // String name = NameGenerator.makeGenericName(2 + (int)(Math.random()*3));
            // String name = NameGenerator.makeAztekName(3 + (int)(Math.random()*3));
            // String name = NameGenerator.makeAsianName(2 + (int)(Math.random()*2));
            String name = NameGenerator.makeRandomName(2 + (int)(Math.random()*3));
            System.out.println(name);
        }
    }
}
