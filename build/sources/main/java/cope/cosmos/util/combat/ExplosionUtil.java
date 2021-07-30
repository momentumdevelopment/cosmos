package cope.cosmos.util.combat;

import cope.cosmos.util.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.Explosion;

import java.util.ArrayList;
import java.util.List;

public class ExplosionUtil implements Wrapper {

    public static float getDamageFromExplosion(double explosionX, double explosionY, double explosionZ, Entity target, boolean ignoreTerrain, boolean prediction) {
        try {
            return (float) (getExplosionDamage(target, new Vec3d(explosionX, explosionY, explosionZ), 6, ignoreTerrain, prediction) / 2.333);
        } catch (Exception ignored) {

        }

        return 0;
    }

    public static float getExplosionDamage(Entity targetEntity, Vec3d explosionPosition, float explosionPower, boolean ignoreTerrain, boolean prediction) {
        Vec3d entityPosition = prediction && targetEntity instanceof EntityPlayer ? new Vec3d(targetEntity.posX + targetEntity.motionX, targetEntity.posY + targetEntity.motionY, targetEntity.posZ + targetEntity.motionZ) : new Vec3d(targetEntity.posX, targetEntity.posY, targetEntity.posZ);

        if (targetEntity.isImmuneToExplosions())
            return 0;

        explosionPower *= 2;
        double distanceToSize = entityPosition.distanceTo(explosionPosition) / explosionPower;
        double blockDensity = 0;
        
        AxisAlignedBB entityBox = targetEntity.getEntityBoundingBox().offset(targetEntity.getPositionVector().subtract(entityPosition));
        Vec3d boxDelta = new Vec3d(1 / ((entityBox.maxX - entityBox.minX) * 2 + 1), 1 / ((entityBox.maxY - entityBox.minY) * 2 + 1), 1 / ((entityBox.maxZ - entityBox.minZ) * 2 + 1));

        double xOff = (1 - Math.floor(1 / boxDelta.x) * boxDelta.x) / 2;
        double zOff = (1 - Math.floor(1 / boxDelta.z) * boxDelta.z) / 2;

        if (boxDelta.x >= 0 && boxDelta.y >= 0 && boxDelta.z >= 0) {
            int nonSolid = 0;
            int total = 0;

            for (double x = 0; x <= 1; x += boxDelta.x) {
                for (double y = 0; y <= 1; y += boxDelta.y) {
                    for (double z = 0; z <= 1; z += boxDelta.z) {
                        Vec3d startPos = new Vec3d(xOff + entityBox.minX + (entityBox.maxX - entityBox.minX) * x, entityBox.minY + (entityBox.maxY - entityBox.minY) * y, zOff + entityBox.minZ + (entityBox.maxZ - entityBox.minZ) * z);

                        if (!rayTraceSolidCheck(startPos, explosionPosition, ignoreTerrain))
                            nonSolid++;

                        total++;
                    }
                }
            }

            blockDensity = (double) nonSolid / (double) total;
        }

        double densityAdjust = (1 - distanceToSize) * blockDensity;
        float damage = (float) (int) ((densityAdjust * densityAdjust + densityAdjust) / 2 * 7 * explosionPower + 1);

        if (targetEntity instanceof EntityLivingBase)
            damage = getBlastReduction((EntityLivingBase)targetEntity, getDamageFromDifficulty(damage, mc.world.getDifficulty()), new Explosion(mc.world, null, explosionPosition.x, explosionPosition.y, explosionPosition.z, explosionPower / 2, false, true));

        return damage;
    }

