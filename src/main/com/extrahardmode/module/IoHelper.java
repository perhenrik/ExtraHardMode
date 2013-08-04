package com.extrahardmode.module;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Helper Functions for general IoStuff
 *
 * @author Diemex
 */
public class IoHelper
{
    /**
     * Copy contents of one File to another
     *
     * @param sourceFile to copy from
     * @param destFile   to copy to
     * @param append     append the content of the the sourcefile to the destination file
     *
     * @throws IOException if bad stuff happens
     */
    public static void copyFile(File sourceFile, File destFile, boolean append) throws IOException
    {
        if (!destFile.exists())
        {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try
        {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile, append).getChannel();
            destination.transferFrom(source, 0L, source.size());
        } finally
        {
            if (source != null)
            {
                source.close();
            }
            if (destination != null)
            {
                destination.close();
            }
        }
    }
}
