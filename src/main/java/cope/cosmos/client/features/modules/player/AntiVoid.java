package cope.cosmos.client.features.modules.player;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.utility.world.TeleportUtil;

@SuppressWarnings("unused")
public class AntiVoid extends Module {
    public static AntiVoid INSTANCE;

    public AntiVoid() {
        super("AntiVoid", Category.PLAYER, "Stops you from falling into the void");
        INSTANCE = this;
    }

    public static Setting<Mode> mode = new Setting<>("Mode", "How to stop you from falling into the void", Mode.SUSPEND);
    public static Setting<Double> glide = new Setting<>(() -> mode.getValue().equals(Mode.GLIDE), "Glide", "The value to divide your vertical motion by", 1.0, 5.0, 10.0, 1);
    public static Setting<Double> floatUp = new Setting<>(() -> mode.getValue().equals(Mode.FLOAT), "Float", "What to set your vertical motion to", 0.1, 0.5, 5.0, 0);

    @Override
    public void onUpdate() {
        if (mc.player.posY <= 0) {
            switch (mode.getValue()) {
                case SUSPEND:
                    mc.player.motionY = 0;
                    break;
                case GLIDE:
                    mc.player.motionY /= glide.getValue();
                    break;
                case RUBBERBAND:
                    TeleportUtil.teleportPlayerKeepMotion(mc.player.posX, mc.player.posY + 4, mc.player.posZ);
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
