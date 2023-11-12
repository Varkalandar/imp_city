package impcity.game.quests;

import impcity.game.Party;
import java.util.Random;
import impcity.game.World;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Create a quest log
 */
public class QuestProcessor
{
    private static final Logger logger = Logger.getLogger(QuestProcessor.class.getName());

    /** All party members are dead */
    private final static int WIPED = -999999;


    private final TravelEvent [] travelEvents = 
    {    
       new TravelEvent("On our way. Clear path, nothing happened.", 0, 0),
       new TravelEvent("Made our way through bushland.", 0, 0),
       new TravelEvent("Passed through a dense forest.", 0, 0),
       new TravelEvent("Wandered over open plains.", 0, 0),
       new TravelEvent("Traversed a deserted area.", 0, 1),
       new TravelEvent("Crossed a mountain.", 0, 1),
       new TravelEvent("Crossed a deep chasm.", 0, 1),
       new TravelEvent("Passed through a swampy area.", 0, 2),
       new TravelEvent("Crossed a wild river.", 0, 3),
       new TravelEvent("Been ambushed by wild animals!", 1, 5),
    };

   
    private final TravelEvent [] combatEvents = 
    {    
       new TravelEvent("Saw some upperworlders at a distance, they probably didn't noticed us.", 2, 0),
       new TravelEvent("Saw some upperworlders. Party might have been noticed.", 3, 0),
       new TravelEvent("Saw some upperworlders nearby. We've likely been noticed.", 4, 0),
       new TravelEvent("Ran into lightly armed wanderers.", 3, 5),
       new TravelEvent("Met a group of armed upperworlders.", 4, 11),
       new TravelEvent("We've been ambushed by upperworlders!", 5, 17),
    };

    
    private final TravelEvent [] hidingEvents =
    {
       new TravelEvent("Party is hiding in a group of trees.", -1, 0),
       new TravelEvent("Party is hiding between some huge rocks.", -1, 0),
       new TravelEvent("Party is hiding in a small cave.", -1, 0),
       new TravelEvent("Party dug holes to hide in.", -1, 0),
    };


    TravelEvent stillHiding = new TravelEvent("Party is still hiding.", -1, 0);

    TravelEvent ambushedInHideout = new TravelEvent("We've been assaulted by upperworlders in our hideout!", 5, 24);


    private final LocationEvent [] locationEvents =
    {
       new LocationEvent("Could not find anything like {0} described on the trasure map.", 0.0),
    };


    private final LocationEvent [] buildingLocationEvents =
    {
       new LocationEvent("Could not find anything like {0} described on the map.", 0.0),
       new LocationEvent("Found a rock pile that might be {0} described on the map.", 0.1),
       new LocationEvent("Found a hut that might be {0} described on the trasure map.", 0.2),
       new LocationEvent("Found a building that looks a bit like {0} described on the map.", 0.3),
       new LocationEvent("Found a building that very much looks like {0} described on the map.", 0.4),
       new LocationEvent("Found a building which most certainly is  {0} described on the map.", 0.5),
       new LocationEvent("Found a signpost and a building which seems to be {0} described on the map.", 0.6),
       new LocationEvent("Found a signpost, markings and a building which must be be {0} described on the map.", 0.7),
       new LocationEvent("Found a signpost, markings and a building which must be be {0} described on the map. Also interrogated a captured local about the building who supoorted our findings.", 0.8),
    };


    /** We need repeatable results, so this must be seeded with the quest seed each time */
    private Random rng;
   
    private enum State {TRAVELLING_TO, SEARCHING, RETRIEVING, TRAVELLING_BACK};

