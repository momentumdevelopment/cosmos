package cope.cosmos.util.combat;

import cope.cosmos.util.Wrapper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class EnemyUtil implements Wrapper {

    public static float getHealth(Entity entity) {
        if (entity instanceof EntityPlayer) {
            return ((EntityPlayer) entity).getHealth() + ((EntityPlayer) entity).getAbsorptionAmount();
        }

        else if (entity instanceof EntityLivingBase) {
            return ((EntityLivingBase) entity).getHealth();
        }

        return 0;
    }

    public static float getArmor(Entity target) {
        if (!(target instanceof EntityPlayer))
            return 0;

        float armorDurability = 0;
        for (ItemStack stack : target.getArmorInventoryList()) {
            if (stack == null || stack.getItem() == Items.AIR)
                continue;

            armorDurability += ((float) (stack.getMaxDamage() - stack.getItemDamage()) / (float) stack.getMaxDamage()) * 100.0f;
        }

        return armorDurability;
    }

    public static boolean getArmor(Entity target, double durability) {
        if (!(target instanceof EntityPlayer))
            return false;

        for (ItemStack stack : target.getArmorInventoryList()) {
            if (stack == null || stack.getItem() == Items.AIR)
                return true;

            if (durability >= ((float) (stack.getMaxDamage() - stack.getItemDamage()) / (float) stack.getMaxDamage()) * 100.0f)
                return true;
        }

        return false;
    }

    public static boolean isDead(Entity entity) {
        return getHealth(entity) <= 0 || entity.isDead;
    }
}
