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
import java.util.HashSet;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import rlgamekit.objects.Registry;


/**
 *
 * @author Hj. Malthaner
 */
public class Party
{
    private static final Logger LOG = Logger.getLogger(Party.class.getName());    
    
    public final ArrayList <Integer> members = new ArrayList<>();
    
    public int intelligence;
    public int stealth;
    public int combat;
    public int carry;
    public int speed;
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
            Mob mob = mobs.get(key);
            
            // Only those still alive can contribute
            if(mob.stats.getCurrent(MobStats.VITALITY) > 0)
            {
                int species = mob.getSpecies();
                double factor = 1 + mob.getLevel() * 0.3;

                SpeciesDescription desc = Species.speciesTable.get(species);

                intelligence = Math.max(intelligence, (int)(desc.intelligence * factor));
                combat += (int)(desc.combat * factor);
                stealth = Math.min(stealth, (int)(desc.stealth * factor));
                speed = Math.min(speed, (int)(desc.speed * factor));
                scouting = Math.max(scouting, (int)((desc.speed + desc.intelligence) * 0.5 * factor));
            
                // Ghost adjustments
                if(mob.isGhost())
                {
                    scouting += 1;
                    stealth += 2;                
                    carry = Math.max(0, carry - 1);
                }
            }
        }
        
        if(members.isEmpty())
        {
            stealth = 0;
            speed = 0;
        }
    }
    
    
    public String decimate(Registry <Mob> mobs, Random rng, int hits)
    {
        // int injuriesOld = countMatches(mobs, (mob) -> {return mob.stats.getCurrent(MobStats.INJURIES) > 0});
        
        int injuries = 0;
        int fatalities = 0;
        
        HashSet <Mob> newInjuries = new HashSet<>();
        
        for(int i=0; i<hits; i++)
        {
            int n = (int)(rng.nextDouble() * members.size());
            
            Mob mob = mobs.get(members.get(n));
            
            int health = mob.stats.getCurrent(MobStats.INJURIES);

            if(health == 0)
            {
                mob.stats.setCurrent(MobStats.INJURIES, 1);
                
                if(!newInjuries.contains(mob))
                {
                    injuries ++;
                    newInjuries.add(mob);
                }
            }
            else if(health == 1)
            {
                // injured again -> dead
                mob.stats.setCurrent(MobStats.INJURIES, 2);
                mob.stats.setCurrent(MobStats.VITALITY, 0);
                fatalities ++;
            }
        }

        // count kills
        kills += fatalities;
        
        LOG.log(Level.INFO, "{0} injured, {1} killed, total kills: {2}", new Object[]{injuries, fatalities, kills});
        
        // write it down
        StringBuilder sb = new StringBuilder();
        count(sb, injuries, "injured");
        
        if(sb.length() > 0) sb.append(' ');
        
        count(sb, fatalities, "killed");
        
        // party might be weaker now
        calculateStats(mobs);
        
        return sb.toString();
    }

    
    private void count(StringBuilder buffer, int n, String what)
    {
        switch (n) 
        {
            case 0:
                // buffer.append("All party members survived. ");
                break;
            case 1:
                buffer.append("One party member was ").append(what).append(".");
                break;
            default:
                buffer.append(n).append(" party members were ").append(what).append(".");
                break;
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
            int key = Integer.parseInt(line.substring(6));
            members.add(key);
        }

        line = reader.readLine();
        
        assert("Party data end".equals(line));
        
        
        calculateStats(mobs);
    }

    
    public int countMatches(Registry <Mob> mobs, Predicate<Mob> check) 
    {
        int count = 0;
        
        for(int key : members)
        {
            Mob mob = mobs.get(key);
            
            if(check.test(mob))
            {
                count ++;
            }
        }

        return count;
    }
}
