package me.ryanhamshire.ExtraHardMode.service;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;

public abstract class EHMModule implements IModule {

   protected ExtraHardMode plugin;

   public EHMModule(ExtraHardMode plugin) {
      this.plugin = plugin;
   }
}
