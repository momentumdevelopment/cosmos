package cope.cosmos.util.world;

import cope.cosmos.util.Wrapper;
import cope.cosmos.util.world.BlockUtil.BlockResistance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HoleUtil implements Wrapper {

    public static boolean isInHole(double posX, double posY, double posZ) {
        return isObsidianHole(new BlockPos(posX, Math.round(posY), posZ)) || isBedRockHole(new BlockPos(posX, Math.round(posY), posZ));
    }

    public static boolean isInHole(Entity entity) {
        return isObsidianHole(new BlockPos(entity.posX, Math.round(entity.posY), entity.posZ)) || isBedRockHole(new BlockPos(entity.posX, Math.round(entity.posY), entity.posZ));
    }

    public static boolean isPartOfHole(BlockPos blockPos) {
        List<Entity> entities = new ArrayList<>();
        entities.addAll(mc.world.getEntitiesWithinAABBExcludingEntity(mc.player, new AxisAlignedBB(blockPos.add(1, 0, 0))));
        entities.addAll(mc.world.getEntitiesWithinAABBExcludingEntity(mc.player, new AxisAlignedBB(blockPos.add(-1, 0, 0))));
        entities.addAll(mc.world.getEntitiesWithinAABBExcludingEntity(mc.player, new AxisAlignedBB(blockPos.add(0, 0, 1))));
        entities.addAll(mc.world.getEntitiesWithinAABBExcludingEntity(mc.player, new AxisAlignedBB(blockPos.add(0, 0, -1))));
        return entities.stream().anyMatch(entity -> entity instanceof EntityPlayer);
    }

    public static boolean isAboveHole(double height) {
        Vec3d belowVector = InterpolationUtil.interpolateEntityTime(mc.player, mc.getRenderPartialTicks());
        return isObsidianHole(new BlockPos(belowVector.x, belowVector.y - height, belowVector.z)) || isBedRockHole(new BlockPos(belowVector.x, belowVector.y - height, belowVector.z));
    }

    public static boolean isDoubleBedrockHoleX(BlockPos blockPos) {
        if (!mc.world.getBlockState(blockPos).getBlock().equals(Blocks.AIR) || !mc.world.getBlockState(blockPos.add(1, 0, 0)).getBlock().equals(Blocks.AIR) || !mc.world.getBlockState(blockPos.add(0, 1, 0)).getBlock().equals(Blocks.AIR) && !mc.world.getBlockState(blockPos.add(1, 1, 0)).getBlock().equals(Blocks.AIR) || !mc.world.getBlockState(blockPos.add(0, 2, 0)).getBlock().equals(Blocks.AIR) && !mc.world.getBlockState(blockPos.add(1, 2, 0)).getBlock().equals(Blocks.AIR))
            return false;

        for (BlockPos connectedPos : new BlockPos[] { blockPos.add(2, 0, 0), blockPos.add(1, 0, 1), blockPos.add(1, 0, -1), blockPos.add(-1, 0, 0), blockPos.add(0, 0, 1), blockPos.add(0, 0, -1), blockPos.add(0, -1, 0), blockPos.add(1, -1, 0) }) {
            if (BlockUtil.getBlockResistance(connectedPos) != BlockResistance.BLANK && (BlockUtil.getBlockResistance(connectedPos) == BlockResistance.UNBREAKABLE))
                continue;

            return false;
        }

        return true;
    }

    public static boolean isDoubleBedrockHoleZ(BlockPos blockPos) {
        if (!mc.world.getBlockState(blockPos).getBlock().equals(Blocks.AIR) || !mc.world.getBlockState(blockPos.add(0, 0, 1)).getBlock().equals(Blocks.AIR) || !mc.world.getBlockState(blockPos.add(0, 1, 0)).getBlock().equals(Blocks.AIR) && !mc.world.getBlockState(blockPos.add(0, 1, 1)).getBlock().equals(Blocks.AIR) || !mc.world.getBlockState(blockPos.add(0, 2, 0)).getBlock().equals(Blocks.AIR) && !mc.world.getBlockState(blockPos.add(0, 2, 1)).getBlock().equals(Blocks.AIR))
            return false;

        for (BlockPos connectedPos : new BlockPos[] { blockPos.add(0, 0, 2), blockPos.add(1, 0, 1), blockPos.add(-1, 0, 1), blockPos.add(0, 0, -1), blockPos.add(1, 0, 0), blockPos.add(-1, 0, 0), blockPos.add(0, -1, 0), blockPos.add(0, -1, 1) }) {
            if (BlockUtil.getBlockResistance(connectedPos) != BlockResistance.BLANK && (BlockUtil.getBlockResistance(connectedPos) == BlockResistance.UNBREAKABLE))
                continue;

            return false;
        }

        return true;
    }

    public static boolean isDoubleObsidianHoleX(BlockPos blockPos) {
        if (isDoubleBedrockHoleX(blockPos))
            return false;

        if (!mc.world.getBlockState(blockPos).getBlock().equals(Blocks.AIR) || !mc.world.getBlockState(blockPos.add(1, 0, 0)).getBlock().equals(Blocks.AIR) || !mc.world.getBlockState(blockPos.add(0, 1, 0)).getBlock().equals(Blocks.AIR) && !mc.world.getBlockState(blockPos.add(1, 1, 0)).getBlock().equals(Blocks.AIR) || !mc.world.getBlockState(blockPos.add(0, 2, 0)).getBlock().equals(Blocks.AIR) && !mc.world.getBlockState(blockPos.add(1, 2, 0)).getBlock().equals(Blocks.AIR))
            return false;

        for (BlockPos connectedPos : new BlockPos[] { blockPos.add(2, 0, 0), blockPos.add(1, 0, 1), blockPos.add(1, 0, -1), blockPos.add(-1, 0, 0), blockPos.add(0, 0, 1), blockPos.add(0, 0, -1), blockPos.add(0, -1, 0), blockPos.add(1, -1, 0) }) {
            if (BlockUtil.getBlockResistance(connectedPos) != BlockResistance.BLANK && (BlockUtil.getBlockResistance(connectedPos) == BlockResistance.RESISTANT || BlockUtil.getBlockResistance(connectedPos) == BlockResistance.UNBREAKABLE))
                continue;

            return false;
        }

        return true;
    }

    public static boolean isDoubleObsidianHoleZ(BlockPos blockPos) {
        if (isDoubleBedrockHoleZ(blockPos))
            return false;

        if (!mc.world.getBlockState(blockPos).getBlock().equals(Blocks.AIR) || !mc.world.getBlockState(blockPos.add(0, 0, 1)).getBlock().equals(Blocks.AIR) || !mc.world.getBlockState(blockPos.add(0, 1, 0)).getBlock().equals(Blocks.AIR) && !mc.world.getBlockState(blockPos.add(0, 1, 1)).getBlock().equals(Blocks.AIR) || !mc.world.getBlockState(blockPos.add(0, 2, 0)).getBlock().equals(Blocks.AIR) && !mc.world.getBlockState(blockPos.add(0, 2, 1)).getBlock().equals(Blocks.AIR))
            return false;

        for (BlockPos connectedPos : new BlockPos[] { blockPos.add(0, 0, 2), blockPos.add(1, 0, 1), blockPos.add(-1, 0, 1), blockPos.add(0, 0, -1), blockPos.add(1, 0, 0), blockPos.add(-1, 0, 0), blockPos.add(0, -1, 0), blockPos.add(0, -1, 1) }) {
            if (BlockUtil.getBlockResistance(connectedPos) != BlockResistance.BLANK && (BlockUtil.getBlockResistance(connectedPos) == BlockResistance.RESISTANT || BlockUtil.getBlockResistance(connectedPos) == BlockResistance.UNBREAKABLE))
                continue;

            return false;
        }

        return true;
    }


    public static boolean isObsidianHole(BlockPos blockPos) {
        return !(BlockUtil.getBlockResistance(blockPos.add(0, 1, 0)) != BlockResistance.BLANK || isBedRockHole(blockPos) || BlockUtil.getBlockResistance(blockPos.add(0, 0, 0)) != BlockResistance.BLANK || BlockUtil.getBlockResistance(blockPos.add(0, 2, 0)) != BlockResistance.BLANK || BlockUtil.getBlockResistance(blockPos.add(0, 0, -1)) != BlockResistance.RESISTANT && BlockUtil.getBlockResistance(blockPos.add(0, 0, -1)) != BlockResistance.UNBREAKABLE || BlockUtil.getBlockResistance(blockPos.add(1, 0, 0)) != BlockResistance.RESISTANT && BlockUtil.getBlockResistance(blockPos.add(1, 0, 0)) != BlockResistance.UNBREAKABLE || BlockUtil.getBlockResistance(blockPos.add(-1, 0, 0)) != BlockResistance.RESISTANT && BlockUtil.getBlockResistance(blockPos.add(-1, 0, 0)) != BlockResistance.UNBREAKABLE || BlockUtil.getBlockResistance(blockPos.add(0, 0, 1)) != BlockResistance.RESISTANT && BlockUtil.getBlockResistance(blockPos.add(0, 0, 1)) != BlockResistance.UNBREAKABLE || BlockUtil.getBlockResistance(blockPos.add(0.5, 0.5, 0.5)) != BlockResistance.BLANK || BlockUtil.getBlockResistance(blockPos.add(0, -1, 0)) != BlockResistance.RESISTANT && BlockUtil.getBlockResistance(blockPos.add(0, -1, 0)) != BlockResistance.UNBREAKABLE);
    }

    public static boolean isBedRockHole(BlockPos blockPos) {
        return BlockUtil.getBlockResistance(blockPos.add(0, 1, 0)) == BlockResistance.BLANK && BlockUtil.getBlockResistance(blockPos.add(0, 0, 0)) == BlockResistance.BLANK && BlockUtil.getBlockResistance(blockPos.add(0, 2, 0)) == BlockResistance.BLANK && BlockUtil.getBlockResistance(blockPos.add(0, 0, -1)) == BlockResistance.UNBREAKABLE && BlockUtil.getBlockResistance(blockPos.add(1, 0, 0)) == BlockResistance.UNBREAKABLE && BlockUtil.getBlockResistance(blockPos.add(-1, 0, 0)) == BlockResistance.UNBREAKABLE && BlockUtil.getBlockResistance(blockPos.add(0, 0, 1)) == BlockResistance.UNBREAKABLE && BlockUtil.getBlockResistance(blockPos.add(0.5, 0.5, 0.5)) == BlockResistance.BLANK && BlockUtil.getBlockResistance(blockPos.add(0, -1, 0)) == BlockResistance.UNBREAKABLE;
    }

    public static boolean isVoidHole(BlockPos blockPos) {
        return mc.player.dimension == -1 ? (blockPos.getY() == 0 || blockPos.getY() == 127) && !Objects.equals(BlockUtil.getBlockResistance(blockPos), BlockResistance.UNBREAKABLE) : blockPos.getY() == 0 && !Objects.equals(BlockUtil.getBlockResistance(blockPos), BlockResistance.UNBREAKABLE);
    }
}
