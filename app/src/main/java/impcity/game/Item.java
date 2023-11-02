package impcity.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;


public class Item
{
    public static final int ARTIFACT_T1 = 1;

    public final int texId;
    public final String name;
    public final int type;

    public Item(String name, int texId, int type)
    {
        this.name = name;
        this.texId = texId;
        this.type = type;
    }

    public Item(BufferedReader reader) throws IOException
    {
        String line;

        line = reader.readLine();

        line = reader.readLine();
        name = line.substring(5);

        line = reader.readLine();
        texId = Integer.parseInt(line.substring(6));

        line = reader.readLine();
        type = Integer.parseInt(line.substring(5));

        line = reader.readLine();
    }


    public void write(final Writer writer) throws IOException
    {
        writer.write("Item data start\n");

        writer.write("name=" + name + "\n");
        writer.write("texid=" + texId + "\n");
        writer.write("type=" + type + "\n");

        writer.write("Item data end\n");
    }
}
