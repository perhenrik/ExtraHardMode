package com.extrahardmode.task;

import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.config.ExplosionType;
import com.extrahardmode.config.RootConfig;
import com.extrahardmode.config.RootNode;
import com.extrahardmode.module.EntityModule;
import com.extrahardmode.service.HackCreeper;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.ArrayList;

/**
 * Creates Explosions. The type determines the power, if there should be fire and the blockDmg. The size of the explosion
 * is determined by the y-level. There are basically 2 settings for every explosion, below and above the specified y-level.
 *
 * Fires an Explosion Event before every Event with a creeper as Entity
 */
public class CreateExplosionTask implements Runnable
{
    /**
     * Plugin reference to get the server etc.
     */
    private ExtraHardMode plugin;
    /**
     * Location of explosion.
     */
    private Location location;
    /**
     * Type that holds information about size and things like blockDmg and Fire
     */
    private ExplosionType type;
    /**
     * Config
     */
    private RootConfig CFG;
    /**
     * Entity specific stuff like marking our Entity as to be ignored
     */
    EntityModule entityModule;
    /**
     * Instance of a the Entity which caused the Explosion
     */
    private Creeper creeper; private TNTPrimed tnt; private ExplosiveMinecart minecartTnt;

    /**
     * Constructor.
     *
     * @param location - Location to make explosion occur.
     * @param type Type that determines size and possible blockdamage or fire of explosion.
     */
    public CreateExplosionTask(ExtraHardMode plugin, Location location, ExplosionType type)
    {
        this.location = location;
        this.type = type;
        this.plugin = plugin;
        CFG = plugin.getModuleForClass(RootConfig.class);
        entityModule = plugin.getModuleForClass(EntityModule.class);
    }

    /**
     * Constructor.
     *
     * @param location  - Location to make explosion occur.
     * @param type      - Type that determines size and possible blockdamage or fire of explosion.
     * @param entity    - Reference to the Entity that caused this Explosion
     */
    public CreateExplosionTask(ExtraHardMode plugin, Location location, ExplosionType type, Entity entity)
    {
        this(plugin, location, type); //Call to standard constructor to save code
        switch (entity.getType())
        {
            case CREEPER:
            {
                this.creeper = (Creeper) entity;
                break;
            }
            case PRIMED_TNT:
            {
                tnt = (TNTPrimed) entity;
                break;
            }
            case MINECART_TNT:
            {
                minecartTnt = (ExplosiveMinecart) entity;
                break;
            }
            default:
            {
                throw new IllegalArgumentException(entity.getType().getName() + " is not handled ");
            }
        }
    }

    @Override
    public void run()
    {
        createExplosion(location, type);
    }

