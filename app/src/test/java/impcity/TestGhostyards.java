package impcity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.lwjgl.LWJGLException;

import impcity.game.ImpCity;
import impcity.game.ai.MobStats;
import impcity.game.map.Map;
import impcity.game.mobs.Mob;

public class TestGhostyards 
{
	@Test
	public void testGraveAllocation() throws LWJGLException, IOException
	{
		ImpCity game = new ImpCity();		
		Map map = new Map(128, 128);
		boolean ok;
		
		game.addGhostyardSquare(map, 0, 0);

		ok = game.isGrave(map, 0, 0);		
		assertFalse(ok, "Location 0, 0 must not be grave yet");

		game.allocateGrave(map, 0, 0);		
		
		ok = game.isGrave(map, 0, 0);		
		assertTrue(ok, "Location 0, 0 must be grave now");

		Mob mob = new Mob(0, 0, 0, Mob.KIND_DENIZEN, 0, 0, map, null, 0, null);
		
		game.populateGrave(map, mob, 0, 0);
	
		ok = game.isGrave(map, 0, 0);		
		assertTrue(ok, "Location 0, 0 must be grave now");
		
        int count = mob.stats.getCurrent(MobStats.GHOST_STEPS);
        assertEquals(10, count, "Mob should have ten attempts left to become a ghost");
        
    	game.turnGraveIntoLair(map, mob);

        boolean isGhost = mob.isGhost();        
        assertEquals(true, isGhost, "Mob should be a ghost now");
	}
}
