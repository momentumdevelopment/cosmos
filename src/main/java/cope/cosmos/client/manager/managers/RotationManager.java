package cope.cosmos.client.manager.managers;

import cope.cosmos.asm.mixins.accessor.ICPacketPlayer;
import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.events.RenderLivingEntityEvent;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.util.Wrapper;
import cope.cosmos.util.player.Rotation;
import cope.cosmos.util.player.Rotation.Rotate;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@SuppressWarnings("unused")
public class RotationManager extends Manager implements Wrapper {
    public RotationManager() {
        super("RotationManager", "Keeps track of server rotations", 11);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private Rotation serverRotation = new Rotation(0, 0, Rotate.NONE);

    private float headPitch = -1;

    public void onUpdate() {
        if (nullCheck()) {
            headPitch = -1;
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
      if (event.getPacket() instanceof CPacketPlayer) {
          if (((ICPacketPlayer) event.getPacket()).isRotating()) {
              serverRotation = new Rotation(((CPacketPlayer) event.getPacket()).getYaw(0), ((CPacketPlayer) event.getPacket()).getPitch(0), Rotate.NONE);
          }
      }
    }

    @SubscribeEvent
    public void onRenderLivingEntity(RenderLivingEntityEvent event) {
        if (event.getEntityLivingBase().equals(mc.player)) {
            event.setCanceled(true);
            event.getModelBase().render(event.getEntityLivingBase(), event.getLimbSwing(), event.getLimbSwingAmount(), event.getAgeInTicks(), event.getNetHeadYaw(), headPitch == -1 ? event.getHeadPitch() : headPitch, event.getScaleFactor());
        }
    }

    public void setHeadPitch(float in) {
        headPitch = in;
    }

    public Rotation getServerRotation() {
        return serverRotation;
    }
}
