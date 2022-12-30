package cope.cosmos.client.features.modules.combat;

import cope.cosmos.asm.mixins.accessor.ICPacketPlayer;
import cope.cosmos.asm.mixins.accessor.IEntity;
import cope.cosmos.asm.mixins.accessor.INetworkManager;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.entity.EntityUtil;
import cope.cosmos.util.math.Timer;
import cope.cosmos.util.math.Timer.Format;
import cope.cosmos.util.player.InventoryUtil;
import cope.cosmos.util.player.PlayerUtil;
import cope.cosmos.util.string.StringFormatter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.client.CPacketUseEntity.Action;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips
 * @since 12/04/2021
 */
public class CriticalsModule extends Module {
    public static CriticalsModule INSTANCE;

    public CriticalsModule() {
        super("Criticals", Category.COMBAT, "Ensures all hits are criticals", () -> StringFormatter.formatEnum(mode.getValue()));
        INSTANCE = this;
    }

    // **************************** anticheat ****************************

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.PACKET)
            .setDescription("Mode for attempting criticals");

    public static Setting<Double> motion = new Setting<>("Motion", 0.0D, 0.42D, 1.0D, 2)
            .setDescription("Vertical motion")
            .setVisible(() -> mode.getValue().equals(Mode.MOTION));

    // **************************** general ****************************

    public static Setting<Double> delay = new Setting<>("Delay", 0.0D, 200.0D, 2000.0D, 0)
            .setDescription("Delay between attacks to attempt criticals");

    // criticals timer
    private static final Timer criticalTimer = new Timer();

    // packet info
    private CPacketUseEntity resendAttackPacket;
    private CPacketAnimation resendAnimationPacket;

    // critical entity
    private Entity criticalEntity;

    @Override
    public void onUpdate() {

        // resend our attack packets
        if (resendAttackPacket != null) {
            mc.player.connection.sendPacket(resendAttackPacket);
            resendAttackPacket = null;

            // resend our animation packets
            if (resendAnimationPacket != null) {
                mc.player.connection.sendPacket(resendAnimationPacket);
                resendAnimationPacket = null;
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {

        // packet for attacks
        if (event.getPacket() instanceof CPacketUseEntity && ((CPacketUseEntity) event.getPacket()).getAction().equals(Action.ATTACK)) {

            // entity we attacked, if there was one
            Entity attackEntity = ((CPacketUseEntity) event.getPacket()).getEntityFromWorld(mc.world);

            // pause in liquid, ladders, blindness, webs, and air, since strict anticheats will flag as irregular movement
            if (PlayerUtil.isInLiquid() || mc.player.isRiding() || mc.player.isPotionActive(MobEffects.BLINDNESS) || mc.player.isOnLadder() || !mc.player.onGround || ((IEntity) mc.player).getInWeb()) {
                return;
            }

            // pause if attacking a crystal, helps compatability with AutoCrystal
            if (attackEntity instanceof EntityEnderCrystal) {
                return;
            }

            // critical hits on 32k's are insignificant
            if (InventoryUtil.getHighestEnchantLevel() >= 1000) {
                return;
            }

            // make sure the attacked entity exists
            if (attackEntity != null && attackEntity.isEntityAlive()) {

                // destroying a vehicle takes 5 hits -> regardless of damage
                if (EntityUtil.isVehicleMob(attackEntity)) {

                    // attack 5 times
                    if (mc.getConnection() != null) {
                        for (int i = 0; i < 5; i++) {
                            ((INetworkManager) mc.getConnection().getNetworkManager()).hookDispatchPacket(new CPacketUseEntity(attackEntity), null);
                            ((INetworkManager) mc.getConnection().getNetworkManager()).hookDispatchPacket(new CPacketAnimation(((CPacketUseEntity) event.getPacket()).getHand()), null);
                        }
                    }
                }

                else {
                    // send position packets after attack if we didn't modify them
                    if (!mode.getValue().equals(Mode.VANILLA) && !mode.getValue().equals(Mode.VANILLA_STRICT)) {

                        // attempt motion criticals
                        if (mode.getValue().equals(Mode.MOTION)) {

                            // jump
                            mc.player.motionY = motion.getValue();

                            // cancel the attack, we'll resend it next tick
                            event.setCanceled(true);
                            resendAttackPacket = (CPacketUseEntity) event.getPacket();
                        }

                        // if our timer has cleared the delay, then we are cleared to attempt another critical attack
                        if (criticalTimer.passedTime(delay.getValue().longValue(), Format.MILLISECONDS)) {
                            if (mode.getValue().equals(Mode.PACKET_STRICT)) {

                                // cancel the attack, we'll resend it after packets
                                event.setCanceled(true);
                            }

                            if (mode.getValue().equals(Mode.PACKET) || mode.getValue().equals(Mode.PACKET_STRICT)) {

                                // send packets for each of the offsets
                                for (float offset : mode.getValue().getOffsets()) {

                                    // last packet on strict should confirm player position
                                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.getEntityBoundingBox().minY + offset, mc.player.posZ, false));
                                }

                                // set our attacked entity
                                criticalEntity = attackEntity;
                            }

                            // resend attack packet
                            if (mode.getValue().equals(Mode.PACKET_STRICT)) {
                                if (mc.getConnection() != null) {
                                    ((INetworkManager) mc.getConnection().getNetworkManager()).hookDispatchPacket(new CPacketUseEntity(attackEntity), null);
                                }
                            }
                        }

                        criticalTimer.resetTime();

                        // add critical effects to the hit
                        mc.player.onCriticalHit(attackEntity);
                    }
                }
            }
        }

        // packet for swing animation
        if (event.getPacket() instanceof CPacketAnimation) {
            if (mode.getValue().equals(Mode.MOTION)) {

                // cancel our swing animation, we'll resend it next tick
                event.setCanceled(true);
                resendAnimationPacket = (CPacketAnimation) event.getPacket();
            }
        }

        // packet for player updates
        if (event.getPacket() instanceof CPacketPlayer) {

            // check if packet is updating motion
            if (((ICPacketPlayer) event.getPacket()).isMoving()) {

                // we have attacked an entity
                if (criticalEntity != null) {

                    // make sure entity is hurt
                    if (criticalEntity.hurtResistantTime <= 16) {
                        criticalEntity = null;
                        return;
                    }

                    event.setCanceled(true);

                    // modify packets
                    if (mode.getValue().equals(Mode.VANILLA)) {

                        // all vanilla packets are off ground
                        ((ICPacketPlayer) event.getPacket()).setOnGround(false);

                        // modify packets based on entity hurt time
                        switch (criticalEntity.hurtResistantTime) {
                            case 20:
                                ((ICPacketPlayer) event.getPacket()).setY(mc.player.getEntityBoundingBox().minY + 0.5F);
                                break;
                            case 19:
                            case 17:
                                ((ICPacketPlayer) event.getPacket()).setY(mc.player.getEntityBoundingBox().minY);
                                break;
                            case 18:
                                ((ICPacketPlayer) event.getPacket()).setY(mc.player.getEntityBoundingBox().minY + 0.3F);
                                break;
                        }
                    }

                    // strict has dynamic onGround packets
                    else if (mode.getValue().equals(Mode.VANILLA_STRICT)) {

                        // all vanilla packets are off ground
                        ((ICPacketPlayer) event.getPacket()).setOnGround(false);

                        // modify packets based on entity hurt time
                        switch (criticalEntity.hurtResistantTime) {
                            case 19:
                                ((ICPacketPlayer) event.getPacket()).setY(mc.player.getEntityBoundingBox().minY + 0.11F);
                                break;
                            case 18:
                                ((ICPacketPlayer) event.getPacket()).setY(mc.player.getEntityBoundingBox().minY + 0.1100013579F);
                                break;
                            case 17:
                                ((ICPacketPlayer) event.getPacket()).setY(mc.player.getEntityBoundingBox().minY + 0.0000013579F);
                                break;
                        }
                    }
                }
            }
        }
    }

    public enum Mode {

        /**
         * Attempts changing hit to a critical via packets
         */
        PACKET(0.05F, 0, 0.03F, 0),

        /**
         * Attempts changing hit to a critical via packets for Updated NCP
         */
        PACKET_STRICT(0.11F, 0.1100013579F, 0.0000013579F),

        /**
         * Attempts changing hit to a critical via modifying vanilla packets
         */
        VANILLA(),

        /**
         * Attempts changing hit to a critical via modifying vanilla packets for Updated NCP
         */
        VANILLA_STRICT(),

        /**
         * Attempts critical via a jump
         */
        MOTION();

        // packet offsets
        private final float[] offsets;

        Mode(float... offsets) {
            this.offsets = offsets;
        }

        /**
         * Gets the packet y offsets for the mode
         * @return The packet y offsets for the mode
         */
        public float[] getOffsets() {
            return offsets;
        }
    }
}