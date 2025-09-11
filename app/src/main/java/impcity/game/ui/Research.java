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

        stats.setMin(KeeperStats.LIFE, 0);
        stats.setCurrent(KeeperStats.LIFE, KeeperStats.LIFE_START);
        stats.setMax(KeeperStats.LIFE, KeeperStats.LIFE_BASE_MAX);

        stats.setMin(KeeperStats.RESEARCH, 0);
        stats.setCurrent(KeeperStats.RESEARCH, KeeperStats.RESEARCH_NONE);
        stats.setMax(KeeperStats.RESEARCH, 10000); // research needed for next discovery
        
        // stats.setCurrent(KeeperStats.RESEARCH, KeeperStats.RESEARCH_LABS | KeeperStats.RESEARCH_FORGES);
        // stats.setCurrent(KeeperStats.RESEARCH, KeeperStats.RESEARCH_GHOSTYARDS);

        stats.setMin(KeeperStats.METALLURGY, 0);
        stats.setCurrent(KeeperStats.METALLURGY, KeeperStats.METALLURGY_NONE);
        stats.setMax(KeeperStats.METALLURGY, 10000); // research needed for next discovery
        
        
        stats.setMin(KeeperStats.RESEARCH_QUEST, 0);
        stats.setCurrent(KeeperStats.RESEARCH_QUEST, 0);
        stats.setMax(KeeperStats.RESEARCH_QUEST, 15000); // research needed for next quest
    }


    public void addRoomResearch(Stats stats, int howmuch, int whichStat)
    {
        int research = stats.getMin(whichStat);
        int limit = stats.getMax(whichStat); 
        int total = research + howmuch;

        // logger.log(Level.INFO, "Mob researched " + howmuch + " points, total is now " + total + " limit is " + limit);
        
        stats.setMin(whichStat, total);

        if(total > limit)
        {
            // step by step research

            int researchBits = stats.getCurrent(whichStat);
            
            if(whichStat == KeeperStats.RESEARCH)
            {
                if((researchBits & KeeperStats.RESEARCH_FORGES) == 0)
                {
                    researchBits |= KeeperStats.RESEARCH_FORGES;
                    announceResearchResult(whichStat, KeeperStats.RESEARCH_FORGES);
                }
                else if((researchBits & KeeperStats.RESEARCH_LABS) == 0)
                {
                    researchBits |= KeeperStats.RESEARCH_LABS;
                    announceResearchResult(whichStat, KeeperStats.RESEARCH_LABS);
                }
                else if((researchBits & KeeperStats.RESEARCH_HEALING) == 0)
                {
                    researchBits |= KeeperStats.RESEARCH_HEALING;
                    announceResearchResult(whichStat, KeeperStats.RESEARCH_HEALING);
                }
            }
            else if(whichStat == KeeperStats.METALLURGY)
            {
                if((researchBits & KeeperStats.METALLURGY_BRONZE) == 0)
                {
                    researchBits |= KeeperStats.METALLURGY_BRONZE;
                    announceResearchResult(whichStat, KeeperStats.METALLURGY_BRONZE);
                }
                else if((researchBits & KeeperStats.METALLURGY_IRON) == 0)
                {
                    researchBits |= KeeperStats.METALLURGY_IRON;
                    announceResearchResult(whichStat, KeeperStats.METALLURGY_IRON);
                }
                else if((researchBits & KeeperStats.METALLURGY_STEEL) == 0)
                {
                    researchBits |= KeeperStats.METALLURGY_STEEL;
                    announceResearchResult(whichStat, KeeperStats.METALLURGY_STEEL);
                }
                else if((researchBits & KeeperStats.METALLURGY_ALLOYS) == 0)
                {
                    researchBits |= KeeperStats.METALLURGY_ALLOYS;
                    announceResearchResult(whichStat, KeeperStats.METALLURGY_ALLOYS);
                }
            }
            
            stats.setMin(whichStat, 0);
            stats.setCurrent(whichStat, researchBits);
            stats.setMax(whichStat, limit * 2);
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


    public void announceResearchResult(int stat, int breakthrough)
    {
        String text = "";
        if(stat == KeeperStats.RESEARCH && breakthrough == KeeperStats.RESEARCH_FORGES)
        {
            text = "Your researchers found out how to build forges.\nRoom unlocked: Forge";
        }
        else if(stat == KeeperStats.RESEARCH && breakthrough == KeeperStats.RESEARCH_LABS)
        {
            text = "Your researchers found out how to build laboratories.\nRoom unlocked: Laboratory";
        }
        else if(stat == KeeperStats.RESEARCH && breakthrough == KeeperStats.RESEARCH_HEALING)
        {
            text = "Your researchers discovered healing.\nRoom unlocked: Healing Well";
        }
        else if(stat == KeeperStats.METALLURGY && breakthrough == KeeperStats.METALLURGY_BRONZE)
        {
            text = "Your researchers found out how to melt bronze.\nTechnology unlocked: Bronze weapons and tools";
        }
        else if(stat == KeeperStats.METALLURGY && breakthrough == KeeperStats.METALLURGY_IRON)
        {
            text = "Your researchers found out how to produce iron.\nTechnology unlocked: Iron weapons and tools";
        }
        else if(stat == KeeperStats.METALLURGY && breakthrough == KeeperStats.METALLURGY_STEEL)
        {
            text = "Your researchers learned to refine iron into steel.\nTechnology unlocked: Steel weapons and tools";
        }
        else if(stat == KeeperStats.METALLURGY && breakthrough == KeeperStats.METALLURGY_ALLOYS)
        {
            text = "Your researchers developed advanced metal alloys.\nTechnology unlocked: Weapons and tools made from advanced metal alloys";
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
