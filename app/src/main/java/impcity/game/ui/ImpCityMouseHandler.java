package impcity.game.ui;

import impcity.game.*;
import impcity.game.species.Species;
import impcity.game.ai.WayPathSource;
import impcity.game.jobs.JobExcavate;
import impcity.game.jobs.JobMining;
import impcity.game.jobs.JobQueue;
import impcity.game.map.LocationCallback;
import impcity.game.processables.FarmSquare;
import java.awt.Point;
import java.util.logging.Level;
import java.util.logging.Logger;

import impcity.game.map.Map;
import impcity.game.mobs.Mob;
import impcity.oal.SoundPlayer;
import impcity.ogl.IsoDisplay;
import impcity.ui.MouseHandler;
import impcity.ui.MousePointerBitmap;
import impcity.ui.TimedMessage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;


/**
 *
 * @author Hj. Malthaner
 */
public class ImpCityMouseHandler implements MouseHandler
{
    public static final Logger LOG = Logger.getLogger(ImpCityMouseHandler.class.getName());
    
    private boolean lastButtonState = false;
    private final ImpCity game;
    private final IsoDisplay display;
    private final SoundPlayer soundPlayer;

    private final GameDisplay gameDisplay;
    private int buttonPressed;
    private int buttonReleased;
    
    private int dragStartX, dragStartY;
    private int dragStartMx, dragStartMy;
    private boolean dragging = false;
    
    /**
     * The item currently grabbed by the player (on cursor)
     */
    public int grabbedItem;

    
    public ImpCityMouseHandler(ImpCity game, 
                               GameDisplay gameDisplay,
                               IsoDisplay display,
                               SoundPlayer soundPlayer)
    {
        this.game = game;
        this.gameDisplay = gameDisplay;
        this.display = display;
        this.soundPlayer = soundPlayer;

        Tool.selected = Tool.MARK_DIG;
        setMousePointer(display.textureCache.textures[Features.CURSOR_HAND]);
    }
    
    
    @Override
    public void processMouse()
    {
        int mx = (Mouse.getX() - display.centerX - 108);
        int my = (Mouse.getY() - display.centerY - 108) * 2;

        int mmi = -mx - my;
        int mmj = mx - my;

        // System.err.println("mmi = " + mmi + " mmj = " + mmj);

        game.mouseI = mmi * Map.SUB / 216;
        game.mouseJ = mmj * Map.SUB / 216;

        // Mouse coordinates in map cells resolution.
        int rasterI = game.mouseI/Map.SUB*Map.SUB;
        int rasterJ = game.mouseJ/Map.SUB*Map.SUB;
        
        display.cursorI = game.mouseI;
        display.cursorJ = game.mouseJ;

        // System.err.println("mi = " + mmi + " mj = " + mmj);

        while(Mouse.next())
        {
            int button = Mouse.getEventButton();
            boolean buttonState = Mouse.getEventButtonState();
            buttonPressed = 0;
            buttonReleased = 0;

            if(buttonState != lastButtonState)
            {
                if(buttonState)
                {
                    buttonPressed = button + 1;
                }
                else
                {
                    buttonReleased = button + 1;
                }
                
                lastButtonState = buttonState;
            }
            
            if(gameDisplay.topDialog != null)
            {
                gameDisplay.topDialog.mouseEvent(buttonPressed, buttonReleased, Mouse.getX(), Mouse.getY());
            }
            else
            {
                if(buttonPressed == 1)
                {
                    dragStartX = display.centerX;
                    dragStartY = display.centerY;
                    dragStartMx = Mouse.getX();
                    dragStartMy = Mouse.getY();
                    dragging = true;
                    // System.out.println("Setting drag start.");
                }

                // Hajo: Drag map while button 1 is pressed
                
                if(Mouse.isButtonDown(0) && dragging)
                {
                    int dx = Mouse.getX() - dragStartMx;
                    int dy = Mouse.getY() - dragStartMy;

                    display.centerX = dragStartX + dx;
                    display.centerY = dragStartY + dy;

                    // System.out.println("Dragging map.");
                }

                if(buttonReleased == 1)
                {
                    dragging = false;
                    handleMenuAndTools(rasterI, rasterJ);
                    
                    if(GameDisplay.debugShowMapInfo && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) gameDisplay.debugCatchMob();
                }

                if(buttonReleased == 2)
                {
                    Map map = gameDisplay.getDisplay().map;
                    DebugCentral.debugMakeArtifact(game, map);
                }
            }
        }
    }


