package impcity.game.mobs;

import java.awt.Point;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import impcity.game.Direction;
import impcity.game.ai.Ai;
import impcity.game.ai.MobStats;
import impcity.game.map.Map;
import impcity.oal.SoundPlayer;
import rlgamekit.item.ItemCatalog;
import rlgamekit.map.data.LayeredMap;
import rlgamekit.pathfinding.Path;
import rlgamekit.stats.Stats;

/**
 * Mob data.
 * 
 * @author Hj. Malthaner
 */
public class Mob
{
    public static final Logger logger = Logger.getLogger(Mob.class.getName());
    
    public static final int SLOT_HEAD = 0;
    public static final int SLOT_BODY = 1;
    public static final int SLOT_LEGS = 2;
    public static final int SLOT_WAIST = 3;
    public static final int SLOT_HANDS = 4;
    public static final int SLOT_FEET = 5;
    public static final int SLOT_FIRST_HAND = 6;
    public static final int SLOT_SECOND_HAND = 7;
    public static final int SLOT_FIRST_RING = 8;
    public static final int SLOT_SECOND_RING = 9;
    public static final int SLOT_NECK = 10;
    
    private Path path;
    public String name;
    public MobVisuals visuals;
    public MovementPattern pattern = new MovementJumping();
    
    private Ai ai;
    public boolean isDying;

    public Ai getAi() 
    {
        return ai;
    }

    public void setAi(Ai ai) 
    {
        this.ai = ai;
    }
    
    /**
     * Current location of the player
     */
    public final Point location;

    /**
     * The map data that is shown to the player (will not show
     * hidden features or changes to "remembered" areas).
     */
    public LayeredMap displayMap;


    private long lastActionTime;
    
    private int stepsPerSecond;
    
    /** i-axis offset in 1/(1<<16) of a square */
    public int iOff;
    
    /** j-axis offset in 1/(1<<16) of a square */
    public int jOff;

    /** z-axis offset in 1/(1<<16) of a pixel */
    public int zOff;

    /** up/down speed as 16 bit fixed point integer */
    public int zSpeed;
    
    private int species;
    private int key;
    
    /**
     * The actual game map. All game functions should use this.
     */
    public final Map gameMap;
    
    
    public final Stats stats;

    public int getSpecies()
    {
        return species;
    }

    public int getKey()
    {
        return key;
    }

    public void setKey(int key)
    {
        this.key = key;
    }
                
    public Mob(int playerX, int playerY, int species, int shadow, int sleep, Map gameMap, Ai ai, int speed, MovementPattern pattern)
    {
        this.visuals = new MobVisuals(shadow, sleep);
        this.species = species;
        this.gameMap = gameMap;
        this.location = new Point(playerX, playerY);
        
        this.pattern = pattern;
        this.stepsPerSecond = speed;
        this.ai = ai;
        

        stats = new Stats(new String []
        {
            "Str", "Dex", "Int", "Wis", "Vit", "Ext1", "Ext2", "Ext3", "Ext4", "Ext5"
        });
        
        visuals.setDisplayCode(species);
        
        this.lastActionTime = System.currentTimeMillis();
    }

    
    public void setPath(Path path) 
    {
        this.path = path;

        if(path != null)
        {
            Path.Node node = path.currentStep();

            if(node != null && node.x == location.x && node.y == location.y)
            {
                path.advance();
            }
        }
        
        lastActionTime = System.currentTimeMillis();        
    }
    
    public Path getPath()
    {
        return path;
    }

    public boolean advance(SoundPlayer soundPlayer) 
    {
        long time = System.currentTimeMillis();
        
        int deltaT = (int)(time - lastActionTime);
        boolean lastStep = false;
        
        if(path != null)
        {
            Path.Node node = path.currentStep();
            
            if(node != null)
            {
                int steps = deltaT * stepsPerSecond * (1 << 16) / 1000;

                // System.err.println("Move: " + time + " steps=" + steps);
                
                int dx = node.x - location.x;
                int dy = node.y - location.y;

                assert(dx >= -1 && dx <= 1);
                assert(dy >= -1 && dy <= 1);
                
                iOff += dx * steps;
                jOff += dy * steps;
            
                // System.err.println("Mob #" + key + " steps=" + steps);
                
                // System.err.println("Mob #" + key + " ioff=" + (iOff/(double)(1<<16)) + " jOff=" + (jOff/(double)(1<<16)));

                boolean needHop;
                needHop = false;

                if(iOff > (1<<15)) 
                {
                    needHop = true;
                }
                if(iOff < -(1<<15)) 
                {
                    needHop = true;
                }

                if(jOff > (1<<15)) 
                {
                    needHop = true;
                }
                if(jOff < -(1<<15)) 
                {
                    needHop = true;
                }

                if(needHop && path != null)
                {
                    hop();
                    iOff = dx * -(1<<15);
                    jOff = dy * -(1<<15);
                }
                
                assert(iOff >= -(1<<15) && iOff <= (1<<15));
                assert(jOff >= -(1<<15) && jOff <= (1<<15));
                
                pattern.calculateMove(this, deltaT);
            }
            else
            {
                zOff = iOff = jOff = 0;
                lastStep = true;
                setPath(null);
            }
        }

        lastActionTime = time;
        
        if(ai != null)
        {
            ai.thinkAfterStep(this);
        }
        
        return lastStep;
    }
    
