package impcity.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

/**
 *
 * @author Hj. Malthaner
 */
public class Clock
{
    private final static int hourShift = 12;

    private static long timeBase;
    private static long elapsedTime;
    private static int hours;
    private static int days;
    
    private static final ArrayList<ClockListener> listeners = new ArrayList<ClockListener>();
    private static final ArrayList<TimeListener> timeListeners = new ArrayList<TimeListener>();
            
    
    public static void init(long time)
    {
        timeBase = time;
        days = 0;
    }

    public static void update(long time)
    {
        elapsedTime += time - timeBase;
        timeBase = time;
        
        for(TimeListener tl : timeListeners)
        {
            tl.onNewTime(elapsedTime);
        }
        
        
        if((elapsedTime >> hourShift) > hours)
        {
            hours = (int)(elapsedTime >> hourShift);
            for(ClockListener listener : listeners)
            {
                listener.newHour(hours);
            }
            
            if(hours % 24 == 0)
            {
                days ++;
                for(ClockListener listener : listeners)
                {
                    listener.newDay(days);
                }                
            }
        }
    }

    public static long time()
    {
        return elapsedTime;
    }
    
    public static int hour()
    {
        return hours % 24;
    }
    
    public static int days()
    {
        return days;
    }
    
    public static void addClockListener(ClockListener listener)
    {
        listeners.add(listener);
    }
    
    public static void addTimeListener(TimeListener listener)
    {
        timeListeners.add(listener);
    }
    
    public static void write(Writer writer) throws IOException
    {
        writer.write("time=" + elapsedTime + "\n");
        writer.write("hours=" + hours + "\n");
        writer.write("days=" + days + "\n");
    }
    
    public static void read(BufferedReader reader) throws IOException
    {
        Clock.init(System.currentTimeMillis());

        String line;
        line = reader.readLine();
        elapsedTime = Long.parseLong(line.substring(5));
        line = reader.readLine();
        hours = Integer.parseInt(line.substring(6));
        line = reader.readLine();
        days = Integer.parseInt(line.substring(5));
    }
    
    public static interface TimeListener
    {
        public void onNewTime(long elapsedTime);
    }
    
    public static interface ClockListener
    {
        public void newHour(int hours);
        public void newDay(int days);
    }
}
