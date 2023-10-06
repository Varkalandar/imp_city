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
import impcity.game.map.Map;
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
    public static boolean showMapInfo;   // show debug info about map
    
    
    private final static int defaultButtonColor = 0xFFFFFFFF;
    private final static int selectedButtonColor = 0xFFFFDD99;
    // private final static int selectedButtonColor = 0xD099DDFF;
    private final static int disabledButtonColor = 0xFF555555;
    
    // private final static int menuBarColor = 0xFF776655;
    private final static int menuBarColor = 0xFFCCCCCC;
    
    private final Texture buttonBar;
    private final ImpCity game;
    private final PixFont fontLow; // low contrast

    private final Texture buttonText;
    private final Texture buttonDig;
    private final Texture buttonLair;
    private final Texture buttonFood;
    private final Texture buttonBook;
    private final Texture buttonTreasury;
    private final Texture buttonForge;
    private final Texture buttonWork;
    private final Texture buttonLab;
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
        this.fontLow = new PixFont("/font/humanistic_128_2");
        
        TextureCache textureCache = display.textureCache;
        
        // buttonBar = textureCache.loadTexture("/ui/menu_bar.png", true);
        buttonBar = textureCache.loadTexture("/ui/menu_bar_marble.png", true);
        buttonDig = textureCache.loadTexture("/ui/button_dig.png", true);
        buttonLair = textureCache.loadTexture("/ui/button_lair.png", true);
        buttonFood = textureCache.loadTexture("/ui/button_food.png", true);
        buttonBook = textureCache.loadTexture("/ui/button_library.png", true);
        buttonTreasury = textureCache.loadTexture("/ui/button_treasury.png", true);
        buttonForge = textureCache.loadTexture("/ui/button_forge.png", true);
        buttonWork = textureCache.loadTexture("/ui/button_work.png", true);
        buttonLab = textureCache.loadTexture("/ui/button_alchemy.png", true);
        buttonHeal = textureCache.loadTexture("/ui/button_healing.png", true);
        buttonDemolish = textureCache.loadTexture("/ui/button_demolish.png", true);
        buttonImp = textureCache.loadTexture("/ui/button_imp.png", true);
        
        buttonText = textureCache.loadTexture("/ui/button_text_short.png", true);
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
        int top = -8;
        
        // IsoDisplay.drawTile(buttonBar, left, 0, 0xFF54463D);
        IsoDisplay.drawTile(buttonBar, left, 0, menuBarColor);
        // IsoDisplay.drawTile(buttonBar, left, 0, 0xFFFFFFFF);

        drawShortButton("Rooms I", left+14, top+81, 
                tabSelected == TAB_ROOMS_I ? 0xFFFFDD99 : 0xFFDDDDDD,
                tabSelected == TAB_ROOMS_I ? selectedButtonColor : defaultButtonColor);
        
        drawShortButton("Rooms II", left+14, top+58, 
                tabSelected == TAB_ROOMS_II ? 0xFFFFDD99 : 0xFFDDDDDD,
                tabSelected == TAB_ROOMS_II ? selectedButtonColor : defaultButtonColor);
        
        drawShortButton("Spells", left+14, top+35, 
                tabSelected == TAB_SPELLS ? 0xFFFFDD99 : 0xFFDDDDDD,
                tabSelected == TAB_SPELLS ? selectedButtonColor : defaultButtonColor);
        
        drawShortButton("Furniture", left+14, top+12, 
                tabSelected == -1 ? 0xFFFFDD99 : 0xFFDDDDDD,
                tabSelected == -1 ? selectedButtonColor : defaultButtonColor);
        
        
        switch (tabSelected) 
        {
            case TAB_ROOMS_I:
                displayRooms1Tab(left-10, top);
                break;
            case TAB_ROOMS_II:
                displayRooms2Tab(left-10, top);
                break;
            case TAB_SPELLS:
                displaySpellsTab(left-10, top);
                break;
            default:
                break;
        }
            
        Mob keeper = game.world.mobs.get(game.getPlayerKey());

        // debug
        if(showMapInfo) 
        {
            display.font.drawStringScaled("Map pos: " + display.cursorI + ", " + display.cursorJ, 0xFFFFFFFF, 20, 600, 0.5);
            
            
            int item = keeper.gameMap.getItem(display.cursorI, display.cursorJ);
            int ino = item & Map.F_ITEM_MASK;
            String flags = (item & Map.F_DECO) == 0 ? "" : " Deco";
            flags += (item & Map.F_FLOOR_DECO) == 0 ? "" : " Floor";
            
            display.font.drawStringScaled("Item: " + ino + flags, 0xFFFFFFFF, 20, 580, 0.5);
        }
        
        int textLeft = left + 854;
        int textColor = 0xFFFFDD99;

        /*
        drawShadowText("Rookie", textColor, textLeft, 64, 0.25);
        drawShadowText(calcReputationDisplay(keeper), textColor, textLeft, 34, 0.25);
        drawShadowText("" + keeper.stats.getCurrent(KeeperStats.GOLD) + " Gold", textColor, textLeft, 4, 0.25);
        
        drawShadowText("" + Clock.days() + " days " + Clock.hour() + " hours", 0xFFDDDDDD, 
                display.displayWidth - 140, display.displayHeight - 30, 0.22);
        */
        
        drawMenuText("Rookie", textColor, textLeft, 73, 0.6);
        drawMenuText(calcReputationDisplay(keeper), textColor, textLeft, 52, 0.6);
        drawMenuText("" + keeper.stats.getCurrent(KeeperStats.GOLD) + " Gold", textColor, textLeft, 31, 0.6);
        drawMenuText("" + game.calcCurrentCreatureCount() + "/" + game.calcMaxCreatureCount() + " Creatures", textColor, textLeft, 10, 0.6);

        drawMenuText("" + Clock.days() + " days " + Clock.hour() + " hours", 0xFFDDDDDD, 
                display.displayWidth - 140, display.displayHeight - 30, 0.5);

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

            int width = (int)(fontLow.getStringWidth(message.message) * factor + 0.5);
            drawShadowText(message.message, message.color,
                    message.x - width/2, message.y + yoff, factor);

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
                    game.soundPlayer.play(Sounds.METAL_HIT, 0.2f, 1.0f);
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
    
    
    private void drawShortButton(String text, int x, int y, int textColor, int buttonColor)
    {
        IsoDisplay.drawTile(buttonText, x, y, buttonColor);
       
        /*
        float f = 0.16f;
        int width = (int)(fontLow.getStringWidth(text) * f + 0.5f);
        drawShadowText(text, textColor, x + (buttonText.image.getWidth() - width)/2, y+2, f);
        */
        
        float f = 0.5f;
        int width = (int)(display.font.getStringWidth(text) * f + 0.5f);
        drawMenuText(text, textColor, x + (buttonText.image.getWidth() - width)/2, y+4, f);
    }

    
    private void drawMenuText(String text, int color, int x, int y, double f)
    {
        PixFont font = display.font;        
        font.drawStringScaled(text, color, x, y, f);
    }
    
    
    public void drawShadowText(String text, int color, int x, int y, double f)
    {
        int shadow = 0x33000000;
        // PixFont font = fontLow;
        PixFont font = display.font; f *= 3.0;
        
        font.drawStringScaled(text, shadow, x+1, y, f);
        font.drawStringScaled(text, shadow, x, y+1, f);
        font.drawStringScaled(text, shadow, x-1, y, f);
        font.drawStringScaled(text, shadow, x, y-1, f);

        font.drawStringScaled(text, color, x, y, f);

    }

    public void drawBoxedShadowText(String text,
                                    int color, int left, int top, int width,
                                    int linespace, double factor)
    {
        int shadow = 0x33000000;
        fontLow.drawText(text, shadow, left+1, top, width, linespace, factor);
        fontLow.drawText(text, shadow, left, top+1, width, linespace, factor);
        fontLow.drawText(text, shadow, left-1, top, width, linespace, factor);
        fontLow.drawText(text, shadow, left, top-1, width, linespace, factor);

        fontLow.drawText(text, color, left, top, width, linespace, factor);
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

    /**
     * Calculate of the button is disabled, normal or selected
     * @return
     */
    private int calculateButtonColor(Tools tool)
    {
        Mob keeper = game.world.mobs.get(game.getPlayerKey());
        int research = keeper.stats.getCurrent(KeeperStats.RESEARCH);

        boolean enabled = true;
        int color;

        switch(tool)
        {
            case MAKE_FORGE:
                enabled = (research & KeeperStats.RESEARCH_FORGES) != 0;
                break;
            case MAKE_LAB:
                enabled = (research & KeeperStats.RESEARCH_LABS) != 0;
                break;
            case MAKE_HOSPITAL:
                enabled = (research & KeeperStats.RESEARCH_HEALING) != 0;
                break;
        }

        if(enabled) {
            if (Tools.selected == tool) {
                color = selectedButtonColor;
            } else {
                color = defaultButtonColor;
            }
        }
        else
        {
            color = disabledButtonColor;
        }

        return color;
    }

    private void displayRooms1Tab(int left, int top)
    {
        IsoDisplay.drawTile(buttonDig, left + 196, top + 16, calculateButtonColor(Tools.MARK_DIG));
        IsoDisplay.drawTile(buttonLair, left + 280, top + 16, calculateButtonColor(Tools.MAKE_LAIR));
        IsoDisplay.drawTile(buttonFood, left + 364, top + 16, calculateButtonColor(Tools.MAKE_FARM));
        IsoDisplay.drawTile(buttonBook, left + 448, top + 16, calculateButtonColor(Tools.MAKE_LIBRARY));
        IsoDisplay.drawTile(buttonLab, left + 532, top + 16, calculateButtonColor(Tools.MAKE_LAB));
        IsoDisplay.drawTile(buttonForge, left + 616, top + 16, calculateButtonColor(Tools.MAKE_FORGE));
        IsoDisplay.drawTile(buttonHeal, left + 700, top + 16, calculateButtonColor(Tools.MAKE_HOSPITAL));

        IsoDisplay.drawTile(buttonDemolish, left + 784, top + 16, calculateButtonColor(Tools.DEMOLISH));
        
        int tipY = 108;
        
        // Hajo: testing tooltips
        if(Mouse.getY() < 100)
        {
            int x = Mouse.getX();
            
            if(x > left + 196 && x < left + 196 + 80)
            {
                drawShadowText("Mark a block for digging", 0xFFFFFFFF, left + 196 - 120, tipY, 0.3);
            }
            else if(x > left + 280 && x < left + 280 + 80)
            {
                drawShadowText("Build lair space for your creatures", 0xFFFFFFFF, left + 280 - 170, tipY, 0.3);
            }
            else if(x > left + 364 && x < left + 364 + 80)
            {
                drawShadowText("Convert floor to farmland", 0xFFFFFFFF, left + 364 - 130, tipY, 0.3);
            }
            else if(x > left + 448 && x < left + 448 + 80)
            {
                drawShadowText("Set up a library", 0xFFFFFFFF, left + 448 - 70, tipY, 0.3);
            }
            else if(x > left + 532 && x < left + 532 + 80)
            {
                drawShadowText("Build a laboratory", 0xFFFFFFFF, left + 532 - 90, tipY, 0.3);
            }
            else if(x > left + 616 && x < left + 616 + 80)
            {
                drawShadowText("Create a forge", 0xFFFFFFFF, left + 616 - 70, tipY, 0.3);
            }
            else if(x > left + 700 && x < left + 700 + 80)
            {
                drawShadowText("Place a healing well", 0xFFFFFFFF, left + 700 - 110, tipY, 0.3);
            }
            else if(x > left + 784 && x < left + 784 + 80)
            {
                drawShadowText("Revert a room to empty space", 0xFFFFFFFF, left + 824 - 210, tipY, 0.3);
            }
        }
    }
    
    private void displayRooms2Tab(int left, int top)
    {
        IsoDisplay.drawTile(buttonDig, left + 196, top + 16, calculateButtonColor(Tools.MARK_DIG));
        IsoDisplay.drawTile(buttonTreasury, left + 532, top + 16, calculateButtonColor(Tools.MAKE_TREASURY));

        IsoDisplay.drawTile(buttonDemolish, left + 792, top + 16, calculateButtonColor(Tools.DEMOLISH));
        
        int tipY = 108;

        // Hajo: testing tooltips
        if(Mouse.getY() < 100)
        {
            int x = Mouse.getX();
            
            if(x > left + 196 && x < left + 196 + 80)
            {
                drawShadowText("Mark a block for digging", 0xFFFFFFFF, left + 196 - 120, tipY, 0.3);
            }
            else if(x > left + 532 && x < left + 532 + 80)
            {
                drawShadowText("Make a storage room", 0xFFFFFFFF, left + 532 - 90, tipY, 0.3);
            }
            else if(x > left + 784 && x < left + 784 + 80)
            {
                drawShadowText("Revert a room to empty space", 0xFFFFFFFF, left + 824 - 210, tipY, 0.3);
            }
        }
    }

    private void displaySpellsTab(int left, int top)
    {
        IsoDisplay.drawTile(buttonImp, left + 196, top + 16, calculateButtonColor(Tools.SPELL_IMP));

        int tipY = 108;

        // Hajo: testing tooltips
        if(Mouse.getY() < 100)
        {
            int x = Mouse.getX();
            
            if(x > left + 196 && x < left + 196 + 80)
            {
                drawShadowText("Spawn a new imp", 0xFFFFFFFF, left + 196 - 120, tipY, 0.3);
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
