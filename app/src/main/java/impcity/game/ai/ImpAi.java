package impcity.game.ai;

import impcity.game.Features;
import impcity.game.ImpCity;
import impcity.game.map.Map;
import impcity.game.species.Species;
import impcity.game.species.SpeciesDescription;
import impcity.game.jobs.Job;
import impcity.game.jobs.JobClaimGround;
import impcity.game.jobs.JobExcavate;
import impcity.game.jobs.JobMining;
import impcity.game.jobs.JobQueue;
import impcity.game.jobs.JobFetchItem;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import impcity.game.Clock;
import impcity.game.Sounds;
import impcity.game.mobs.Mob;
import impcity.game.map.LocationPathDestination;
import rlgamekit.pathfinding.Path;

/**
 *
 * @author Hj. Malthaner
 */
public class ImpAi extends AiBase
{
    public static final Logger logger = Logger.getLogger(ImpAi.class.getName());
    private static final int thinkCooldown = 200;

    public enum Goal
    {
        FIND_LAIR, BUILD_LAIR,
        FIND_JOB, FIND_PATH_TO_JOB, GO_TO_JOB, WORKING,
        GO_TO_SLEEP, SLEEP, 
        GOLD_TO_TREASURY, DROP_GOLD,
        ITEM_TO_FORGE, ITEM_TO_LAB, DROP_ITEM
    }
    
    public Goal goal;
    private long thinkTime;
    private long pathTime;
    private final ImpCity game;
    private Job currentJob;

    public ImpAi(ImpCity game)
    {
        this.game = game;
        this.goal = Goal.FIND_LAIR;
        this.thinkTime = Clock.time() + (int)(Math.random() * 2000);
        this.pathTime = Clock.time() + (int)(Math.random() * 2000);
    }

