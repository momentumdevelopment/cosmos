package cope.cosmos.client.features.modules.movement;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.client.StringFormatter;

public class Jesus extends Module {

    public static Jesus INSTANCE;

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.SOLID).setDescription("How to walk on water");

    public Jesus() {
        super("Jesus", Category.MOVEMENT, "Lets you walk on water as if it were a solid block", () -> StringFormatter.formatEnum(mode.getValue()));
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        if(!nullCheck()) return;

        if(mode.getValue() == Mode.DOLPHIN) {
            if(mc.player.isInWater()) {
                mc.player.motionY = 0.001;
                mc.player.motionX *= 1.2;
                mc.player.motionZ *= 1.2;
                if (mc.player.collidedHorizontally) {
                    mc.player.onGround = true;
                }
            }
        }
    }

    public enum Mode {
        SOLID, DOLPHIN
    }

}
