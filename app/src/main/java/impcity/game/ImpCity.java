package impcity.game;

import impcity.game.ui.*;
import impcity.game.species.Species;
import impcity.game.species.SpeciesDescription;
import impcity.game.processables.FarmSquare;
import impcity.game.ai.ImpAi;
import impcity.game.ai.CreatureAi;
import impcity.game.ai.MobStats;
import impcity.game.jobs.JobFrame;
import impcity.game.jobs.JobQueue;
import impcity.game.processables.PortalSquare;
import impcity.game.quests.Quest;
import impcity.game.quests.QuestGenerator;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import impcity.game.Clock;
import impcity.game.GameInterface;
import impcity.game.Texture;
import impcity.game.TextureCache;
import impcity.ogl.GlTextureCache;
import impcity.game.mobs.Mob;
import impcity.game.World;
import impcity.game.ai.Ai;
import impcity.game.map.LocationCallback;
import impcity.game.map.Map;
import impcity.game.map.RectArea;
import impcity.game.mobs.MovementJumping;
import impcity.oal.SoundPlayer;
import impcity.ogl.IsoDisplay;
import impcity.ui.PostRenderHook;
import impcity.ui.TimedMessage;
import java.lang.reflect.Field;
import java.util.Arrays;
import org.lwjgl.LWJGLException;
import rlgamekit.objects.Cardinal;
import rlgamekit.objects.Registry;

/**
 * Imp city main class.
 * 
 * @author Hj. Malthaner
 */
public class ImpCity implements PostRenderHook, GameInterface
{
    private static final String nameVersion = "Imp City v0.18";
    
    private static final Logger logger = Logger.getLogger(ImpCity.class.getName());
    
    /** Dungeon processor will suspend while this is true */
    public volatile boolean processorLock = false;
    public volatile boolean processorActive = false;
    
    private final TextureCache textureCache;
    private final IsoDisplay display;
    public final SoundPlayer soundPlayer;
    
    public int mouseI, mouseJ;

    int playerKey;
    
    public final World world;
    private Mob player;
    public final JobQueue jobQueue = new JobQueue();
    private final GameDisplay gameDisplay;
    private final List <FarmSquare> farmland = Collections.synchronizedList(new ArrayList<FarmSquare>());
    private final List <PortalSquare> portals = Collections.synchronizedList(new ArrayList<PortalSquare>());
    private final List <Point> lairs = Collections.synchronizedList(new ArrayList<Point>());
    private final List <Point> treasuries = Collections.synchronizedList(new ArrayList<Point>());
    private final List <Point> libraries = Collections.synchronizedList(new ArrayList<Point>());
    private final List <Point> forges = Collections.synchronizedList(new ArrayList<Point>());
    private final List <Point> workshops = Collections.synchronizedList(new ArrayList<Point>());
    private final List <Point> hospitals = Collections.synchronizedList(new ArrayList<Point>());
    private final List <Point> claimed = Collections.synchronizedList(new ArrayList<Point>());
    
    public final List <Room> forgeRooms = new ArrayList<Room>();
    public final List <Mob> generators = Collections.synchronizedList(new ArrayList<Mob>());
    public final List <Quest> quests = Collections.synchronizedList(new ArrayList<Quest>());


