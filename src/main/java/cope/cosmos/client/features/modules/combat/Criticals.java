package cope.cosmos.client.features.modules.combat;

import cope.cosmos.client.events.MotionUpdateEvent;
import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.InventoryUtil;
import cope.cosmos.util.player.PlayerUtil;
import cope.cosmos.util.system.Timer;
import cope.cosmos.util.system.Timer.Format;
import cope.cosmos.util.world.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips
 * @since 12/04/2021
 */
@SuppressWarnings("unused")
public class Criticals extends Module {
    public static Criticals INSTANCE;

    public Criticals() {
        super("Criticals", Category.COMBAT, "Ensures all hits are criticals", () -> {

            // criticals info
            StringBuilder info = new StringBuilder();

            // mode info
            info.append(Setting.formatEnum(mode.getValue()));

            // time info
            if (!mode.getValue().equals(Mode.MOTION)) {
                info.append(", ").append(delay.getValue() - criticalTimer.getMilliseconds());
            }

            return info.toString();
        });

        INSTANCE = this;
    }

    public static Setting<Mode> mode = new Setting<>("Mode", "Mode for attempting criticals", Mode.PACKET);
    public static Setting<Double> motion = new Setting<>(() -> mode.getValue().equals(Mode.MOTION), "Motion", "Vertical motion", 0.0D, 0.4D, 1.0D, 2);

    public static Setting<Double> modifier = new Setting<>("Modifier", "Modifies the damage done by a critical attack", 0.0D, 1.5D, 10.0D, 2);
    public static Setting<Double> delay = new Setting<>("Delay", "Delay between attacks to attempt criticals", 0.0D, 200.0D, 2000.0D, 0);

    // criticals timer
    private static final Timer criticalTimer = new Timer();

    // packet info
    CPacketUseEntity resendAttackPacket;
    CPacketAnimation resendAnimationPacket;

    // critical entity
    Entity criticalEntity;

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
    public void onMotionUpdate(MotionUpdateEvent event) {
        if (criticalEntity != null) {
            // make sure entity is hurt
            if (criticalEntity.hurtResistantTime <= 16) {
                criticalEntity = null;
                return;
            }

            // modify packets
            if (mode.getValue().equals(Mode.VANILLA)) {
                // all vanilla packets are off ground
                event.setOnGround(false);

                // modify packets based on entity hurt time
                switch (criticalEntity.hurtResistantTime) {
                    case 20:
                        event.setY(mc.player.getEntityBoundingBox().minY + 0.5F);
                        break;
                    case 19:
                    case 17:
                        event.setY(mc.player.getEntityBoundingBox().minY);
                        break;
                    case 18:
                        event.setY(mc.player.getEntityBoundingBox().minY + 0.3F);
                        break;
                }
            }

            // strict has dynamic onGround packets
            else if (mode.getValue().equals(Mode.VANILLA_STRICT)) {
                // modify packets based on entity hurt time
                switch (criticalEntity.hurtResistantTime) {
                    case 19:
                        event.setOnGround(false);
                        event.setY(mc.player.getEntityBoundingBox().minY + 0.062602401692772F);
                        break;
                    case 18:
                        event.setOnGround(false);
                        event.setY(mc.player.getEntityBoundingBox().minY + 0.0726023996066094F);
                        break;
                    case 17:
                        event.setOnGround(true);
                        event.setY(mc.player.getEntityBoundingBox().minY);
                        break;
                }
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        // packet for attacks
        if (event.getPacket() instanceof CPacketUseEntity && ((CPacketUseEntity) event.getPacket()).getAction().equals(CPacketUseEntity.Action.ATTACK)) {
            // entity we attacked, if there was one
            Entity attackEntity = ((CPacketUseEntity) event.getPacket()).getEntityFromWorld(mc.world);

            // pause in liquid and air, since strict anticheats will flag as irregular movement
            if (PlayerUtil.isInLiquid() || mc.player.fallDistance >= 3) {
                return;
            }

            // pause if attacking a crystal, helps compatability with AutoCrystal
            if (attackEntity instanceof EntityEnderCrystal) {
                return;
            }

            // critical hits on 32k's are insignificant
            if (InventoryUtil.isHolding32k()) {
                return;
            }

            // make sure the attacked entity exists
            if (attackEntity != null && !attackEntity.isDead) {

                // destroying a vehicle takes 5 hits -> regardless of damage
                if (EntityUtil.isVehicleMob(attackEntity)) {
                    // attack 5 times
                    for (int i = 0; i < 5; i++) {
                        mc.player.connection.sendPacket(new CPacketUseEntity(attackEntity));
                        mc.player.connection.sendPacket(new CPacketAnimation());
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
                        if (criticalTimer.passedTime(delay.getValue().longValue(), Format.SYSTEM)) {
                            if (mode.getValue().equals(Mode.PACKET) || mode.getValue().equals(Mode.PACKET_STRICT)) {
                                // send packets for each of the offsets
                                for (float offset : mode.getValue().getOffsets()) {
                                    // last packet on strict should confirm player position
                                    if (mode.getValue().equals(Mode.PACKET_STRICT)) {
                                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.getEntityBoundingBox().minY + offset, mc.player.posZ, offset >= mode.getValue().getOffsets().length - 1));
                                    }

                                    else {
                                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.getEntityBoundingBox().minY + offset, mc.player.posZ, false));
                                    }
                                }
                            }

                            // set our attacked entity
                            criticalEntity = attackEntity;

                            // reset our timer
                            criticalTimer.resetTime();
                        }

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
    }

    @SubscribeEvent
    public void onCriticalHit(CriticalHitEvent event) {
        // set the damage modifier for critical hits
        event.setDamageModifier(modifier.getValue().floatValue());
    }

    public enum Mode {
        /**
         * Attempts changing hit to a critical via packets
         */
        PACKET(0.05F, 0, 0.03F, 0),

        /**
         * Attempts changing hit to a critical via modifying vanilla packets
         */
        VANILLA(),

        /**
         * Attempts changing hit to a critical via modifying vanilla packets for Updated NCP
         */
        VANILLA_STRICT(),

        /**
         * Attempts changing hit to a critical via packets for Updated NCP
         */
        PACKET_STRICT(0.062602401692772F, 0.0726023996066094F, 0),

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