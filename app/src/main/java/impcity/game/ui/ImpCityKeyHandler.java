package impcity.game.ui;

import impcity.game.Features;
import impcity.game.ImpCity;
import impcity.game.quests.Quest;
import impcity.game.quests.QuestGenerator;
import impcity.game.mobs.Mob;
import impcity.ogl.IsoDisplay;
import impcity.ui.KeyHandler;
import org.lwjgl.input.Keyboard;

/**
 *
 * @author Hj. Malthaner
 */
public class ImpCityKeyHandler implements KeyHandler
{
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
                if(Keyboard.getEventKey() == Keyboard.KEY_I)
                {
                    // gameDisplay.showCreatureList = !gameDisplay.showCreatureList;
                }
                else if(Keyboard.getEventKey() == Keyboard.KEY_T)
                {
                    Quest quest = QuestGenerator.makeTreasureQuest();
                    QuestMessage questMessage = new QuestMessage(game, gameDisplay, display,
                                                                 600, 400, quest,
                                                                 "Discovery!",
                                                                 "[ Send Party ]", "[ Leave It ]");

                    MessageHook hookedMessage =
                            new MessageHook(Features.MESSAGE_TROPHY_QUEST,
                                            questMessage);

                    gameDisplay.addHookedMessage(hookedMessage);
                }
                else if(Keyboard.getEventKey() == Keyboard.KEY_H)
                {
                    Quest quest = QuestGenerator.makeTechnologyQuest();
                    QuestMessage questMessage = new QuestMessage(game, gameDisplay, display,
                                                                 600, 400, quest,
                                                                 "Humble Suggestion",
                                                                 "[ Assemble Party ]", "[ Leave It ]");

                    MessageHook hookedMessage =
                            new MessageHook(Features.MESSAGE_RESEARCH_QUEST,
                                            questMessage);

                    gameDisplay.addHookedMessage(hookedMessage);
                }
                else if(Keyboard.getEventKey() == Keyboard.KEY_L && isCtrlDown)
                {
                    game.load();
                }
                else if(Keyboard.getEventKey() == Keyboard.KEY_S && isCtrlDown)
                {
                    game.save();
                }
                else if(Keyboard.getEventKey() == Keyboard.KEY_ESCAPE)
                {
                    display.quit();
                }
                else if(Keyboard.getEventKey() == Keyboard.KEY_RETURN)
                {
                    // collectString(null);
                }
                else if(Keyboard.getEventKey() == Keyboard.KEY_F1)
                {
                    GameDisplay.showMapInfo = !GameDisplay.showMapInfo;
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
}