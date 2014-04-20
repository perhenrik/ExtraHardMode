package com.extrahardmode.service.config.customtypes;


import com.extrahardmode.service.RegexHelper;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Holds one blocktype, but a range of metadata for that block.
 * F.e. this could have meta for spruce, oak and jungle wood, but exclude birch.
 *
 * @author Diemex
 */
public final class BlockType
{
    private static Pattern seperators = Pattern.compile("[^A-Za-z0-9_]");
    private int blockId = -1;
    private Set<Byte> meta = new LinkedHashSet<Byte>();


    public BlockType(int blockId)
    {
        this.blockId = blockId;
    }


    public BlockType(Material mat, Byte... meta)
    {
        this.blockId = mat.getId();
        Collections.addAll(this.meta, meta);
    }


    public BlockType(int blockId, Byte... meta)
    {
        this.blockId = blockId;
        Collections.addAll(this.meta, meta);
    }


    public BlockType(int blockId, byte meta)
    {
        this.blockId = blockId;
        this.meta.add(meta);
    }


    public BlockType(int blockId, Collection<Byte> meta)
    {
        this.blockId = blockId;
        this.meta.addAll(meta);
    }


    public int getBlockId()
    {
        return blockId;
    }


    public Set<Byte> getAllMeta()
    {
        return meta;
    }


    public byte getMeta()
    {
        return meta.size() > 0 ? meta.iterator().next() : 0;
    }


    private boolean matchesMeta(byte meta)
    {
        if (this.meta.size() > 0)
        {
            for (Byte aMeta : this.meta)
            {
                if (aMeta == meta)
                    return true;
            }
        } else //no meta specified -> all blocks match
            return true;
        return false;
    }


    public boolean matches(int blockId)
    {
        return this.blockId == blockId;
    }


    public boolean matches(int blockId, byte meta)
    {
        return matches(blockId) && matchesMeta(meta);
    }


    public boolean matches(Block block)
    {
        return matches(block.getTypeId(), block.getData());
    }


    public boolean matches(ItemStack stack)
    {
        return matches(stack.getTypeId(), stack.getData().getData());
    }


    public static BlockType loadFromConfig(String input)
    {
        if (input == null)
            return null;
        //PREPARATION
        int blockId;
        Set<Byte> meta = new HashSet<Byte>();
        input = RegexHelper.trimWhitespace(input);
        String[] splitted = seperators.split(input);
        if (splitted.length == 0)
            return null;
        //BLOCK META
        for (int i = 1; i < splitted.length; i++) //first value is blockId
            meta.add(RegexHelper.parseByte(splitted[i]));

        //BLOCK ID
        String blockIdString = splitted[0];
        Material material = Material.matchMaterial(blockIdString);
        if (material == null) //Not found in material enum
        {
            // try as a number (blockId)
            String tempId = RegexHelper.stripNumber(input);
            if (!tempId.isEmpty())
                material = Material.getMaterial(tempId);
            // still fail -> try as enum again but strip numbers
            if (material == null)
                material = Material.matchMaterial(RegexHelper.stripEnum(input));
        }
        if (material != null)
            blockId = material.getId();
        else //mod item or -1 if not valid
            blockId = RegexHelper.parseNumber(input, -1);
        return new BlockType(blockId, meta);
    }


    public String saveToString()
    {
        StringBuilder builder = new StringBuilder();
        Material material = Material.getMaterial(blockId);
        builder.append(material != null ? material.name() : blockId);

        boolean first = true;
        for (Byte metaBit : meta)
        {
            if (first) builder.append('@');
            else builder.append(',');
            builder.append(metaBit);
            if (first) first = false;
        }

        return builder.toString();
    }


    public boolean isValid()
    {
        return blockId >= 0;
    }


    @Override
    public String toString()
    {
        return saveToString();
    }


    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;
        else if (obj == this)
            return true;
        else if (!(obj instanceof BlockType))
            return false;
        return new EqualsBuilder()
                .append(blockId, ((BlockType) obj).blockId)
                .append(meta, ((BlockType) obj).meta)
                .isEquals();
    }


    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(13, 53) //two primes
                .append(blockId)
                .append(meta)
                .hashCode();
    }
}
