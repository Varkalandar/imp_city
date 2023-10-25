package impcity.editor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import impcity.game.Texture;
import impcity.game.TextureCache;
import impcity.game.World;
import impcity.game.map.Map;
import impcity.ogl.GlTextureCache;
import impcity.ogl.IsoDisplay;
import impcity.ui.Colors;
import impcity.ui.KeyHandler;
import impcity.ui.MouseHandler;
import impcity.ui.PostRenderHook;
import impcity.uikit.dialogs.StringInputDialog;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;

/**
 * Map editor for static maps.
 * 
 * @author Hj. Malthaner
 */
public class Editor implements PostRenderHook, TextureCache.LoaderCallback
{
    private static final String NAME_VERSION = "Map Editor r005";
    private static final Logger logger = Logger.getLogger(Editor.class.getName());

    private final TextureCache textureCache;
    private final IsoDisplay display;
    
    private int mouseI, mouseJ;
    private int mode = 1;
    
    private int selection;
    
    private int tileStart = 0;
    private final Texture tileBg;
    
    private final ArrayList <Integer> filteredList = new ArrayList<Integer>();
    
    private final Texture[] texturesInUiOrder;
    

    public static void main(String[] args)
    {
        Editor editor = null;
        
        try
        {
            editor = new Editor();
            editor.run();
        } 
        catch (LWJGLException ex)
        {
            logger.log(Level.SEVERE, ex.toString(), ex);
        } 
        catch (IOException ex) 
        {
            logger.log(Level.SEVERE, ex.toString(), ex);
        } 
        finally
        {
            if(editor != null && editor.display != null)
            {
                try
                {
                    editor.display.destroy();
                }
                catch (Exception ex)
                {
                    logger.log(Level.SEVERE, ex.toString(), ex);
                } 
            }
        }
    }
    private final int tilesPerRow = 9;
    
    private Editor() throws LWJGLException, IOException
    {
        World world = new World();
        
        textureCache = new GlTextureCache();
        display = new IsoDisplay(world.mobs, textureCache);
        
        display.map = makeMap(20, 20, 4);
        
        display.create();
        display.setViewDist(100);
        
        tileBg = textureCache.loadTexture("/ui/editor_tile_bg.png", false);
        
        textureCache.initialize(this);
        texturesInUiOrder = new Texture[textureCache.textures.length];
                 
        for(Texture tex : textureCache.textures)
        {
            if(tex != null) texturesInUiOrder[tex.uiOrder] = tex;
        }

        display.map = makeMap(20, 20, 19);
    }
    
    private void run()
    {
        display.setTitle(NAME_VERSION);
        
        display.postRenderHook = this;
        display.mouseHandler = new MyMouseHandler();
        display.keyHandler = new MyKeyHandler();
        display.centerY += 100;

        display.run();
    }
    