    static
    {
        Logger.getLogger(ImpCity.class.getName()).log(Level.INFO,
                "App directory: " + (new File("./")).getAbsolutePath());
        
        try 
        {
            // addLibraryPath("lwjgl-2.9.3\\native\\windows");
            // addLibraryPath("lwjgl-2.9.3/native/linux");
            
            // System.setProperty("org.lwjgl.util.Debug", "true");
        }
        catch (Exception ex) 
        {
            Logger.getLogger(ImpCity.class.getName()).log(Level.SEVERE, "Failure in static init", ex);
        }
    }

    
    public static void addLibraryPath(String pathToAdd) throws Exception 
    {
        Logger.getLogger(ImpCity.class.getName()).log(Level.INFO, "Adding library path: " + pathToAdd);

        Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
        usrPathsField.setAccessible(true);

        String[] paths = (String[]) usrPathsField.get(null);

        for (String path : paths)
        {
            if (path.equals(pathToAdd))
            {
                return;
            }
        }
        
        String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
        newPaths[newPaths.length - 1] = pathToAdd;
        usrPathsField.set(null, newPaths);
    }
    
    
    public ImpCity() throws LWJGLException, IOException
    {
        Clock.init(System.currentTimeMillis());
        Clock.addClockListener(new MyClockListener());
        
        mouseI = -1;
        mouseJ = -1;

        world = new World();

        textureCache = new GlTextureCache();
        display = new IsoDisplay(world.mobs, textureCache);
        display.create();        
        
        gameDisplay = new GameDisplay(this, display);
        
        soundPlayer = new SoundPlayer();
    }
    
    
    public void initialize() throws IOException
    {
        final Texture intro = textureCache.loadTexture("/ui/dance_of_rebirth_by_shiroikuro.jpg", false);
        
        textureCache.initialize(new TextureCache.LoaderCallback() 
        {
            @Override
            public void update(String msg)
            {
                splash(intro, msg);
            }
        });
        
        
        splash(intro, "Preparing map ...");
        loadMap("/big_60.map");

        logger.log(Level.INFO, "Map loaded. Size={0}x{1}", new Object[]{player.gameMap.getWidth(), player.gameMap.getHeight()});
        
        display.map = player.gameMap;
        
        // display.map.recalculateBlockedAreas();
        display.centerOn(player);
        display.setTitle(nameVersion);
        
        
        soundPlayer.init();
        
        String PATH = "/sfx/";

        String [] sampleFiles = new String []
        {
            PATH + "click.wav",
            PATH + "wosh.wav",
            PATH + "new_plant.wav",
            PATH + "magic_farmland.wav",
            PATH + "magic_library.wav",
            PATH + "deselect.wav",
            PATH + "arrival_new.wav",
            PATH + "magic_treasury.wav",
            PATH + "magic_lair.wav",
            PATH + "magic_workshop.wav",
            PATH + "381547__tumbleweed3288__falling-and-rolling-stones_excerpt.wav",
            PATH + "25060__wim__roofhammering01_excerpt.wav",
            PATH + "20797__acclivity__fly_excerpt.wav",
            PATH + "445974__breviceps__cartoon-slurp_excerpt.wav",
            PATH + "273722__thearxx08__angle-grinder.wav",
            PATH + "170957__timgormly__metal-ping.wav",
            PATH + "428953__jbp__crunching-on-a-snack-chomping.wav"
        };

        splash(intro, "Loading sounds ...");
        if(!soundPlayer.loadSamples(sampleFiles))
        {
            logger.log(Level.SEVERE, "Error while loading sound data.");
        }
        
        player.stats.setCurrent(KeeperStats.GOLD, 0);
        player.stats.setCurrent(KeeperStats.RESEARCH, 0);
        player.stats.setMin(KeeperStats.RESEARCH, 0);
        player.stats.setMax(KeeperStats.RESEARCH, 10000); // research needed for next discovery
    }

    @Override
    public World getWorld()
    {
        return world;
    }

    @Override
    public int getPlayerKey()
    {
        return playerKey;
    }
    
    public Point getViewPosition()
    {
        return display.getViewPosition();
    }
    
    public void run()
    {
        display.postRenderHook = this;
        display.mouseHandler = new ImpCityMouseHandler(this, gameDisplay, display, soundPlayer);
        display.keyHandler = new ImpCityKeyHandler(this, display, gameDisplay);
        
        DungeonProcessingThread dpt = new DungeonProcessingThread(this);
        dpt.start();
        
        DungeonSweepingThread dst = new DungeonSweepingThread(this, gameDisplay, display);
        dst.start();
        
        display.run();
    }
    
    public void destroy()
    {
        soundPlayer.destroy();
        display.destroy();
    }
    
    @Override
    public void displayMore() 
    {
        gameDisplay.displayMore();
        update();
    }

