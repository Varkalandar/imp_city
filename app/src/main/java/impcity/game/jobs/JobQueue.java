package impcity.game.jobs;

import impcity.game.ImpCity;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple priority queue for jobs
 * 
 * @author Hj. Malthaner
 */
public class JobQueue
{
    public static final Logger logger = Logger.getLogger(JobQueue.class.getName());
    
    public static final int PRI_HIGH = 0; 
    public static final int PRI_NORM = 1; 
    public static final int PRI_LOW = 2; 

    private final ArrayList<List<Job>>  queues = new ArrayList<List<Job>>();
    
    public JobQueue()
    {
        for(int i=0; i<3; i++)
        {
            queues.add(new ArrayList<Job>(64));
        }
    }
    
    public synchronized void add(Job job, int priority)
    {
        queues.get(priority).add(job);
    }
    
    public synchronized Job nextJob()
    {
        for(List<Job> queue : queues)
        {
            if(!queue.isEmpty())
            {
                return queue.remove(0);
            }
        }
        
        return null;
    }

    public synchronized boolean isEmpty()
    {
        boolean empty = true;
        
        for(List<Job> queue : queues)
        {
            if(!queue.isEmpty())
            {
                empty &= false;
            }
        }
        
        return empty;
    }

    public synchronized boolean remove(Class jc, int x, int y)
    {
        boolean ok = false;
        Point p = new Point(x, y);
        
        for(List<Job> queue : queues)
        {
            for(int i=queue.size()-1; i>=0; i--)
            {
                Job job = queue.get(i);
                // Hajo: I hope this is the right way to do it
                if(job.getClass().equals(jc))
                {
                    if(job.getLocation().equals(p))
                    {
                        ok |= (queue.remove(i) != null);
                    }
                }
            }
        }

        return ok;
    }
    
    public void read(ImpCity game, BufferedReader reader) throws IOException 
    {
        for(List<Job> queue : queues)
        {
            queue.clear();
        
            String line;

            line = reader.readLine();
            int jobCount = Integer.parseInt(line.substring(5));
            
            for(int i=0; i<jobCount; i++)
            {
                Job job;
                
                line = reader.readLine();
                if(line.contains("JobExcavate"))
                {
                    job = new JobExcavate(game, 0, 0);
                    job.read(reader);
                }
                else if(line.contains("JobClaimGround"))
                {
                    job = new JobClaimGround(game, 0, 0);
                    job.read(reader);
                }
                else
                {
                    job = null;
                    logger.log(Level.SEVERE, "Unknown job type: {0}", line);
                }
                if(job != null)
                {
                    queue.add(job);
                }
            }
        
        }
    }

    public void write(Writer writer) throws IOException 
    {
        for(List<Job> queue : queues)
        {
            writer.write("jobs=" + queue.size() + "\n");
            for(Job job : queue)
            {
                writer.write("jobType=" + job.getClass().getSimpleName() + "\n");
                job.write(writer);
            }
        }
    }
}
