package impcity.game.ai;

import impcity.game.Features;
import impcity.game.ImpCity;
import impcity.game.KeeperStats;
import impcity.game.jobs.JobFetchItem;
import impcity.game.jobs.JobQueue;
import impcity.game.Sounds;
import impcity.game.species.Species;
import impcity.game.species.SpeciesDescription;
import impcity.game.processables.FarmSquare;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import impcity.game.Clock;
import impcity.game.Direction;
import static impcity.game.ai.JobPreference.LABORATORY;
import impcity.game.mobs.Mob;
import impcity.game.map.LocationPathDestination;
import impcity.game.map.Map;
import rlgamekit.objects.Registry;
import rlgamekit.pathfinding.Area;
import rlgamekit.pathfinding.Path;

/**
 *
 * @author Hj. Malthaner
 */
public class CreatureAi extends AiBase
{
    private static final Logger logger = Logger.getLogger(CreatureAi.class.getName());
    private static final int THINK_COOLDOWN = 300;
    private static final int MAX_HUNGER = 6000000;
    private static final int MAX_SLEEP = 12000000;

    public enum Goal
    {
        FIND_LAIR, BUILD_LAIR,
        GO_SLEEP, SLEEP,
        GO_RANDOM, GOING,
        FIND_FOOD, FEEDING,
        FIND_WORKPLACE, GO_WORK, WORKING,
        GO_FIGHT, FIGHT,
    }
    
    private Goal goal;
    private long thinkTime;
    private long lastThinkTime;
    private long pathTime;
    private long researchTime;
    private long nextSoundTime;
    private final ImpCity game;
    
    private int hungry;
    private int sleepy;
    

    public CreatureAi(ImpCity game)
    {
        this.game = game;
        this.goal = Goal.FIND_LAIR;
        this.thinkTime = Clock.time() + (int)(Math.random() * 4000);
        this.pathTime = Clock.time() + (int)(Math.random() * 4000);
        this.researchTime = Clock.time();
        this.hungry = 0;
        this.sleepy = 0;
        this.lastThinkTime = Clock.time();
        this.nextSoundTime = Clock.time() + 1000;
    }
    

    public void setHome(Point p)
    {
        home.x = p.x;
        home.y = p.y;
        goal = Goal.GO_SLEEP;
    }
    

    @Override
    public void think(Mob mob, Registry<Mob> mobs) 
    {
        // Overrun? restore
        mob.gameMap.setMob(mob.location.x, mob.location.y, mob.getKey());
      
        // Hajo: animated goals must be processed every step
        if(goal == Goal.WORKING)
        {
            work(mob);
        }
        
        // Is it time to think yet?
        if(thinkTime >= Clock.time())
        {
            // Not yet, keep a cool head, erm CPU
            // System.err.println("Mob=" + mob.getKey() + " AI skips thinking.");
            return;
        }
        else
        {
            // System.err.println("Mob=" + mob.getKey() + " AI thinks.");
            thinkTime = Clock.time() + THINK_COOLDOWN;
        }

        // Hajo: Precision? Seems to be good enough ...
        int deltaTime = (int)((Clock.time() - lastThinkTime) >> 3);
        hungry += deltaTime;
        
        if(goal == Goal.SLEEP)
        {
            sleepy -= deltaTime;
        }
        else
        {
            sleepy += deltaTime;
        }
        
        // System.err.println("Mob=" + mob.getKey() + " reaches hunger level: " + hungry);
        
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
            // once we have a lair, alarms have top priority

            Mob intruder = mobs.get(alarmKey);

            // is the intruder still alive?
            // -> at times the alarmKey has become obsolete and now points to a
            //    normal creature. Needs extra check if the mob is actually an
            //    intruder
            if (intruder != null && intruder.kind == Mob.KIND_INTRUDER) 
            {             
                if (goal != Goal.GO_FIGHT)
                {
                    // called to fight, find path
                    logger.info("Creature " + mob.name + " (" + mob.getKey() + ") was alarmed goes to fight intruder #" + alarmKey);
                    goal = Goal.GO_FIGHT;
                    mob.setPath(null);
                    mob.visuals.setBubble(Features.BUBBLE_FIGHT);                
                }
                if (goal == Goal.GO_FIGHT)
                {
                    // intruders move, we need to update our path now and then
                    Path currentPath = mob.getPath();
                    
                    if(currentPath != null)
                    {
                        Path.Node node = currentPath.getStep(currentPath.length() - 1);

                        Mob other = mobs.get(alarmKey);

                        if(other != null && other.stats.getCurrent(MobStats.VITALITY) > 0) {
                            // our target is still around and alive
                            int dx = other.location.x - node.x;
                            int dy = other.location.y - node.y;
                            int dist2 = dx * dx + dy * dy;

                            if (dist2 > Map.SUB) {
                                // out of strike range, we need a new path
                                mob.setPath(null);
                            }
                        }
                    }
                }
            }
            else
            {
                // intruder is no longer around
                if (goal == Goal.GO_FIGHT)
                {
                    // alarm is done, go random
                    goal = Goal.GO_RANDOM;
                    mob.setPath(null);
                    mob.visuals.setBubble(0);
                    alarm(0);
                }
            }
        }
        
