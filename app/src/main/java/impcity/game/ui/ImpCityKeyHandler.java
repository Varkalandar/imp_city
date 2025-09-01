package impcity.game.ui;

import impcity.game.ImpCity;
import impcity.game.mobs.Mob;
import impcity.ogl.IsoDisplay;
import impcity.ui.KeyHandler;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.input.Keyboard;

/**
 *
 * @author Hj. Malthaner
 */
public class ImpCityKeyHandler implements KeyHandler
{
    private static final Logger logger = Logger.getLogger(ImpCityKeyHandler.class.getName());

    private final ImpCity game;
    private final IsoDisplay display;
    private final GameDisplay gameDisplay;

    public ImpCityKeyHandler(ImpCity game, IsoDisplay display, GameDisplay gameDisplay)
    {
        this.game = game;
        this.display = display;
        this.gameDisplay = gameDisplay;
    }
    
    @Override
    public void processKeyboard()
    {
        Mob player = game.world.mobs.get(game.getPlayerKey());
        boolean isCtrlDown = (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL));
        
        if(Keyboard.isKeyDown(Keyboard.KEY_LEFT) ||
           (Keyboard.isKeyDown(Keyboard.KEY_A) && !isCtrlDown))
        {
            display.centerX += 16;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT) ||
           (Keyboard.isKeyDown(Keyboard.KEY_D) && !isCtrlDown))
        {
            display.centerX -= 16;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_UP) ||
           (Keyboard.isKeyDown(Keyboard.KEY_W) && !isCtrlDown))
        {
            display.centerY -= 16;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_DOWN) ||
           (Keyboard.isKeyDown(Keyboard.KEY_S) && !isCtrlDown))
        {
            display.centerY += 16;
        }

        if(Keyboard.next())
        {
            if(Keyboard.getEventKeyState() == true && !Keyboard.isRepeatEvent())
            {
                if(Keyboard.getEventKey() == Keyboard.KEY_J)
                {
                    gameDisplay.debugShowJobQueue = !gameDisplay.debugShowJobQueue;
                }
                else if(Keyboard.getEventKey() == Keyboard.KEY_A && isCtrlDown)
                {
                    game.makeArtifactQuest();
                }
                else if(Keyboard.getEventKey() == Keyboard.KEY_T)
                {
                    game.makeTreasureQuest();
                }
                else if(Keyboard.getEventKey() == Keyboard.KEY_H)
                {
                    // game.makeTechnologyQuest();
                }
                else if(Keyboard.getEventKey() == Keyboard.KEY_E)
                {
                    gameDisplay.openExpeditionBook();
                }
                else if(Keyboard.getEventKey() == Keyboard.KEY_Q)
                {
                    gameDisplay.openQuestBook();
                }
                else if(Keyboard.getEventKey() == Keyboard.KEY_L && isCtrlDown)
                {
                    // game.load();
                    showLoadGameDialog();
                }
                else if(Keyboard.getEventKey() == Keyboard.KEY_S && isCtrlDown)
                {
                    // game.save();
                    showSaveGameDialog();
                }
                else if(Keyboard.getEventKey() == Keyboard.KEY_ESCAPE)
                {
                    // display.quit();
                    gameDisplay.showDialog(null);
                }
                else if(Keyboard.getEventKey() == Keyboard.KEY_RETURN)
                {
                    // collectString(null);
                }
                else if(Keyboard.getEventKey() == Keyboard.KEY_TAB)
                {
                    display.setShowItemNames(!display.getShowItemNames());
                }
                else if(Keyboard.getEventKey() == Keyboard.KEY_F1)
                {
                    GameDisplay.debugShowMapInfo = !GameDisplay.debugShowMapInfo;
                }
            }
        }
    }
    
    @Override
    public boolean collectString(StringBuilder buffer)
    {
        boolean done = false;
        
        if(Keyboard.next())
        {
            if(Keyboard.getEventKeyState() == true)
            {
                done = Keyboard.isKeyDown(Keyboard.KEY_RETURN);
                if(!done)
                {
                    char key = Keyboard.getEventCharacter();
                    buffer.append(key);
                    System.err.println("text=" + buffer);
                }
            }        
        }
    
        return done;
    }

    
    private void showSaveGameDialog() 
    {
        ArrayList <String> entries = new ArrayList<>(16);
        
        try
        {
            File directory = new File("savegame");
            File [] files = directory.listFiles((File dir, String name) -> name.startsWith("game_") && name.endsWith(".map"));
  
            for (int i=0; i<8; i++) 
            {
                String appendix = "(free slot)";
                
                if (i < files.length)
                {
                    FileTime time = 
                            Files.getLastModifiedTime(files[i].toPath(), LinkOption.NOFOLLOW_LINKS);
                    appendix = time.toString();
                }

                entries.add("Game #" + i + ": " + appendix);
            }
        }
        catch(IOException ioex)
        {
            logger.log(Level.SEVERE, "Error while listing saved games", ioex);
        }
    
        ListChoice choice = new ListChoice(display.textureCache, gameDisplay,
                                           500, 400, 
                                           "Save Game", entries,
                                           (int n) -> {
                                               gameDisplay.showDialog(null);
                                               game.save(n);
                                           });
        gameDisplay.showDialog(choice);
    }

    
    private void showLoadGameDialog() 
    {
        ArrayList <String> entries = new ArrayList<>(16);
        try
        {
            File directory = new File("savegame");
            File [] files = directory.listFiles((File dir, String name) -> name.startsWith("game_") && name.endsWith(".map"));
  
            int i = 0;
            for (File file : files) 
            {
                FileTime time = 
                        Files.getLastModifiedTime(file.toPath(), LinkOption.NOFOLLOW_LINKS);
                
                entries.add("Game #" + i + ": " + time.toString());
                i ++;
                if(i > 8) {
                    break;
                }
            }
        }
        catch(IOException ioex)
        {
            logger.log(Level.SEVERE, "Error while listing saved games", ioex);
        }

            /*
        entries.add("Game 1");
        entries.add("Game 2");
        entries.add("Game 3");
        entries.add("Game 4");
        entries.add("Game 5");
        entries.add("Game 6");
        entries.add("Game 7");
        entries.add("Game 8");
*/
        ListChoice choice = new ListChoice(display.textureCache, gameDisplay,
                                           500, 400,
                                           "Load Game", entries,
                                           (int n) -> {
                                               gameDisplay.showDialog(null);
                                               game.load(n);
                                           });
        gameDisplay.showDialog(choice);
    }
}