    public static boolean rayTraceSolidCheck(Vec3d start, Vec3d end, boolean ignoreTerrain) {
        if (!Double.isNaN(start.x) && !Double.isNaN(start.y) && !Double.isNaN(start.z)) {
            if (!Double.isNaN(end.x) && !Double.isNaN(end.y) && !Double.isNaN(end.z)) {
                int currX = MathHelper.floor(start.x);
                int currY = MathHelper.floor(start.y);
                int currZ = MathHelper.floor(start.z);

                int endX = MathHelper.floor(end.x);
                int endY = MathHelper.floor(end.y);
                int endZ = MathHelper.floor(end.z);

                BlockPos blockPos = new BlockPos(currX, currY, currZ);
                IBlockState blockState = mc.world.getBlockState(blockPos);
                Block block = blockState.getBlock();

                if ((blockState.getCollisionBoundingBox(mc.world, blockPos) != Block.NULL_AABB) && block.canCollideCheck(blockState, false) && (getBlocks().contains(block) || !ignoreTerrain)) {
                    RayTraceResult collisionInterCheck = blockState.collisionRayTrace(mc.world, blockPos, start, end);
                    
                    if (collisionInterCheck != null) 
                        return true;
                }

                double seDeltaX = end.x - start.x;
                double seDeltaY = end.y - start.y;
                double seDeltaZ = end.z - start.z;

                int steps = 200;
                while (steps-- >= 0) {
                    if (Double.isNaN(start.x) || Double.isNaN(start.y) || Double.isNaN(start.z)) 
                        return false;
                    
                    if (currX == endX && currY == endY && currZ == endZ) 
                        return false;

                    boolean unboundedX = true;
                    boolean unboundedY = true;
                    boolean unboundedZ = true;

                    double stepX = 999;
                    double stepY = 999;
                    double stepZ = 999;
                    double deltaX = 999;
                    double deltaY = 999;
                    double deltaZ = 999;

                    if (endX > currX)
                        stepX = currX + 1;
                    else if (endX < currX)
                        stepX = currX;
                    else
                        unboundedX = false;

                    if (endY > currY)
                        stepY = currY + 1;
                    else if (endY < currY)
                        stepY = currY;
                    else
                        unboundedY = false;

                    if (endZ > currZ)
                        stepZ = currZ + 1;
                    else if (endZ < currZ)
                        stepZ = currZ;
                    else
                        unboundedZ = false;

                    if (unboundedX) 
                        deltaX = (stepX - start.x) / seDeltaX;
                    
                    if (unboundedY)
                        deltaY = (stepY - start.y) / seDeltaY;
                    
                    if (unboundedZ) 
                        deltaZ = (stepZ - start.z) / seDeltaZ;

                    if (deltaX == 0)
                        deltaX = -1e-4;
                    if (deltaY == 0)
                        deltaY = -1e-4;
                    if (deltaZ == 0)
                        deltaZ = -1e-4;

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

                    currX = MathHelper.floor(start.x) - (facing == EnumFacing.EAST ? 1 : 0);
                    currY = MathHelper.floor(start.y) - (facing == EnumFacing.UP ? 1 : 0);
                    currZ = MathHelper.floor(start.z) - (facing == EnumFacing.SOUTH ? 1 : 0);

                    blockPos = new BlockPos(currX, currY, currZ);
                    blockState = mc.world.getBlockState(blockPos);
                    block = blockState.getBlock();

                    if (block.canCollideCheck(blockState, false) && (getBlocks().contains(block) || !ignoreTerrain)) {
                        RayTraceResult collisionInterCheck = blockState.collisionRayTrace(mc.world, blockPos, start, end);
                        
                        if (collisionInterCheck != null) 
                            return true;
                    }
                }
            }
        }

        return false;
    }

    public static float getBlastReduction(EntityLivingBase entity, float damage, Explosion explosion) {
        damage = CombatRules.getDamageAfterAbsorb(damage, entity.getTotalArmorValue(), (float)entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
        float enchantmentModifierDamage = 0;
        
        try {
            enchantmentModifierDamage = (float) EnchantmentHelper.getEnchantmentModifierDamage(entity.getArmorInventoryList(), DamageSource.causeExplosionDamage(explosion));
        } catch (Exception ignored) {
            
        }
        
        enchantmentModifierDamage = MathHelper.clamp(enchantmentModifierDamage, 0, 20);

        damage *= 1 - enchantmentModifierDamage / 25;
        PotionEffect resistanceEffect = entity.getActivePotionEffect(MobEffects.RESISTANCE);

        if (entity.isPotionActive(MobEffects.RESISTANCE) && resistanceEffect != null)
            damage = damage * (25 - (resistanceEffect.getAmplifier() + 1) * 5) / 25;

        damage = Math.max(damage, 0);
        return damage;
    }

    public static List<Block> getBlocks() {
        List<Block> list = new ArrayList<>();
        list.add(Blocks.OBSIDIAN);
        list.add(Blocks.BEDROCK);
        list.add(Blocks.COMMAND_BLOCK);
        list.add(Blocks.BARRIER);
        list.add(Blocks.ENCHANTING_TABLE);
        list.add(Blocks.END_PORTAL_FRAME);
        list.add(Blocks.BEACON);
        list.add(Blocks.ANVIL);
        list.add(Blocks.ENDER_CHEST);
        return list;
    }

    public static float getDamageFromDifficulty(float damage, EnumDifficulty difficulty) {
        switch (difficulty) {
            case PEACEFUL:
                return 0;
            case EASY:
                return damage * 0.5f;
            case NORMAL:
                return damage;
            case HARD:
            default:
                return damage * 1.5f;
        }
    }
}