    @Override
    public void think(Mob mob) 
    {
        // Overrun? restore
        mob.gameMap.setMob(mob.location.x, mob.location.y, mob.getKey());
        
        // Hajo Don't think too heavily
        if(thinkTime >= Clock.time())
        {
            // cool head, erm CPU
            // System.err.println("Mob=" + mob.getKey() + " AI skips thinking.");
            return;
        }
        else
        {
            // System.err.println("Mob=" + mob.getKey() + " AI thinks.");
            thinkTime = Clock.time() + thinkCooldown;
        }
        
        // System.err.println("Mob=" + mob.getKey() + " current goal is: " + goal);
        
        if(home.x == -1 || !isLair(mob, home.x, home.y))
        {
            // Hajo: either no home yet, or home was destroyed
            if(goal != Goal.BUILD_LAIR)
            {
                goal = Goal.FIND_LAIR;
            }
        }
        else
        {
            // sanity check - player might have built something over the imp
            // and the imp is now stuck in a movement blocked area.
            Point p = mob.location;
            if(mob.gameMap.isMovementBlocked(p.x, p.y))
            {
                logger.log(Level.WARNING, "Imp #{0} is stuck at {1}, {2} and will be warped home.",
                        new Object[]{mob.getKey(), mob.location.x, mob.location.y});

                teleportMob(mob, home);
            }
        }
        
        if(goal == Goal.BUILD_LAIR &&
           home.equals(mob.location))
        {
            // Hajo: check space - someone might have 
            // used up the space till we arrived her
            
            boolean ok = checkLairSpace(mob, home.x, home.y);
            
            if(ok)
            {
                placeLair(mob, home.x, home.y);
                goal = Goal.SLEEP;
                mob.setPath(null);
            }
            else
            {
                // search again ...
                home.x = -1;
                home.y = -1;
                goal = Goal.FIND_LAIR;
                mob.setPath(null);
            }
        }
        
        if(goal == Goal.SLEEP)
        {
            if(mob.location.equals(home))
            {
                mob.visuals.setBubble(Features.BUBBLE_SLEEPING);
                mob.visuals.setSleeping(true);
                mob.zOff = 0;
            }
            
            // Hajo: are there jobs?
            if(!game.jobQueue.isEmpty())
            {
                findJob(mob);
            }
        }
        else if(goal == Goal.FIND_JOB)
        {
            // Hajo: are there jobs?
            if(!game.jobQueue.isEmpty())
            {
                findJob(mob);
            }
            else
            {
                goal = Goal.GO_TO_SLEEP;
                mob.setPath(null);
            }
        }
        else if(goal == Goal.GO_TO_JOB)
        {
            if(!currentJob.isValid(mob))
            {
                currentJob = null;
                goal = Goal.GO_TO_SLEEP;
                mob.setPath(null);
            }
            // Hajo: are we there yet?
            else if(mob.getPath() == null)
            {
                goal = Goal.WORKING;
                workStep = 0;
            }
        }
        else if(goal == Goal.WORKING)
        {
            if(currentJob != null)
            {
                // is the job still valid?
                if(!currentJob.isValid(mob))
                {
                    currentJob = null;
                    goal = Goal.FIND_JOB;
                    mob.setPath(null);
                    logger.log(Level.INFO, "Imp #{0} tried to work, but job had become invalid.", mob.getKey());
                }
                else
                {
                    currentJob.execute(mob);
                    
                    // Hajo: Mining is a recurring job
                    if(currentJob instanceof JobMining)
                    {
                        completeMiningJob(mob);
                    }
                    else if(currentJob instanceof JobFetchItem)
                    {
                        completePickupJob(mob);
                        currentJob = null;
                    }
                    else
                    {
                        // Hajo: wait a while extra
                        pathTime = Clock.time() + 10 * thinkCooldown;                        
                        
                        currentJob = null;
                        goal = Goal.FIND_JOB;
                        mob.setPath(null);
                        logger.log(Level.INFO, "Imp #{0} completes current job.", mob.getKey());
                    }
                }
            }
            else
            {
                logger.log(Level.WARNING, "Imp #{0} tried to work, but had no job.", mob.getKey());
                goal = Goal.FIND_JOB;
                mob.setPath(null);
            }
        }
        else if(goal == Goal.GO_TO_SLEEP)
        {
            // Hajo: are there jobs?
            if(!game.jobQueue.isEmpty())
            {
                findJob(mob);
            }
        }
        else if(goal == Goal.DROP_GOLD)
        {
            // Hajo: are we there yet?
            if(mob.getPath() == null)
            {
                dropCarryItem(mob);
                if(currentJob == null)
                {
                    goal = Goal.FIND_JOB;
                }
                else
                {
                    goal = Goal.FIND_PATH_TO_JOB;
                }
            }
        }
        else if(goal == Goal.DROP_ITEM)
        {
            // Hajo: are we there yet?
            if(mob.getPath() == null)
            {
                dropCarryItem(mob);
                goal = Goal.FIND_PATH_TO_JOB;
            }
        }
    }

