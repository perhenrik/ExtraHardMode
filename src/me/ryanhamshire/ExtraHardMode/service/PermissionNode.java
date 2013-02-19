package me.ryanhamshire.ExtraHardMode.service;

public enum PermissionNode {
   BYPASS("bypass"),
   /**
    * Silent
    */
   SILENT_STONE_MINING_HELP("silent.stone_mining_help"),
   SILENT_NO_PLACING_ORE_AGAINST_STONE("silent.no_placing_ore_against_stone"),
   SILENT_REALISTIC_BUILDING("silent.realistic_building"),
   SILENT_LIMITED_TORCH_PLACEMENT("silent.limited_torch_placement"),
   SILENT_NO_TORCHES_HERE("silent.no_torches_here");

   private static final String PREFIX = "ExtraHardMode.";

   private final String node;

   private PermissionNode(String subperm) {
      this.node = PREFIX + subperm;
   }

   public String getNode() {
      return node;
   }
}
