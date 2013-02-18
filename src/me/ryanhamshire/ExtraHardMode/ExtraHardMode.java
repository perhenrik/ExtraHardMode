/*
    ExtraHardMode Server Plugin for Minecraft
    Copyright (C) 2012 Ryan Hamshire

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.ryanhamshire.ExtraHardMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import me.ryanhamshire.ExtraHardMode.event.BlockEventHandler;
import me.ryanhamshire.ExtraHardMode.event.EntityEventHandler;
import me.ryanhamshire.ExtraHardMode.event.PlayerEventHandler;
import me.ryanhamshire.ExtraHardMode.module.EntityModule;
import me.ryanhamshire.ExtraHardMode.module.PhysicsModule;
import me.ryanhamshire.ExtraHardMode.service.IModule;
import me.ryanhamshire.ExtraHardMode.task.MoreMonstersTask;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class.
 */
public class ExtraHardMode extends JavaPlugin {

   /**
    * Plugin tag.
    */
   public static final String TAG = "[EHM]";

   /**
    * This handles the config files (messages and plugin options)
    */
   public DataStore dataStore;

   /**
    * Registered modules.
    */
   private final Map<Class<? extends IModule>, IModule> modules = new HashMap<>();

   /**
    * for computing random chance
    */
   private final Random randomNumberGenerator = new Random();

   /**
    * configuration variables, loaded/saved from a config.yml
    */

   /**
    * list of worlds where extra hard mode rules apply
    */
   public ArrayList<World> config_enabled_worlds;

   /**
    * general monster rules
    */

   /**
    * max y value for extra monster spawns
    */
   public int config_moreMonstersMaxY;
   /**
    * what to multiply monster spawns by
    */
   public int config_moreMonstersMultiplier;
   /**
    * max y value for monsters to spawn in the light
    */
   public int config_monsterSpawnsInLightMaxY;

   /**
    * monster grinder fix rules
    */

   /**
    * whether monster grinders (or "farms") should be inhibited
    */
   public boolean config_inhibitMonsterGrinders;

   /**
    * world modification rules
    */

   /**
    * minimum y for placing standard torches
    */
   public int config_standardTorchMinY;
   /**
    * whether stone is hardened to encourage cave exploration over tunneling
    */
   public boolean config_superHardStone;
   /**
    * whether players may place blocks directly underneath themselves
    */
   public boolean config_limitedBlockPlacement;
   /**
    * whether TNT should be more powerful and plentiful
    */
   public boolean config_betterTNT;
   /**
    * which materials beyond sand and gravel should be subject to gravity
    */
   public ArrayList<Material> config_moreFallingBlocks;
   /**
    * whether players are limited to placing torches against specific materials
    */
   public boolean config_limitedTorchPlacement;
   /**
    * whether rain should break torches
    */
   public boolean config_rainBreaksTorches;
   /**
    * percent chance for broken netherrack to start a fire
    */
   public int config_brokenNetherrackCatchesFirePercent;

   /**
    * zombie rules
    */

   /**
    * whether zombies apply a debuff to players on hit
    */
   public boolean config_zombiesDebilitatePlayers;
   /**
    * percent chance for a zombie to reanimate after death
    */
   public int config_zombiesReanimatePercent;

   /**
    * skeleton rules
    */

   /**
    * percent chance skeletons have a chance to knock back targets with arrows
    */
   public int config_skeletonsKnockBackPercent;
   /**
    * percent chance skeletons will release silverfish instead of firing arrows
    */
   public int config_skeletonsReleaseSilverfishPercent;
   /**
    * whether or not arrows will pass harmlessly through skeletons
    */
   public int config_skeletonsDeflectArrowsPercent;

   /**
    * creeper rules
    */

   /**
    * whether creepers explode when caught on fire
    */
   public boolean config_flamingCreepersExplode;
   /**
    * percentage of creepers which will spawn charged
    */
   public int config_chargedCreeperSpawnPercent;
   /**
    * whether charged creepers explode when damaged
    */
   public boolean config_chargedCreepersExplodeOnHit;
   /**
    * percentage of creepers which spawn activated TNT on death
    */
   public int config_creepersDropTNTOnDeathPercent;

