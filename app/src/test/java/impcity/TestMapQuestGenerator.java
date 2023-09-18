package impcity;

import impcity.game.quests.Quest;
import impcity.game.quests.QuestGenerator;

/**
 *
 * @author Hj. Malthaner
 */
public class TestMapQuestGenerator
{
    public static void main(String [] args)
    {
        for(int i=0; i<50; i++)
        {
            Quest quest = QuestGenerator.makeTreasureQuest();
            // System.out.println(quest.story.substring(0, 60));
            // System.out.println(quest.story.substring(60, quest.story.length()));
            System.out.println(quest.story + "\n");
        }
    }
}
