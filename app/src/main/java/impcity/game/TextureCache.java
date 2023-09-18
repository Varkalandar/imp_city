package impcity.game;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Hj. Malthaner
 */
public class TextureCache 
{
    private static final Logger logger = Logger.getLogger(TextureCache.class.getName());

    public final Texture [] grounds = new Texture [300];
    public final Texture [] species = new Texture [401];
    public final Texture [] textures = new Texture [2300];
    
    public void initialize(LoaderCallback callback)
    {
        if(callback != null) callback.update("Loading ground textures ...");
        loadTextures(callback.getClass(),"/tex/", "grounds/", "catalog.xml", grounds, 0);
        
        if(callback != null) callback.update("Loading creature textures ...");
        loadTextures(callback.getClass(),"/tex/", "mobs/", "catalog.xml", species, 0);
        
        if(callback != null) callback.update("Loading item textures ...");
        loadTextures(callback.getClass(),"/tex/", "items/", "catalog.xml", textures, 0);
    }

    
    public void mergeTilesFromFile(Class owner, String path, String folder, Texture [] tex, int idOffset)
    {
        loadTextures(owner, path, folder, "catalog.xml", tex, idOffset);
    }
    
    
    private void loadTextures(Class owner, String path, String folder, String fileName, Texture[] textures, int idOffset)
    {
        try 
        {
            InputStream in  = owner.getResourceAsStream(path + folder + fileName);
            
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder;
            docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(in);

            NodeList fileTypes = doc.getElementsByTagName("Tile");

            int tileCount = fileTypes.getLength();

            logger.log(Level.INFO, "Tiles in {0}: {1}", new Object [] {folder, tileCount});
            
            for (int tileIndex = 0; tileIndex < tileCount; tileIndex++) 
            {
                int tileId = -1;
                String tileName = null;
                boolean rgba = false;
                String tags = "";
                Rectangle area = new Rectangle();
                int width = 0;
                int height = 0;
                int footX = 0;
                int footY = 0;
                int alpha = 0xFF;
                
                
                Node tileNode = fileTypes.item(tileIndex);
            
                // System.err.println("- " + tileNode.getNodeValue());
                
                NodeList cnlist = tileNode.getChildNodes();
                for (int n = 0; n < cnlist.getLength(); n++) 
                {
                    Node cnNode = cnlist.item(n);
                    String childName = cnNode.getNodeName();
                    // System.err.println("-- " + name);
                    
                    if("Description".equals(childName))
                    {
                        NodeList descList = cnNode.getChildNodes();
                        
                        for (int i = 0; i < descList.getLength(); i++) 
                        {
                            Node descNode = descList.item(i);
                            String name = descNode.getNodeName();
                            // System.err.println("--- " + name);
                            
                            if("id".equals(name))
                            {
                                tileId = Integer.parseInt(descNode.getTextContent());
                                tileId += idOffset;

                                // System.err.println("tileId=" + tileId);
                            }
                            if("width".equals(name))
                            {
                                width = Integer.parseInt(descNode.getTextContent());
                            }
                            if("height".equals(name))
                            {
                                height = Integer.parseInt(descNode.getTextContent());
                            }
                            if("footX".equals(name))
                            {
                                footX = Integer.parseInt(descNode.getTextContent());
                            }
                            if("footY".equals(name))
                            {
                                footY = Integer.parseInt(descNode.getTextContent());
                            }
                        }
                    }
                    if("Metadata".equals(childName))
                    {
                        NodeList metaList = cnNode.getChildNodes();
                        int stringIndex = 0;
                        
                        for (int i = 0; i < metaList.getLength(); i++) 
                        {
                            Node metaNode = metaList.item(i);
                            String name = metaNode.getNodeName();
                            
                            // System.err.println("--- " + name + " children: " + metaNode.getChildNodes().getLength());
                            
                            if("string".equals(name) && stringIndex == 0)
                            {
                                // the tile name is the first string attribute
                                tileName = metaNode.getTextContent();
                                //System.err.println("id=" + tileId + " name='" + tileName + "'");
                                stringIndex ++;
                            }
                            else if("string".equals(name) && stringIndex == 1)
                            {
                                // the tags follow in the second string attribute
                                tags = metaNode.getTextContent();
                                //System.err.println("id=" + tileId + " tags='" + tags + "'");
                                
                                rgba = tags.contains("rgba");
                                stringIndex ++;
                            }
                            else if("string".equals(name) && stringIndex == 2)
                            {
                                // area is third string attribute
                                String areaString = metaNode.getTextContent();
                                
                                String [] parts = areaString.split("x");
                                
                                if(parts.length == 4)
                                {
                                    area.x = Integer.parseInt(parts[0]);
                                    area.y = Integer.parseInt(parts[1]);
                                    area.width = Integer.parseInt(parts[2]);
                                    area.height = Integer.parseInt(parts[3]);
                                }
                                else
                                {
                                    if(parts.length > 1)
                                    {
                                        logger.log(Level.INFO, "id={0}: Expected 4 area paramaters, got {1}", new Object[]{tileId, parts.length});
                                    }
                                }
                                // System.err.println("id=" + tileId + " area=" + area);
                                stringIndex ++;
                            }
                        }
                    }
                }

                if(tileName != null && tileName.length() > 0)
                {
                    String filename = tileId + "-" + tileName + ".png";

                    
                    int stackRun = 1;
                    int p = tags.indexOf("n=");
                    if(p != -1)
                    {
                        stackRun = Integer.parseInt(tags.substring(p+2, p+3));
                    }    
                    
                    
                    // System.err.println("loading: " + filename + " rgba=" + rgba + " tileId=" + tileId + 
                    //                   " area=" + area + " stackRun=" + stackRun);
                    
                    
                    if(width > 1 || height > 1)
                    {
                        Texture tex;
                        tex = loadTexture(path + folder + filename, rgba);
                    
                        // Hajo: legacy/unset?
                        if(footX == 0 && footY == 0)
                        {
                            footX = width / 2;
                        }

                        textures[tileId] = new Texture(tex.id, tex.image, tileName, tags, area, footX, footY, 
                                                       stackRun, tileIndex, tileId);
                    }
                }
            }            
        }
        catch (ParserConfigurationException ex) 
        {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
        catch (SAXException ex) 
        {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
        catch (IOException ex) 
        {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    
    public Texture getStackTexture(int n, int count)
    {
        // are there several stack size images available?

        Texture tex = textures[n];

        if(count > tex.stackRun) count = tex.stackRun;

        // show proper stack size
        tex = textures[n + count - 1];
        
        return tex;
    }

    public Texture loadTexture(String filename, boolean hasAlpha) throws IOException
    {
        InputStream in = Class.class.getResourceAsStream(filename);
        BufferedImage img = ImageIO.read(in);
        in.close();

        return new Texture(-1, img);
    }
    
    public static interface LoaderCallback
    {
        public void update(String what);
    }
}
