package impcity.game.ui;

import impcity.game.Features;
import impcity.game.ImpCity;
import impcity.game.KeeperStats;
import java.util.logging.Level;
import java.util.logging.Logger;
import rlgamekit.stats.Stats;


/**
 *
 * @author hjm
 */
public class Research 
{
    private static final Logger LOG = Logger.getLogger(Research.class.getName());
    private GameDisplay gameDisplay;
    
    
    public Research()
    {
    }
    
    
    public void setGameDisplay(GameDisplay gameDisplay)
    {
    	this.gameDisplay = gameDisplay;
    }
    
    
    public void initialize(Stats stats)
    {
        stats.setMin(KeeperStats.COINS, 0);
        stats.setCurrent(KeeperStats.COINS, 0);
        stats.setMax(KeeperStats.COINS, 0);

        stats.setMin(KeeperStats.MANA, 0);
        stats.setCurrent(KeeperStats.MANA, KeeperStats.MANA_START);
        stats.setMax(KeeperStats.MANA, KeeperStats.MANA_BASE_MAX);

        stats.setMin(KeeperStats.RESEARCH, 0);
        stats.setCurrent(KeeperStats.RESEARCH, 0);
        stats.setMax(KeeperStats.RESEARCH, 10000); // research needed for next discovery
        
        stats.setCurrent(KeeperStats.RESEARCH, KeeperStats.RESEARCH_LABS | KeeperStats.RESEARCH_FORGES);
        // stats.setCurrent(KeeperStats.RESEARCH, KeeperStats.RESEARCH_GHOSTYARDS);

        stats.setMin(KeeperStats.RESEARCH_QUEST, 0);
        stats.setCurrent(KeeperStats.RESEARCH_QUEST, 0);
        stats.setMax(KeeperStats.RESEARCH_QUEST, 15000); // research needed for next quest
    }
    
    
    public void addRoomResearch(Stats stats, int howmuch)
    {
        int research = stats.getMin(KeeperStats.RESEARCH);
        int limit = stats.getMax(KeeperStats.RESEARCH); 
        int total = research + howmuch;

        // logger.log(Level.INFO, "Mob researched " + howmuch + " points, total is now " + total + " limit is " + limit);
        
        stats.setMin(KeeperStats.RESEARCH, total);

        if(total > limit)
        {
            // step by step research

            int researchBits = stats.getCurrent(KeeperStats.RESEARCH);
            if((researchBits & KeeperStats.RESEARCH_FORGES) == 0)
            {
                researchBits |= KeeperStats.RESEARCH_FORGES;
                announceResearchResult(KeeperStats.RESEARCH_FORGES);
                stats.setMin(KeeperStats.RESEARCH, 0);
                stats.setCurrent(KeeperStats.RESEARCH, researchBits);
                stats.setMax(KeeperStats.RESEARCH, limit * 2);
            }
            else if((researchBits & KeeperStats.RESEARCH_LABS) == 0)
            {
                researchBits |= KeeperStats.RESEARCH_LABS;
                announceResearchResult(KeeperStats.RESEARCH_LABS);
                stats.setMin(KeeperStats.RESEARCH, 0);
                stats.setCurrent(KeeperStats.RESEARCH, researchBits);
                stats.setMax(KeeperStats.RESEARCH, limit * 2);
            }
            else if((researchBits & KeeperStats.RESEARCH_HEALING) == 0)
            {
                researchBits |= KeeperStats.RESEARCH_HEALING;
                announceResearchResult(KeeperStats.RESEARCH_HEALING);
                stats.setMin(KeeperStats.RESEARCH, 0);
                stats.setCurrent(KeeperStats.RESEARCH, researchBits);
                stats.setMax(KeeperStats.RESEARCH, limit * 2);
            }
        }        
    }


    public void addQuestResearch(ImpCity game, Stats stats, int howmuch)
    {
        int research = stats.getMin(KeeperStats.RESEARCH_QUEST);
        int limit = stats.getMax(KeeperStats.RESEARCH_QUEST);
        int total = research + howmuch;

        // LOG.log(Level.INFO, "Mob researched quests: " + howmuch + " points, total is now " + total + " limit is " + limit);

        stats.setMin(KeeperStats.RESEARCH_QUEST, total);

        if(total > limit)
        {
            if(Math.random() < 0.40)
            {
                game.makeArtifactQuest();
            }
            else
            {
                game.makeTreasureQuest();
            }

            limit += 5000 + (int)(Math.random() * 10000);
            stats.setMax(KeeperStats.RESEARCH_QUEST, limit);
        }
    }


    public void announceResearchResult(int breakthrough)
    {
        String text = "";
        if(breakthrough == KeeperStats.RESEARCH_FORGES)
        {
            text = "Your researchers found out how to build forges.\nRoom unlocked: Forge";
        }
        else if(breakthrough == KeeperStats.RESEARCH_LABS)
        {
            text = "Your researchers found out how to build laboratories.\nRoom unlocked: Laboratory";
        }
        else if(breakthrough == KeeperStats.RESEARCH_HEALING)
        {
            text = "Your researchers discovered healing.\nRoom unlocked: Healing Well";
        }

        GenericMessage message =
                new GenericMessage(
                        gameDisplay,
                        600, 400,
                        "Discovery!",
                        text,
                        "[ Acknowledged ]", null);

        MessageHook hookedMessage =
                new MessageHook(Features.MESSAGE_IDEA_BLUE,
                        message);

        gameDisplay.addHookedMessage(hookedMessage);
    }
}