   /**
    * pig zombie rules
    */

   /**
    * whether pig zombies are always hostile
    */
   public boolean config_alwaysAngryPigZombies;
   /**
    * whether pig zombies drop nether wart in nether fortresses
    */
   public boolean config_fortressPigsDropWart;

   /**
    * ghast rules
    */

   /**
    * whether ghasts should deflect arrows and drop extra loot
    */
   public boolean config_ghastsDeflectArrows;

   /**
    * magma cube rules
    */

   /**
    * whether damaging a magma cube turns it into a blaze
    */
   public boolean config_magmaCubesBecomeBlazesOnDamage;
   /**
    * percentage chance that a blaze spawn will trigger a flame slime spawn as
    * well
    */
   public int config_flameSlimesSpawnWithNetherBlazePercent;

   /**
    * blaze rules
    */

   /**
    * percentage of pig zombies which will be replaced with blazes
    */
   public int config_bonusNetherBlazeSpawnPercent;
   /**
    * whether blazes explode and spread fire when they die
    */
   public boolean config_blazesExplodeOnDeath;
   /**
    * percentage of skeletons near bedrock which will be replaced with blazes
    */
   public int config_nearBedrockBlazeSpawnPercent;
   /**
    * percentage chance that a blaze slain in the nether will split into two
    * blazes
    */
   public int config_netherBlazesSplitOnDeathPercent;
   /**
    * whether blazes drop fire when damaged
    */
   public boolean config_blazesDropFireOnDamage;
   /**
    * whether blazes drop extra loot
    */
   public boolean config_blazesDropBonusLoot;

   /**
    * spider rules
    */

   /**
    * percentage of zombies which will be replaced with spiders under sea level
    */
   public int config_bonusUndergroundSpiderSpawnPercent;
   /**
    * whether spiders drop webbing when they die
    */
   public boolean config_spidersDropWebOnDeath;

   /**
    * enderman rules
    */

   /**
    * whether endermen may teleport players
    */
   public boolean config_improvedEndermanTeleportation;

   /**
    * witch rules
    */

   /**
    * percentage of surface zombies which spawn as witches
    */
   public int config_bonusWitchSpawnPercent;

   /**
    * ender dragon rules
    */

   /**
    * whether the ender dragon respawns
    */
   public boolean config_respawnEnderDragon;
   /**
    * whether it drops an egg when slain
    */
   public boolean config_enderDragonDropsEgg;
   /**
    * whether it drops a pair of villager eggs when slain
    */
   public boolean config_enderDragonDropsVillagerEggs;
   /**
    * whether the dragon spits fireballs and summons minions
    */
   public boolean config_enderDragonAdditionalAttacks;
   /**
    * whether server wide messages will broadcast player victories and defeats
    */
   public boolean config_enderDragonCombatAnnouncements;
   /**
    * whether players will be allowed to build in the end
    */
   public boolean config_enderDragonNoBuilding;

   /**
    * melons and wheat
    */

   /**
    * whether food crops die more easily
    */
   public boolean config_weakFoodCrops;

   /**
    * mushrooms
    */

   /**
    * whether bonemeal may be used on mushrooms
    */
   public boolean config_noBonemealOnMushrooms;

   /**
    * nether wart
    */

   /**
    * whether nether wart will ever drop more than 1 wart when broken
    */
   public boolean config_noFarmingNetherWart;

   /**
    * sheep (wool)
    */

   /**
    * whether sheep will always regrow white wool
    */
   public boolean config_sheepRegrowWhiteWool;

   /**
    * water
    */

   /**
    * whether players may move water source blocks
    */
   public boolean config_dontMoveWaterSourceBlocks;
   /**
    * whether players may swim while wearing armor
    */
   public boolean config_noSwimmingInArmor;

