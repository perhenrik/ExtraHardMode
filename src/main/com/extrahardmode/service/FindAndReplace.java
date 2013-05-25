package com.extrahardmode.service;

/**
 * Holds information about a FindAndReplace
 *
 * first is the variable/placeholder to be replaced
 * second is the value to replace the variable (placeholder) with
 *
 * @author Max
 */
public class FindAndReplace
{
    private final String replaceWith;
    private final String searchFor;

    /**
     * Constructor
     *
     * @param searchFor a String
     * @param replaceWith some other String
     */
    public FindAndReplace(String searchFor, String replaceWith)
    {
        this.searchFor = searchFor;
        this.replaceWith = replaceWith;
    }

    /**
     * What do we want to replace it with
     *
     * @return the replacer-String
     */
    public String getReplaceWith()
    {
        return replaceWith;
    }

    /**
     * What are we looking for
     *
     * @return the search-String
     */
    public String getSearchWord()
    {
        return searchFor;
    }

}