    private State state;
   
   
    public QuestResult createLog(World world, Quest quest)
    {
        this.rng = new Random(quest.seed);
        
        LocationEvent locationEvent = null;
        StringBuilder buffer = new StringBuilder();
        int days;
        
        state = State.TRAVELLING_TO;
        days = processTravel(world, quest, buffer, 1);

        if(days == WIPED)
        {
            // Hajo: party is completely gone
        }
        else if(days < 0)
        {
            buffer.append('\n');
            
            // Hajo: party decided to retreat.
            state = State.TRAVELLING_BACK;
            int daysBack = processTravel(world, quest, buffer, 1 + -days);
            quest.duration = 1 + -days + daysBack;
            buffer.append("Day ").append(quest.duration).append(": ");
            buffer.append("Finally. Reached our safe dungeon again. Not going anywhere anymore. No.");
        }
        else
        {
            // Hajo: party is still alive
        
            buffer.append('\n');

            locationEvent = processLocationSearch(quest, buffer, days);

            buffer.append('\n');

            if(locationEvent.found)
            {
                // could be that there is something here ...
                days += processRetrieval(quest, buffer, days + locationEvent.searchTime);

                buffer.append('\n');
            }

            state = State.TRAVELLING_BACK;
            int daysBack = processTravel(world, quest, buffer, 1 + days + locationEvent.searchTime);

            quest.duration = 1 + days + locationEvent.searchTime + daysBack;
            
            buffer.append("Day ").append(quest.duration).append(": ");
            buffer.append("Reached a secret dungeon entrance. Home again!");
        }
        
        QuestResult result = new QuestResult(world.mobs, quest, locationEvent, buffer.toString());
        return result;
    }

    
    private int processTravel(World world, Quest quest, StringBuilder buffer, int days)
    {
        Party party = quest.party;
        buffer.append("Travel Log:\n");

        int currentDangerLevel = 0;
        int travelTimeDays = quest.travelTime / quest.party.speed;
        int hiding = 0;
        
        for(int i=0; i<travelTimeDays && travelTimeDays < 100; i++)
        {
            buffer.append("Day ").append(days + i).append(": ");
            // buffer.append("(").append(party.members.size()).append(") ");
            
            int escalatedDangerLevel = currentDangerLevel +
                                       (int)(rng.nextDouble() * 2); 
            
            // let's see how many are left ...
            if(party.members.isEmpty())
            {
                buffer.append("Party was wiped. You'll never know this ...\n");
                return WIPED;
            } 
            else if(party.members.size() * 2 < party.kills || party.members.size() < 2)
            {
                // Too few left, retreat
                // Hajo: already on the way back home?
                if(state == State.TRAVELLING_BACK)
                {
                    // seems so ...
                }
                else
                {
                    buffer.append("The remaining ").append(party.members.size()).append(" party members decided to reatreat, due to losses.\n");
                    
                    int daysLeft = - (i + 1);
                    logger.log(Level.INFO, "Too many losses. Party retreats. Days left: " + daysLeft);
                    return daysLeft;
                }
            }
            
            int n;
            TravelEvent event = travelEvents[0];
            
            if(escalatedDangerLevel == 0)
            {
                // natural events
                n = (int)(rng.nextDouble() * travelEvents.length);
                event = travelEvents[n];
                if(hiding > 0)
                {
                    buffer.append("Continuing the journey. ");
                    hiding = 0;
                }
            }
            else if(escalatedDangerLevel > 0)
            {
                // possibility to meet upperworlders
                
                int intelligence = 5;
                
                if(intelligence < 5 || currentDangerLevel < 3 + (int)(rng.nextDouble() * 2))
                {
                    if(hiding > 0)
                    {
                        buffer.append("Continuing the journey. ");
                        hiding = 0;
                    }
                    
                    // first, travel notes:
                    
                    n = (int)(rng.nextDouble() * travelEvents.length);
                    event = travelEvents[n];
                    
                    buffer.append(event.message).append(' ');
                    int kills = calculateKills(world, buffer, party, event);
                    if(kills > 0)
                    {
                        String kf = party.decimate(world.mobs, rng, kills);
                        buffer.append(kf).append(' ');
                    }
                    // then, combat notes
                    
                    n = (int)(rng.nextDouble() * Math.min(combatEvents.length, escalatedDangerLevel));
                    
                    // stealth helps to get less noticed
                    if(n > 0)
                    {
                        if(rng.nextDouble() * 10 < party.stealth)
                        {
                            n--;
                        }
                    }
                    
                    event = combatEvents[n];
                }
                else
                {
                    // Play hide and seek
                    n = (int)(rng.nextDouble() * hidingEvents.length);
                    event = hidingEvents[n];
                    
                    // no progress while hiding
                    travelTimeDays ++;
                    hiding ++;
                    
                    // continued hiding?
                    if(hiding > 1)
                    {
                        event = stillHiding;
                    
                        // being discovered? -> Ambush chance
                        double chance = 1.0 - 100.0 / (100+hiding);

                        if(rng.nextDouble() < chance)
                        {
                            event = ambushedInHideout;
                            hiding = 0;
                        }
                    }
                }
            }
            
            buffer.append(event.message).append(' ');

            int kills = calculateKills(world, buffer, party, event);
            if(kills > 0)
            {
                String kf = party.decimate(world.mobs, rng, kills);
                buffer.append(kf).append(' ');
            }
            
            currentDangerLevel = escalatedDangerLevel +
                                 (int)(rng.nextDouble() * event.dangerLevel)
                                 -1;  // deescalate over time
            
            if(currentDangerLevel < 0) currentDangerLevel = 0;
            
            
            // buffer.append(" dl=").append(currentDangerLevel);
            // buffer.append(" hiding=").append(hiding);
            buffer.append('\n');
        }
    
        return travelTimeDays;
    }