        if(goal == Goal.BUILD_LAIR && home.equals(mob.location))
        {
            // Hajo: check space - someone might have 
            // used up the space till we arrived here
            
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
            
            // Hajo: go for a walk?
            if(Math.random() < 0.004 && sleepy < MAX_SLEEP / 4)
            {
                goal = Goal.GO_RANDOM;
                mob.visuals.setBubble(0);
                mob.visuals.setSleeping(false);
                mob.setPath(null); // trigger path finding
            }
            // Hajo: go to a library?
            if(Math.random() < 0.004 && sleepy < MAX_SLEEP / 4)
            {
                goal = Goal.GO_RANDOM;
                mob.visuals.setBubble(0);
                mob.visuals.setSleeping(false);
                mob.setPath(null); // trigger path finding
            }
            if(hungry > MAX_HUNGER)
            {
                // hungry?
                goal = Goal.FIND_FOOD;
                mob.visuals.setBubble(Features.BUBBLE_FOOD);
                mob.visuals.setSleeping(false);
                mob.setPath(null); // trigger path finding
            }
        }
        else if(goal == Goal.GOING)
        {
            // Are we there yet?
            Path path = mob.getPath();
            
            if(path == null)
            {
                goal = Goal.GO_RANDOM;
                mob.visuals.setBubble(0);
                pathTime = Clock.time() + (int)(20 * Math.random()) * THINK_COOLDOWN;
            }
            
            // Hajo: tired again?
            if(Math.random() < 0.004 && sleepy > MAX_SLEEP / 2)
            {
                goal = Goal.GO_SLEEP;
                mob.visuals.setBubble(Features.BUBBLE_GO_SLEEPING);
                mob.setPath(null); // trigger path finding
            }
            if(Math.random() < 0.02)
            {
                goal = Goal.FIND_WORKPLACE;
                mob.visuals.setBubble(Features.BUBBLE_WORK);
                mob.setPath(null); // trigger path finding
            }
            // in a worky mood?
            /*
            if(Math.random() < 0.1)
            {
                goal = Goal.FIND_WORKSHOP;
                mob.visuals.setBubble(Features.BUBBLE_WORK);
                mob.setPath(null); // trigger path finding
            }
            */
            // hungry?
            if(hungry > MAX_HUNGER)
            {
                goal = Goal.FIND_FOOD;
                mob.visuals.setBubble(Features.BUBBLE_FOOD);
                mob.setPath(null); // trigger path finding
            }
        }
        else if(goal == Goal.FEEDING)
        {
            // Are we there yet?
            Path path = mob.getPath();
            
            if(path == null)
            {
                game.soundPlayer.playFromPosition(Sounds.CRUNCH_MUNCH, 0.4f, 1.0f, mob.location, game.getViewPosition());
                int n = mob.gameMap.getItem(mob.location.x, mob.location.y);
                mob.gameMap.setItem(mob.location.x, mob.location.y, 0);
                
                if(n > 0)
                {
                    logger.log(Level.INFO, "Mob #{0} ate food #{1}", new Object[] {mob.getKey(), n});
                    if(n > Features.I_MUSHROOM)
                    {
                        hungry = 0;
                    }
                    else
                    {
                        // mushrooms are not that feeding like the other plants
                        hungry -= MAX_HUNGER/2;
                    }
                    
                    goal = Goal.GO_RANDOM;
                    mob.visuals.setBubble(0);
                }
                else
                {
                    logger.log(Level.INFO, "Mob #{0} needs to search for food again.", mob.getKey());
                    goal = Goal.FIND_FOOD;
                    mob.setPath(null);
                    addReputation(-10);
                }
            }
        }
        else if(goal == Goal.GO_WORK)
        {
            // Are we there yet?
            Path path = mob.getPath();
            
            if(path == null)
            {
                goal = Goal.WORKING;
                mob.visuals.setBubble(0);
                prepareWork(mob);
            }
        }
        else if(goal == Goal.WORKING)
        {
            // randomly stop working
            if(Math.random() < 0.01)
            {
                finishWork(mob);
                goal = Goal.GO_SLEEP;
                // Hajo: small break before we go pathfinding.
                thinkTime = Clock.time() + 10 * THINK_COOLDOWN;
                pathTime = Clock.time() + 10 * THINK_COOLDOWN;
            }
        }
    }
    

    @Override
    public void findNewPath(Mob mob, Registry<Mob> mobs) 
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
            pathTime = Clock.time() + 3 * THINK_COOLDOWN;
        }

        SpeciesDescription desc = Species.speciesTable.get(mob.getSpecies());
        
        if(goal == Goal.FIND_LAIR)
        {
            // Hajo: find an empty dormitory spot
            
            Path path = new Path();
            boolean ok = findLair(mob.gameMap, desc, path, mob.location);
            
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
                // try looking again in one second
                pathTime = pathTime + 1000;
            }
        }
        else if(goal == Goal.GO_RANDOM)
        {
            Area area = new Area();
            area.findArea(new WayPathSource(mob.gameMap, desc.size, false),
                          mob.location.x, mob.location.y);
            
            ArrayList <Point> locations = area.getArea();

            if(locations.isEmpty())
            {
                logger.log(Level.WARNING, "Creature #{0} is stuck at {1}, {2} and will be warped home.",
                        new Object[]{mob.getKey(), mob.location.x, mob.location.y});

                mob.teleportTo(home);
            }
            else
            {
                Point p = locations.get((int) (Math.random() * locations.size()));

                Path path = new Path();

                path.findPath(new WayPathSource(mob.gameMap, desc.size, false),
                        new LocationPathDestination(p.x, p.y, 0),
                        mob.location.x, mob.location.y);

                mob.setPath(path);
                goal = Goal.GOING;
            }
        }
        else if(goal == Goal.FIND_FOOD)
        {
            HashSet <Integer> plants = new HashSet<>();
            
            plants.add(Features.I_MUSHROOM);
            plants.add(Features.PLANTS_FIRST + Features.PLANTS_STRIDE * 3 + 0);
            plants.add(Features.PLANTS_FIRST + Features.PLANTS_STRIDE * 3 + 1);
            plants.add(Features.PLANTS_FIRST + Features.PLANTS_STRIDE * 3 + 2);
            plants.add(Features.PLANTS_FIRST + Features.PLANTS_STRIDE * 3 + 3);
            plants.add(Features.PLANTS_FIRST + Features.PLANTS_STRIDE * 3 + 4);
            plants.add(Features.PLANTS_FIRST + Features.PLANTS_STRIDE * 3 + 5);
            plants.add(Features.PLANTS_FIRST + Features.PLANTS_STRIDE * 3 + 6);

            Path path = new Path();
            
            boolean ok = 
            path.findPath(new WayPathSource(mob.gameMap, desc.size, false),
                          new FeaturePathDestination(mob.gameMap, plants, 0, Features.GROUND_GRASS_DARK, 0),
                          mob.location.x, mob.location.y);

            if(ok)
            {
                mob.setPath(path);
                goal = Goal.FEEDING;
            }
            else
            {
                addReputation(-10);
                
                // Hajo: No food - try to find a path to farmland
                
                // Hajo: randomly take a farm tile
                List <FarmSquare> farmland = game.getFarmland();

                if(farmland.isEmpty())
                {
                    // Hajo: no farms ...
                    goal = Goal.GO_RANDOM;
                    addReputation(-10);
                }
                else
                {
                    FarmSquare farm = farmland.get((int)(Math.random() * farmland.size()));
                    int x = farm.x + Map.SUB / 4 + (int)(Math.random() * Map.SUB/2);
                    int y = farm.y + Map.SUB / 4 + (int)(Math.random() * Map.SUB/2);
                    
                    ok = 
                    path.findPath(new WayPathSource(mob.gameMap, desc.size, false),
                                  new LocationPathDestination(x, y, 0),
                                  mob.location.x, mob.location.y);

                    if(ok && path.length() > 0)
                    {
                        mob.setPath(path);
                        goal = Goal.FEEDING;
                    }
                    else
                    {
                        // No reachable farms
                        mob.visuals.setBubble(0);
                        goal = Goal.GO_RANDOM;
                    }
                }
                
                goal = Goal.GO_RANDOM;
                logger.log(Level.INFO, "Mob #{0} couldn't find food.", mob.getKey());
            }
        }
        else if(goal == Goal.FIND_WORKPLACE)
        {
            findPathToWorkplace(mob, desc);
        }
        else if(goal == Goal.GO_SLEEP)
        {
            Path path = new Path();
            
            path.findPath(new WayPathSource(mob.gameMap, desc.size, false),
                          new LocationPathDestination(home.x, home.y, 0), 
                          mob.location.x, mob.location.y);
            
            mob.setPath(path);
            goal = Goal.SLEEP;
            mob.visuals.setBubble(Features.BUBBLE_GO_SLEEPING);
        }
        else if(goal == Goal.GO_FIGHT) {
            
            Mob intruder = mobs.get(alarmKey);
            
            if(intruder != null) 
            {
                Point alarmLocation = intruder.location;
                logger.info("Creature " + mob.name + " (" + mob.getKey() + ") is alarmed and tries to find a path to intruder #" + alarmKey + " at " + alarmLocation);
                Path path = new Path();

                path.findPath(new WayPathSource(mob.gameMap, desc.size, false),
                              new LocationPathDestination(alarmLocation.x, alarmLocation.y, 0), 
                              mob.location.x, mob.location.y);

                mob.setPath(path);
                // goal = Goal.FIGHT;
                mob.visuals.setBubble(Features.BUBBLE_FIGHT);
            }
            else
            {
                // intruder is no longer around
                alarm(0);
            }
        }
        else
        {
            // no other goals yet
        }
        
        if(mob.getPath() != null && goal != Goal.BUILD_LAIR)
        {
            // Hajo: mob found a new path
            playStartingSound(mob, desc);
        }
    }


    private void playStartingSound(Mob mob, SpeciesDescription desc)
    {
        if(desc.startingSound >= 0) // got a sound ?
        {
            game.soundPlayer.playFromPosition(desc.startingSound, 0.9f, 1.0f, mob.location, game.getViewPosition());
        }
    }


    /**
     * This is a complicated case, because some workplaces
     * are only a specific ground (farmland) while others
     * require the creature to show up in a certain position
     * relative to an item (bookshelf, forge, distill ...)
     */
    private void findPathToWorkplace(Mob mob, SpeciesDescription desc)
    {
        List <Point> workplaces = null;

        switch(desc.jobPreference)
        {
            case FARM:
                workplaces = game.getInnerFarmlandLocations();
                if (workplaces.isEmpty()) {
                    workplaces = game.getFarmlandLocations();
                }
                break;
            case LIBRARY:
                workplaces = game.getLibraries();
                break;
            case FORGE:
                workplaces = game.getForges();
                break;
            case LABORATORY:
                workplaces = game.getLaboratories();
                break;
        }

        // override - ghosts can only do library research
        if(mob.isGhost())
        {
            workplaces = game.getLibraries();
        }
        
        if(workplaces == null || workplaces.isEmpty())
        {
            logger.log(Level.INFO, "Mob {0} could not find a workplace",
                    Species.speciesTable.get(mob.getSpecies()).name);
            // Hajo: no suitable workplaces ...
            mob.visuals.setBubble(0);
            goal = Goal.GO_RANDOM;
            addReputation(-10);
        }
        else
        {
            // Some creatures are actually happy to have a workplace
            addReputation(+10);

            Point p = findWorkingSpot(mob, desc, workplaces);
            boolean ok = false;            
            Path path = new Path();

            if(p != null)
            {
                ok =
                path.findPath(new WayPathSource(mob.gameMap, desc.size, false),
                              new LocationPathDestination(p.x, p.y, 0),
                              mob.location.x, mob.location.y);
            }
            
            if(ok && path.length() > 0)
            {
                mob.setPath(path);
                goal = Goal.GO_WORK;
            }
            else
            {
                addReputation(-10);
                mob.visuals.setBubble(0);
                goal = Goal.GO_RANDOM;
                logger.log(Level.INFO, "Mob {0}, a {1} could not find a path to a workplace",
                        new Object[]{mob.getKey(), Species.speciesTable.get(mob.getSpecies()).name});
            }
        }
    }

    
    private Point findWorkingSpot(Mob mob, SpeciesDescription desc, List <Point> workplaces)
    {
        Map map = mob.gameMap;
        Point p;

        // First, look for a random workplace square
        Point work = workplaces.get((int) (Math.random() * workplaces.size()));

        switch(desc.jobPreference)
        {
            case LIBRARY:
                p = findNearestFeatureOnGround(map, work, Features.I_BOOKSHELF_HALF_RIGHT, Features.GROUND_LIBRARY);
                if(p != null)
                {
                    p.x += 4;
                    p.y += (int)(Math.random() * 6) - 1;
                }
                break;
                
            case FORGE:
                p = findNearestFeatureOnGround(map, work, Features.I_ANVIL, Features.GROUND_FORGE);
                if(p != null)
                {
                    p.x += (int)(Math.random() * 3) - 1;
                    p.y += 3 + (int)(Math.random() * 2);
                }
                break;
                
            case LABORATORY:
                p = findNearestFeatureOnGround(map, work, Features.I_LAB_TABLE, Features.GROUND_LABORATORY);
                if(p != null)
                {
                    // sit in a circle around the lab table

                    int tries = 0;

                    while(tries < 16)
                    {
                        double angle = Math.random() * Math.PI * 2.0;
                        int cr = 6;
                        int cx = p.x + (int)(Math.cos(angle) * cr);
                        int cy = p.y + (int)(Math.sin(angle) * cr);

                        if(mob.gameMap.isMovementBlockedRadius(cx, cy, desc.size))
                        {
                            tries ++;
                        }
                        else
                        {
                            p.x = cx;
                            p.y = cy;
                            // seems we can go there ...
                            break;
                        }
                    }
                }
                break;

            case FARM:
                p = new Point();
                p.x = work.x + (int) (Math.random() * Map.SUB);
                p.y = work.y + (int) (Math.random() * Map.SUB);
                break;
            default:
                p = new Point();
                // around the middle ...
                p.x = work.x + Map.SUB / 4 + (int) (Math.random() * Map.SUB / 2);
                p.y = work.y + Map.SUB / 4 + (int) (Math.random() * Map.SUB / 2);
        }
        
        return p;
    }


    private static Point findNearestFeatureOnGround(Map map, Point start, int feature, int ground)
    {
        Path nearestWorkspot = new Path();

        nearestWorkspot.findPath(new WayPathSource(map, 0, true),
                                 new FeaturePathDestination(map, feature, 0, ground, 0),
                                 start.x, start.y);

        int length = nearestWorkspot.length();
        Path.Node node = nearestWorkspot.getStep(length - 1);

        Point result = null;
        if(node != null)
        {
            result = new Point(node.x, node.y);
        }

        return result;
    }


    public boolean findLair(Map map, SpeciesDescription desc, Path path, Point location) 
    {
        boolean ok = 
            path.findPath(new WayPathSource(map, desc.size, false),
                          new LairPathDestination(map, desc, Features.GROUND_LAIR),
                          location.x, location.y);
        return ok;
    }
    
    
    @Override
    public void thinkAfterStep(Mob mob, Registry<Mob> mobs) 
    {
        think(mob, mobs);
    }
    
    
    @Override
    public void write(Writer writer) throws IOException
    {
        writer.write("goal=" + goal + "\n");
        writer.write("homeX=" + home.x + "\n");
        writer.write("homeY=" + home.y + "\n");
        writer.write("hungry=" + hungry + "\n");
        writer.write("sleepy=" + sleepy + "\n");
        writer.write("intruder=" + alarmKey + "\n");
    }
    
    
    @Override
    public void read(BufferedReader reader) throws IOException
    {
        String line;
        line = reader.readLine();
        goal = Goal.valueOf(line.substring(5));
        line = reader.readLine();
        home.x = Integer.parseInt(line.substring(6));
        line = reader.readLine();
        home.y = Integer.parseInt(line.substring(6));
        line = reader.readLine();
        hungry = Integer.parseInt(line.substring(7));
        line = reader.readLine();
        sleepy = Integer.parseInt(line.substring(7));
        line = reader.readLine();
        alarmKey = Integer.parseInt(line.substring(9));
        
        this.researchTime = Clock.time();
        this.lastThinkTime = Clock.time();
    }


    /**
     * This is called once the creature reaches the workplace.
     * @param mob The creature
     */
    private void prepareWork(Mob mob) 
    {
        int rasterI = mob.location.x/Map.SUB*Map.SUB;
        int rasterJ = mob.location.y/Map.SUB*Map.SUB;

        switch (mob.getSpecies()) 
        {
            case Species.BOOKWORMS_BASE:
                prepareLibraryWork(mob);
                break;
                
            case Species.HAT_MAGE_BASE:
                prepareLaboratoryWork(mob);
                break;
                
            case Species.CONIANS_BASE:
                prepareForgeWork(mob, rasterI, rasterJ);
                break;
                
            default:
                break;
        }

        int ground = mob.gameMap.getFloor(rasterI, rasterJ);
        if(ground >= Features.GROUND_LIBRARY && ground <= Features.GROUND_LIBRARY + 3)
        {
            researchTime = Clock.time();
        }
    }


    private void prepareForgeWork(Mob mob, int rasterI, int rasterJ)
    {
        Point p = game.forgeRooms.scanForResources(mob.gameMap,
//                                             (resource) -> {return resource == Features.I_COPPER_ORE || resource == Features.I_TIN_ORE;},
                                             (resource) -> {return resource == Features.I_COPPER_ORE;},
                                             rasterI, rasterJ);
        if(p == null)
        {
            // nothing to do here, no resources available
            goal = Goal.GO_RANDOM;
        }
        else
        {
            // Nice! there is stuff to play with
            addReputation(5);
        }
    }


    private void prepareLaboratoryWork(Mob mob)
    {
        // face the distill
        Mob distill = findClosestDistill(mob);
        int cx = distill.location.x;
        int cy = distill.location.y;
        int direction = Direction.dirFromVector(cx - mob.location.x, cy - mob.location.y);
        mob.visuals.setDisplayCode(Species.HAT_MAGE_BASE + direction);
    }


    private static void prepareLibraryWork(Mob mob)
    {
        // Turn worm towards the bookshelf
        int direction = 6 + (int)(Math.random() * 3) & 7;
        mob.visuals.setDisplayCode(Species.BOOKWORMS_BASE + direction);
    }


    private void finishWork(Mob mob) 
    {
        if(mob.getSpecies() == Species.HAT_MAGE_BASE)
        {
            mob.zOff = 0;
        }
    }
    
    
    private void work(Mob mob)
    {
        int species = mob.getSpecies();
        
        workStep ++;
        
        // Hajo: todo: there should be a check for type of work
        // here, not species
        switch (species) 
        {
            case Species.CONIANS_BASE:
                workingConian(mob);
                break;
            case Species.BOOKWORMS_BASE:
                workingBookworm(mob);
                break;
            case Species.POWERSNAILS_BASE:
                workingPowersnail(mob, species);
                break;
            case Species.HAT_MAGE_BASE:
                workingHatMage(mob);
                break;
            default:
                // do something ... spin around thoughtfully
                
                if((workStep & 15) == 0)
                {
                    int dir = mob.visuals.getDisplayCode() - species;
                    dir = (dir + 1) & 7;
                    mob.visuals.setDisplayCode(species + dir);
                }   break;
        }

        if(workStep > 63)
        {
            workStep = 0;
            produce(mob);
        }    
    }


    /** Powersnails spread plant seeds */
    private void workingPowersnail(Mob mob, int species)
    {
        if((workStep % 12) == 0)
        {
            int dir = mob.visuals.getDisplayCode() - species;
            dir = (dir + 1) & 7;
            mob.visuals.setDisplayCode(mob.getSpecies() + dir);

            spreadSeedlings(mob);
        }
    }

    
    private void workingBookworm(Mob mob)
    {
        if((workStep & 15) == 0)
        {
            mob.visuals.frontParticles.addParticle(-2 + (2 - (int)(Math.random() * 5)), 24,
                                                  0, 0.6,
                                                  120,
                                                  Features.GLYPHS_FIRST + (int)(Math.random() * Features.GLYPHS_COUNT),
                                                  0x80FFFFFF);
        }
    }
    

    private void workingConian(Mob mob)
    {
        if(nextSoundTime < Clock.time())
        {
            nextSoundTime = Clock.time() + 3000;
            game.soundPlayer.playFromPosition(Sounds.FORGE_WORK, 0.2f, 1.0f, mob.location, game.getViewPosition());
        }

        // do something ... spin around
        int dir = mob.visuals.getDisplayCode() - mob.getSpecies();
        dir = (dir + 1) & 7;
        mob.visuals.setDisplayCode(mob.getSpecies() + dir);

        double speed = 5;
        mob.visuals.backParticles.addParticle(0, 0,
                                              speed * Math.random() * 2.0 - speed, speed * Math.random(),
                                              20,
                                              Features.P_ORANGE_SPARK_1 + (int)(Math.random() *3),
                                              0xFFFFFFFF);
        mob.visuals.frontParticles.addParticle(0, 0,
                                              speed * Math.random() * 2.0 - speed, speed * Math.random(),
                                              20,
                                              Features.P_ORANGE_SPARK_1 + (int)(Math.random() *3),
                                              0xFFFFFFFF);
    }
    

    private void workingHatMage(Mob mob)
    {
        // levitate some up and down
        int z = 20 + (int)(Math.sin(workStep/32.0 * Math.PI) * 16);
        mob.zOff = z << 16;

        Mob distillGenerator = findClosestDistill(mob);
        
        if(distillGenerator != null)
        {
            int particle;
            int color;
            double speed;
            
            if(Math.random() < 0.5)
            {
                particle = Features.P_SILVER_SPARK_1 + (int)(Math.random() * 9);
                color = 0xFFFFFFFF;
                speed = 3;
            }
            else
            {
                // clouds
                particle = Features.I_STEAM_CLOUD + (int)(Math.random() * 2);
                color = 0x40FFFFFF;
                speed = 1.5;
            }

            distillGenerator
                    .visuals
                    .backParticles
                    .addParticle(2 - (int)(Math.random() * 8.0), (int)(Math.random() * 40.0),
                        speed * (1.0 - Math.random() * 2.0), speed * Math.random(),
                18,
                        particle,
                        color);
        }
    }
    

    private Mob findClosestDistill(Mob mob)
    {
        int best = 250;
        Mob distillGenerator = null;
        
        // find closest generator
        for(Mob generator : game.generators)
        {
            if(generator.stats.getCurrent(MobStats.GENERATOR) == MobStats.G_DISTILL)
            {
                int dx = generator.location.x - mob.location.x;
                int dy = generator.location.y - mob.location.y;
                int d = dx * dx + dy * dy;

                if(d < best) 
                {
                    best = d;
                    distillGenerator = generator;
                }
            }
        }
        
        return distillGenerator;
    }
    

    private void produce(Mob mob)
    {
        int rasterI = mob.location.x/Map.SUB*Map.SUB;
        int rasterJ = mob.location.y/Map.SUB*Map.SUB;
        
        int ground = mob.gameMap.getFloor(rasterI, rasterJ);
        if(ground >= Features.GROUND_FORGE && ground <= Features.GROUND_FORGE + 3)
        {
            produceInForge(mob, rasterI, rasterJ);
        }
        else if(ground >= Features.GROUND_LIBRARY && ground <= Features.GROUND_LIBRARY + 3)
        {
            produceInLibrary();
        }
        else if(ground >= Features.GROUND_LABORATORY && ground <= Features.GROUND_LABORATORY + 3)
        {
        }

        // Mobs gain experience while working
        mob.addExperience(1);
    }
    

    private void spreadSeedlings(Mob mob) 
    {
        Map map = mob.gameMap;
        int radius = Map.SUB + 10;
        int xr = (int)(Math.random() * radius) - radius / 2;
        int yr = (int)(Math.random() * radius) - radius / 2;

        boolean ok = true;
        int dist2 = xr*xr + yr*yr;

        // must be inside circle
        ok &= dist2 < radius * radius / 4;
                
        xr += mob.location.x;
        yr += mob.location.y;
                
        // must be a reachable location ...
        int size = 5;
        for(int j=-size; j<=size && ok; j++)
        {
            for(int i=-size; i<=size && ok; i++)
            {           
                int xpos = xr + i;
                int ypos = yr + j;

                int ground = map.getFloor(xpos - (xpos % Map.SUB), ypos - (ypos % Map.SUB));
                ok &= (ground >= Features.GROUND_GRASS_DARK && ground < Features.GROUND_GRASS_DARK + 3);
            }
        }

        if(ok)
        {
            int n = map.getItem(xr, yr);

            if(n == 0 || Features.DUST_SET.contains(n))
            {
                // empty square. Plant something new
                int choice = (int)(Math.random() * 10);

                if(choice < 7)
                {
                    map.setItem(xr, yr, Features.PLANTS_FIRST + choice);
                }
                else
                {
                    map.setItem(xr, yr, Features.I_MUSHROOM);
                }   
                
                game.soundPlayer.playFromPosition(Sounds.PLANTING, 0.1f, 1.0f, mob.location, game.getViewPosition());
            }
        }

        double speed = 3;
        for(int i=0; i<2; i++)
        {
            mob.visuals.backParticles.addParticle(0, 0, 
                                                  speed * Math.random() * 2.0 - speed, 
                                                  speed * Math.random(),
                                                  12, 
                                                  Features.P_BROWN_SHARD_1 + (int)(Math.random() * 4),
                                                  0xFFFFFFFF);
            mob.visuals.frontParticles.addParticle(0, 0, 
                                                  speed * Math.random() * 2.0 - speed, 
                                                  - speed * Math.random(),
                                                  12, 
                                                  Features.P_BROWN_SHARD_1 + (int)(Math.random() * 4),
                                                  0xFFFFFFFF);
        }    
    }
    

    private void addReputation(int amount)
    {
        Mob keeper = game.world.mobs.get(game.getPlayerKey());
        int rep = keeper.stats.getCurrent(KeeperStats.REPUTATION);
        keeper.stats.setCurrent(KeeperStats.REPUTATION, rep + amount);
    }


    private void produceInForge(Mob mob, int rasterI, int rasterJ) 
    {
        Map map = mob.gameMap;
        Point p = game.forgeRooms.scanForResources(map,
                // (resource) -> {return resource == Features.I_COPPER_ORE || resource == Features.I_TIN_ORE;},
                (resource) -> {return resource == Features.I_COPPER_ORE;},
                rasterI, rasterJ);

        if(p == null)
        {
            // No resources -> stop working
            goal = Goal.GO_RANDOM;
        }
        else
        {
            map.setItem(p.x, p.y, 0);
            map.dropItem(mob.location.x,
                         mob.location.y,
                         Features.I_COPPER_COINS,
                         (int x, int y) -> {
                            JobFetchItem job = new JobFetchItem(game, x, y, Features.I_COPPER_COINS);
                            game.jobQueue.add(job, JobQueue.PRI_LOW);
                         });
        }
    }

    
    private void produceInLibrary() 
    {
        Mob keeper = game.world.mobs.get(game.getPlayerKey());

        // first, accumulate wisdom
        long now = Clock.time();
        int research = (int)((now - researchTime) >> 3);        

        game.research.addRoomResearch(keeper.stats, research);
        game.research.addQuestResearch(game, keeper.stats, research);
        researchTime = Clock.time();
    }
}
