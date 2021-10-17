package cope.cosmos.util.player;

import cope.cosmos.asm.mixins.accessor.IEntityPlayerSP;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.MotionUpdateEvent;
import cope.cosmos.util.Wrapper;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationUtil implements Wrapper {

    public static void updateRotationPackets(MotionUpdateEvent event) {
        if (mc.player.isSprinting() != ((IEntityPlayerSP) mc.player).getServerSprintState()) {
            if (mc.player.isSprinting())
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
            else
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));

            ((IEntityPlayerSP) mc.player).setServerSprintState(mc.player.isSprinting());
        }

        if (mc.player.isSneaking() != ((IEntityPlayerSP) mc.player).getServerSneakState()) {
            if (mc.player.isSneaking())
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            else
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));

            ((IEntityPlayerSP) mc.player).setServerSneakState(mc.player.isSneaking());
        }

        double updatedPosX = mc.player.posX - ((IEntityPlayerSP) mc.player).getLastReportedPosX();
        double updatedPosY = mc.player.getEntityBoundingBox().minY - ((IEntityPlayerSP) mc.player).getLastReportedPosY();
        double updatedPosZ = mc.player.posZ - ((IEntityPlayerSP) mc.player).getLastReportedPosZ();

        double updatedRotationYaw = event.getYaw() - ((IEntityPlayerSP) mc.player).getLastReportedYaw();
        double updatedRotationPitch = event.getPitch() - ((IEntityPlayerSP) mc.player).getLastReportedPitch();

        int positionUpdateTicks = ((IEntityPlayerSP) mc.player).getPositionUpdateTicks();
        ((IEntityPlayerSP) mc.player).setPositionUpdateTicks(positionUpdateTicks++);

        boolean positionUpdate = updatedPosX * updatedPosX + updatedPosY * updatedPosY + updatedPosZ * updatedPosZ > 9.0E-4 || ((IEntityPlayerSP) mc.player).getPositionUpdateTicks() >= 20;
        boolean rotationUpdate = updatedRotationYaw != 0 || updatedRotationPitch != 0;

        if (mc.player.isRiding()) {
            mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.motionX, -999, mc.player.motionZ, event.getYaw(), event.getPitch(), mc.player.onGround));
            positionUpdate = false;
        }

        else if (positionUpdate && rotationUpdate)
            mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.getEntityBoundingBox().minY, mc.player.posZ, event.getYaw(), event.getPitch(), mc.player.onGround));
        else if (positionUpdate)
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.getEntityBoundingBox().minY, mc.player.posZ, mc.player.onGround));
        else if (rotationUpdate)
            mc.player.connection.sendPacket(new CPacketPlayer.Rotation(event.getYaw(), event.getPitch(), mc.player.onGround));
        else if (((IEntityPlayerSP) mc.player).getPreviousOnGround() != mc.player.onGround)
            mc.player.connection.sendPacket(new CPacketPlayer(mc.player.onGround));

        if (positionUpdate) {
            ((IEntityPlayerSP) mc.player).setLastReportedPosX(mc.player.posX);
            ((IEntityPlayerSP) mc.player).setLastReportedPosY(mc.player.getEntityBoundingBox().minY);
            ((IEntityPlayerSP) mc.player).setLastReportedPosZ(mc.player.posZ);
            ((IEntityPlayerSP) mc.player).setPositionUpdateTicks(0);
        }

        if (rotationUpdate) {
            ((IEntityPlayerSP) mc.player).setLastReportedYaw(event.getYaw());
            ((IEntityPlayerSP) mc.player).setLastReportedPitch(event.getPitch());
        }

        ((IEntityPlayerSP) mc.player).setPreviousOnGround(mc.player.onGround);
    }
}
