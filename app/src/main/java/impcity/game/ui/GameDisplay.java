package impcity.game.ui;

import impcity.game.ImpCity;
import impcity.game.Tools;
import impcity.game.KeeperStats;
import impcity.game.Sounds;
import java.io.IOException;
import java.util.ArrayList;
import impcity.game.Clock;
import impcity.game.Texture;
import impcity.game.TextureCache;
import impcity.game.mobs.Mob;
import impcity.ogl.IsoDisplay;
import impcity.ui.PixFont;
import impcity.ui.TimedMessage;
import org.lwjgl.input.Mouse;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;

/**
 *
 * @author Hj. Malthaner
 */
public class GameDisplay
{
    private final static int selectedButtonColor = 0xFFFFDD77;
    
    private final Texture buttonBar;
    private final ImpCity game;
    private final PixFont fontHigh; // high contrast
    private final PixFont fontLow; // low contrast

    private final Texture buttonText;
    private final Texture buttonDig;
    private final Texture buttonLair;
    private final Texture buttonFood;
    private final Texture buttonBook;
    private final Texture buttonTreasury;
    private final Texture buttonForge;
    private final Texture buttonWork;
    private final Texture buttonHeal;
    private final Texture buttonDemolish;
    private final Texture buttonImp;
    
    public static final int TAB_ROOMS_I = 1;
    public static final int TAB_ROOMS_II = 2;
    public static final int TAB_SPELLS = 3;
    
    private int tabSelected = TAB_ROOMS_I;
    
    private final ArrayList<TimedMessage> messages = new ArrayList<TimedMessage>();
    private final ArrayList<MessageHook> hookedMessageStack = new ArrayList<MessageHook>();
    private final IsoDisplay display;
    
    public UiDialog topDialog;
    
    
    
    public GameDisplay(ImpCity game, IsoDisplay display) throws IOException
    {
        this.game = game;
        this.display = display;
        // this.font = display.font;
        this.fontHigh = new PixFont("/font/humanistic_128b");
        this.fontLow = new PixFont("/font/humanistic_128bbl");
        
        TextureCache textureCache = display.textureCache;
        
        buttonBar = textureCache.loadTexture("/ui/main_bar_bg.jpg", false);
        buttonDig = textureCache.loadTexture("/ui/button_dig.png", true);
        buttonLair = textureCache.loadTexture("/ui/button_lair.png", true);
        buttonFood = textureCache.loadTexture("/ui/button_food.png", true);
        buttonBook = textureCache.loadTexture("/ui/button_library.png", true);
        buttonTreasury = textureCache.loadTexture("/ui/button_treasury.png", true);
        buttonForge = textureCache.loadTexture("/ui/button_forge.png", true);
        buttonWork = textureCache.loadTexture("/ui/button_work.png", true);
        buttonHeal = textureCache.loadTexture("/ui/button_healing.png", true);
        buttonDemolish = textureCache.loadTexture("/ui/button_demolish.png", true);
        buttonImp = textureCache.loadTexture("/ui/button_imp.png", true);
        
        buttonText = textureCache.loadTexture("/ui/button_text_short.png", true);
      
    }    
    
    public PixFont getFontHigh()
    {
        return fontHigh;
    }
    
    public PixFont getFontLow()
    {
        return fontLow;
    }

    public void addMessage(TimedMessage message)
    {
        messages.add(message);
    }

    public void selectTab(int tab)
    {
        tabSelected = tab;
    }
    
    public int getSelectedTab()
    {
        return tabSelected;
    }
    
