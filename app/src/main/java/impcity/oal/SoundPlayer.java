package impcity.oal;

import java.awt.Point;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.WaveData;

public class SoundPlayer
{
    private static final Logger logger = Logger.getLogger(SoundPlayer.class.getName());

    private boolean initialized;

    /**
     * Buffers hold sound data.
     */
    IntBuffer [] buffers;
    
    /**
     * Sources are points emitting sound.
     * It seems a source can only play one sound at a time, so this also is
     * the max. number of sounds that can be played at the same time.
     */
    IntBuffer [] sources = new IntBuffer [8];

    /**
     * Position of the source sound.
     */
    FloatBuffer sourcePos = (FloatBuffer) BufferUtils.createFloatBuffer(3).put(new float[]
    {
        0.0f, 0.0f, 0.0f
    }).rewind();
    
    /**
     * Velocity of the source sound.
     */
    FloatBuffer sourceVel = (FloatBuffer) BufferUtils.createFloatBuffer(3).put(new float[]
    {
        0.0f, 0.0f, 0.0f
    }).rewind();
    
    /**
     * Position of the listener.
     */
    FloatBuffer listenerPos = (FloatBuffer) BufferUtils.createFloatBuffer(3).put(new float[]
    {
        0.0f, 0.0f, 0.0f
    }).rewind();
    
    /**
     * Velocity of the listener.
     */
    FloatBuffer listenerVel = (FloatBuffer) BufferUtils.createFloatBuffer(3).put(new float[]
    {
        0.0f, 0.0f, 0.0f
    }).rewind();
    
    /**
     * Orientation of the listener. (first 3 elements are "at", second 3 are
     * "up")
     */
    FloatBuffer listenerOri = (FloatBuffer) BufferUtils.createFloatBuffer(6).put(new float[]
    {
        0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f
    }).rewind();


    public SoundPlayer()
    {
        buffers = new IntBuffer[256];
        
        for(int i=0; i<buffers.length; i++)
        {
            buffers[i] = BufferUtils.createIntBuffer(1);        
        }
    
        for(int i=0; i<sources.length; i++)
        {
             sources[i] = BufferUtils.createIntBuffer(1);
        }
    }
    
    /**
     * This function will load our sample data from the disk using the Alut
     * utility and send the data into OpenAL as a buffer. A source is then also
     * created to play that buffer.
     * 
     * @param sampleNames Sample file names.
     * @return true if successful, false otherwise
     */
    public boolean loadSamples(String [] sampleNames)
    {
        if(initialized)
        {
            for(int i=0; i<sampleNames.length; i++)
            {
                loadFileIntoBuffer(i, sampleNames[i]);
            }


            for (IntBuffer source : sources) 
            {
                // Bind the buffer with the source.
                AL10.alGenSources(source);

                if (AL10.alGetError() != AL10.AL_NO_ERROR)
                {
                    logger.log(Level.SEVERE, "AL Error {0}", AL10.alGetError());            
                    return false;
                }

                AL10.alSourcei(source.get(0), AL10.AL_BUFFER, buffers[0].get(0));
                AL10.alSourcef(source.get(0), AL10.AL_PITCH, 1.0f);
                AL10.alSourcef(source.get(0), AL10.AL_GAIN, 1.0f);
                AL10.alSource(source.get(0), AL10.AL_POSITION, sourcePos);
                AL10.alSource(source.get(0), AL10.AL_VELOCITY, sourceVel);

                // Do another error check and return.
                if (AL10.alGetError() != AL10.AL_NO_ERROR)
                {
                    logger.log(Level.SEVERE, "AL Error {0}", AL10.alGetError());            
                    return false;
                }
            }

            return true;
        }
        else
        {
            logger.log(Level.INFO, "Not initialized.");
            return false;
        }
    }

    /**
     * void setListenerValues()
     *
     * We already defined certain values for the Listener, but we need to tell
     * OpenAL to use that data. This function does just that.
     */
    void setListenerValues()
    {
        if(initialized)
        {
            AL10.alListener(AL10.AL_POSITION, listenerPos);
            AL10.alListener(AL10.AL_VELOCITY, listenerVel);
            AL10.alListener(AL10.AL_ORIENTATION, listenerOri);
        }
    }
    
    /**
     * We have allocated memory for our buffers and sources which needs to be
     * returned to the system. This function frees that memory.
     */
    public void destroy()
    {
        if(initialized)
        {
            for(IntBuffer source : sources)
            {
                AL10.alDeleteSources(source);
            }

            for(IntBuffer buffer : buffers)
            {
                if(buffer != null)
                {
                    AL10.alDeleteBuffers(buffer);
                }
            }
            AL.destroy();
            initialized = false;
        }
    }

    
    /**
     * Initialize OpenAL and clear the error bit.
     */
    public void init()
    {
        try
        {
            AL.create();
            initialized = true;
        }
        catch (LWJGLException le)
        {
            logger.log(Level.SEVERE, "Failed AL.create()", le);
            return;
        }
        AL10.alGetError();
    }

    public boolean play(int sample, float volume)
    {
        if(initialized)
        {
            for(IntBuffer source : sources)
            {
                if(!isPlaying(source))
                {
                    // AL10.alSourceStop(source.get(0));
                    AL10.alSourcef(source.get(0), AL10.AL_GAIN, volume);
                    AL10.alSourcei(source.get(0), AL10.AL_BUFFER, buffers[sample].get(0));
                    AL10.alSourcePlay(source.get(0));
                    return true;
                }       
            }
        }        
        return false;
    }

    public boolean playFromPosition(int sample, float volume, Point soundLocation, Point listenerLocation)
    {
        boolean ok = false;
        
        if(initialized)
        {
            int xd = soundLocation.x - listenerLocation.x;
            int yd = soundLocation.y - listenerLocation.y;
            int distance2 = xd*xd + yd*yd;
            
            volume /= 1 + (distance2 * 0.005f);  // quadratic drop

            if(volume > 1f/256f)
            {
                ok = play(sample, volume);
            }
        }
        
        return ok;
    }
    
    
    private boolean loadFileIntoBuffer(int n, String filename)
    {
        // Load wav data into a buffer.
        AL10.alGenBuffers(buffers[n]);

        if (AL10.alGetError() != AL10.AL_NO_ERROR)
        {
            logger.log(Level.SEVERE, "AL Error {0}", AL10.alGetError());            
            return false;
        }
        
        InputStream in = this.getClass().getResourceAsStream(filename);
        
        if(in == null)
        {
            logger.log(Level.SEVERE, "Input stream is null for file {0}", filename);
            return false;
        }
        
        WaveData waveFile = WaveData.create(new BufferedInputStream(in));

        if(waveFile == null)
        {
            logger.log(Level.SEVERE, "Creating wave data failed for file {0}", filename);
            return false;
        }
        
        AL10.alBufferData(buffers[n].get(0), waveFile.format, waveFile.data, waveFile.samplerate);
        waveFile.dispose();
        
        return true;
    }

    boolean isPlaying(IntBuffer source)
    {
        if(initialized)
        {
            int state = AL10.alGetSourcei(source.get(0), AL10.AL_SOURCE_STATE);

            return (state == AL10.AL_PLAYING);
        }
        
        return false;
    }
}
