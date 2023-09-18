package impcity.game.quests;

/**
 *
 * @author Hj. Malthaner
 */
public class NameGenerator
{
    /** list of human name syllables */
    private static String [] syllables =
    {
        "el", "ha", "pa", "ul", "ns", "geo", "rge", "jo", "na", "tan", "pe", "ter",
        "cha", "erl", "ie", "au", "fr", "ed", "je", "eff", "rey", "li", "sa", "an", "ni",
        "ka", "tra", "cle", "opa", "am", "me", "lie", "sha", "sho", "ver", "oni", "con",
        "nie", "bin", "tt", "st", "mon", "ca", "ky", "gre", "zy", "ce", "tho",
    };
    
    private static String [] aztekSyllables =
    {
        "teo", "ti", "hu", "la", "atl", "an", "que", "zal", "co", "po",
        "ca", "te", "pe", "tl", "tli", "tz"
    };

    private static String [] asianSyllables =
    {
        "Ho", "Chi", "Minh", "Deng", "Xiao", "Ping", "Be", "Jing", "Mao", "Tse", 
        "Tung", "Mi", "Do", "Pa", "Xi", "Li", "Lang", "Biao", "Chang", "Bu", 
        "Cheng", "Gao", "Guan", "Han", "Ling", "Shui", "Sun", "Jin", "Gis", "Kan",
        "Feng",
    };
    
    private static String [] locationSyllables =
    {
        "er", "dro", "za", "en", "in", "un", "ko", "k'la", "me",
        "fra", "cur", "uz", "mon", "gor", "keif", "zir", "con",
        "mac", "ume", "lon", "jar", "def", "car", "bal", "war",
        "taz", "zee", "plu", "vol", "l'th", "cth", "a'k", "r't"
    };
    

    public static String makeName(int syllableCount, String [] syllableTable)
    {
        StringBuilder buf = new StringBuilder();
        
        for(int i=0; i<syllableCount; i++)
        {
            String syllable = syllableTable[(int)(Math.random()*syllableTable.length)];
            buf.append(syllable);
        }
        
        buf.setCharAt(0, Character.toUpperCase(buf.charAt(0)));
        
        return buf.toString();
    }

    public static String makeGenericName(int syllableCount)
    {
        return makeName(syllableCount, syllables);
    }
    
    public static String makeAztekName(int syllableCount)
    {
        return makeName(syllableCount, aztekSyllables);
    }

    public static String makeAsianName(int syllableCount)
    {
        StringBuilder buf = new StringBuilder();
        
        for(int i=0; i<syllableCount; i++)
        {
            String syllable = asianSyllables[(int)(Math.random()*asianSyllables.length)];
            buf.append(syllable);
            if(i < syllableCount - 1)
            {
                buf.append("-");
            }
        }
        
        buf.setCharAt(0, Character.toUpperCase(buf.charAt(0)));
        
        return buf.toString();
    }

    public static String makeRandomName(int syllableCount)
    {
        int i = (int)(Math.random() * 3);
        switch(i)
        {
            case 0: return makeGenericName(syllableCount);
            case 1: return makeAztekName(syllableCount);
            case 2: return makeAsianName(syllableCount);
        }
        return "";
    }

    /**
     * Create a name for a in-game location.
     * 
     * @param min Minimum syllable count for name
     * @param additional Up to this many syllables with be randomly appended
     * @return The created name.
     */
    public static String makeLocationName(int min, int additional)
    {
        int n = min + (int)(Math.random() * additional);
        
        StringBuilder builder = new StringBuilder();
        
        for(int i=0; i<n; i++)
        {
            builder.append(locationSyllables[(int)(Math.random() * locationSyllables.length)]);
        }
        
        char first = builder.charAt(0);
        builder.setCharAt(0, Character.toUpperCase(first));
        
        return builder.toString();
    }

}
