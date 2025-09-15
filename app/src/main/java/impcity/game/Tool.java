package impcity.game;

/**
 * 
 * @author Hj. Malthaner
 */
public enum Tool 
{
    MARK_DIG("Mark a block for digging", 0, 50),
    MAKE_LAIR("Build lair space for your creatures", 0, 0),
    MAKE_FARM("Convert floor to farmland", 0, 0),
    MAKE_LIBRARY("Set up a library", 0, 0),
    MAKE_FORGE("Create a forge", 0, 0),
    MAKE_LAB("Build a laboratory", 0, 0),
    MAKE_HOSPITAL("Place a healing well", 0, 0),
    MAKE_TREASURY("Make a treasury", 0, 0),
    MAKE_GHOSTYARD("Dig a ghostyard", 0, 0),
    DEMOLISH("Revert a room to empty space", 0, 0),
    
    SPELL_IMP("Spawn a new imp", 0, 50),
    SPELL_GRAB("Grab an item", 0, 10),
    SPELL_PLACE_RESOURCE("Place a resource node", 0, 100),
    SPELL_PLACE_DECORATION("Place a decoration item", 0, 10),
    BOOK_CREATURES("Open creature book", 0, 0),
    BOOK_QUESTS("Open quest location list", 0, 0),
    BOOK_EXPEDITION("Open expeditions book", 0, 0);

    public static Tool selected;
    public static int parameter;
    
    public final String UI_DESCRIPTION;
    public final int COST_COPPER;      
    public final int COST_MANA;      
   
    
    Tool(String description, int costCopper, int costMana)
    {
        this.UI_DESCRIPTION = description;
        this.COST_COPPER = costCopper;
        this.COST_MANA = costMana;
    }
}
