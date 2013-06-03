package com.extrahardmode.module;

import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.config.RootConfig;
import com.extrahardmode.config.RootNode;
import com.extrahardmode.features.Feature;
import com.extrahardmode.service.EHMModule;
import org.apache.commons.lang.Validate;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

/**
 * Player centric actions
 *
 * @author Max
 */
public class PlayerModule extends EHMModule
{
    RootConfig CFG;

    /**
     * Constructor
     *
     * @param plugin
     */
    public PlayerModule (ExtraHardMode plugin)
    {
        super(plugin);
        CFG = plugin.getModuleForClass(RootConfig.class);
    }

    public boolean playerBypasses(Player player, Feature feature)
    {
        Validate.notNull(player, "We can't check if a Player bypasses if there is no Player!");

        final boolean bypassPermsEnabled = CFG.getBoolean(RootNode.BYPASS_PERMISSION, player.getWorld().getName());
        final boolean opsBypass = CFG.getBoolean(RootNode.BYPASS_OPS, player.getWorld().getName());
        final boolean creativeBypasses = CFG.getBoolean(RootNode.BYPASS_CREATIVE, player.getWorld().getName());

        boolean bypasses = false;

        if (bypassPermsEnabled)
            bypasses = player.hasPermission(feature.getBypassNode().getNode());
        if (!bypasses && opsBypass)
            bypasses = player.isOp();
        if (!bypasses && creativeBypasses)
            bypasses = player.getGameMode().equals(GameMode.CREATIVE);

        return bypasses;
    }

    @Override
    public void starting() {
    }

    @Override
    public void closing() {
    }
}
