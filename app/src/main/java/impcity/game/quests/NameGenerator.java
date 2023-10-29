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

    private static String [] strangeSyllables =
    {
        "Ka", "Ti", "Ben", "Wa", "On", "Tu", "Be", "Daco", "Et", "Nam",
        "Fer", "Mi", "Do", "Pa", "Ki", "Li", "So", "Per", "Mut", "Bu",
        "Fu", "Klen", "Gram", "Fort", "Zing", "Lemo", "Orka", "Dera", "Aray", "Kan",
        "Malo", "Ter", "Walt", "Kass", "Dron", "Tefer", "Erret", "Tandu", "Emmo",
            "Krati", "Bul", "Baro", "Arbo", "Irwen", "Nitak", "Strel", "Baku", "Nami",
            "Lade", "Kab", "Chew", "Falg", "Ran", "Gelo", "Gruwin", "Fradon", "Eskrem",
            "Karduf", "Smoka", "Teraf", "Actel", "Zepnik", "Pellin", "Ahrer", "Ermut"
    };
    
    private static final String [] nordicFirstSyllables =
    {
        "Helm", "Fried", "Hart", "Diet", "Hein", "Ulf", "Giesel", "Andwara",
        "Gott", "Fran", "Fren", "Chlod", "Lud", "Stark", "Irm"
    };
            
    private static final String [] nordicLastSyllables =
    {
        "mut", "run", "rich", "bar", "bert", "wig", "gard" 
    };
            
    private static final String [] locationSyllables =
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


    private static boolean isVowel(char c)
    {
        return "aeiou".indexOf(c) >= 0;
    }


    private static String addVowels(String input)
    {
        int consonantRun = 0;

        for(int i = 0; i<input.length(); i++)
        {
            if(isVowel(input.charAt(i)))
            {
                consonantRun ++;
            }

            if(consonantRun == 4)
            {
                String fillers = "aeiou'";
                return input.substring(0, i-1) +
                        fillers.charAt((int)(Math.random() * fillers.length())) +
                        addVowels(input.substring(i-1));
            }
        }

        return input;
    }


    public static String makeGenericName(int syllableCount)
    {
        return makeName(syllableCount, syllables);
    }
    
    public static String makeAztekName(int syllableCount)
    {
        return makeName(syllableCount, aztekSyllables);
    }

    
    public static String makeNordicName()
    {
        String first = nordicFirstSyllables[(int)(Math.random() * nordicFirstSyllables.length)];

        // add a filler vowel?
        String filler;
        double chance = Math.random();
        if(chance < 0.1)
        {
            filler = "e";
        }
        else if(chance < 0.2)
        {
            filler = "a";
        }
        else if(chance < 0.3)
        {
            filler = "o";
        }
        else if(chance < 0.4)
        {
            filler = "u";
        }
        else if(chance < 0.5)
        {
            filler = "i";
        }
        else
        {
            filler = "";
        }
        
        String last = nordicLastSyllables[(int)(Math.random() * nordicLastSyllables.length)];
        
        return first + filler + last;
    }
        
    
    public static String makeStrangeName(int syllableCount)
    {
        StringBuilder buf = new StringBuilder();
        
        for(int i=0; i<syllableCount; i++)
        {
            String syllable = strangeSyllables[(int)(Math.random() * strangeSyllables.length)];
            buf.append(syllable);
            if(i < syllableCount - 1)
            {
                if(Math.random() < 0.4)
                {
                    buf.append("-");
                }
                else
                {
                    buf.append(" ");
                }
            }
        }
        
        buf.setCharAt(0, Character.toUpperCase(buf.charAt(0)));
        
        return buf.toString();
    }

    public static String makeRandomName(int syllableCount)
    {
        String result = "";

        int i = (int)(Math.random() * 4);
        switch(i)
        {
            case 0: result = makeGenericName(syllableCount);
                break;
            case 1: result = makeAztekName(syllableCount);
                break;
            case 2: result = makeStrangeName(Math.max(2, syllableCount - 1));
                break;
            case 3: result = makeNordicName();
                break;
        }

        return addVowels(result);
    }
    

    /**
     * Create a name for an in-game location.
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
