package com.extrahardmode.task;


import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.config.RootConfig;
import com.extrahardmode.config.RootNode;
import com.extrahardmode.module.MsgModule;
import com.extrahardmode.module.PlayerModule;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 *
 */
public class ArmorWeightTask implements Runnable
{
    private final ExtraHardMode mPlugin;
    private final RootConfig CFG;
    private final MsgModule mMessenger;
    private static Set<UUID> mPlayerList = new HashSet<UUID>();


    public ArmorWeightTask(ExtraHardMode plugin)
    {
        mPlugin = plugin;
        CFG = plugin.getModuleForClass(RootConfig.class);
        mMessenger = plugin.getModuleForClass(MsgModule.class);
    }


    @Override
    public void run()
    {
        for (Player player : mPlugin.getServer().getOnlinePlayers())
        {
            final float basespeed = (float) CFG.getDouble(RootNode.ARMOR_SLOWDOWN_BASESPEED, player.getWorld().getName());
            final int slowdownPercent = CFG.getInt(RootNode.ARMOR_SLOWDOWN_PERCENT, player.getWorld().getName());
            final float armorPoints = PlayerModule.getArmorPoints(player);
            if (armorPoints != 0)
            {
                float value = basespeed * (1 - armorPoints / 0.8F * (slowdownPercent / 100F));
                player.setWalkSpeed(value);
            } else
                player.setWalkSpeed(basespeed);
        }
    }
}
