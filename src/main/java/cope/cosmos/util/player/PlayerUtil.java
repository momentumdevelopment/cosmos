package cope.cosmos.util.player;

import cope.cosmos.util.Wrapper;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;

/**
 * @author linustouchtips, aesthetical
 * @since 05/07/2021
 */
public class PlayerUtil implements Wrapper {

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
        return mc.player.isHandActive() && mc.player.getActiveItemStack().getItem().equals(Items.EXPERIENCE_BOTTLE) && mc.gameSettings.keyBindUseItem.isKeyDown();
    }

    /**
     * Checks if the player is mining
     * @return Whether the player is mining
     */
    public static boolean isMining() {
        return InventoryUtil.isHolding(Items.DIAMOND_PICKAXE) && mc.playerController.getIsHittingBlock();
    }

    /**
     * Checks if the player is liquid
     * @return Whether the player is in liquid
     */
    public static boolean isInLiquid() {
        return mc.player.isInLava() || mc.player.isInWater();
    }

    /**
     * Checks if the player is collided with a block
     * @return Whether the player is collided with a block
     */
    public static boolean isCollided() {
        return mc.player.collidedHorizontally || mc.player.collidedVertically;
    }
}
