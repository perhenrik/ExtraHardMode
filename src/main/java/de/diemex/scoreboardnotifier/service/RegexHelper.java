/*
 * This file is part of
 * KeepXP Server Plugin for Minecraft
 *
 * Copyright (C) 2013 Diemex
 *
 * KeepXP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * KeepXP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero Public License
 * along with KeepXP.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.diemex.scoreboardnotifier.service;


import java.util.regex.Pattern;

/**
 * @author Diemex
 */
public class RegexHelper
{
    private static Pattern onlyNums = Pattern.compile("[^0-9]");
    private static Pattern onlyEnum = Pattern.compile("[^A-Z_]");


    public static int parseNumber(String input) throws NumberFormatException
    {
        int num;
        input = onlyNums.matcher(input).replaceAll("");
        if (input.length() > 0)
            num = Integer.parseInt(input);
        else
            throw new NumberFormatException("Not a readable number \"" + input + "\"");
        return num;
    }


    public static String stripEnum(String input)
    {
        input = input.toUpperCase();
        input = onlyEnum.matcher(input).replaceAll("");
        return input;
    }
}
