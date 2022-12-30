

package cope.cosmos.client.manager.managers;

import cope.cosmos.asm.mixins.accessor.ICPacketPlayer;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.motion.movement.MotionUpdateEvent;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.events.render.entity.RenderRotationsEvent;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.util.Wrapper;
import cope.cosmos.util.holder.Rotation;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips, aesthetical
 * @since 07/30/2021
 */
public class RotationManager extends Manager implements Wrapper {

    // the current server rotation
    private final Rotation serverRotation = new Rotation(Float.NaN, Float.NaN);

    // current rotation
    private Rotation rotation = new Rotation(Float.NaN, Float.NaN);

    // rotation stay time
    private long stay = 0;

    public RotationManager() {
        super("RotationManager", "Keeps track of server rotations");
        Cosmos.EVENT_BUS.register(this);
    }

    @Override
    public void onTick() {

        // reset after 250 ms
        if (System.currentTimeMillis() - stay >= 250 && rotation.isValid()) {
            rotation = new Rotation(Float.NaN, Float.NaN);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPacketSend(PacketEvent.PacketSendEvent event) {

        // rotation packet
        if (event.getPacket() instanceof CPacketPlayer) {

            // packet
            CPacketPlayer packet = (CPacketPlayer) event.getPacket();

            // check if the packet has rotations
            if (((ICPacketPlayer) packet).isRotating()) {

                // update our server rotation
                serverRotation.setYaw(packet.getYaw(0));
                serverRotation.setPitch(packet.getPitch(0));
            }
        }
    }

    @SubscribeEvent
    public void onMotionUpdate(MotionUpdateEvent event) {
        if (rotation.isValid()) {

            // assign vanilla values
            event.setOnGround(mc.player.onGround);
            event.setX(mc.player.posX);
            event.setY(mc.player.getEntityBoundingBox().minY);
            event.setZ(mc.player.posZ);

            // set the rotation to be our custom value
            event.setYaw(rotation.getYaw());
            event.setPitch(rotation.getPitch());

            // cancel, we'll send our own rotations
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderRotations(RenderRotationsEvent event) {

        // we only want to force rotation rendering if we are currently rotating
        if (rotation.isValid()) {

            // cancel, we'll render our own rotations
            event.setCanceled(true);

            // we should render rotations on the server rotation rather than our client side rotations
            // as the two could not match
            event.setYaw(serverRotation.getYaw());
            event.setPitch(serverRotation.getPitch());
        }
    }

    /**
     * Queues a rotation to be sent on the next tick
     * @param in The rotation to be sent on the next tick
     */
    public void setRotation(Rotation in) {
        rotation = in;
        stay = System.currentTimeMillis();
    }

    /**
     * Gets the current client rotations
     * @return The current client rotations
     */
    public Rotation getRotation() {
        return rotation;
    }

    /**
     * Gets the current server rotations
     * @return The current server rotations
     */
    public Rotation getServerRotation() {
        return serverRotation;
    }
}