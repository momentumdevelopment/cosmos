package cope.cosmos.client.features.modules.movement;

import cope.cosmos.client.events.StepEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.client.StringFormatter;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Step extends Module {

    static final double[] one = {0.42, 0.753};
    static final double[] oneF = {0.42, 0.75, 1.0, 1.16, 1.23, 1.2};
    static final double[] two = {0.42, 0.78, 0.63, 0.51, 0.9, 1.21, 1.45, 1.43};
    static final double[] twoF = {0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869, 2.019, 1.907};
    public static Step INSTANCE;

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.NORMAL).setDescription("How to step up blocks");
    public static Setting<Double> height = new Setting<>("Height", 1.0, 1.0, 2.5, 1).setDescription("The maximum height to step up blocks");
    public static Setting<Boolean> timer = new Setting<>("Timer", false).setDescription("Use timer to rubberband less on normal mode");

    public Step() {
        super("Step", Category.MOVEMENT, "Allows you to step up blocks", () -> StringFormatter.formatEnum(mode.getValue()));
        INSTANCE = this;
    }

    boolean timering;

    @Override
    public void onDisable() {
        super.onDisable();
        mc.player.stepHeight = 0.5f;
    }

    @Override
    public void onUpdate() {
        mc.player.stepHeight = height.getValue().floatValue();

        if (timering)
            getCosmos().getTickManager().setClientTicksDirect(50);
    }

    @SubscribeEvent
    public void onStepEvent(StepEvent event) {

        if (mode.getValue().equals(Mode.VANILLA))
            return;

        if (!mc.player.onGround) {
            event.setHeight(.6f);
            return;
        }

        double step = event.getBB().minY - mc.player.posY;

        if (step == 1)
            step(one);
        else if (step == 1.5)
            step(oneF);
        else if (step == 2)
            step(two);
        else if (step == 2.5)
            step(twoF);
        else if (step > 0.5) // as not to cancel any vanilla steps such as stairs, paths
            event.setHeight(.6f);

    }

    void step(double[] offsets) {

        if (timer.getValue()) {
            getCosmos().getTickManager().setClientTicksDirect(50 * (offsets.length + 1 ));
            // + 1 to offsets length because of the movement packet vanilla sends at the top of the step
            timering = true;
            // only timer for one tick
        }

        for (double offset : offsets)
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + offset, mc.player.posZ, false));

        // send our NCP offsets

    }

    public enum Mode {
        NORMAL, VANILLA
    }
}
