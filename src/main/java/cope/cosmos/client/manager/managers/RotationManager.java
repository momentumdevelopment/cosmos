package cope.cosmos.client.manager.managers;

import cope.cosmos.asm.mixins.accessor.ICPacketPlayer;
import cope.cosmos.client.events.MotionUpdateEvent;
import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.events.RenderLivingEntityEvent;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.util.Wrapper;
import cope.cosmos.util.player.Rotation;
import cope.cosmos.util.player.Rotation.Rotate;
import cope.cosmos.util.system.Timer;
import cope.cosmos.util.world.AngleUtil;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@SuppressWarnings("unused")
public class RotationManager extends Manager implements Wrapper {
    private Rotation rotation = new Rotation(-1.0f, -1.0f, Rotate.NONE);
    private final Timer timer = new Timer();

    public RotationManager() {
        super("RotationManager", "Keeps track of server rotations", 11);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onMotionUpdate(MotionUpdateEvent event) {
        if (!event.isCanceled() && !this.hasNoRotations() && this.timer.passed(350L, Timer.Format.SYSTEM)) {
            this.reset();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketPlayer) {
            CPacketPlayer packet = (CPacketPlayer) event.getPacket();
            if (((ICPacketPlayer) packet).isRotating() && !this.hasNoRotations()) {
                ((ICPacketPlayer) packet).setYaw(this.rotation.getYaw());
                ((ICPacketPlayer) packet).setPitch(this.rotation.getPitch());
            }
        }
    }

    @SubscribeEvent
    public void onRenderLivingEntity(RenderLivingEntityEvent event) {
        if (event.getEntityLivingBase().equals(mc.player) && !this.hasNoRotations()) {
            // turn entire body
            mc.player.renderYawOffset = this.rotation.getYaw();
            mc.player.rotationYawHead = this.rotation.getYaw();

            event.setCanceled(true);
            event.getModelBase().render(
                    event.getEntityLivingBase(),
                    event.getLimbSwing(),
                    event.getLimbSwingAmount(),
                    event.getAgeInTicks(),
                    event.getNetHeadYaw(), // when setting this to the current rotations, it looks incorrect although the rotations are just fine.
                    this.rotation.getPitch() == -1.0f ?
                            event.getHeadPitch() :
                            this.rotation.getPitch(),
                    event.getScaleFactor()
            );
        }
    }

    public void rotate(Entity entity, boolean center, Rotate rotate) {
        float[] rotations = center ? AngleUtil.calculateCenter(entity) : AngleUtil.calculateAngles(entity);
        this.rotate(rotations[0], rotations[1], rotate);
    }

    public void rotate(BlockPos pos, boolean center, Rotate rotate) {
        float[] rotations = center ? AngleUtil.calculateCenter(pos) : AngleUtil.calculateAngles(pos);
        this.rotate(rotations[0], rotations[1], rotate);
    }

    public void rotate(float yaw, float pitch, Rotate rotate) {
        this.rotation = new Rotation(yaw, pitch, rotate);
        this.timer.reset();

        if (rotate == Rotate.CLIENT) {
            this.updateClientSideRotations();
        }
    }

    private void reset() {
        Rotate previous = this.rotation.getRotation();
        this.rotation = new Rotation(mc.player.rotationYaw, mc.player.rotationPitch, Rotate.NONE);
        if (previous == Rotate.CLIENT) {
            this.updateClientSideRotations();
        }
    }

    private void updateClientSideRotations() {
        mc.player.rotationYaw = this.rotation.getYaw();
        mc.player.rotationPitch = this.rotation.getPitch();
    }

    public boolean hasNoRotations() {
        return this.rotation.getYaw() == -1.0f && this.rotation.getPitch() == -1.0f;
    }
}
