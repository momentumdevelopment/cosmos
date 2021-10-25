package cope.cosmos.client.features.modules.misc;

import cope.cosmos.asm.mixins.accessor.ICPacketPlayer;
import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.Rotation;
import cope.cosmos.util.player.Rotation.Rotate;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("unused")
public class AntiAim extends Module {
    public AntiAim() {
        super("AntiAim", Category.MISC, "Makes you harder to hit");
    }

    public static Setting<Yaw> yaw = new Setting<>("Yaw", "Changes how your yaw is rotated", Yaw.LINEAR);
    public static Setting<Pitch> pitch = new Setting<>("Pitch", "Changes how your pitch is rotated", Pitch.NONE);

    int aimTicks = 0;
    float aimYaw = 0;
    float aimPitch = 0;
    boolean aimToggle = false;

    @Override
    public void onEnable() {
        super.onEnable();
        aimTicks = 0;
    }

    @Override
    public void onUpdate() {
        switch (yaw.getValue()) {
            case LINEAR:
                aimYaw += 5;
                break;
            case REVERSE:
                aimYaw -= 5;
                break;
            case RANDOM:
                aimYaw = ThreadLocalRandom.current().nextInt(0, 360);
                break;
            case TOGGLE:
                aimYaw += ThreadLocalRandom.current().nextInt(-360, 360);
                break;
            case NONE:
                break;
        }

        switch (pitch.getValue()) {
            case TOGGLE:
                if (aimPitch == -90 || aimPitch == 90) {
                    aimToggle = !aimToggle;
                }

                aimPitch = aimPitch + (aimToggle ? 5 : -5);
                break;
            case RANDOM:
                aimPitch = ThreadLocalRandom.current().nextInt(-90, 90);
                break;
            case MINMAX:
                aimPitch = (aimTicks % 2 == 0) ? 90 : -90;
                break;
            case NONE:
                break;
        }

        // make sure pitch isn't above or below the max/min
        aimPitch = MathHelper.clamp(aimPitch, -90, 90);

        // update player model
        Rotation aimRotation = new Rotation(aimYaw, aimPitch);
        aimRotation.updateModelRotations();

        aimTicks++;
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketPlayer) {
            ((ICPacketPlayer) event.getPacket()).setYaw(aimYaw);
            ((ICPacketPlayer) event.getPacket()).setYaw(aimPitch);
        }
    }

    public enum Yaw {
        LINEAR, REVERSE, RANDOM, TOGGLE, NONE
    }

    public enum Pitch {
        TOGGLE, RANDOM, MINMAX, NONE
    }
}
