package com.extrahardmode.service;


import java.util.Random;

/**
 * @author Diemex
 */
public class OurRandom
{
    private static Random rdm = new Random();


    public static double nextDouble()
    {
        return rdm.nextDouble();
    }


    public static int nextInt(int range)
    {
        return rdm.nextInt(range);
    }
}
