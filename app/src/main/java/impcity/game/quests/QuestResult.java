package impcity.game.quests;

import impcity.game.Party;
import impcity.game.ai.MobStats;
import impcity.game.mobs.Mob;
import rlgamekit.objects.Registry;

/**
 *
 * @author Hj. Malthaner
 */
public class QuestResult 
{
    public final String story;
    public final String summary;
    public final Quest quest;
    public final boolean success;

    QuestResult(Registry<Mob> mobs, Quest quest, QuestProcessor.LocationEvent locationEvent, String story)
    {
        this.quest = quest;
        this.story = story;
        this.summary = makeSummary(mobs, quest, locationEvent);
        this.success = locationEvent != null && locationEvent.found;
    }

    private String makeSummary(Registry<Mob> mobs, Quest quest, QuestProcessor.LocationEvent locationEvent)
    {
        StringBuilder sb = new StringBuilder();
        Party party = quest.party;
        int injuries = 0;
        
        for(Integer key : party.members)
        {
            Mob mob = mobs.get(key);
            
            int health = mob.stats.getCurrent(MobStats.INJURIES);
            if(health > 0) injuries ++;
        }
        
        
        sb.append("Creatures returning: ").append(party.members.size()).append('\n');
        sb.append("Creatures killed in the quest: ").append(party.kills).append('\n');
        sb.append("Returning injured: ").append(injuries).append('\n').append('\n');
        sb.append("Creatures report: ").append('\n').append('\n');

        if(locationEvent == null)
        {
            sb.append("We had so many losses during the journey, we couldn't reach the location.").append('\n');
        }
        else
        {
            if(locationEvent.found)
            {
                sb.append("We found the location and we are coming back with ")
                        .append(quest.party.carry)
                        .append(' ')
                        .append(quest.treasureName)
                        .append("!\n");
            }
            else
            {
                sb.append("We couldn't find ")
                        .append(quest.locationName.toLowerCase())
                        .append(", although we've searched the area for ")
                        .append(locationEvent.searchTime)
                        .append(" days! The map must be wrong.\n");
            }
        }
        
        return sb.toString();
    }
}
