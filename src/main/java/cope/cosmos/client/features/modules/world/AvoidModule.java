package cope.cosmos.client.features.modules.world;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.events.motion.collision.CollisionBoundingBoxEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.exploits.PacketFlightModule;
import cope.cosmos.client.features.setting.Setting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockFire;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author aesthetical, linustouchtips
 * @since 11/21/2021
 */
public class AvoidModule extends Module {
    public static AvoidModule INSTANCE;

    public AvoidModule() {
        super("Avoid", new String[] {"AntiVoid", "NoVoid", "Static"}, Category.WORLD, "Avoids certain blocks");
        INSTANCE = this;
    }

    // **************************** general ****************************

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.SOLID)
            .setDescription("How to avoid objects");

    public static Setting<Boolean> fire = new Setting<>("Fire", true)
            .setDescription("Prevents you from walking into fire");

    public static Setting<Boolean> cactus = new Setting<>("Cactus", true)
            .setAlias("Cacti")
            .setDescription("Prevents you from walking into cacti");

    public static Setting<Boolean> unloaded = new Setting<>("Unloaded", true)
            .setDescription("Prevents you from entering unloaded chunks");

    public static Setting<Boolean> voids = new Setting<>("Void", true)
            .setDescription("Prevents you from falling into the void");

    @Override
    public void onTick() {

        // can't void if spectator or if packetfly is on
        if (!mc.player.isSpectator() && !PacketFlightModule.INSTANCE.isEnabled()) {

            // void
            if (voids.getValue() && mc.player.posY < 1) {

                // notify the player that we are attempting to get out of the void
                getCosmos().getChatManager().sendClientMessage("[Avoid] " + ChatFormatting.RED + "Attempting to get player out of void!", -6980085);

                // solidify blocks
                if (mode.getValue().equals(Mode.SOLID)) {

                    // stop all vertical motion
                    mc.player.motionY = 0;
                }
            }
        }
    }

    @SubscribeEvent
    public void onBoundingBoxCollision(CollisionBoundingBoxEvent event) {
        if (nullCheck()) {

            // check if we are the ones colliding
            if (event.getEntity() != null && event.getEntity().equals(mc.player)) {

                // can't void if spectator or if packetfly is on
                if (!mc.player.isSpectator() && !PacketFlightModule.INSTANCE.isEnabled()) {

                    // full box
                    if (mode.getValue().equals(Mode.SOLID)) {

                        // check collision side
                        if (event.getPosition().getY() == mc.player.getEntityBoundingBox().minY) {

                            // check if we need to avoid
                            if (fire.getValue() && event.getBlock() instanceof BlockFire || cactus.getValue() && event.getBlock() instanceof BlockCactus || unloaded.getValue() && !mc.world.isBlockLoaded(event.getPosition(), false)) {

                                // full box
                                AxisAlignedBB fullCollisionBox = Block.FULL_BLOCK_AABB.offset(event.getPosition());

                                // add the full box to collision list
                                if (event.getCollisionBox().intersects(fullCollisionBox)) {
                                    event.getCollisionList().add(fullCollisionBox);
                                    event.setCanceled(true);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public enum Mode {

        /**
         * Makes the void block completely solid
         */
        SOLID,
    }
}
