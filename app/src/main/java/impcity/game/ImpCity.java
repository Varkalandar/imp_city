package impcity.game;

import impcity.game.quests.QuestResult;
import impcity.game.room.Furnisher;
import impcity.game.room.Room;
import impcity.game.room.RoomList;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import impcity.ogl.GlTextureCache;
import impcity.game.mobs.Mob;
import impcity.game.ai.Ai;
import impcity.game.map.Map;
import impcity.game.map.RectArea;
import impcity.game.mobs.MovementJumping;
import impcity.oal.SoundPlayer;
import impcity.ogl.IsoDisplay;
import impcity.ogl.Light;
import impcity.ui.PostRenderHook;
import impcity.ui.TimedMessage;

import java.lang.reflect.Field;
import java.util.Arrays;

import impcity.utils.StringUtils;

import java.util.function.IntUnaryOperator;
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
    private static final String NAME_VERSION = "Imp City " + Version.VERSION;
    
    private static final Logger LOG = Logger.getLogger(ImpCity.class.getName());
        
    private final TextureCache textureCache;
    private final IsoDisplay display;
    public final SoundPlayer soundPlayer;
    public final Research research;
    
    public int mouseI, mouseJ;

    int playerKey;
    
    public final World world;
    private Mob player;
    
    public final JobQueue jobQueue = new JobQueue();
    private final GameDisplay gameDisplay;
    
    private final List <FarmSquare> farmland = Collections.synchronizedList(new ArrayList<>());
    private final List <PortalSquare> portals = Collections.synchronizedList(new ArrayList<>());
    private final List <Point> lairs = Collections.synchronizedList(new ArrayList<>());
    private final List <Point> treasuries = Collections.synchronizedList(new ArrayList<>());
    private final List <Point> libraries = Collections.synchronizedList(new ArrayList<>());
    private final List <Point> forges = Collections.synchronizedList(new ArrayList<>());
    private final List <Point> labs = Collections.synchronizedList(new ArrayList<>());
    private final List <Point> hospitals = Collections.synchronizedList(new ArrayList<>());
    private final List <Point> claimed = Collections.synchronizedList(new ArrayList<>());
    
    public final RoomList forgeRooms = new RoomList();
    public final RoomList libraryRooms = new RoomList();
    public final RoomList labRooms = new RoomList();
    
    public final List <Mob> generators = Collections.synchronizedList(new ArrayList<>());
    public final List <Quest> quests = Collections.synchronizedList(new ArrayList<>());


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
            LOG.log(Level.SEVERE, "Failure in static init", ex);
        }
    }

    
    public static void addLibraryPath(String pathToAdd) throws Exception 
    {
        LOG.log(Level.INFO, "Adding library path: " + pathToAdd);

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
        display = new IsoDisplay(world.mobs, world.items, textureCache);
        display.create();        
        
        gameDisplay = new GameDisplay(this, display);
        
        soundPlayer = new SoundPlayer();
        
        research = new Research(gameDisplay);
    }
    
    
    public void initialize() throws IOException
    {
        final Texture intro = textureCache.loadTexture("/ui/dance_of_rebirth_by_shiroikuro.jpg", false);
        
        textureCache.initialize((String msg) -> { splash(intro, msg); });
        
        splash(intro, "Preparing map ...");
        loadMap("/big_60.map");

        LOG.log(Level.INFO, "Map loaded. Size={0}x{1}", new Object[]{player.gameMap.getWidth(), player.gameMap.getHeight()});
        
        display.map = player.gameMap;
        
        // display.map.recalculateBlockedAreas();
        display.centerOn(player);
        display.setTitle(NAME_VERSION);
        
        soundPlayer.init();
        
        String PATH = "/sfx/";

        String [] sampleFiles = new String []
        {
            PATH + "click.wav",
            PATH + "wosh.wav",
            PATH + "new_plant.wav",
            PATH + "farmland.wav",
            PATH + "magic_library.wav",
            PATH + "deselect.wav",
            PATH + "arrival_new.wav",
            PATH + "hjm-coindrop_v2.wav",
            PATH + "magic_lair.wav",
            PATH + "magic_workshop.wav",
            PATH + "381547__tumbleweed3288__falling-and-rolling-stones_excerpt.wav",
            PATH + "25060__wim__roofhammering01_excerpt.wav",
            PATH + "20797__acclivity__fly_excerpt.wav",
            PATH + "445974__breviceps__cartoon-slurp_excerpt.wav",
            PATH + "273722__thearxx08__angle-grinder.wav",
            PATH + "170957__timgormly__metal-ping.wav",
            PATH + "428953__jbp__crunching-on-a-snack-chomping.wav",
            PATH + "hjm-coin_clicker_3.wav",
        };

        splash(intro, "Loading sounds ...");
        if(!soundPlayer.loadSamples(sampleFiles))
        {
            LOG.log(Level.SEVERE, "Error while loading sound data.");
        }        
        
        // register some named features
        display.setDecoDisplayName(Features.I_TUNNEL_PORTAL, "Tunnel Portal");
        display.setDecoDisplayName(Features.I_MINERAL_BLOCK, "Raw Cobaltite Deposit");
        display.setDecoDisplayName(Features.I_TIN_ORE_MOUND, "Tin Ore");
        display.setDecoDisplayName(Features.I_COPPER_ORE_MOUND, "Copper Ore");
        
        // gameDisplay.showDialog(new CreatureOverview(this, gameDisplay, display));
    
        research.initialize(player.stats);
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

        UiDialog.setSoundPlayer(soundPlayer);

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
    

    public int countMobs(int type)
    {
        int count = 0;
        int max = world.mobs.nextFreeKey();
        for(int i=0; i<max; i++)
        {
            Mob mob = world.mobs.get(i);
            if(mob != null && mob.getSpecies() == type) count++;
        }

        return count;
    }


    public int calcMaxCreatureCount()
    {
        // Todo: better calculation needed? Room types?
        return lairs.size() / 2 + claimed.size() / 4;
    }
    

    public int calcCurrentCreatureCount()
    {
        int count = world.mobs.keySet().size();

        // special case, that one hidden, invisible globo
        count --;

        // special case, the generators also should not count as creatures
        count -= generators.size();

        return count;
    }
    

    @Override
    public void displayMore() 
    {
        gameDisplay.displayMore();
        displayMoreUpdate();
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
        catch(IOException ex)
        {
            LOG.log(Level.SEVERE, mapName, ex);
        }

        player = new Mob(30, 350, Species.GLOBOS_BASE, 0, 0, gameMap, null, 45, new MovementJumping());
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
            LOG.log(Level.SEVERE, ex.toString(), ex);
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
                    LOG.log(Level.SEVERE, ex.toString(), ex);
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
    

    private void displayMoreUpdate()
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

                mob.displayMoreUpdate();
            }
        } 
        catch(ConcurrentModificationException cmex)
        {
            // Hajo: this can happen while loading the map ...
            // Todo: find a way to handle this cleanly.
            LOG.log(Level.INFO, cmex.getMessage());
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
        LOG.log(Level.INFO, "Converting map.");
        
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

                    if(select > 0.98)
                    {
                        map.setItem(x, y, Features.I_COPPER_ORE_MOUND);
                    }
                    else if(select > 0.97)
                    {
                        map.setItem(x, y, Features.I_TIN_ORE_MOUND);
                    }
                    else if(select > 0.96)
                    {
                        map.setItem(x, y, Features.I_MINERAL_BLOCK);
                    }
                    else if(select > 0.95)
                    {
                        map.setItem(x, y, Features.I_GRAPHITE_BLOCK);
                    }
                    else if(select > 0.94)
                    {
                        map.setItem(x, y, Features.I_IRON_ORE_BLOCK);
                    }
                    else
                    {
                        map.setItem(x, y, Features.I_STEEP_EARTH_BLOCK + (int)(Math.random() * 3));
                    }
                }
                if(y==0 || x==0 || x>=w-Map.SUB || y>=h-Map.SUB)
                {
                    map.setItem(x, y, Features.I_PERM_ROCK + (int)(Math.random() * 3));
                }

                // old maps had deco flags that now mess with the item flags
                for(int yy = 0; yy<Map.SUB; yy++)
                {
                    for(int xx= 0; xx<Map.SUB; xx++)
                    {
                        int n = map.getItem(x+xx, y+yy) & 0xFF00FFFF;
                        map.setItem(x+xx, y+yy, n);
                    }
                }
            }
        }
    }
    

    private void activateMap(Map map)
    {
        LOG.log(Level.INFO, "Activating map.");
        
        farmland.clear();
        portals.clear();
        lairs.clear();
        libraries.clear();
        forges.clear();
        claimed.clear();
        hospitals.clear();

        forgeRooms.clear();
        libraryRooms.clear();
        labRooms.clear();

        generators.clear();
        
        int w = map.getWidth();
        int h = map.getHeight();
        
        for(int y=0; y<h; y+=Map.SUB)
        {
            for(int x=0; x<w; x+=Map.SUB)
            {
                // initialize with invisible. Claimed squares will light up
                map.setColor(x, y, 0);
            }
        }
        
        for(int y=0; y<h; y+=Map.SUB)
        {
            for(int x=0; x<w; x+=Map.SUB)
            {
                int ground = map.getFloor(x, y);
                if(ground >= Features.GROUND_GRASS_DARK && ground < Features.GROUND_GRASS_DARK + 3)
                {
                    addFarmlandSquare(map, x, y);
                    addClaimedSquare(map, x, y);
                }
                else if(ground >= Features.GROUND_LAIR && ground < Features.GROUND_LAIR + 3)
                {
                    addLairSquare(map, x, y);
                    addClaimedSquare(map, x, y);
                }
                else if(ground >= Features.GROUND_LIBRARY && ground < Features.GROUND_LIBRARY + 3)
                {
                    addLibrarySquare(map, x, y);
                    addClaimedSquare(map, x, y);
                }
                else if(ground >= Features.GROUND_FORGE && ground < Features.GROUND_FORGE + 3)
                {
                    addForgeSquare(map, x, y);
                    addClaimedSquare(map, x, y);
                }
                else if(ground >= Features.GROUND_LABORATORY && ground < Features.GROUND_LABORATORY + 3)
                {
                    addLabSquare(map, x, y);
                    addClaimedSquare(map, x, y);
                }
                else if(ground >= Features.GROUND_TREASURY && ground < Features.GROUND_TREASURY + 3)
                {
                    addTreasurySquare(map, x, y);
                    addClaimedSquare(map, x, y);
                }
                else if(ground >= Features.GROUND_POLY_TILES && ground < Features.GROUND_POLY_TILES + 3)
                {
                    addClaimedSquare(map, x, y);
                }
                else if(ground >= Features.GROUND_HOSPITAL && ground < Features.GROUND_HOSPITAL + 3)
                {
                    addHospitalSquare(map, x, y);
                    addClaimedSquare(map, x, y);
                }
                else if(ground >= Features.GROUND_IMPASSABLE && ground < Features.GROUND_IMPASSABLE + 3)
                {
                    // Nothing to do ...
                }
                else if(ground >= Features.GROUND_QUAD_TILES && ground < Features.GROUND_QUAD_TILES + 3)
                {
                    addClaimedSquare(map, x, y);
                }
                else
                {
                    LOG.log(Level.SEVERE, "Unknown ground type {0} at {1}, {2}", new Object[]{ground, x, y});
                }

                for(int j=0; j<Map.SUB; j++)
                {
                    for(int i=0; i<Map.SUB; i++)
                    {
                        int item = map.getItem(x+i, y+j);
                        if(item == Features.I_TUNNEL_PORTAL)
                        {
                            PortalSquare p = new PortalSquare(this, x, y, Clock.time() + 5000l + (long)(Math.random() * 1000));
                            portals.add(p);
                            LOG.log(Level.INFO, "Adding portal at {0}, {1}", new Object[]{x, y});
                        }
                        
                        if(item == Features.I_WELL)
                        {
                            Rectangle r = new Rectangle(x+i-2, y+j-2, 4, 4);
                            map.setAreaMovementBlocked(r, true);
                        }
                    }
                }
            }
        }

        // old maps hat wall blocks at sub (0, 0) which have to be moved to
        // (Map.SUB/2-1, Map.SUB/2-1) to avoid clipping errors in the display
        for(int y = h; y >= 0; y -= Map.SUB)
        {
            for (int x = w; x >= 0; x -= Map.SUB)
            {
                int block = map.getItem(x, y) & Map.F_IDENT_MASK;
                if (block >= Features.I_PERM_ROCK && block <= Features.I_STEEP_EARTH_BLOCK + 20)
                {
                    map.setItem(x, y, 0);
                    map.setItem(x + Map.O_BLOCK, y + Map.O_BLOCK, block);
                }
            }
        }

        placeEnclosure(map, 12, Features.GROUND_IMPASSABLE, Features.I_PERM_ROCK);
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
                LOG.log(Level.SEVERE, "Cannot create savegame folder!");
            }
            else
            {
                File file = new File(folderName, "test.map");
                player.gameMap.save(file);

                FileWriter writer = new FileWriter(folderName + "/test.mob");

                saveMobs(writer);
                saveItems(writer);

                jobQueue.write(writer);
                Clock.write(writer);
                
                saveQuests(writer);
                
                writer.close();

                gameDisplay.addMessage(new TimedMessage("Game saved!", 0xFFFFFFFF, display.displayWidth/2, 300, Clock.time()));
            }
        }
        catch(IOException ioex)
        {
            LOG.log(Level.SEVERE, "Exception while saving the game", ioex);
        }
    }
    

    private void saveMobs(FileWriter writer) throws IOException
    {
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
    }
    

    private void saveItems(FileWriter writer) throws IOException
    {
        Registry<Item> items = world.items;
        Set<Cardinal> keys = items.keySet();

        writer.write("items=" + keys.size() + "\n");

        for(Cardinal key : keys)
        {
            int n = key.intValue();
            writer.write("key=" + n + "\n");
            Item item = items.get(n);
            item.write(writer);
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
            synchronized(world)
            {
                LOG.log(Level.INFO, "Loading saved game.");

                world.mobs.clear();

                File file = new File("./savegame/test.map");
                player.gameMap.load(file);
                Map map = player.gameMap;

                BufferedReader reader = new BufferedReader(new FileReader("./savegame/test.mob"));

                String line;

                line = reader.readLine();
                int mobCount = Integer.parseInt(line.substring(5));
                loadMobs(map, reader, mobCount);

                line = reader.readLine();
                int itemCount = Integer.parseInt(line.substring(6));
                loadItems(map, reader, itemCount);

                jobQueue.read(this, reader);
                Clock.read(reader);

                loadQuests(reader);

                reader.close();
                activateMap(map);

                LOG.log(Level.INFO, "Game loaded.");
                gameDisplay.addMessage(new TimedMessage("Game loaded!", 0xFFFFFFFF, display.displayWidth/2, 300, Clock.time()));
            }
        }
        catch(IOException ioex)
        {
            LOG.log(Level.SEVERE, "Exception while loading a game", ioex);
        }
    }

    
    private void loadMobs(Map map, BufferedReader reader, int count) throws IOException
    {
        String line;
        Registry<Mob> mobs = world.mobs;
        for(int i = 0; i< count; i++)
        {
            LOG.log(Level.INFO, "loading mob " + (i+1) + " of " + count);

            line = reader.readLine();
            int species = Integer.parseInt(line.substring(8));
            SpeciesDescription desc = Species.speciesTable.get(species);


            if(desc == null)
            {
                LOG.log(Level.INFO, "loading a generator " + line);

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
                LOG.log(Level.INFO, "loading a " + desc.name);

                Mob mob;
                mob = new Mob(0, 0, species, Features.SHADOW_BASE, desc.sleepImage, map, null, desc.speed, desc.move);
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
                    // on expedition
                    mob.visuals.setBubble(0);
                    mob.visuals.setDisplayCode(Features.I_EXPEDITION_BANNER);
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
    }


    private void loadItems(Map map, BufferedReader reader, int count) throws IOException
    {
        String line;
        Registry<Item> items = world.items;
        for (int i = 0; i < count; i++)
        {
            LOG.log(Level.INFO, "loading item " + (i + 1) + " of " + count);
            line = reader.readLine();
            int key = Integer.parseInt(line.substring(4));
            LOG.log(Level.INFO, "Item key is " + key);

            Item item = new Item(reader);
            items.put(key, item);
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
                quests.add(quest);
            }

            line = reader.readLine();
            assert("Quest list end".equals(line));
        }
        else
        {
            LOG.log(Level.WARNING, "Savegame has no quest data block, either old or buggy.");
        }
    }
    

    public void addFarmlandSquare(Map map, int rasterI, int rasterJ) 
    {
        FarmSquare p = new FarmSquare(rasterI, rasterJ, Clock.time() + (long)(Math.random() * 1000));
                
        if(!farmland.contains(p))
        {
            map.setFloor(rasterI, rasterJ, Features.GROUND_GRASS_DARK + (int)(Math.random() * 2));
            farmland.add(p);
        }                
        refreshPillars(rasterI, rasterJ);
    }
    
    
    public void addLairSquare(Map map, int rasterI, int rasterJ) 
    {
        Point p = new Point(rasterI, rasterJ);
        if(!lairs.contains(p))
        {
            map.setFloor(rasterI, rasterJ, Features.GROUND_LAIR + (int)(Math.random() * 3));
            lairs.add(p);
        }                
        refreshPillars(rasterI, rasterJ);
    }
    

    public void addTreasurySquare(Map map, int rasterI, int rasterJ) 
    {
        Point p = new Point(rasterI, rasterJ);
        if(!treasuries.contains(p))
        {
            map.setFloor(rasterI, rasterJ, Features.GROUND_TREASURY + (int)(Math.random() * 2));
            treasuries.add(p);
        }                
        refreshPillars(rasterI, rasterJ);
    }


    public void addLibrarySquare(Map map, int rasterI, int rasterJ)
    {
        addRoomSquare(map, rasterI, rasterJ,
                libraries, libraryRooms,
                Features.GROUND_LIBRARY, 1,
                (m, x, y) -> {furnishLibrary(m, x, y);});
    }


    public void addForgeSquare(final Map map, int rasterI, int rasterJ)
    {
        addRoomSquare(map, rasterI, rasterJ,
                forges, forgeRooms,
                Features.GROUND_FORGE, 3,
                (m, x, y) -> {furnishForge(m, x, y);});
    }


    public void addLabSquare(Map map, int rasterI, int rasterJ)
    {
        addRoomSquare(map, rasterI, rasterJ,
                labs, labRooms,
                Features.GROUND_LABORATORY, 3,
                (m, x, y) -> {furnishLab(m, x, y);});
    }


    private void addRoomSquare(Map map, int rasterI, int rasterJ,
                                    List<Point> squares, RoomList rooms,
                                    int floor, int floorRange,
                                    Furnisher action)
    {
        Point p = new Point(rasterI, rasterJ);
        Room room;

        // new room square was added
        squares.add(p);
        map.setFloor(p.x, p.y, floor + (int)(Math.random() * floorRange));
        room = rooms.addNewSquare(p);
        LOG.log(Level.INFO, "Room list contains " + rooms.size() + " rooms");
        
        room.refurnish(this, map, floor, action);
    }


    public void removeLibrarySquare(Map map, int rasterI, int rasterJ)
    {
        Point p = new Point(rasterI, rasterJ);
        libraryRooms.removeSquareAndRebuild(this,
                                            map,
                                            p,
                                            libraries,
                                            Features.GROUND_LIBRARY,
                                            (m, x, y) -> {furnishLibrary(m, x, y);});
    }


    public void removeForgeSquare(final Map map, int rasterI, int rasterJ)
    {
        Point p = new Point(rasterI, rasterJ);
        forgeRooms.removeSquareAndRebuild(this,
                map,
                p,
                forges,
                Features.GROUND_FORGE,
                (m, x, y) -> {furnishForge(m, x, y);});
    }


    public void removeLabSquare(Map map, int rasterI, int rasterJ)
    {
        Point p = new Point(rasterI, rasterJ);
        labRooms.removeSquareAndRebuild(this,
                map,
                p,
                labs,
                Features.GROUND_LABORATORY,
                (m, x, y) -> {furnishLab(m, x, y);});
    }


    private void furnishLibrary(Map map, int x, int y)
    {
        x -= Map.SUB/2;
        y -= Map.SUB/2;

        map.setItem(x, y + 4, Features.I_BOOKSHELF_HALF_RIGHT);

        Rectangle r = new Rectangle(x, y + 3, 5, 1);
        map.setAreaMovementBlocked(r, true);

        map.setItem(x + 6, y + 2, Features.I_TORCH_STAND);
        Light light = new Light(x + 6, y + 2, 48, 3, 0xFFFFAA55, 0.25);
        map.lights.add(light);
    }


    private void furnishLab(Map map, int x, int y)
    {
        map.setItem(x, y, Features.I_LAB_TABLE);
        
        addParticleGenerator(map, x, y, 4, MobStats.G_DISTILL);
        
        Light light = new Light(x, y, 30, 2, 0xFF556677, 0.7);
        map.lights.add(light);
        
        // lab equipment is not walkable
        RectArea area = new RectArea(x - 1, y - 1, 3, 3);

        area.traverseWithoutCorners((int px, int py) -> {
            map.setMovementBlocked(px, py, true);
            map.setPlacementBlocked(px, py, true);
            return false;
        });
    }


    private void furnishForge(Map map, int x, int y)
    {
        x -= Map.SUB/2;
        y -= Map.SUB/2;

        int volx = x + Map.SUB/4;
        int voly = y + Map.SUB/4;
        map.setItem(volx, voly, Features.I_SMALL_VOLCANO);

        Light light = new Light(volx, voly, 30, 3, 0xFF302010, 0.5);
        map.lights.add(light);

        RectArea area = new RectArea(volx - 2, voly - 2, 3, 3);

        area.traverseWithoutCorners((int px, int py) -> {
            map.setMovementBlocked(px, py, true);
            return false;
        });

        area = new RectArea(volx - 2, voly - 2, 5, 5);

        area.traverseWithoutCorners((int px, int py) -> {
            map.setPlacementBlocked(px, py, true);
            return false;
        });

        addParticleGenerator(map, volx, voly, 21, MobStats.G_VOLCANO);

        // Place anvil and placement block anvil area.
        map.setItem(volx+3, voly+1, Features.I_ANVIL);
        area = new RectArea(volx + 2, voly - 1, 3, 4);

        area.traverseWithoutCorners((int px, int py) -> {
            map.setPlacementBlocked(px, py, true);
            return false;
        });        
    }

    
    public void addHospitalSquare(final Map map, int rasterI, int rasterJ) 
    {
        Point p = new Point(rasterI, rasterJ);
        if(!hospitals.contains(p))
        {
            resetSquare(map, p.x, p.y);
            map.setFloor(rasterI, rasterJ, Features.GROUND_HOSPITAL + (int)(Math.random() * 1));
            
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
    
    
    public void addClaimedSquare(Map map, int rasterI, int rasterJ) 
    {
        Point p = new Point(rasterI, rasterJ);
        
        LOG.log(Level.INFO, "Claiming square " + rasterI + ", " + rasterJ);
        
        if(!claimed.contains(p))
        {
            claimed.add(p);
            refreshPillars(rasterI, rasterJ);
            revealArea(map, p);
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

    public List<Point> getLaboratories()
    {
        return labs;
    }

    public List<Point> getHospitals()
    {
        return hospitals;
    }
    

    public List<Point> getClaimedSquares()
    {
        return claimed;
    }    

    
    public void spawnImp(Map gameMap, int x, int y) 
    {
        SpeciesDescription desc = Species.speciesTable.get(Species.IMPS_BASE);
        
        ImpAi impAi = new ImpAi(this);
        Mob imp = new Mob(x, y, Species.IMPS_BASE, Features.SHADOW_BASE, desc.sleepImage, gameMap, impAi, desc.speed, desc.move);
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

    
    public void resetSquare(Map map, int rasterI, int rasterJ) 
    {
        map.setWayLikeItem(rasterI, rasterJ, 0);
        clearItems(map, rasterI, rasterJ, Map.SUB, Features.keepTreasureFilter);
        
        int n = map.getFloor(rasterI, rasterJ);
        if(n < Features.GROUND_POLY_TILES || n >= Features.GROUND_POLY_TILES + 3)
        {
            map.setFloor(rasterI, rasterJ, Features.GROUND_POLY_TILES + (int)(Math.random() * 3));            
        }
    }

    
    public void clearItems(Map map, int x, int y, int range, IntUnaryOperator itemFilter)
    {
        map.traverseArea(x, y, range, range, 
            (ii, jj) ->
            {        
                map.setItem(ii, jj, itemFilter.applyAsInt(map.getItem(ii, jj)));
                map.setMovementBlocked(ii, jj, false);
                map.setPlacementBlocked(ii, jj, false);
                map.removeLight(ii, jj);

                removeGeneratorFrom(ii, jj);
            });
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
        List<Point> result = new ArrayList<>(farmland.size());
        for(FarmSquare farm : farmland)
        {
            result.add(new Point(farm.x, farm.y));
        }
        
        return result;
    }

    /**
     * The player starts with a restricted area. This method is used to set these walls
     * and later to remover them again
     */
    public void placeEnclosure(Map map, int distance, int ground, int block)
    {
        PortalSquare ps = getPortals().get(0);
        Point p = ps.getLocation();

        System.err.println(p);

        int x = p.x / Map.SUB;
        int y = p.y / Map.SUB;


        // left and right wall (relative to portal)
        for(int i = 0; i < distance*2; i++)
        {
            int rasterI = (x + i) * Map.SUB;
            int rasterJ = y * Map.SUB;

            map.setFloor(rasterI, rasterJ - distance*Map.SUB, ground);
            map.setFloor(rasterI, rasterJ + distance*Map.SUB, ground);

            map.setItem(rasterI + Map.O_BLOCK, rasterJ - distance*Map.SUB + Map.O_BLOCK, block + (int)(Math.random() * 3.0));
            map.setItem(rasterI + Map.O_BLOCK, rasterJ + distance*Map.SUB + Map.O_BLOCK, block + (int)(Math.random() * 3.0));
        }

        // bottom wall (opposite to portal)
        for(int j = 1; j < distance * 2; j++)
        {
            int rasterI = (x + distance * 2 - 1) * Map.SUB;
            int rasterJ = (y + j - distance) * Map.SUB;

            map.setFloor(rasterI, rasterJ, ground);
            map.setItem(rasterI + Map.O_BLOCK, rasterJ + Map.O_BLOCK, block + (int)(Math.random() * 3.0));
        }
    }


    public void makeTreasureQuest()
    {
        // debug quest book, add some extra quests
        /*
        for(int i=0; i<20; i++)
        {
            Quest quest = QuestGenerator.makeTreasureQuest();
            quests.add(quest);
        }
        */
        
        Quest quest = QuestGenerator.makeTreasureQuest();
        QuestMessage questMessage = new QuestMessage(this, gameDisplay, display,
                                                     600, 400, quest,
                                                     "Discovery!",
                                                     "[ Accept ]", "[ Dismiss ]");

        MessageHook hookedMessage =
                new MessageHook(Features.MESSAGE_TROPHY_QUEST,
                                questMessage);

        gameDisplay.addHookedMessage(hookedMessage);
    }


    public void makeArtifactQuest()
    {
        Quest quest = QuestGenerator.makeArtifactQuest();
        QuestMessage questMessage = new QuestMessage(this, gameDisplay, display,
                                                     600, 400, quest,
                                                     "Artifact Discovered!",
                                                     "[ Accept ]", "[ Dismiss ]");

        MessageHook hookedMessage =
                new MessageHook(Features.MESSAGE_ARTIFACT_QUEST,
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
                new MessageHook(Features.MESSAGE_IDEA_RED,
                                questMessage);


        gameDisplay.addHookedMessage(hookedMessage);
    }
    
    private void addParticleGenerator(Map map, int x, int y, int z, int type)
    {
        // Hajo: Hack: Generators must be mobs, due to display
        // restrictions. -> They have species 0 as marker!
        Mob generator = new Mob(x, y, 0, 0, 0, map, null, 0, new MovementJumping());
        generator.stats.setCurrent(MobStats.GENERATOR, type);
                
        int key = world.mobs.nextFreeKey();
        generator.setKey(key);
        generator.zOff = z << 16;
        world.mobs.put(key, generator);
        map.setMob(x, y, key);
        generators.add(generator);
    }


    private void splash(Texture intro, String msg)
    {
        display.clear();
        IsoDisplay.drawTile(intro, 1200 - intro.image.getWidth(), 0);
        // display.font.drawString(nameVersion, 0xFFFFFF, 220, 440);
        // display.font.drawStringScaled(msg, 0xDDDDDD, 220, 400, 0.8);
        gameDisplay.getFontLow().drawStringScaled(NAME_VERSION, 0xFFFFDDAA, 210, 445, 0.5);
        gameDisplay.getFontLow().drawStringScaled(msg, 0xFFDDDDDD, 210, 396, 0.3);
        display.update();
    }


    public void removeGeneratorFrom(int i, int j) 
    {
        ArrayList<Mob> killList = new ArrayList<>();
        
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

    public void reactivateReturningCreatures(Quest quest)
    {
        Mob keeper = world.mobs.get(getPlayerKey());
        int count = 0;
        
        for(int key : quest.party.members)
        {
            Mob mob = world.mobs.get(key);

            // Hajo: make creature look south-east
            int species = mob.getSpecies();
            mob.visuals.setDisplayCode(species+3);

            if (mob.getAi() != null)
            {
                LOG.log(Level.SEVERE, "AI must be null");
            }

            LOG.log(Level.INFO, "Setting returned creature "  + key + " home to " + mob.location.x + ", " + mob.location.y);

            CreatureAi ai = new CreatureAi(this);
            ai.setHome(mob.location);
            mob.setAi(ai);

            if (!ai.isLair(mob, mob.location.x, mob.location.y))
            {
                LOG.log(Level.SEVERE, "Mob location must be their lair.");
            }

            // give them some experience
            mob.addExperience(200);

            // now line them up 
            Point p = keeper.location;
            
            mob.location.x = p.x;
            mob.location.y = p.y + count * 2 - quest.party.members.size();

            LOG.log(Level.INFO, "Setting returned creature "  + key + " location to " + mob.location.x + ", " + mob.location.y);
            
            count ++;
        }            
    }


    public int createItem(String name, int texId, int type)
    {
        Item item = new Item(StringUtils.upperCaseFirst(name), texId, type);

        int key = world.items.nextFreeKey();
        world.items.put(key, item);

        return key | Map.F_ITEM;
    }


    public void storePartyTreasures(QuestResult questResult)
    {
        if(questResult.success)
        {
            int count = questResult.quest.party.carry;
            if(questResult.quest.treasureSize <= count ||
                    questResult.quest.treasureType == Quest.TT_ARTIFACT)
            {
                questResult.quest.status |= Quest.SF_PLUNDERED;
                count = questResult.quest.treasureSize;
            }
            
            // create treasures.

            Mob keeper = world.mobs.get(getPlayerKey());
            Map map = keeper.gameMap;
            Point location = keeper.location;

            int item;

            switch (questResult.quest.treasureType)
            {
                case Quest.TT_SILVER:
                    item = Features.I_SILVER_COINS;
                    break;
                case Quest.TT_GOLD:
                    item = Features.I_GOLD_COINS;
                    break;
                case Quest.TT_ARTIFACT:
                    item = createArtifactForTreasure(questResult.quest.treasureName);
                    handleArtifactReward(map);
                    count = 1;
                    break;
                default:
                    item = Features.I_GOLD_COINS;
            }

            for(int i = 0; i < count; i++)
            {
                map.dropItem(location.x, location.y, item, (x, y) -> {});
            }
        }
    }
    

    private void handleArtifactReward(Map map)
    {
        int count = 0;

        for(Cardinal key : world.items.keySet())
        {
            Item item = world.items.get(key.intValue());
            if(item.type == Item.ARTIFACT_T1)
            {
                count ++;
            }
        }

        // let's hope the count never shrinks and this method is only called
        // after an increase in count ...

        if(count == 4)
        {
            placeEnclosure(map, 12, Features.GROUND_IMPASSABLE, Features.I_STEEP_EARTH_BLOCK);

            GenericMessage message =
                    new GenericMessage(
                            gameDisplay,
                            600, 400,
                            "Breakthrough!",
                            "With the power of four collected artifacts, you could lift the" +
                                    " enclosure around your dungeon and now can expand into" +
                                    " new territories.",
                            "[ Acknowledged ]", null);

            MessageHook hookedMessage =
                    new MessageHook(Features.MESSAGE_IDEA_GREEN,
                            message);

            gameDisplay.addHookedMessage(hookedMessage);

        }
    }


    public int createArtifactForTreasure(String treasureName)
    {
        int img;
        
        if(treasureName.contains("dried frog"))
        {
            img = Features.ARTIFACT_DRIED_FROG;
        }
        else if(treasureName.contains("carved pumpkin"))
        {
            img = Features.ARTIFACT_CARVED_PUMPKIN;
        }
        else if(treasureName.contains("mummified cat"))
        {
            img = Features.ARTIFACT_CAT_MUMMY;
        }
        else if(treasureName.contains("preserved toe"))
        {
            img = Features.ARTIFACT_PRESERVED_TOE;
        }
        else if(treasureName.contains("giant egg"))
        {
            img = Features.ARTIFACT_GIANT_EGG;
        }
        else if(treasureName.contains("linen cloth"))
        {
            img = Features.ARTIFACT_LINEN_CLOTH;
        }
        else if(treasureName.contains("petrified bones"))
        {
            img = Features.ARTIFACT_PETRIFIED_BONES;
        }
        else if(treasureName.contains("goat skin"))
        {
            img = Features.ARTIFACT_GOAT_SKIN;
        }
        else if(treasureName.contains("rabbit's paw"))
        {
            img = Features.ARTIFACT_RABBIT_PAW;
        }
        else if(treasureName.contains("shoes"))
        {
            img = Features.ARTIFACT_SHOES;
        }
        else if(treasureName.contains("mug"))
        {
            img = Features.ARTIFACT_MUG;
        }
        else if(treasureName.contains("urn"))
        {
            img = Features.ARTIFACT_URN;
        }
        else
        {
            img = Features.ARTIFACTS_FIRST;
        }
        
        return createItem(treasureName, img, Item.ARTIFACT_T1);
    }
    
    
    private void revealArea(Map map, Point p) 
    {
        int d = 2;
        
        for(int i=-d; i<=d; i++)
        {
            for(int j=-d; j<=d; j++)
            {
                map.setColor(p.x + i * Map.SUB, p.y + j * Map.SUB, 0xFFA0A0A0);
            }            
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
