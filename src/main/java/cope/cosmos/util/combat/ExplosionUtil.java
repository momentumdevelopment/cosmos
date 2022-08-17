package cope.cosmos.util.combat;

import cope.cosmos.util.Wrapper;
import cope.cosmos.util.world.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraft.world.Explosion;

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
        return calculateExplosionDamage(entity, vector, 6, blockDestruction);
    }

    /**
     * Calculates the amount of damage an entity will take
     * @param entity The target player
     * @param vector The position
     * @param explosionSize The size of the explosion
     * @param blockDestruction If to ignore terrain blocks when calculating block density
     * @return the explosion damage to the target player
     */
    public static float calculateExplosionDamage(Entity entity, Vec3d vector, float explosionSize, boolean blockDestruction) {

        // the real explosion size
        double doubledExplosionSize = explosionSize * 2.0;

        // distance from the explosion
        double dist = entity.getDistance(vector.x, vector.y, vector.z) / doubledExplosionSize;
        if (dist > 1) {
            return 0;
        }

        // block density factor
        double v = (1 - dist) * getBlockDensity(blockDestruction, vector, entity.getEntityBoundingBox());

        // damage caused by explosion onto the entity
        float damage = CombatRules.getDamageAfterAbsorb(DamageUtil.getScaledDamage((float) ((v * v + v) / 2.0 * 7.0 * doubledExplosionSize + 1.0)), ((EntityLivingBase) entity).getTotalArmorValue(), (float) ((EntityLivingBase) entity).getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());

        // explosion damage source
        DamageSource damageSource = DamageSource.causeExplosionDamage(new Explosion(entity.world, entity, vector.x, vector.y, vector.z, (float) doubledExplosionSize, false, true));

        // damage modified based on enchantment modifiers
        int n = EnchantmentHelper.getEnchantmentModifierDamage(entity.getArmorInventoryList(), damageSource);
        if (n > 0) {
            damage = CombatRules.getDamageAfterMagicAbsorb(damage, n);
        }

        // damage modified based on potion modifiers
        if (((EntityLivingBase) entity).isPotionActive(MobEffects.RESISTANCE)) {
            PotionEffect potionEffect = ((EntityLivingBase) entity).getActivePotionEffect(MobEffects.RESISTANCE);
            if (potionEffect != null) {
                damage = damage * (25.0F - (potionEffect.getAmplifier() + 1) * 5) / 25.0F;
            }
        }

        // return final damage
        return Math.max(damage, 0);
    }

    /**
     * Gets the block density
     * @param blockDestruction if to ignore terrain blocks
     * @param vector the position of the block
     * @param bb the bounding box
     * @return the density of the block
     */
    public static double getBlockDensity(boolean blockDestruction, Vec3d vector, AxisAlignedBB bb) {
        
        // diffs
        double diffX = 1 / ((bb.maxX - bb.minX) * 2D + 1D);
        double diffY = 1 / ((bb.maxY - bb.minY) * 2D + 1D);
        double diffZ = 1 / ((bb.maxZ - bb.minZ) * 2D + 1D);
        double diffHorizontal = (1 - Math.floor(1D / diffX) * diffX) / 2D;
        double diffTranslational = (1 - Math.floor(1D / diffZ) * diffZ) / 2D;

        // has any density
        if (diffX >= 0 && diffY >= 0 && diffZ >= 0) {
            
            // solid & non-solid block count
            float solid = 0;
            float nonSolid = 0;

            for (double x = 0; x <= 1; x = x + diffX) {
                for (double y = 0; y <= 1; y = y + diffY) {
                    for (double z = 0; z <= 1; z = z + diffZ) {

                        // scaled diffs
                        double scaledDiffX = bb.minX + (bb.maxX - bb.minX) * x;
                        double scaledDiffY = bb.minY + (bb.maxY - bb.minY) * y;
                        double scaledDiffZ = bb.minZ + (bb.maxZ - bb.minZ) * z;

                        // check solid and update
                        if (!isSolid(new Vec3d(scaledDiffX + diffHorizontal, scaledDiffY, scaledDiffZ + diffTranslational), vector, blockDestruction)) {
                            solid++;
                        }

                        // is not solid
                        nonSolid++;
                    }
                }
            }

            return solid / nonSolid;
        } 
        
        else {
            return 0;
        }
    }
    
    // ******************* WHAT??? **********************
    // I really don't understand any of this, sum mc spaghetti code - linus

    /**
     * A wrapper for vanilla raytracing with blockDestruction
     * @param start the starting position
     * @param end the ending position
     * @param blockDestruction if to ignore terrain blocks
     * @return if the raytrace found a solid block
     */
    public static boolean isSolid(Vec3d start, Vec3d end, boolean blockDestruction) {

        // why??? when would it ever be NaN
        if (!Double.isNaN(start.x) && !Double.isNaN(start.y) && !Double.isNaN(start.z)) {
            if (!Double.isNaN(end.x) && !Double.isNaN(end.y) && !Double.isNaN(end.z)) {

                // current
                int currX = MathHelper.floor(start.x);
                int currY = MathHelper.floor(start.y);
                int currZ = MathHelper.floor(start.z);

                // end, should ease to
                int endX = MathHelper.floor(end.x);
                int endY = MathHelper.floor(end.y);
                int endZ = MathHelper.floor(end.z);

                // block states
                BlockPos blockPos = new BlockPos(currX, currY, currZ);
                IBlockState blockState = mc.world.getBlockState(blockPos);
                Block block = blockState.getBlock();

                if ((blockState.getCollisionBoundingBox(mc.world, blockPos) != Block.NULL_AABB) && block.canCollideCheck(blockState, false) && ((BlockUtil.resistantBlocks.contains(block) || BlockUtil.unbreakableBlocks.contains(block)) || !blockDestruction)) {
                    RayTraceResult collisionInterCheck = blockState.collisionRayTrace(mc.world, blockPos, start, end);

                    // what??
                    if (collisionInterCheck != null) {
                        return true;
                    }
                }

                // diffs
                double seDeltaX = end.x - start.x;
                double seDeltaY = end.y - start.y;
                double seDeltaZ = end.z - start.z;

                // normalized steps
                int steps = 200;

                // lol
                while (steps-- >= 0) {

                    // why??? when would it ever be NaN?
                    if (Double.isNaN(start.x) || Double.isNaN(start.y) || Double.isNaN(start.z)) {
                        return false;
                    }

                    // end equals start?? when the fuck would this even happen?
                    if (currX == endX && currY == endY && currZ == endZ) {
                        return false;
                    }

                    // bounds
                    boolean unboundedX = true;
                    boolean unboundedY = true;
                    boolean unboundedZ = true;

                    // steps
                    double stepX = 999;
                    double stepY = 999;
                    double stepZ = 999;
                    double deltaX = 999;
                    double deltaY = 999;
                    double deltaZ = 999;

                    // step X ++
                    if (endX > currX) {
                        stepX = currX + 1;
                    }

                    // step X
                    else if (endX < currX) {
                        stepX = currX;
                    } 
                    
                    // no longer unbounded; needs clamp
                    else {
                        unboundedX = false;
                    }

                    if (endY > currY) {
                        stepY = currY + 1.0;
                    } 
                    
                    else if (endY < currY) {
                        stepY = currY;
                    } 
                    
                    else {
                        unboundedY = false;
                    }

                    if (endZ > currZ) {
                        stepZ = currZ + 1.0;
                    } 
                    
                    else if (endZ < currZ) {
                        stepZ = currZ;
                    }
                    
                    else {
                        unboundedZ = false;
                    }

                    // somehow stayed bounded??? how would it even reach this??
                    if (unboundedX) {
                        deltaX = (stepX - start.x) / seDeltaX;
                    }

                    if (unboundedY) {
                        deltaY = (stepY - start.y) / seDeltaY;
                    }

                    if (unboundedZ) {
                        deltaZ = (stepZ - start.z) / seDeltaZ;
                    }

                    // -1.0E-4 ??? 
                    if (deltaX == 0) {
                        deltaX = -1.0E-4;
                    }

                    if (deltaY == 0) {
                        deltaY = -1.0E-4;
                    }

                    if (deltaZ == 0) {
                        deltaZ = -1.0E-4;
                    }

                    // find the facing based on current and end values
                    EnumFacing facing;

                    if (deltaX < deltaY && deltaX < deltaZ) {
                        facing = endX > currX ? EnumFacing.WEST : EnumFacing.EAST;
                        start = new Vec3d(stepX, start.y + seDeltaY * deltaX, start.z + seDeltaZ * deltaX);
                    } 
                    
                    else if (deltaY < deltaZ) {
                        facing = endY > currY ? EnumFacing.DOWN : EnumFacing.UP;
                        start = new Vec3d(start.x + seDeltaX * deltaY, stepY, start.z + seDeltaZ * deltaY);
                    } 
                    
                    else {
                        facing = endZ > currZ ? EnumFacing.NORTH : EnumFacing.SOUTH;
                        start = new Vec3d(start.x + seDeltaX * deltaZ, start.y + seDeltaY * deltaZ, stepZ);
                    }

                    // new current
                    currX = MathHelper.floor(start.x) - (facing == EnumFacing.EAST ? 1 : 0);
                    currY = MathHelper.floor(start.y) - (facing == EnumFacing.UP ? 1 : 0);
                    currZ = MathHelper.floor(start.z) - (facing == EnumFacing.SOUTH ? 1 : 0);

                    // new block states
                    blockPos = new BlockPos(currX, currY, currZ);
                    blockState = mc.world.getBlockState(blockPos);
                    block = blockState.getBlock();

                    // can collide ?? Should check the non-explosion blocks first
                    if (block.canCollideCheck(blockState, false) && ((BlockUtil.resistantBlocks.contains(block) || BlockUtil.unbreakableBlocks.contains(block)) || !blockDestruction)) {
                        RayTraceResult collisionInterCheck = blockState.collisionRayTrace(mc.world, blockPos, start, end);
                        
                        // what??
                        if (collisionInterCheck != null) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}