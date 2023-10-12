package impcity.game.quests;

/**
 * Only treasure maps so far.
 * @author Hj. Malthaner
 */
public class QuestGenerator
{
    
    private static final String [] mapQualities = 
    {
        "a precise", 
        "a well readable", 
        "a tattered", 
        "a sketchy",
        "a complicated",
        "a fretted",
        "a faded",
        "a fragmented",
        "a partly burned",
        "an eroded",
        "an incomplete",
        "parts of a",
        "a largely unintelligible",
        "a seemingly encrypted", 
        "a heavily encrypted", 
    };
    
    private static final int [] mapQualityFindDifficulties = 
    {
        1, 
        2, 
        3, 
        4,
        5,
        6,
        7,
        8,
        9,
        10,
        11,
        12,
        13,
        14,
        16,
    };
    
    private static String [] areaLocations = 
    {
        "in {0} great plain",
        "in {0} grassy plain",
        "in {0} valley",
        "in {0} bushland",
        "in {0} river bend",
        "in {0} lake",
        "in {0} cave",
        "in {0} gorge",
        "in {0} forest",
        "in {0} quarry",
        "in {0} inactive volcano",
        "in {0} hollow tree trunk",
        "in {0} hilltop",
        "under {0} huge oak tree",
        "under {0} old linden tree",
        "under {0} big ash tree",
        "under {0} sacred fig tree",
        "under {0} statue",
        "under {0} cairn",
        "under {0} big rock",
        "at the foot of {0} massive crag",
        "at the foot of {0} obelisk",
        "near {0} scare crow",
        "near {0} obelisk",
        "near {0} ancient tree trunk",
    };
    
    private static String [] buildingLocations =
    {
        "tower",
        "temple",
        "palace",
        "stronghold",
        "fortress",
        "maze",
        "henge",
        "cemetery",
        "citadel",
        "grave",
        "tomb",
        "pyramid",
        "fountain",
        "magazine",
        "storehouse",
        "palace",
        "crypt",
        "tunnel",
        "fortification",
        "archive",    
        "arsenal",    
    };
        
      
    private static String [] buildingMods = 
    {
        "in {0}",
        "in {0} ruined",
        "in {0} old",
        "in {0} disused",
        "in {0} run down",
        "in {0} dark",
        "in {0} ancient",
        "in {0} holy",
        "in {0} unholy",
        "in {0} abandoned",
        "in {0} cursed",
        "in {0} haunted",
        "in {0} spider infested",
    };
    
    private static String [] treasureMods = 
    {
        "one or two",
        "a few",
        "some",
        "many",
        "numerous",
        "piles of"
    };

    private static int [] treasureModsSize = 
    {
        1, 
        2, 
        3, 
        4,
        5,
        6,
    };

    private static String [] distances =
    {
        "Your creatures can reach the place within some days.",
        "It'll probably take several days, maybe a week, to get there and back.",
        "Quite the journey, but within a few weeks your minions should be able to get there and back.",
        "It'll be a several months long ride into mostly unknown territory.",
        "The distance makes you wonder though, if you'll be still alive when your expedition returns.",
    };
    
    
    public static Quest makeTreasureQuest()
    {
        String [] intros =
        {
            "Your creatures discovered",
            "Hard working creatures of yours found",
            "Your creatures found",
            "A group of your creatures deciphered",
            "Your minions discovered",
            "A minion of yours found",
            "Your minions deciphered",
            "Your librarians stumbled upon",
            "Your library workers encountered",
            "In a dusty corner appeared",
            "Hidden under a pile of old papers there was",
            "From some old portfolio a bookworm retrieved",
            "Under a forgotten lunch box a janitor found"
        };

        String [] tellVars =
        {
            "tells of",
            "describes",
            "unveils",
            "shows the location of",
        };
        
        String [] findVars =
        {
          //  " riches to be found ",
          //  " treasures being hidden ",
          //  " rewards being buried ",
          //  " riches to be retreived ",
            " chests of gold ",
            " chests of gold and silver ",
            " urns of silver ",
            " boxes of gems ",
            " bags of coins ",
            " bags of gold nuggets ",
            " chests of valuables ",
            " money pouches ",
            " pots of ancient coins ",
            " gold ingots ",
            " silver ingots ",
            " lumps of silver ",
            " crates of artwork ",
        };
        
        Quest quest = new Quest();
        quest.seed = System.currentTimeMillis();
        
        StringBuilder text = new StringBuilder();
        int n;

        n = (int)(intros.length * Math.random());
        text.append(intros[n]);
        text.append(' ');
                
        n = (int)(mapQualities.length * Math.random());
        text.append(mapQualities[n]);
        quest.findingDifficulty = mapQualityFindDifficulties[n];

        text.append( " treasure map. The map ");
        
        n = (int)(tellVars.length * Math.random());
        text.append(tellVars[n]);
        text.append(' ');

        
        n = (int)(treasureMods.length * Math.random());
        text.append(treasureMods[n]);
        quest.treasureSize = treasureModsSize[n];
        
        n = (int)(findVars.length * Math.random());
        text.append(findVars[n]);

        boolean namedLocation = (Math.random() < 0.5);
        StringBuilder locationName = new StringBuilder();
        
        if(Math.random() < 0.5)
        {
            n = (int)(areaLocations.length * Math.random());
            locationName.append(areaLocations[n]);
            quest.locationIsBuilding = false;
        }
        else
        {        
            n = (int)(buildingMods.length * Math.random());
            locationName.append(buildingMods[n]);
            locationName.append(' ');

            n = (int)(buildingLocations.length * Math.random());
            locationName.append(buildingLocations[n]);
            
            quest.locationIsBuilding = true;
        }        
                
        int p = locationName.indexOf("{0}");
        if(namedLocation)
        {
            locationName.replace(p, p+3, "the");
            locationName.append(" of ");
            locationName.append(NameGenerator.makeLocationName(2, 5));
        }
        else
        {
            if("aeoui".indexOf(locationName.charAt(p+4)) > -1)
            {
                locationName.replace(p, p+3, "an");
            }
            else
            {
                locationName.replace(p, p+3, "a");
            }
                
        }
        
        // Upper case the first letter for display
        quest.locationName =
                Character.toUpperCase(locationName.charAt(p)) +
                locationName.substring(p + 1);

        text.append(locationName);
        text.append(". ");
        
        n = (int)(distances.length * Math.random());
        text.append(distances[n]);

        n += 2;
        quest.travelTime = 2 + n*n;
        
        quest.story = text.toString();
        
        return quest;
    }
 
    public static Quest makeTechnologyQuest()
    {
        String [] intros =
        {
            "Your creatures suggest",
            "Hard working creatures of yours propose",
            "A group of your creatures came up with the idea",
            "A clever minion of yours recommends",
        };

        Quest quest = new Quest();
        quest.seed = System.currentTimeMillis();
        
        StringBuilder text = new StringBuilder();
        int n;

        n = (int)(intros.length * Math.random());
        text.append(intros[n]);

        text.append(" to research metallurgy. They ask you ");
        text.append(" to assemble a party and spy on the smiths of ");

        String locationName = NameGenerator.makeLocationName(2, 5);
        quest.locationName = locationName;

        text.append(locationName);
        text.append(" to learn bronze working. ");
        
        n = (int)(distances.length * Math.random());
        text.append(distances[n]);
        n += 2;

        quest.travelTime = 2 + n*n;
        quest.findingDifficulty = 1;
        quest.treasureSize = 1; // Todo: Metallurgy level should go here
        
        quest.story = text.toString();
        
        return quest;
    }
}
