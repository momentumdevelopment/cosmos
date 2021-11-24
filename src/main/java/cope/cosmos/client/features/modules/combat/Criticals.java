package cope.cosmos.client.features.modules.combat;

import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.combat.Aura.Timing;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.InventoryUtil;
import cope.cosmos.util.player.PlayerUtil;
import cope.cosmos.util.system.Timer;
import cope.cosmos.util.system.Timer.Format;
import cope.cosmos.util.world.TeleportUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@SuppressWarnings("unused")
public class Criticals extends Module {
    public static Criticals INSTANCE;

    public Criticals() {
        super("Criticals", Category.COMBAT, "Ensures all hits are criticals", () -> Setting.formatEnum(mode.getValue()));
        INSTANCE = this;
    }

    public static Setting<Mode> mode = new Setting<>("Mode", "Mode for criticals", Mode.PACKET);
    public static Setting<Double> motion = new Setting<>(() -> mode.getValue().equals(Mode.MOTION), "Motion", "Vertical motion", 0.0D, 0.4D, 1.0D, 2);

    public static Setting<Double> modifier = new Setting<>("Modifier", "Modifies the damage done by a critical attack", 0.0D, 1.5D, 10.0D, 2);

    public static Setting<Double> delay = new Setting<>("Delay", "Delay between attacks to attempt criticals", 0.0D, 200.0D, 2000.0D, 0);
    public static Setting<Double> delayThirtyTwoK = new Setting<>("32K", "Delay between 32K attacks to attempt criticals", 0.0D, 0.0D, 2000.0D, 0).setParent(delay);

    public static Setting<Boolean> teleport = new Setting<>("Teleport", "Teleports up slightly to sync positions", false);
    public static Setting<Boolean> particles = new Setting<>("Particles", "Show critical particles", true);
    
    public static Setting<Boolean> pause = new Setting<>("Pause", "When to pause", true);
    public static Setting<Boolean> pauseLiquid = new Setting<>("Liquid", "Pause in Liquid", true).setParent(pause);
    public static Setting<Boolean> pauseAir = new Setting<>("Air", "Pause when falling or flying", true).setParent(pause);
    public static Setting<Boolean> pauseCrystal = new Setting<>("Crystal", "Pause if attacking crystal", true).setParent(pause);
    public static Setting<Boolean> pauseThirtyTwoK = new Setting<>("32K", "Pause if using 32K", true).setParent(pause);

    Timer criticalTimer = new Timer();

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketUseEntity && ((CPacketUseEntity) event.getPacket()).getAction().equals(CPacketUseEntity.Action.ATTACK)  && ((CPacketUseEntity) event.getPacket()).getEntityFromWorld(mc.world) != null) {
            if (pause.getValue()) {
                if (PlayerUtil.isInLiquid() && pauseLiquid.getValue())
                    return;

                if (mc.player.fallDistance >= 5 && pauseAir.getValue())
                    return;

                if (((CPacketUseEntity) event.getPacket()).getEntityFromWorld(mc.world) instanceof EntityEnderCrystal && pauseCrystal.getValue())
                    return;

                if (InventoryUtil.isHolding32k() && pauseThirtyTwoK.getValue())
                    return;
            }

            handleCritical(((CPacketUseEntity) event.getPacket()).getEntityFromWorld(mc.world));
        }
    }

    @SubscribeEvent
    public void onCriticalHit(CriticalHitEvent event) {
        event.setDamageModifier(modifier.getValue().floatValue());
    }

    public void handleCritical(Entity entity) {
        if (Aura.INSTANCE.isActive() && Aura.timing.getValue().equals(Timing.SEQUENTIAL))
            return;

        if (criticalTimer.passed(InventoryUtil.isHolding32k() ? delayThirtyTwoK.getValue().longValue() : delay.getValue().longValue(), Format.SYSTEM)) {
            if (mode.getValue().equals(Mode.MOTION)) {
                mc.player.jump();
            }

            else {
                for (float offset : mode.getValue().getOffsets()) {
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + offset, mc.player.posZ, false));

                    // should be silent since the server already confirmed the position packet
                    if (teleport.getValue()) {
                        TeleportUtil.teleportPlayerNoPacket(mc.player.posX, mc.player.posY + offset, mc.player.posZ);
                    }
                }
            }

            if (particles.getValue()) {
                mc.player.onCriticalHit(entity);
            }

            criticalTimer.reset();
        }
    }

    public enum Mode {
        PACKET(0.05F, 0, 0.03F, 0), STRICT(0.062602401692772F, 0.0726023996066094F, 0), MOTION();

        private final float[] offsets;

        Mode(float... offsets) {
            this.offsets = offsets;
        }

        public float[] getOffsets() {
            return offsets;
        }
    }
}