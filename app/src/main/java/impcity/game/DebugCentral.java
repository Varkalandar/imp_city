package impcity.game;

import impcity.game.map.Map;
import impcity.game.quests.ArtifactGenerator;
import impcity.utils.StringUtils;

/**
 * Methods used to help in debugging.
 */
public class DebugCentral
{
    public static boolean debugFunctionsOn = false;


    public static void debugMakeArtifact(ImpCity game, Map map)
    {
        if (debugFunctionsOn)
        {
            String name = StringUtils.upperCaseFirst(ArtifactGenerator.makeArtifactName(0));
            int item = game.createArtifactForTreasure(name);
            map.setItem(game.mouseI, game.mouseJ, item);
        }
    }
}