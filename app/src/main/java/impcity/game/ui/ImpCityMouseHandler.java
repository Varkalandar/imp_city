package impcity.game.ui;

import impcity.game.*;
import impcity.game.species.Species;
import impcity.game.ai.WayPathSource;
import impcity.game.jobs.JobExcavate;
import impcity.game.jobs.JobMining;
import impcity.game.jobs.JobQueue;
import impcity.game.processables.FarmSquare;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.logging.Logger;

import impcity.game.map.Map;
import impcity.game.mobs.Mob;
import impcity.oal.SoundPlayer;
import impcity.ogl.IsoDisplay;
import impcity.ui.MouseHandler;
import impcity.ui.MousePointerBitmap;
import org.lwjgl.input.Mouse;

/**
 *
 * @author Hj. Malthaner
 */
public class ImpCityMouseHandler implements MouseHandler
{
    public static final Logger logger = Logger.getLogger(ImpCityMouseHandler.class.getName());
    
    private boolean lastButtonState = false;
    private final ImpCity game;
    private final IsoDisplay display;
    private final SoundPlayer soundPlayer;

    private final GameDisplay gameDisplay;
    private int buttonPressed;
    private int buttonReleased;
    
    private int dragStartX, dragStartY;
    private int dragStartMx, dragStartMy;
    
    public ImpCityMouseHandler(ImpCity game, 
                               GameDisplay gameDisplay,
                               IsoDisplay display,
                               SoundPlayer soundPlayer)
    {
        this.game = game;
        this.gameDisplay = gameDisplay;
        this.display = display;
        this.soundPlayer = soundPlayer;

        Tools.selected = Tools.MARK_DIG;
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
                    
                    // System.out.println("Setting drag start.");
                }

                // Hajo: Drag map while button 1 is pressed
                
                if(Mouse.isButtonDown(0))
                {
                    int dx = Mouse.getX() - dragStartMx;
                    int dy = Mouse.getY() - dragStartMy;

                    display.centerX = dragStartX + dx;
                    display.centerY = dragStartY + dy;

                    // System.out.println("Dragging map.");
                }

