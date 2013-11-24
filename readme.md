# ExtraHardMode
version: 3.4-SNAPSHOT
  build: 57

> Accept the challenges so that you can feel the exhilaration of victory.
> --George S. Patton


Read more at http://www.brainyquote.com/quotes/quotes/g/georgespa122094.html#jqGFtMtlOUfDQsFx.99

## Useful Links
 [Materials in Bukkit >](http://jd.bukkit.org/rb/apidocs/org/bukkit/Material.html)
 [Minecraft ids >](http://minecraft.gamepedia.com/Data_values)

## Setup

By default the plugin is disabled for all worlds. You need to manually set the worlds where you want to have it enabled.

``` yaml
Enabled Worlds: [world, world_nether]
# or: (this is a comment in yaml and will be ignored)
Enabled Worlds:
- world
- world_nether 
```

It doesn't matter which difficulty you set your server on. The difficulty level in your server.properties file determines how much damage monsters do to players and how much damage a player can take from starvation. On "hard" difficulty, zombies can break down wooden doors.

## Bypassing
By default operators, players in creative and players with the [bypass permission](#permissions) bypass all player related features of extrahardmode. You can change it so that your ops can also play like regular players.

## World Rules
All the features that affect your world or how players perceive your world.

### Mining
This module contains an "anti-branchmining" and basic "physics" module. 

#### Inhibit Tunelling
Inhibit Tunneling basically wears down tools quicker when breaking stone.
``` yaml
Amount of Stone Tool Can Mine (Tool@Blocks):
# Tool can be an item id or a bukkit-tool-name, f.e.
#That means that you can mine 32 blocks of stone before your pickaxe will break
- IRON_PICKAXE@32
#257 is the minecraft id of a iron pickaxe and that means the same as "IRON_PICKAXE@32"
- 257@32
#Note: You can use item ids from mods that add new tools too!
```

#### Breaking Blocks Softens Surrounding Stone: 
If breaking any of the blocks listed stone blocks touching the block that has been broken will turn into cobblestone. The cobblestone will then fall down.
``` yaml
#A list of blocks (ores) that will soften the surrounding stone
Blocks (Block@id,id2):
# You can use minecraft ids and data
# If f.e. you wanted only spruce wood planks to soften surrounding stone and not the other planks
# you would check http://minecraft.gamepedia.com/Data_values#Wood_Planks for the data of spruce wood which is 1
- WOOD@1 # = spruce wood
- ADD_OTHER_BLOCKS
```

### Torches 
This module will restrict the usage of torches, but mainly their placement.

``` yaml
#Block placement of torches below y-level 30. Makes for scarrier caves on the lower levels, increases mob spawns and makes diamonds harder to get. Set to 0 to disable.
No Placement Under Y: 30
#Soft materials include sand and dirt. Idea is that players don't litter the landscape with torches.
No Placement On Soft Materials: true
# When it rains there is a chance that torches will be removed in a chunk. The torches wil drop to the ground. Even a nonsolid block is enough to protect your torches.
Rain Breaks Torches: true
```

### Play Sounds 
Includes a couple sounds that will be played to the player.
``` yaml
Torch Fizzing: true #A lava fizz when a torch's placement has been blocked
Creeper Tnt Warning: true #A Ghast shriek when a creeper drops tnt
```

### Breaking Netherrack Starts Fire Percent 
Sets the player on fire when mining netherrack. This is to make it tuffer to dig tunnels in the nether and avoid all dangers by doing so.

### Limited Block Placement 
Will make building things harder. 
It blocks:
- Straight pillaring up (jumping and placing a block directly beneath you)
- Building bridges in the sky (branching out with no blocks to support)

It's a bit hard to explain, try it out. Maybe I will make a 20 second video.

### Better Tree Felling 
This is a cool feature. It makes your trees alive when you chop them! The trunk and branches of a tree will fall and potentially injure you. It makes it easier to chop trees, but you have to watch out a little for the falling logs. Also by making branchlogs fall down most treetops should decay. 
This one would be best shown in a ~20 second video.

### Player 
All features directly affecting a player.

#### Enhanced Environmental Injuries 
Increases damage taken from falldamage, suffocation and burning. Applies various statuseffects like a few second slowness potion when taking falldamage or a blindness effect when taking fire damage.

#### Extinguishing Fires Ignites Player 
Punching fire with your bare fists to extinguish it will set you on fire for a short amount of time. Fire is dangerous don't play with it!

#### No Swimming When Too Heavy 
Adds a weight system to your inventory. If your inventory exceeds the weigh you will be pulled down and eventually drown.
``` yaml
Enable: true #Enable feature
#Set to false if you want to exempt players from drowning when swimming up 1x1 waterstreams
Block Elevators/Waterfalls: true
#The maximum inventory weight you can have before starting to drown
Max Points: 18.0
#One piece of worn armor would add 2.0 weight. So full set of armor adds 8.0
One Piece Of Worn Armor Adds: 2.0
#A stack of any item adds 1.0, half a stack add 0.5 so it calculates fractions
One Stack Adds: 1.0
#A tool is any item that doesn't stack, swords, axes, not worn armor, shears etc
One Tool Adds: 0.5
#Basically an esoteric percentage of how fast you drown. 35 actually doesnt really make you drown. 50 would make you drown
Drown Rate: 35
#If your inventory weight exceeds the max weight every weightpoint will add 2 to the drownrate. Weight = 25
=> (base) + (exceeding) * (modifier) = 35 + 7 * 2 = 49
Overencumbrance Adds To Drown Rate: 2
```

## General Monster Rules 
Rules affecting all types of monsters.

### Inhibit Monster Grinders 
    
#### More Monsters 
Increases packspawns for natural spawns. Multiplies it with random monsters.
``` yaml
#Maximum y value where packspawns are increased. Set to 0 to disable
Max Y: 55
#Multiplier. Normal packspawns are 1-4 monsters. A setting of 2 would increase it to 2-8. Note that this doesn't increase spawnlimits. If the spawnlimit is reached and no natural spawns occur this won't increase the limit.
Multiplier: 2
```

#### Monsters Spawn In Light 
This works indepent from "More Monsters". It spawns monsters even if there is sufficient light. Currently it will spawn monsters in locations where you have been previously. It's meant to make players not feel completely safe once they have lit up caves.

``` yaml
#Feature enabled below 55 (caves). Set to 1 to disable 
Max Y: 55
```

#### Horses 
WIP: Will include food as requirement for horses.
``` yaml
#Block the usage of chests on horses in caves, to prevent usage of horses as mobile chests.
Block Usage Of Chest Below: 55
```

### Zombies 

#### Slow Players 
Instead of speeding Zombies up, a Zombie will slow a player down for a few seconds when the player is hit by a zombie.

#### Resurrecting 
Zombies may resurrect when slain. They will respawn after a few seconds and might ambush a player.

### Skeletons 
Skeletons may shoot silverfish, shoot knockback arrows and are immune to arrows. (Work in progress)

### Silverfish 
Silverfish may spawn naturally in caves if "More Monsters" is activated and may be summoned by skeletons.
``` yaml
#Block silverfish from entering blocks like stone and cobblestone to prevent them from despawning.
Cant enter blocks: true
#Drop 1 cobble on death
Drop Cobble: true
#Show particles to make silverfish more visible. This is to combat silverfish glitching into blocks and not being visible.
Particles To Make Better Visible: true
```

### Spiders 

#### Bonus Underground Spawns 
Increase spawns of spiders in cave to make them a bit creepier and increase the number of spiderwebs.

#### Drop Webs 
Slaying a spider will drop web blocks in which a player can get stuck. The webs will be removed on the surface after a while, but stay in caves.

### Creepers 
#### Charged Creepers 
Charged creepers will spawn natually and explode on death. It's supposed to be a fearsome monster and fighting it should be avoided.

#### Drop Tnt 
Killing a creeper may drop ignited tnt. The dropped tnt will act the same as player ignited tnt which can be configured at the bottom.

#### Fire Triggers Explosion 
A creeper which dies while being on fire will launch in the air and explode with some fireworks. Small gimmick, doesn't really increase difficulty ;).

### Blazes 
```
#continue here
Near Bedrock Spawn Percent: 50
Bonus Nether Spawn Percent: 20
Drop Fire On Damage: true
Bonus Loot: true
Nether Split On Death Percent: 25
``` 

## Commands

### ehm
help menu
### ehm reload
reloads the config and restarts some tasks
requires admin permission
### ehm version
shows the version number of the plugin


## Permissions

### Admin

#### extrahardmode.admin
Needed for commands like reload

### Bypassing

#### extrahardmode.bypass.*
Bypasses all features which are triggered by a player, defaults to op

#### extrahardmode.bypass.creepers
Bypasses all creeper rules

### Silence

#### extrahardmode.silent.*
Grants ALL silent permission nodes
#### extrahardmode.silent.stone_mining_help
Hides the stone mining help message
#### extrahardmode.silent.no_placing_ore_against_stone
Hides the no placing ore against stone message
#### extrahardmode.silent.realistic_building
Hides the realistic building message
#### extrahardmode.silent.limited_torch_placement
Hides the limited torch placement message
#### extrahardmode.silent.no_torches_here
Hides the no torches here message