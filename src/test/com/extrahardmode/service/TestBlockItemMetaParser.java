/*
 * This file is part of
 * ExtraHardMode Server Plugin for Minecraft
 *
 * Copyright (C) 2012 Ryan Hamshire
 * Copyright (C) 2013 Diemex
 *
 * ExtraHardMode is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ExtraHardMode is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero Public License
 * along with ExtraHardMode.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.extrahardmode.service;


import com.extrahardmode.service.config.Status;
import org.bukkit.Material;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 *
 */
public class TestBlockItemMetaParser
{
    /**
     * Test if simple enum values get processed correctly
     */
    @Test
    public void basicGetFallingBlocks()
    {
        /* Just some basic plain enums */
        List<String> fallingBlocks = new ArrayList<String>(2);
        fallingBlocks.add(String.valueOf(Material.ANVIL.getId()));
        fallingBlocks.add(String.valueOf(Material.COBBLESTONE.getId()));
        Response<Map<Integer, List<Byte>>> retrievedData = SpecialParsers.parseMaterials(fallingBlocks);

        assertTrue("Basic data retrival", retrievedData.getContent().containsKey(Material.ANVIL.getId()));
        assertEquals("No data inserted so we should get no data back", Collections.<Byte>emptyList(), retrievedData.getContent().get(Material.ANVIL.getId()));
        assertSame("Nothing has been adjusted", retrievedData.getStatusCode(), Status.OK);

        assertTrue("Basic Data retrival", retrievedData.getContent().containsKey(Material.COBBLESTONE.getId()));
        assertEquals("No data inserted so we should get no data back", Collections.<Byte>emptyList(), retrievedData.getContent().get(Material.ANVIL.getId()));
    }


    /**
     * Will whitespace be trimmed?
     */
    @Test
    public void whitespaceGetFallingBlocks()
    {
        List<String> fallingBlocks = new ArrayList<String>(1);
        fallingBlocks.add("   3 4 ");

        Response<Map<Integer, List<Byte>>> retrievedData = SpecialParsers.parseMaterials(fallingBlocks);

        assertTrue("Will the String be trimmed and still get recognixed", retrievedData.getContent().containsKey(34));
        assertEquals("No data inserted", Collections.<Byte>emptyList(), retrievedData.getContent().get(34));
        Assert.assertSame("Our value has been adjusted", retrievedData.getStatusCode(), Status.NEEDS_TO_BE_ADJUSTED);
    }


    /**
     * Will enums get converted to the block ids?
     */
    @Test
    public void convertGetFallingBlocks()
    {

        List<String> fallingBlocks = new ArrayList<String>(2);
        fallingBlocks.add(Material.ANVIL.name());
        fallingBlocks.add(Material.BEACON.name());

        Response<Map<Integer, List<Byte>>> retrievedData = SpecialParsers.parseMaterials(fallingBlocks);

        assertTrue(retrievedData.getContent().containsKey(Material.ANVIL.getId()));
        assertTrue(retrievedData.getContent().containsKey(Material.BEACON.getId()));

        assertEquals(Collections.<Byte>emptyList(), retrievedData.getContent().get(Material.ANVIL.getId()));
        assertEquals(Collections.<Byte>emptyList(), retrievedData.getContent().get(Material.BEACON.getId()));

        assertSame("Nothing has been adjusted", retrievedData.getStatusCode(), Status.OK);
    }


    /**
     * Will simple metadata be attached correctly
     */
    @Test
    public void metaGetFallingBlocks()
    {

        List<String> fallingBlocks = new ArrayList<String>(1);
        fallingBlocks.add(Material.BED.getId() + "@" + "1");

        Response<Map<Integer, List<Byte>>> retrievedData = SpecialParsers.parseMaterials(fallingBlocks);

        assertTrue(retrievedData.getContent().containsKey(Material.BED.getId()));

        byte bit = retrievedData.getContent().get(Material.BED.getId()).get(0);
        assertEquals("First element should be the meta we inserted", (byte) 1, bit);

        assertSame("Nothing has been adjusted", retrievedData.getStatusCode(), Status.OK);
    }


