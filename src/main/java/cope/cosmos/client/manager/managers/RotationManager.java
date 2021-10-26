package cope.cosmos.client.manager.managers;

import cope.cosmos.asm.mixins.accessor.ICPacketPlayer;
import cope.cosmos.client.events.MotionUpdateEvent;
import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.util.Wrapper;
import cope.cosmos.util.player.Rotation;
import cope.cosmos.util.player.Rotation.Rotate;
import cope.cosmos.util.system.Timer;
import cope.cosmos.util.system.Timer.*;
import cope.cosmos.util.world.AngleUtil;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@SuppressWarnings("unused")
public class RotationManager extends Manager implements Wrapper {

    private final Rotation.MutableRotation clientRotation = new Rotation.MutableRotation(Float.NaN, Float.NaN);
    private final Rotation.MutableRotation serverRotation = new Rotation.MutableRotation(Float.NaN, Float.NaN);

    private final Timer resetTimer = new Timer();

    public RotationManager() {
        super("RotationManager", "Keeps track of server rotations", 11);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onMotionUpdate(MotionUpdateEvent event) {
        if (!event.isCanceled() && clientRotation.isValid() && resetTimer.passed(350, Format.SYSTEM)) {
            restoreRotations();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketPlayer) {
            CPacketPlayer packet = (CPacketPlayer) event.getPacket();
            if (((ICPacketPlayer) packet).isRotating()) {
                if (clientRotation.isValid()) {
                    serverRotation.setYaw(clientRotation.getYaw());
                    serverRotation.setPitch(clientRotation.getPitch());

                    // update the packet with our custom values
                    ((ICPacketPlayer) packet).setYaw(clientRotation.getYaw());
                    ((ICPacketPlayer) packet).setPitch(clientRotation.getPitch());

                }

                else {
                    serverRotation.setYaw(packet.getYaw(0));
                    serverRotation.setPitch(packet.getPitch(0));
                }
            }
        }
    }

    public void setRotations(Entity entity, Rotate rotate) {
        float[] rotations = AngleUtil.calculateAngles(entity);
        setRotations(rotations[0], rotations[1], rotate);
    }

    public void setRotations(BlockPos position, Rotate rotate) {
        float[] rotations = AngleUtil.calculateAngles(position);
        setRotations(rotations[0], rotations[1], rotate);
    }

    public void setRotations(float yaw, float pitch, Rotate rotate) {
        clientRotation.setYaw(yaw);
        clientRotation.setPitch(pitch);
        clientRotation.setRotation(rotate);
        
        resetTimer.reset();

        if (rotate.equals(Rotate.CLIENT)) {
            mc.player.rotationYaw = clientRotation.getYaw();
            mc.player.rotationPitch = clientRotation.getPitch();
        }
    }

    private void restoreRotations() {
        Rotate previousRotation = clientRotation.getRotation();

        clientRotation.setYaw(mc.player.rotationYaw);
        clientRotation.setPitch(mc.player.rotationPitch);

        if (previousRotation.equals(Rotate.CLIENT)) {
            mc.player.rotationYaw = clientRotation.getYaw();
            mc.player.rotationPitch = clientRotation.getPitch();
        }
    }

    public Rotation.MutableRotation getClientRotation() {
        return clientRotation;
    }

    public Rotation.MutableRotation getServerRotation() {
        return serverRotation;
    }
}
