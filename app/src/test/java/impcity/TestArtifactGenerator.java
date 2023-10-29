package impcity;

import impcity.game.quests.ArtifactGenerator;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Hj. Malthaner
 */
public class TestArtifactGenerator
{
    @Test
    public void testArtifactNameGeneration()
    {
        for(int i=0; i<50; i++)
        {
            String name = ArtifactGenerator.makeArtifactName(0);
            System.err.println(name);
        }
    }
}
