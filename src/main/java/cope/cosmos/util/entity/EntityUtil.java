package cope.cosmos.util.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityWolf;

/**
 * @author linustouchtips
 * @since 05/07/2021
 */
public class EntityUtil {

    /**
     * Check whether an entity is a passive mob
     * @param entity The entity to check
     * @return Whether an entity is a passive mob
     */
    public static boolean isPassiveMob(Entity entity) {

        // check if its a wolf that isn't angry
        if (entity instanceof EntityWolf) {
            return !((EntityWolf) entity).isAngry();
        }

        // check if it's an iron golem that isn't angry
        if (entity instanceof EntityIronGolem) {
            return ((EntityIronGolem) entity).getRevengeTarget() == null;
        }

        // check it's entity properties
        return entity instanceof EntityAgeable || entity instanceof EntityAmbientCreature || entity instanceof EntitySquid;
    }

    /**
     * Check whether an entity is a vehicle mob
     * @param entity The entity to check
     * @return Whether an entity is a vehicle mob
     */
    public static boolean isVehicleMob(Entity entity) {
        return entity instanceof EntityBoat || entity instanceof EntityMinecart;
    }

    /**
     * Check whether an entity is a hostile mob
     * @param entity The entity to check
     * @return Whether an entity is a hostile mob
     */
    public static boolean isHostileMob(Entity entity) {
        return (entity.isCreatureType(EnumCreatureType.MONSTER, false) && !EntityUtil.isNeutralMob(entity)) || entity instanceof EntitySpider;
    }

    /**
     * Check whether an entity is a neutral mob
     * @param entity The entity to check
     * @return Whether an entity is a neutral mob
     */
    public static boolean isNeutralMob(Entity entity) {
        return entity instanceof EntityPigZombie && !((EntityPigZombie) entity).isAngry() || entity instanceof EntityWolf && !((EntityWolf) entity).isAngry() || entity instanceof EntityEnderman && ((EntityEnderman) entity).isScreaming();
    }
}
