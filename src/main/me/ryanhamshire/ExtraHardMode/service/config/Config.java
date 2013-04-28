package me.ryanhamshire.ExtraHardMode.service.config;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * A Wrapper that contains
 * <ul>
 *     <li>a FileConfiguration</li>
 *     <li>A reference to the config file</li>
 *     <li>Information about the Mode this Config is loaded in</li>
 *     <li>Information about which status this Config is in</li>
 * </ul>
 */
public class Config
{
    /**
     * Loaded FileConfiguration
     */
    private FileConfiguration config;
    /**
     * Location we loaded the File from
     */
    private File configFile;
    /**
     * Mode with which this config will get loaded
     */
    private Mode mode = Mode.NOT_SET;
    /**
     * Some status information about this Config
     */
    private Status status = Status.OK;

    /**
     * Constructor
     * @param config that's loaded                         config
     * @param fullFilePath fileName including the directory!
     */
    public Config (FileConfiguration config, String fullFilePath)
    {
        this.config = config;
        configFile = new File(fullFilePath);
        if (!configFile.exists())
        {
            try
            {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
            } catch (IOException e)
            {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            Validate.isTrue(configFile.exists() && configFile.canWrite(), "FilePath " + fullFilePath + " doesn't exist or is not writable");
        }
    }

    /**
     * Constructor
     * @param config that's loaded
     * @param file File-Object to save to
     */
    public Config (FileConfiguration config, File file)
    {
        this.config = config;
        configFile = file;
    }

    /**
     * Get loaded FileConfiguration
     * @return
     */
    public FileConfiguration getConfig ()
    {
        return config;
    }

    /**
     * Set FileConfiguration
     * @param config
     */
    public void setConfig (FileConfiguration config)
    {
        this.config = config;
    }

    /**
     * Returns the File to save the FileConfiguration to
     * @return
     */
    public File getConfigFile ()
    {
        return configFile;
    }

    /**
     * Get the fileName of this Config
     */
    public String getFileName ()
    {
        return configFile.getName();
    }

    /**
     * Set where to save this Config
     * @param configFile
     */
    public void setConfigFile (File configFile)
    {
        this.configFile = configFile;
    }

    /**
     * Get the Mode this config should be loaded
     * @return mode or Mode.NOT_SET if not set yet
     */
    public Mode getMode ()
    {
        return mode;
    }

    /**
     * Set the Mode with which this Config should be loaded
     * @param mode to set
     */
    public void setMode (Mode mode)
    {
        this.mode = mode;
    }

    /**
     * Get the Status of this config
     * @return Status, initialized with Status.OK
     */
    public Status getStatus ()
    {
        return status;
    }

    /**
     * Set the Status of this Config
     * @param status of the Config
     */
    public void setStatus (Status status)
    {
        this.status = status;
    }
}
