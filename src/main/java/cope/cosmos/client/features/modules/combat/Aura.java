package cope.cosmos.client.features.modules.combat;

import cope.cosmos.asm.mixins.accessor.IEntityPlayerSP;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.*;
import cope.cosmos.client.manager.managers.TickManager.TPS;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.client.ColorUtil;
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
import net.minecraft.network.play.client.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;
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
    public static Setting<Double> range = new Setting<>("Range", "Range to attack entities", 0.0, 4.0, 7.0, 1);
    public static Setting<Double> wallsRange = new Setting<>("WallsRange", "Range to attack entities through walls", 0.0, 4.0, 7.0, 1);

    public static Setting<Timing> timing = new Setting<>("Timing", "Mode for timing attacks", Timing.VANILLA);
    public static Setting<Delay> delayMode = new Setting<>("Mode", "Mode for timing units", Delay.SWING).setParent(timing);
    public static Setting<Double> delayFactor = new Setting<>(() -> delayMode.getValue().equals(Delay.SWING), "Factor", "Vanilla attack factor", 0.0, 1.0, 1.0, 2).setParent(timing);
    public static Setting<Double> delay = new Setting<>(() -> delayMode.getValue().equals(Delay.CUSTOM), "Delay", "Attack Delay in ms", 0.0, 1000.0, 2000.0, 0).setParent(timing);
    public static Setting<Double> delayTicks = new Setting<>(() -> delayMode.getValue().equals(Delay.TICK), "Ticks", "Attack Delay in ticks", 0.0, 15.0, 20.0, 0).setParent(timing);
    public static Setting<TPS> delayTPS = new Setting<>(() -> delayMode.getValue().equals(Delay.TPS), "TPS", "Sync attack timing to server ticks", TPS.AVERAGE).setParent(timing);
    public static Setting<Double> delaySwitch = new Setting<>("Switch", "Time to delay attacks after switching items", 0.0, 0.0, 500.0, 0).setParent(timing);
    public static Setting<Double> delayRandom = new Setting<>("Random", "Randomizes delay to simulate vanilla attacks", 0.0, 0.0, 200.0, 0).setParent(timing);
    public static Setting<Double> delayTicksExisted = new Setting<>("TicksExisted", "The minimum age of the target to attack", 0.0, 0.0, 50.0, 0).setParent(timing);

    public static Setting<Double> timer = new Setting<>("Timer", "Client-Side timer", 0.0, 1.0, 2.0, 2);
    public static Setting<Double> fov = new Setting<>("FOV", "Field of vision for the process to function", 1.0, 180.0, 180.0, 0);

    public static Setting<Weapon> weapon = new Setting<>("Weapon", "Weapon to use for attacking", Weapon.SWORD);
    public static Setting<Boolean> weaponOnly = new Setting<>("OnlyWeapon", "Only attack if holding weapon", true).setParent(weapon);
    public static Setting<Boolean> weaponThirtyTwoK = new Setting<>("32K", "Only attack if holding 32k", false).setParent(weapon);
    public static Setting<Boolean> weaponBlock = new Setting<>("Block", "Automatically blocks if you're holding a shield", false).setParent(weapon);

    public static Setting<Rotate> rotate = new Setting<>("Rotation", "Mode for attack rotations", Rotate.NONE);
    public static Setting<Limit> rotateLimit = new Setting<>(() -> rotate.getValue().equals(Rotate.PACKET), "Limit", "Mode for when to restrict rotations", Limit.NONE).setParent(rotate);
    public static Setting<Bone> rotateBone = new Setting<>("Bone", "What body part to rotate to", Bone.EYES);
    public static Setting<Double> rotateRandom = new Setting<>("Random", "Randomize rotations to simulate real rotations", 0.0, 0.0, 5.0, 1).setParent(rotate);

    public static Setting<Hand> swing = new Setting<>("Swing", "Hand to swing", Hand.MAINHAND);
    public static Setting<Boolean> raytrace = new Setting<>("Raytrace", "Verify if target is visible", false);
    public static Setting<Boolean> packet = new Setting<>("Packet", "Attack with packets", true);
    public static Setting<Boolean> teleport = new Setting<>("Teleport", "Vanilla teleport to target", false);
    public static Setting<Boolean> reactive = new Setting<>("Reactive", "Spams attacks when target pops a totem", false);
    public static Setting<Boolean> merge = new Setting<>("Merge", "Merges this attack and the next attack", false);
    public static Setting<Boolean> stopSprint = new Setting<>("StopSprint", "Stops sprinting before attacking", false);
    public static Setting<Boolean> stopSneak = new Setting<>("StopSneak", "Stops sneaking before attacking", false);

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

    private Entity auraTarget;

    private final Timer auraTimer = new Timer();
    private final Timer criticalTimer = new Timer();
    private final Timer switchTimer = new Timer();

    private int strictTicks;

    private boolean yawLimit;
    private Vec3d attackVector = Vec3d.ZERO;

    @Override
    public void onUpdate() {
        if (strictTicks > 0) {
            strictTicks--;
        }

        else {
            // find our target
            auraTarget = TargetUtil.getTargetEntity(range.getValue(), target.getValue(), targetPlayers.getValue(), targetPassives.getValue(), targetNeutrals.getValue(), targetHostiles.getValue());

            if (pause.getValue()) {
                if (PlayerUtil.isEating() && pauseEating.getValue() || PlayerUtil.isMining() && pauseMining.getValue() || PlayerUtil.isMending() && pauseMending.getValue())
                    return;

                else if (PlayerUtil.getHealth() <= pauseHealth.getValue())
                    return;
            }

            killAura();
        }
    }

    @Override
    public void onRender3D() {
        if (auraTarget != null && render.getValue()) {
            RenderUtil.drawCircle(new RenderBuilder().setup().line(1.5F).depth(true).blend().texture(), InterpolationUtil.getInterpolatedPos(auraTarget, 1), auraTarget.width, auraTarget.height * (0.5 * (Math.sin((mc.player.ticksExisted * 3.5) * (Math.PI / 180)) + 1)), ColorUtil.getPrimaryColor());
        }
    }

    @Override
    public boolean isActive() {
        return INSTANCE.isEnabled() && auraTarget != null;
    }

    public void killAura() {
        if (auraTarget != null) {
            if (!InventoryUtil.isHolding(weapon.getValue().getItem()) && weaponOnly.getValue() || !InventoryUtil.isHolding32k() && weaponThirtyTwoK.getValue())
                return;

            if (AngleUtil.calculateAngleDifference(mc.player.rotationYaw, AngleUtil.calculateAngles(auraTarget.getPositionVector())[0]) > fov.getValue())
                return;

            if (!RaytraceUtil.raytraceEntity(auraTarget, auraTarget.getEyeHeight())) {
                if (raytrace.getValue() || mc.player.getDistance(auraTarget) > wallsRange.getValue())
                    return;
            }

            if (auraTarget.ticksExisted < delayTicksExisted.getValue())
                return;

            InventoryUtil.switchToSlot(weapon.getValue().getItem(), autoSwitch.getValue());

            // set the client ticks
            Cosmos.INSTANCE.getTickManager().setClientTicks(timer.getValue().floatValue());

            if (teleport.getValue()) {
                TeleportUtil.teleportPlayer(auraTarget.posX, auraTarget.posY, auraTarget.posZ);
            }

            if (!rotate.getValue().equals(Rotate.NONE)) {
                attackVector = auraTarget.getPositionVector();
                
                switch (rotateBone.getValue()) {
                    case EYES:
                        attackVector.addVector(0, auraTarget.getEyeHeight(), 0);
                        break;
                    case BODY:
                        attackVector.addVector(0, (auraTarget.height / 2),0);
                        break;
                    case FEET:
                        break;
                }

                if (rotate.getValue().equals(Rotate.CLIENT)) {
                    float[] attackAngles = AngleUtil.calculateAngles(attackVector);

                    // update our players rotation
                    mc.player.rotationYaw = attackAngles[0];
                    mc.player.rotationYawHead = attackAngles[0];
                    mc.player.rotationPitch = attackAngles[1];
                }
            }

            // if holding a shield then automatically block before attacking
            if (weaponBlock.getValue() && InventoryUtil.isHolding(Items.SHIELD)) {
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));
            }

            // stops sprinting before attacking
            boolean sprint = mc.player.isSprinting();
            if (stopSprint.getValue()) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                mc.player.setSprinting(false);
            }

            // stops sneaking before attacking
            boolean sneak = mc.player.isSneaking();
            if (stopSneak.getValue()) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                mc.player.setSneaking(false);
            }

            boolean onGround = mc.player.onGround;
            float fallDistance = mc.player.fallDistance;

            if (handleDelay()) {
                // if we passed our critical time, then we can attempt a critical attack
                if (criticalTimer.passed(300, Format.SYSTEM) && timing.getValue().equals(Timing.SEQUENTIAL)) {
                    mc.player.fallDistance = 0.1F;
                    mc.player.onGround = false;

                    // make sure we only try to land criticals every 300 milliseconds
                    criticalTimer.reset();
                }

                // attack the target
                for (int i = 0; i < iterations.getValue(); i++) {
                    PlayerUtil.attackEntity(auraTarget, packet.getValue(), swing.getValue(), variation.getValue());
                }

                // makes attacks more consistent, especially on really low TPS
                if (merge.getValue()) {
                    mc.player.connection.sendPacket(new CPacketPlayer(ThreadLocalRandom.current().nextBoolean()));
                }

                if (timing.getValue().equals(Timing.SEQUENTIAL)) {
                    mc.player.onCriticalHit(auraTarget);
                    mc.player.fallDistance = fallDistance;
                    mc.player.onGround = onGround;
                }
            }

            if (stopSneak.getValue()) {
                if (sneak) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                    mc.player.setSneaking(true);
                }
            }

            if (stopSprint.getValue()) {
                if (sprint) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
                    mc.player.setSprinting(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onTotemPop(TotemPopEvent event) {
        if (event.getPopEntity().equals(auraTarget) && reactive.getValue()) {
            new Thread(() -> {
                // spam attacks a player when they pop a totem, useful for insta-killing people on 32k servers - thanks bon55
                for (int i = 0; i < 5; i++) {
                    mc.player.connection.sendPacket(new CPacketUseEntity(auraTarget));
                    PlayerUtil.swingArm(swing.getValue());
                }
            }).start();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRotationUpdate(RotationUpdateEvent event) {
        if (isActive() && rotate.getValue().equals(Rotate.PACKET)) {
            // cancel the existing rotations, we'll send our own
            event.setCanceled(true);

            float[] packetAngles = AngleUtil.calculateAngles(attackVector);

            // add random values to our rotations to simulate vanilla rotations
            if (rotateRandom.getValue() > 0) {
                Random randomAngle = new Random();
                packetAngles[0] += randomAngle.nextFloat() * (randomAngle.nextBoolean() ? rotateRandom.getValue() : -rotateRandom.getValue());
            }

            if (!rotateLimit.getValue().equals(Limit.NONE)) {
                float yawDifference = MathHelper.wrapDegrees(packetAngles[0] - ((IEntityPlayerSP) mc.player).getLastReportedYaw());

                if (Math.abs(yawDifference) > 55 && !yawLimit) {
                    packetAngles[0] = ((IEntityPlayerSP) mc.player).getLastReportedYaw();
                    strictTicks++;
                    yawLimit = true;
                }

                if (strictTicks <= 0) {
                    if (rotateLimit.getValue().equals(Limit.STRICT)) {
                        packetAngles[0] = ((IEntityPlayerSP) mc.player).getLastReportedYaw() + (yawDifference > 0 ? Math.min(Math.abs(yawDifference), 55) : -Math.min(Math.abs(yawDifference), 55));
                    }

                    yawLimit = false;
                }
            }

            // add our rotation to our client rotations
            getCosmos().getRotationManager().addRotation(new Rotation(packetAngles[0], packetAngles[1]), 1000);
        }
    }

    @SubscribeEvent
    public void onRenderRotations(RenderRotationsEvent event) {
        if (isActive() && rotate.getValue().equals(Rotate.PACKET)) {
            event.setCanceled(true);

            float[] packetAngles = AngleUtil.calculateAngles(attackVector);
            if (rotateRandom.getValue() > 0) {
                Random randomAngle = new Random();
                packetAngles[0] += randomAngle.nextFloat() * (randomAngle.nextBoolean() ? rotateRandom.getValue() : -rotateRandom.getValue());
            }

            event.setYaw(packetAngles[0]);
            event.setPitch(packetAngles[1]);
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketHeldItemChange) {
            switchTimer.reset();
        }
    }

    public boolean handleDelay() {
        Random attackRandom = new Random();

        switch (delayMode.getValue()) {
            case TPS:
                double tpsScaled = delayFactor.getValue() + (attackRandom.nextBoolean() ? attackRandom.nextFloat() * (delayRandom.getValue().longValue() / delayRandom.getMax()) : -attackRandom.nextFloat() * (delayRandom.getValue().longValue() / delayRandom.getMax()));

                // delay by server ticks
                return mc.player.getCooledAttackStrength(delayTPS.getValue().equals(TPS.NONE) ? 0 : 20 - getCosmos().getTickManager().getTPS(delayTPS.getValue())) >= tpsScaled && switchTimer.passed(delaySwitch.getValue().longValue(), Format.SYSTEM);
            case SWING:
                double swingScaled = delayFactor.getValue() + (attackRandom.nextBoolean() ? attackRandom.nextFloat() * (delayRandom.getValue().longValue() / delayRandom.getMax()) : -attackRandom.nextFloat() * (delayRandom.getValue().longValue() / delayRandom.getMax()));

                // vanilla swing delays
                return mc.player.getCooledAttackStrength(0) >= swingScaled && switchTimer.passed(delaySwitch.getValue().longValue(), Format.SYSTEM);
            case CUSTOM:
                long delayScaled = (long) (delay.getValue().longValue() + (attackRandom.nextBoolean() ? (attackRandom.nextFloat() * delayRandom.getValue().longValue()) : -(attackRandom.nextFloat() * delayRandom.getValue().longValue())));

                // delay in milliseconds
                if (auraTimer.passed(delayScaled, Format.SYSTEM)) {
                    auraTimer.reset();
                    return switchTimer.passed(delaySwitch.getValue().longValue(), Format.SYSTEM);
                }

            case TICK:
                long tickScaled = (long) (delayTicks.getValue().longValue() + (attackRandom.nextBoolean() ? (attackRandom.nextFloat() * (delayRandom.getValue().longValue() / 50)) : -(attackRandom.nextFloat() * (delayRandom.getValue().longValue() / 50))));

                // delay in ticks
                return auraTimer.passed(tickScaled, Format.TICKS) && switchTimer.passed(delaySwitch.getValue().longValue(), Format.SYSTEM);
        }

        return true;
    }

    public enum Delay {
        SWING, CUSTOM, TICK, TPS
    }

    public enum Timing {
        VANILLA, SEQUENTIAL, NONE
    }

    public enum Weapon {
        SWORD(Items.DIAMOND_SWORD), AXE(Items.DIAMOND_AXE), PICKAXE(Items.DIAMOND_PICKAXE);

        private final Item item;

        Weapon(Item item) {
            this.item = item;
        }

        public Item getItem() {
            return item;
        }
    }

    public enum Limit {
        NORMAL, STRICT, NONE
    }

    public enum Bone {
        EYES, BODY, FEET
    }
}