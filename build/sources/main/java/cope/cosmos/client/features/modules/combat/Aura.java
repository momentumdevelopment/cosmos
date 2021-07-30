package cope.cosmos.client.features.modules.combat;

import cope.cosmos.asm.mixins.accessor.ICPacketPlayer;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.manager.managers.TickManager.TPS;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.combat.TargetUtil;
import cope.cosmos.util.combat.TargetUtil.Target;
import cope.cosmos.util.player.InventoryUtil;
import cope.cosmos.util.player.InventoryUtil.Switch;
import cope.cosmos.util.player.PlayerUtil;
import cope.cosmos.util.player.PlayerUtil.Hand;
import cope.cosmos.util.player.Rotation;
import cope.cosmos.util.player.Rotation.Rotate;
import cope.cosmos.util.render.RenderBuilder;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.system.Timer;
import cope.cosmos.util.system.Timer.Format;
import cope.cosmos.util.world.AngleUtil;
import cope.cosmos.util.world.InterpolationUtil;
import cope.cosmos.util.world.RaytraceUtil;
import cope.cosmos.util.world.TeleportUtil;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("unused")
public class Aura extends Module {
    public static Aura INSTANCE;

    public Aura() {
        super("Aura", Category.COMBAT, "Attacks nearby entities", () -> Setting.formatEnum(target.getValue()));
        INSTANCE = this;
    }

    public static Setting<Double> iterations = new Setting<>("Iterations", "Attacks per iteration", 0.0, 1.0, 5.0, 0);
    public static Setting<Double> variation = new Setting<>("Variation", "Probability of your hits doing damage", 0.0, 100.0, 100.0, 0);
    public static Setting<Double> range = new Setting<>("Range", "Range to attack entities", 0.0, 5.0, 7.0, 1);

    public static Setting<Timing> timing = new Setting<>("Timing", "Mode for timing attacks", Timing.COOLDOWN);
    public static Setting<Delay> delayMode = new Setting<>("Mode", "Mode for timing units", Delay.SWING).setParent(timing);
    public static Setting<Double> delayFactor = new Setting<>(() -> delayMode.getValue().equals(Delay.SWING), "Factor", "Vanilla attack factor", 0.0, 1.0, 1.0, 2).setParent(timing);
    public static Setting<Double> delay = new Setting<>(() -> delayMode.getValue().equals(Delay.CUSTOM), "Delay", "Attack Delay in ms", 0.0, 1000.0, 2000.0, 0).setParent(timing);
    public static Setting<Double> delayTicks = new Setting<>(() -> delayMode.getValue().equals(Delay.TICK), "Ticks", "Attack Delay in ticks", 0.0, 15.0, 20.0, 0).setParent(timing);
    public static Setting<TPS> delayTPS = new Setting<>(() -> delayMode.getValue().equals(Delay.TPS), "TPS", "Sync attack timing to server ticks", TPS.AVERAGE).setParent(timing);
    public static Setting<Double> delaySwitch = new Setting<>("Switch", "Time to delay attacks after switching items", 0.0, 0.0, 500.0, 0).setParent(timing);

    public static Setting<Double> timer = new Setting<>("Timer", "Client-Side timer", 0.0, 1.0, 2.0, 2);
    public static Setting<Double> fov = new Setting<>("FOV", "Field of vision for the process to function", 1.0, 180.0, 180.0, 0);

    public static Setting<Weapon> weapon = new Setting<>("Weapon", "Weapon to use for attacking", Weapon.SWORD);
    public static Setting<Boolean> weaponOnly = new Setting<>("OnlyWeapon", "Only attack if holding weapon", true).setParent(weapon);
    public static Setting<Boolean> weaponThirtyTwoK = new Setting<>("32K", "Only attack if holding 32k", false).setParent(weapon);
    public static Setting<Boolean> weaponBlock = new Setting<>("Block", "Automatically blocks if you're holding a shield", false).setParent(weapon);

    public static Setting<Rotate> rotate = new Setting<>("Rotation", "Mode for attack rotations", Rotate.NONE);
    public static Setting<Float> rotateStep = new Setting<>("Step", "Number of divisions when sending rotation packets", 1.0F, 1.0F, 10.0F, 0).setParent(rotate);
    public static Setting<Boolean> rotateCenter = new Setting<>("Center", "Center rotations on target", false).setParent(rotate);
    public static Setting<Boolean> rotateRandom = new Setting<>("Random", "Randomize rotations to simulate real rotations", false).setParent(rotate);

