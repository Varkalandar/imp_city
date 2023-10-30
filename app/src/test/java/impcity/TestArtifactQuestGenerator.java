package impcity;

import impcity.game.quests.Quest;
import impcity.game.quests.QuestGenerator;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Hj. Malthaner
 */
public class TestArtifactQuestGenerator
{
    @Test
    public void testStoryGeneration()
    {
        for(int i=0; i<50; i++)
        {
            Quest quest = QuestGenerator.makeArtifactQuest();
            System.err.println(quest.story + "\n");
        }
    }
}
