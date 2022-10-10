package cope.cosmos.client.features.modules.world;

import cope.cosmos.asm.mixins.accessor.ICPacketPlayerTryUseItemOnBlock;
import cope.cosmos.client.events.block.LiquidInteractEvent;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.combat.AutoCrystalModule;
import cope.cosmos.client.features.modules.combat.SelfFillModule;
import cope.cosmos.client.features.modules.combat.HoleFillModule;
import cope.cosmos.client.features.modules.combat.SurroundModule;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.world.SneakBlocks;
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
 * This module attempts to put all the bloat modules (i.e. NoInteract, GhostHand, LiquidInteract, NoHeightLimit) in one module
 * @author linustouchtips
 * @since 06/08/2021
 */
public class InteractModule extends Module {
    public static InteractModule INSTANCE;

    public InteractModule() {
        super("Interact", new String[] {"NoInteract", "LiquidInteract", "NoHeightLimit"}, Category.WORLD, "Changes how you interact with blocks & entities");
        INSTANCE = this;
    }

    // **************************** hand interactions ****************************

    public static Setting<Boolean> ghostHand = new Setting<>("GhostHand", false)
            .setDescription("Allows you to interact with blocks through walls");

    public static Setting<Boolean> noSwing = new Setting<>("NoSwing", false)
            .setDescription("Cancels the server side animation for swinging");

    // **************************** block interactions ****************************

    public static Setting<Boolean> ignoreContainer = new Setting<>("IgnoreContainers", false)
            .setDescription("Ignores containers");

    // **************************** illegal interactions ****************************

    public static Setting<Boolean> liquid = new Setting<>("Liquid", false)
            .setDescription("Allows you to place blocks on liquid");

    public static Setting<Boolean> heightLimit = new Setting<>("HeightLimit", true)
            .setDescription("Allows you to interact with blocks at height limit");

    public static Setting<Boolean> worldBorder = new Setting<>("WorldBorder", false)
            .setDescription("Allows you to interact with blocks at the world border");

    @SubscribeEvent
    public void onLiquidInteract(LiquidInteractEvent event) {

        // allow interactions with liquids
        if (liquid.getValue() || event.getLiquidLevel() && event.getBlockState().getValue(BlockLiquid.LEVEL) == 0) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {

        // packet for placing on blocks
        if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {

            // position where we need to limit the face
            BlockPos limitPosition = ((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getPos();

            // click on the the opposite face at height borders
            if (heightLimit.getValue() && limitPosition.getY() >= (mc.world.getHeight() - 1) && ((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getDirection().equals(EnumFacing.UP)) {
                ((ICPacketPlayerTryUseItemOnBlock) event.getPacket()).setDirection(EnumFacing.DOWN);
            }

            // click on the the opposite face at world borders
            if (worldBorder.getValue() && mc.world.getWorldBorder().contains(limitPosition)) {

                // opposite face
                EnumFacing oppositeFace = ((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getDirection().getOpposite();

                // update packet
                ((ICPacketPlayerTryUseItemOnBlock) event.getPacket()).setDirection(oppositeFace);
            }

            // cancel the right-click packet if we're interacting with a container
            if (ignoreContainer.getValue()) {
                if (SneakBlocks.contains(mc.world.getBlockState(limitPosition).getBlock())) {
                    event.setCanceled(true);
                }
            }

            if (ghostHand.getValue()) {

                // check if we are auto-placing, we can't attempt to open containers during an auto-place process
                if (AutoCrystalModule.INSTANCE.isActive() || HoleFillModule.INSTANCE.isActive() || SelfFillModule.INSTANCE.isEnabled() || SurroundModule.INSTANCE.isActive()) {
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
