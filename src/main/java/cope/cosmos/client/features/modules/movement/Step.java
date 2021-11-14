package cope.cosmos.client.features.modules.movement;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import net.minecraft.network.play.client.CPacketPlayer;

import java.util.HashMap;
import java.util.Map;

public class Step extends Module {
    private static final double[][] COLLISIONS = new double[][] {
            new double[] { 2.6, 2.4 },
            new double[] { 2.1, 1.9 },
            new double[] { 1.6, 1.4 },
            new double[] { 1.0, 0.6 }
    };

    public static Setting<Mode> mode = new Setting<>("Mode", "How to step up blocks", Mode.NCP);
    public static Setting<Double> height = new Setting<>("Height", "The height to step up blocks", 1.0, 1.0, 2.5, 1);

    private final Map<Double, double[]> ncpStepHeights = new HashMap<>();

    public Step() {
        super("Step", Category.MOVEMENT, "Steps up blocks faster", () -> Setting.formatEnum(mode.getValue()));

        ncpStepHeights.put(1.0, new double[] { 0.42, 0.753 });
        ncpStepHeights.put(1.5, new double[] { 0.42, 0.75, 1.0, 1.16, 1.23, 1.2 });
        ncpStepHeights.put(2.0, new double[] { 0.42, 0.78, 0.63, 0.51, 0.9, 1.21, 1.45, 1.43 });
        ncpStepHeights.put(2.5, new double[] { 0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869, 2.019, 1.907 });
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (mode.getValue() == Mode.Vanilla) {
            mc.player.stepHeight = 0.5f;
        }
    }

    @Override
    public void onUpdate() {
        if (mode.getValue() == Mode.NCP) {
            if (!mc.player.onGround) {
                return;
            }

            double stepHeight = 0.0; // the amount of blocks we have to step up
            for (double[] collisions : COLLISIONS) {
                if (isBoundingBoxEmpty(mc.player.motionX, collisions[0], mc.player.motionZ) && !isBoundingBoxEmpty(mc.player.motionX, collisions[1], mc.player.motionZ)) {
                    stepHeight = Math.round(collisions[0] * 2.0) / 2.0; // round to nearest half
                }
            }

            // @todo: maybe use NCP lag compensation against itself? or find a way to do 1.5-2.5 step
            if (stepHeight != 0.0 && height.getValue() >= stepHeight) { // if we have blocks to step up and we're within our step height range
                double[] ncpHeights = ncpStepHeights.getOrDefault(stepHeight, null); // get our step heights from the map assigned to the block height
                if (ncpHeights == null || ncpHeights.length == 0) {
                    return;
                }

                for (double height : ncpHeights) {
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + height, mc.player.posZ, mc.player.onGround));
                }

                // since we're ony sending packets, we're desynced with the server in our position. let's fix that by setting our clientside position
                mc.player.setPosition(mc.player.posX, mc.player.posY + stepHeight, mc.player.posZ);
            }
        } else {
            switch (mode.getValue()) {
                case Vanilla: {
                    // simple, vanilla stepheight.
                    mc.player.stepHeight = height.getValue().floatValue();
                    break;
                }

                case Spider: {
                    // honestly the anticheat has to be complete dog for this to work, but just in case
                    mc.player.motionY = 0.239;
                    break;
                }
            }
        }
    }

    private boolean isBoundingBoxEmpty(double x, double y, double z) {
        return mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(x, y, z)).isEmpty();
    }

    public enum Mode {
        NCP, Vanilla, Spider
    }
}
