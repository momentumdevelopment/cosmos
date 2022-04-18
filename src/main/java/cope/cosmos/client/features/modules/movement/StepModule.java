package cope.cosmos.client.features.modules.movement;

import cope.cosmos.client.events.motion.movement.StepEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.string.StringFormatter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.entity.passive.EntityMule;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;
import java.util.List;

/**
 * @author Doogie13, linustouchtips
 * @since 12/27/2021
 */
public class StepModule extends Module {
    public static StepModule INSTANCE;

    public StepModule() {
        super("Step", Category.MOVEMENT, "Allows you to step up blocks", () -> StringFormatter.formatEnum(mode.getValue()));
        INSTANCE = this;
    }

    // **************************** general ****************************

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.NORMAL)
            .setDescription("Mode for how to step up blocks");

    public static Setting<Double> height = new Setting<>("Height", 1.0, 1.0, 2.5, 1)
            .setDescription("The maximum height to step up blocks");

    public static Setting<Boolean> useTimer = new Setting<>("Timer", true)
            .setDescription("Uses timer to slow down packets")
            .setVisible(() -> mode.getValue().equals(Mode.NORMAL));

    public static Setting<Boolean> entityStep = new Setting<>("EntityStep", false)
            .setDescription("Allows you to step up blocks while riding entities");

    // timer enabled??
    private boolean timer;

    // riding entity (player, sometimes null)
    private Entity entityRiding;

    @Override
    public void onDisable() {
        super.onDisable();

        // reset our step heights
        mc.player.stepHeight = 0.5F;

        if (entityRiding != null) {
            if (entityRiding instanceof EntityHorse || entityRiding instanceof EntityLlama || entityRiding instanceof EntityMule || entityRiding instanceof EntityPig && entityRiding.isBeingRidden() && ((EntityPig) entityRiding).canBeSteered()) {
                entityRiding.stepHeight = 1;
            }
            
            else {
                entityRiding.stepHeight = 0.5F;
            }
        }
    }

    @Override
    public void onUpdate() {

        // update our player's step height
        mc.player.stepHeight = height.getValue().floatValue();

        if (mc.player.isRiding() && mc.player.getRidingEntity() != null) {
            entityRiding = mc.player.getRidingEntity();

            // update our riding entity's step height
            if (entityStep.getValue()) {
                mc.player.getRidingEntity().stepHeight = height.getValue().floatValue();
            }
        }

        // reset our timer if needed
        if (timer) {
            getCosmos().getTickManager().setClientTicks(1);
        }
    }

    @SubscribeEvent
    public void onStep(StepEvent event) {
        if (mode.getValue().equals(Mode.NORMAL)) {

            // don't attempt to step if we are not on the ground
            if (!mc.player.onGround) {
                event.setHeight(0.6F);
            }

            else {

                // current step height
                double stepHeight = event.getAxisAlignedBB().minY - mc.player.posY;

                // calculate the packet offsets
                double[] offsets = getOffset(stepHeight);

                if (offsets.length > 1) {
                    if (useTimer.getValue()) {

                        // add 1 to offsets length because of the movement packet vanilla sends at the top of the step
                        getCosmos().getTickManager().setClientTicks(1 / (offsets.length + 1F));

                        // only slow down timer for one tick
                        timer = true;
                    }

                    // send our NCP offsets
                    for (double offset : offsets) {
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + offset, mc.player.posZ, false));
                    }
                }

                // as not to cancel any vanilla steps such as stairs, paths
                else if (stepHeight > 0.5) {
                    event.setHeight(0.6F);
                }
            }
        }
    }

    /**
     * Gets the NCP packet offsets for a given step height
     * @param height The step height
     * @return The NCP packet offsets for the given step height
     */
    public double[] getOffset(double height) {

        // list of step heights
        List<StepHeight> stepHeights = Arrays.asList(
                new StepHeight(1, 0.42, 0.753),
                new StepHeight(1.5, 0.42, 0.75, 1.0, 1.16, 1.23, 1.2),
                new StepHeight(2, 0.42, 0.78, 0.63, 0.51, 0.9, 1.21, 1.45, 1.43),
                new StepHeight(2.5, 0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869, 2.019, 1.907)
        );

        // find the offsets for the step height
        return stepHeights.stream()
                .filter(stepHeight -> stepHeight.getHeight() == height)
                .findFirst()
                .orElse(new StepHeight(0, 0))
                .getOffsets();
    }

    public enum Mode {

        /**
         * Sends packets to simulate a jump, bypasses NCP
         */
        NORMAL,

        /**
         * Increases step height, does not send packets
         */
        VANILLA
    }

    public static class StepHeight {

        // step info
        private final double height;
        private final double[] offsets;

        public StepHeight(double height, double... offsets) {
            this.height = height;
            this.offsets = offsets;
        }

        /**
         * Gets the height for the step
         * @return The height for the step
         */
        public double getHeight() {
            return height;
        }

        /**
         * Gets the packet offsets for the step
         * @return The packet offsets for the step
         */
        public double[] getOffsets() {
            return offsets;
        }
    }
}
