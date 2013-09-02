package com.extrahardmode.service;


import java.util.Random;

/**
 * @author Diemex
 */
public class OurRandom
{
    private static Random rdm;


    public OurRandom()
    {
        rdm = new Random();
    }


    public static double nextDouble()
    {
        return rdm.nextDouble();
    }


    public static int nextInt(int range)
    {
        return rdm.nextInt(range);
    }


    public static boolean percentChance(int percentage)
    {
        return nextInt(100) < percentage;
    }


    /**
     * Create a new Random object, call when plugin is enabled to ensure it's fresh etc.
     */
    public static void reload()
    {
        rdm = new Random();
    }
}
