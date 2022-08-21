package cope.cosmos.client.features.modules.movement;

import cope.cosmos.client.events.motion.movement.StepEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.string.StringFormatter;
import cope.cosmos.util.math.Timer;
import cope.cosmos.util.math.Timer.Format;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.entity.passive.EntityMule;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author Doogie13, linustouchtips, aesthetical
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

    public static Setting<Boolean> strict = new Setting<>("Strict", false)
            .setDescription("Confirms step height")
            .setVisible(() -> mode.getValue().equals(Mode.NORMAL));

    public static Setting<Boolean> entityStep = new Setting<>("EntityStep", false)
            .setDescription("Allows you to step up blocks while riding entities");

    // timer enabled??
    private boolean timer;

    // riding entity (player, sometimes null)
    private Entity entityRiding;

    // step timer
    private final Timer stepTimer = new Timer();

    @Override
    public void onDisable() {
        super.onDisable();

        // reset our step heights
        mc.player.stepHeight = 0.6F;

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

        // reset our timer if needed
        if (timer && mc.player.onGround) {
            getCosmos().getTickManager().setClientTicks(1);
            timer = false;
        }

        if (mc.player.isRiding() && mc.player.getRidingEntity() != null) {
            entityRiding = mc.player.getRidingEntity();

            // update our riding entity's step height
            if (entityStep.getValue()) {
                mc.player.getRidingEntity().stepHeight = height.getValue().floatValue();
            }
        }

        // wait 200 ms between steps to prevent packet spam
        if (stepTimer.passedTime(200, Format.MILLISECONDS)) {

            // update our player's step height
            mc.player.stepHeight = height.getValue().floatValue();
        }

        // prevent step
        else {
            mc.player.stepHeight = 0.5F;
        }
    }

    @SubscribeEvent
    public void onStep(StepEvent event) {

        // step with packets
        if (mode.getValue().equals(Mode.NORMAL)) {

            // current step height
            double stepHeight = event.getAxisAlignedBB().minY - mc.player.posY;

            // do not step if we're on the ground or the step height is greater than our max
            if (stepHeight <= 0 || stepHeight > height.getValue()) {
                return;
            }

            // calculate the packet offsets
            double[] offsets = getOffset(stepHeight);

            // valid step height?
            if (offsets != null && offsets.length > 1) {

                // uses timer to slow down packet speeds so we don't flag for packet spam
                if (useTimer.getValue()) {

                    // add 1 to offsets length because of the movement packet vanilla sends at the top of the step
                    getCosmos().getTickManager().setClientTicks(1F / offsets.length);

                    // only slow down timer for one tick
                    timer = true;
                }

                // send our NCP offsets
                for (double offset : offsets) {
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + offset, mc.player.posZ, false));
                }
            }

            stepTimer.resetTime();
        }
    }

    /**
     * Gets the NCP packet offsets for a given step height
     * @param height The step height
     * @return The NCP packet offsets for the given step height
     */
    public double[] getOffset(double height) {

        // confirm step height (helps bypass on NCP Updated)
        // enchantment tables, 0.75 block offset
        if (height == 0.75) {

            if (strict.getValue()) {
                return new double[] {
                        0.42,
                        0.753,
                        0.75
                };
            }

            else {
                return new double[] {
                        0.42,
                        0.753
                };
            }
        }

        // end portal frames, 0.8125 block offset
        else if (height == 0.8125) {

            if (strict.getValue()) {
                return new double[] {
                        0.39,
                        0.7,
                        0.8125
                };
            }

            else {
                return new double[] {
                        0.39,
                        0.7
                };
            }
        }

        // chests, 0.875 block offset
        else if (height == 0.875) {

            if (strict.getValue()) {
                return new double[] {
                        0.39,
                        0.7,
                        0.875
                };
            }

            else {
                return new double[] {
                        0.39,
                        0.7
                };
            }
        }

        // 1 block offset -> LITERALLY IMPOSSIBLE TO PATCH BECAUSE ITS JUST THE SAME PACKETS AS A JUMP
        else if (height == 1) {

            if (strict.getValue()) {
                return new double[] {
                        0.42,
                        0.753,
                        1
                };
            }

            else {
                return new double[] {
                        0.42,
                        0.753
                };
            }
        }

        // 1.5 block offset
        else if (height == 1.5) {
            return new double[] {
                    0.42,
                    0.75,
                    1.0,
                    1.16,
                    1.23,
                    1.2
            };
        }

        // 2 block offset
        else if (height == 2) {
            return new double[] {
                    0.42,
                    0.78,
                    0.63,
                    0.51,
                    0.9,
                    1.21,
                    1.45,
                    1.43
            };
        }

        // 2.5 block offset
        else if (height == 2.5) {
            return new double[] {
                    0.425,
                    0.821,
                    0.699,
                    0.599,
                    1.022,
                    1.372,
                    1.652,
                    1.869,
                    2.019,
                    1.907
            };
        }

        return null;
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
}