    @Override
    public void findNewPath(Mob mob) 
    {
        // Hajo Don't think too heavily
        if(pathTime >= Clock.time())
        {
            // cool head, erm CPU
            // System.err.println("Mob=" + mob.getKey() + " AI skips pathfinding.");
            return;
        }
        else
        {
            // System.err.println("Mob=" + mob.getKey() + " is pathfinding.");
            pathTime = Clock.time() + 3 * thinkCooldown;
        }

        SpeciesDescription desc = Species.speciesTable.get(mob.getSpecies());

        if(goal == Goal.FIND_LAIR)
        {
            // Hajo: find an empty dormitory spot
            
            Path path = new Path();
            
            boolean ok = 
            path.findPath(new WayPathSource(mob.gameMap, desc.size, false),
                          new LairPathDestination(mob.gameMap, desc, Features.GROUND_LAIR),
                          mob.location.x, mob.location.y);

            if(ok)
            {
                mob.setPath(path);
                goal = Goal.BUILD_LAIR;
                
                Path.Node node = path.getStep(path.length() - 1);
                home.x = node.x;
                home.y = node.y;
            }
            else
            {
                // Hajo: normal condition at start.
            }
        }
        else if(goal == Goal.FIND_PATH_TO_JOB)
        {
            Point p = currentJob.getLocation();

            Path path = new Path();

            boolean ok;
            
            if(currentJob instanceof JobFetchItem)
            {
                ok = path.findPath(new ImpPathSource(mob, desc.size), 
                                   new LocationPathDestination(p.x, p.y, desc.size),
                                  mob.location.x, mob.location.y);
            }
            else
            {
                ok = path.findPath(new MiningPathSource(mob.gameMap, desc.size), 
                                   new LocationPathDestination(p.x, p.y, 0), 
                                  mob.location.x, mob.location.y);
            }
            
            if(ok)
            {
                // Hajo: Hack - some jobs require the imp to stop
                // at the border of the job square, rather than in the center of it
                // -> we search a new path to the border, now that we know a spot
                // at the border

                if(currentJob instanceof JobMining ||
                   currentJob instanceof JobExcavate)
                {
                    int length = path.length();
                    Path.Node node = path.getStep(length-9);

                    if(node != null)
                    {
                        ok = 
                            path.findPath(new MiningPathSource(mob.gameMap, desc.size), 
                                  new LocationPathDestination(node.x, node.y, 0), 
                                  mob.location.x, mob.location.y);                

                        // logger.log(Level.INFO, "Imp #{0} tries truncated path, result={1} length={2}", new Object[]{mob.getKey(), ok, path.length()});
                    }
                }

                if(ok)
                {
                    mob.setPath(path);
                    mob.visuals.setBubble(Features.BUBBLE_WORK);
                    mob.visuals.setSleeping(false);
                    goal = Goal.GO_TO_JOB;
                    // System.err.println("Imp #" + mob.getKey() + " found a path to it's job at " + p);
                }
                else
                {
                    logger.log(Level.INFO, "Imp #" + mob.getKey() + " can't find a truncated path to it's job at " + p);

                    // Hajo: try the job later again ...
                    
                    if(currentJob instanceof JobExcavate)
                    {
                        game.jobQueue.add(currentJob, JobQueue.PRI_LOW);
                    }
                    
                    currentJob = null;
                    goal = Goal.GO_TO_SLEEP;
                    mob.visuals.setBubble(Features.BUBBLE_GO_SLEEPING);
                    mob.setPath(null);
                }
            }
            else
            {
                logger.log(Level.INFO, "Imp #" + mob.getKey() + " can't find a path to it's job " +
                                   currentJob + " at " + p);

                // Hajo: try the job later again ...
                if(currentJob instanceof JobExcavate)
                {
                    game.jobQueue.add(currentJob, JobQueue.PRI_LOW);
                }
                
                currentJob = null;
                goal = Goal.GO_TO_SLEEP;
                mob.visuals.setBubble(Features.BUBBLE_GO_SLEEPING);
                mob.setPath(null);
            }
        }
        else if(goal == Goal.GO_TO_SLEEP)
        {
            Path path = new Path();
            
            boolean ok =
            path.findPath(new ImpPathSource(mob, desc.size), 
                          new LocationPathDestination(home.x, home.y, 0), 
                          mob.location.x, mob.location.y);
            
            if(ok)
            {
                mob.setPath(path);
                goal = Goal.SLEEP;
                mob.visuals.setBubble(Features.BUBBLE_GO_SLEEPING);
            }
            else
            {
                // Hajo: this is an emergeny case - the imp can't find
                // a path to it's lair. As a workaround, we warp the imp home.
                logger.log(Level.WARNING, "Imp #{0} is stuck at {1}, {2} and will be warped home.",
                        new Object[]{mob.getKey(), mob.location.x, mob.location.y});

                teleportMob(mob, home);
            }
        }
        else if(goal == Goal.GOLD_TO_TREASURY)
        {
            Path path = new Path();
            
            int itemSize = game.world.isArtifact(mob.stats.getCurrent(MobStats.CARRY)) ? 2 : 0;
            
            boolean ok = 
            path.findPath(new ImpPathSource(mob, desc.size), 
                          new FeaturePathDestination(mob.gameMap, 0, itemSize, Features.GROUND_TREASURY, 3),
                          mob.location.x, mob.location.y);
            
            if(ok)
            {
                mob.setPath(path);
                goal = Goal.DROP_GOLD;
            }
            else
            {
                // No more mining ...
                currentJob = null;
                goal = Goal.FIND_JOB;
                mob.visuals.setBubble(0);
                mob.visuals.setSleeping(false);
                mob.stats.setCurrent(MobStats.GOLD, 0);
                mob.setPath(null);
            }
        }
        else if(goal == Goal.ITEM_TO_FORGE || goal == Goal.ITEM_TO_LAB)
        {
            Path path = new Path();

            int requiredGround = (goal == Goal.ITEM_TO_FORGE) ? Features.GROUND_FORGE : Features.GROUND_LABORATORY;

            boolean ok = 
            path.findPath(new ImpPathSource(mob, desc.size), 
                          new FeaturePathDestination(mob.gameMap, 0, 0, requiredGround, 3),
                          mob.location.x, mob.location.y);
            
            if(ok)
            {
                mob.setPath(path);
                mob.visuals.setBubble(mob.stats.getCurrent(MobStats.CARRY));
                goal = Goal.DROP_ITEM;
            }
            else
            {
                logger.log(Level.WARNING, "Imp completed mining job but could not find a path for the produce.");

                // No more mining ... clean up

                // clear the mining symbol
                Point p = currentJob.getLocation();
                int rasterI = p.x/ Map.SUB*Map.SUB;
                int rasterJ = p.y/Map.SUB*Map.SUB;
                mob.gameMap.setItem(rasterI+Map.SUB/2, rasterJ+Map.SUB/2, 0);

                // clear the job
                currentJob = null;
                goal = Goal.FIND_JOB;
                mob.visuals.setBubble(0);
                mob.visuals.setSleeping(false);
                mob.stats.setCurrent(MobStats.CARRY, 0);
                mob.setPath(null);
            }
        }
        else
        {
            // no other goals yet
        }
    }

