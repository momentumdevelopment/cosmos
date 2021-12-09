package cope.cosmos.client.features.modules.combat;

import cope.cosmos.asm.mixins.accessor.ICPacketPlayer;
import cope.cosmos.asm.mixins.accessor.IEntity;
import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.client.StringFormatter;
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
            info.append(StringFormatter.formatEnum(mode.getValue()));

            // time info
            if (!mode.getValue().equals(Mode.MOTION)) {

                // time till next critical attempt
                double timeTillCritical = (delay.getValue() - criticalTimer.getMilliseconds()) / 1000;

                // clamp time till next critical
                if (timeTillCritical < 0) {
                    timeTillCritical = 0;
                }

                if (timeTillCritical > delay.getValue()) {
                    timeTillCritical = delay.getValue();
                }

                info.append(", ").append(timeTillCritical);
            }

            return info.toString();
        });

        INSTANCE = this;
    }

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.PACKET).setDescription("Mode for attempting criticals");
    public static Setting<Double> motion = new Setting<>("Motion", 0.0D, 0.4D, 1.0D, 2).setDescription("Vertical motion").setVisible(() -> mode.getValue().equals(Mode.MOTION));

    public static Setting<Double> modifier = new Setting<>("Modifier", 0.0D, 1.5D, 10.0D, 2).setDescription("Modifies the damage done by a critical attack");
    public static Setting<Double> delay = new Setting<>("Delay", 0.0D, 200.0D, 2000.0D, 0).setDescription("Delay between attacks to attempt criticals");

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
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        // packet for attacks
        if (event.getPacket() instanceof CPacketUseEntity && ((CPacketUseEntity) event.getPacket()).getAction().equals(CPacketUseEntity.Action.ATTACK)) {
            // entity we attacked, if there was one
            Entity attackEntity = ((CPacketUseEntity) event.getPacket()).getEntityFromWorld(mc.world);

            // pause in liquid, webs, and air, since strict anticheats will flag as irregular movement
            if (PlayerUtil.isInLiquid() || mc.player.fallDistance >= 3 || ((IEntity) mc.player).getInWeb()) {
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
                        if (mc.getConnection() != null) {
                            mc.getConnection().getNetworkManager().sendPacket(new CPacketUseEntity(attackEntity));
                            mc.getConnection().getNetworkManager().sendPacket(new CPacketAnimation());
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
                        if (criticalTimer.passedTime(delay.getValue().longValue(), Format.SYSTEM)) {
                            if (mode.getValue().equals(Mode.PACKET) || mode.getValue().equals(Mode.PACKET_STRICT)) {
                                // send packets for each of the offsets
                                for (float offset : mode.getValue().getOffsets()) {
                                    // last packet on strict should confirm player position
                                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.getEntityBoundingBox().minY + offset, mc.player.posZ, false));
                                }

                                // set our attacked entity
                                criticalEntity = attackEntity;
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

        if (event.getPacket() instanceof CPacketPlayer) {
            if (((ICPacketPlayer) event.getPacket()).isMoving()) {
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
                                ((ICPacketPlayer) event.getPacket()).setY(mc.player.getEntityBoundingBox().minY + 0.062602401692772F);
                                break;
                            case 18:
                                ((ICPacketPlayer) event.getPacket()).setY(mc.player.getEntityBoundingBox().minY + 0.0726023996066094F);
                                break;
                            case 17:
                                ((ICPacketPlayer) event.getPacket()).setY(mc.player.getEntityBoundingBox().minY);
                                break;
                        }
                    }
                }
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
        PACKET_STRICT(0.062602401692772F, 0.0726023996066094F, 0, 0),

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