    /**
     * Take a hop from one square to the next on the path.
     */
    private void hop() 
    {
        Path.Node node = path.currentStep();
        if(node != null)
        {
            // check if this place has been blocked since the path was found
            if(gameMap.isMovementBlocked(node.x, node.y))
            {
                // can't go there ...
                path = null;
                setPath(null);
                return;
            }

            gameMap.setMob(location.x, location.y, 0);

            int dx = node.x - location.x;
            int dy = node.y - location.y;

            location.x = node.x;
            location.y = node.y;

            // System.err.println("Mob #" + key + " hops to " + location.x + ", " + location.y);

            gameMap.setMob(location.x, location.y, key);

            path.advance();


            visuals.setDisplayCode(species + Direction.dirFromVector(dx, dy));
        }
        else
        {
            zOff = iOff = jOff = 0;
            
            setPath(null);
        }
    }
    
    /**
     * Called every frame in between display
     */
    public void update()
    {
        // drive particles in sync with display     
            
        visuals.frontParticles.driveParticles();
        visuals.backParticles.driveParticles();    
              
        if(path == null && ai != null)
        {
            // Hajo: we need a new path ...
            // ... but only if we are still alive
            if(stats.getCurrent(MobStats.VITALITY) >= 0)
            {
                ai.think(this);
                ai.findNewPath(this);
            }
        }
    }

    /**
     * Called after all the ordinary stuff (map) has been drawn.
     */
    public void displayMoreUpdate() 
    {
        // Hajo: Avoid invisible monsters (can happen if the first of a "stack" dies
        gameMap.setMob(location.x, location.y, getKey());
    }

    public void addExperience(int howmuch)
    {
        stats.setCurrent(MobStats.EXPERIENCE, stats.getCurrent(MobStats.EXPERIENCE) + howmuch);
    }

    public int getLevel()
    {
        return (int)(Math.log(stats.getCurrent(MobStats.EXPERIENCE) * 0.01) * 1.5);
    }

    public void write(final Writer writer)  throws IOException
    {
        writer.write("Inventory start\n");
        writer.write("v.1\n");
        writer.write("Inventory end\n");

        writer.write("Equipment start\n");
        writer.write("v.1\n");
        writer.write("Equipment end\n");

        writer.write("Mob data start\n");
        
        writer.write("name=" + name + "\n");
        writer.write("rkey=" + key + "\n");
        writer.write("xpos=" + location.x + "\n");
        writer.write("ypos=" + location.y + "\n");

        writer.write("spec=" + species + "\n");
        writer.write("ioff=" + iOff + "\n");
        writer.write("joff=" + jOff + "\n");
        writer.write("sped=" + stepsPerSecond + "\n");
        writer.write("Mob data end\n");

        writer.write("Path data start\n");
        if(path == null)
        {
            writer.write("pl=<null>\n");            
        }
        else
        {
            writer.write("pl=" + path.length() + "\n");
            for(int i=0; i<path.length(); i++)
            {
                Path.Node node = path.getStep(i);
                writer.write("px=" + node.x + "\n");
                writer.write("py=" + node.y + "\n");
            }
            writer.write("pi=" + path.getCurrentStepIndex() + "\n");
        }
        
        writer.write("Path data end\n");
        
        writer.write("Stats data start\n");
        for(int i=0; i<stats.size(); i++)
        {
            writer.write("min=" + stats.getMin(i) + "\n");
            writer.write("max=" + stats.getMax(i) + "\n");
            writer.write("cur=" + stats.getCurrent(i) + "\n");
        }        
        writer.write("Stats data end\n");
    }

    public void save(File folder) throws IOException
    {        
        boolean ok = folder.exists();
        
        if(!ok)
        {
            folder.mkdirs();
        }
        
        if(ok)
        {
            File file = new File(folder, "player.dat");
            FileWriter writer = new FileWriter(file);
            write(writer);
            writer.close();

            System.err.println("Game saved.\n");
        }
        else
        {
            logger.log(Level.SEVERE, "Could not create directory: {0}", folder);
        }
    }