   /**
    * player death
    */

   /**
    * how much health after respawn
    */
   public int config_playerRespawnHealth;
   /**
    * how much food bar after respawn
    */
   public int config_playerRespawnFoodLevel;
   /**
    * percentage of item stacks lost on death
    */
   public int config_playerDeathItemStacksForfeitPercent;

   /**
    * player damage
    */

   /**
    * whether players take additional damage and/or debuffs from environmental
    * injuries
    */
   public boolean config_enhancedEnvironmentalDamage;
   /**
    * whether players catch fire when extinguishing a fire up close
    */
   public boolean config_extinguishingFireIgnitesPlayers;

   /**
    * explosions disable option, needed to dodge bugs in popular plugins
    */
   public boolean config_workAroundExplosionsBugs;

   /**
    * tree felling
    */

   /**
    * whether tree logs respect gravity
    */
   public boolean config_betterTreeChopping;

   /**
    * initializes well... everything
    */
   @Override
   public void onEnable() {
      // Register modules
      registerModule(EntityModule.class, new EntityModule(this));
      registerModule(PhysicsModule.class, new PhysicsModule(this));

      this.dataStore = new DataStore(this);

      // load the config if it exists
      FileConfiguration config = YamlConfiguration.loadConfiguration(new File(DataStore.configFilePath));

      // read configuration settings (note defaults), write back to config file

      // enabled worlds defaults
      List<String> defaultEnabledWorldNames = new ArrayList<>();
      List<World> worlds = this.getServer().getWorlds();
      for(int i = 0; i < worlds.size(); i++) {
         defaultEnabledWorldNames.add(worlds.get(i).getName());
      }

      // get enabled world names from the config file
      List<String> enabledWorldNames = config.getStringList("ExtraHardMode.Worlds");
      if(enabledWorldNames == null || enabledWorldNames.size() == 0) {
         enabledWorldNames = defaultEnabledWorldNames;
      }

      // validate enabled world names
      this.config_enabled_worlds = new ArrayList<World>();
      for(int i = 0; i < enabledWorldNames.size(); i++) {
         String worldName = enabledWorldNames.get(i);
         World world = this.getServer().getWorld(worldName);
         if(world == null) {
            this.getLogger().info("Error: There's no world named \"" + worldName + "\".  Please update your config.yml.");
         } else {
            this.config_enabled_worlds.add(world);
         }
      }

      // write enabled world names to config file
      config.set("ExtraHardMode.Worlds", enabledWorldNames);

      this.config_standardTorchMinY = config.getInt("ExtraHardMode.PermanentFlameMinYCoord", 30);
      config.set("ExtraHardMode.PermanentFlameMinYCoord", this.config_standardTorchMinY);

      this.config_superHardStone = config.getBoolean("ExtraHardMode.HardenedStone", true);
      config.set("ExtraHardMode.HardenedStone", this.config_superHardStone);

      this.config_enhancedEnvironmentalDamage = config.getBoolean("ExtraHardMode.EnhancedEnvironmentalInjuries", true);
      config.set("ExtraHardMode.EnhancedEnvironmentalInjuries", this.config_enhancedEnvironmentalDamage);

      this.config_extinguishingFireIgnitesPlayers = config.getBoolean("ExtraHardMode.ExtinguishingFiresIgnitesPlayers", true);
      config.set("ExtraHardMode.ExtinguishingFiresIgnitesPlayers", this.config_extinguishingFireIgnitesPlayers);

      this.config_betterTNT = config.getBoolean("ExtraHardMode.BetterTNT", true);
      config.set("ExtraHardMode.BetterTNT", this.config_betterTNT);

      this.config_inhibitMonsterGrinders = config.getBoolean("ExtraHardMode.InhibitMonsterGrinders", true);
      config.set("ExtraHardMode.InhibitMonsterGrinders", this.config_inhibitMonsterGrinders);

      this.config_limitedBlockPlacement = config.getBoolean("ExtraHardMode.LimitedBlockPlacement", true);
      config.set("ExtraHardMode.LimitedBlockPlacement", this.config_limitedBlockPlacement);

      this.config_limitedTorchPlacement = config.getBoolean("ExtraHardMode.LimitedTorchPlacement", true);
      config.set("ExtraHardMode.LimitedTorchPlacement", this.config_limitedTorchPlacement);

      this.config_rainBreaksTorches = config.getBoolean("ExtraHardMode.RainBreaksTorches", true);
      config.set("ExtraHardMode.RainBreaksTorches", this.config_rainBreaksTorches);

      this.config_brokenNetherrackCatchesFirePercent = config.getInt("ExtraHardMode.NetherrackCatchesFirePercent", 20);
      config.set("ExtraHardMode.NetherrackCatchesFirePercent", this.config_brokenNetherrackCatchesFirePercent);

      this.config_moreMonstersMaxY = config.getInt("ExtraHardMode.MoreMonsters.MaxYCoord", 55);
      config.set("ExtraHardMode.MoreMonsters.MaxYCoord", this.config_moreMonstersMaxY);

      this.config_moreMonstersMultiplier = config.getInt("ExtraHardMode.MoreMonsters.Multiplier", 2);
      config.set("ExtraHardMode.MoreMonsters.Multiplier", this.config_moreMonstersMultiplier);

      this.config_monsterSpawnsInLightMaxY = config.getInt("ExtraHardMode.MonstersSpawnInLightMaxY", 50);
      config.set("ExtraHardMode.MonstersSpawnInLightMaxY", this.config_monsterSpawnsInLightMaxY);

      this.config_zombiesDebilitatePlayers = config.getBoolean("ExtraHardMode.Zombies.SlowPlayers", true);
      config.set("ExtraHardMode.Zombies.SlowPlayers", this.config_zombiesDebilitatePlayers);

      this.config_zombiesReanimatePercent = config.getInt("ExtraHardMode.Zombies.ReanimatePercent", 50);
      config.set("ExtraHardMode.Zombies.ReanimatePercent", this.config_zombiesReanimatePercent);

      this.config_skeletonsKnockBackPercent = config.getInt("ExtraHardMode.Skeletons.ArrowsKnockBackPercent", 30);
      config.set("ExtraHardMode.Skeletons.ArrowsKnockBackPercent", this.config_skeletonsKnockBackPercent);

      this.config_skeletonsReleaseSilverfishPercent = config.getInt("ExtraHardMode.Skeletons.ReleaseSilverfishPercent", 30);
      config.set("ExtraHardMode.Skeletons.ReleaseSilverfishPercent", this.config_skeletonsReleaseSilverfishPercent);

      this.config_skeletonsDeflectArrowsPercent = config.getInt("ExtraHardMode.Skeletons.DeflectArrowsPercent", 100);
      config.set("ExtraHardMode.Skeletons.DeflectArrowsPercent", this.config_skeletonsDeflectArrowsPercent);

      this.config_bonusUndergroundSpiderSpawnPercent = config.getInt("ExtraHardMode.Spiders.BonusUndergroundSpawnPercent", 20);
      config.set("ExtraHardMode.Spiders.BonusUndergroundSpawnPercent", this.config_bonusUndergroundSpiderSpawnPercent);

      this.config_spidersDropWebOnDeath = config.getBoolean("ExtraHardMode.Spiders.DropWebOnDeath", true);
      config.set("ExtraHardMode.Spiders.DropWebOnDeath", this.config_spidersDropWebOnDeath);

      this.config_bonusWitchSpawnPercent = config.getInt("ExtraHardMode.Witches.BonusSpawnPercent", 5);
      config.set("ExtraHardMode.Witches.BonusSpawnPercent", this.config_bonusWitchSpawnPercent);

      this.config_chargedCreeperSpawnPercent = config.getInt("ExtraHardMode.Creepers.ChargedCreeperSpawnPercent", 20);
      config.set("ExtraHardMode.Creepers.ChargedCreeperSpawnPercent", this.config_chargedCreeperSpawnPercent);

      this.config_creepersDropTNTOnDeathPercent = config.getInt("ExtraHardMode.Creepers.DropTNTOnDeathPercent", 20);
      config.set("ExtraHardMode.Creepers.DropTNTOnDeathPercent", this.config_creepersDropTNTOnDeathPercent);

      this.config_chargedCreepersExplodeOnHit = config.getBoolean("ExtraHardMode.Creepers.ChargedCreepersExplodeOnDamage", true);
      config.set("ExtraHardMode.Creepers.ChargedCreepersExplodeOnDamage", this.config_chargedCreepersExplodeOnHit);

      this.config_flamingCreepersExplode = config.getBoolean("ExtraHardMode.Creepers.FireTriggersExplosion", true);
      config.set("ExtraHardMode.Creepers.FireTriggersExplosion", this.config_flamingCreepersExplode);

      this.config_nearBedrockBlazeSpawnPercent = config.getInt("ExtraHardMode.Blazes.NearBedrockSpawnPercent", 50);
      config.set("ExtraHardMode.Blazes.NearBedrockSpawnPercent", this.config_nearBedrockBlazeSpawnPercent);

      this.config_bonusNetherBlazeSpawnPercent = config.getInt("ExtraHardMode.Blazes.BonusNetherSpawnPercent", 20);
      config.set("ExtraHardMode.Blazes.BonusNetherSpawnPercent", this.config_bonusNetherBlazeSpawnPercent);

      this.config_flameSlimesSpawnWithNetherBlazePercent = config.getInt("ExtraHardMode.MagmaCubes.SpawnWithNetherBlazePercent", 100);
      config.set("ExtraHardMode.MagmaCubes.SpawnWithNetherBlazePercent", this.config_flameSlimesSpawnWithNetherBlazePercent);

      this.config_magmaCubesBecomeBlazesOnDamage = config.getBoolean("ExtraHardMode.MagmaCubes.GrowIntoBlazesOnDamage", true);
      config.set("ExtraHardMode.MagmaCubes.GrowIntoBlazesOnDamage", this.config_magmaCubesBecomeBlazesOnDamage);

      this.config_blazesExplodeOnDeath = config.getBoolean("ExtraHardMode.Blazes.ExplodeOnDeath", true);
      config.set("ExtraHardMode.Blazes.ExplodeOnDeath", this.config_blazesExplodeOnDeath);

      this.config_blazesDropFireOnDamage = config.getBoolean("ExtraHardMode.Blazes.DropFireOnDamage", true);
      config.set("ExtraHardMode.Blazes.DropFireOnDamage", this.config_blazesDropFireOnDamage);

      this.config_blazesDropBonusLoot = config.getBoolean("ExtraHardMode.Blazes.BonusLoot", true);
      config.set("ExtraHardMode.Blazes.BonusLoot", this.config_blazesDropBonusLoot);

      this.config_netherBlazesSplitOnDeathPercent = config.getInt("ExtraHardMode.Blazes.NetherSplitOnDeathPercent", 25);
      config.set("ExtraHardMode.Blazes.NetherSplitOnDeathPercent", this.config_netherBlazesSplitOnDeathPercent);

      this.config_alwaysAngryPigZombies = config.getBoolean("ExtraHardMode.PigZombies.AlwaysAngry", true);
      config.set("ExtraHardMode.PigZombies.AlwaysAngry", this.config_alwaysAngryPigZombies);

      this.config_fortressPigsDropWart = config.getBoolean("ExtraHardMode.PigZombies.DropWartInFortresses", true);
      config.set("ExtraHardMode.PigZombies.DropWartInFortresses", this.config_fortressPigsDropWart);

      this.config_ghastsDeflectArrows = config.getBoolean("ExtraHardMode.Ghasts.DeflectArrows", true);
      config.set("ExtraHardMode.Ghasts.DeflectArrows", this.config_ghastsDeflectArrows);

      this.config_improvedEndermanTeleportation = config.getBoolean("ExtraHardMode.Endermen.MayTeleportPlayers", true);
      config.set("ExtraHardMode.Endermen.MayTeleportPlayers", this.config_improvedEndermanTeleportation);

      this.config_respawnEnderDragon = config.getBoolean("ExtraHardMode.EnderDragon.Respawns", true);
      config.set("ExtraHardMode.EnderDragon.Respawns", this.config_respawnEnderDragon);

      this.config_enderDragonDropsEgg = config.getBoolean("ExtraHardMode.EnderDragon.DropsEgg", true);
      config.set("ExtraHardMode.EnderDragon.DropsEgg", this.config_enderDragonDropsEgg);

      this.config_enderDragonDropsVillagerEggs = config.getBoolean("ExtraHardMode.EnderDragon.DropsVillagerEggs", true);
      config.set("ExtraHardMode.EnderDragon.DropsVillagerEggs", this.config_enderDragonDropsVillagerEggs);

      this.config_enderDragonAdditionalAttacks = config.getBoolean("ExtraHardMode.EnderDragon.HarderBattle", true);
      config.set("ExtraHardMode.EnderDragon.HarderBattle", this.config_enderDragonAdditionalAttacks);

      this.config_enderDragonCombatAnnouncements = config.getBoolean("ExtraHardMode.EnderDragon.BattleAnnouncements", true);
      config.set("ExtraHardMode.EnderDragon.BattleAnnouncements", this.config_enderDragonCombatAnnouncements);

      this.config_enderDragonNoBuilding = config.getBoolean("ExtraHardMode.EnderDragon.NoBuildingAllowed", true);
      config.set("ExtraHardMode.EnderDragon.NoBuildingAllowed", this.config_enderDragonNoBuilding);

      this.config_weakFoodCrops = config.getBoolean("ExtraHardMode.Farming.WeakCrops", true);
      config.set("ExtraHardMode.Farming.WeakCrops", this.config_weakFoodCrops);

      this.config_noBonemealOnMushrooms = config.getBoolean("ExtraHardMode.Farming.NoBonemealOnMushrooms", true);
      config.set("ExtraHardMode.Farming.NoBonemealOnMushrooms", this.config_noBonemealOnMushrooms);

      this.config_noFarmingNetherWart = config.getBoolean("ExtraHardMode.Farming.NoFarmingNetherWart", true);
      config.set("ExtraHardMode.Farming.NoFarmingNetherWart", this.config_noFarmingNetherWart);

      this.config_sheepRegrowWhiteWool = config.getBoolean("ExtraHardMode.Farming.SheepGrowOnlyWhiteWool", true);
      config.set("ExtraHardMode.Farming.SheepGrowOnlyWhiteWool", this.config_sheepRegrowWhiteWool);

      this.config_dontMoveWaterSourceBlocks = config.getBoolean("ExtraHardMode.Farming.BucketsDontMoveWaterSources", true);
      config.set("ExtraHardMode.Farming.BucketsDontMoveWaterSources", this.config_dontMoveWaterSourceBlocks);

      this.config_noSwimmingInArmor = config.getBoolean("ExtraHardMode.NoSwimmingWhenHeavy", true);
      config.set("ExtraHardMode.NoSwimmingWhenHeavy", this.config_noSwimmingInArmor);

      this.config_playerDeathItemStacksForfeitPercent = config.getInt("ExtraHardMode.PlayerDeath.ItemStacksForfeitPercent", 10);
      config.set("ExtraHardMode.PlayerDeath.ItemStacksForfeitPercent", this.config_playerDeathItemStacksForfeitPercent);

      this.config_playerRespawnHealth = config.getInt("ExtraHardMode.PlayerDeath.RespawnHealth", 15);
      config.set("ExtraHardMode.PlayerDeath.RespawnHealth", this.config_playerRespawnHealth);

      this.config_playerRespawnFoodLevel = config.getInt("ExtraHardMode.PlayerDeath.RespawnFoodLevel", 15);
      config.set("ExtraHardMode.PlayerDeath.RespawnFoodLevel", this.config_playerRespawnFoodLevel);

      this.config_betterTreeChopping = config.getBoolean("ExtraHardMode.BetterTreeFelling", true);
      config.set("ExtraHardMode.BetterTreeFelling", this.config_betterTreeChopping);

      this.config_workAroundExplosionsBugs = config.getBoolean("ExtraHardMode.WorkAroundOtherPluginsExplosionBugs", false);
      config.set("ExtraHardMode.WorkAroundOtherPluginsExplosionBugs", this.config_workAroundExplosionsBugs);

      // default additional falling blocks
      this.config_moreFallingBlocks = new ArrayList<Material>();
      this.config_moreFallingBlocks.add(Material.DIRT);
      this.config_moreFallingBlocks.add(Material.GRASS);
      this.config_moreFallingBlocks.add(Material.COBBLESTONE);
      this.config_moreFallingBlocks.add(Material.MOSSY_COBBLESTONE);
      this.config_moreFallingBlocks.add(Material.MYCEL);
      this.config_moreFallingBlocks.add(Material.JACK_O_LANTERN);

      // build a default config entry for those blocks
      ArrayList<String> defaultMoreFallingBlocksList = new ArrayList<String>();
      for(int i = 0; i < this.config_moreFallingBlocks.size(); i++) {
         defaultMoreFallingBlocksList.add(this.config_moreFallingBlocks.get(i).name());
      }

      // try to load the list from the config file
      List<String> moreFallingBlocksList = config.getStringList("ExtraHardMode.AdditionalFallingBlocks");

      // if it fails, use the above default list instead
      if(moreFallingBlocksList == null || moreFallingBlocksList.size() == 0) {
         getLogger()
               .warning(
                     "Warning: The additional falling blocks list may not be empty.  If you don't want any additional falling blocks, list only a material which is never a block, like DIAMOND_SWORD.");
         moreFallingBlocksList = defaultMoreFallingBlocksList;
      }

      // parse this final list of additional falling blocks
      this.config_moreFallingBlocks = new ArrayList<Material>();
      for(int i = 0; i < moreFallingBlocksList.size(); i++) {
         String blockName = moreFallingBlocksList.get(i);
         Material material = Material.getMaterial(blockName);
         if(material == null) {
            getLogger().warning("Additional Falling Blocks Configuration: Material not found: " + blockName + ".");
         } else {
            this.config_moreFallingBlocks.add(material);
         }
      }

      // write it back to the config
      config.set("ExtraHardMode.AdditionalFallingBlocks", moreFallingBlocksList);

      // save config values to file system
      try {
         config.save(DataStore.configFilePath);
      } catch(IOException exception) {
         getLogger().severe("Unable to write to the configuration file at \"" + DataStore.configFilePath + "\"");
      }

      // register for events
      PluginManager pluginManager = this.getServer().getPluginManager();

      // player events
      PlayerEventHandler playerEventHandler = new PlayerEventHandler(this);
      pluginManager.registerEvents(playerEventHandler, this);

      // block events
      BlockEventHandler blockEventHandler = new BlockEventHandler(this);
      pluginManager.registerEvents(blockEventHandler, this);

      // entity events
      EntityEventHandler entityEventHandler = new EntityEventHandler(this);
      pluginManager.registerEvents(entityEventHandler, this);

      // FEATURE: monsters spawn in the light under a configurable Y level
      MoreMonstersTask task = new MoreMonstersTask(this);
      // Every 60 seconds
      this.getServer().getScheduler().scheduleSyncRepeatingTask(this, task, 1200L, 1200L);
   }

