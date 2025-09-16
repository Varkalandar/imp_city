package impcity.game.ui;

import impcity.game.ImpCity;
import impcity.game.Tool;
import impcity.game.KeeperStats;
import impcity.game.Sounds;
import java.io.IOException;
import java.util.ArrayList;
import impcity.game.Clock;
import impcity.game.Texture;
import impcity.game.TextureCache;
import impcity.game.jobs.Job;
import impcity.game.map.Map;
import impcity.game.mobs.Mob;
import impcity.game.species.Species;
import impcity.ogl.IsoDisplay;
import impcity.ui.PixFont;
import impcity.ui.TimedMessage;
import java.util.List;
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
    public static boolean debugShowMapInfo;   // show debug info about map
    public static boolean debugShowJobQueue;   // show debug info about map
    private static int debugMobKey;
    
    private final static int defaultButtonColor = 0xFFCCCCCC;
    // private final static int selectedButtonColor = 0xFFFFDD99;
    private final static int selectedButtonColor = 0xFFB0F030;
    private final static int lackingManaButtonColor = 0xFFF0B030;
    private final static int disabledButtonColor = 0x70CCCCCC;
    private final static int toolTipColor = Colors.BRIGHT_SILVER_INK;

    private final CreatureBook creaturesBook;
    private final QuestBook questBook;
    private final ExpeditionBook expeditionBook;
    
    
    // private final Texture buttonBar;
    private final ImpCity game;
    private final PixFont fontLow; // script font, for in-game text

    private final Texture buttonCreatures;
    private final Texture buttonMap;
    private final Texture buttonExpedition;

    private final Texture buttonDig;
    private final Texture buttonLair;
    private final Texture buttonFood;
    private final Texture buttonBook;
    private final Texture buttonTreasury;
    private final Texture buttonForge;
    private final Texture buttonLab;
    private final Texture buttonHeal;
    private final Texture buttonGhost;
    private final Texture buttonDemolish;
    private final Texture buttonImp;
    private final Texture buttonHand;
    private final Texture buttonOreNode;
    
    public static final int TAB_NONE = 0;
    public static final int TAB_ROOMS = 1;
    public static final int TAB_SPELLS = 3;
    public static final int TAB_BOOKS = 4;
    
    private int tabSelected = TAB_ROOMS;
    
    private final ArrayList<TimedMessage> messages = new ArrayList<>();
    private final ArrayList<MessageHook> hookedMessageStack = new ArrayList<>();
    private final IsoDisplay display;
    
    public UiDialog topDialog;

    
    public GameDisplay(ImpCity game, IsoDisplay display) throws IOException
    {
        this.game = game;
        this.display = display;
        this.fontLow = new PixFont("/font/humanistic_128_2");
        
        this.creaturesBook = new CreatureBook(game, this, display);
        this.questBook = new QuestBook(display, this, game);
        this.expeditionBook = new ExpeditionBook(display, this, game);
        
        TextureCache textureCache = display.textureCache;
        
        buttonDig = textureCache.loadTexture("/ui/button_dig.png", true);
        buttonLair = textureCache.loadTexture("/ui/button_lair.png", true);
        buttonFood = textureCache.loadTexture("/ui/button_food.png", true);
        buttonBook = textureCache.loadTexture("/ui/button_library.png", true);
        buttonTreasury = textureCache.loadTexture("/ui/button_treasury.png", true);
        buttonForge = textureCache.loadTexture("/ui/button_forge.png", true);
        buttonLab = textureCache.loadTexture("/ui/button_alchemy.png", true);
        buttonHeal = textureCache.loadTexture("/ui/button_healing.png", true);
        buttonGhost = textureCache.loadTexture("/ui/button_ghostyard.png", true);
        buttonDemolish = textureCache.loadTexture("/ui/button_demolish.png", true);

        buttonImp = textureCache.loadTexture("/ui/button_imp.png", true);
        buttonHand = textureCache.loadTexture("/ui/button_hand.png", true);
        buttonOreNode = textureCache.loadTexture("/ui/button_ore_node.png", true);
        
        buttonCreatures = textureCache.loadTexture("/ui/button_creatures.png", true);
        buttonMap = textureCache.loadTexture("/ui/button_map.png", true);
        buttonExpedition = textureCache.loadTexture("/ui/button_expedition.png", true);
    }

    
    public IsoDisplay getDisplay()
    {
        return display;
    }
    
    
    public PixFont getFontLow()
    {
        return fontLow;
    }
    
    
    public PixFont getUiFont()
    {
        return display.font;
    }

    
    public void addMessage(TimedMessage message)
    {
        messages.add(message);
    }

    
    public void selectTab(int tab)
    {
        if(tabSelected == tab)
        {
            tabSelected = TAB_NONE;
        }
        else
        {
            tabSelected = tab;
        }
    }
    
    public int getSelectedTab()
    {
        return tabSelected;
    }
    
    public void displayMore()
    {
        // System.err.println("view=" + display.getViewPosition() + " mouseI=" + display.cursorI + " mouseJ=" + display.cursorJ);
        
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        IsoDisplay.fillRect(0, 26, 120, 70, 0x77000000);

        drawLeftButton("Rooms >", 120, 72, TAB_ROOMS);
        drawLeftButton("Spells >", 120, 52, TAB_SPELLS);
        drawLeftButton("Books >", 120, 32, TAB_BOOKS);
        
        switch (tabSelected) 
        {
            case TAB_ROOMS:
                displayRooms1Tab(132, 30);
                break;
            case TAB_SPELLS:
                displaySpellsTab(132, 30);
                break;
            case TAB_BOOKS:
                displayBooksTab(132, 30);
                break;
            default:
                break;
        }
            
        Mob keeper = game.world.mobs.get(game.getPlayerKey());

        // debug
        if(debugShowMapInfo) 
        {
            showMapInfo(keeper);
        }
        if(debugShowJobQueue) 
        {
            showJobQueue();
        }
        
        drawStatusBar(keeper);

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
            double factor = (0.5 + yoff/120.0) * message.factor;

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
        
        yoff = 0;
        
        for(MessageHook hook : hookedMessageStack)
        {
            // Hajo: Is this message still falling?
            if(hook.yoff > yoff)
            {
                // y = hook.yoff;
                hook.yoff -= 8;
                
                // sitting now?
                if(hook.yoff <= yoff)
                {
                    hook.yoff = yoff;
                    game.soundPlayer.play(Sounds.METAL_HIT, 0.2f, 1.0f);
                }
            }
            else
            {
                // nope, it already sits
                // -> raise stack level.
                yoff += 64;
            }
            
            IsoDisplay.drawTile(display.textureCache.textures[hook.icon], display.displayWidth - 68, hook.yoff + 24);
        }
    }

    private void drawStatusBar(Mob keeper)
    {
        int textLeft = (display.displayWidth - 900) / 2;
        int textColor = Colors.BRIGHT_GOLD_INK;

        IsoDisplay.fillRect(0, 0, display.displayWidth, 24, 0x77000000);

        drawMenuText("Life " + keeper.stats.getCurrent(KeeperStats.LIFE) + "/" + keeper.stats.getMax(KeeperStats.LIFE), 
        		     textColor, textLeft, 2, 0.6);

        drawMenuText("Mana " + keeper.stats.getCurrent(KeeperStats.MANA) + "/" + keeper.stats.getMax(KeeperStats.MANA), 
        		     textColor, textLeft + 170, 2, 0.6);
        
        drawMenuText(calcReputationDisplay(keeper), textColor, textLeft + 340, 2, 0.6);
        drawMenuText("" + keeper.stats.getMax(KeeperStats.COINS) + " Gold, " +
                        keeper.stats.getCurrent(KeeperStats.COINS) + " Silver, " +
                        keeper.stats.getMin(KeeperStats.COINS) + " Copper",
                        textColor, textLeft + 480, 2, 0.6);
        drawMenuText("" + game.calcCurrentCreatureCount() + "/" + game.calcMaxCreatureCount() + " Creatures",
                        textColor, textLeft + 810, 2, 0.6);
    }


    public void drawMenuText(String text, int color, int x, int y, double f)
    {
        PixFont font = display.font;        
        font.drawStringScaled(text, color, x, y, f);
    }
    
    
    public void drawShadowText(String text, int color, int x, int y, double f)
    {
        int shadow = 0x33000000;
        PixFont font = fontLow;
        // PixFont font = display.font; f *= 3.0;
        
        font.drawStringScaled(text, shadow, x+1, y, f);
        font.drawStringScaled(text, shadow, x, y+1, f);
        font.drawStringScaled(text, shadow, x-1, y, f);
        font.drawStringScaled(text, shadow, x, y-1, f);

        font.drawStringScaled(text, color, x, y, f);

    }

    public int drawBoxedShadowText(String text,
                                    int color, int left, int top, int width,
                                    int linespace, double factor)
    {
        int shadow = 0x33000000;
        fontLow.drawText(text, shadow, left+1, top, width, linespace, factor);
        fontLow.drawText(text, shadow, left, top+1, width, linespace, factor);
        fontLow.drawText(text, shadow, left-1, top, width, linespace, factor);
        fontLow.drawText(text, shadow, left, top-1, width, linespace, factor);

        int y = fontLow.drawText(text, color, left, top, width, linespace, factor);
        return y;
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
    private int calculateButtonColor(Tool tool)
    {
        Mob keeper = game.world.mobs.get(game.getPlayerKey());
        int research = keeper.stats.getCurrent(KeeperStats.RESEARCH);

        boolean costCovered = true;
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
            case MARK_DIG:
                enabled = true;
                costCovered = keeper.stats.getCurrent(KeeperStats.MANA) >= Tool.MARK_DIG.COST_MANA;
                break;
            case SPELL_IMP:
                enabled = true;
                int imps = game.countMobs(Species.IMPS_BASE);
                int cost = Math.max(1, imps - 3) * Tool.SPELL_IMP.COST_MANA;
                costCovered = keeper.stats.getCurrent(KeeperStats.MANA) >= cost;
                break;
        }

        if(enabled) {
            if (Tool.selected == tool) {
                if (costCovered) {
                    color = selectedButtonColor;
                }
                else {
                    color = lackingManaButtonColor;
                }
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

    
    int calculateTabButtonNumber(int x, int y)
    {
        int n = -1; // default - no button was hit
        
        if(y >= 30 && y <= 90)
        {
            n = x - 132 - 5; // left margin - half button gap
            n /= 70; // button width
        }

        return n;
    }
    
    
    private void displayRooms1Tab(int left, int top)
    {
        IsoDisplay.fillRect(122, 26, 720, 70, 0x77000000);
        
        IsoDisplay.drawTile(buttonDig, left + 0, top, 60, 60, calculateButtonColor(Tool.MARK_DIG));
        IsoDisplay.drawTile(buttonLair, left + 70, top, 60, 60, calculateButtonColor(Tool.MAKE_LAIR));
        IsoDisplay.drawTile(buttonFood, left + 140, top, 60, 60, calculateButtonColor(Tool.MAKE_FARM));
        IsoDisplay.drawTile(buttonBook, left + 210, top, 60, 60, calculateButtonColor(Tool.MAKE_LIBRARY));
        IsoDisplay.drawTile(buttonTreasury, left + 280, top, 60, 60, calculateButtonColor(Tool.MAKE_TREASURY));
        IsoDisplay.drawTile(buttonLab, left + 350, top, 60, 60, calculateButtonColor(Tool.MAKE_LAB));
        IsoDisplay.drawTile(buttonForge, left + 420, top, 60, 60, calculateButtonColor(Tool.MAKE_FORGE));
        IsoDisplay.drawTile(buttonHeal, left + 490, top, 60, 60, calculateButtonColor(Tool.MAKE_HOSPITAL));
        IsoDisplay.drawTile(buttonGhost, left + 560, top, 60, 60, calculateButtonColor(Tool.MAKE_GHOSTYARD));

        IsoDisplay.drawTile(buttonDemolish, left + 640, top, 60, 60, calculateButtonColor(Tool.DEMOLISH));
        
        int tipY = 112;
        int n = calculateTabButtonNumber(Mouse.getX(), Mouse.getY());

        if(n == 0)
        {
            drawMenuText(Tool.MARK_DIG.UI_DESCRIPTION, toolTipColor, 70, tipY, 0.6);
            drawMenuText("Cost: " + Tool.MARK_DIG.COST_MANA + " mana", toolTipColor, 70, tipY-18, 0.5);
        }
        else if(n == 1)
        {
            drawMenuText(Tool.MAKE_LAIR.UI_DESCRIPTION, toolTipColor, 140, tipY, 0.6);
            drawMenuText("Upkeep: " + KeeperStats.MANA_LAIR_COST + " mana/hour", toolTipColor, 140, tipY-18, 0.5);
        }
        else if(n == 2)
        {
            drawMenuText(Tool.MAKE_FARM.UI_DESCRIPTION, toolTipColor, 210, tipY, 0.6);
            drawMenuText("Upkeep: " + KeeperStats.MANA_FARMLAND_COST + " mana/hour", toolTipColor, 210, tipY-18, 0.5);
        }
        else if(n == 3)
        {
            drawMenuText(Tool.MAKE_LIBRARY.UI_DESCRIPTION, toolTipColor, 280, tipY, 0.6);
            drawMenuText("Upkeep: " + KeeperStats.MANA_LIBRARY_COST + " mana/hour", toolTipColor, 280, tipY-18, 0.5);
        }
        else if(n == 4)
        {
            drawMenuText(Tool.MAKE_TREASURY.UI_DESCRIPTION, toolTipColor, 350, tipY, 0.6);
            drawMenuText("Upkeep: " + KeeperStats.MANA_TREASURY_COST + " mana/hour", toolTipColor, 350, tipY-18, 0.5);
        }
        else if(n == 5)
        {
            drawMenuText(Tool.MAKE_LAB.UI_DESCRIPTION, toolTipColor, 440, tipY, 0.6);
            drawMenuText("Cost: 1 Copper Upkeep: " + KeeperStats.MANA_LABORATORY_COST + " mana/hour", toolTipColor, 440, tipY-18, 0.5);
        }
        else if(n == 6)
        {
            drawMenuText(Tool.MAKE_FORGE.UI_DESCRIPTION, toolTipColor, 490, tipY, 0.6);
            drawMenuText("Upkeep: " + KeeperStats.MANA_FORGE_COST + " mana/hour", toolTipColor, 490, tipY-18, 0.5);
        }
        else if(n == 7)
        {
            drawMenuText(Tool.MAKE_HOSPITAL.UI_DESCRIPTION, toolTipColor, 560, tipY, 0.6);
            drawMenuText("Cost: 5 Copper Upkeep: " + KeeperStats.MANA_LABORATORY_COST + " mana/hour", toolTipColor, 560, tipY-18, 0.5);
        }
        else if(n == 8)
        {
            drawMenuText(Tool.MAKE_GHOSTYARD.UI_DESCRIPTION, toolTipColor, 640, tipY, 0.6);
            drawMenuText("Cost: n/a Upkeep: " + KeeperStats.MANA_GHOSTYARD_COST + " mana/hour", toolTipColor, 640, tipY-18, 0.5);
        }
        else if(n == 9)
        {
            drawMenuText(Tool.DEMOLISH.UI_DESCRIPTION, toolTipColor, 720, tipY, 0.6);
        }
    }
    

    private void displaySpellsTab(int left, int top)
    {
        IsoDisplay.fillRect(122, 26, 650, 70, 0x77000000);
        
        IsoDisplay.drawTile(buttonImp, left + 0, top, 60, 60, calculateButtonColor(Tool.SPELL_IMP));
        IsoDisplay.drawTile(buttonHand, left + 70, top, 60, 60, calculateButtonColor(Tool.SPELL_IMP));
        IsoDisplay.drawTile(buttonOreNode, left + 140, top, 60, 60, calculateButtonColor(Tool.SPELL_PLACE_RESOURCE));

        int tipY = 108;
        int n = calculateTabButtonNumber(Mouse.getX(), Mouse.getY());

        if(n == 0)
        {
            int imps = game.countMobs(Species.IMPS_BASE);
            int cost = Math.max(1, imps - 3) * Tool.SPELL_IMP.COST_MANA;
            drawMenuText(Tool.SPELL_IMP.UI_DESCRIPTION, toolTipColor, 90, tipY, 0.6);
            drawMenuText("Cost: " + cost + " mana", toolTipColor, 90, tipY-18, 0.4);
        }
        else if(n == 1)
        {
            drawMenuText(Tool.SPELL_GRAB.UI_DESCRIPTION, toolTipColor, 90 + 70, tipY, 0.6);
            drawMenuText("Cost: " + Tool.SPELL_GRAB.COST_MANA + " mana", toolTipColor, 90 + 70, tipY-18, 0.4);
        }
        else if(n == 2)
        {
            drawMenuText(Tool.SPELL_PLACE_RESOURCE.UI_DESCRIPTION, toolTipColor, 90 + 140, tipY, 0.6);
            drawMenuText("Cost: " + Tool.SPELL_PLACE_RESOURCE.COST_MANA + " mana", toolTipColor, 90 + 140, tipY-18, 0.4);
        }
        else if(n == 3)
        {
            drawMenuText(Tool.SPELL_PLACE_DECORATION.UI_DESCRIPTION, toolTipColor, 90 + 140, tipY, 0.6);
            drawMenuText("Cost: " + Tool.SPELL_PLACE_DECORATION.COST_MANA + " mana", toolTipColor, 90 + 140, tipY-18, 0.4);
        }
    }

    
    private void displayBooksTab(int left, int top)
    {
        IsoDisplay.fillRect(122, 26, 650, 70, 0x77000000);
        
        IsoDisplay.drawTile(buttonCreatures, left + 0, top, 60, 60, calculateButtonColor(Tool.BOOK_CREATURES));
        IsoDisplay.drawTile(buttonMap, left + 70, top, 60, 60, calculateButtonColor(Tool.BOOK_QUESTS));
        IsoDisplay.drawTile(buttonExpedition, left + 140, top, 60, 60, calculateButtonColor(Tool.BOOK_EXPEDITION));

        int tipY = 108;
        int n = calculateTabButtonNumber(Mouse.getX(), Mouse.getY());

        if(n == 0)
        {
            drawMenuText(Tool.BOOK_CREATURES.UI_DESCRIPTION, toolTipColor, 70, tipY, 0.6);
        }
        else if(n == 1)
        {
            drawMenuText(Tool.BOOK_QUESTS.UI_DESCRIPTION, toolTipColor, 140, tipY, 0.6);
        }
        else if(n == 2)
        {
            drawMenuText(Tool.BOOK_EXPEDITION.UI_DESCRIPTION, toolTipColor, 210, tipY, 0.6);
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
            int top = hookedMessageStack.get(hookedMessageStack.size()-1).yoff + 80;
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


    void openCreaturesBook() 
    {
        showDialog(creaturesBook);
    }


    void openQuestBook() 
    {
        showDialog(questBook);
    }


    void openExpeditionBook() 
    {
        showDialog(expeditionBook);
    }


    private void drawLeftButton(String label, int x, int y, int tab) 
    {
        PixFont font = getUiFont();
        int bw = (int)(font.getStringWidth(label) * 0.6);
        int color = tabSelected == tab ? Colors.BRIGHT_GOLD_INK : Colors.DIM_GOLD_INK;

        font.drawStringScaled(label, color, x - bw - 4, y, 0.6);
    }
    
    
    private void showMapInfo(Mob keeper)
    {
        display.font.drawStringScaled("Map pos: " + display.cursorI + ", " + display.cursorJ, 0xFFFFFFFF, 20, 600, 0.5);

        int item = keeper.gameMap.getItem(display.cursorI, display.cursorJ);
        int ino = item & Map.F_IDENT_MASK;
        String flags = (item & Map.F_ITEM) == 0 ? "" : " Item";
        flags += (item & Map.F_FLOOR_DECO) == 0 ? "" : " Floor";

        display.font.drawStringScaled("Item: " + ino + flags, 0xFFFFFFFF, 20, 580, 0.5);

        int mk;
        if(debugMobKey > 0)
        {
            mk = debugMobKey;
        }
        else
        {
            mk = keeper.gameMap.getMob(display.cursorI, display.cursorJ);
        }
        
        if(mk > 0)
        {
            display.font.drawStringScaled("Mob: " + mk + " " + game.world.mobs.get(mk).getAi(), 0xFFFFFFFF, 20, 560, 0.5);
        }        
    }
    
    private void showJobQueue()
    {
        List <Job> allJobs = game.jobQueue.getAllJobs();
        int yoff = 0;
        
        for(Job job : allJobs)
        {
            drawMenuText(job.toString(), Colors.WHITE, 200, 500-yoff, 0.4);
            yoff += 20;
        }
    }

    void debugCatchMob() 
    {
        Mob player = game.world.mobs.get(game.getPlayerKey());
        Map map = player.gameMap;        
        
        debugMobKey = map.getMob(display.cursorI, display.cursorJ);
    }
}
