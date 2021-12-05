package cope.cosmos.utility.world;

import cope.cosmos.utility.IUtility;
import net.minecraft.network.play.client.CPacketPlayer;

public class TeleportUtil implements IUtility {

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