    private void markForExcavation(Map map, int rasterI, int rasterJ) 
    {
        int ground = map.getFloor(rasterI, rasterJ);
        int block = map.getItem(rasterI + Map.O_BLOCK, rasterJ + Map.O_BLOCK) & Map.F_IDENT_MASK;

        if(Features.canBeDug(ground, block))
        {
            if (game.payMana(Tool.MARK_DIG.COST_MANA))
            {
                createExcavationJob(map, rasterI, rasterJ);
            }
            else 
            {
                TimedMessage tm = new TimedMessage("Not enough mana!",
                                       Colors.BRIGHT_GOLD_INK,
                                       Mouse.getX(), Mouse.getY() + 20,
                                       Clock.time(), 0.3);
                gameDisplay.addMessage(tm);
            }
        }
        else if(Features.canBeMined(ground, block))
        {
            createMiningJob(map, rasterI, rasterJ);
        }
    }

    
    private void makeLair(Map map, int rasterI, int rasterJ) 
    {
        int n = map.getFloor(rasterI, rasterJ);
        if(n >= Features.GROUND_POLY_TILES && n < Features.GROUND_POLY_TILES + 3)
        {
            game.addLairSquare(map, rasterI, rasterJ);
            game.soundPlayer.play(Sounds.MAKE_LAIR, 0.2f, 2.0f);            
        }
        else
        {
            markForExcavation(map, rasterI, rasterJ);
        }
    }
        
    
    private void makeFarm(Map map, int rasterI, int rasterJ)
    {
        int n = map.getFloor(rasterI, rasterJ);
        if(n >= Features.GROUND_POLY_TILES && n < Features.GROUND_POLY_TILES + 3)
        {
            game.soundPlayer.play(Sounds.MAKE_FARMLAND, 0.5f, 0.5f, 0.55f);            
            game.addFarmlandSquare(map, rasterI, rasterJ);
        }
        else
        {
            markForExcavation(map, rasterI, rasterJ);
        }
    }
    
    
    private void makeLibrary(Map map, int rasterI, int rasterJ) 
    {
        int n = map.getFloor(rasterI, rasterJ);
        if(n >= Features.GROUND_POLY_TILES && n < Features.GROUND_POLY_TILES + 3)
        {
            game.addLibrarySquare(map, rasterI, rasterJ);
            game.soundPlayer.play(Sounds.MAKE_LIBRARY, 0.8f, 1.0f);            
        }
        else
        {
            markForExcavation(map, rasterI, rasterJ);
        }
    }
    
    
    private void makeLab(Map map, int rasterI, int rasterJ)
    {
        int n = map.getFloor(rasterI, rasterJ);
        if(n >= Features.GROUND_POLY_TILES && n < Features.GROUND_POLY_TILES + 3)
        {
            boolean ok = game.payCoins(map, Features.I_COPPER_COINS, 1);

            if(ok)
            {
                game.addLabSquare(map, rasterI, rasterJ);
                game.soundPlayer.play(Sounds.MAKE_FORGE, 0.2f, 1.0f);
            }
            else
            {
                TimedMessage tm = new TimedMessage("Not enough copper!",
                                                   Colors.BRIGHT_GOLD_INK,
                                                   Mouse.getX(), Mouse.getY() + 20,
                                                   Clock.time(), 0.3);
                gameDisplay.addMessage(tm);
            }
        }
        else
        {
            markForExcavation(map, rasterI, rasterJ);
        }
    }

    
    private void makeForge(Map map, int rasterI, int rasterJ)
    {
        int n = map.getFloor(rasterI, rasterJ);
        if(n >= Features.GROUND_POLY_TILES && n < Features.GROUND_POLY_TILES + 3)
        {
            game.addForgeSquare(map, rasterI, rasterJ);
            game.soundPlayer.play(Sounds.MAKE_FORGE, 0.2f, 1.0f);
        }
        else
        {
            markForExcavation(map, rasterI, rasterJ);
        }
    }
    
    
    private void makeHospital(Map map, int rasterI, int rasterJ)
    {
        int n = map.getFloor(rasterI, rasterJ);
        if((n >= Features.GROUND_POLY_TILES && n < Features.GROUND_POLY_TILES + 3) ||
           (n >= Features.GROUND_HOSPITAL && n < Features.GROUND_HOSPITAL + 3))
        {
            boolean ok = game.payCoins(map, Features.I_COPPER_COINS, 5);

            if(ok)
            {
                game.addHospitalSquare(map, rasterI, rasterJ);
                game.soundPlayer.play(Sounds.MAKE_FORGE, 0.2f, 1.0f);
            }
            else
            {
                TimedMessage tm = new TimedMessage("Not enough copper!",
                                                   Colors.BRIGHT_GOLD_INK,
                                                   Mouse.getX(), Mouse.getY() + 20,
                                                   Clock.time(), 0.3);
                gameDisplay.addMessage(tm);
            }
        }
        else
        {
            markForExcavation(map, rasterI, rasterJ);
        }
    }

    
    private void makeTreasury(Map map, int rasterI, int rasterJ)
    {
        int n = map.getFloor(rasterI, rasterJ);
        if(n >= Features.GROUND_POLY_TILES && n < Features.GROUND_POLY_TILES + 3)
        {
            game.soundPlayer.play(Sounds.MAKE_TREASURY, 0.4f, 1.0f);            
            game.addTreasurySquare(map, rasterI, rasterJ);
        }
        else
        {
            markForExcavation(map, rasterI, rasterJ);
        }
    }

    
    private void makeGhostyard(Map map, int rasterI, int rasterJ)
    {
        int n = map.getFloor(rasterI, rasterJ);
        if(n >= Features.GROUND_POLY_TILES && n < Features.GROUND_POLY_TILES + 3)
        {
            game.addGhostyardSquare(map, rasterI, rasterJ);
            game.soundPlayer.play(Sounds.MAKE_FARMLAND, 0.2f, 1.0f);
        }
        else if(n >= Features.GROUND_GHOSTYARD && n < Features.GROUND_GHOSTYARD + 3)
        {
        	if(game.isGrave(map, rasterI, rasterJ))
        	{
                int gx = rasterI + Map.SUB/3;
                int gy = rasterJ + Map.SUB/2;

                int key = map.getMob(gx+1, gy);        		
                game.turnGraveIntoLair(map, game.world.mobs.get(key));
        	}
        	else
        	{
                game.allocateGrave(map, rasterI, rasterJ);

                // kill one mob for testing the grave
        		Mob mob = game.world.mobs.get(game.world.mobs.nextFreeKey() - 1);
        		mob.setPath(null);
        		mob.setAi(null);
                game.populateGrave(map, mob, rasterI, rasterJ);
        	}
        }        
        else
        {
            markForExcavation(map, rasterI, rasterJ);
        }
    }

    
    private void demolishRoom(Map map, int rasterI, int rasterJ)
    {
        Point p = new Point(rasterI, rasterJ);
        int n = map.getFloor(rasterI, rasterJ);
        if(n >= Features.GROUND_POLY_TILES && n < Features.GROUND_POLY_TILES + 3)
        {
            // actually, nothing to do, that's a free square already
        }
        else if(n >= Features.GROUND_LIBRARY && n < Features.GROUND_LIBRARY + 3)
        {
            game.resetSquare(map, rasterI, rasterJ);
            game.removeLibrarySquare(map, rasterI, rasterJ);
        }
        else if(n >= Features.GROUND_LAIR && n < Features.GROUND_LAIR + 3)
        {
            // lair
            game.getLairs().remove(p);
            game.resetSquare(map, rasterI, rasterJ);
        }
        else if(n >= Features.GROUND_GRASS_DARK && n < Features.GROUND_GRASS_DARK + 3)
        {
            // farm
            for(int i=0; i<game.getFarmland().size(); i++)
            {
                FarmSquare farm = game.getFarmland().get(i);
                if(farm.x == p.x && farm.y == p.y)
                {
                    game.getFarmland().remove(i);
                    game.resetSquare(map, rasterI, rasterJ);
                    break;
                }
            }
        }
        else if(n >= Features.GROUND_TREASURY && n < Features.GROUND_TREASURY + 3)
        {
            game.getTreasuries().remove(p);
            game.resetSquare(map, rasterI, rasterJ);
        }
        else if(n >= Features.GROUND_FORGE && n < Features.GROUND_FORGE + 3)
        {
            game.resetSquare(map, rasterI, rasterJ);
            game.removeForgeSquare(map, rasterI, rasterJ);
        }
        else if(n >= Features.GROUND_HOSPITAL && n < Features.GROUND_HOSPITAL + 3)
        {
            game.getHospitals().remove(p);
            game.resetSquare(map, rasterI, rasterJ);
        }
        else if(n >= Features.GROUND_LABORATORY && n < Features.GROUND_LABORATORY + 3)
        {
            game.resetSquare(map, rasterI, rasterJ);
            game.removeLabSquare(map, rasterI, rasterJ);
        }
        else if(n >= Features.GROUND_GHOSTYARD && n < Features.GROUND_GHOSTYARD + 3)
        {
            game.getGhostyards().remove(p);
            game.resetSquare(map, rasterI, rasterJ);
        }

        game.refreshPillars(rasterI, rasterJ);
    }


