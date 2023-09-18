package impcity.game.species;

/**
 * Some species have requirements for rooms before they settle in a dungeon.
 * 
 * @author Hj. Malthaner
 */
public class RoomRequirements 
{
    public int lairs;
    public int farms;
    public int treasury;           // should this be gold reserves rather?
    public int libraries;
    public int forges;

    public RoomRequirements(
                int lairs,
                int farms,
                int treasury,
                int libraries,
                int workshops
            )
    {
        this.lairs = lairs;
        this.farms = farms;
        this.treasury = treasury;
        this.libraries = libraries;
        this.forges = workshops;
    }
}
