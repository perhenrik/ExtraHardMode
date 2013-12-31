package com.extrahardmode.compatibility;


import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

/**
 * @author Diemex
 */
public class CompatWorldGuard implements ICompat, IMonsterProtection
{
    private WorldGuardPlugin worldguardPlugin = null;


    @Override
    public boolean isEnabled()
    {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
        if (plugin instanceof WorldGuardPlugin)
            worldguardPlugin = (WorldGuardPlugin) plugin;
        return worldguardPlugin != null;
    }


    @Override
    public boolean denySpawn(Location loc)
    {
        return !getRegion(loc).allows(DefaultFlag.MOB_SPAWNING);
    }


    private ApplicableRegionSet getRegion(Location loc)
    {
        Validate.notNull(worldguardPlugin, "You derped up and didn't check if WorldGuard is enabled!");
        RegionManager manager = worldguardPlugin.getRegionManager(loc.getWorld());
        return manager.getApplicableRegions(loc);
    }
}