    public void displayMore()
    {
        // System.err.println("view=" + display.getViewPosition() + " mouseI=" + display.cursorI + " mouseJ=" + display.cursorJ);
        
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        int left = calcMainUiBarLeft();
        
        IsoDisplay.drawTile(buttonBar, left, 0);
        
        IsoDisplay.drawTile(buttonText, left + 14, 14, tabSelected == -1 ? selectedButtonColor : 0xFFFFFFFF);
        IsoDisplay.drawTile(buttonText, left + 14, 37, tabSelected == TAB_SPELLS ? selectedButtonColor : 0xFFFFFFFF);
        IsoDisplay.drawTile(buttonText, left + 14, 60, tabSelected == TAB_ROOMS_II ? selectedButtonColor : 0xFFFFFFFF);
        IsoDisplay.drawTile(buttonText, left + 14, 83, tabSelected == TAB_ROOMS_I ? selectedButtonColor : 0xFFFFFFFF);
        
        fontLow.drawStringScaled("Rooms I", tabSelected == TAB_ROOMS_I ? 0xFFFFFF : 0, left+62, 82, 0.16);
        fontLow.drawStringScaled("Rooms II", tabSelected == TAB_ROOMS_II ? 0xFFFFFF : 0, left+60, 59, 0.16);
        fontLow.drawStringScaled("Spells", tabSelected == TAB_SPELLS ? 0xFFFFFF : 0, left+64, 36, 0.16);
        fontLow.drawStringScaled("?????", 0x000000, left+70, 13, 0.16);
        
        
        switch (tabSelected) 
        {
            case TAB_ROOMS_I:
                displayRooms1Tab(left);
                break;
            case TAB_ROOMS_II:
                displayRooms2Tab(left);
                break;
            case TAB_SPELLS:
                displaySpellsTab(left);
                break;
            default:
                break;
        }
            

        // debug
        // font.drawStringScaled("Mouse: " + display.cursorI + "," + display.cursorJ, 0xFFFFFF, 20, 600, 0.5);

        
        fontLow.drawStringScaled("" + Clock.days() + " days " + Clock.hour() + " hours", 0xFFF0E0, 20, 8, 0.18);
        
        Mob keeper = game.world.mobs.get(game.getPlayerKey());

        fontLow.drawStringScaled("Rookie", 0xFFF0E0, 20, 80, 0.28);
        fontLow.drawStringScaled(calcReputationDisplay(keeper), 0xFFF0E0, 20, 60, 0.18);
        fontLow.drawStringScaled("" + keeper.stats.getCurrent(KeeperStats.GOLD) + " GP", 0xFFF0E0, 20, 27, 0.28);
        
        if(topDialog != null)
        {
            topDialog.display((display.displayWidth - topDialog.width) / 2, 
                              (display.displayHeight - topDialog.height)/ 2);
        }
        
        int yoff;
        
        for(TimedMessage message : messages)
        {
            yoff = 8 + (((int)(Clock.time() - message.time)) >> 4);
            double factor = 0.5 + yoff/120.0;
            fontHigh.drawStringCentered(message.message, message.color, message.x, message.y + yoff, 0, factor);

            if(yoff > 100)
            {
                message.message = null;
                message.time = 0;
            }
        }
        
        purgeOutdatedMessages();
        
        yoff = 8;
        
        for(MessageHook hook : hookedMessageStack)
        {
            // int y = yoff;
            
            // Hajo: Is this message still falling?
            if(hook.yoff > yoff)
            {
                // y = hook.yoff;
                hook.yoff -= 8;
                
                // sitting now?
                if(hook.yoff <= yoff)
                {
                    game.soundPlayer.play(Sounds.METAL_HIT, 0.2f);
                }
            }
            else
            {
                // nope, it already sits
                // -> raise stack level.
                yoff += 32;
            }
            
            IsoDisplay.drawTile(display.textureCache.textures[hook.icon], display.displayWidth - 40, hook.yoff);
            
        }
    }

    public int calcMainUiBarLeft()
    {
        return (display.displayWidth-buttonBar.image.getWidth())/2;
    }
    
    private void purgeOutdatedMessages() 
    {
        for(int i=messages.size()-1; i >=0; i--)
        {
            TimedMessage message = messages.get(i);
            if(message.message == null || message.time == 0)
            {
                messages.remove(i);
            }
        }
    }

    private void displayRooms1Tab(int left)
    {
        IsoDisplay.drawTile(buttonDig, left + 196, 16, Tools.selected == Tools.MARK_DIG ? selectedButtonColor : 0xFFFFFFFF);
        IsoDisplay.drawTile(buttonLair, left + 280, 16, Tools.selected == Tools.MAKE_LAIR ? selectedButtonColor : 0xFFFFFFFF);
        IsoDisplay.drawTile(buttonFood, left + 364, 16, Tools.selected == Tools.MAKE_FARM ? selectedButtonColor : 0xFFFFFFFF);
        IsoDisplay.drawTile(buttonBook, left + 448, 16, Tools.selected == Tools.MAKE_LIBRARY ? selectedButtonColor : 0xFFFFFFFF);
        IsoDisplay.drawTile(buttonWork, left + 532, 16, Tools.selected == Tools.MAKE_TREASURY ? selectedButtonColor : 0xFFFFFFFF);
        IsoDisplay.drawTile(buttonForge, left + 616, 16, Tools.selected == Tools.MAKE_FORGE ? selectedButtonColor : 0xFFFFFFFF);
        IsoDisplay.drawTile(buttonHeal, left + 700, 16, Tools.selected == Tools.MAKE_HOSPITAL ? selectedButtonColor : 0xFFFFFFFF);

        IsoDisplay.drawTile(buttonDemolish, left + 824, 16, Tools.selected == Tools.DEMOLISH ? selectedButtonColor : 0xFFFFFFFF);
        
        // Hajo: testing tooltips
        if(Mouse.getY() < 100)
        {
            int x = Mouse.getX();
            
            if(x > left + 196 && x < left + 196 + 80)
            {
                fontLow.drawStringScaled("Mark a block for digging", 0xFFFFFFFF, left + 196 - 120, 124, 0.3);
            }
            else if(x > left + 280 && x < left + 280 + 80)
            {
                fontLow.drawStringScaled("Build lair space for your creatures", 0xFFFFFFFF, left + 280 - 170, 124, 0.3);
            }
            else if(x > left + 364 && x < left + 364 + 80)
            {
                fontLow.drawStringScaled("Convert floor to farmland", 0xFFFFFFFF, left + 364 - 130, 124, 0.3);
            }
            else if(x > left + 448 && x < left + 448 + 80)
            {
                fontLow.drawStringScaled("Set up a library", 0xFFFFFFFF, left + 448 - 70, 124, 0.3);
            }
            else if(x > left + 532 && x < left + 532 + 80)
            {
                fontLow.drawStringScaled("Make a workshop", 0xFFFFFFFF, left + 532 - 90, 124, 0.3);
            }
            else if(x > left + 616 && x < left + 616 + 80)
            {
                fontLow.drawStringScaled("Create a forge", 0xFFFFFFFF, left + 616 - 70, 124, 0.3);
            }
            else if(x > left + 700 && x < left + 700 + 80)
            {
                fontLow.drawStringScaled("Place a healing well", 0xFFFFFFFF, left + 700 - 110, 124, 0.3);
            }
            else if(x > left + 824 && x < left + 824 + 80)
            {
                fontLow.drawStringScaled("Revert a room to empty space", 0xFFFFFFFF, left + 824 - 210, 124, 0.3);
            }
        }
    }
    
