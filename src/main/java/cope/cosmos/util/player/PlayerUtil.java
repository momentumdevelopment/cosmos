package cope.cosmos.util.player;

import cope.cosmos.util.Wrapper;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.util.EnumHand;

public class PlayerUtil implements Wrapper {

    public static double getHealth() {
        return mc.player.getHealth() + mc.player.getAbsorptionAmount();
    }

    public static void swingArm(Hand hand) {
        switch (hand) {
            case MAINHAND:
                mc.player.swingArm(EnumHand.MAIN_HAND);
                break;
            case OFFHAND:
                mc.player.swingArm(EnumHand.OFF_HAND);
                break;
            case PACKET:
                mc.player.connection.sendPacket(new CPacketAnimation(mc.player.getHeldItemMainhand().getItem().equals(Items.END_CRYSTAL) ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND));
                break;
            case NONE:
                break;
        }
    }

    public static void lockLimbs() {
        mc.player.prevLimbSwingAmount = 0;
        mc.player.limbSwingAmount = 0;
        mc.player.limbSwing = 0;
    }

    public static boolean isEating() {
        if (!mc.player.isHandActive()) {
            return false;
        }

        return mc.player.getActiveItemStack().getItemUseAction().equals(EnumAction.EAT) || mc.player.getActiveItemStack().getItemUseAction().equals(EnumAction.DRINK);
    }

    public static boolean isMending() {
        return mc.player.isHandActive() && mc.player.getActiveItemStack().getItem() == Items.EXPERIENCE_BOTTLE && mc.gameSettings.keyBindUseItem.isKeyDown();
    }

    public static boolean isMining() {
        return InventoryUtil.isHolding(Items.DIAMOND_PICKAXE) && mc.playerController.getIsHittingBlock();
    }

    public static boolean isInLiquid() {
        return mc.player.isInLava() || mc.player.isInWater();
    }

    public static boolean isCollided() {
        return mc.player.collidedHorizontally || mc.player.collidedVertically;
    }

    public enum Hand {
        MAINHAND, OFFHAND, PACKET, NONE
    }
}
