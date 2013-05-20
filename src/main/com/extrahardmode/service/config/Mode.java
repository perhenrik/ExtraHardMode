package com.extrahardmode.service.config;

/**
 * Determines how to load the specific ConfigFile
 */
public enum Mode
{
    /**
     * This is the main configFile and gets overriden by other Configs
     */
    MAIN,
    /**
     * Override the settings of the main config in specific worlds
     */
    INHERIT,
    /**
     * All options which aren't found default to disabled, this allows to only activate a few things and not having to disable everything else
     */
    DISABLE,
    /**
     * The mode hasn't been set yet
     */
    NOT_SET
}
