package cope.cosmos.client.features.modules.player;

import cope.cosmos.asm.mixins.accessor.ICPacketPlayerTryUseItemOnBlock;
import cope.cosmos.client.events.EntityHitboxSizeEvent;
import cope.cosmos.client.events.LiquidInteractEvent;
import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.events.ReachEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.combat.AutoCrystal;
import cope.cosmos.client.features.modules.combat.Burrow;
import cope.cosmos.client.features.modules.combat.HoleFill;
import cope.cosmos.client.features.modules.combat.Surround;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.PlayerUtil;
import cope.cosmos.util.player.PlayerUtil.Hand;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
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

    public static Setting<Double> reach = new Setting<>("Reach", 0.0, 0.0, 3.0, 2).setDescription("Player reach extension");
    public static Setting<Hand> hand = new Setting<>("Hand", Hand.NONE).setDescription("Swinging hand");
    public static Setting<Boolean> ghostHand = new Setting<>("GhostHand", false).setDescription("Allows you to interact with blocks through walls");
    public static Setting<Boolean> noSwing = new Setting<>("NoSwing", false).setDescription("Cancels the server side animation for swinging");

    public static Setting<Boolean> ignoreContainer = new Setting<>("IgnoreContainers", false).setDescription("Ignores containers");

    public static Setting<Boolean> hitBox = new Setting<>("HitBox", true).setDescription("Ignores entity hitboxes");
    public static Setting<Double> hitBoxExtend = new Setting<>("Extend", 0.0, 0.0, 2.0, 2).setParent(hitBox).setDescription("Entity hitbox extension").setVisible(() -> !hitBox.getValue());
    public static Setting<Boolean> hitBoxPlayers = new Setting<>("PlayersOnly", true).setParent(hitBox).setDescription("Only ignores player hitboxes").setVisible(() -> hitBox.getValue());

    public static Setting<Boolean> liquid = new Setting<>("Liquid", false).setDescription("Allows you to place blocks on liquid");
    public static Setting<Boolean> heightLimit = new Setting<>("HeightLimit", true).setDescription("Allows you to interact with blocks at height limit");
    public static Setting<Boolean> worldBorder = new Setting<>("WorldBorder", false).setDescription("Allows you to interact with blocks at the world border");

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

        if (ghostHand.getValue() && mc.gameSettings.keyBindUseItem.isKeyDown()) {
            if (AutoCrystal.INSTANCE.isActive() || HoleFill.INSTANCE.isActive() || Burrow.INSTANCE.isEnabled() || Surround.INSTANCE.isActive()) {
                return;
            }

            new ArrayList<>(mc.world.loadedTileEntityList).forEach(tileEntity -> {
                RayTraceResult openResult = mc.player.rayTrace(mc.playerController.getBlockReachDistance(), mc.getRenderPartialTicks());

                if (openResult != null) {
                    if (openResult.getBlockPos().equals(tileEntity.getPos())) {
                        return;
                    }

                    if (openResult.typeOfHit.equals(RayTraceResult.Type.BLOCK) && openResult.getBlockPos().getDistance(tileEntity.getPos().getX(), tileEntity.getPos().getY(), tileEntity.getPos().getZ()) <= 5) {
                        mc.playerController.processRightClickBlock(mc.player, mc.world, tileEntity.getPos(), EnumFacing.UP, new Vec3d(0, 0, 0), EnumHand.MAIN_HAND);
                    }
                }
            });
        }

        if (hitBox.getValue() && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit.equals(RayTraceResult.Type.ENTITY)) {
            if (hitBoxPlayers.getValue() && !(mc.objectMouseOver.entityHit instanceof EntityPlayer)) {
                return;
            }

            RayTraceResult hitboxResult = mc.player.rayTrace(mc.playerController.getBlockReachDistance(), mc.getRenderPartialTicks());

            if (hitboxResult != null && hitboxResult.typeOfHit.equals(RayTraceResult.Type.BLOCK)) {
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
}
