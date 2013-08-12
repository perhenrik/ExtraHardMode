package com.extrahardmode.compatibility;


import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.service.EHMModule;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;

/**
 * Handles compatibility for all supported plugins in one class
 *
 * @author Diemex
 */
public class CompatHandler extends EHMModule
{
    private static Set<IBlockProtection> blockProtectionPls;


    private static Set<IMonsterProtection> monsterProtectionPls;

    public CompatHandler(ExtraHardMode plugin)
    {
        super(plugin);
    }


    public static boolean isExplosionProtected(Location loc)
    {
        for (IBlockProtection prot : blockProtectionPls)
            if (prot.isExplosionProtected(loc))
                return true;
        return false;
    }


    public static boolean canMonsterSpawn(Location loc)
    {
        for (IMonsterProtection prot : monsterProtectionPls)
            if (prot.denySpawn(loc))
                return true;
        return false;
    }


    @Override
    public void starting()
    {
        blockProtectionPls = new HashSet<IBlockProtection>();
        monsterProtectionPls = new HashSet<IMonsterProtection>();
        //BlockProtection plugins
        WorldGuard w = new WorldGuard();
        if (w.isEnabled())
            blockProtectionPls.add(w);
    }


    @Override
    public void closing()
    {
        blockProtectionPls = null;
    }
}
