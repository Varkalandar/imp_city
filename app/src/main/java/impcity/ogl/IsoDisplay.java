package impcity.ogl;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import impcity.game.Clock;
import impcity.game.Item;
import impcity.game.Texture;
import impcity.game.TextureCache;
import impcity.game.ai.MobStats;
import impcity.game.map.Map;
import impcity.game.mobs.Mob;
import impcity.ui.KeyHandler;
import impcity.ui.MouseHandler;
import impcity.ui.MousePointerBitmap;
import impcity.ui.PixFont;
import impcity.ui.PostRenderHook;
import java.util.ArrayList;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.opengl.GL13;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.util.glu.GLU.gluErrorString;
import rlgamekit.objects.Cardinal;
import rlgamekit.objects.Registry;

/**
 * Isometric map display.
 * 
 * @author Hj. Malthaner
 */
public class IsoDisplay implements PostRenderHook
{
    public static final Logger logger = Logger.getLogger(IsoDisplay.class.getName());

    private String title = "Initializing ... please wait.";

    public int displayHeight = 768;
    public int displayWidth = 1200;

    private final Dimension newDisplaySize = new Dimension();

    private int viewDist = 8;
    
    public PixFont font;
    public final HotspotMap hotspotMap;
    
    public Map map;
    private final Registry<Mob> mobs;
    private final Registry<Item> items;

    private final DrawableVerticalList vList = new DrawableVerticalList();
    
    
    public final TextureCache textureCache;
    private final Texture [] lightTextures = new Texture[32];
    
    
    public int centerX, centerY;
    public int cursorI, cursorJ, cursorN;
    
    public KeyHandler keyHandler = new DummyKeyHandler();
    public MouseHandler mouseHandler = new DummyMouseHandler();
    public PostRenderHook postRenderHook = this;
    private MousePointerBitmap mousePointer;

    private JFrame frame;
    private Canvas canvas;

    private boolean quitRequested;
    public File loadMapRequested;
    private boolean showItemNames;
    private final String[] decoDisplayNames;
    
    public IsoDisplay(Registry<Mob> mobs, Registry<Item> items, TextureCache textureCache)
    {
        this.mobs = mobs;
        this.items = items;
        this.textureCache = textureCache;
        this.decoDisplayNames = new String[textureCache.textures.length];
        
        cursorI = -1;
        cursorJ = -1;
        cursorN = 9;
        
        map = new Map(16, 16);
        
        centerX = 400;
        centerY = 300;

        hotspotMap = new HotspotMap();
        hotspotMap.setFactor(0.5);
    }