    private void loadMap(String mapName)
    {
        Map gameMap = new Map(16, 16);

        try
        {
            InputStream in  = this.getClass().getResourceAsStream(mapName);
            gameMap.load(in);
            convertMap(gameMap);
        }
        catch(Exception ex)
        {
            logger.log(Level.SEVERE, mapName, ex);
        }

        player = new Mob(world, 30, 350, Species.GLOBOS_BASE, gameMap, null, 45, new MovementJumping());
        playerKey = world.mobs.nextFreeKey();
        world.mobs.put(playerKey, player);
        player.setKey(playerKey);
        player.visuals.setDisplayCode(0);
        
        activateMap(gameMap);
        makeImps(gameMap);
    }

    public static void main(String[] args)
    {
        int result = 0;
        
        ImpCity game = null;
        try
        {
            game = new ImpCity();
            game.initialize();
            game.run();
        } 
        catch (Exception ex)
        {
            logger.log(Level.SEVERE, ex.toString(), ex);
        } 
        finally
        {
            if(game != null)
            {
                try
                {
                    game.destroy();
                }
                catch (Exception ex)
                {
                    logger.log(Level.SEVERE, ex.toString(), ex);
                    result = 3;
                } 
            }
            else
            {
                result = 1;
            }
        }
        
        System.exit(result);
    }

    private void update()
    {
        /*
        System.err.println("Jobs: " + jobList.size());
        for(Job job : jobList)
        {
            System.err.println("jobs =" + job);
        }
        */
        
        try
        {
            Set<Cardinal> keys = world.mobs.keySet();

            for(Cardinal key : keys)
            {
                Mob mob = world.mobs.get(key.intValue());

                if(mob.getPath() != null)
                {
                    mob.advance(soundPlayer);

                    // Hajo: Testing - clean dust from floor
                    cleanDust(mob.gameMap, mob.location);
                    
                    if(key.intValue() == playerKey)
                    {
                        display.centerOn(mob);
                    }
                }

                mob.update();
            }
        } 
        catch(ConcurrentModificationException cmex)
        {
            // Hajo: this can happen while loading the map ...
            // Todo: find a way to handle this cleanly.
            logger.log(Level.INFO, cmex.getMessage());
        }
    }

    private void makeImps(Map gameMap)
    {
        spawnImp(gameMap, 20, 340);
        spawnImp(gameMap, 40, 350);
        spawnImp(gameMap, 40, 360);
    }

    private void convertMap(Map map)
    {
        logger.log(Level.INFO, "Converting map.");
        
        int w = map.getWidth();
        int h = map.getHeight();
        
        for(int y=0; y<h; y+=Map.SUB)
        {
            for(int x=0; x<w; x+=Map.SUB)
            {
                int ground = map.getFloor(x, y);
                if(ground >= Features.GROUND_IMPASSABLE && ground < Features.GROUND_IMPASSABLE+3)
                {
                    double select = Math.random();
                    /*
                    if(select > 0.995)
                    {
                        // map.setItem(x, y, Map.F_DECO + Features.I_TREASURE_BLOCK + (int)(Math.random() * 3));
                        map.setItem(x, y, Map.F_DECO + Features.I_GOLD_MOUND);
                    }
                    else 
                    */ 
                    if(select > 0.98)
                    {
                        map.setItem(x, y, Map.F_DECO + Features.I_COPPER_ORE_MOUND);
                    }
                    else if(select > 0.96)
                    {
                        map.setItem(x, y, Map.F_DECO + Features.I_TIN_ORE_MOUND);
                    }
                    else
                    {
                        map.setItem(x, y, Map.F_DECO + Features.I_STEEP_EARTH_BLOCK + (int)(Math.random() * 3));
                    }
                }
                if(y==0 || x==0 || x==w-1 || y==h-1)
                {
                    map.setItem(x, y, Map.F_DECO + Features.I_PERM_ROCK + (int)(Math.random() * 3));
                }
            }
        }
    }