   /**
    * sends a color-coded message to a player
    * 
    * @param player
    * @param color
    * @param messageID
    * @param args
    */
   public void sendMessage(Player player, ChatColor color, CustomizableMessage.Type messageID, String... args) {
      String message = this.dataStore.getMessage(messageID, args);
      sendMessage(player, color, message);
   }

   /**
    * sends a color-coded message to a player
    * 
    * @param player
    * @param color
    * @param message
    */
   public void sendMessage(Player player, ChatColor color, String message) {
      if(player == null) {
         getLogger().info(color + message);
      } else {
         // FEATURE: don't spam messages
         PlayerData playerData = this.dataStore.getPlayerData(player.getName());
         long now = Calendar.getInstance().getTimeInMillis();
         if(!message.equals(playerData.lastMessageSent) || now - playerData.lastMessageTimestamp > 30000) {
            player.sendMessage(color + message);
            playerData.lastMessageSent = message;
            playerData.lastMessageTimestamp = now;
         }
      }
   }

   /**
    * Computes random chance
    * 
    * @param percentChance
    *           - Percentage of success.
    * @return True if it was successful, else false.
    */
   public boolean random(int percentChance) {
      return randomNumberGenerator.nextInt(101) < percentChance;
   }

   /**
    * Get random generator.
    * 
    * @return
    */
   public Random getRandom() {
      return randomNumberGenerator;
   }