    public static Setting<Hand> swing = new Setting<>("Swing", "Hand to swing", Hand.MAINHAND);
    public static Setting<Boolean> raytrace = new Setting<>("Raytrace", "Verify if target is visible", false);
    public static Setting<Boolean> packet = new Setting<>("Packet", "Attack with packets", true);
    public static Setting<Boolean> teleport = new Setting<>("Teleport", "Vanilla teleport to target", false);
    public static Setting<Boolean> stopSprint = new Setting<>("StopSprint", "Stops sprinting before attacking", false);

    public static Setting<Boolean> pause = new Setting<>("Pause", "When to pause", true);
    public static Setting<Double> pauseHealth = new Setting<>("Health", "Pause when below this health", 0.0, 10.0, 36.0, 0).setParent(pause);
    public static Setting<Boolean> pauseEating = new Setting<>("Eating", "Pause when eating", false).setParent(pause);
    public static Setting<Boolean> pauseMining = new Setting<>("Mining", "Pause when mining", true).setParent(pause);
    public static Setting<Boolean> pauseMending = new Setting<>("Mending", "Pause when mending", false).setParent(pause);

    public static Setting<Switch> autoSwitch = new Setting<>("Switch", "Mode for switching to weapon", Switch.NORMAL);

    public static Setting<Target> target = new Setting<>("Target", "Priority for searching target", Target.CLOSEST);
    public static Setting<Boolean> targetPlayers = new Setting<>("Players", "Target players", true).setParent(target);
    public static Setting<Boolean> targetPassives = new Setting<>("Passives", "Target passives", false).setParent(target);
    public static Setting<Boolean> targetNeutrals = new Setting<>("Neutrals", "Target neutrals", false).setParent(target);
    public static Setting<Boolean> targetHostiles = new Setting<>("Hostiles", "Target hostiles", false).setParent(target);

    public static Setting<Boolean> render = new Setting<>("Render", "Render a visual over the target", true);
    public static Setting<Color> color = new Setting<>("Color", "Color of the visual", new Color(144, 0, 255, 45)).setParent(render);

    public static Entity auraTarget = null;
    public static Timer auraTimer = new Timer();
    public static Rotation auraRotation = new Rotation(Float.NaN, Float.NaN, rotate.getValue());

    @Override
    public void onUpdate() {
        auraTarget = TargetUtil.getTargetEntity(range.getValue(), target.getValue(), targetPlayers.getValue(), targetPassives.getValue(), targetNeutrals.getValue(), targetHostiles.getValue());

        if (auraTarget != null && handlePause())
            killAura();
    }

    @Override
    public void onRender3d() {
        if (auraTarget != null && render.getValue())
            RenderUtil.drawCircle(new RenderBuilder().setup().line(1.5F).depth(true).blend().texture(), InterpolationUtil.getInterpolatedPos(auraTarget, 1), auraTarget.width, auraTarget.height * (0.5 * (Math.sin((mc.player.ticksExisted * 3.5) * (Math.PI / 180)) + 1)), color.getValue());
    }