    private void displayRooms2Tab(int left)
    {
        IsoDisplay.drawTile(buttonDig, left + 196, 16, Tools.selected == Tools.MARK_DIG ? selectedButtonColor : 0xFFFFFFFF);
        IsoDisplay.drawTile(buttonTreasury, left + 532, 16, Tools.selected == Tools.MAKE_TREASURY ? selectedButtonColor : 0xFFFFFFFF);

        IsoDisplay.drawTile(buttonDemolish, left + 824, 16, Tools.selected == Tools.DEMOLISH ? selectedButtonColor : 0xFFFFFFFF);
        
        // Hajo: testing tooltips
        if(Mouse.getY() < 100)
        {
            int x = Mouse.getX();
            
            if(x > left + 196 && x < left + 196 + 80)
            {
                fontLow.drawStringScaled("Mark a block for digging", 0xFFFFFFFF, left + 196 - 120, 124, 0.3);
            }
            else if(x > left + 532 && x < left + 532 + 80)
            {
                fontLow.drawStringScaled("Make a storage room", 0xFFFFFFFF, left + 532 - 90, 124, 0.3);
            }
            else if(x > left + 824 && x < left + 824 + 80)
            {
                fontLow.drawStringScaled("Revert a room to empty space", 0xFFFFFFFF, left + 824 - 210, 124, 0.3);
            }
        }
    }

    private void displaySpellsTab(int left)
    {
        IsoDisplay.drawTile(buttonImp, left + 196, 16, Tools.selected == Tools.SPELL_IMP ? selectedButtonColor : 0xFFFFFFFF);

        // Hajo: testing tooltips
        if(Mouse.getY() < 100)
        {
            int x = Mouse.getX();
            
            if(x > left + 196 && x < left + 196 + 80)
            {
                fontLow.drawStringScaled("Spawn a new imp", 0xFFFFFFFF, left + 196 - 120, 124, 0.3);
            }
        }
    }
    
    private String twoDigits(int v)
    {
        if(v > 9)
        {
            return "" + v;
        }
        else
        {
            return "0" + v;
        }
    }

    private String calcReputationDisplay(Mob keeper)
    {
        int rep = keeper.stats.getCurrent(KeeperStats.REPUTATION);
        
        String base;
        
        if(rep > 500)
        {
            base = "Superb";
        }
        else if(rep > 300)
        {
            base = "Attractive";
        }
        else if(rep > 100)
        {
            base = "Nice";
        }
        else if(rep > -100)
        {
            base = "Boring";
        }
        else if(rep > -300)
        {
            base = "Poor";
        }
        else if(rep > -500)
        {
            base = "Bad";
        }
        else if(rep > -700)
        {
            base = "Disgusting";
        }
        else
        {
            base = "Worst";
        }
        
        return base + " (" + rep + ")";
    }

    private static String calcLevelString(int level, int maxLevel)
    {
        String [] names = 
        {
"Rookie",
"Cave Digger",
"Flea Herder",

"Dungeon Leader",
"Creature Master"  
        };

        double f = (double)level/(double)maxLevel;
        
        return names[(int)(f * names.length)];
    
    }
    
    public final synchronized void showDialog(UiDialog dialog)
    {
        topDialog = dialog;
    }

    public void addHookedMessage(MessageHook hookedMessage) 
    {
        // Hajo: calaculate starting height - at least above the screen,
        // but with some distance to the highest former message
        
        int height = display.displayHeight;
        
        if(!hookedMessageStack.isEmpty())
        {
            int top = hookedMessageStack.get(hookedMessageStack.size()-1).yoff + 40;
            if(top > height) height = top;
        }

        hookedMessage.yoff = height;
        hookedMessageStack.add(hookedMessage);
    }

    void activateHookedMessage(int n) 
    {
        if(n >= 0 && n < hookedMessageStack.size())
        {
            MessageHook hookedMessage = hookedMessageStack.get(n);
            hookedMessageStack.remove(n);
            hookedMessage.activate(this);
        }
    }

}
