package cope.cosmos.util.player;

import cope.cosmos.util.Wrapper;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Mouse;

public class PlayerUtil implements Wrapper {

    public static double getHealth() {
        return mc.player.getHealth() + mc.player.getAbsorptionAmount();
    }

    public static BlockPos getPosition() {
        return new BlockPos(mc.player.posX + 0.5, mc.player.posY, mc.player.posZ + 0.5);
    }

    public static void attackEntity(Entity entity, boolean packet, Hand hand, double variation) {
        if (Math.random() <= (variation / 100)) {
            if (packet)
                mc.player.connection.sendPacket(new CPacketUseEntity(entity));
            else
                mc.playerController.attackEntity(mc.player, entity);
        }

        swingArm(hand);
        mc.player.resetCooldown();
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
        return mc.player.getHeldItemMainhand().getItemUseAction().equals(EnumAction.EAT) || mc.player.getHeldItemMainhand().getItemUseAction().equals(EnumAction.DRINK);
    }

    public static boolean isMending() {
        return InventoryUtil.isHolding(Items.EXPERIENCE_BOTTLE) && Mouse.isButtonDown(1);
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