    private void activateMap(Map map)
    {
        logger.log(Level.INFO, "Activating map.");
        
        farmland.clear();
        portals.clear();
        lairs.clear();
        forges.clear();
        claimed.clear();
        forgeRooms.clear();
        hospitals.clear();
        generators.clear();
        
        int w = map.getWidth();
        int h = map.getHeight();
        
        for(int y=0; y<h; y+=Map.SUB)
        {
            for(int x=0; x<w; x+=Map.SUB)
            {
                int ground = map.getFloor(x, y);
                if(ground >= Features.GROUND_GRASS_DARK && ground < Features.GROUND_GRASS_DARK + 3)
                {
                    addFarmlandSquare(x, y);
                    addClaimedSquare(x, y);
                }
                else if(ground >= Features.GROUND_LAIR && ground < Features.GROUND_LAIR + 3)
                {
                    addLairSquare(x, y);
                    addClaimedSquare(x, y);
                }
                else if(ground >= Features.GROUND_LIBRARY && ground < Features.GROUND_LIBRARY + 3)
                {
                    addLibrarySquare(x, y);
                    addClaimedSquare(x, y);
                }
                else if(ground >= Features.GROUND_FORGE && ground < Features.GROUND_FORGE + 3)
                {
                    addForgeSquare(map, x, y);
                    addClaimedSquare(x, y);
                }
                else if(ground >= Features.GROUND_TREASURY && ground < Features.GROUND_TREASURY + 3)
                {
                    addTreasurySquare(x, y);
                    addClaimedSquare(x, y);
                }
                else if(ground >= Features.GROUND_POLY_TILES && ground < Features.GROUND_POLY_TILES + 3)
                {
                    addClaimedSquare(x, y);
                }
                else if(ground >= Features.GROUND_HOSPITAL && ground < Features.GROUND_HOSPITAL + 3)
                {
                    addHospitalSquare(map, x, y);
                    addClaimedSquare(x, y);
                }
                else if(ground >= Features.GROUND_IMPASSABLE && ground < Features.GROUND_IMPASSABLE + 3)
                {
                    // Nothing to do ...
                }
                else
                {
                    logger.log(Level.SEVERE, "Unknown ground type {0} at {1}, {2}", new Object[]{ground, x, y});
                }
                
                for(int j=0; j<Map.SUB; j++)
                {
                    for(int i=0; i<Map.SUB; i++)
                    {
                        int item = map.getItem(x+i, y+j);
                        if(item == Map.F_DECO + Features.I_TUNNEL_PORTAL)
                        {
                            PortalSquare p = new PortalSquare(this, x, y, Clock.time() + 5000l + (long)(Math.random() * 1000));
                            portals.add(p);
                            logger.log(Level.INFO, "Adding portal at {0}, {1}", new Object[]{x, y});
                        }
                        
                        if(item == Map.F_DECO + Features.I_WELL)
                        {
                            Rectangle r = new Rectangle(x+i-1, y+j-1, 4, 4);
                            map.setAreaMovementBlocked(r, true);
                        }
                    }
                }
            }
        }
    }

    public void save() 
    {
        try
        {
            String folderName = "./savegame";
            boolean ok = true;
            File folder = new File(folderName);
            
            if(!folder.exists())
            {
                ok = folder.mkdirs();
            }
            
            if(!ok)
            {
                logger.log(Level.SEVERE, "Cannot create savegame folder!");
            }
            else
            {
                File file = new File(folderName, "test.map");
                player.gameMap.save(file);

                FileWriter writer = new FileWriter(folderName + "/test.mob");
                
                
                Registry<Mob> mobs = world.mobs;
                Set<Cardinal> keys = mobs.keySet();

                writer.write("mobs=" + keys.size() + "\n");
                
                for(Cardinal key : keys)
                {
                    Mob mob = mobs.get(key.intValue());
                    writer.write("species=" + mob.getSpecies() + "\n");
                    mob.write(writer);
                    
                    writer.write("AI data start\n");

                    Ai ai = mob.getAi();
                    if(ai==null)
                    {
                        writer.write("ai=<null>\n");
                    }
                    else
                    {
                        writer.write("ai=" + ai.getClass().getName() + "\n");
                        ai.write(writer);
                    }
                    writer.write("AI data end\n");
                }
                
                jobQueue.write(writer);
                Clock.write(writer);
                
                saveQuests(writer);
                
                writer.close();
                
                // gameDisplay.addMessage(new TimedMessage("Game saved!", 0xFFFFFF, 500, 400, Clock.time()));
                gameDisplay.addMessage(new TimedMessage("Game saved!", 0xFFFFFF, display.displayWidth/2, 300, Clock.time()));                
            }
        }
        catch(IOException ioex)
        {
            logger.log(Level.SEVERE, "Exception while saving the game", ioex);
        }
    }

