package impcity.game.species;

/**
 * Some species have requirements for rooms before they settle in a dungeon.
 * 
 * @author Hj. Malthaner
 */
public class RoomRequirements 
{
    public final int lairs;
    public final int farms;
    public final int treasury;           // should this be gold reserves rather?
    public final int libraries;
    public final int forges;
    public final int labs;

    public RoomRequirements(
                int lairs,
                int farms,
                int treasury,
                int libraries,
                int forges,
                int labs
            )
    {
        this.lairs = lairs;
        this.farms = farms;
        this.treasury = treasury;
        this.libraries = libraries;
        this.forges = forges;
        this.labs = labs;
    }
}
