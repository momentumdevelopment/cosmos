package cope.cosmos.util.combat;

import cope.cosmos.util.Wrapper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/**
 * @author linustouchtips
 * @since 05/05/2021
 */
public class EnemyUtil implements Wrapper {

    /**
     * Gets the health of an entity
     * @param entity The entity to get the health for
     * @return The health of the entity
     */
    public static float getHealth(Entity entity) {
        // player
        if (entity instanceof EntityPlayer) {
            return ((EntityPlayer) entity).getHealth() + ((EntityPlayer) entity).getAbsorptionAmount();
        }

        // living
        else if (entity instanceof EntityLivingBase) {
            return ((EntityLivingBase) entity).getHealth() + ((EntityLivingBase) entity).getAbsorptionAmount();
        }

        return 0;
    }

    /**
     * Gets the armor durability of all the armor of a player
     * @param target The player to check armor durability for
     * @return The armor durability of all the armor of the player
     */
    public static float getArmor(Entity target) {
        if (target instanceof EntityPlayer) {
            // total durability
            float armorDurability = 0;

            // check durability for each piece of armor
            for (ItemStack armor : target.getArmorInventoryList()) {
                if (armor != null && !armor.getItem().equals(Items.AIR)) {
                    armorDurability += (armor.getMaxDamage() - armor.getItemDamage() / (float) armor.getMaxDamage()) * 100;
                }
            }

            return armorDurability;
        }

        return 0;
    }

    /**
     * Checks if an entity is dead
     * @param entity The entity to check
     * @return Whether the entity is dead
     */
    public static boolean isDead(Entity entity) {
        return getHealth(entity) <= 0 || entity.isDead;
    }
}