    private void spawnImp(Map map, int rasterI, int rasterJ) 
    {
        WayPathSource wps = new WayPathSource(map, Species.speciesTable.get(Species.IMPS_BASE).size, false);

        int n = map.getFloor(rasterI, rasterJ);
        if(n >= Features.GROUND_POLY_TILES && n < Features.GROUND_POLY_TILES + 3 &&
           wps.isMoveAllowed(game.mouseI, game.mouseJ, game.mouseI, game.mouseJ))
        {
            int imps = game.countMobs(Species.IMPS_BASE);
            int cost = Math.max(1, imps - 3) * Tool.SPELL_IMP.COST_MANA;

            boolean ok = game.payMana(cost);

            if(ok)
            {
                game.spawnImp(map, game.mouseI, game.mouseJ);
                game.soundPlayer.play(Sounds.CLAIM_SQUARE, 1.0f, 1.0f);
            }
            else
            {
                LOG.log(Level.INFO, "Not enough mana.");
                TimedMessage tm = new TimedMessage("Not enough mana!",
                                                   Colors.BRIGHT_GOLD_INK,
                                                   Mouse.getX(), Mouse.getY() + 20,
                                                   Clock.time(), 0.3);
                gameDisplay.addMessage(tm);
            }
        }
    }
    
    
    private void grabItem(Map map)
    {
        if(grabbedItem == 0)
        {
            if(game.payMana(10))
            {
                Point where = 
                    map.spirallytraverseArea(game.mouseI, game.mouseJ, 5,
                        new LocationCallback() {
                            @Override
                            public boolean visit(int x, int y) {
                                int item = map.getItem(x, y);

                                return item != 0 && (Features.isCoins(item) || Features.isResource(item));
                            }
                        });
                            
                // success?
                if(where != null)
                {
                    int item = map.getItem(where.x, where.y);
                    LOG.info("Grabbing item=" + item);
                    map.setItem(where.x, where.y, 0);
                    setMousePointer(display.textureCache.textures[item & 0xFFFF]);
                    grabbedItem = item;
                }
            }
        }
        else
        {
            // deposit item
            Point where = map.dropItem(game.mouseI, game.mouseJ, grabbedItem, (x, y) -> {});
            
            if(where != null)
            {
                grabbedItem = 0;
                setMousePointer(display.textureCache.textures[Features.CURSOR_HAND]);
            }
        }
    }
    
    
    private void placeDecoration(Map map)
    {
        int item = map.getItem(game.mouseI, game.mouseJ);
        
        // allowed to place a decoration here?
        if(item == 0 || Features.DUST_SET.contains(item))
        {
            map.setItem(game.mouseI, game.mouseJ, Tool.parameter);
        }
    }
    
    
    @Override
    public void read(BufferedReader reader) throws IOException
    {
        String line = reader.readLine();
        if(line != null)
        {
            grabbedItem = Integer.parseInt(line.substring(5));
        }
        else
        {
            // older games have no grabbed items
            grabbedItem = 0;                        
        }

        if(grabbedItem == 0)
        {
            setMousePointer(display.textureCache.textures[Features.CURSOR_HAND]);
        }
        else
        {
            setMousePointer(display.textureCache.textures[grabbedItem & 0xFFFF]);
            Tool.selected = Tool.SPELL_GRAB;            
        }
    }