    @Override
    public void thinkAfterStep(Mob mob) 
    {
        think(mob);
    }
    
    @Override
    public void write(Writer writer) throws IOException
    {
        writer.write("goal=" + goal + "\n");
        writer.write("step=" + workStep + "\n");
        writer.write("homeX=" + home.x + "\n");
        writer.write("homeY=" + home.y + "\n");
        
        if(currentJob == null)
        {
            writer.write("jobType=<null>\n");
        }
        else
        {
            writer.write("jobType=" + currentJob.getClass().getSimpleName() + "\n");
            currentJob.write(writer);
        }        
    }
    
    @Override
    public void read(BufferedReader reader) throws IOException
    {
        String line;
        line = reader.readLine();
        goal = Goal.valueOf(line.substring(5));
        line = reader.readLine();
        workStep = Integer.parseInt(line.substring(5));
        line = reader.readLine();
        home.x = Integer.parseInt(line.substring(6));
        line = reader.readLine();
        home.y = Integer.parseInt(line.substring(6));

        thinkTime = Clock.time() + thinkCooldown;
        pathTime = Clock.time() + 3 * thinkCooldown;
        
        line = reader.readLine();
        if("jobType=<null>".equals(line))
        {
            currentJob = null;
        }
        else
        {
            if(line.contains("JobExcavate"))
            {
                currentJob = new JobExcavate(game, 0, 0);
                currentJob.read(reader);
            }
            else if(line.contains("JobClaimGround"))
            {
                currentJob = new JobClaimGround(game, 0, 0);
                currentJob.read(reader);
            }
            else if(line.contains("JobMining"))
            {
                currentJob = new JobMining(game, 0, 0);
                currentJob.read(reader);
            }
            else if(line.contains("JobFetchItem"))
            {
                currentJob = new JobFetchItem(game, 0, 0, 0);
                currentJob.read(reader);
            }
            else
            {
                currentJob = null;
                logger.log(Level.SEVERE, "Unknown job type: {0}", line);
            }
        }
    }
    
    private void findJob(Mob mob) 
    {
        // Hajo: are there jobs?
        if(!game.jobQueue.isEmpty())
        {
            currentJob = game.jobQueue.nextJob();
            if(currentJob.isValid(mob))
            {
                goal = Goal.FIND_PATH_TO_JOB;
                mob.visuals.setBubble(0);
                mob.setPath(null); // trigger path finding

                logger.log(Level.INFO, "Imp #{0} takes job {1}", new Object[]{mob.getKey(), currentJob});
                
            }
            else
            {
                // Hajo: dump those invalid jobs ...
                currentJob = null;
                findJob(mob);
            }
        }
        else
        {
            goal = Goal.GO_TO_SLEEP;
            mob.visuals.setBubble(0);
            mob.setPath(null); // trigger path finding
        }
    }
    
