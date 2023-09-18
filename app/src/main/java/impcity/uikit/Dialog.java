package impcity.uikit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import impcity.ui.PixFont;

/**
 *
 * @author Hj. Malthaner
 */
public class Dialog 
{
    public final HashMap <String, String> valueMap = new HashMap <String, String> ();
    public final HashMap <String, DisplayElement> displayElements = new HashMap<String, DisplayElement> ();
    public final ArrayList <Trigger> triggers = new ArrayList <Trigger> ();
            
    private final PixFont font;
    
    public Dialog(PixFont font)
    {
        this.font = font;
    }
    
    
    public void parse(InputStream is) throws IOException
    {
        InputStreamReader ir = new InputStreamReader(is);
        BufferedReader reader = new BufferedReader(ir);
        
        String line;
        
        line = reader.readLine();
        String separator = "" + line.charAt(0);
        
        while((line = reader.readLine()) != null)
        {
            String [] parts = line.split(separator);
            
            if(parts[0].equals("label"))
            {
                DisplayLabel e = new DisplayLabel();
                
                e.key = parts[1];
                e.area.x = Integer.parseInt(parts[2]);
                e.area.y = Integer.parseInt(parts[3]);
                e.font = font;
                e.fontScale = Double.parseDouble(parts[4]);
                        
                if(parts.length > 5)
                {
                    e.color = Integer.parseInt(parts[5].substring(2), 16);
                }
                
                displayElements.put(e.key, e);
                
                // System.err.println("key=" + e.key + " x=" + e.area.x + " y=" + e.area.y);
            }
                
            if(parts[0].equals("text"))
            {
                DisplayLabel e = new DisplayLabel();
                
                e.value = parts[1];
                e.area.x = Integer.parseInt(parts[2]);
                e.area.y = Integer.parseInt(parts[3]);
                e.font = font;
                e.fontScale = Double.parseDouble(parts[4]);
                e.color = Integer.parseInt(parts[5].substring(2), 16);

                e.key = e.value + e.area.x + e.area.y;

                displayElements.put(e.key, e);
            }

            if(parts[0].equals("trigger"))
            {
                Trigger e = new Trigger();
                
                e.key = parts[1];
                e.setArea(
                        Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[3]),
                        Integer.parseInt(parts[4]),
                        Integer.parseInt(parts[5])
                );
                
                e.font = font;
                        
                triggers.add(e);
                
                /*
                System.err.println("key=" + e.key + 
                        " x=" + e.area.x + " y=" + e.area.y + 
                        " w=" + e.area.width + " h=" + e.area.height);
                */
            }
        }
            
        reader.close();
    }
    
    public void display(int xpos, int ypos)
    {
        displayBackground(xpos, ypos);
        
        Set <String> keys = displayElements.keySet();
        
        for(String key: keys)
        {
            DisplayElement e = displayElements.get(key);

            String value = valueMap.get(key);
            if(value != null)
            {
                e.value = value;
            }
            
            e.display(xpos, ypos);
       }
    }
    
    public Trigger processMouseEvent(MouseEvent event)
    {
        Trigger result = null;
        
        if(event.buttonPressed == 0)
        {
            for(Trigger t : triggers)
            {
                t.activated = false;
            }            
        }
        else if(event.buttonPressed >= 1)
        {
            for(Trigger t : triggers)
            {
                t.handleMouseEvent(event);
                if(t.activated)
                {
                    result = t;
                    event.consume();
                    break;
                }
            }
        }
        
        return result;
    }

    public void processKeyEvent(KeyEvent event)
    {
        Collection <DisplayElement> elements = displayElements.values();
        
        for(DisplayElement element : elements)
        {
            element.handleKeyEvent(event);
        }
    }

    protected void displayBackground(int xpos, int ypos) 
    {
    }
}
