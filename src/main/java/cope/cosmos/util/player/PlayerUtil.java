package cope.cosmos.util.player;

import cope.cosmos.client.features.modules.movement.FlightModule;
import cope.cosmos.util.Wrapper;
import cope.cosmos.util.world.BlockUtil;
import net.minecraft.block.material.Material;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemTool;
import net.minecraft.util.math.BlockPos;

/**
 * @author linustouchtips, aesthetical
 * @since 05/07/2021
 */
public class PlayerUtil implements Wrapper {

    /**
     * Gets the player's position
     * @return The player's position
     */
    public static BlockPos getPosition() {
        return new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
    }

    /**
     * Gets the player's total health (i.e. health + absorption)
     * @return The player's total health
     */
    public static double getHealth() {
        return mc.player.getHealth() + mc.player.getAbsorptionAmount();
    }

    /**
     * Checks if the player is eating
     * @return Whether the player is eating
     */
    public static boolean isEating() {
        if (mc.player.isHandActive()) {
            return mc.player.getActiveItemStack().getItemUseAction().equals(EnumAction.EAT) || mc.player.getActiveItemStack().getItemUseAction().equals(EnumAction.DRINK);
        }

        return false;
    }

    /**
     * Checks if the player is mending
     * @return Whether the player is mending
     */
    public static boolean isMending() {
        return InventoryUtil.isHolding(Items.EXPERIENCE_BOTTLE) && mc.gameSettings.keyBindUseItem.isKeyDown();
    }

    /**
     * Checks if the player is mining
     * @return Whether the player is mining
     */
    public static boolean isMining() {
        return InventoryUtil.isHolding(ItemTool.class) && mc.playerController.getIsHittingBlock() && BlockUtil.isBreakable(mc.objectMouseOver.getBlockPos()) && !mc.world.isAirBlock(mc.objectMouseOver.getBlockPos());
    }

    /**
     * Checks if the player is liquid
     * @return Whether the player is in liquid
     */
    public static boolean isInLiquid() {
        return mc.player.isInsideOfMaterial(Material.LAVA) || mc.player.isInsideOfMaterial(Material.WATER) || mc.player.isInWater() || mc.player.isInLava();
    }

    /**
     * Checks if the player is collided with a block
     * @return Whether the player is collided with a block
     */
    public static boolean isCollided() {
        return mc.player.collidedHorizontally || mc.player.collidedVertically;
    }

    /**
     * Checks if the player is flying
     * @return Whether the player is flying
     */
    public static boolean isFlying() {
        return FlightModule.INSTANCE.isActive() || mc.player.isElytraFlying() || mc.player.capabilities.isFlying;
    }
}