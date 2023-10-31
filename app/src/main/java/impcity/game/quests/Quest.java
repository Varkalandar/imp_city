package impcity.game.quests;

import impcity.game.Party;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import impcity.game.mobs.Mob;
import java.util.logging.Level;
import java.util.logging.Logger;
import rlgamekit.objects.Registry;

public class Quest
{
    private static final Logger logger = Logger.getLogger(Quest.class.getName());
    
    /** set to true if this quest locations had been found already */
    public static final int SF_FOUND = 1;
    /** set to true if this quest locations has been completely plundered */
    public static final int SF_PLUNDERED = 2;
    
    // Treasure types
    public static final int TT_SILVER = 1;
    public static final int TT_GOLD = 2;
    public static final int TT_GEMS = 4;
    public static final int TT_ARTIFACT = 8;
    
    public long seed;
    public String story;
    public String locationName;
    public boolean locationIsBuilding;
    public int findingDifficulty;
    public int treasureSize;
    public int treasureType;
    public String treasureName;
    public int guardHardness;

    /** The day when the expedition was launched */
    public int startTime;

    /** input for quest processor */
    public int travelTime;
    
    /** The calculated amount of time for this quest. */
    public int duration;
    
    /** estimated time of arrival i.e. launch time + duration */
    public int eta;
    
    /** Bitfield made from status flags */
    public int status;
        
    /** Keeps track how many expeditions have been sent so far */
    public int expeditions;
    
    public Party party;

    public void write(FileWriter writer) throws IOException
    {
        writer.write("Quest data start\n");
        writer.write("qseed=" + seed + "\n");
        writer.write("story=" + story + "\n");
        writer.write("lname=" + locationName + "\n");
        writer.write("tname=" + treasureName + "\n");
        writer.write("build=" + locationIsBuilding + "\n");
        writer.write("fdiff=" + findingDifficulty + "\n");
        writer.write("tsize=" + treasureSize + "\n");
        writer.write("guard=" + guardHardness + "\n");
        writer.write("start=" + startTime + "\n");
        writer.write("ttime=" + travelTime + "\n");
        writer.write("durat=" + duration + "\n");
        writer.write("state=" + status + "\n");
        writer.write("exped=" + expeditions + "\n");
        writer.write("ttype=" + treasureType + "\n");
        writer.write("eta=" + eta + "\n");

        if(party != null)
        {
            party.write(writer);
        }
        else
        {
            writer.write("party=<null>\n");
        }
        writer.write("Quest data end\n");
    }
    
    public void load(BufferedReader reader, Registry <Mob> mobs) throws IOException
    {
        String line;
        
        line = reader.readLine();
        
        if(!"Quest data start".equals(line))
        {
            logger.log(Level.SEVERE, "Game data seems corrupted:" + line);
            logger.log(Level.SEVERE, "Expected: Quest data start");
        }
        
        line = reader.readLine();
        seed = Long.parseLong(line.substring(6));

        line = reader.readLine();
        story = line.substring(6);

        line = reader.readLine();
        locationName = line.substring(6);
        
        line = reader.readLine();
        treasureName = line.substring(6);
        
        line = reader.readLine();
        locationIsBuilding = Boolean.parseBoolean(line.substring(6));
        
        line = reader.readLine();
        findingDifficulty = Integer.parseInt(line.substring(6));
        
        line = reader.readLine();
        treasureSize = Integer.parseInt(line.substring(6));
        
        line = reader.readLine();
        guardHardness = Integer.parseInt(line.substring(6));
        
        line = reader.readLine();
        startTime = Integer.parseInt(line.substring(6));
        
        line = reader.readLine();
        travelTime = Integer.parseInt(line.substring(6));

        line = reader.readLine();
        duration = Integer.parseInt(line.substring(6));
        
        line = reader.readLine();
        
        // old quests didn't have status or expeditions
        if(line.startsWith("eta="))
        {
            eta = Integer.parseInt(line.substring(4));
            status = 0;
            expeditions = 0;
            treasureType = TT_GOLD;
        }
        else
        {
            status = Integer.parseInt(line.substring(6));
    
            line = reader.readLine();
            expeditions = Integer.parseInt(line.substring(6));
            
            line = reader.readLine();
            treasureType = Integer.parseInt(line.substring(6));

            line = reader.readLine();
            eta = Integer.parseInt(line.substring(4));
        }
        
        eta = Integer.parseInt(line.substring(4));

        line = reader.readLine();
        if("party=<null>".equals(line))
        {
            party = null;
        }
        else 
        {
            party = new Party();
            party.load(reader, mobs);
        }
        
        line = reader.readLine();
        
        if(!"Quest data end".equals(line))
        {
            logger.log(Level.SEVERE, "Game data seems corrupted:" + line);
            logger.log(Level.SEVERE, "Expected: Quest data end");
        }
    }
}
