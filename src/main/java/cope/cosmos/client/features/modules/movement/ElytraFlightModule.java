package cope.cosmos.client.features.modules.movement;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.events.motion.movement.TravelEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.InventoryManager.Switch;
import cope.cosmos.util.string.StringFormatter;
import cope.cosmos.util.player.MotionUtil;
import cope.cosmos.util.player.PlayerUtil;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@SuppressWarnings("unused")
public class ElytraFlightModule extends Module {
    public static ElytraFlightModule INSTANCE;

    public ElytraFlightModule() {
        super("ElytraFlight", Category.MOVEMENT, "Allows you to fly faster on an elytra", () -> StringFormatter.formatEnum(mode.getValue()));
        INSTANCE = this;
    }

    public static Setting<Elytra> mode = new Setting<>("Mode", Elytra.CONTROL).setDescription("Mode for ElytraFlight");
    public static Setting<Double> yaw = new Setting<>("Yaw", 0.0, 30.0, 90.0, 1).setDescription("Maximum allowed yaw").setVisible(() -> mode.getValue().equals(Elytra.CONTROL_STRICT));
    public static Setting<Double> pitch = new Setting<>("Pitch", 0.0, 30.0, 90.0, 1).setDescription("Maximum allowed pitch").setVisible(() -> mode.getValue().equals(Elytra.CONTROL_STRICT));

    public static Setting<Double> glide = new Setting<>("Glide", 0.0, 2.5, 5.0, 2).setDescription("Speed when gliding");
    public static Setting<Double> ascend = new Setting<>("Ascend", 0.0, 1.0, 5.0, 2).setDescription("Speed when ascending");
    public static Setting<Double> descend = new Setting<>("Descend", 0.0, 1.0, 5.0, 2).setDescription("Speed when descending");
    public static Setting<Double> fall = new Setting<>("Fall", 0.0, 0.0, 0.1, 3).setDescription("Speed when stationary");
    public static Setting<Switch> firework = new Setting<>("Firework", Switch.NONE).setDescription("Mode to switch to fireworks if necessary");
    public static Setting<Boolean> lockRotation = new Setting<>("LockRotation", false).setDescription("Locks rotation and flies in a straight path");

    public static Setting<Boolean> takeOff = new Setting<>("TakeOff", false).setDescription("Easier takeoff");
    public static Setting<Double> takeOffTimer = new Setting<>("Timer", 0.0, 0.2, 1.0, 2).setDescription("Timer ticks when taking off").setParent(takeOff);

    public static Setting<Boolean> pause = new Setting<>("Pause", true).setDescription("Pause elytra flight when");
    public static Setting<Boolean> pauseLiquid = new Setting<>("Liquid", true).setDescription("When in liquid").setParent(pause);
    public static Setting<Boolean> pauseCollision = new Setting<>("Collision", false).setDescription("When colliding").setParent(pause);

    @SubscribeEvent
    public void onTravel(TravelEvent event) {
        if (nullCheck()) {
            if (mc.player.isElytraFlying()) {
                if (pause.getValue()) {
                    if (PlayerUtil.isInLiquid() && pauseLiquid.getValue()) {
                        return;
                    }

                    else if (PlayerUtil.isCollided() && pauseCollision.getValue()) {
                        return;
                    }
                }

                // cancel vanilla movements
                event.setCanceled(true);

                elytraFlight();
            }
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (!mc.player.isElytraFlying() && takeOff.getValue()) {
            getCosmos().getTickManager().setClientTicks(takeOffTimer.getValue().floatValue());

            if (mc.player.onGround) {
                mc.player.jump();
            }

            else {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
            }
        }
    }

    public void elytraFlight() {
        Cosmos.INSTANCE.getTickManager().setClientTicks(1);

       if (lockRotation.getValue()) {
            mc.player.rotationYaw = MathHelper.clamp(mc.player.rotationYaw, -yaw.getValue().floatValue(), yaw.getValue().floatValue());
            mc.player.rotationPitch = MathHelper.clamp(mc.player.rotationPitch, -pitch.getValue().floatValue(), pitch.getValue().floatValue());
        }

        MotionUtil.stopMotion(-fall.getValue());
        MotionUtil.setMoveSpeed(glide.getValue(), 0.6F);

        switch (mode.getValue()) {
            case CONTROL:
                handleControl();
                break;
            case CONTROL_STRICT:
                handleStrict();
                break;
            case PACKET:
                break;
        }

        mc.player.prevLimbSwingAmount = 0;
        mc.player.limbSwingAmount = 0;
        mc.player.limbSwing = 0;
    }

    public void handleControl() {
        if (mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.player.motionY = ascend.getValue();
        }

        else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
            mc.player.motionY = -descend.getValue();
        }
    }

    public void handleStrict() {
        if (mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.player.rotationPitch = -pitch.getValue().floatValue();
            mc.player.motionY = ascend.getValue();
        }

        else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
            mc.player.rotationPitch = pitch.getValue().floatValue();
            mc.player.motionY = -descend.getValue();
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {
        if (event.getPacket() instanceof SPacketPlayerPosLook && !firework.getValue().equals(Switch.NONE)) {
            if (mc.player.isElytraFlying()) {
                getCosmos().getInventoryManager().switchToItem(Items.FIREWORKS, firework.getValue());
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
            }
        }
    }

    @Override
    public boolean isActive() {
        return isEnabled() && mc.player.isElytraFlying();
    }

    public enum Elytra {
        CONTROL, CONTROL_STRICT, PACKET
    }
}
