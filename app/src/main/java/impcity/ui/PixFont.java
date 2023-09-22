package impcity.ui;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import impcity.game.Texture;
import impcity.ogl.GlTextureCache;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glVertex2i;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;

/**
 * A bitmap font class.
 *
 * @author Hj. Malthaner
 */
public class PixFont
{
    private final int letterWidths [] = new int [256];
    private final int letterHeights [] = new int [256];
    private final String slips [] = new String [256];

    private final int rasterX;
    private final int rasterY;
    
    /** suggested line spacing for this font */
    private int linespace;
    
    /** stores the height of the tallest letter in this font */
    private int letterHeight;

    private Texture texture;
    
    /** 
     * Get the height of the tallest letter in this font.
     * @return the height of the tallest letter in this font
     */
    public int getLetterHeight()
    {
        return letterHeight;
    }
    
    public int getLinespace()
    {
        return linespace;
    }
    
    public void setLinespace(final int linespace)
    {
        this.linespace = linespace;
    }

    public PixFont() throws IOException
    {
        this("/font/serif_crackle_32");
    }
    
    public PixFont(String font) throws IOException
    {
        texture = GlTextureCache.loadTexture(this.getClass(), font + ".png", GL_TEXTURE0, GL_RGBA);
                
        rasterX = texture.image.getWidth() / 8;
        rasterY = texture.image.getHeight() / 32;
        
        scanDimensions(font, texture.image);
        
        letterHeight = 0;
        
        for(int i=0; i<letterHeights.length; i++)
        {
            if(letterHeights[i] > letterHeight)
            {
                letterHeight = letterHeights[i];
            }
        }
        
        linespace = (int)((double)letterHeight*1.4 + 0.5);

        System.err.println("Font = " + font);
        System.err.println("rasterX = " + rasterX + " rasterY = " + rasterY);
        System.err.println("Letter height = " + letterHeight + " linespace = " + linespace);
    }

    public int [] getLetterWidths()
    {
        return letterWidths;
    }

    public int drawString(final String text, 
                           final int color, final int x, final int y)
    {
        final int letters = text.length();
        int runx = x;
        glBindTexture(GL_TEXTURE_2D, texture.id);
        
        glBegin(GL_QUADS);
        
        for(int p=0; p<letters; p++)
        {
            final char c = text.charAt(p);
            drawCharacter(runx, y, color, c);

            runx += letterWidths[c];
            
            if(p < letters-1 && slips[c] != null)
            {
                final char next = text.charAt(p+1);
                if(slips[c].indexOf(next) >= 0)
                {
                    runx --;
                }
            }            
        }
        glEnd();
        
        return runx - x;
    }

    public int drawStringScaled(final String text, 
                                 final int color, final int x, final int y,
                                 final double factor)
    {
        final int letters = text.length();
        
        glBindTexture(GL_TEXTURE_2D, texture.id);
        
        glBegin(GL_QUADS);
        int runx = 0;
        
        for(int p=0; p<letters; p++)
        {
            final char c = text.charAt(p);
            drawCharacterScaled(x+(int)(runx*factor), y, color, c, factor);

            runx += letterWidths[c];
            
            if(p < letters-1 && slips[c] != null)
            {
                final char next = text.charAt(p+1);
                if(slips[c].indexOf(next) >= 0)
                {
                    runx --;
                }
            }
        }
        glEnd();
    
        return (int)(runx * factor);
    }

    public void drawStringCentered(final String text, 
                                 final int color, final int x, final int y,
                                 final int w,
                                 final double factor)
    {
        int width = (int)(getStringWidth(text) * factor + 0.5);
        
        drawStringScaled(text, color, x + (w - width)/2, y, factor);
    }
    
    public void drawText(final String text,
                         final int color, final int left, int top, final int width,
                         int linespace, double factor)
    {
        glBindTexture(GL_TEXTURE_2D, texture.id);
        
        glBegin(GL_QUADS);
        
        // reverse scale the span
        int span = (int)(width/factor);
        
        final String breaks = " ";

        int start = 0;
        int x = 0;
        int y = 0;
        int wordWidth = 0;
        
        final int letters = text.length();
        
        // scan text, draw word by word
        for(int p=0; p<letters; p++)
        {
            final char c = text.charAt(p);
            wordWidth += letterWidths[c];

            if(p < letters-1 && slips[c] != null)
            {
                char next = text.charAt(p+1);
            
                if(slips[c].indexOf(next) >= 0)
                {
                    wordWidth --;
                }
            }
            
            if(p == letters-1 || breaks.indexOf(c) >= 0 || c == '\n')
            {
                // we found a word end

                if(x + wordWidth >= span)
                {
                    // we need a line break
                    x = 0;
                    y += linespace;
                }

                // draw word

                for(int i=start; i<=p; i++)
                {
                    final char cc = text.charAt(i);
                    drawCharacterScaled(left + (int)(x * factor), 
                                        top - (int)(y * factor), 
                                        color, cc, factor);
                    x += letterWidths[cc];
                }

                if(c == '\n')
                {
                    // hard line break
                    x = 0;
                    y += linespace;
                }
                         
                // next word;
                start = p+1;
                wordWidth = 0;
            }
        }
        glEnd();
    }