    @Override
    public void displayMore() 
    {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        display.font.drawStringScaled("Editor", 0xFFFFBF20, 10, display.displayHeight - 60, 1.44);
        
        display.font.drawStringScaled("1: Ground", 0xFFCCCCCC, 10, display.displayHeight - 100, 0.6);
        display.font.drawStringScaled("2: Left Wall", 0xFFCCCCCC, 10, display.displayHeight - 125, 0.6);
        display.font.drawStringScaled("3: Right Wall", 0xFFCCCCCC, 10, display.displayHeight - 150, 0.6);
        display.font.drawStringScaled("4: Way", 0xFFCCCCCC, 10, display.displayHeight - 175, 0.6);
        display.font.drawStringScaled("5: Rising items", 0xFFCCCCCC, 10, display.displayHeight - 200, 0.6);
        display.font.drawStringScaled("6: Flat items", 0xFFCCCCCC, 10, display.displayHeight - 225, 0.6);
        display.font.drawStringScaled("7: ...", 0xFFCCCCCC, 10, display.displayHeight - 250, 0.6);
        display.font.drawStringScaled("8: ...", 0xFFCCCCCC, 10, display.displayHeight - 275, 0.6);
        display.font.drawStringScaled("9: ...", 0xFFCCCCCC, 10, display.displayHeight - 300, 0.6);
        display.font.drawStringScaled("0: Clear", 0xFFCCCCCC, 10, display.displayHeight - 325, 0.6);
        display.font.drawStringScaled("l: Load map", 0xFFCCCCCC, 10, display.displayHeight - 350, 0.6);
        display.font.drawStringScaled("s: Save map", 0xFFCCCCCC, 10, display.displayHeight - 375, 0.6);
        
        display.font.drawStringScaled("Mode: " + mode, 0xFFBFFF20, 10, 320, 0.6);
        display.font.drawStringScaled("Selection: " + selection, 0xFFBFFF20, 10, 295, 0.6);
        display.font.drawStringScaled("Cursor: i=" + mouseI + " j=" + mouseJ, 0xFFBFFF20, 10, 270, 0.6);
        display.font.drawStringScaled("Use the cursor keys to pan the map.", 0xFFCCCCCC, 10, 220, 0.5);
        display.font.drawStringScaled("Use page up/down to page the selection.", 0xFFCCCCCC, 10, 200, 0.5);
        
        
        filteredList.clear();
        
        if(mode == 1)
        {
            for(int i=0; i<textureCache.grounds.length; i++)
            {
                Texture tex = textureCache.grounds[i];
                if(tex != null)
                {
                    if(tex.tags.contains("floor"))
                    {
                        filteredList.add(i);
                    }
                }            
            }
        }
        else
        {    
            for(int i=0; i<texturesInUiOrder.length; i++)
            {
                Texture tex = texturesInUiOrder[i];

                if(tex != null)
                {
                    switch (mode) 
                    {
                        case 1:
                            if(tex.tags.contains("floor"))
                            {
                                filteredList.add(i);
                            }   
                            break;
                        case 2:
                            if(tex.tags.contains("lwall"))
                            {
                                filteredList.add(i);
                            }
                            break;
                        case 3:
                            if(tex.tags.contains("rwall"))
                            {
                                filteredList.add(i);
                            }
                            break;
                        case 4:
                            if(tex.tags.contains("floor"))
                            {
                                filteredList.add(i);
                            }
                            break;
                        case 5:
                            if(tex.tags.contains("item"))
                            {
                                filteredList.add(i);
                            }
                            break;
                        case 6:
                            if(tex.tags.contains("floordeco"))
                            {
                                filteredList.add(i);
                            }
                            break;
                        default:
                            filteredList.add(i);
                            break;
                    }
                }
            }
        }
        
        displaySelectionList();
    }
    
    private void displaySelectionList()
    {
        int x = 0;
        int step = 130;
        
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        
        
        for(int i=0; i<tilesPerRow; i++)
        {
            if(filteredList.size() > tileStart + i)
            {
                GL11.glScissor(x*step, 0, step, 180);
                
                IsoDisplay.drawTile(tileBg, x*step, 0);
                Integer tileIndex = filteredList.get(tileStart + i);
                
                if(mode == 1 || mode == 4)
                {
                    Texture tex = textureCache.grounds[tileIndex];
                    if(tex != null)
                    {
                        int c = 0x909090;
                        IsoDisplay.drawGround(tex, x*step, 10, c, c, c, c);
                        display.font.drawStringScaled(tex.name, 0xFFFFFFFF, x*step+4, 162, 0.45);
                    }
                }
                else
                {
                    Texture tex = texturesInUiOrder[tileIndex];
                    IsoDisplay.drawTile(tex, x*step + (step - tex.image.getWidth())/2, 10);

                    display.font.drawStringScaled(tex.name, 0xFFFFFFFF, x*step+4, 162, 0.45);
                    
                    /*
                    if(tex.image.getWidth() > step)
                    {
                        IsoDisplay.drawTile(tex, x*step, 10);
                    }
                    else
                    {
                    }
                    */
                }                
                
                display.font.drawString("" + (char)('a' + x), 0xFFFFFFFF, x*step+4, 132);
                x++;
            }
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private Map makeMap(int w, int h, int floor)
    {
        Map map = new Map(w*Map.SUB, h*Map.SUB);
        
        int count = variantCount(floor);
        
        for(int j=0; j<w; j++)
        {
            for(int i=0; i<h; i++)
            {
                map.setFloor(i*Map.SUB, j*Map.SUB, floor+(int)(Math.random() * count));

                if(i==0 || j==0)
                {
                    map.setColor(i*Map.SUB, j*Map.SUB, 0);
                }
            }
        }        
        return map;
    }

    private int variantCount(int selection) 
    {
        Texture tex = textureCache.grounds[selection];

        int count = 3;

        if(tex != null)
        {
            int p = tex.tags.indexOf("n=");
            if(p >= 0)
            {
                count = Integer.parseInt(tex.tags.substring(p+2));
            }
        }
        
        return count;
    }

    private void askPlayerSpawnPosition() 
    {
        StringInputDialog stringInput = new StringInputDialog(display.font, "Please enter the player spawn position:", "120x120", 0.6);
        stringInput.display((display.displayWidth - stringInput.width) / 2,
                            (display.displayHeight - stringInput.height) / 2);
        
        String input = stringInput.getInput();
        
        String [] parts = input.split("x");
        
        if(parts.length == 2)
        {
            int x = Integer.parseInt(parts[0].trim());
            int y = Integer.parseInt(parts[1].trim());

            display.map.setPlayerSpawnPosition(x, y);
        }
    }

    @Override
    public void update(String what) {

    }

    private class MyKeyHandler implements KeyHandler
    {
        private File lastFile;
        @Override
        public void processKeyboard()
        {
            if (Keyboard.isKeyDown(Keyboard.KEY_LEFT))
            {
                display.centerX -= 8;
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
            {
                display.centerX += 8;
            }

            if (Keyboard.isKeyDown(Keyboard.KEY_UP))
            {
                display.centerY += 8;
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_DOWN))
            {
                display.centerY -= 8;
            }
            
            if(Keyboard.next())
            {
            
                if(Keyboard.getEventKeyState() == true)
                {
                    int key = Keyboard.getEventKey();
                    
                    if(key == Keyboard.KEY_1)
                    {
                        mode = 1;
                    }
                    if(key == Keyboard.KEY_2)
                    {
                        mode = 2;
                    }
                    if(key == Keyboard.KEY_3)
                    {
                        mode = 3;
                    }
                    if(key == Keyboard.KEY_4)
                    {
                        mode = 4;
                    }
                    if(key == Keyboard.KEY_5)
                    {
                        mode = 5;
                    }
                    if(key == Keyboard.KEY_6)
                    {
                        mode = 6;
                    }

                    if(key == Keyboard.KEY_0) selection = 0;
                    if(key == Keyboard.KEY_A) selection = filteredList.get(tileStart+0);
                    if(key == Keyboard.KEY_B) selection = filteredList.get(tileStart+1);
                    if(key == Keyboard.KEY_C) selection = filteredList.get(tileStart+2);
                    if(key == Keyboard.KEY_D) selection = filteredList.get(tileStart+3);
                    if(key == Keyboard.KEY_E) selection = filteredList.get(tileStart+4);
                    if(key == Keyboard.KEY_F) selection = filteredList.get(tileStart+5);
                    if(key == Keyboard.KEY_G) selection = filteredList.get(tileStart+6);
                    if(key == Keyboard.KEY_H) selection = filteredList.get(tileStart+7);
                    if(key == Keyboard.KEY_I) selection = filteredList.get(tileStart+8);
                    // if(key == Keyboard.KEY_J) selection = filteredList.get(tileStart+9);


                    if (key == Keyboard.KEY_L)
                    {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                loadMap();
                            }
                        });
                    }
                    if (key == Keyboard.KEY_P)
                    {
                       askPlayerSpawnPosition();
                    }
                    if (key == Keyboard.KEY_S)
                    {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                saveMap();
                            }
                        });
                    }

