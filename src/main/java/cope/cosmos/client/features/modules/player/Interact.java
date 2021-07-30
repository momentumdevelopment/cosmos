package cope.cosmos.client.features.modules.player;

import cope.cosmos.client.events.*;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.PlayerUtil;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import cope.cosmos.util.player.PlayerUtil.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Objects;

@SuppressWarnings("unused")
public class Interact extends Module {
    public static Interact INSTANCE;

    public Interact() {
        super("Interact", Category.PLAYER, "Interaction changes & various exploits");
        INSTANCE = this;
    }

    public static Setting<Double> reach = new Setting<>("Reach", "Player reach extension", 0.0, 0.0, 3.0, 2);
    public static Setting<Hand> hand = new Setting<>("Hand", "Swinging hand", Hand.NONE);
    public static Setting<Boolean> ghostHand = new Setting<>("GhostHand", "Allows you to interact with blocks through walls", false);

    public static Setting<Boolean> hitBox = new Setting<>("HitBox", "Ignores entity hitboxes", true);
    public static Setting<Double> hitBoxExtend = new Setting<>("Extend", "Entity hitbox extension", 0.0, 0.0, 2.0, 2).setParent(hitBox);
    public static Setting<Boolean> hitBoxPlayers = new Setting<>("PlayersOnly", "Only ignores player hitboxes", true).setParent(hitBox);

    public static Setting<Boolean> liquid = new Setting<>("Liquid", "Allows you to place blocks on liquid", false);
    public static Setting<Boolean> heightLimit = new Setting<>("HeightLimit", "Allows you to interact with blocks at height limit", true);

    @Override
    public void onUpdate() {
        if (!hand.getValue().equals(Hand.NONE)) {
            switch (hand.getValue()) {
                case MAINHAND:
                    mc.player.swingingHand = EnumHand.MAIN_HAND;
                    break;
                case OFFHAND:
                    mc.player.swingingHand = EnumHand.OFF_HAND;
                    break;
            }
        }

        if (ghostHand.getValue()) {
            new ArrayList<>(mc.world.loadedTileEntityList).forEach(tileEntity -> {
                if (new BlockPos(Objects.requireNonNull(mc.player.rayTrace(mc.playerController.getBlockReachDistance(), mc.getRenderPartialTicks())).getBlockPos()).equals(tileEntity.getPos()))
                    return;

                RayTraceResult rayTraceResult = mc.player.rayTrace(mc.playerController.getBlockReachDistance(), mc.getRenderPartialTicks());

                if (rayTraceResult != null && rayTraceResult.typeOfHit.equals(RayTraceResult.Type.BLOCK) && rayTraceResult.getBlockPos().getDistance(tileEntity.getPos().getX(), tileEntity.getPos().getY(), tileEntity.getPos().getZ()) <= 5 && mc.gameSettings.keyBindUseItem.isKeyDown())
                    mc.playerController.processRightClickBlock(mc.player, mc.world, tileEntity.getPos(), EnumFacing.UP, new Vec3d(0, 0, 0), EnumHand.MAIN_HAND);

            });
        }

        if (hitBox.getValue() && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit.equals(RayTraceResult.Type.ENTITY)) {
            if (hitBoxPlayers.getValue() && !(mc.objectMouseOver.entityHit instanceof EntityPlayer))
                return;

            RayTraceResult hitboxResult = mc.player.rayTrace(6, mc.getRenderPartialTicks());

            if (hitboxResult != null && hitboxResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                BlockPos hitboxPos = hitboxResult.getBlockPos();

                if (mc.gameSettings.keyBindAttack.isKeyDown()) {
                    mc.playerController.onPlayerDamageBlock(hitboxPos, EnumFacing.UP);
                    PlayerUtil.swingArm(Hand.MAINHAND);
                }
            }
        }
    }

    @SubscribeEvent
    public void onHitboxSize(EntityHitboxSizeEvent event) {
        if (nullCheck() && hitBox.getValue())
            event.setHitboxSize((float) ((double) hitBoxExtend.getValue()));
    }

    @SubscribeEvent
    public void onReach(ReachEvent event) {
        if (nullCheck() && hitBox.getValue())
            event.setReach((mc.player.capabilities.isCreativeMode ? 5 : 4.5F) + (float) ((double) reach.getValue()));
    }

    @SubscribeEvent
    public void onLiquidInteract(LiquidInteractEvent event) {
        event.setCanceled(nullCheck() && (liquid.getValue() || event.getLiquidLevel() && event.getBlockState().getValue(BlockLiquid.LEVEL) == 0));
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (nullCheck() && event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock && heightLimit.getValue() && ((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getPos().getY() == 255 && ((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getDirection().equals(EnumFacing.UP)){
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getPos(), EnumFacing.DOWN, ((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getHand(), ((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getFacingX(), ((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getFacingY(), ((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getFacingZ()));
            event.setCanceled(true);
        }
    }
}
