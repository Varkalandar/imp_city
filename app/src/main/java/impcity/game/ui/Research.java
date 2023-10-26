package impcity.game.ui;

import impcity.game.Features;
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
    private static final Logger logger = Logger.getLogger(Research.class.getName());
    private final GameDisplay gameDisplay;
    
    
    public Research(GameDisplay gameDisplay)
    {
        this.gameDisplay = gameDisplay;
    }
    
    
    public void initialize(Stats stats)
    {
        stats.setCurrent(KeeperStats.COINS, 0);
        stats.setCurrent(KeeperStats.RESEARCH, 0);

        // stats.setCurrent(KeeperStats.RESEARCH, KeeperStats.RESEARCH_LABS | KeeperStats.RESEARCH_FORGES);

        stats.setMin(KeeperStats.RESEARCH, 0);
        stats.setMax(KeeperStats.RESEARCH, 10000); // research needed for next discovery        
    }
    
    
    public void addRoomResearch(Stats stats, int howmuch)
    {
        int research = stats.getMin(KeeperStats.RESEARCH);
        int limit = stats.getMax(KeeperStats.RESEARCH); 
        int total = research + howmuch;

        logger.log(Level.INFO, "Mob researched " + howmuch + " points, total is now " + total + " limit is " + limit);
        
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
