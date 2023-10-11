package impcity;

import impcity.game.quests.Quest;
import impcity.game.quests.QuestGenerator;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Hj. Malthaner
 */
public class TestMapQuestGenerator
{
    @Test
    public void testStoryGeneration()
    {
        for(int i=0; i<50; i++)
        {
            Quest quest = QuestGenerator.makeTreasureQuest();
            // System.out.println(quest.story.substring(0, 60));
            // System.out.println(quest.story.substring(60, quest.story.length()));
            System.err.println(quest.story + "\n");
        }
    }
}