    private void saveQuests(FileWriter writer) throws IOException
    {
        writer.write("Quest list start\n");
        writer.write("count=" + quests.size() + "\n");
        
        for(Quest quest : quests)
        {
            quest.write(writer);
        }
        
        writer.write("Quest list end\n");
    }

    public void load() 
    {
        try
        {
            // Hajo: signal the processor to stop looping
            processorLock = true;
            
            // Hajo: wait till processor has completed the current loop
            while(processorActive)
            {
                safeSleep(100);
            }
            
            logger.log(Level.INFO, "Loading saved game.");
            
            world.mobs.clear();
            
            File file = new File("./savegame/test.map");
            player.gameMap.load(file);
            Map map = player.gameMap;
            
            BufferedReader reader = new BufferedReader(new FileReader("./savegame/test.mob"));
            
            String line;
            
            line = reader.readLine();
                
            int mobCount = Integer.parseInt(line.substring(5));
            
            Registry<Mob> mobs = world.mobs;
            for(int i=0; i<mobCount; i++)
            {
                logger.log(Level.INFO, "loading mob " + (i+1) + " of " + mobCount);
                
                line = reader.readLine();
                int species = Integer.parseInt(line.substring(8));
                SpeciesDescription desc = Species.speciesTable.get(species);

                
                if(desc == null)
                {
                    logger.log(Level.INFO, "loading a generator " + line);

                    // Hajo: this is no real player, only a generator
                    // -> these are re-installed in activateMap, so
                    // we don't need to do anything here
                
                    do
                    {
                        line = reader.readLine();
                        
                    } while(!"AI data end".equals(line));

                }
                else
                {
                    logger.log(Level.INFO, "loading a " + desc.name);

                    Mob mob;
                    mob = new Mob(world, 0, 0, species, map, null, desc.speed, desc.move);
                    mob.read(reader, null);

                    line = reader.readLine();
                    if(!"AI data start".equals(line))
                    {
                        throw new IOException("Missing: AI data start for mob=" + mob.getKey());
                    }

                    Ai ai = null;
                    line = reader.readLine();
                    if("ai=<null>".equals(line))
                    {
                    }
                    else
                    {
                        if(line.contains("ImpAi"))
                        {
                            ai = new ImpAi(this);
                            ai.read(reader);
                        }
                        else if(line.contains("CreatureAi"))
                        {
                            ai = new CreatureAi(this);
                            ai.read(reader);
                        }
                        else
                        {
                            throw new IOException("Unknown AI type mob=" + mob.getKey() + " : " + line.substring(3));
                        }
                    }
                    mob.setAi(ai);

                    mobs.put(mob.getKey(), mob);

                    if(mob.getSpecies() == Species.GLOBOS_BASE)
                    {
                        // Player should be the only Globo in the game ...
                        player = mob;
                        playerKey = mob.getKey();
                        player.visuals.setDisplayCode(0);

                        display.centerOn(player);
                    }

                    line = reader.readLine();

                    if(!"AI data end".equals(line))
                    {
                        throw new IOException("Missing: AI data end for mob=" + mob.getKey());
                    }
                }
            }

            jobQueue.read(this, reader);
            Clock.read(reader);

            loadQuests(reader);
            
            reader.close();
            activateMap(map);
            
            logger.log(Level.INFO, "Game loaded.");
            gameDisplay.addMessage(new TimedMessage("Game loaded!", 0xFFFFFF, display.displayWidth/2, 300, Clock.time()));
        }
        catch(IOException ioex)
        {
            logger.log(Level.SEVERE, "Exception while loading a game", ioex);
        }
        finally
        {
            processorLock = false;
        }
    }
    
