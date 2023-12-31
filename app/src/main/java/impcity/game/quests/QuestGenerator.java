package impcity.game.quests;

/**
 * Only treasure maps so far.
 * @author Hj. Malthaner
 */
public class QuestGenerator
{
    private static final NonRepetitiveRng locationOutdoorRng = 
            new NonRepetitiveRng(5, System.currentTimeMillis());
    
    private static final NonRepetitiveRng locationBuildingRng = 
            new NonRepetitiveRng(5, System.currentTimeMillis());
    
    
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
    
    private static final String [] areaLocations = 
    {
        "hidden behind {0} waterfall",
        "buried in {0} giant mole hill",
        "hidden in {0} sinkhole",
        "hidden in {0} group of bushes",
        "cached in {0} river bend",
        "stored in {0} tunnel",
        "stored in {0} dry well",
        "placed in {0} cave",
        "enscounded in {0} gorge",
        "hidden in {0} forest",
        "covert in {0} quarry",
        "hidden in {0} inactive volcano",
        "covert in {0} hollow tree trunk",
        "buried in {0} hilltop",
        "buried under {0} huge oak tree",
        "buried under {0} old linden tree",
        "buried under {0} big ash tree",
        "stashed under {0} sacred fig tree",
        "hidden under {0} statue",
        "stashed under {0} cairn",
        "hidden under {0} big rock",
        "hidden at the foot of {0} massive crag",
        "hidden at the foot of {0} obelisk",
        "buried near {0} scare crow",
        "buried near {0} obelisk",
        "stored near {0} ancient tree trunk",
    };
    
    private static final String [] buildingTypes =
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
        
      
    private static final String [] buildingHidingModes = 
    {
        "stashed", "hidden", "kept", "placed", "stored"
    };
    
    private static final String [] buildingMods = 
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
    
    private static final String [] treasureMods = 
    {
        "one or two",
        "a few",
        "some",
        "many",
        "numerous",
        "piles of"
    };

    private static final int [] treasureModsSize = 
    {
        1, 
        2, 
        3, 
        4,
        5,
        6,
    };

    private static final String [] distances =
    {
        "Your creatures can reach the place within some days.",
        "It'll probably take several days, maybe a week, to get there and back.",
        "Quite the journey, but within a few weeks your minions should be able to get there and back.",
        "It'll be a several weeks long expedition into barely known territory.",
        "It'll be a several months long ride into mostly unknown territory.",
    };

    private static final int [] distanceDays =
    {
            3,
            5,
            12,
            24,
            36,
    };

    
    private static final String [] intros =
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
    

    private static final String [] tellVars =
        {
            "tells of",
            "describes",
            "unveils",
            "shows the location of",
        };

    
    public static Quest makeTreasureQuest()
    {        
        String [] treasureVariations =
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
        
        n = (int)(treasureVariations.length * Math.random());
        text.append(treasureVariations[n]);
        quest.treasureName = treasureVariations[n].trim();
        calculateTreasureType(quest);
        
        boolean namedLocation = (Math.random() < 0.5);
        StringBuilder locationName = new StringBuilder();
        
        // make a location or a building quest?
        if(Math.random() < 0.5)
        {
            n = locationOutdoorRng.random(areaLocations.length);
            locationName.append(areaLocations[n]);
            quest.locationIsBuilding = false;
        }
        else
        {
            n = (int)(buildingHidingModes.length * Math.random());
            locationName.append(buildingHidingModes[n]);
            locationName.append(' ');
            
            n = (int)(buildingMods.length * Math.random());
            locationName.append(buildingMods[n]);
            locationName.append(' ');

            n = locationBuildingRng.random(buildingTypes.length);
            locationName.append(buildingTypes[n]);
            
            quest.locationIsBuilding = true;
        }        
                
        int p = locationName.indexOf("{0}");
        if(namedLocation)
        {
            locationName.replace(p, p+3, "the");
            locationName.append(" of ");
            locationName.append(NameGenerator.makeLocationName(2, 3));
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

        int travelDays = distanceDays[n];
        quest.travelTime = travelDays + (int)(Math.random() * travelDays);
        
        quest.story = text.toString();
        
        return quest;
    }

    
    public static Quest makeArtifactQuest() 
    {
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

        text.append( " note. The note ");
        
        n = (int)(tellVars.length * Math.random());
        text.append(tellVars[n]);
        text.append(' ');

        String artifact = ArtifactGenerator.makeArtifactName(0);
        
        quest.treasureSize = 1;        
        quest.treasureName = artifact;
        quest.treasureType = Quest.TT_ARTIFACT;
        
        boolean namedLocation = (Math.random() < 0.5);
        StringBuilder locationName = new StringBuilder();
        
        text.append(artifact);
        text.append(" being ");
        
        // make a location or a building quest?
        if(Math.random() < 0.2)
        {
            n = locationOutdoorRng.random(areaLocations.length);
            locationName.append(areaLocations[n]);
            quest.locationIsBuilding = false;
        }
        else
        {
            n = (int)(buildingHidingModes.length * Math.random());
            locationName.append(buildingHidingModes[n]);
            locationName.append(' ');
            
            n = (int)(buildingMods.length * Math.random());
            locationName.append(buildingMods[n]);
            locationName.append(' ');

            n = locationBuildingRng.random(buildingTypes.length);
            locationName.append(buildingTypes[n]);
            
            quest.locationIsBuilding = true;
        }        
                
        int p = locationName.indexOf("{0}");
        if(namedLocation)
        {
            locationName.replace(p, p+3, "the");
            locationName.append(" of ");
            locationName.append(NameGenerator.makeLocationName(2, 3));
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

        int travelDays = distanceDays[n];
        quest.travelTime = travelDays + (int)(Math.random() * travelDays);
        
        quest.story = text.toString();
        
        return quest;        
    }
    
    
    
    public static Quest makeTechnologyQuest()
    {
        String [] techIntros =
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

        n = (int)(techIntros.length * Math.random());
        text.append(techIntros[n]);

        text.append(" to research metallurgy. They ask you ");
        text.append(" to assemble a party and spy on the smiths of ");

        String locationName = NameGenerator.makeLocationName(2, 3);
        quest.locationName = locationName;

        text.append(locationName);
        text.append(" to learn bronze working. ");
        
        n = (int)(distances.length * Math.random());
        text.append(distances[n]);

        int travelDays = distanceDays[n];
        quest.travelTime = travelDays + (int)(Math.random() * travelDays);

        quest.findingDifficulty = 1;
        quest.treasureSize = 1; // Todo: Metallurgy level should go here
        
        quest.story = text.toString();
        
        return quest;
    }

    
    private static void calculateTreasureType(Quest quest) 
    {
        if(quest.treasureName.contains("gold"))
        {
            quest.treasureType = Quest.TT_GOLD;
        }
        else if(quest.treasureName.contains("silver"))
        {
            quest.treasureType = Quest.TT_SILVER;
        }
        else
        {
            quest.treasureType = Quest.TT_GEMS;
        }
    }

}
