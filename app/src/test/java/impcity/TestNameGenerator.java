package impcity;

import impcity.game.quests.NameGenerator;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Hj. Malthaner
 */
public class TestNameGenerator
{
    @Test
    public void testStoryGeneration()
    {
        for(int i=0; i<50; i++)
        {
            // String name = NameGenerator.makeGenericName(2 + (int)(Math.random()*3));
            // String name = NameGenerator.makeAztekName(3 + (int)(Math.random()*3));
            // String name = NameGenerator.makeAsianName(2 + (int)(Math.random()*2));
            String name = NameGenerator.makeRandomName(2 + (int)(Math.random()*3));
            System.err.println(name);
        }
    }
}
