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
import java.util.LinkedList;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;
import org.junit.jupiter.api.Test;

/**
 * Tests the success rate of a standard party vs quest finding difficulty.
 * 
 * @author Hj. Malthaner
 */
public class TestMapQuestProcessorSuccessRate
{
    @Test
    public void testMapQuestProcessor()
    {
        int runs = 20;
        
        double [] p = new double [16]; 
        for(int difficulty=0; difficulty<16; difficulty++)
        {
            int count = 0;
            for(int i=0; i<runs; i++)
            {
                boolean success = runOneQuest(difficulty);
                if(success) count ++;
            }

            p[difficulty] = (double)count / (double)runs;
        }
        
        for(int difficulty=0; difficulty<16; difficulty++)
        {
            System.err.println("Difficulty: " + difficulty + "\t Success rate:" + p[difficulty]);
        }
    }        
        
        
    public boolean runOneQuest(int difficulty)
    {
        Quest quest = QuestGenerator.makeTreasureQuest();
        quest.findingDifficulty = difficulty;
        
        // System.out.println(quest.story + "\n");

        QuestProcessor processor = new QuestProcessor();

        
        World world = new World();
        quest.party = assembleParty(world);
        
        QuestResult result = processor.createLog(world , quest);
        
        
        // renew party - first run might have killed or injured some
        quest.party = assembleParty(world);
        
        // Hajo: test if quest is idempotent
        QuestResult result2 = processor.createLog(world , quest);
        
        if(!result.story.equals(result2.story))
        {
            DiffMatchPatch dmp = new DiffMatchPatch();
            LinkedList<DiffMatchPatch.Diff> diffs = dmp.diffMain(result.story, result2.story, false);            
            
            for(DiffMatchPatch.Diff diff : diffs)
            {
                System.err.println(diff);
            }            
            
            System.err.println(result.story + "\n");
            System.err.println(result2.story + "\n");            
            assert(false);
        }
        
        if(!result.summary.equals(result2.summary))
        {
            assert(false);
        }
            
        
        return (quest.status & Quest.SF_FOUND) != 0;
    }


    private Party assembleParty(World world)
    {
        Party party = new Party();
        
        addPartyMember(world, party, Species.BOOKWORMS_BASE);
        addPartyMember(world, party, Species.POWERSNAILS_BASE);
        addPartyMember(world, party, Species.CONIANS_BASE);
        addPartyMember(world, party, Species.MOSQUITOES_BASE);
        addPartyMember(world, party, Species.MOSQUITOES_BASE);
        addPartyMember(world, party, Species.MOSQUITOES_BASE);
    
        party.calculateStats(world.mobs);
        
        return party;
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
