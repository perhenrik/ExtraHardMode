package com.extrahardmode.features.monsters.skeletors;


import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.service.ListenerModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * @author Diemex
 */
public class CustomMinionSkeliListener extends ListenerModule
{
    public CustomMinionSkeliListener(ExtraHardMode plugin)
    {
        super(plugin);
    }


    @EventHandler
    private void onEntityDamage(EntityDamageByEntityEvent event)
    {

    }


    @EventHandler
    private void onEntityDeath(EntityDeathEvent event)
    {

    }
}
