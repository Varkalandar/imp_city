package impcity.game;

import impcity.game.ai.MobStats;
import impcity.game.species.Species;
import impcity.game.species.SpeciesDescription;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import impcity.game.mobs.Mob;
import rlgamekit.objects.Registry;

/**
 *
 * @author Hj. Malthaner
 */
public class Party
{
    public final ArrayList <Integer> members = new ArrayList<>();
    
    public int intelligence;
    public int stealth;
    public int combat;
    public int carry;
    public int speed = 1;
    public int scouting;
    
    public int kills;
    
    public void calculateStats(Registry <Mob> mobs) 
    {
        intelligence = 0;
        combat = 0;
        stealth = 999;
        carry = members.size();
        speed = 999;
        scouting = 0;
        kills = 0;
        
        for(int key : members)
        {
            int species = mobs.get(key).getSpecies();
            
            SpeciesDescription desc = Species.speciesTable.get(species);
            
            intelligence = Math.max(intelligence, desc.intelligence);
            combat += desc.combat;
            stealth = Math.min(stealth, desc.stealth);
            speed = Math.min(speed, desc.speed);
            scouting = Math.max(scouting, (desc.speed + desc.intelligence)/2);
        }
        
        if(members.isEmpty())
        {
            stealth = 0;
            speed = 0;
        }
    }
    
    public String decimate(Registry <Mob> mobs, Random rng, int kills)
    {
        int injuries = 0;
        int fatalities = 0;
        
        for(int i=0; i<kills; i++)
        {
            int n = (int)(rng.nextDouble() * members.size());
            
            Mob mob = mobs.get(members.get(n));
            
            int health = mob.stats.getCurrent(MobStats.INJURIES);

            if(health == 0)
            {
                // no injuries yet
                // Hajo: check for crictical hits
                if(rng.nextDouble() < 0.95)
                {
                    mob.stats.setCurrent(MobStats.INJURIES, 1);
                    injuries++;
                }
                else
                {
                    // critical hit -> dead
                    members.remove(n);
                    fatalities++;
                }
            }
            else
            {
                // injured again -> dead
                members.remove(n);
                fatalities++;
            }
        }
        
        this.kills += fatalities;
        
        StringBuilder sb = new StringBuilder();
        count(sb, injuries, "injured");
        
        if(sb.length() > 0) sb.append(' ');
        
        count(sb, fatalities, "killed");
        
        calculateStats(mobs);
        return sb.toString();
    }

    private void count(StringBuilder buffer, int n, String what)
    {
        if(n == 0)
        {
            // buffer.append("All party members survived. ");
        }
        else if(n == 1)
        {
            buffer.append("One party member was ").append(what).append(".");
        }
        else
        {
            buffer.append(n).append(" party members were ").append(what).append(".");
        }
    }

    public void write(FileWriter writer) throws IOException
    {
        writer.write("Party data start\n");
        writer.write("size=" + members.size() + "\n");

        for(Integer key : members)
        {
            writer.write("mobid=" + key + "\n");
        }
        
        writer.write("Party data end\n");
    }

    public void load(BufferedReader reader, Registry <Mob> mobs) throws IOException
    {
        members.clear();
        
        String line;
        
        line = reader.readLine();
        int count = Integer.parseInt(line.substring(5));
    
        for(int i=0; i<count; i++)
        {
            line = reader.readLine();
            int key = Integer.parseInt(line.substring(5));
            members.add(key);
        }

        line = reader.readLine();
        
        assert("Party data end".equals(line));
        
        
        calculateStats(mobs);
    }
}