    /**
     * More ids per Material
     */
    @Test
    public void moreMetaGetFallingBlocks()
    {
        List<String> fallingBlocks = new ArrayList<String>(1);
        fallingBlocks.add(Material.BED.getId() + "@1,2,3");

        Response<Map<Integer, List<Byte>>> retrievedData = SpecialParsers.parseMaterials(fallingBlocks);

        assertTrue(retrievedData.getContent().containsKey(Material.BED.getId()));

        List<Byte> meta = retrievedData.getContent().get(Material.BED.getId());

        assertEquals((byte) 1, (byte) meta.get(0));
        assertEquals((byte) 2, (byte) meta.get(1));
        assertEquals((byte) 3, (byte) meta.get(2));

        assertSame("Nothing has been adjusted", retrievedData.getStatusCode(), Status.OK);
    }


    /**
     * Bogus input
     */
    @Test
    public void invalidGetFallingBlocks()
    {
        List<String> fallingBlocks = new ArrayList<String>();
        fallingBlocks.add("34@12fag4");
        fallingBlocks.add("25adsaf:12d");

        Response<Map<Integer, List<Byte>>> retrievedData = SpecialParsers.parseMaterials(fallingBlocks);

        assertEquals(true, retrievedData.getContent().containsKey(34));
        assertEquals((byte) 124, (byte) retrievedData.getContent().get(34).get(0));

        assertEquals(true, retrievedData.getContent().containsKey(25));
        assertEquals((byte) 12, (byte) retrievedData.getContent().get(25).get(0));

        assertSame("All the wrong characters should be stripped", retrievedData.getStatusCode(), Status.NEEDS_TO_BE_ADJUSTED);
    }


    /**
     * Same block multiple times with different meta
     */
    @Test
    public void mergeMetaTest()
    {
        List<String> fallingBlocks = new ArrayList<String>();
        fallingBlocks.add("12:1");
        fallingBlocks.add("12:2");

        Response<Map<Integer, List<Byte>>> retrievedData = SpecialParsers.parseMaterials(fallingBlocks);

        assertEquals((byte) 1, (byte) retrievedData.getContent().get(12).get(0));
        assertEquals((byte) 2, (byte) retrievedData.getContent().get(12).get(1));

        assertSame("Nothing has been adjusted", retrievedData.getStatusCode(), Status.NEEDS_TO_BE_ADJUSTED);
    }


    /**
     * BlockIds whic are out of range should be kept
     */
    @Test
    public void outOfRangeParseTest()
    {
        List<String> fallingBlocks = new ArrayList<String>();
        fallingBlocks.add("1200");

        Response<Map<Integer, List<Byte>>> retrievedData = SpecialParsers.parseMaterials(fallingBlocks);

        assertTrue("Keep blockids for mod compatibility", retrievedData.getContent().containsKey(1200));

        assertSame("Keep blockids for mod compatibility", retrievedData.getStatusCode(), Status.OK);
    }


    /**
     * BlockIds which are out of range should be kept with their meta
     */
    @Test
    public void outOfRangeParseMetaTest()
    {
        List<String> fallingBlocks = new ArrayList<String>();
        fallingBlocks.add("1200:1200");

        Response<Map<Integer, List<Byte>>> retrievedData = SpecialParsers.parseMaterials(fallingBlocks);

        assertEquals((byte) Byte.MAX_VALUE, (byte) retrievedData.getContent().get(1200).get(0));

        assertSame("Keep blockids for mod compatibility", retrievedData.getStatusCode(), Status.NEEDS_TO_BE_ADJUSTED);
    }


    /**
     * Normal with blockmeta
     */
    @Test
    public void getStringsForTest()
    {
        List<Byte> myMeta = new ArrayList<Byte>(3);
        myMeta.add((byte) 1);
        myMeta.add((byte) 2);
        myMeta.add((byte) 3);

        Map<Integer, List<Byte>> blocks = new HashMap<Integer, List<Byte>>(1);
        blocks.put(12, myMeta);

        List outPutStrings = SpecialParsers.convertToStringList(blocks);

        assertEquals("blockname@meta1,meta2,meta3 is the expected format", "SAND@1,2,3", outPutStrings.get(0));
    }


    /**
     * Normal without blockmeta
     */
    @Test
    public void getStringsForNullTest()
    {
        List<Byte> myMeta = new ArrayList<Byte>(3);
        myMeta.add((byte) 1);
        myMeta.add((byte) 2);
        myMeta.add((byte) 3);

        Map<Integer, List<Byte>> blocks = new HashMap<Integer, List<Byte>>(1);
        blocks.put(12, null);
        blocks.put(1, Collections.<Byte>emptyList());

        List outPutStrings = SpecialParsers.convertToStringList(blocks);

        assertEquals("plain enum expected", "SAND", outPutStrings.get(0));
        assertEquals("plain enum expected aswell", "STONE", outPutStrings.get(1));
    }
}