    public void killAura() {
        InventoryUtil.switchToSlot(weapon.getValue().getItem(), autoSwitch.getValue());
        Cosmos.INSTANCE.getTickManager().setClientTicks(timer.getValue());

        if (teleport.getValue())
            TeleportUtil.teleportPlayer(auraTarget.posX, auraTarget.posY, auraTarget.posZ);

        if (!rotate.getValue().equals(Rotate.NONE)) {
            float[] auraAngles = rotateCenter.getValue() ? AngleUtil.calculateCenter(auraTarget) : AngleUtil.calculateAngles(auraTarget);
            auraRotation = new Rotation(rotateRandom.getValue() ? auraAngles[0] + (float) (ThreadLocalRandom.current().nextDouble(-4, 4)) : auraAngles[0], rotateRandom.getValue() ? auraAngles[1] + (float) (ThreadLocalRandom.current().nextDouble(-4, 4)) : auraAngles[1], rotate.getValue());

            if (!Float.isNaN(auraRotation.getYaw()) && !Float.isNaN(auraRotation.getPitch()))
                auraRotation.updateModelRotations();
        }

        if (weaponBlock.getValue() && InventoryUtil.isHolding(Items.SHIELD))
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));

        boolean sprint = mc.player.isSprinting();

        if (stopSprint.getValue()) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
            mc.player.setSprinting(false);
        }

        if (handleDelay()) {
            for (int i = 0; i < iterations.getValue(); i++) {
                PlayerUtil.attackEntity(auraTarget, packet.getValue(), swing.getValue(), variation.getValue());
            }
        }

        if (stopSprint.getValue()) {
            if (sprint)
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));

            mc.player.setSprinting(sprint);
        }

        if (Criticals.INSTANCE.isEnabled() && timing.getValue().equals(Timing.SEQUENTIAL)) {
            Criticals.INSTANCE.handleCriticals(auraTarget);
            Criticals.INSTANCE.handleFallback(auraTarget);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPacketSend(PacketEvent.PacketSendEvent packetSendEvent) {
        if ((packetSendEvent.getPacket() instanceof CPacketPlayer) && !Float.isNaN(auraRotation.getYaw()) && !Float.isNaN(auraRotation.getPitch()) && rotate.getValue().equals(Rotate.PACKET)) {
            if (Math.abs(auraRotation.getYaw() - mc.player.rotationYaw) >= 20 || Math.abs(auraRotation.getPitch() - mc.player.rotationPitch) >= 20) {
                for (float step = rotateStep.getValue() - 1; step > 0; step--) {
                    mc.player.connection.sendPacket(new CPacketPlayer.Rotation(auraRotation.getYaw() / step + 1, auraRotation.getPitch() / step + 1, mc.player.onGround));
                }
            }

            ((ICPacketPlayer) packetSendEvent.getPacket()).setYaw(auraRotation.getYaw());
            ((ICPacketPlayer) packetSendEvent.getPacket()).setPitch(auraRotation.getPitch());
        }
    }

    public boolean handleDelay() {
        if (timing.getValue().equals(Timing.COOLDOWN) || timing.getValue().equals(Timing.SEQUENTIAL)) {
            switch (delayMode.getValue()) {
                case TPS:
                    return mc.player.getCooledAttackStrength(delayTPS.getValue().equals(TPS.NONE) ? 0 : 20 - Cosmos.INSTANCE.getTickManager().getTPS(delayTPS.getValue())) >= delayFactor.getValue();
                case SWING:
                    return mc.player.getCooledAttackStrength(0) >= delayFactor.getValue();
                case CUSTOM:
                    if (auraTimer.passed((long) ((double) delay.getValue()), Format.SYSTEM)) {
                        auraTimer.reset();
                        return true;
                    }

                    break;
                case TICK:
                    return auraTimer.passed((long) ((double) delayTicks.getValue()), Format.TICKS);
            }
        }

        return true;
    }

    public boolean handlePause() {
        if (pause.getValue()) {
            if (Cosmos.INSTANCE.getSwitchManager().switchAttackReady((long) ((double) delaySwitch.getValue())))
                return false;

            if (!InventoryUtil.isHolding(weapon.getValue().getItem()) && weaponOnly.getValue() || !InventoryUtil.isHolding32k() && weaponThirtyTwoK.getValue())
                return false;

            else if (PlayerUtil.isEating() && pauseEating.getValue() || PlayerUtil.isMining() && pauseMining.getValue() || PlayerUtil.isMending() && pauseMending.getValue())
                return false;

            else if (PlayerUtil.getHealth() <= pauseHealth.getValue())
                return false;

            else if (AngleUtil.calculateAngleDifference(mc.player.rotationYaw, AngleUtil.calculateAngles(auraTarget)[0]) > fov.getValue())
                return false;

            else
                return RaytraceUtil.raytraceEntity(auraTarget, auraTarget.getEyeHeight()) || !raytrace.getValue();
        }

        return true;
    }

    public enum Delay {
        SWING, CUSTOM, TICK, TPS
    }

    public enum Timing {
        SEQUENTIAL, COOLDOWN, NONE
    }

    @SuppressWarnings("unused")
    public enum Weapon {
        SWORD(Items.DIAMOND_SWORD), AXE(Items.DIAMOND_AXE), PICKAXE(Items.DIAMOND_PICKAXE);

        private final Item item;

        Weapon(Item item) {
            this.item = item;
        }

        public Item getItem() {
            return this.item;
        }
    }
}