   public boolean plantDies(Block block, byte newDataValue) {
      World world = block.getWorld();
      if(!this.config_enabled_worlds.contains(world) || !this.config_weakFoodCrops)
         return false;

      // not evaluated until the plant is nearly full grown
      if(newDataValue <= (byte) 6)
         return false;

      Material material = block.getType();
      if(material == Material.CROPS || material == Material.MELON_STEM || material == Material.CARROT || material == Material.PUMPKIN_STEM
            || material == Material.POTATO) {
         int deathProbability = 25;

         // plants in the dark always die
         if(block.getLightFromSky() < 10) {
            deathProbability = 100;
         }

         else {

            Biome biome = block.getBiome();

            // the desert environment is very rough on crops
            if(biome == Biome.DESERT || biome == Biome.DESERT_HILLS) {
               deathProbability += 50;
            }

            // unwatered crops are more likely to die
            Block belowBlock = block.getRelative(BlockFace.DOWN);
            byte moistureLevel = 0;
            if(belowBlock.getType() == Material.SOIL) {
               moistureLevel = belowBlock.getData();
            }

            if(moistureLevel == 0) {
               deathProbability += 25;
            }
         }

         if(random(deathProbability)) {
            return true;
         }
      }

      return false;
   }

   /**
    * Register a module.
    * 
    * @param clazz
    *           - Class of the instance.
    * @param module
    *           - Module instance.
    * @throws IllegalArgumentException
    *            - Thrown if an argument is null.
    */
   public <T extends IModule> void registerModule(Class<T> clazz, T module) {
      // Check arguments.
      if(clazz == null) {
         throw new IllegalArgumentException("Class cannot be null");
      } else if(module == null) {
         throw new IllegalArgumentException("Module cannot be null");
      }
      // Add module.
      modules.put(clazz, module);
      // Tell module to start.
      module.starting();
   }

   /**
    * Deregister a module.
    * 
    * @param clazz
    *           - Class of the instance.
    * @return Module that was removed. Returns null if no instance of the module
    *         is registered.
    */
   public <T extends IModule> T deregisterModuleForClass(Class<T> clazz) {
      // Check arguments.
      if(clazz == null) {
         throw new IllegalArgumentException("Class cannot be null");
      }
      // Grab module and tell it its closing.
      T module = clazz.cast(modules.get(clazz));
      if(module != null) {
         module.closing();
      }
      return module;
   }

   /**
    * Retrieve a registered module.
    * 
    * @param clazz
    *           - Class identifier.
    * @return Module instance. Returns null is an instance of the given class
    *         has not been registered with the API.
    */
   public <T extends IModule> T getModuleForClass(Class<T> clazz) {
      return clazz.cast(modules.get(clazz));
   }
}
