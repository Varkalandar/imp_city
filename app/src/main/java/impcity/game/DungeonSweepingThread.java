package impcity.game;

import impcity.game.ui.GameDisplay;
import impcity.game.jobs.Job;
import impcity.game.jobs.JobQueue;
import impcity.game.jobs.JobFetchItem;
import impcity.game.quests.Quest;
import impcity.game.quests.QuestProcessor;
import impcity.game.quests.QuestResult;
import impcity.game.ui.QuestResultMessage;
import static java.lang.Thread.sleep;
import java.util.logging.Level;
import java.util.logging.Logger;

import impcity.game.map.Map;
import impcity.game.mobs.Mob;
import impcity.game.ui.MessageHook;
import impcity.ogl.IsoDisplay;

/**
 * Background thread for collecting items that lay around
 * and set up jobs to clean them up.
 * 
 * @author Hj. Malthaner
 */
public class DungeonSweepingThread extends Thread
{
    private static final Logger logger = Logger.getLogger(DungeonSweepingThread.class.getName());
    
    private final ImpCity game;
    private final GameDisplay gameDisplay;
    private final IsoDisplay display;

    // count the treasures
    private int gold, silver, bronze;

    public DungeonSweepingThread(ImpCity game, GameDisplay gameDisplay, IsoDisplay display)
    {
        setDaemon(true);
        setPriority(MIN_PRIORITY);
        this.game = game;
        this.gameDisplay = gameDisplay;
        this.display = display;
        setName("DungeonSweepingThread");        
    }
    
    @Override
    public void run()
    {
        Mob player = game.world.mobs.get(game.getPlayerKey());
        Map map = player.gameMap;
        
        while(true)
        {
            try
            {
                gold = 0;
                silver = 0;
                bronze = 0;

                for(int j=0; j<map.getHeight(); j++)
                {
                    map = player.gameMap;
                    for(int i=0; i<map.getHeight(); i++)
                    {
                        int item = map.getItem(i, j);
                        if(item > 0)
                        {
                            processItem(map, i, j, item);
                        }
                    }
                
                    safeSleep(50);
                    // logger.log(Level.INFO, "Dungeon sweeping thread completes row {0}", j);
                }

                // record treasures

                player.stats.setMax(KeeperStats.COINS, gold);
                player.stats.setCurrent(KeeperStats.COINS, silver);
                player.stats.setMin(KeeperStats.COINS, bronze);

                for(Quest quest : game.quests)
                {                    
                    if(quest.party != null && quest.eta <= Clock.days())
                    {
                        createQuestResult(quest);
                    }
                }
            }
            catch(Exception ex)
            {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }

    private void createQuestResult(Quest quest)
    {
        QuestProcessor processor = new QuestProcessor();
        QuestResult questResult = processor.createLog(game.world, quest);
        // System.out.println(result.story);
        
        game.storePartyTreasures(questResult);
        game.reactivateReturningCreatures(questResult.quest);

        questResult.quest.party = null;  // only return once.
        
        QuestResultMessage message = 
                new QuestResultMessage(game, gameDisplay, display, 600, 700, questResult, "[ Ok ]");

        MessageHook hookedMessage =
            new MessageHook(Features.MESSAGE_TROPHY_RESULT,
                            message);

        gameDisplay.addHookedMessage(hookedMessage); 
    }
    
    private void safeSleep(int millis)
    {
        try
        {
            sleep(millis);
        } 
        catch (InterruptedException ex)
        {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private void processItem(Map map, int i, int j, int item) 
    {
        if(Features.isCoins(item) || game.world.isArtifact(item))
        {
            if(!game.getTreasuries().isEmpty())
            {
                int rasterI = i/Map.SUB*Map.SUB;
                int rasterJ = j/Map.SUB*Map.SUB;
                int ground = map.getFloor(rasterI, rasterJ);

                // Hajo: outside a treasurey?
                if(ground < Features.GROUND_TREASURY || ground >= Features.GROUND_TREASURY + 3)
                {
                    Job job = new JobFetchItem(game, i, j, item);
                    game.jobQueue.add(job, JobQueue.PRI_LOW);
                }
            }
            
            // bookkeeping
            if(item == Features.I_GOLD_COINS) gold ++;
            if(item == Features.I_SILVER_COINS) silver ++;
            if(item == Features.I_BRONZE_COINS) bronze ++;
        }
    }
}
