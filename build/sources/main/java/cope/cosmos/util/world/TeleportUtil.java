package cope.cosmos.util.world;

import cope.cosmos.util.Wrapper;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.Vec3d;

public class TeleportUtil implements Wrapper {

    public static void teleportPlayer(double x, double y, double z) {
        mc.player.setVelocity(0, 0, 0);
        mc.player.setPosition(x, y, z);
        mc.player.connection.sendPacket(new CPacketPlayer.Position(x, y, z, true));
    }

    public static void teleportPlayerNoPacket(double x, double y, double z) {
        mc.player.setVelocity(0, 0, 0);
        mc.player.setPosition(x, y, z);
    }

    public static void teleportPlayerKeepMotion(double x, double y, double z) {
        mc.player.setPosition(x, y, z);
    }
}