    public void read(final BufferedReader reader,
                     final ItemCatalog itemCatalog)
                     throws IOException
    {
        String line;

        line = reader.readLine();

        if("Inventory start".equals(line) == false) 
        {
            throw new IOException("Missing: Inventory start, got: " + line);
        }

        line = reader.readLine();

        if("v.1".equals(line) == false) 
        {
            throw new IOException("Missing: v.1 for inventory");
        }

        while("Inventory end".equals(line) == false && line != null) 
        {
            line = reader.readLine();
            if("Inventory end".equals(line) == false) 
            {
                throw new IOException("Missing: Inventory end");
            }
        }

        line = reader.readLine();

        if("Equipment start".equals(line) == false) 
        {
            throw new IOException("Missing: Equipment start");
        }

        line = reader.readLine();

        if("v.1".equals(line) == false) 
        {
            throw new IOException("Missing: v.1 for equipment");
        }


        while("Equipment end".equals(line) == false && line != null) 
        {
            line = reader.readLine();
            if("Equipment end".equals(line) == false) 
            {
                throw new IOException("Missing: Equipment end");
            }
        }
        
        
        line = reader.readLine();

        if("Mob data start".equals(line) == false) 
        {
            throw new IOException("Missing: Mob data start");
        }

        line = reader.readLine();
        name = line.substring(5);

        line = reader.readLine();
        key = Integer.parseInt(line.substring(5));
        
        line = reader.readLine();
        location.x = Integer.parseInt(line.substring(5));
        line = reader.readLine();
        location.y = Integer.parseInt(line.substring(5));

        line = reader.readLine();
        species = Integer.parseInt(line.substring(5));
        line = reader.readLine();
        iOff = Integer.parseInt(line.substring(5));
        line = reader.readLine();
        jOff = Integer.parseInt(line.substring(5));
        line = reader.readLine();
        stepsPerSecond = Integer.parseInt(line.substring(5));

        line = reader.readLine();
    
        if("Mob data end".equals(line) == false) 
        {
            throw new IOException("Missing: Mob data end for mob=" + key);
        }

        line = reader.readLine();
    
        if("Path data start".equals(line) == false) 
        {
            throw new IOException("Missing: Path data start");
        }
        
        line = reader.readLine();
        
        if("pl=<null>".equals(line))
        {
            path = null;
        }
        else
        {
            int pathLength = Integer.parseInt(line.substring(3));
            path = new Path();
            for(int i=0; i<pathLength; i++)
            {
                line = reader.readLine();
                int px = Integer.parseInt(line.substring(3));
                line = reader.readLine();
                int py = Integer.parseInt(line.substring(3));
                path.addStep(px, py);
            }
            line = reader.readLine();
            int pi = Integer.parseInt(line.substring(3));
            for(int i=0; i<pi; i++)
            {
                path.advance();
            }
        }
        
        line = reader.readLine();

        if("Path data end".equals(line) == false) 
        {
            throw new IOException("Missing: Path data end for mob=" + key);
        }
        
        
        line = reader.readLine();
        if("Stats data start".equals(line) == false) 
        {
            throw new IOException("Missing: Stats data start for mob=" + key + " got '" + line + "' instead");
        }
        for(int i=0; i<stats.size(); i++)
        {
            int n;
            line = reader.readLine();
            n = Integer.parseInt(line.substring(4));
            stats.setMin(i, n);
            line = reader.readLine();
            n = Integer.parseInt(line.substring(4));
            stats.setMax(i, n);
            line = reader.readLine();
            n = Integer.parseInt(line.substring(4));
            stats.setCurrent(i, n);
        }        
        line = reader.readLine();
        if("Stats data end".equals(line) == false) 
        {
            throw new IOException("Missing: Stats data end for mob=" + key + " got '" + line + "' instead");
        }
        
        visuals.setDisplayCode(species);

        // Hajo: give pre-experience-code saved mobs some basic experience after loading
        if(stats.getCurrent(MobStats.EXPERIENCE) == 0)
        {
            stats.setCurrent(MobStats.EXPERIENCE, MobStats.BEGINNER_EXPERIENCE);
        }
    }
    
    public void load(File folder, final ItemCatalog itemCatalog) throws IOException
    {
        File file = new File(folder, "player.dat");
        FileReader fread = new FileReader(file);
        BufferedReader reader = new BufferedReader(fread);

        read(reader, itemCatalog);
        reader.close();
    }

    public int[] calculateTotalResistances() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double calculateTotalBlock() {
        return 0;
    }

    public int calculateTotalDodge() {
        return 0;
    }

    public int calculateTotalDef() {
        return 0;
    }

    public int calculateTotalAttackRating() {
        return 0;
    }

    public void teleportTo(Point destination)
    {
        logger.log(Level.INFO, "Creature #{0} at {1}, {2} will be teleported to {3}, {4}.",
                new Object[]{getKey(), location.x, location.y, destination.x, destination.y});
        gameMap.setMob(location.x, location.y, 0);
        location.x = destination.x;
        location.y = destination.y;
        gameMap.setMob(location.x, location.y, getKey());
    }

	public boolean isGhost() 
	{
		return stats.getMax(MobStats.GHOST_STEPS) > 0;
	}

}
