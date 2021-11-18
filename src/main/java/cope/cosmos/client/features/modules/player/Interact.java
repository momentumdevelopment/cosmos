package cope.cosmos.client.features.modules.player;

import cope.cosmos.asm.mixins.accessor.ICPacketPlayerTryUseItemOnBlock;
import cope.cosmos.client.events.*;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.combat.*;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.PlayerUtil;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import cope.cosmos.util.player.PlayerUtil.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class Interact extends Module {
    public static Interact INSTANCE;

    public Interact() {
        super("Interact", Category.PLAYER, "Changes how you interact with blocks & entities");
        INSTANCE = this;
    }

    public static Setting<Double> reach = new Setting<>("Reach", "Player reach extension", 0.0, 0.0, 3.0, 2);
    public static Setting<Hand> hand = new Setting<>("Hand", "Swinging hand", Hand.NONE);
    public static Setting<Boolean> ghostHand = new Setting<>("GhostHand", "Allows you to interact with blocks through walls", false);
    public static Setting<Boolean> noSwing = new Setting<>("NoSwing", "Cancels the server side animation for swinging", false);

    public static Setting<Boolean> ignoreContainer = new Setting<>("IgnoreContainers", "Ignores containers", false);

    public static Setting<Boolean> hitBox = new Setting<>("HitBox", "Ignores entity hitboxes", true);
    public static Setting<Double> hitBoxExtend = new Setting<>("Extend", "Entity hitbox extension", 0.0, 0.0, 2.0, 2).setParent(hitBox);
    public static Setting<Boolean> hitBoxPlayers = new Setting<>("PlayersOnly", "Only ignores player hitboxes", true).setParent(hitBox);

    public static Setting<Boolean> liquid = new Setting<>("Liquid", "Allows you to place blocks on liquid", false);
    public static Setting<Boolean> heightLimit = new Setting<>("HeightLimit", "Allows you to interact with blocks at height limit", true);
    public static Setting<Boolean> worldBorder = new Setting<>("WorldBorder", "Allows you to interact with blocks at the world border", false);

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

        if (ghostHand.getValue() && mc.gameSettings.keyBindUseItem.isKeyDown() && !isAutoPlacing()) {
            new ArrayList<>(mc.world.loadedTileEntityList).forEach(tileEntity -> {
                RayTraceResult openResult = mc.player.rayTrace(mc.playerController.getBlockReachDistance(), mc.getRenderPartialTicks());

                if (openResult != null) {
                    if (openResult.getBlockPos().equals(tileEntity.getPos()))
                        return;

                    if (openResult.typeOfHit.equals(RayTraceResult.Type.BLOCK) && openResult.getBlockPos().getDistance(tileEntity.getPos().getX(), tileEntity.getPos().getY(), tileEntity.getPos().getZ()) <= 5) {
                        mc.playerController.processRightClickBlock(mc.player, mc.world, tileEntity.getPos(), EnumFacing.UP, new Vec3d(0, 0, 0), EnumHand.MAIN_HAND);
                    }
                }
            });
        }

        if (hitBox.getValue() && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit.equals(RayTraceResult.Type.ENTITY)) {
            if (hitBoxPlayers.getValue() && !(mc.objectMouseOver.entityHit instanceof EntityPlayer))
                return;

            RayTraceResult hitboxResult = mc.player.rayTrace(mc.playerController.getBlockReachDistance(), mc.getRenderPartialTicks());

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
        if (hitBox.getValue()) {
            event.setHitboxSize(hitBoxExtend.getValue().floatValue());
        }
    }

    @SubscribeEvent
    public void onReach(ReachEvent event) {
        event.setReach((mc.player.capabilities.isCreativeMode ? 5 : 4.5F) + reach.getValue().floatValue());
    }

    @SubscribeEvent
    public void onLiquidInteract(LiquidInteractEvent event) {
        event.setCanceled(liquid.getValue() || event.getLiquidLevel() && event.getBlockState().getValue(BlockLiquid.LEVEL) == 0);
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
            BlockPos limitPosition = ((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getPos();

            if (heightLimit.getValue() && limitPosition.getY() >= (mc.world.getHeight() - 1) && ((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getDirection().equals(EnumFacing.UP)) {
                ((ICPacketPlayerTryUseItemOnBlock) event.getPacket()).setDirection(EnumFacing.DOWN);
            }

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

            if (getCosmos().getInteractionManager().getSneakBlocks().contains(mc.world.getBlockState(limitPosition).getBlock()) && ignoreContainer.getValue()) {
                event.setCanceled(true);
            }
        }

        if (event.getPacket() instanceof CPacketAnimation && noSwing.getValue()) {
            event.setCanceled(true);
        }
    }

    public boolean isAutoPlacing() {
        return AutoCrystal.INSTANCE.isActive() || HoleFill.INSTANCE.isActive() || Burrow.INSTANCE.isEnabled() || Surround.INSTANCE.isActive();
    }
}