    /**
     * Creates a Explosion, can be different above/below a certain y-level
     */
    public void createExplosion(Location loc, ExplosionType type)
    {
        int power = type.getPowerA();
        boolean setFire = type.isFireA();
        boolean damageWorld = type.allowBlockDmgA();
        String worldName = loc.getWorld().getName();

        final int border = CFG.getInt(RootNode.EXPLOSIONS_Y, loc.getWorld().getName());

        if (loc.getY() <= border)
        {
            switch (type)
            {
                case CREEPER:
                    power = CFG.getInt(RootNode.EXPLOSIONS_CREEPERS_BELOW_POWER, worldName);
                    setFire = CFG.getBoolean(RootNode.EXPLOSIONS_CREEPERS_BELOW_FIRE, worldName);
                    damageWorld = CFG.getBoolean(RootNode.EXPLOSIONS_CREEPERS_BELOW_WORLD_GRIEF, worldName);
                    break;
                case CREEPER_CHARGED:
                    power = CFG.getInt(RootNode.EXPLOSIONS_CHARGED_CREEPERS_BELOW_POWER, worldName);
                    setFire = CFG.getBoolean(RootNode.EXPLOSIONS_CHARGED_CREEPERS_BELOW_FIRE, worldName);
                    damageWorld = CFG.getBoolean(RootNode.EXPLOSIONS_CHARGED_CREEPERS_BELOW_WORLD_GRIEF, worldName);
                    break;
                case TNT:
                    power = CFG.getInt(RootNode.EXPLOSIONS_TNT_BELOW_POWER, worldName);
                    setFire = CFG.getBoolean(RootNode.EXPLOSIONS_TNT_BELOW_FIRE, worldName);
                    damageWorld = CFG.getBoolean(RootNode.EXPLOSIONS_TNT_BELOW_WORLD_GRIEF, worldName);
                    break;
                case OVERWORLD_BLAZE:
                    power = CFG.getInt(RootNode.EXPLOSIONS_BLAZE_BELOW_POWER, worldName);
                    setFire = CFG.getBoolean(RootNode.EXPLOSIONS_BLAZE_BELOW_FIRE, worldName);
                    damageWorld = CFG.getBoolean(RootNode.EXPLOSIONS_BLAZE_BELOW_WORLD_GRIEF, worldName);
                    break;
                case GHAST_FIREBALL:
                    power = CFG.getInt(RootNode.EXPLOSIONS_GHAST_BELOW_POWER, worldName);
                    setFire = CFG.getBoolean(RootNode.EXPLOSIONS_GHAST_BELOW_FIRE, worldName);
                    damageWorld = CFG.getBoolean(RootNode.EXPLOSIONS_GHAST_BELOW_WORLD_GRIEF, worldName);
                    break;
                default:
                    power = type.getPowerB();
                    setFire = type.isFireB();
                    damageWorld = type.allowBlockDmgB();
            }
        }
        else if (loc.getY() > border)
        {
            switch (type)
            {
                case CREEPER:
                    power = CFG.getInt(RootNode.EXPLOSIONS_CREEPERS_ABOVE_POWER, worldName);
                    setFire = CFG.getBoolean(RootNode.EXPLOSIONS_CREEPERS_ABOVE_FIRE, worldName);
                    damageWorld = CFG.getBoolean(RootNode.EXPLOSIONS_CREEPERS_ABOVE_WORLD_GRIEF, worldName);
                    break;
                case CREEPER_CHARGED:
                    power = CFG.getInt(RootNode.EXPLOSIONS_CHARGED_CREEPERS_ABOVE_POWER, worldName);
                    setFire = CFG.getBoolean(RootNode.EXPLOSIONS_CHARGED_CREEPERS_ABOVE_FIRE, worldName);
                    damageWorld = CFG.getBoolean(RootNode.EXPLOSIONS_CHARGED_CREEPERS_ABOVE_WORLD_GRIEF, worldName);
                    break;
                case TNT:
                    power = CFG.getInt(RootNode.EXPLOSIONS_TNT_ABOVE_POWER, worldName);
                    setFire = CFG.getBoolean(RootNode.EXPLOSIONS_TNT_ABOVE_FIRE, worldName);
                    damageWorld = CFG.getBoolean(RootNode.EXPLOSIONS_TNT_ABOVE_WORLD_GRIEF, worldName);
                    break;
                case OVERWORLD_BLAZE:
                    power = CFG.getInt(RootNode.EXPLOSIONS_BLAZE_ABOVE_POWER, worldName);
                    setFire = CFG.getBoolean(RootNode.EXPLOSIONS_BLAZE_ABOVE_FIRE, worldName);
                    damageWorld = CFG.getBoolean(RootNode.EXPLOSIONS_BLAZE_ABOVE_WORLD_GRIEF, worldName);
                    break;
                case GHAST_FIREBALL:
                    power = CFG.getInt(RootNode.EXPLOSIONS_GHAST_ABOVE_POWER, worldName);
                    setFire = CFG.getBoolean(RootNode.EXPLOSIONS_GHAST_ABOVE_FIRE, worldName);
                    damageWorld = CFG.getBoolean(RootNode.EXPLOSIONS_GHAST_ABOVE_WORLD_GRIEF, worldName);
                    break;
                default:
                    power = type.getPowerA();
                    setFire = type.isFireA();
                    damageWorld = type.allowBlockDmgA();
            }
        }

        if (validateLocationSafe(loc, type))
        {
            loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), power, setFire, damageWorld);
        }
    }

    /**
     * Validate if the given Location is not protected by a protection plugin
     * @param loc The Location
     * @param type ExplosionType determining the size of the Explosion and size of the area to check
     */
    public boolean validateLocationSafe(Location loc, ExplosionType type)
    {
        boolean isSafe = true;
        int boomSize;
        if (loc.getY() < CFG.getInt(RootNode.EXPLOSIONS_Y, loc.getWorld().getName()))
            boomSize = type.getPowerB();
        else
            boomSize = type.getPowerA();
        boomSize *= 2; //raughly the size to check borders of protected areas
        ArrayList<Block> boundaries = getBlockList(loc, boomSize);

        switch (type)
        {
            case CREEPER: case CREEPER_CHARGED:
                if (creeper != null)
                {
                    EntityExplodeEvent suicide = new EntityExplodeEvent(creeper, loc, boundaries, 1);
                    plugin.getServer().getPluginManager().callEvent(suicide);
                    creeper.remove();
                    isSafe = !suicide.isCancelled();
                }
                break;
            default: //mark all Explosions as Creeper Explosions
                Creeper mockCreeper = new HackCreeper(loc);
                entityModule.flagIgnore(mockCreeper);

                EntityExplodeEvent suicide = new EntityExplodeEvent(mockCreeper, loc, boundaries, 1);
                plugin.getServer().getPluginManager().callEvent(suicide);
                mockCreeper.remove();
                isSafe = !suicide.isCancelled();
                break;
        }
        return isSafe;
    }

    /**
     * Check pillars in the 4 outer corners of a virtual cube around the explosion. If the protection plugin uses a cuboid
      selection system then there is no way that an explosion can reach into a protected area without atleast on corner touching it
     */
    private ArrayList<Block> getBlockList (Location loc, int boomSize)
    {
        //Doesn't aim to be accurate, just to prevent explosions on the edges of protected land
        ArrayList<Block> boundaries = new ArrayList<Block>();
        Location cubeLoc = loc.clone();

        for (int i = 0; i < 8; i++) //4 outer walls
        {
            //10(5*2) blocks per cornerPillar, no matter the size of the explosion. (x+n-1)/n == x/n but always rounds up
            for (int j = -boomSize; j < boomSize; j+=(boomSize+4)/5)
            {
                switch (i)
                {
                    case 0:
                        cubeLoc = new Location (loc.getWorld(), loc.getX() + boomSize, loc.getY() + j, loc.getZ() + boomSize);
                        break;
                    case 1:
                        cubeLoc = new Location (loc.getWorld(), loc.getX() - boomSize, loc.getY() + j, loc.getZ() + boomSize);
                        break;
                    case 2:
                        cubeLoc = new Location (loc.getWorld(), loc.getX() + boomSize, loc.getY() + j, loc.getZ() - boomSize);
                        break;
                    case 3:
                        cubeLoc = new Location (loc.getWorld(), loc.getX() - boomSize, loc.getY() + j, loc.getZ() - boomSize);
                        break;
                    case 4: //Locations in the middle inbetween the corners. Needed if explosion bigger than claim
                        cubeLoc = new Location (loc.getWorld(), loc.getX() - boomSize/2, loc.getY() + j, loc.getZ() - boomSize/2);
                        break;
                    case 5:
                        cubeLoc = new Location (loc.getWorld(), loc.getX() - boomSize/2, loc.getY() + j, loc.getZ() - boomSize/2);
                        break;
                    case 6:
                        cubeLoc = new Location (loc.getWorld(), loc.getX() - boomSize/2, loc.getY() + j, loc.getZ() - boomSize/2);
                        break;
                    case 7:
                        cubeLoc = new Location (loc.getWorld(), loc.getX() - boomSize/2, loc.getY() + j, loc.getZ() - boomSize/2);
                        break;
                }
                boundaries.add(cubeLoc.getBlock());
            }
        }
        return boundaries;
    }
}