    private void dropCarryItem(Mob mob) 
    {
        if(mob.stats.getCurrent(MobStats.CARRY) > 0)
        {
            // Hajo: imp carries an item -> drop this item
            int item = mob.stats.getCurrent(MobStats.CARRY); 

            // make some noise?
            if(Features.isCoins(item))
            {
                game.soundPlayer.playFromPosition(Sounds.COINS_DROP, 0.4f, 1.0f,
                        mob.location, game.getViewPosition());
            }
            
            mob.visuals.setBubble(0);
            mob.gameMap.dropItem(mob.location.x, mob.location.y, item);
            mob.stats.setCurrent(MobStats.CARRY, 0);
        }
    }
    

    private void completeMiningJob(Mob mob) 
    {
        // Hajo: show some animation for mining
        if(workStep < 30)
        {
            if((workStep & 3) == 1)
            {
                splashMiningSparks(mob);
            }
            
            // spin some
            int dir = mob.visuals.getDisplayCode() - mob.getSpecies();
            dir = (dir + 1) & 7;
            mob.visuals.setDisplayCode(mob.getSpecies() + dir);

            if((workStep & 7) == 1)
            {
                game.soundPlayer.playFromPosition(Sounds.DIG_SQUARE, 0.15f, (float)(0.7 + (Math.random() *  0.4)),
                        mob.location, game.getViewPosition());
            }

            workStep ++;
        }    
        else
        {
            logger.log(Level.INFO, "Imp #{0} completes mining job.", mob.getKey());
            
            goal = calculateGoalForCargo(mob);
            mob.setPath(null);

            // Hajo: wait a while extra
            pathTime = Clock.time() + 5 * thinkCooldown;
        }
    }
    

    private Goal calculateGoalForCargo(Mob mob)
    {
        Goal newGoal = Goal.GO_TO_SLEEP;
        int cargo = mob.stats.getCurrent(MobStats.CARRY);

        if(cargo > 0)
        {
            // Imp is carrying something. Check where it has to go
            if(cargo == Features.I_MINERAL)
            {
                newGoal = Goal.ITEM_TO_LAB;
            }
            else if(Features.isCoins(cargo) || game.world.isArtifact(cargo))
            {
                newGoal = Goal.GOLD_TO_TREASURY;
            }
            else
            {
                newGoal = Goal.ITEM_TO_FORGE;
            }
        }

        return newGoal;
    }

    private void completePickupJob(Mob mob)
    {
        logger.log(Level.INFO, "Imp #{0} completes pick up job.", mob.getKey());
        
        if(mob.stats.getCurrent(MobStats.CARRY) > 0)
        {
            goal = calculateGoalForCargo(mob);
            mob.setPath(null);
        }
        else
        {
            // Nothing to do ...
            goal = Goal.GO_TO_SLEEP;
            mob.setPath(null);
        }
    }

    
    private void splashMiningSparks(Mob mob)
    {
        for(int i = 0; i<25; i++)
        {
            double speed = 0.15 + Math.random() * 3.0;
            
            mob.visuals.backParticles.addParticle(0, 12, 
                                                  speed * Math.random() * 2.0 - speed, 
                                                  speed * Math.random(),
                                                  20, 
                                                  Features.P_BROWN_SHARD_1 + (int)(Math.random() * 4),
                                                  0xFFFFFFFF);
            mob.visuals.frontParticles.addParticle(0, 12, 
                                                  speed * Math.random() * 2.0 - speed, 
                                                  - speed * Math.random(),
                                                  20, 
                                                  Features.P_BROWN_SHARD_1 + (int)(Math.random() * 4),
                                                  0xFFFFFFFF);
        }
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("ImpAi, goal=");
        sb.append(goal.toString());
        
        if(currentJob != null)
        {
            sb.append(", job=");
            sb.append(currentJob.getClass().getName());
        }
        
        return sb.toString();
    }
}