    @Override
    public void write(Writer writer) throws IOException
    {
        writer.write("item=" + grabbedItem + "\n");
    }
    
    
    private void setMousePointer(Texture tex)
    {
        MousePointerBitmap mp = new MousePointerBitmap();
        // mp.tex = TextureCache.textures[17];
        mp.tex = tex;
        mp.grabX = 0;
        mp.grabY = mp.tex.image.getHeight()-1;
        mp.hue = 0xFFFFFFFF;
        display.setMousePointer(mp);
    }
    

    private void handleRoom1Buttons(int mouseX, int mouseY)
    {
        Mob keeper = game.world.mobs.get(game.getPlayerKey());
        int research = keeper.stats.getCurrent(KeeperStats.RESEARCH);

        int n = gameDisplay.calculateTabButtonNumber(mouseX, mouseY);
        
        if(n == 0)
        {
            Tool.selected = Tool.MARK_DIG;
            setMousePointer(display.textureCache.textures[Features.CURSOR_HAND]);
            soundPlayer.play(Sounds.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
        else if(n == 1)
        {
            Tool.selected = Tool.MAKE_LAIR;
            setMousePointer(display.textureCache.textures[Features.CURSOR_HAND]);
            soundPlayer.play(Sounds.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
        else if(n == 2)
        {
            Tool.selected = Tool.MAKE_FARM;
            setMousePointer(display.textureCache.textures[Features.CURSOR_HAND]);
            soundPlayer.play(Sounds.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
        else if(n == 3)
        {
            Tool.selected = Tool.MAKE_LIBRARY;
            setMousePointer(display.textureCache.textures[Features.CURSOR_HAND]);
            soundPlayer.play(Sounds.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
        else if(n == 4)
        {
            Tool.selected = Tool.MAKE_TREASURY;
            setMousePointer(display.textureCache.textures[Features.CURSOR_HAND]);
            soundPlayer.play(Sounds.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
        else if(n == 5 && (research & KeeperStats.RESEARCH_LABS) != 0)
        {
            Tool.selected = Tool.MAKE_LAB;
            setMousePointer(display.textureCache.textures[Features.CURSOR_HAND]);
            soundPlayer.play(Sounds.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
        else if(n == 6 && (research & KeeperStats.RESEARCH_FORGES) != 0)
        {
            Tool.selected = Tool.MAKE_FORGE;
            setMousePointer(display.textureCache.textures[Features.CURSOR_HAND]);
            soundPlayer.play(Sounds.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
        else if(n == 7 && (research & KeeperStats.RESEARCH_HEALING) != 0)
        {
            Tool.selected = Tool.MAKE_HOSPITAL;
            setMousePointer(display.textureCache.textures[Features.CURSOR_HAND]);
            soundPlayer.play(Sounds.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
        else if(n == 8 && (research & KeeperStats.RESEARCH_GHOSTYARDS) != 0)
        {
            Tool.selected = Tool.MAKE_GHOSTYARD;
            setMousePointer(display.textureCache.textures[Features.CURSOR_HAND]);
            soundPlayer.play(Sounds.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
        else if(n == 9)
        {
            Tool.selected = Tool.DEMOLISH;
            setMousePointer(display.textureCache.textures[Features.CURSOR_HAND]);
            soundPlayer.play(Sounds.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
    }


    private void handleSpellButtons(int mouseX, int mouseY)
    {
        int n = gameDisplay.calculateTabButtonNumber(mouseX, mouseY);

        if(n == 0)
        {
            Tool.selected = Tool.SPELL_IMP;
            // setMousePointer(TextureCache.species[Species.IMPS_BASE+2]);
            setMousePointer(display.textureCache.textures[Features.CURSOR_HAND]);
            soundPlayer.play(Sounds.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
        else if(n == 1)
        {
            Tool.selected = Tool.SPELL_GRAB;
            // setMousePointer(TextureCache.species[Species.IMPS_BASE+2]);
            // setMousePointer(display.textureCache.textures[Features.CURSOR_HAND]);
            soundPlayer.play(Sounds.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
        else if(n == 2)
        {
            soundPlayer.play(Sounds.UI_BUTTON_CLICK, 1.0f, 1.0f);
            ImageChoice decorationChoice = 
                    new ImageChoice(display.textureCache, gameDisplay, 800, 600, 
                            "Chose a decoration to place:", Features.DECORATIONS_SET, 
                            (texId) -> {
                                Tool.selected = Tool.SPELL_PLACE_DECORATION;
                                Tool.parameter = Features.I_TORCH_STAND;
                                gameDisplay.showDialog(null);
                                // setMousePointer(display.textureCache.textures[texId]);
                            });
        
            gameDisplay.showDialog(decorationChoice);
        }
    }

    
    private void handleBookButtons(int mouseX, int mouseY)
    {
        int n = gameDisplay.calculateTabButtonNumber(mouseX, mouseY);

        if(n == 0)
        {
            gameDisplay.openCreaturesBook();
            soundPlayer.play(Sounds.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
        else if(n == 1)
        {
            gameDisplay.openQuestBook();
            soundPlayer.play(Sounds.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
        else if(n == 2)
        {
            gameDisplay.openExpeditionBook();
            soundPlayer.play(Sounds.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
    }

    
    private void createExcavationJob(Map map, int rasterI, int rasterJ) 
    {
        // check for digging mark
        int mark = map.getItem(rasterI+Map.SUB/2, rasterJ+Map.SUB/2);

        if(mark == Features.MINING_MARK)
        {
            // Hajo: check if there are mobs on this square
            // we only unmark if it's empty, due to the ground changes
            boolean ok = true;

            for(int j=0; j<Map.SUB; j++)
            {
                for(int i=0; i<Map.SUB; i++)
                {
                    int n = map.getMob(rasterI+i, rasterJ+j);
                    ok &= (n == 0);
                }
            }

            if(ok)
            {
                // Hajo: clean square
                // try to cancel this job - only works if no worker
                // has taken the job yet. But AI will check the mark
                // too.
                game.jobQueue.remove(JobExcavate.class, rasterI + Map.SUB/2, rasterJ + Map.SUB/2);

                // now we unset the mark
                map.setFloor(rasterI, rasterJ, Features.GROUND_IMPASSABLE);
                map.setItem(rasterI+Map.SUB/2, rasterJ+Map.SUB/2, 0);
            }
        }
        else
        {
            // Hajo: is there still earth on this square?
            int ground = map.getFloor(rasterI, rasterJ);
            if(ground >= Features.GROUND_IMPASSABLE && ground < Features.GROUND_IMPASSABLE+3)
            {
                // Hajo: ok -> now set mining mark
                map.setFloor(rasterI, rasterJ, Features.GROUND_LIGHT_SOIL);
                map.setItem(rasterI+Map.SUB/2, rasterJ+Map.SUB/2, Features.MINING_MARK);

                JobExcavate job = new JobExcavate(game, rasterI + Map.SUB/2, rasterJ + Map.SUB/2);
                game.jobQueue.add(job, JobQueue.PRI_HIGH);

                soundPlayer.play(Sounds.UI_BUTTON_CLICK, 0.7f, 1.0f);                    
            }
        }
    }

    private void createMiningJob(Map map, int rasterI, int rasterJ) 
    {
        int mark = map.getItem(rasterI+Map.SUB/2, rasterJ+Map.SUB/2);

        if(mark == Features.MINING_MARK)
        {
            // Hajo: check if there are mobs on this square
            // we only unmark if it's empty, due to the ground changes
            boolean ok = true;

            for(int j=0; j<Map.SUB; j++)
            {
                for(int i=0; i<Map.SUB; i++)
                {
                    int n = map.getMob(rasterI+i, rasterJ+j);
                    ok &= (n == 0);
                }
            }

            if(ok)
            {
                // Hajo: clean square
                // try to cancel this job - only works if no worker
                // has taken the job yet. But AI will check the mark
                // too.
                game.jobQueue.remove(JobMining.class, rasterI + Map.SUB/2, rasterJ + Map.SUB/2);

                // now we unset the mark
                map.setFloor(rasterI, rasterJ, Features.GROUND_IMPASSABLE);
                map.setItem(rasterI+Map.SUB/2, rasterJ+Map.SUB/2, 0);
            }
        }
        else
        {
            map.setFloor(rasterI, rasterJ, Features.GROUND_LIGHT_SOIL);

            if(mark == 0)
            {
                mark = Features.MINING_MARK-3;
            }

            map.setItem(rasterI+Map.SUB/2, rasterJ+Map.SUB/2, mark+1);

            JobMining job = new JobMining(game, rasterI + Map.SUB/2, rasterJ + Map.SUB/2);
            game.jobQueue.add(job, JobQueue.PRI_LOW);
        }
    }

    private void handleMenuBar()
    {
        int mouseX = Mouse.getX();
        int mouseY = Mouse.getY();
        
        // Hajo: this was a click into the menu bar

        // Hajo: click on one of the left buttons?

        if(mouseX >= 0 && mouseX <= 130)
        {
            if(mouseY >= 72 && mouseY <= 72+20)
            {
                gameDisplay.selectTab(GameDisplay.TAB_ROOMS);
                soundPlayer.play(Sounds.UI_BUTTON_CLICK, 1.0f, 1.0f);
            } 
            else if(mouseY >= 52 && mouseY <= 52+20)
            {
                gameDisplay.selectTab(GameDisplay.TAB_SPELLS);
                soundPlayer.play(Sounds.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
            else if(mouseY >= 32 && mouseY <= 32+20)
            {
                gameDisplay.selectTab(GameDisplay.TAB_BOOKS);
                soundPlayer.play(Sounds.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
        }
        else
        {
            // not left, then check the bar button
            switch (gameDisplay.getSelectedTab()) 
            {
                case GameDisplay.TAB_ROOMS:
                    handleRoom1Buttons(mouseX, mouseY);
                    break;
                case GameDisplay.TAB_SPELLS:
                    handleSpellButtons(mouseX, mouseY);
                    break;
                case GameDisplay.TAB_BOOKS:
                    handleBookButtons(mouseX, mouseY);
                    break;
                default:
                    break;
            }
        }
    }
    

    private void handleMenuAndTools(int rasterI, int rasterJ)
    {
        int mouseX = Mouse.getX();
        int mouseY = Mouse.getY();
        
        if(mouseX > display.displayWidth - 69)
        {
            handleMessageStack(mouseY);
        }
        else if(mouseY < 100)
        {
            handleMenuBar();
        }
        else
        {
            Mob player = game.world.mobs.get(game.getPlayerKey());
            Map map = player.gameMap;

            switch(Tool.selected)
            {
                case MARK_DIG:
                    markForExcavation(map, rasterI, rasterJ);
                    break;
                case MAKE_LAIR:
                    makeLair(map, rasterI, rasterJ);
                    break;
                case MAKE_FARM:
                    makeFarm(map, rasterI, rasterJ);
                    break;
                case MAKE_TREASURY:
                    makeTreasury(map, rasterI, rasterJ);
                    break;
                case MAKE_LIBRARY:
                    makeLibrary(map, rasterI, rasterJ);
                    break;
                case MAKE_LAB:
                    makeLab(map, rasterI, rasterJ);
                    break;
                case MAKE_FORGE:
                    makeForge(map, rasterI, rasterJ);
                    break;
                case MAKE_HOSPITAL:
                    makeHospital(map, rasterI, rasterJ);
                    break;
                case MAKE_GHOSTYARD:
                    makeGhostyard(map, rasterI, rasterJ);
                    break;
                case DEMOLISH:
                    demolishRoom(map, rasterI, rasterJ);
                    break;
                case SPELL_IMP:
                    spawnImp(map, rasterI, rasterJ);
                    break;
                case SPELL_GRAB:
                    grabItem(map);
                    break;
                case SPELL_PLACE_DECORATION:
                    placeDecoration(map);
                    break;
            }
        }            
    }
    

    private void handleMessageStack(int mouseY) 
    {
        int n = (mouseY - 24) / 64;
        gameDisplay.activateHookedMessage(n);
    }
}    
