package com.extrahardmode.service.config.customtypes;


import org.bukkit.block.Block;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BlockTypeList
{
    private Map<Integer, BlockType> blockTypeMap = new HashMap<Integer, BlockType>();
    /**
     * Empty List with no values
     */
    public final static BlockTypeList EMPTY_LIST = new BlockTypeList();


    public BlockTypeList()
    {
    }


    public BlockTypeList(Collection<BlockType> blockTypes)
    {
        for (BlockType blockType : blockTypes)
            add(blockType);
    }


    public boolean contains(Block block)
    {
        BlockType type = blockTypeMap.get(block.getTypeId());
        return type != null && type.matches(block);
    }


    public boolean contains(int blockId)
    {
        return blockTypeMap.containsKey(blockId);
    }


    public void add(BlockType blockType)
    {
        blockTypeMap.put(blockType.getBlockId(), blockType);
    }


    public BlockType get(int blockId)
    {
        return blockTypeMap.get(blockId);
    }


    public Iterator iterator()
    {
        return blockTypeMap.keySet().iterator();
    }


    public BlockType[] toArray()
    {
        return blockTypeMap.values().toArray(new BlockType[blockTypeMap.size()]);
    }
}
