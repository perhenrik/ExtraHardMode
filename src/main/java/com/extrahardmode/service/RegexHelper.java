package com.extrahardmode.service;


import java.util.regex.Pattern;

/**
 * Just some regex functions I use all the time like striping invalid characters before parsing an int
 *
 * @author Diemex
 */
public class RegexHelper
{
    private static Pattern onlyNums = Pattern.compile("[^0-9]");
    private static Pattern onlyEnum = Pattern.compile("[^A-Z_]");
    private static Pattern containsNums = Pattern.compile(".*\\d.*");
    private static Pattern containsLetters = Pattern.compile(".*[a-zA-Z_].*");


    /**
     * Returns a default value instead of a NumberFormatException when input is invalid
     *
     * @return matched number
     */
//    public static int parseNumber(String input, int defaultReturn)
//    {
//
//    }
    public static int parseNumber(String input) throws NumberFormatException
    {
        int num;
        if (containsNumbers(input))
        {
            input = onlyNums.matcher(input).replaceAll("");
            num = Integer.parseInt(input);
        } else
            throw new NumberFormatException("Not a readable number \"" + input + "\"");
        return num;
    }


    public static boolean containsNumbers(String str)
    {
        return containsNums.matcher(str).matches();
    }


    public static boolean containsLetters(String str)
    {
        return containsLetters.matcher(str).matches();
    }


    public static String stripEnum(String input)
    {
        input = input.toUpperCase();
        input = onlyEnum.matcher(input).replaceAll("");
        return input;
    }
}