    public void create() throws LWJGLException, IOException
    {
        boolean useFrame = true;
        
        if(useFrame)
        {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            
            frame = new JFrame();

            frame.getContentPane().setPreferredSize(new Dimension(displayWidth, displayHeight));
            frame.pack();
            
            frame.setLayout(new BorderLayout());

            frame.setResizable(true);
            frame.setLocation((screenSize.width - displayWidth)/2, (screenSize.height - displayHeight - 32) / 2);
            frame.addWindowListener(new MyWindowListener());
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            
            canvas = new Canvas();
            frame.add(canvas);
            frame.setVisible(true);
            
            try
            {
                BufferedImage icon = ImageIO.read(this.getClass().getResourceAsStream("/icon.png"));
                frame.setIconImage(icon);
            }
            catch(IOException ex)
            {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
            Display.setParent(canvas);
        }        
        else
        {
            Display.setDisplayMode(new DisplayMode(displayWidth, displayHeight));
            Display.setFullscreen(false);
        }
        
        setTitle(title);
        Display.setVSyncEnabled(true);
        Display.create();
        
        //Keyboard
        Keyboard.create();

        //Mouse
        Mouse.setGrabbed(false);
        Mouse.create();
        
        //OpenGL
        initGL();
        resizeGL(canvas.getWidth(), canvas.getHeight());

        canvas.addComponentListener(new ComponentAdapter() 
            {
                @Override
                public void componentResized(ComponentEvent e)
                {
                    newDisplaySize.width = canvas.getWidth();
                    newDisplaySize.height = canvas.getHeight();
                }
            }
        );
        
        lightTextures[0] = GlTextureCache.loadTexture(this.getClass(), "/light_1.png", GL_TEXTURE0, GL_RGBA);
        lightTextures[1] = GlTextureCache.loadTexture(this.getClass(), "/light_2.png", GL_TEXTURE0, GL_RGBA);
        lightTextures[2] = GlTextureCache.loadTexture(this.getClass(), "/light_sparks.png", GL_TEXTURE0, GL_RGBA);
        lightTextures[3] = GlTextureCache.loadTexture(this.getClass(), "/vignette.png", GL_TEXTURE0, GL_RGBA);
        
        // System.err.println("light id=" + lightTextures[0].id);
        
        try 
        {
            font = new PixFont();
        }
        catch (IOException ex) 
        {
            Logger.getLogger(IsoDisplay.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void quit()
    {
        quitRequested = true;
    }
    
    public void destroy()
    {
        // Methods already check if created before destroying.
        Mouse.destroy();
        Keyboard.destroy();
        Display.destroy();

        if(frame != null)
        {
            frame.setVisible(false);
            frame.dispose();
        }
    }

    public void initGL()
    {
        // 2D Initialization
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glEnable(GL_TEXTURE_2D);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        glDisable(GL_LIGHTING);
        glDisable(GL_DEPTH_TEST);
    }

    
    public void resizeGL(int width, int height)
    {
        newDisplaySize.width = displayWidth = width;
        newDisplaySize.height = displayHeight = height;
        
        int res = java.awt.Toolkit.getDefaultToolkit().getScreenResolution();   
        
        logger.log(Level.INFO, "Adjusting for res {0}", res);
           
        // Doesn't have the desired effect. Disabled for now
        // int scaledWidth = displayWidth * res / 96;
        // int scaledHeight = displayHeight * res / 96;

        int scaledWidth = displayWidth;
        int scaledHeight = displayHeight;
        
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, scaledWidth, 0, scaledHeight, 1, -1);
        GL11.glViewport(0, 0, scaledWidth, scaledHeight);
        exitOnGLError("glViewport");
    }
    

    public void render(long currentTime)
    {
        if(loadMapRequested != null)
        {
            try
            {
                map.load(loadMapRequested);
                loadMapRequested = null;
            } 
            catch(IOException ex)
            {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        else
        {
            clear();
            displayMap();

            hotspotMap.layout(this);
            hotspotMap.display(this);
            
            postRenderHook.displayMore();

            if(mousePointer != null)
            {
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                drawTile(mousePointer.tex, 
                         Mouse.getX() - mousePointer.grabX, Mouse.getY() - mousePointer.grabY,
                         mousePointer.hue);
            }
        }
    }
    
    public void setDecoDisplayName(int deco, String name)
    {
        decoDisplayNames[deco & Map.F_IDENT_MASK] = name;
    }
    
    public void setViewDist(int dist)
    {
        viewDist = dist;
    }
    
    private void displayMap()
    {
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        // draw top-down, left-right
        
        int xd = -108;
        int yd = -54;
        
        int mapW = map.getWidth()/Map.SUB;
        int mapH = map.getHeight()/Map.SUB;
        
        int centerI = ((centerX - displayWidth / 2) / -xd - (centerY - displayHeight / 2) / yd) / 2 + 1;
        int centerJ = ((centerX - displayWidth / 2) / xd - (centerY - displayHeight / 2) / yd) / 2 + 1;  
        
        int iMin = Math.max(0, centerI-viewDist);
        int iMax = Math.min(mapW, centerI+viewDist);
        
        int jMin = Math.max(0, centerJ-viewDist);
        int jMax = Math.min(mapH, centerJ+viewDist);
        
        // Hajo: first, draw floors
        
        for(int i = iMin; i < iMax; i++)
        {
            for(int j = jMin; j < jMax; j++)
            {
                int x0 = centerX + i*xd - j*xd;
                int y0 = centerY + i*yd + j*yd;

                drawFloor(xd, i, j, x0, y0);
            }
        }
        
        // Hajo: then, floor decorations
        
        for(int i = iMin; i < iMax; i++)
        {
            for(int j = jMin; j < jMax; j++)
            {
                int x0 = centerX + i*xd - j*xd;
                int y0 = centerY + i*yd + j*yd;

                drawFloorDecoration(xd, yd, i, j, x0, y0);
            }
        }

        // Hajo: now, draw everything that rises up
        
        // Hajo: we need to maintain a list for mobs
        // since the map can only hold one mob per 
        // cell
        buildVList();

        for(int i = iMin; i < iMax; i++)
        {
            for(int j = jMin; j < jMax; j++)
            {
                drawItems(xd, yd, i, j);
            }
        }
        
        // glBlendFunc(GL_SRC_ALPHA, GL_DST_ALPHA);
        glBlendFunc(GL_ONE, GL_ONE);
        
        for(Light light : map.lights)
        {
            int x0 = centerX + light.x*xd/Map.SUB - light.y*xd/Map.SUB;
            int y0 = centerY + light.x*yd/Map.SUB + light.y*yd/Map.SUB; 
            
            x0 += 108;
            y0 += 108 + light.yoff;

            drawLight(lightTextures[light.type], x0, y0, light.argb, light.size);
        }
   }

    private void drawItems(int xd, int yd, int i, int j)
    {
        int x0 = centerX + i*xd - j*xd;
        int y0 = centerY + i*yd + j*yd;
        
        if(y0 < displayHeight + 200 && y0 > -500 && x0 > -400 && x0 < displayWidth + 400)
        {
            int mapI = i * Map.SUB;
            int mapJ = j * Map.SUB;

            int c1 = map.getColor(mapI, mapJ);

            if(c1 != 0)  // is this square visible?
            {
                int n;
                n = map.getLeftWall(mapI, mapJ);
                if(n > 0)
                {
                    drawWall(textureCache.textures[n], x0, y0 - yd -10);
                }

                n = map.getRightWall(mapI, mapJ);
                if(n > 0)
                {
                    drawWall(textureCache.textures[n], x0 - xd, y0 - yd -10);
                }


                // upper half of the diamond
                for(int line = 0; line < Map.SUB-1; line++)
                {
                    int jj = 0;
                    int ii = line;
                    int maxStep = line+1;

                    for(int step = 0; step < maxStep; step++)
                    {
                        int x = x0 - xd + ii * xd / Map.SUB - jj * xd / Map.SUB;
                        int y = y0 - xd + ii * yd / Map.SUB + jj * yd / Map.SUB;

                        n = map.getItem(mapI + ii, mapJ + jj);
                        // map.setItem(mapI + ii, mapJ + jj, 1004);

                        drawItem(xd, yd, mapI + ii, mapJ + jj, n, x, y);

                        ii --;
                        jj ++;
                    }
                }

                // lower half of the diamond
                for(int line = 0; line < Map.SUB; line++)
                {
                    int jj = line;
                    int ii = Map.SUB - 1;
                    int maxStep = Map.SUB - line;

                    for(int step = 0; step < maxStep; step++)
                    {
                        int x = x0 - xd + ii* xd / Map.SUB - jj* xd / Map.SUB;
                        int y = y0 - xd + ii* yd / Map.SUB + jj* yd / Map.SUB;

                        n = map.getItem(mapI + ii, mapJ + jj);
                        // map.setItem(mapI + ii, mapJ + jj, 1028);

                        drawItem(xd, yd, mapI + ii, mapJ + jj, n, x, y);

                        ii --;
                        jj ++;
                    }
                }
            }
        }
    }

    private void drawItem(int xd, int yd, int mi, int mj, int n, int x, int y) 
    {
        if(n > 0)
        {
            if((n & Map.F_FLOOR_DECO) == 0)
            {
                if((n & Map.F_ITEM) == 0)
                {
                    // Hajo: unregistered item
                    Texture tex = textureCache.textures[n];
                    if(tex == null)
                    {
                        logger.log(Level.SEVERE, "No item texture loaded for id={0}", n);
                    }
                    else
                    {
                        drawTile(tex, x-tex.footX, y - tex.image.getHeight() + tex.footY);

                        String name = decoDisplayNames[n];
                        if(name != null)
                        {
                            showName(name, mi, mj, x, y);
                        }
                    }
                }
                else
                {
                    int key = n & 0xFFFF;
                    Item item = items.get(key);

                    Texture tex = textureCache.textures[item.texId];

                    if(tex == null)
                    {
                        logger.log(Level.SEVERE, "No deco texture loaded for id={0}", item.texId);
                    }
                    else
                    {
                        drawTile(tex, x - tex.footX, y - tex.image.getHeight() + tex.footY);
                        showName(item.name, mi, mj, x, y);
                    }
                }
            }
        }

        // Hajo: debug blocked areas
                    /*
                    if(map.isPlacementBlocked(mapI+ii, mapJ+jj))
                    {
                        drawTileStanding(TextureCache.textures[1028], x, y);
                    }
                    */

/*
                    if(map.isMovementBlocked(mapI+ii, mapJ+jj))
                    {
                        Texture tex = textureCache.textures[1008];
                        drawTile(tex, x - tex.footX, y - tex.image.getHeight() + tex.footY);
                    }
*/

                    /*
                    if(centerI*Map.SUB == mapI && centerJ*Map.SUB == mapJ)
                    {
                        drawTileStanding(TextureCache.textures[13], x, y);
                    }
                    */

        n = map.getMob(mi, mj);
        if(n > 0)
        {
            // more drawables here?
            DrawableVerticalList.DrawableLink dl = vList.get(mi, mj);

            while(dl != null)
            {
                Mob mob = mobs.get(dl.id);

                if(mob != null)
                {
                    int xoff = (mob.iOff * xd / Map.SUB - mob.jOff * xd / Map.SUB) >> 16;
                    int yoff = (mob.iOff * yd / Map.SUB + mob.jOff * yd / Map.SUB) >> 16;

                    mob.visuals.display(this, x + xoff, y + yoff, mob.zOff >> 16);
                    font.drawStringScaled("#" + mob.getKey(), 0xFFFFFFFF, x + xoff - 10, y + yoff + 30, 0.5);
                }
                
                dl = dl.next;
            }
        }

        // Hajo: spell effects and such
        Drawable drawable = map.getEffect(mi, mj);
        if(drawable != null)
        {
            drawable.display(this, x, y, 0);
        }

        if(mi == cursorI && mj == cursorJ && cursorN > 0)
        {
            Texture tex = textureCache.textures[cursorN];
            drawTile(tex, x -tex.footX, y - tex.image.getHeight() + tex.footY);
        }
    }

    private void showName(String name, int mi, int mj, int x, int y)
    {
        if(showItemNames ||
                        (Math.abs(cursorI - mi) < 2 && Math.abs(cursorJ - mj) < 2))
        {
            hotspotMap.addHotspot(mi, mj,
                    x + (int)(font.getStringWidth(name) * 0.25),
                    y + 16,
                    name );
        }
    }

    private void drawFloorDecoration(int xd, int yd, int i, int j, int x0, int y0)
    {
        if(y0 < displayHeight && y0 > -216 && x0 > -216 && x0 < displayWidth)
        {
            int mapI = i * Map.SUB;
            int mapJ = j * Map.SUB;

            for(int jj=0; jj<Map.SUB; jj++)
            {
                for(int ii=0; ii<Map.SUB; ii++)
                {
                    int x = x0 - xd + ii* xd /Map.SUB - jj* xd /Map.SUB;
                    int y = y0 - xd + ii* yd /Map.SUB + jj* yd /Map.SUB;

                    int n = map.getItem(mapI+ii, mapJ+jj);

                    if(n > 0)
                    {
                        if((n & Map.F_FLOOR_DECO) != 0)
                        {
                            Texture tex = textureCache.textures[n & 0xFFFF];
                            drawTile(tex, x-tex.footX, y - tex.image.getHeight() + tex.footY);
                        }
                    }
                }
            }
        }
    }

    private void drawFloor(int xd, int i, int j, int x0, int y0)
    {
        if(y0 < displayHeight && y0 > -216 && x0 > -216 && x0 < displayWidth)
        {
            int mapI = i * Map.SUB;
            int mapJ = j * Map.SUB;

            int n = map.getFloor(mapI, mapJ);
            int c1 = map.getColor(mapI, mapJ);
            if(n > 0 && c1 != 0)
            {
                Texture tex = textureCache.grounds[n];

                if(tex != null)
                {
                    int c2 = map.getColor(mapI+Map.SUB, mapJ);
                    int c3 = map.getColor(mapI+Map.SUB, mapJ+Map.SUB);
                    int c4 = map.getColor(mapI, mapJ+Map.SUB);

                    drawGround(tex, x0, y0, c1, c2, c3, c4);
                }
                else
                {
                    logger.log(Level.SEVERE, "No floor texture loaded for id={0}", n);
                }
            }

            n = map.getWay(mapI, mapJ);
            if(n > 0 && c1 != 0)
            {
                Texture tex = textureCache.grounds[n];

                if(tex != null)
                {
                    int c2 = map.getColor(mapI+Map.SUB, mapJ);
                    int c3 = map.getColor(mapI+Map.SUB, mapJ+Map.SUB);
                    int c4 = map.getColor(mapI, mapJ+Map.SUB);

                    drawGround(tex, x0, y0, c1, c2, c3, c4);
                }
            }

            n = map.getWayLikeItem(mapI, mapJ);
            if(n > 0 && c1 != 0)
            {
                Texture tex = textureCache.textures[n];

                if(tex != null)
                {
                    drawTile(tex, x0 - xd - tex.footX, y0 - xd - tex.image.getHeight() + tex.footY);
                }
            }
        }
    }

    public void run()
    {
        long lastTime = 0;
        int frameCount = 0;
        
        while (!Display.isCloseRequested() && 
               !quitRequested)
        {            
            long currentTime = System.currentTimeMillis();
            Clock.update(currentTime);
            
            if (Display.isVisible())
            {
                if(lastTime < currentTime - 30000)
                {
                    lastTime = currentTime;
                    // Display.setTitle(title + " FPS: " + frameCount );
                    System.err.println("Average FPS last 30 seconds: " + (frameCount + 15)/30);
                    frameCount = 0;
                }
                
                if(newDisplaySize.width != displayWidth || newDisplaySize.height != displayHeight)
                {
                    resizeGL(newDisplaySize.width, newDisplaySize.height);
                }
                
                keyHandler.processKeyboard();
                mouseHandler.processMouse();

                render(currentTime);
                
                frameCount ++;
            } 
            else
            {
                if (Display.isDirty())
                {
                    render(currentTime);
                }
                safeSleep(100);
            }
            
            update();
            
            Display.sync(60);
        }
    }

    
    public void update()
    {
        Display.update();

        synchronized(mobs)
        {
            ArrayList <Cardinal> killList = new ArrayList<>();
            
            Set <Cardinal> keys = new HashSet<>(mobs.keySet());

            for(Cardinal key : keys)
            {
                Mob mob = mobs.get(key.intValue());
                mob.update(mobs);
                
                // check for dead intruders
                if(mob.kind == Mob.KIND_INTRUDER &&     
                   mob.stats.getCurrent(MobStats.VITALITY) == 0)
                {
                    killList.add(key);
                }
            }
            
            // clean up killed intruders
            for (Cardinal key : killList)
            {
                logger.info("Removing dead intruder #" + key.intValue());
                mobs.remove(key.intValue());
            }
        }
    }

    
    private void safeSleep(int millis)
    {
        try
        {
            Thread.sleep(millis);
        } 
        catch (InterruptedException ex)
        {
        }
    }

    private void exitOnGLError(String errorMessage)
    {
        int errorValue = glGetError();

        if (errorValue != GL_NO_ERROR)
        {
            String errorString = gluErrorString(errorValue);
            System.err.println("ERROR - " + errorMessage + ": " + errorString);

            if (Display.isCreated())
            {
                Display.destroy();
            }
            
            System.exit(-1);
        }
    }
    
    public static void drawTileStanding(Texture tex, int x, int y) 
    {
        x -= tex.image.getWidth()/2;
        
        drawTile(tex, x, y);
    }
    
    public static void drawTileCentered(Texture tex, int x, int y) 
    {
        x -= tex.image.getWidth()/2;
        y -= tex.image.getHeight()/2;
        
        drawTile(tex, x, y);
    }
    
    private void drawLight(Texture tex, int x, int y, int argb, double size) 
    {
        int w = (int)(tex.image.getWidth() * size);
        int h = (int)(tex.image.getHeight() * size);
        
        x -= w/2;
        y -= h/2;
        
        glBindTexture(GL_TEXTURE_2D, tex.id);
        glBegin(GL_QUADS);

        GlUtils.colorArgb(argb);
        
        glTexCoord2f(0.0f, 1.0f);
        glVertex2i(x, y);

        glTexCoord2f(1.0f, 1.0f);
        glVertex2i(x+w, y);

        glTexCoord2f(1.0f, 0.0f);
        glVertex2i(x+w, y+h);

        glTexCoord2f(0.0f, 0.0f);
        glVertex2i(x, y+h);

        glEnd();
    }
    
    public static void drawTile(Texture tex, int x, int y) 
    {
        int w = tex.image.getWidth();
        int h = tex.image.getHeight();

        drawTile(tex, x, y, w, h, 1.0f, 1.0f, 1.0f, 1.0f);
    }
    
    public static void drawTile(Texture tex, int x, int y, int argb) 
    {
        int w = tex.image.getWidth();
        int h = tex.image.getHeight();

        drawTile(tex, x, y, w, h, argb);
    }

    public static void drawTile(Texture tex, int x, int y, int w, int h, int argb) 
    {
        glBindTexture(GL_TEXTURE_2D, tex.id);
        glBegin(GL_QUADS);

        GlUtils.colorArgb(argb);

        glTexCoord2f(0.0f, 1.0f);
        glVertex2i(x, y);

        glTexCoord2f(1.0f, 1.0f);
        glVertex2i(x+w, y);

        glTexCoord2f(1.0f, 0.0f);
        glVertex2i(x+w, y+h);

        glTexCoord2f(0.0f, 0.0f);
        glVertex2i(x, y+h);

        glEnd();
    }

    public static void drawTile(Texture tex, int x, int y, int w, int h, float a, float r, float g, float b) 
    {
        glBindTexture(GL_TEXTURE_2D, tex.id);
        glBegin(GL_QUADS);

        glColor4f(r, g, b, a);

        glTexCoord2f(0.0f, 1.0f);
        glVertex2i(x, y);

        glTexCoord2f(1.0f, 1.0f);
        glVertex2i(x+w, y);

        glTexCoord2f(1.0f, 0.0f);
        glVertex2i(x+w, y+h);

        glTexCoord2f(0.0f, 0.0f);
        glVertex2i(x, y+h);

        glEnd();
    }

    public static void drawWall(Texture tex, int x, int y) 
    {
        int w = tex.image.getWidth();
        int h = tex.image.getHeight();
        
        glBindTexture(GL_TEXTURE_2D, tex.id);
        glBegin(GL_QUADS);

        glColor3f(1.0f, 1.0f, 1.0f);
        glTexCoord2f(0.0f, 1.0f);
        glVertex2i(x, y);

        glTexCoord2f(1.0f, 1.0f);
        glVertex2i(x+w, y);

        glTexCoord2f(1.0f, 0.0f);
        glVertex2i(x+w, y+h);

        glTexCoord2f(0.0f, 0.0f);
        glVertex2i(x, y+h);

        glEnd();
    }

    public static void drawGround(Texture tex, int x, int y, int c1, int c2, int c3, int c4)
    {
        glBindTexture(GL_TEXTURE_2D, tex.id);

        glBegin(GL_QUADS);

        GlUtils.colorRgb(c3);
        glTexCoord2f(0.0f, 1.0f);
        glVertex2i(x+108, y);

        GlUtils.colorRgb(c4);
        glTexCoord2f(1.0f, 1.0f);
        glVertex2i(x+216, y+54);
        
        GlUtils.colorRgb(c1);
        glTexCoord2f(1.0f, 0.0f);
        glVertex2i(x+108, y+108);

        GlUtils.colorRgb(c2);
        glTexCoord2f(0.0f, 0.0f);
        glVertex2i(x, y+54);

        glEnd();
    }
    
    public static void fillRect(int x, int y, int w, int h, int argb)
    {
        glBindTexture(GL_TEXTURE_2D, 0);

        glBegin(GL_QUADS);

        GlUtils.colorArgb(argb);

        glVertex2i(x, y);
        glVertex2i(x+w, y);
        glVertex2i(x+w, y+h);
        glVertex2i(x, y+h);

        glEnd();
    }

    public void drawString(String text, int color, int x, int y, double scale)
    {
        font.drawStringScaled(text, color, x, y, scale);
    }
    
    @Override
    public void displayMore() 
    {
    }

    public void centerOn(int x, int y, int iOff, int jOff) 
    {
        long xd = ((((long)x - (long)y) << 16) + (iOff - jOff)) * 108l;
        long yd = ((((long)x + (long)y) << 16) + (iOff + jOff)) * 54l;
        
        // centerX = displayWidth / 2 + (int)(xd / Map.SUB >> 16) - 32;
        // centerY = displayHeight / 2 + (int)(yd / Map.SUB >> 16) - 100;
        centerX = displayWidth / 2 + (int)(xd / Map.SUB >> 16) - 96;
        centerY = displayHeight / 2 + (int)(yd / Map.SUB >> 16) - 120;
    }

    public void centerOn(Mob player) 
    {
        centerOn(player.location.x, player.location.y, player.iOff, player.jOff);        
    }

    public void setTitle(String title) 
    {
        this.title = title;
        
        if(frame != null)
        {
            frame.setTitle(title);
        }
    }

    public Point getViewPosition()
    {
        int xd = -108;
        int yd = -54;
        int centerI = ((centerX - displayWidth / 2)*Map.SUB / -xd - (centerY - displayHeight / 2)*Map.SUB / yd) / 2 + Map.SUB;
        int centerJ = ((centerX - displayWidth / 2)*Map.SUB / xd - (centerY - displayHeight / 2)*Map.SUB / yd) / 2 + Map.SUB;  
        
        return new Point(centerI + Map.SUB/2, centerJ - Map.SUB/2);
    }
    
    public MousePointerBitmap getMousePointer()
    {
        return mousePointer;
    }
    
    public void setMousePointer(MousePointerBitmap mp)
    {
        this.mousePointer = mp;

        if(mousePointer == null)
        {
            frame.setCursor(Cursor.getDefaultCursor());
        }
        else
        {
            try
            {
                BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
                int[] data = img.getRaster().getPixels(0, 0, 16, 16, (int [])null);
                
                IntBuffer ib = BufferUtils.createIntBuffer(16*16);
                for(int i=0;i<data.length;i+=4)
                {
                    ib.put(data[i] | data[i+1]<<8 | data[i+2]<<16 | data[i+3]<<24);
                }
                ib.flip();

                org.lwjgl.input.Cursor cursor = new org.lwjgl.input.Cursor(
					img.getWidth(),
					img.getHeight(),
					0,
					0,
					1,
					ib,
					null);                
                Mouse.setNativeCursor(cursor);
            } catch (LWJGLException ex)
            {
                Logger.getLogger(IsoDisplay.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public boolean getShowItemNames()
    {
        return showItemNames;
    }
    
    public void setShowItemNames(boolean yesno) 
    {
        showItemNames = yesno;
    }
    
    private void buildVList()
    {
        try
        {
            vList.clear();

            Set<Cardinal> keys = mobs.keySet();

            for(Cardinal c : keys)
            {
                Mob mob = mobs.get(c.intValue());

                vList.addDrawable(mob.visuals, mob.location.x, mob.location.y, mob.getKey());
            }
        }
        catch(Exception ex)
        {
            logger.log(Level.INFO, ex.getMessage(), ex);
        }
    }
    
    public void clear() 
    {
        glClear(GL_COLOR_BUFFER_BIT);
        hotspotMap.clear();
    }

    private class MyWindowListener implements WindowListener
    {

        public MyWindowListener()
        {
        }

        @Override
        public void windowOpened(WindowEvent e)
        {
        }

        @Override
        public void windowClosing(WindowEvent e)
        {
            quit();
        }

        @Override
        public void windowClosed(WindowEvent e)
        {
        }

        @Override
        public void windowIconified(WindowEvent e)
        {
        }

        @Override
        public void windowDeiconified(WindowEvent e)
        {
        }

        @Override
        public void windowActivated(WindowEvent e)
        {
        }

        @Override
        public void windowDeactivated(WindowEvent e)
        {
        }
    }

    private class DummyKeyHandler implements KeyHandler
    {
        @Override
        public void processKeyboard()
        {
        }
        
        @Override
        public boolean collectString(StringBuilder buffer)
        {
            return true;
        }
    }
    
    private static class DummyMouseHandler implements MouseHandler
    {
        @Override
        public void processMouse()
        {
        }
    }
}
