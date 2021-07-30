package cope.cosmos.client.features.modules.movement;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.events.TravelEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.InventoryUtil;
import cope.cosmos.util.player.InventoryUtil.Switch;
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
public class ElytraFlight extends Module {
    public static ElytraFlight INSTANCE;

    public ElytraFlight() {
        super("ElytraFlight", Category.MOVEMENT, "Allows you to fly faster on an elytra");
        INSTANCE = this;
    }

    public static Setting<Elytra> mode = new Setting<>("Mode", "Mode for ElytraFlight", Elytra.CONTROL);
    public static Setting<Double> yaw = new Setting<>(() -> mode.getValue().equals(Elytra.STRICT),"Yaw", "Maximum allowed yaw", 0.0, 30.0, 90.0, 1);
    public static Setting<Double> pitch = new Setting<>(() -> mode.getValue().equals(Elytra.STRICT),"Pitch", "Maximum allowed pitch", 0.0, 30.0, 90.0, 1);
    public static Setting<Double> glide = new Setting<>("Glide", "Speed when gliding", 0.0, 2.5, 5.0, 2);
    public static Setting<Double> ascend = new Setting<>("Ascend", "Speed when ascending", 0.0, 1.0, 5.0, 2);
    public static Setting<Double> descend = new Setting<>("Descend", "Speed when descending", 0.0, 1.0, 5.0, 2);
    public static Setting<Double> fall = new Setting<>("Fall", "Speed when stationary", 0.0, 0.0, 0.1, 3);
    public static Setting<Switch> firework = new Setting<>("Firework", "Mode to switch to fireworks if necessary", Switch.NONE);
    public static Setting<Boolean> lockRotation = new Setting<>("LockRotation", "Locks rotation and flies in a straight path", false);

    public static Setting<Boolean> takeOff = new Setting<>("TakeOff", "Easier takeoff", false);
    public static Setting<Double> takeOffTimer = new Setting<>("Timer", "Timer ticks when taking off", 0.0, 0.2, 1.0, 2).setParent(takeOff);

    public static Setting<Boolean> pause = new Setting<>("Pause", "Pause elytra flight when", true);
    public static Setting<Boolean> pauseLiquid = new Setting<>("Liquid", "When in liquid", true).setParent(pause);
    public static Setting<Boolean> pauseCollision = new Setting<>("Collision", "When colliding", false).setParent(pause);

    @SubscribeEvent
    public void onTravel(TravelEvent event) {
        try {
            if (nullCheck() && handlePause() && mc.player.isElytraFlying())
                elytraFlight(event);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (!mc.player.isElytraFlying() && takeOff.getValue()) {
            Cosmos.INSTANCE.getTickManager().setClientTicks(takeOffTimer.getValue());

            if (mc.player.onGround)
                mc.player.motionY = 0.4;
            else
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
        }
    }

    public void elytraFlight(TravelEvent event) {
        event.setCanceled(true);
        Cosmos.INSTANCE.getTickManager().setClientTicks(1);

       if (lockRotation.getValue()) {
            mc.player.rotationYaw = (float) MathHelper.clamp(mc.player.rotationYaw, -yaw.getValue(), yaw.getValue());
            mc.player.rotationPitch = (float) MathHelper.clamp(mc.player.rotationPitch, -pitch.getValue(), pitch.getValue());
        }

        MotionUtil.stopMotion(-fall.getValue());
        MotionUtil.setMoveSpeed(glide.getValue(), 0.6F);

        switch (mode.getValue()) {
            case CONTROL:
                handleControl();
                break;
            case STRICT:
                handleStrict();
                break;
            case PACKET:
                break;
        }

        PlayerUtil.lockLimbs();
    }

    public void handleControl() {
        if (mc.gameSettings.keyBindJump.isKeyDown())
            mc.player.motionY = ascend.getValue();
        else if (mc.gameSettings.keyBindSneak.isKeyDown())
            mc.player.motionY = -descend.getValue();
    }

    public void handleStrict() {
        if (mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.player.rotationPitch = (float) -pitch.getValue();
            mc.player.motionY = ascend.getValue();
        }

        else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
            mc.player.rotationPitch = (float) ((double) pitch.getValue());
            mc.player.motionY = -descend.getValue();
        }
    }

    public boolean handlePause() {
        if (pause.getValue()) {
            if (PlayerUtil.isInLiquid() && pauseLiquid.getValue())
                return true;

            else if (PlayerUtil.isCollided() && pauseCollision.getValue())
                return true;
        }

        return true;
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {
        if (nullCheck() && event.getPacket() instanceof SPacketPlayerPosLook && !firework.getValue().equals(Switch.NONE)) {
            if (mc.player.isElytraFlying()) {
                InventoryUtil.switchToSlot(Items.FIREWORKS, firework.getValue());
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
            }
        }
    }

    public enum Elytra {
        CONTROL, STRICT, PACKET
    }
}
