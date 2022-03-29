package cope.cosmos.util.combat;

import cope.cosmos.util.Wrapper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.MobEffects;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

/**
 * @author aesthetical
 * @since 03/19/2022
 */
public class ExplosionUtil implements Wrapper {

    /**
     * Calculates the damage to the target an end crystal explosion will do
     * @param entity The target
     * @param vector The position
     * @param blockDestruction If to ignore terrain blocks when calculating block density
     * @return the explosion damage to an end crystal to the target player
     */
    public static float getDamageFromExplosion(Entity entity, Vec3d vector, boolean blockDestruction) {
        return calculateExplosionDamage(entity, vector, blockDestruction, 12, false, true);
    }

    /**
     * Calculates the amount of damage an entity will take
     * @param entity The target player
     * @param vector The position
     * @param blockDestruction If to ignore terrain blocks when calculating block density
     * @param powerSq The power of the explosion squared
     * @param causesFire If the explosion causes fire
     * @param damagesTerrain If the explosion damages the terrain
     * @return the explosion damage to the target player
     */
    public static float calculateExplosionDamage(Entity entity, Vec3d vector, boolean blockDestruction, double powerSq, boolean causesFire, boolean damagesTerrain) {
//        if (player == null || player.isCreative() || player.isDead) {
//            return 0F;
//        }

        double size = entity.getDistanceSq(vector.x, vector.y, vector.z) / powerSq;
        double density = mc.world.getBlockDensity(vector, entity.getEntityBoundingBox());

        double impact = (1 - size) * density;
        float damage = (float) ((impact * impact + impact) / 2F * 7F * powerSq + 1);

        return getBlastReduction(entity, getScaledDamage(damage),
                new Explosion(entity.world, entity, vector.x, vector.y, vector.z, (float) (powerSq / 2F), causesFire, damagesTerrain));
    }

    /**
     * Calculates the actual damage taking into account armor, combat rules, and potion effects
     * @param entity The target entity
     * @param damage The roughly calculated damage
     * @param explosion The explosion object
     * @return a reduced damage value
     */
    public static float getBlastReduction(Entity entity, float damage, Explosion explosion) {
        DamageSource src = DamageSource.causeExplosionDamage(explosion);

        if (entity instanceof EntityLivingBase) {
            damage = CombatRules.getDamageAfterAbsorb(damage, ((EntityLivingBase) entity).getTotalArmorValue(), (float) ((EntityLivingBase) entity).getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
        }

        int enchantModifier = 0;
        try {
            enchantModifier = EnchantmentHelper.getEnchantmentModifierDamage(entity.getArmorInventoryList(), src);
        } catch (NullPointerException ignored) { }

        float eof = MathHelper.clamp(enchantModifier, 0, 20);
        damage *= 1F - eof / 25F;

        if (entity instanceof EntityLivingBase) {
            if (((EntityLivingBase) entity).isPotionActive(MobEffects.RESISTANCE)) {
                damage -= damage / 4F;
            }
        }

        // can't be negative damage :<
        return Math.max(0, damage);
    }

    /**
     * Calculates the scaled damage based off of the difficulty
     * @param damage The damage to scale
     * @return The scaled damage based off of the difficulty
     */
    public static float getScaledDamage(float damage) {
        if (mc.world != null) {
            int diff = mc.world.getDifficulty().getDifficultyId();
            return damage * (diff * 0.5F);
        }

        return damage;
    }
}