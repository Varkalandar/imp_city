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

/**
 *
 * @author Hj. Malthaner
 */
public class TestMapQuestProcessor
{
    public static void main(String [] args)
    {
        Quest quest = QuestGenerator.makeTreasureQuest();
        System.out.println(quest.story + "\n");

        QuestProcessor processor = new QuestProcessor();

        
        World world = new World();
        Party party = new Party();
        
        addPartyMember(world, party, Species.WYVERNS_BASE);
        addPartyMember(world, party, Species.CONIANS_BASE);
        addPartyMember(world, party, Species.CONIANS_BASE);
        addPartyMember(world, party, Species.KILLERBEETLES_BASE);
        addPartyMember(world, party, Species.KILLERBEETLES_BASE);
        addPartyMember(world, party, Species.KILLERBEETLES_BASE);
        
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
        mob = new Mob(world, 0, 0, species, null, null, 0, null);
        int key = world.mobs.nextFreeKey();
        mob.setKey(key);
        world.mobs.put(key, mob);

        // Hajo: they start at full health
        mob.stats.setCurrent(MobStats.INJURIES, 0);
        
        party.members.add(key);
    }
}