    private void drawCharacter(int x, int y, int color, int character)
    {
        float tw = 8f * rasterX;
        float th = 32f * rasterY;
        
        float tx = (character % 8) * rasterX / tw;
        float ty = (character / 8) * rasterY / th;

        float txx = tx + ((rasterX-1) / tw);
        float tyy = ty + ((rasterY-1) / th);

        glColor3f(((color >> 16) & 0xFF)/255f, ((color >> 8) & 0xFF) /255f, (color & 0xFF)/255f);
        
        glTexCoord2f(tx, tyy);
        glVertex2i(x, y);

        glTexCoord2f(txx, tyy);
        glVertex2i(x+31, y);

        glTexCoord2f(txx, ty);
        glVertex2i(x+31, y+31);

        glTexCoord2f(tx, ty);
        glVertex2i(x, y+31);
    }
    
    private void drawCharacterScaled(int x, int y, int color, int character, double factor)
    {
        float frx = rasterX - 1;
        float fry = rasterY - 1;
        
        float tw = 8f * rasterX;
        float th = 32f * rasterY;
        
        float tx = (character % 8) * rasterX / tw;
        float ty = (character / 8) * rasterY / th;

        float txx = tx + (frx / tw);
        float tyy = ty + (fry / th);

        // glColor3f(((color >> 16) & 0xFF)/255f, ((color >> 8) & 0xFF) /255f, (color & 0xFF)/255f);
        glColor4f(((color >> 16) & 0xFF)/255f, ((color >> 8) & 0xFF) /255f, (color & 0xFF)/255f, ((color >> 24) & 0xFF)/255f);
        
        glTexCoord2f(tx, tyy);
        glVertex2i(x, y);

        glTexCoord2f(txx, tyy);
        glVertex2i(x+(int)(frx*factor), y);

        glTexCoord2f(txx, ty);
        glVertex2i(x+(int)(frx*factor), y+(int)(fry*factor));

        glTexCoord2f(tx, ty);
        glVertex2i(x, y+(int)(fry*factor));
    }

    public int getStringWidth(String text)
    {
        final int letters = text.length();
        int w = 0;
        
        for(int p=0; p<letters; p++)
        {
            final char c = text.charAt(p);
            w += letterWidths[c];
        }
        
        return w;
    }

    private void scanDimensions(String font, final BufferedImage tilesheet) throws IOException
    {
        for(int letter=0; letter<256; letter++)
        {
            final int sx = (letter & 7) * rasterX;
            final int sy = (letter >> 3) * rasterY;

            final BufferedImage tile = tilesheet.getSubimage(sx, sy, rasterX, rasterY);

            boolean ok;
            
            ok = true;
            for(int x=rasterX-1; x>=0 && ok; x--)
            {
                for(int y=0; y<rasterY && ok; y++)
                {
                    final int argb = tile.getRGB(x, y);
                    if((argb >>> 24) > 127)
                    {
                        // found a colored pixel
                        letterWidths[letter] = x+1;
                        // System.err.println("Width " + ((char)letter) + " = " + letterWidths[letter]);
                        ok = false;
                    }
                }
            }
	    
            ok = true;
            for(int y=rasterY-1; y>=0 && ok; y--)
            {
                for(int x=0; x<rasterX && ok; x++)
                {
                    final int argb = tile.getRGB(x, y);
                    if((argb >>> 24) > 127)
                    {
                        // found a colored pixel
                        letterHeights[letter] = y+1;
                        // System.err.println("Height of " + letter + " is " + letterHeights[letter]);
                        ok = false;
                    }
                }
            }
        }

        InputStream in = this.getClass().getResourceAsStream(font + ".kern");
                
        if(in == null)
        {
            // no kerning adjustments ?
            letterWidths[32] = 5;
        }
        else
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            
            String line;
            
            while((line = reader.readLine()) != null)
            {
                if(line.length() > 2)
                {
                    int c = line.charAt(0);
                    if(line.charAt(1) == ' ')
                    {
                        int adjust = Integer.parseInt(line.substring(2));
                        letterWidths[c] += adjust;
                    }
                    else
                    {
                        slips[c] = line.substring(2);
                    }
                }
            }
            
            reader.close();
        }
    }
}
