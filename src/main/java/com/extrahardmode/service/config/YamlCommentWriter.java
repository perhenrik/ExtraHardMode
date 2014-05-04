package com.extrahardmode.service.config;


import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Adds comments to yaml files
 */
public class YamlCommentWriter
{
    /**
     * Add comments to a configuration file (after writing it with snakeyaml)
     *
     * @param input    yml config file
     * @param comments path = key, comment lines = values
     */
    public static void write(File input, Map<String, String[]> comments)
    {
        BufferedReader br;
        //output
        ByteArrayOutputStream memStream = null;
        FileOutputStream outStream = null;
        OutputStreamWriter writer = null;
        //nodes
        String[] nodes = new String[20];
        try
        {
            br = new BufferedReader(new FileReader(input));
            memStream = new ByteArrayOutputStream();
            outStream = new FileOutputStream(input);
            writer = new OutputStreamWriter(memStream, Charset.forName("UTF-8").newEncoder());

            String line;
            while ((line = br.readLine()) != null)
            {
                boolean comment = isComment(line);
                boolean listItem = isListItem(line);

                int level = getIndentation(line) / 2;

                String nodename = getNodeName(line);
                nodes[level] = nodename;
                nodes[level + 1] = null;  //bcs
                //path
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i <= level; i++)
                    if (nodes[i] != null)
                    {
                        if (i > 0)
                            sb.append('.');
                        sb.append(nodes[i]);
                    }
                String path = sb.toString();

                //we have a comment?
                if (comments.containsKey(path) && !(comment || listItem)) //TODO split long lines
                    for (String commentLine : comments.get(path))
                        writer.write(StringUtils.repeat(" ", level * 2) + "# " + commentLine + String.format("%n"));
                writer.write(line + String.format("%n"));

                line.length();  //useless breakpoint line
            }
            br.close();
            //Overwrite the original file
            memStream.writeTo(outStream);
        }
        //BLABLABLA EXCEPTIONS BLABLA
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                if (writer != null) writer.close();
                if (memStream != null) memStream.close();
                if (outStream != null) outStream.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }


    private static boolean isComment(String line)
    {
        return line.startsWith("#");
    }


    private static boolean isListItem(String line)
    {
        return line.substring(getIndentation(line)).startsWith("-");
    }


    private static String getNodeName(String line)
    {
        if (isComment(line) || !line.contains(":"))
            return null;
        return line.substring(getIndentation(line)).split(":")[0];
    }


    private static int getIndentation(String line)
    {
        int level = 0;
        for (char c : line.toCharArray())
        {
            if (c == ' ')
                level++;
            else
                break;
        }
        return level;
    }
}
