package cope.cosmos.client.features.modules.movement;

import cope.cosmos.asm.mixins.accessor.IEntity;
import cope.cosmos.asm.mixins.accessor.INetHandlerPlayClient;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.MotionUtil;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.network.play.server.SPacketSetPassengers;
import net.minecraft.util.EnumHand;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips
 * @since 02/25/2022
 */
public class EntitySpeedModule extends Module {
    public static EntitySpeedModule INSTANCE;

    public EntitySpeedModule() {
        super("EntitySpeed", Category.MOVEMENT, "Allows you to move faster while riding entities");
        INSTANCE = this;
    }

    // **************************** speeds ****************************

    public static Setting<Double> speed = new Setting<>("Speed", 0.0, 1.0, 3.0, 2)
            .setDescription("Speed when moving");

    // **************************** anticheat ****************************

    public static Setting<Boolean> strict = new Setting<>("Strict", false)
            .setDescription("Remounts entities constantly");

    @Override
    public void onUpdate() {

        // make sure we are riding an entity
        if (mc.player.isRiding() && mc.player.getRidingEntity() != null) {

            // keep yaw consistent
            mc.player.getRidingEntity().rotationYaw = mc.player.rotationYaw;

            // lololol
            if (mc.player.getRidingEntity() instanceof EntityLlama) {
                ((EntityLlama) mc.player.getRidingEntity()).rotationYawHead = mc.player.rotationYawHead;
            }

            // make sure the entity is not in a liquid
            if (mc.player.getRidingEntity().isInWater() || mc.player.getRidingEntity().isInLava()) {
                return;
            }

            // make sure the entity is not in a web
            if (((IEntity) mc.player.getRidingEntity()).getInWeb()) {
                return;
            }

            // make sure the entity can have speed applied
            if (mc.player.getRidingEntity().fallDistance > 2) {
                return;
            }

            // the current movement input values of the user
            float forward = mc.player.movementInput.moveForward;
            float strafe = mc.player.movementInput.moveStrafe;
            float yaw = mc.player.rotationYaw;

            // remount
            if (strict.getValue()) {

                // send remount packets
                if (!mc.gameSettings.keyBindSneak.isKeyDown()) {
                    mc.player.connection.sendPacket(new CPacketUseEntity(mc.player.getRidingEntity(), EnumHand.MAIN_HAND));
                }
            }

            // prevent entity from moving into unloaded chunks
            if (mc.world.getChunkFromChunkCoords((int) (mc.player.getRidingEntity().posX + mc.player.getRidingEntity().motionX) >> 4, (int) (mc.player.getRidingEntity().posZ + mc.player.getRidingEntity().motionX) >> 4) instanceof EmptyChunk) {
                mc.player.getRidingEntity().motionX = 0;
                mc.player.getRidingEntity().motionZ = 0;
            }

            else {

                // our facing values, according to movement not rotations
                double cos = Math.cos(Math.toRadians(yaw + 90));
                double sin = Math.sin(Math.toRadians(yaw + 90));

                // if we're not inputting any movements, then we shouldn't be adding any motion
                if (!MotionUtil.isMoving()) {
                    mc.player.getRidingEntity().motionX = 0;
                    mc.player.getRidingEntity().motionZ = 0;
                }

                else {

                    // update the movements
                    mc.player.getRidingEntity().motionX = (forward * speed.getValue() * cos) + (strafe * speed.getValue() * sin);
                    mc.player.getRidingEntity().motionZ = (forward * speed.getValue() * sin) - (strafe * speed.getValue() * cos);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {

        if (nullCheck()) {

            // if the client is not done loading the surrounding terrain, DO NOT CANCEL MOVEMENT PACKETS!!!!
            if (!((INetHandlerPlayClient) mc.player.connection).isDoneLoadingTerrain()) {
                return;
            }

            // make sure we are riding an entity
            if (mc.player.isRiding() && mc.player.getRidingEntity() != null) {

                // make sure we are not dismounting
                if (!mc.gameSettings.keyBindSneak.isKeyDown()) {

                    // packet for server passenger updates
                    if (event.getPacket() instanceof SPacketSetPassengers) {

                        // cancel server passenger updates
                        if (strict.getValue()) {
                            event.setCanceled(true);
                        }
                    }

                    // packet for rubberbands
                    if (event.getPacket() instanceof SPacketPlayerPosLook) {

                        // cancel rubberbands
                        if (strict.getValue()) {
                            event.setCanceled(true);
                        }
                    }
                }
            }
        }
    }
}