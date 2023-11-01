package impcity.game.quests;

import java.util.ArrayList;

/**
 *
 * @author hjm
 */
public class ArtifactGenerator 
{
    private static enum Of
    {
        LOCATION,
        PERSON,
        NAME        
    };
    
    private static class Artifact
    {
        public final String name;
        public final Of of;

        public Artifact(String name, Of of)
        {
            this.name = name;
            this.of = of;
        }
    };

    
    private static final String [] attribute = 
    {
        "",
        "ruined",
        "old",
        "ancient",
        "blessed",
        "radiant",
        "bewitched",
        "ancient",
        "holy",
        "unholy",
        "divine",
        "cursed",
        "haunted",
        "demonic",
        "angelic",
        "everdark",
        "timeless",
        "hexed",
        "beautiful",
        "luck bearing",
        "gorgonic",
        "weatherbeaten",
        "reinforced",
        "twinned",
        "warding",
        "colorful",
        "striped",
        "massive",
        "torn",
        "sunbleached",
        "eternal",
        "splendid",
        "shiny",
    };

    
    
    private static final ArrayList<Artifact> tier1 = new ArrayList<>();
    private static final ArrayList<Artifact> tier2 = new ArrayList<>();
    
    static
    {
        tier1.add(new Artifact("dried frog", Of.LOCATION));
        tier1.add(new Artifact("mummified cat", Of.LOCATION));
        tier1.add(new Artifact("linen cloth", Of.LOCATION));
        tier1.add(new Artifact("goat skin", Of.LOCATION));
        tier1.add(new Artifact("rabbit's paw", Of.LOCATION));
        tier1.add(new Artifact("preserved toe", Of.PERSON));
        tier1.add(new Artifact("petrified bones", Of.PERSON));
        tier1.add(new Artifact("carved pumpkin", Of.PERSON));
        tier1.add(new Artifact("mug", Of.PERSON));
        tier1.add(new Artifact("jar", Of.PERSON));
        tier1.add(new Artifact("urn", Of.PERSON));
        tier1.add(new Artifact("shoes", Of.PERSON));

        tier2.add(new Artifact("scarab", Of.LOCATION));
        tier2.add(new Artifact("skull", Of.PERSON));
        tier2.add(new Artifact("preserved eye", Of.PERSON));
        tier2.add(new Artifact("hair", Of.PERSON));
    }
    
    
    
    public static String makeArtifactName(int tier)
    {
        String att = attribute[(int)(Math.random() * attribute.length)];
        
        Artifact artifact = tier1.get((int)(Math.random() * tier1.size())); 
        
        String of = "";
        
        switch(artifact.of)
        {
            case LOCATION:
                of = " of " + NameGenerator.makeLocationName(2, 4);
                break;
            case PERSON:
                of = " of " + NameGenerator.makeRandomName(2 + (int)(Math.random()*3));
                break;
            case NAME:
                of = " '" + NameGenerator.makeRandomName(2 + (int)(Math.random()*3)) + "'";
                break;
        }
        
        if(att.length() > 0)
        {
            att += " ";
        }
        
        return "the " + att + artifact.name + of;
    }
    
}