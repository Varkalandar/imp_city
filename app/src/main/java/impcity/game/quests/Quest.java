package impcity.game.quests;

import impcity.game.Party;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import impcity.game.mobs.Mob;
import rlgamekit.objects.Registry;

public class Quest
{
    public long seed;
    public String story;
    public String locationName;
    public boolean locationIsBuilding;
    public int findingDifficulty;
    public int treasureSize;
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
    
    public Party party;

    public void write(FileWriter writer) throws IOException
    {
        writer.write("Quest data start\n");
        writer.write("qseed=" + seed + "\n");
        writer.write("story=" + story + "\n");
        writer.write("lname=" + locationName + "\n");
        writer.write("build=" + locationIsBuilding + "\n");
        writer.write("fdiff=" + findingDifficulty + "\n");
        writer.write("tsize=" + treasureSize + "\n");
        writer.write("guard=" + guardHardness + "\n");
        writer.write("start=" + startTime + "\n");
        writer.write("ttime=" + travelTime + "\n");
        writer.write("durat=" + duration + "\n");
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
        
        assert("Quest data start".equals(line));
        
        line = reader.readLine();
        seed = Long.parseLong(line.substring(6));

        line = reader.readLine();
        story = line.substring(6);

        line = reader.readLine();
        locationName = line.substring(6);
        
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
        
        assert("Quest data end".equals(line));
    }
}