    private LocationEvent processLocationSearch(Quest quest, 
                                                StringBuilder buffer, 
                                                int days)
    {
        // Search at least 3 days, more if there are more intelligent creatures.
        int maxSearchDays = Math.min(quest.party.intelligence, 3);

        // accept matches at least this good (dumb creatures are more likely to make mistakes)
        double threshold = Math.min(quest.party.intelligence/8.0, 0.5);
        
        int searchDays = 0;
        
        buffer.append("Reached destination:\n");
        
        LocationEvent event;
        LocationEvent best = new LocationEvent("dummy", -1.0);

        do
        {
            //
            // Higher intelligence and scouting should increase the 
            // probability of finding something, unless the map was fake.
            //
            // So does the number of past expeditions to this location
            //
            int boost =
                    (quest.party.intelligence +
                     quest.party.scouting +
                     quest.expeditions * 3) / 3;
            
            double p = boostedProbability(boost);


            // find something that matches. Use boosted probability to find better matches
            do
            {
                if(quest.locationIsBuilding)
                {
                    int n = (int)(rng.nextDouble() * buildingLocationEvents.length);
                    event = buildingLocationEvents[n];
                }
                else
                {
                    int n = (int)(rng.nextDouble() * locationEvents.length);
                    event = locationEvents[n];
                }
            }
            while(event.probability < p);

            searchDays++;

            // over time accept worse and worse matches ...
            threshold -= 0.1;

            if(event.probability > best.probability)
            {
                best = event;
                buffer.append("Day ").append(days + searchDays).append(": ");
                buffer.append(event.message.replace("{0}", quest.locationName.toLowerCase()));
            }
            else
            {
                buffer.append("Day ").append(days + searchDays).append(": ");
                buffer.append("We found nothing ");
                if(best != null && best.probability > 0.0) buffer.append("better ");
                buffer.append("yet.");
            }
            
            buffer.append('\n');
        } 
        while(event.probability < threshold && searchDays < maxSearchDays);   
        
        searchDays ++;

        // check what we found
        if(best.probability == 0.0)
        {
            buffer.append("Day ").append(days + searchDays)
                    .append(": We give up searching. The map must be wrong.\n");
        }
        else
        {
            buffer.append("Day ").append(days + searchDays)
                    .append(": We think we found the object described on the map.\n");
            // Found something which is considered the destination
            event.found = true;
        }
        
        event.searchTime = searchDays;
        
        return event;
    }


    private int processRetrieval(Quest quest, StringBuilder buffer, int days)
    {
        quest.status |= Quest.SF_FOUND;
        
        int retDays = 0;
        // is it guarded
        if(quest.guardHardness == 0)
        {
            // no ...
            retDays ++;
            if(quest.locationIsBuilding)
            {
                buffer.append("Day ").append(days + retDays)
                        .append(": We entered ")
                        .append(quest.locationName.toLowerCase())
                        .append(" and found ")
                        .append(quest.treasureName).append("!");
            }
            else
            {
                buffer.append("Day ").append(days + retDays)
                        .append(": We've been digging and searching around ")
                        .append(quest.locationName.toLowerCase())
                        .append(" and finally found ")
                        .append(quest.treasureName).append("!");
            }
        }
        else
        {
            
        }
        
        return retDays;
    }


    private int calculateKills(World world, StringBuilder buffer, Party party, TravelEvent event) 
    {
        int kills = 0;
        
        if(event.combatLevel > 0)
        {
            // todo: party vs. enemies
            kills = (int)(rng.nextDouble() * (1 + event.combatLevel - party.combat));
            if(kills < 0) kills = 0; // don't magically add new members from the dead realms ...
        }
        
        // Hajo: we need one to return and tell ... at least most of the time
        int stillAlive = party.calculateStillAlive(world.mobs);
        if(kills >= stillAlive)
        {
            if(rng.nextDouble() < 0.95)
            {
                // spare one
                kills = stillAlive - 1;
            }
        }
        
        // never kill more than there are ...
        if(kills > stillAlive) 
        {
            kills = stillAlive;
        }
        
        logger.log(Level.INFO, "Combat. Creatures killed: " + kills);
        
        return kills;
    }

    
    /**
     * Roll a probability (0 .. 1). Roll boost times, take best.
     * @param boost Number of rolls.
     * @return Best roll
     */
    private double boostedProbability(int boost) 
    {
        double p = 0;
        
        for(int i=0; i< boost; i++)
        {
            double pn = rng.nextDouble();
            
            if(pn > p) p = pn;
        }
        
        return p;
    }

    
    private static class TravelEvent
    {
        final String message;
        final int dangerLevel;
        final int combatLevel;
        
        public TravelEvent(String message, int dangerLevel, int combatLevel)
        {
            this.message = message;
            this.dangerLevel = dangerLevel;
            this.combatLevel = combatLevel;
        }
    }


    public static class LocationEvent
    {
        final String message;
        final double probability;
        public int searchTime;
        public boolean found;
        
        public LocationEvent(String message, double probability)
        {
            this.message = message;
            this.probability = probability;
        }
    }
}
