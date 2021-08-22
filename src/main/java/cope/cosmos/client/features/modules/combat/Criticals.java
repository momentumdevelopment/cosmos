package cope.cosmos.client.features.modules.combat;

import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.features.modules.combat.Aura.Timing;
import cope.cosmos.util.player.InventoryUtil;
import cope.cosmos.util.player.PlayerUtil;
import cope.cosmos.util.system.Timer;
import cope.cosmos.util.system.Timer.Format;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("unused")
public class Criticals extends Module {
    public static Criticals INSTANCE;

    public Criticals() {
        super("Criticals", Category.COMBAT, "Ensures all hits are criticals");
        INSTANCE = this;
    }

    public static Setting<Mode> mode = new Setting<>("Mode", "Mode for criticals", Mode.PACKET);

    public static Setting<Double> motion = new Setting<>(() -> mode.getValue().equals(Mode.MOTION), "Motion", "Vertical motion", 0.0D, 0.4D, 1.0D, 2);
    public static Setting<Double> modifier = new Setting<>("Modifier", "Modifies the damage done by a critical attack", 0.0D, 1.5D, 10.0D, 2);

    public static Setting<Double> delay = new Setting<>("Delay", "Delay between attacks to attempt criticals", 0.0D, 200.0D, 2000.0D, 0);
    public static Setting<Double> delayThirtyTwoK = new Setting<>("32K", "Delay between 32K attacks to attempt criticals", 0.0D, 0.0D, 2000.0D, 0).setParent(delay);

    public static Setting<Boolean> reset = new Setting<>("Reset", "Resets the player position after attempting to critical", false);
    public static Setting<Boolean> teleport = new Setting<>("Teleport", "Teleports up slightly to sync positions", false);
    public static Setting<Boolean> particles = new Setting<>("Particles", "Show critical particles", true);
    public static Setting<FallBack> fallBack = new Setting<>("FallBack", "Resets player packets after attempting criticals", FallBack.CONFIRM);

    public static Setting<Boolean> pause = new Setting<>("Pause", "When to pause", true);
    public static Setting<Boolean> pauseLiquid = new Setting<>("Liquid", "Pause in Liquid", true).setParent(pause);
    public static Setting<Boolean> pauseAir = new Setting<>("Air", "Pause when falling or flying", true).setParent(pause);
    public static Setting<Boolean> pauseCrystal = new Setting<>("Crystal", "Pause if attacking crystal", true).setParent(pause);
    public static Setting<Boolean> pauseThirtyTwoK = new Setting<>("32K", "Pause if using 32K", true).setParent(pause);

    Timer criticalTimer = new Timer();

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (nullCheck() && event.getPacket() instanceof CPacketUseEntity && ((CPacketUseEntity) event.getPacket()).getAction().equals(CPacketUseEntity.Action.ATTACK)  && ((CPacketUseEntity) event.getPacket()).getEntityFromWorld(mc.world) != null) {
            if (Aura.INSTANCE.isEnabled() && Aura.timing.getValue().equals(Timing.SEQUENTIAL))
                return;

            handleCriticals(Objects.requireNonNull(((CPacketUseEntity) event.getPacket()).getEntityFromWorld(mc.world)));
            handleFallback(Objects.requireNonNull(((CPacketUseEntity) event.getPacket()).getEntityFromWorld(mc.world)));
        }
    }

    @SubscribeEvent
    public void onCriticalHit(CriticalHitEvent event) {
        if (nullCheck())
            event.setDamageModifier(modifier.getValue().floatValue());
    }

    public void handleCriticals(Entity entity) {
        if (pause.getValue()) {
            if (PlayerUtil.isInLiquid() && pauseLiquid.getValue())
                return;

            if (!mode.getValue().equals(Mode.MOTION) && (mc.player.fallDistance > 5 || !mc.player.onGround) && pauseAir.getValue())
                return;

            if (Objects.requireNonNull(entity) instanceof EntityEnderCrystal && pauseCrystal.getValue())
                return;

            if (InventoryUtil.isHolding32k() && pauseThirtyTwoK.getValue())
                return;
        }

        mc.player.fallDistance = 0.2F;
        if (criticalTimer.passed((long) ((double) (InventoryUtil.isHolding32k() ? delayThirtyTwoK.getValue() : delay.getValue())), Format.SYSTEM)) {
            switch (mode.getValue()) {
                case PACKET:
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.05, mc.player.posZ, false));
                    resetPacket(0.05);
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                    resetPacket(0);
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.03, mc.player.posZ, false));
                    resetPacket(0.03);
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                    resetPacket(0);
                    break;
                case STRICT:
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.062602401692772, mc.player.posZ, false));
                    resetPacket(0.062602401692772);
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.0726023996066094, mc.player.posZ, false));
                    resetPacket(0.0726023996066094);
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                    resetPacket(0);
                    break;
                case MOTION:
                    mc.player.motionY = motion.getValue();
                    break;
            }

            criticalTimer.reset();
        }
    }

    public void handleFallback(Entity entity) {
        switch (mode.getValue()) {
            case PACKET:
            case STRICT:
                switch (fallBack.getValue()) {
                    case CONFIRM:
                        mc.player.connection.sendPacket(new CPacketPlayer());
                        break;
                    case RANDOM:
                        mc.player.connection.sendPacket(new CPacketPlayer(ThreadLocalRandom.current().nextBoolean()));
                        break;
                }

                if (particles.getValue())
                    mc.player.onCriticalHit(entity);

                break;
            case MOTION:
                if (particles.getValue())
                    mc.player.onCriticalHit(entity);

                break;
        }
    }

    public void resetPacket(double sent) {
        if (reset.getValue())
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));

        if (teleport.getValue())
            mc.player.setPosition(mc.player.posX, mc.player.posY + sent, mc.player.posZ);
    }

    public enum Mode {
        PACKET, STRICT, MOTION
    }

    public enum FallBack {
        CONFIRM, RANDOM, NONE
    }
}