    private void loadQuests(BufferedReader reader) throws IOException
    {
        String line;
        
        line = reader.readLine();
        if("Quest list start".equals(line))
        {
            line = reader.readLine();
            int count = Integer.parseInt(line.substring(6));

            quests.clear();
            for(int i=0; i<count; i++)
            {
                Quest quest = new Quest();
                quest.load(reader, world.mobs);
            }

            line = reader.readLine();
            assert("Quest list end".equals(line));
        }
        else
        {
            logger.log(Level.WARNING, "Savegame has no quest data block, either old or buggy.");
        }
    }

    public void addFarmlandSquare(int rasterI, int rasterJ) 
    {
        FarmSquare p = new FarmSquare(rasterI, rasterJ, Clock.time() + (long)(Math.random() * 1000));
                
        if(!farmland.contains(p))
        {
            farmland.add(p);
        }                
        refreshPillars(rasterI, rasterJ);
    }
    
    public void addLairSquare(int rasterI, int rasterJ) 
    {
        Point p = new Point(rasterI, rasterJ);
        if(!lairs.contains(p))
        {
            lairs.add(p);
        }                
        refreshPillars(rasterI, rasterJ);
    }

    public void addTreasurySquare(int rasterI, int rasterJ) 
    {
        Point p = new Point(rasterI, rasterJ);
        if(!treasuries.contains(p))
        {
            treasuries.add(p);
        }                
        refreshPillars(rasterI, rasterJ);
    }

    public void addLibrarySquare(int rasterI, int rasterJ) 
    {
        Point p = new Point(rasterI, rasterJ);
        if(!libraries.contains(p))
        {
            // logger.log(Level.INFO, "Adding library square {0}, {1}", new Object[]{p.x, p.y});
            libraries.add(p);
        }                
        refreshPillars(rasterI, rasterJ);
    }
    
    public void addWorkshopSquare(final Map map, int rasterI, int rasterJ) 
    {
        Point p = new Point(rasterI, rasterJ);
        if(!workshops.contains(p))
        {
            workshops.add(p);
        }                
        refreshPillars(rasterI, rasterJ);
    }

    public void addForgeSquare(final Map map, int rasterI, int rasterJ) 
    {
        Point p = new Point(rasterI, rasterJ);
        if(!forges.contains(p))
        {
            // Hajo: see if this is a new room or if it is
            // an extension of a room

            int dmax = 999;
            Room bestRoom = null;
            for(Room room : forgeRooms)
            {
                for(Point rp : room.squares)
                {
                    int d = Math.abs(rp.x - p.x) + Math.abs(rp.y - p.y);
                    
                    if(d < dmax) 
                    {
                        dmax = d;
                        bestRoom = room;
                    }
                }
            }
            
            if(dmax > Map.SUB)
            {
                // Hajo: this is a new room
                Room room = new Room();
                room.squares.add(p);
                forgeRooms.add(room);
            }
            else if(bestRoom != null)
            {
                bestRoom.squares.add(p);
            }
            else
            {
                logger.log(Level.SEVERE, "Algorithm error!");
            }
            
            forges.add(p);
            
            // Hajo: todo: there should be real room furnishing code here ...
            
            int volx = p.x + Map.SUB/4;
            int voly = p.y + Map.SUB/4;
            map.setItem(volx, voly, Features.I_SMALL_VOLCANO);

            RectArea area = new RectArea(volx - 2, voly - 2, 3, 3);

            area.traverseWithoutCorners(new LocationCallback() 
            {
                @Override
                public boolean visit(int x, int y)
                {
                    map.setMovementBlocked(x, y, true);
                    return false;
                }
            });
            
            area = new RectArea(volx - 2, voly - 2, 5, 5);

            area.traverseWithoutCorners(new LocationCallback() 
            {
                @Override
                public boolean visit(int x, int y)
                {
                    map.setPlacementBlocked(x, y, true);
                    return false;
                }
            });

            addParticleGenerator(map, volx, voly, 21, MobStats.G_VOLCANO);
            
            // Place anvil and placement block anvil area.
            map.setItem(volx+3, voly+1, Features.I_ANVIL);
            area = new RectArea(volx + 2, voly - 1, 3, 4);

            area.traverseWithoutCorners(new LocationCallback() 
            {
                @Override
                public boolean visit(int x, int y)
                {
                    map.setPlacementBlocked(x, y, true);
                    return false;
                }
            });
        }                
        refreshPillars(rasterI, rasterJ);
    }
    
