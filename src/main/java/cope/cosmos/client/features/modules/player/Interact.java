package cope.cosmos.client.features.modules.player;

import cope.cosmos.asm.mixins.accessor.ICPacketPlayerTryUseItemOnBlock;
import cope.cosmos.client.events.LiquidInteractEvent;
import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.combat.AutoCrystal;
import cope.cosmos.client.features.modules.combat.Burrow;
import cope.cosmos.client.features.modules.combat.HoleFill;
import cope.cosmos.client.features.modules.combat.Surround;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.PlayerUtil.Hand;
import net.minecraft.block.BlockLiquid;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;

/**
 * @author linustouchtips
 * @since 06/08/2021
 */
@SuppressWarnings("unused")
public class Interact extends Module {
    public static Interact INSTANCE;

    public Interact() {
        super("Interact", Category.PLAYER, "Changes how you interact with blocks & entities");
        INSTANCE = this;
    }

    // this module attempts to put all the bloat modules (i.e. Swing, LiquidInteract, NoHeightLimit) in one module

    // hand interactions
    public static Setting<Hand> hand = new Setting<>("Hand", Hand.NONE).setDescription("Swinging hand");
    public static Setting<Boolean> ghostHand = new Setting<>("GhostHand", false).setDescription("Allows you to interact with blocks through walls");
    public static Setting<Boolean> noSwing = new Setting<>("NoSwing", false).setDescription("Cancels the server side animation for swinging");

    // block interactions
    public static Setting<Boolean> ignoreContainer = new Setting<>("IgnoreContainers", false).setDescription("Ignores containers");

    // illegal interactions
    public static Setting<Boolean> liquid = new Setting<>("Liquid", false).setDescription("Allows you to place blocks on liquid");
    public static Setting<Boolean> heightLimit = new Setting<>("HeightLimit", true).setDescription("Allows you to interact with blocks at height limit");
    public static Setting<Boolean> worldBorder = new Setting<>("WorldBorder", false).setDescription("Allows you to interact with blocks at the world border");

    @Override
    public void onUpdate() {
        if (!hand.getValue().equals(Hand.NONE)) {
            // update the player's swinging hand
            switch (hand.getValue()) {
                case MAINHAND:
                    mc.player.swingingHand = EnumHand.MAIN_HAND;
                    break;
                case OFFHAND:
                    mc.player.swingingHand = EnumHand.OFF_HAND;
                    break;
            }
        }
    }

    @SubscribeEvent
    public void onLiquidInteract(LiquidInteractEvent event) {
        // allow interactions with liquids
        event.setCanceled(liquid.getValue() || event.getLiquidLevel() && event.getBlockState().getValue(BlockLiquid.LEVEL) == 0);
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
            // position where we need to limit the face
            BlockPos limitPosition = ((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getPos();

            // click on the the opposite face at height borders
            if (heightLimit.getValue() && limitPosition.getY() >= (mc.world.getHeight() - 1) && ((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getDirection().equals(EnumFacing.UP)) {
                ((ICPacketPlayerTryUseItemOnBlock) event.getPacket()).setDirection(EnumFacing.DOWN);
            }

            // click on the the opposite face at world borders
            if (worldBorder.getValue() && mc.world.getWorldBorder().contains(limitPosition)) {
                switch (((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getDirection()) {
                    case EAST:
                        ((ICPacketPlayerTryUseItemOnBlock) event.getPacket()).setDirection(EnumFacing.WEST);
                        break;
                    case WEST:
                        ((ICPacketPlayerTryUseItemOnBlock) event.getPacket()).setDirection(EnumFacing.EAST);
                        break;
                    case NORTH:
                        ((ICPacketPlayerTryUseItemOnBlock) event.getPacket()).setDirection(EnumFacing.SOUTH);
                        break;
                    case SOUTH:
                        ((ICPacketPlayerTryUseItemOnBlock) event.getPacket()).setDirection(EnumFacing.NORTH);
                        break;
                }
            }

            // cancel the right-click packet if we're interacting with a container
            if (getCosmos().getInteractionManager().getSneakBlocks().contains(mc.world.getBlockState(limitPosition).getBlock()) && ignoreContainer.getValue()) {
                event.setCanceled(true);
            }

            if (ghostHand.getValue()) {
                // check if we are auto-placing, we can't attempt to open containers during an auto-place process
                if (AutoCrystal.INSTANCE.isActive() || HoleFill.INSTANCE.isActive() || Burrow.INSTANCE.isEnabled() || Surround.INSTANCE.isActive()) {
                    return;
                }

                // iterate through all containers in the world
                new ArrayList<>(mc.world.loadedTileEntityList).forEach(tileEntity -> {
                    // if we clicked the container then we don't need to cancel the click
                    if (limitPosition.equals(tileEntity.getPos())) {
                        return;
                    }

                    // cancel the click and click the container instead
                    if (limitPosition.getDistance(tileEntity.getPos().getX(), tileEntity.getPos().getY(), tileEntity.getPos().getZ()) <= 3) {
                        mc.playerController.processRightClickBlock(mc.player, mc.world, tileEntity.getPos(), EnumFacing.UP, new Vec3d(0, 0, 0), EnumHand.MAIN_HAND);
                    }
                });
            }
        }

        // cancel the swing animation packet
        if (event.getPacket() instanceof CPacketAnimation && noSwing.getValue()) {
            event.setCanceled(true);
        }
    }
}