                    if (key == Keyboard.KEY_X)
                    {
                        makeSoftHills();
                    }
                    
                    if (key == Keyboard.KEY_PRIOR)
                    {
                        if(tileStart >= tilesPerRow) tileStart -= tilesPerRow;
                    }
                    if (key == Keyboard.KEY_NEXT)
                    {
                        tileStart += tilesPerRow;
                    }
                }
            }
        }
        
        @Override
        public boolean collectString(StringBuilder buffer)
        {
            return true;
        }

        private void loadMap()
        {
            JFileChooser jfc;
            
            File file = new File(".");
            
            if(file.exists())
            {
                jfc = new JFileChooser(file);
            } 
            else
            {
                jfc = new JFileChooser(".");
            }
            int ok = jfc.showOpenDialog(null);
            
            if(ok == JFileChooser.APPROVE_OPTION)
            {
                // Hajo: async, will be loaded from OpenGL thread
                // on next frame
                lastFile = jfc.getSelectedFile();
                display.loadMapRequested = lastFile;
            }
        }

        private void saveMap()
        {
            try
            {
                int ok = JFileChooser.APPROVE_OPTION;
                
                if(lastFile == null)
                {
                    JFileChooser jfc = new JFileChooser(".");
                    ok = jfc.showSaveDialog(null);
                    if(ok == JFileChooser.APPROVE_OPTION)
                    {
                        lastFile = jfc.getSelectedFile();
                    }
                }

                if(ok == JFileChooser.APPROVE_OPTION)
                {
                    display.map.save(lastFile);
                } 
            }   
            catch (IOException ex)
            {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        private void makeSoftHills() 
        {
            // Hajo: set floor light levels
            for(int j=0; j<display.map.getHeight(); j++)
            {
                for(int i=0; i<display.map.getWidth(); i++)
                {            
                    int mapI = i*Map.SUB;
                    int mapJ = j*Map.SUB;
                    
                    if(i==0 || j==0 || i == display.map.getWidth()-1 || j == display.map.getHeight()-1)
                    {
                        display.map.setColor(mapI, mapJ, 0);
                    }
                    else
                    {
                        int brightness = 160;
                        // int argb = Colors.randomColor(brightness, 24, 24, 24);
                        int argb = Colors.randomGray(brightness, 12);
                        display.map.setColor(mapI, mapJ, argb);
                    }
                }
            }
        }
    }
    
    private class MyMouseHandler implements MouseHandler
    {
        @Override
        public void processMouse()
        {
            int mx = (Mouse.getX() - display.centerX - 108);
            int my = (Mouse.getY() - display.centerY - 108) * 2;

            int mmi = -mx - my;
            int mmj =  mx - my;

            // System.err.println("mmi = " + mmi + " mmj = " + mmj);

            int raster = 216/Map.SUB;

            mouseI = mmi / raster;
            mouseJ = mmj / raster;

            // System.err.println("mi = " + mouseI + " mj = " + mouseJ);
            
            display.cursorI = mouseI;
            display.cursorJ = mouseJ;
            
            if(selection == 0 || mode == 1 || mode == 4)
            {
                display.cursorN = 13;
            }
            else
            {                
                if(texturesInUiOrder[selection] != null)
                {
                    display.cursorN = texturesInUiOrder[selection].cacheIndex;
                }
                else
                {
                    display.cursorN = 0;
                }
            }
            
            while(Mouse.next())
            {
                // int button = Mouse.getEventButton();
                
                boolean lmbPressed = Mouse.isButtonDown(0);
                boolean rmbPressed = Mouse.isButtonDown(1);
                
                int rasterI = mouseI/Map.SUB*Map.SUB;
                int rasterJ = mouseJ/Map.SUB*Map.SUB;
                if(lmbPressed)
                {
                    // System.err.println("button = " + button);
                    handleLeftClick(rasterI, rasterJ);
                }
                
                if(rmbPressed)
                {
                    handleRightClick(rasterI, rasterJ);
                }                
            }
        }

        private void handleLeftClick(int rasterI, int rasterJ) 
        {
            Map map = display.map;
            switch (mode) {
                case 1:
                    if(selection == 0)
                    {
                        map.setFloor(rasterI, rasterJ, 0);
                    }
                    else
                    {
                        int count = variantCount(selection);
                        map.setFloor(rasterI, rasterJ, selection + (int)(Math.random() * count));
                    }   
                    break;
                case 2:
                    map.setLeftWall(rasterI, rasterJ, texturesInUiOrder[selection].cacheIndex);
                    break;
                case 3:
                    map.setRightWall(rasterI, rasterJ, texturesInUiOrder[selection].cacheIndex);
                    break;
                case 4:
                    map.setWay(rasterI, rasterJ, texturesInUiOrder[selection].cacheIndex);
                    break;
                default:
                    if(selection == 0)
                    {
                        map.setItem(mouseI, mouseJ, 0);
                    }
                    else
                    {
                        if(mode == 6)
                        {
                            map.setItem(mouseI, mouseJ, Map.F_FLOOR_DECO + texturesInUiOrder[selection].cacheIndex);
                        }
                        else
                        {
                            map.setItem(mouseI, mouseJ, Map.F_DECO + texturesInUiOrder[selection].cacheIndex);
                        }
                    }   
                    break;
            // System.err.println("Setting item");
            }
        }
        
        private void handleRightClick(int rasterI, int rasterJ) 
        {
            Map map = display.map;
            int n;
            
            switch (mode) 
            {
                case 1:
                    selection = map.getFloor(rasterI, rasterJ);
                    break;
                case 2:
                    //map.setLeftWall(rasterI, rasterJ, texturesInUiOrder[selection].cacheIndex);
                    break;
                case 3:
                    //map.setRightWall(rasterI, rasterJ, texturesInUiOrder[selection].cacheIndex);
                    break;
                case 4:
                    //map.setWay(rasterI, rasterJ, texturesInUiOrder[selection].cacheIndex);
                    break;
                case 5:
                    n = map.getItem(mouseI, mouseJ);
                    selection = itemToSelection(n & Map.F_ITEM_MASK);
                    break;
                case 6:
                    n = map.getItem(mouseI, mouseJ);
                    selection = itemToSelection(n & Map.F_ITEM_MASK);
                    break;
            }
        }

        private int itemToSelection(int item) 
        {
            for(int i=0; i<texturesInUiOrder.length; i++)
            {
                if(texturesInUiOrder[i] != null && item == texturesInUiOrder[i].cacheIndex)
                {
                    return i;
                }
            }
            
            return 0;
        }
    }
}