    public void addHospitalSquare(final Map map, int rasterI, int rasterJ) 
    {
        Point p = new Point(rasterI, rasterJ);
        if(!hospitals.contains(p))
        {
            cleanSquare(map, rasterI, rasterJ);
            
            hospitals.add(p);
            
            // map.setWayLikeItem(p.x, p.y, Features.I_HEALING_WELL_2 + (int)(Math.random()*2));
            map.setWayLikeItem(p.x, p.y, Features.I_HEALING_WELL_2);
            
            int volx = p.x + Map.SUB/2;
            int voly = p.y + Map.SUB/2;

            addParticleGenerator(map, volx, voly, 2, MobStats.G_HEALING_WELL);
            map.setAreaPlacementBlocked(new Rectangle(p.x, p.y, Map.SUB, Map.SUB), true);
            map.setMovementBlocked(volx, voly, true);
        }    
        else
        {
            // Hajo: allow player to change the well type randomly
            // map.setWayLikeItem(p.x, p.y, Features.I_HEALING_WELL_2 + (int)(Math.random()*2));
        }
        refreshPillars(rasterI, rasterJ);
    }
    
    public void addClaimedSquare(int rasterI, int rasterJ) 
    {
        Point p = new Point(rasterI, rasterJ);
        if(!claimed.contains(p))
        {
            claimed.add(p);
            refreshPillars(rasterI, rasterJ);
        }                
    }
    
    public List<FarmSquare> getFarmland()
    {
        return farmland;
    }

    public List<PortalSquare> getPortals() 
    {
        return portals;
    }
    
    public List<Point> getLairs()
    {
        return lairs;
    }

    public List<Point> getTreasuries() 
    {
        return treasuries;
    }
    
    public List<Point> getLibraries() 
    {
        return libraries;
    }

    public List<Point> getForges()
    {
        return forges;
    }

    public List<Point> getWorkshops()
    {
        return workshops;
    }

    public List<Point> getHospitals()
    {
        return hospitals;
    }

    public List<Point> getClaimedSquares()
    {
        return claimed;
    }
    
    void spawnImp() 
    {
        int x = display.cursorI;
        int y = display.cursorJ;
        spawnImp(player.gameMap, x, y);
    }

    public void spawnImp(Map gameMap, int x, int y) 
    {
        SpeciesDescription desc = Species.speciesTable.get(Species.IMPS_BASE);
        
        ImpAi impAi = new ImpAi(this);
        Mob imp = new Mob(world, x, y, Species.IMPS_BASE, gameMap, impAi, desc.speed, desc.move);
        int impKey = world.mobs.nextFreeKey();
        world.mobs.put(impKey, imp);
        imp.setKey(impKey);
        
        imp.stats.setCurrent(MobStats.GOLD, 0);
    }

    private void cleanDust(Map map, Point location)
    {
        for(int j=location.y-1; j<=location.y+1; j++)
        {
            for(int i=location.x-1; i<=location.x+1; i++)
            {
                if(Features.DUST_SET.contains(map.getItem(i, j)))
                {
                    map.setItem(i, j, 0);
                }
            }            
        }
    }

    private void cleanSquare(Map map, int rasterI, int rasterJ)
    {
        for(int x=0; x<Map.SUB; x++)
        {
            for(int y=0; y<Map.SUB; y++)
            {
                map.setItem(x, y, 0);
            }            
        }
    }
    
    public void refreshPillars(int rasterI, int rasterJ)
    {
        for(int x=-Map.SUB; x<=Map.SUB; x+=Map.SUB)
        {
            for(int y=-Map.SUB; y<=Map.SUB; y+=Map.SUB)
            {
                JobFrame job = new JobFrame(this, rasterI + x, rasterJ + y);
                if(job.isValid(player)) job.execute(player);
            }            
        }
    }
    
    public List<Point> getFarmlandLocations() 
    {
        List<Point> result = new ArrayList<Point>(farmland.size());
        for(FarmSquare farm : farmland)
        {
            result.add(new Point(farm.x, farm.y));
        }
        
        return result;
    }

