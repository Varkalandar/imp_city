package impcity;

import impcity.game.Party;
import impcity.game.ai.MobStats;
import impcity.game.species.Species;
import impcity.game.quests.Quest;
import impcity.game.quests.QuestGenerator;
import impcity.game.quests.QuestProcessor;
import impcity.game.quests.QuestResult;
import impcity.game.World;
import impcity.game.mobs.Mob;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Hj. Malthaner
 */
public class TestMapQuestProcessor
{
    @Test
    public void testMapQuestProcessor()
    {
        for(int i=0; i<10; i++)
        {
            runOneQuest();
        }
    }
        
        
        
    public void runOneQuest()
    {
        Quest quest = QuestGenerator.makeTreasureQuest();
        System.out.println(quest.story + "\n");

        QuestProcessor processor = new QuestProcessor();

        
        World world = new World();
        Party party = new Party();
        
        addPartyMember(world, party, Species.BOOKWORMS_BASE);
        addPartyMember(world, party, Species.POWERSNAILS_BASE);
        addPartyMember(world, party, Species.CONIANS_BASE);
        addPartyMember(world, party, Species.MOSQUITOES_BASE);
        addPartyMember(world, party, Species.MOSQUITOES_BASE);
        addPartyMember(world, party, Species.MOSQUITOES_BASE);
    
        party.calculateStats(world.mobs);

        quest.party = party;
        
        QuestResult result = processor.createLog(world , quest);
        
        System.out.println(result.story + "\n");
        System.out.println(result.summary + "\n");
        
        
        // Hajo: test if quest is idempotent
        QuestResult result2 = processor.createLog(world , quest);
        assert(result.story.equals(result2.story));
        assert(result.summary.equals(result2.summary));
    }

    
    private static void addPartyMember(World world, Party party, int species)
    {
        Mob mob;
        mob = new Mob(0, 0, species, 0, 0, null, null, 0, null);
        int key = world.mobs.nextFreeKey();
        mob.setKey(key);
        world.mobs.put(key, mob);

        // Hajo: they start at full health
        mob.stats.setCurrent(MobStats.INJURIES, 0);
        mob.stats.setCurrent(MobStats.VITALITY, 20);
        mob.stats.setCurrent(MobStats.EXPERIENCE, MobStats.BEGINNER_EXPERIENCE);
        
        party.members.add(key);
    }
}
