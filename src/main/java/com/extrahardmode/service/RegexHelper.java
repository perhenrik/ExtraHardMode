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
    private static Pattern whitespace = Pattern.compile("\\s"); //Includes tabs/newline characters


    /**
     * Returns a default value instead of a NumberFormatException when input is invalid
     *
     * @return matched number
     */
    public static int parseNumber(String input, int defaultReturn)
    {
        int num = defaultReturn;
        if (containsNumbers(input))
        {
            input = stripNumber(input);
            num = Integer.parseInt(input);
        }
        return num;
    }


    public static int parseNumber(String input) throws NumberFormatException
    {
        int num;
        if (containsNumbers(input))
        {
            input = stripNumber(input);
            num = Integer.parseInt(input);
        } else
            throw new NumberFormatException("Not a readable number \"" + input + "\"");
        return num;
    }


    public static int safeCast(int input, int minValue, int maxValue)
    {
        return (input < minValue) ? minValue : (input > maxValue) ? maxValue : input;
    }


    public static Short parseShort(String input)
    {
        if (!containsNumbers(input))
            return null;
        input = stripNumber(input);

        int metaInt = Integer.parseInt(input);
        /* Prevent out of range errors */

        return (short) safeCast(metaInt, Short.MIN_VALUE, Short.MAX_VALUE);
    }


    public static Byte parseByte(String input)
    {
        if (!containsNumbers(input))
            return null;
        input = stripNumber(input);

        int metaInt = Integer.parseInt(input);
        /* Prevent out of range errors */

        return (byte) safeCast(metaInt, Byte.MIN_VALUE, Byte.MAX_VALUE);
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


    public static String stripNumber(String input)
    {
        return onlyNums.matcher(input).replaceAll("");
    }


    public static String trimWhitespace(String input)
    {
        if (whitespace.matcher(input).find())
        {
            input = whitespace.matcher(input).replaceAll("");
        }
        return input;
    }
}