    public void announceResearchResult(int breakthrough)
    {
        String text = "";
        if(breakthrough == KeeperStats.RESEARCH_FORGES)
        {
            text = "Your researchers found out how to build forges.\nRoom unlocked: Forge";
        }
        else if(breakthrough == KeeperStats.RESEARCH_WORKSHOPS)
        {
            text = "Your researchers found out how to build workshops.\nRoom unlocked: Workshop";
        }
        else if(breakthrough == KeeperStats.RESEARCH_HEALING)
        {
            text = "Your researchers discovered healing.\nRoom unlocked: Healing Well";
        }

        GenericMessage message =
                new GenericMessage(
                        this, gameDisplay, display,
                        600, 400,
                        "Discovery!",
                        text,
                        "[ Acknowledged ]", null);

        MessageHook hookedMessage =
                new MessageHook(Features.MESSAGE_TROPHY_QUEST,
                        message);

        gameDisplay.addHookedMessage(hookedMessage);
    }

    public void makeTreasureQuest()
    {
        Quest quest = QuestGenerator.makeTreasureQuest();
        QuestMessage questMessage = new QuestMessage(this, gameDisplay, display,
                                                     600, 400, quest,
                                                     "Discovery!",
                                                     "[ Send Party ]", "[ Leave It ]");

        MessageHook hookedMessage =
                new MessageHook(Features.MESSAGE_TROPHY_QUEST,
                                questMessage);

        gameDisplay.addHookedMessage(hookedMessage);
    }
    
    public void makeTechnologyQuest()
    {
        Quest quest = QuestGenerator.makeTechnologyQuest();
        QuestMessage questMessage = new QuestMessage(this, gameDisplay, display,
                                                     600, 400, quest,
                                                     "Humble Suggestion",
                                                     "[ Assemble Party ]", "[ Leave It ]");

        MessageHook hookedMessage =
                new MessageHook(Features.MESSAGE_RESEARCH_QUEST,
                                questMessage);


        gameDisplay.addHookedMessage(hookedMessage);
    }
    
    private void addParticleGenerator(Map map, int x, int y, int z, int type)
    {
        // Hajo: Hack: Generators must be mobs, due to display
        // restrictions. -> They have species 0 as marker!
        Mob generator = new Mob(world, x, y, 0, map, null, 0, new MovementJumping());
        generator.stats.setCurrent(MobStats.GENERATOR, type);
                
        int key = world.mobs.nextFreeKey();
        generator.setKey(key);
        generator.zOff = z << 16;
        world.mobs.put(key, generator);
        map.setMob(x, y, key);
        generators.add(generator);
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

    private void splash(Texture intro, String msg)
    {
        display.clear();
        IsoDisplay.drawTile(intro, 1200 - intro.image.getWidth(), 0);
        // display.font.drawString(nameVersion, 0xFFFFFF, 220, 440);
        // display.font.drawStringScaled(msg, 0xDDDDDD, 220, 400, 0.8);
        gameDisplay.getFontLow().drawStringScaled(nameVersion, 0xFFDDAA, 210, 445, 0.5);
        gameDisplay.getFontLow().drawStringScaled(msg, 0xDDDDDD, 210, 396, 0.3);
        display.update();
    }

    public void removeGeneratorFrom(int i, int j) 
    {
        ArrayList<Mob> killList = new ArrayList<Mob>();
        
        for(Mob generator : generators)
        {
            if(generator.location.x == i && generator.location.y == j)
            {
                killList.add(generator);
            }
        }
        
        for(Mob generator : killList)
        {
            generators.remove(generator);
            world.mobs.remove(generator.getKey());
        }
    }

    private class MyClockListener implements Clock.ClockListener
    {

        @Override
        public void newDay(int days)
        {
        }

        @Override
        public void newHour(int hours)
        {
            Mob keeper = world.mobs.get(getPlayerKey());
            int reputation = keeper.stats.getCurrent(KeeperStats.REPUTATION);
            
            reputation = reputation - reputation / 16;
            keeper.stats.setCurrent(KeeperStats.REPUTATION, reputation);
        }
    }
}
