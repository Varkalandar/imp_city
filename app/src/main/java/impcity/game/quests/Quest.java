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
    public int guardHardness;

    /** input for quest processor */
    public int travelTime;
    
    /** The calculated amount of time for this quest. */
    public int duration;
    
    /** estimated time of arrival i.e. lunch time + duration */
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
        writer.write("ttime=" + travelTime + "\n");
        writer.write("durat=" + duration + "\n");
        writer.write("eta=" + eta + "\n");
        
        party.write(writer);
        
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
        travelTime = Integer.parseInt(line.substring(6));
        
        line = reader.readLine();
        duration = Integer.parseInt(line.substring(6));
        
        line = reader.readLine();
        eta = Integer.parseInt(line.substring(4));

        if(party == null) 
        {
            party = new Party();
        }
        
        party.load(reader, mobs);
        
        assert("Quest data end".equals(line));
    }
    
}