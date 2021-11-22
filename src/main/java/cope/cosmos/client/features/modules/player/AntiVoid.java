package cope.cosmos.client.features.modules.player;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import net.minecraft.network.play.client.CPacketPlayer;

public class AntiVoid extends Module {
    public static AntiVoid INSTANCE;

    public AntiVoid() {
        super("AntiVoid", Category.PLAYER, "Stops you from falling into the void");
        INSTANCE = this;
    }

    public static Setting<Mode> mode = new Setting<>("Mode", "How to stop you from falling into the void", Mode.SUSPEND);
    public static Setting<Double> glide = new Setting<>("Glide", "The value to divide your vertical motion by", 1.0, 5.0, 10.0, 1);
    public static Setting<Double> floatUp = new Setting<>("Float", "What to set your vertical motion to", 0.1, 0.5, 5.0, 0);

    @Override
    public void onUpdate() {
        if (mc.player.posY <= 0.0) {
            switch (mode.getValue()) {
                case SUSPEND:
                    mc.player.motionY = 0.0;
                    break;
                case GLIDE:
                    mc.player.motionY /= glide.getValue();
                    break;
                case RUBBERBAND:
                    mc.player.setPosition(mc.player.posX, mc.player.posY + 4.0, mc.player.posZ);
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 4.0, mc.player.posZ, false));
                    break;
                case FLOAT:
                    mc.player.motionY = floatUp.getValue();
                    break;
            }
        }
    }

    public enum Mode {
        SUSPEND, GLIDE, RUBBERBAND, FLOAT
    }
}
