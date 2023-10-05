package impcity.game.species;

import impcity.game.ai.JobPreference;
import impcity.game.mobs.*;

/**
 * Description of the abilities of a monster or animal species
 * 
 * @author Hj. Malthaner
 */
public class SpeciesDescription
{
    public final String name;
    /** 
     * Species size measured in map sub-grid units 
     */
    public final int size;
    
    /**
     * Base index into the species image tables
     */
    public final int baseImage;
    
    // Image to show if this creature is asleep
    public final int sleepImage;
    
    // are there additional display choices for this species?
    // Basic species have 8 unanimated directional frames
    public int animationSteps = 0;
    public boolean hasSittingPoses = false;
    public boolean hasLayingPoses = false;
    
    public final int lair;
    public final int lairSize;

    public final MovementPattern move;
    public final int speed;
    public final int startingSound;

    public final RoomRequirements roomRequirements;
    
    // Other species specific data here

    public final int intelligence;
    public final int stealth;
    public final int combat;
    public final int carry;

    public final JobPreference jobPreference;

    public SpeciesDescription(String name,
                              int size,
                              int baseImage,
                              int sleepImage,
                              int lair,
                              int lairSize,
                              MovementPattern move,
                              int speed,
                              int startingSound,
                              RoomRequirements roomRequirements,
                              int intelligence,
                              int combat,
                              int stealth,
                              int carry,
                              JobPreference jobPreference
            )
    {
            this.name = name;
            this.size = size;
            this.baseImage = baseImage;
            this.sleepImage = sleepImage;
            this.lair = lair;
            this.lairSize = lairSize;
            this.move = move;
            this.speed = speed;
            this.startingSound = startingSound;
            this.roomRequirements = roomRequirements;
            this.intelligence = intelligence;
            this.combat = combat;
            this.stealth = stealth;
            this.carry = carry;
            this.jobPreference = jobPreference;
    }	
}