                if(buttonReleased == 1)
                {
                    handleMenuAndTools(rasterI, rasterJ);
                }
            }
        }
    }

    private void markForExcavation(Map map, int rasterI, int rasterJ) 
    {
        // Hajo: first check if this is a diggable square at all.
        
        int ground = map.getFloor(rasterI, rasterJ);
        if(ground < Features.GROUND_IMPASSABLE && ground >= Features.GROUND_IMPASSABLE + 3) 
        {
            // wrong ground
            return;
        }
        
        int item = map.getItem(rasterI, rasterJ) & 0xFFFF;
        // if(item >= Features.I_EARTH_BLOCK && item < Features.I_EARTH_BLOCK + 3) 
        if(item >= Features.I_STEEP_EARTH_BLOCK && item < Features.I_STEEP_EARTH_BLOCK + 3) 
        {
            // this is should be a diggable sqaure, it has earth
            createExcavationJob(map, rasterI, rasterJ);
        }
        // else if(item >= Features.I_TREASURE_BLOCK && item < Features.I_TREASURE_BLOCK + 3) 
        else if((item >= Features.I_GOLD_MOUND && item < Features.I_GOLD_MOUND + 3) ||
                (item >= Features.I_COPPER_ORE_MOUND && item < Features.I_COPPER_ORE_MOUND + 3) ||
                (item >= Features.I_TIN_ORE_MOUND && item < Features.I_TIN_ORE_MOUND + 3))
        {
            createMiningJob(map, rasterI, rasterJ);
        }
            
    }
    private void makeLair(Map map, int rasterI, int rasterJ) 
    {
        int n = map.getFloor(rasterI, rasterJ);
        if(n >= Features.GROUND_POLY_TILES && n < Features.GROUND_POLY_TILES + 3)
        {
            map.setFloor(rasterI, rasterJ, Features.GROUND_LAIR + (int)(Math.random() * 3));
            game.addLairSquare(rasterI, rasterJ);
            game.soundPlayer.play(Sounds.MAKE_LAIR, 1.0f, 1.0f);            
        }
    }
        
    private void makeFarm(Map map, int rasterI, int rasterJ)
    {
        int n = map.getFloor(rasterI, rasterJ);
        if(n >= Features.GROUND_POLY_TILES && n < Features.GROUND_POLY_TILES + 3)
        {
            map.setFloor(rasterI, rasterJ, Features.GROUND_GRASS_DARK + (int)(Math.random() * 2));
            game.addFarmlandSquare(rasterI, rasterJ);
            game.soundPlayer.play(Sounds.MAKE_FARMLAND, 1.0f, 1.0f);            
        }
    }
    
    private void makeLibrary(Map map, int rasterI, int rasterJ) 
    {
        int n = map.getFloor(rasterI, rasterJ);
        if(n >= Features.GROUND_POLY_TILES && n < Features.GROUND_POLY_TILES + 3)
        {
            map.setFloor(rasterI, rasterJ, Features.GROUND_LIBRARY + (int)(Math.random() * 1));
            game.addLibrarySquare(rasterI, rasterJ);

            // map.setItem(rasterI+2, rasterJ+Map.SUB-4, Map.F_DECO + Features.I_BOOKSHELF_RIGHT);
            map.setItem(rasterI, rasterJ+4, Map.F_DECO + Features.I_BOOKSHELF_HALF_RIGHT);

            Rectangle r = new Rectangle(rasterI, rasterJ+3, 5, 1);
            map.setAreaMovementBlocked(r, true);
            
            game.soundPlayer.play(Sounds.MAKE_LIBRARY, 0.8f, 1.0f);            
        }
    }
    
    private void makeWorkshop(Map map, int rasterI, int rasterJ)
    {
        int n = map.getFloor(rasterI, rasterJ);
        if(n >= Features.GROUND_POLY_TILES && n < Features.GROUND_POLY_TILES + 3)
        {
            map.setFloor(rasterI, rasterJ, Features.GROUND_WORKSHOP + (int)(Math.random() * 3));
            game.addWorkshopSquare(map, rasterI, rasterJ);
            game.soundPlayer.play(Sounds.MAKE_WORKSHOP, 0.2f, 1.0f);
        }
    }

    private void makeForge(Map map, int rasterI, int rasterJ)
    {
        int n = map.getFloor(rasterI, rasterJ);
        if(n >= Features.GROUND_POLY_TILES && n < Features.GROUND_POLY_TILES + 3)
        {
            map.setFloor(rasterI, rasterJ, Features.GROUND_FORGE + (int)(Math.random() * 3));
            game.addForgeSquare(map, rasterI, rasterJ);
            game.soundPlayer.play(Sounds.MAKE_WORKSHOP, 0.2f, 1.0f);
        }
    }
    
    private void makeHospital(Map map, int rasterI, int rasterJ)
    {
        int n = map.getFloor(rasterI, rasterJ);
        if((n >= Features.GROUND_POLY_TILES && n < Features.GROUND_POLY_TILES + 3) ||
           (n >= Features.GROUND_HOSPITAL && n < Features.GROUND_HOSPITAL + 3))
        {
            map.setFloor(rasterI, rasterJ, Features.GROUND_HOSPITAL + (int)(Math.random() * 1));
            game.addHospitalSquare(map, rasterI, rasterJ);
            game.soundPlayer.play(Sounds.MAKE_WORKSHOP, 0.2f, 1.0f);            
        }
    }

    private void makeTreasury(Map map, int rasterI, int rasterJ)
    {
        int n = map.getFloor(rasterI, rasterJ);
        if(n >= Features.GROUND_POLY_TILES && n < Features.GROUND_POLY_TILES + 3)
        {
            map.setFloor(rasterI, rasterJ, Features.GROUND_TREASURY + (int)(Math.random() * 2));
            game.addTreasurySquare(rasterI, rasterJ);
            game.soundPlayer.play(Sounds.MAKE_TREASURY, 0.4f, 1.0f);            
        }
    }

    private void demolishRoom(Map map, int rasterI, int rasterJ)
    {
        Point p = new Point(rasterI, rasterJ);
        int n = map.getFloor(rasterI, rasterJ);
        if(n >= Features.GROUND_POLY_TILES && n < Features.GROUND_POLY_TILES + 3)
        {
            // actually, nothing to do, that's a free squre already
        }
        else if(n >= Features.GROUND_LIBRARY && n < Features.GROUND_LIBRARY + 3)
        {
            game.getLibraries().remove(p);
            resetSquare(map, rasterI, rasterJ);
        }
        else if(n >= Features.GROUND_LAIR && n < Features.GROUND_LAIR + 3)
        {
            // lair
            game.getLairs().remove(p);
            resetSquare(map, rasterI, rasterJ);
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
                    resetSquare(map, rasterI, rasterJ);
                    break;
                }
            }
        }
        else if(n >= Features.GROUND_TREASURY && n < Features.GROUND_TREASURY + 3)
        {
            game.getTreasuries().remove(p);
            resetSquare(map, rasterI, rasterJ);
        }
        else if(n >= Features.GROUND_FORGE && n < Features.GROUND_FORGE + 3)
        {
            game.getForges().remove(p);
            resetSquare(map, rasterI, rasterJ);
        }
        else if(n >= Features.GROUND_HOSPITAL && n < Features.GROUND_HOSPITAL + 3)
        {
            game.getHospitals().remove(p);
            resetSquare(map, rasterI, rasterJ);
        }
    
        game.refreshPillars(rasterI, rasterJ);
    }

    private void spawnImp(Map map, int rasterI, int rasterJ) 
    {
        WayPathSource wps = new WayPathSource(map, Species.speciesTable.get(Species.IMPS_BASE).size);
        
        int n = map.getFloor(rasterI, rasterJ);
        if(n >= Features.GROUND_POLY_TILES && n < Features.GROUND_POLY_TILES + 3 &&
           wps.isMoveAllowed(game.mouseI, game.mouseJ, game.mouseI, game.mouseJ))
        {
            game.spawnImp(map, game.mouseI, game.mouseJ);
            game.soundPlayer.play(Sounds.CLAIM_SQUARE, 1.0f, 1.0f);
        }
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

    private void handleRoom1Buttons(int left, int mouseX)
    {
        Mob keeper = game.world.mobs.get(game.getPlayerKey());
        int research = keeper.stats.getCurrent(KeeperStats.RESEARCH);

        if(mouseX >= left + 196 && mouseX <= left + 196 + 80)
        {
            Tools.selected = Tools.MARK_DIG;
            setMousePointer(display.textureCache.textures[Features.CURSOR_HAND]);
            soundPlayer.play(Sounds.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
        else if(mouseX >= left + 280 && mouseX <= left + 280 + 80)
        {
            Tools.selected = Tools.MAKE_LAIR;
            setMousePointer(display.textureCache.textures[Features.CURSOR_HAND]);
            soundPlayer.play(Sounds.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
        else if(mouseX >= left + 364 && mouseX <= left + 364 + 80)
        {
            Tools.selected = Tools.MAKE_FARM;
            setMousePointer(display.textureCache.textures[Features.CURSOR_HAND]);
            soundPlayer.play(Sounds.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
        else if(mouseX >= left + 448 && mouseX <= left + 448 + 80)
        {
            Tools.selected = Tools.MAKE_LIBRARY;
            setMousePointer(display.textureCache.textures[Features.CURSOR_HAND]);
            soundPlayer.play(Sounds.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
        else if(mouseX >= left + 528 && mouseX <= left + 528 + 80 && (research & KeeperStats.RESEARCH_WORKSHOPS) != 0)
        {
            Tools.selected = Tools.MAKE_WORKSHOP;
            setMousePointer(display.textureCache.textures[Features.CURSOR_HAND]);
            soundPlayer.play(Sounds.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
        else if(mouseX >= left + 608 && mouseX <= left + 616 + 80 && (research & KeeperStats.RESEARCH_FORGES) != 0)
        {
            Tools.selected = Tools.MAKE_FORGE;
            setMousePointer(display.textureCache.textures[Features.CURSOR_HAND]);
            soundPlayer.play(Sounds.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
        else if(mouseX >= left + 700 && mouseX <= left + 700 + 80 && (research & KeeperStats.RESEARCH_HEALING) != 0)
        {
            Tools.selected = Tools.MAKE_HOSPITAL;
            setMousePointer(display.textureCache.textures[Features.CURSOR_HAND]);
            soundPlayer.play(Sounds.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
        else if(mouseX >= left + 824 && mouseX <= left + 824 + 80)
        {
            Tools.selected = Tools.DEMOLISH;
            setMousePointer(display.textureCache.textures[Features.CURSOR_HAND]);
            soundPlayer.play(Sounds.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
    }

    private void handleRoom2Buttons(int left, int mouseX)
    {
        if(mouseX >= left + 196 && mouseX <= left + 196 + 80)
        {
            Tools.selected = Tools.MARK_DIG;
            setMousePointer(display.textureCache.textures[Features.CURSOR_HAND]);
            soundPlayer.play(Sounds.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
        else if(mouseX >= left + 528 && mouseX <= left + 528 + 80)
        {
            Tools.selected = Tools.MAKE_TREASURY;
            setMousePointer(display.textureCache.textures[Features.CURSOR_HAND]);
            soundPlayer.play(Sounds.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
        else if(mouseX >= left + 824 && mouseX <= left + 824 + 80)
        {
            Tools.selected = Tools.DEMOLISH;
            setMousePointer(display.textureCache.textures[Features.CURSOR_HAND]);
            soundPlayer.play(Sounds.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
    }

    private void handleSpellButtons(int left, int mouseX) 
    {
        if(mouseX >= left + 196 && mouseX <= left + 196 + 80)
        {
            Tools.selected = Tools.SPELL_IMP;
            // setMousePointer(TextureCache.species[Species.IMPS_BASE+2]);
            setMousePointer(display.textureCache.textures[Features.CURSOR_HAND]);
            soundPlayer.play(Sounds.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
    }

    private void resetSquare(Map map, int rasterI, int rasterJ) 
    {
        map.setWayLikeItem(rasterI, rasterJ, 0);
        for(int j=0; j<Map.SUB; j++)
        {
            for(int i=0; i<Map.SUB; i++)
            {
                map.setItem(rasterI+i, rasterJ+j, 0);
                map.setMovementBlocked(rasterI+i, rasterJ+j, false);
                map.setPlacementBlocked(rasterI+i, rasterJ+j, false);
                
                game.removeGeneratorFrom(rasterI+i, rasterJ+j);
            }
        }
        
        int n = map.getFloor(rasterI, rasterJ);
        if(n < Features.GROUND_POLY_TILES || n >= Features.GROUND_POLY_TILES + 3)
        {
            map.setFloor(rasterI, rasterJ, Features.GROUND_POLY_TILES + (int)(Math.random() * 3));            
        }
    }
    
    private void createExcavationJob(Map map, int rasterI, int rasterJ) 
    {
        // check for digging mark
        int mark = map.getItem(rasterI+4, rasterJ+4);

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
                // Hajo: clean sqaure
                // try to cancel this job - only works if no worker
                // has taken the job yet. But AI will check the mark
                // too.
                game.jobQueue.remove(JobExcavate.class, rasterI + Map.SUB/2, rasterJ + Map.SUB/2);

                // now we unset the mark
                map.setFloor(rasterI, rasterJ, Features.GROUND_IMPASSABLE);
                map.setItem(rasterI+4, rasterJ+4, 0);
            }
        }
        else
        {
            // Hajo: is there still earth on this sqaure?
            int ground = map.getFloor(rasterI, rasterJ);
            if(ground >= Features.GROUND_IMPASSABLE && ground < Features.GROUND_IMPASSABLE+3)
            {
                // Hajo: ok -> now set mining mark
                map.setFloor(rasterI, rasterJ, Features.GROUND_LIGHT_SOIL);
                map.setItem(rasterI+4, rasterJ+4, Features.MINING_MARK);

                JobExcavate job = new JobExcavate(game, rasterI + Map.SUB/2, rasterJ + Map.SUB/2);
                game.jobQueue.add(job, JobQueue.PRI_HIGH);

                soundPlayer.play(Sounds.UI_BUTTON_CLICK, 0.7f, 1.0f);                    
            }
        }
    }

    private void createMiningJob(Map map, int rasterI, int rasterJ) 
    {
        int mark = map.getItem(rasterI+4, rasterJ+4);

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
                // Hajo: clean sqaure
                // try to cancel this job - only works if no worker
                // has taken the job yet. But AI will check the mark
                // too.
                game.jobQueue.remove(JobMining.class, rasterI + Map.SUB/2, rasterJ + Map.SUB/2);

                // now we unset the mark
                map.setFloor(rasterI, rasterJ, Features.GROUND_IMPASSABLE);
                map.setItem(rasterI+4, rasterJ+4, 0);
            }
        }
        else
        {
            map.setFloor(rasterI, rasterJ, Features.GROUND_LIGHT_SOIL);

            if(mark == 0)
            {
                mark = Features.MINING_MARK-3;
            }

            map.setItem(rasterI+4, rasterJ+4, mark+1);

            JobMining job = new JobMining(game, rasterI + Map.SUB/2, rasterJ + Map.SUB/2);
            game.jobQueue.add(job, JobQueue.PRI_LOW);
        }
    }

    private void handleMenuBar()
    {
        int mouseX = Mouse.getX();
        int mouseY = Mouse.getY();
        int left = gameDisplay.calcMainUiBarLeft();

        // Hajo: this was a click into the menu bar

        // Hajo: click on one of the left buttons?

        if(mouseX >= left + 14 && mouseX <= left + 14 + 140)
        {
            if(mouseY >= 85 && mouseY <= 85 + 30)
            {
                gameDisplay.selectTab(GameDisplay.TAB_ROOMS_I);
                soundPlayer.play(Sounds.UI_BUTTON_CLICK, 1.0f, 1.0f);
            } 
            else if(mouseY >= 62 && mouseY <= 62 + 30)
            {
                gameDisplay.selectTab(GameDisplay.TAB_ROOMS_II);
                soundPlayer.play(Sounds.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
            else if(mouseY >= 39 && mouseY <= 39 + 30)
            {
                gameDisplay.selectTab(GameDisplay.TAB_SPELLS);
                soundPlayer.play(Sounds.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
        }


        if(gameDisplay.getSelectedTab() == GameDisplay.TAB_ROOMS_I)
        {
            handleRoom1Buttons(left, mouseX);
        }
        else if(gameDisplay.getSelectedTab() == GameDisplay.TAB_ROOMS_II)
        {
            handleRoom2Buttons(left, mouseX);
        }
        else if(gameDisplay.getSelectedTab() == GameDisplay.TAB_SPELLS)
        {
            handleSpellButtons(left, mouseX);
        }
    }

    private void handleMenuAndTools(int rasterI, int rasterJ)
    {
        int mouseX = Mouse.getX();
        int mouseY = Mouse.getY();
        
        if(mouseX > display.displayWidth - 40)
        {
            handleMessageStack(mouseX, mouseY);
        }
        else if(mouseY < 100)
        {
            handleMenuBar();
        }
        else
        {
            Mob player = game.world.mobs.get(game.getPlayerKey());
            Map map = player.gameMap;

            switch(Tools.selected)
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
                case MAKE_WORKSHOP:
                    makeWorkshop(map, rasterI, rasterJ);
                    break;
                case MAKE_FORGE:
                    makeForge(map, rasterI, rasterJ);
                    break;
                case MAKE_HOSPITAL:
                    makeHospital(map, rasterI, rasterJ);
                    break;
                case DEMOLISH:
                    demolishRoom(map, rasterI, rasterJ);
                    break;
                case SPELL_IMP:
                    spawnImp(map, rasterI, rasterJ);
                    break;
            }
        }            
    }

    private void handleMessageStack(int mouseX, int mouseY) 
    {
        int n = mouseY / 32;
        gameDisplay.activateHookedMessage(n);
    }
}    
