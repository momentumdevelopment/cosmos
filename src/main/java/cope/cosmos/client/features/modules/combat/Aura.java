package cope.cosmos.client.features.modules.combat;

import com.google.common.util.concurrent.AtomicDouble;
import cope.cosmos.asm.mixins.accessor.IEntityPlayerSP;
import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.events.RenderRotationsEvent;
import cope.cosmos.client.events.RotationUpdateEvent;
import cope.cosmos.client.events.TotemPopEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.TickManager.TPS;
import cope.cosmos.util.client.ColorUtil;
import cope.cosmos.util.client.StringFormatter;
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
import cope.cosmos.util.world.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;
import java.util.TreeMap;

/**
 * @author linustouchtips
 * @since 10/05/2021
 */
@SuppressWarnings("unused")
public class Aura extends Module {
    public static Aura INSTANCE;

    public Aura() {
        super("Aura", Category.COMBAT, "Attacks nearby entities", () -> StringFormatter.formatEnum(target.getValue()));
        INSTANCE = this;
    }

    // general settings
    public static Setting<Double> iterations = new Setting<>("Iterations", "Attacks per iteration", 0.0, 1.0, 5.0, 0);
    public static Setting<Double> variation = new Setting<>("Variation", "Probability of your hits doing damage", 0.0, 100.0, 100.0, 0);
    public static Setting<Double> range = new Setting<>("Range", "Range to attack entities", 0.0, 6.0, 7.0, 1);
    public static Setting<Double> wallsRange = new Setting<>("WallsRange", "Range to attack entities through walls", 0.0, 6.0, 7.0, 1);

    // timing category
    public static Setting<Timing> timing = new Setting<>("Timing", "Mode for timing attacks", Timing.VANILLA);
    public static Setting<Delay> delayMode = new Setting<>("Mode", "Mode for timing units", Delay.SWING).setParent(timing);
    public static Setting<Double> delayFactor = new Setting<>(() -> delayMode.getValue().equals(Delay.SWING), "Factor", "Vanilla attack factor", 0.0, 1.0, 1.0, 2).setParent(timing);
    public static Setting<Double> delay = new Setting<>(() -> delayMode.getValue().equals(Delay.MILLISECONDS), "Delay", "Attack Delay in ms", 0.0, 1000.0, 2000.0, 0).setParent(timing);
    public static Setting<Double> delayTicks = new Setting<>(() -> delayMode.getValue().equals(Delay.TICK), "Ticks", "Attack Delay in ticks", 0.0, 15.0, 20.0, 0).setParent(timing);
    public static Setting<TPS> delayTPS = new Setting<>(() -> delayMode.getValue().equals(Delay.TPS), "TPS", "Sync attack timing to server ticks", TPS.AVERAGE).setParent(timing);
    public static Setting<Double> delaySwitch = new Setting<>("Switch", "Time to delay attacks after switching items", 0.0, 0.0, 500.0, 0).setParent(timing);
    public static Setting<Double> delayRandom = new Setting<>("Random", "Randomizes delay to simulate vanilla attacks", 0.0, 0.0, 200.0, 0).setParent(timing);
    public static Setting<Double> delayTicksExisted = new Setting<>("TicksExisted", "The minimum age of the target to attack", 0.0, 0.0, 50.0, 0).setParent(timing);

    // misc. category
    public static Setting<Double> timer = new Setting<>("Timer", "Client-Side timer", 0.0, 1.0, 2.0, 2);
    public static Setting<Double> fov = new Setting<>("FOV", "Field of vision for the process to function", 1.0, 180.0, 180.0, 0);

    // weapon category
    public static Setting<Weapon> weapon = new Setting<>("Weapon", "Weapon to use for attacking", Weapon.SWORD);
    public static Setting<Boolean> weaponOnly = new Setting<>("OnlyWeapon", "Only attack if holding weapon", true).setParent(weapon);
    public static Setting<Boolean> weaponThirtyTwoK = new Setting<>("32K", "Only attack if holding 32k", false).setParent(weapon);
    public static Setting<Boolean> weaponBlock = new Setting<>("Block", "Automatically blocks if you're holding a shield", false).setParent(weapon);

    // rotate category
    public static Setting<Rotate> rotate = new Setting<>("Rotation", "Mode for attack rotations", Rotate.NONE);
    public static Setting<Limit> rotateLimit = new Setting<>(() -> rotate.getValue().equals(Rotate.PACKET), "Limit", "Mode for when to restrict rotations", Limit.NONE).setParent(rotate);
    public static Setting<Bone> rotateBone = new Setting<>("Bone", "What body part to rotate to", Bone.EYES);
    public static Setting<Double> rotateRandom = new Setting<>("Random", "Randomize rotations to simulate real rotations", 0.0, 0.0, 5.0, 1).setParent(rotate);

    // anti-cheat category
    public static Setting<Hand> swing = new Setting<>("Swing", "Hand to swing", Hand.MAINHAND);
    public static Setting<Boolean> raytrace = new Setting<>("Raytrace", "Verify if target is visible", false);
    public static Setting<Boolean> packet = new Setting<>("Packet", "Attack with packets", true);
    public static Setting<Boolean> teleport = new Setting<>("Teleport", "Vanilla teleport to target", false);
    public static Setting<Boolean> reactive = new Setting<>("Reactive", "Spams attacks when target pops a totem", false);
    public static Setting<Boolean> stopSprint = new Setting<>("StopSprint", "Stops sprinting before attacking", false);
    public static Setting<Boolean> stopSneak = new Setting<>("StopSneak", "Stops sneaking before attacking", false);

    // pause category
    public static Setting<Boolean> pause = new Setting<>("Pause", "When to pause", true);
    public static Setting<Double> pauseHealth = new Setting<>("Health", "Pause when below this health", 0.0, 2.0, 36.0, 0).setParent(pause);
    public static Setting<Boolean> pauseEating = new Setting<>("Eating", "Pause when eating", false).setParent(pause);
    public static Setting<Boolean> pauseMining = new Setting<>("Mining", "Pause when mining", true).setParent(pause);
    public static Setting<Boolean> pauseMending = new Setting<>("Mending", "Pause when mending", false).setParent(pause);

    // switch category
    public static Setting<Switch> autoSwitch = new Setting<>("Switch", "Mode for switching to weapon", Switch.NORMAL);

    // target category
    public static Setting<Target> target = new Setting<>("Target", "Priority for searching target", Target.CLOSEST);
    public static Setting<Boolean> targetPlayers = new Setting<>("Players", "Target players", true).setParent(target);
    public static Setting<Boolean> targetPassives = new Setting<>("Passives", "Target passives", false).setParent(target);
    public static Setting<Boolean> targetNeutrals = new Setting<>("Neutrals", "Target neutrals", false).setParent(target);
    public static Setting<Boolean> targetHostiles = new Setting<>("Hostiles", "Target hostiles", false).setParent(target);

    // render category
    public static Setting<Boolean> render = new Setting<>("Render", "Render a visual over the target", true);

    // attack target
    private Entity auraTarget;

    // attack timers
    private final Timer auraTimer = new Timer();
    private final Timer criticalTimer = new Timer();
    private final Timer switchTimer = new Timer();

    // tick clamp
    private int strictTicks;

    // rotation info
    private boolean yawLimit;
    private Vec3d attackVector = Vec3d.ZERO;

    @Override
    public void onUpdate() {
        if (strictTicks > 0) {
            strictTicks--;
        }

        else {
            // prefer a player if there is one in range
            boolean playerBias = false;

            // pause if needed
            if (pause.getValue()) {
                // pause if the player is doing something else
                if (PlayerUtil.isEating() && pauseEating.getValue() || PlayerUtil.isMining() && pauseMining.getValue() || PlayerUtil.isMending() && pauseMending.getValue()) {
                    return;
                }

                // pause if the player is at a critical health
                else if (PlayerUtil.getHealth() <= pauseHealth.getValue()) {
                    return;
                }
            }

            // map for potential targets
            TreeMap<Double, Entity> attackTargets = new TreeMap<>();

            // find our target
            for (Entity entity : mc.world.loadedEntityList) {
                // distance to the entity
                double distance = mc.player.getDistance(entity);

                // vector to trace to
                double traceOffset = 0;

                // scale by bone
                switch (rotateBone.getValue()) {
                    case EYES:
                        traceOffset = auraTarget.getEyeHeight();
                        break;
                    case BODY:
                        traceOffset = (auraTarget.height / 2);
                        break;
                    case FEET:
                        break;
                }

                // check if it's in range
                boolean wallAttack = !RaytraceUtil.raytraceEntity(auraTarget, traceOffset) && raytrace.getValue();
                if (distance > (wallAttack ? wallsRange.getValue() : range.getValue())) {
                    continue;
                }

                // make sure the entity is truly visible, useful for strict antichears
                float[] attackAngles = AngleUtil.calculateAngles(entity.getPositionVector());
                if (AngleUtil.calculateAngleDifference(mc.player.rotationYaw, attackAngles[0]) > fov.getValue()) {
                    continue;
                }

                // make sure the target has existed in the world for at least a certain number of ticks
                if (entity.ticksExisted < delayTicksExisted.getValue()) {
                    continue;
                }

                // there is at least one player that is attackable
                if (!playerBias && entity instanceof EntityPlayer) {
                    playerBias = true;
                }

                // add potential target to our map
                attackTargets.put(distance, entity);
            }

            if (!attackTargets.isEmpty()) {
                // find the nearest player
                if (playerBias) {

                    // check distance
                    AtomicDouble closestPlayer = new AtomicDouble(Double.MAX_VALUE);
                    attackTargets.forEach((distance, entity) -> {
                        if (entity instanceof EntityPlayer) {
                            if (distance <= closestPlayer.get()) {

                                // update our closest target
                                auraTarget = entity;
                                closestPlayer.set(distance);
                            }
                        }
                    });
                }

                else {
                    // closest target is the last entry
                    auraTarget = attackTargets.lastEntry().getValue();
                }
            }

            // if we found a target to attack, then attack
            if (auraTarget != null) {

                // switch to our weapon
                InventoryUtil.switchToSlot(weapon.getValue().getItem(), autoSwitch.getValue());

                // make sure we are holding our weapon
                if (!InventoryUtil.isHolding(weapon.getValue().getItem()) && weaponOnly.getValue() || !InventoryUtil.isHolding32k() && weaponThirtyTwoK.getValue()) {
                    return;
                }

                // set the client ticks
                getCosmos().getTickManager().setClientTicks(timer.getValue().floatValue());

                // teleport to our target, rarely works on an actual server
                if (teleport.getValue()) {
                    TeleportUtil.teleportPlayer(auraTarget.posX, auraTarget.posY, auraTarget.posZ);
                }

                if (!rotate.getValue().equals(Rotate.NONE)) {
                    // vector to rotate to
                    attackVector = auraTarget.getPositionVector();

                    // scale rotation vector by bone
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

                    // update client rotations
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

                // save old fall states
                boolean onGround = mc.player.onGround;
                float fallDistance = mc.player.fallDistance;

                // whether or not we are cleared to attack
                boolean attackCleared = false;

                // randomized delay
                long randomFactor = 0;

                if (delayRandom.getValue() > 0) {
                    Random attackRandom = new Random();

                    // scale delay by random based on delay mode
                    switch (delayMode.getValue()) {
                        case SWING:
                        case TPS:
                            randomFactor = (long) (attackRandom.nextFloat() * (delayRandom.getValue().longValue() / delayRandom.getMax().longValue()));
                            break;
                        case MILLISECONDS:
                            randomFactor = (long) (attackRandom.nextFloat() * delayRandom.getValue().longValue());
                            break;
                        case TICK:
                            randomFactor = (long) (attackRandom.nextFloat() * (delayRandom.getValue().longValue() / 50F));
                            break;
                    }

                    // negative or positive?
                    if (attackRandom.nextBoolean()) {
                        randomFactor *= -1;
                    }
                }

                // scale delay based on delay mode
                switch (delayMode.getValue()) {
                    case SWING:
                        attackCleared = mc.player.getCooledAttackStrength(0) >= delayFactor.getValue() + randomFactor;
                        break;
                    case TPS:
                        attackCleared = mc.player.getCooledAttackStrength(delayTPS.getValue().equals(TPS.NONE) ? 0 : 20 - getCosmos().getTickManager().getTPS(delayTPS.getValue())) >= delayFactor.getValue() + randomFactor;
                        break;
                    case MILLISECONDS:
                        attackCleared = auraTimer.passedTime(delay.getValue().longValue() + randomFactor, Format.SYSTEM);
                        break;
                    case TICK:
                        attackCleared = auraTimer.passedTime(delayTicks.getValue().longValue() + randomFactor, Format.TICKS);
                        break;
                }

                // if we are cleared to attack, then attack
                if (attackCleared) {

                    // make sure our switch timer has cleared it's time, attacking right after switching flags Updated NCP
                    if (switchTimer.passedTime(delaySwitch.getValue().longValue(), Format.SYSTEM)) {

                        // if we passed our critical time, then we can attempt a critical attack
                        if (criticalTimer.passedTime(300, Format.SYSTEM) && timing.getValue().equals(Timing.SEQUENTIAL)) {

                            // spoof our fall state to simulate a critical attack
                            mc.player.fallDistance = 0.1F;
                            mc.player.onGround = false;

                            // make sure we only try to land a critical attack every 300 milliseconds
                            criticalTimer.resetTime();
                        }

                        // attack the target
                        for (int i = 0; i < iterations.getValue(); i++) {
                            getCosmos().getInteractionManager().attackEntity(auraTarget, packet.getValue(), swing.getValue(), variation.getValue());
                        }

                        // reset fall state
                        if (timing.getValue().equals(Timing.SEQUENTIAL)) {
                            mc.player.fallDistance = fallDistance;
                            mc.player.onGround = onGround;
                        }
                    }

                    // reset sneak state
                    if (stopSneak.getValue() && sneak) {
                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                        mc.player.setSneaking(true);
                    }

                    // reset sprint state
                    if (stopSprint.getValue() && sprint) {
                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
                        mc.player.setSprinting(true);
                    }

                    // reset the client ticks
                    getCosmos().getTickManager().setClientTicks(1);

                    // reset our aura timer
                    auraTimer.resetTime();
                }
            }
        }
    }

    @Override
    public void onRender3D() {
        // render a visual around the target
        if (auraTarget != null && render.getValue()) {
            RenderUtil.drawCircle(new RenderBuilder()
                    .setup()
                    .line(1.5F)
                    .depth(true)
                    .blend()
                    .texture(), InterpolationUtil.getInterpolatedPos(auraTarget, 1), auraTarget.width, auraTarget.height * (0.5 * (Math.sin((mc.player.ticksExisted * 3.5) * (Math.PI / 180)) + 1)), ColorUtil.getPrimaryColor());
        }
    }

    @Override
    public boolean isActive() {
        return INSTANCE.isEnabled() && auraTarget != null;
    }

    @SubscribeEvent
    public void onTotemPop(TotemPopEvent event) {
        if (event.getPopEntity().equals(auraTarget) && reactive.getValue()) {
            new Thread(() -> {
                // spam attacks a player when they pop a totem, useful for insta-killing people on 32k servers - thanks bon55
                for (int i = 0; i < 5; i++) {
                    getCosmos().getInteractionManager().attackEntity(auraTarget, true, swing.getValue(), 100);
                }
            }).start();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRotationUpdate(RotationUpdateEvent event) {
        if (isActive() && rotate.getValue().equals(Rotate.PACKET)) {
            // cancel the existing rotations, we'll send our own
            event.setCanceled(true);

            // angles to the last attack
            float[] packetAngles = AngleUtil.calculateAngles(attackVector);

            // add random values to our rotations to simulate vanilla rotations
            if (rotateRandom.getValue() > 0) {
                Random randomAngle = new Random();
                packetAngles[0] += randomAngle.nextFloat() * (randomAngle.nextBoolean() ? rotateRandom.getValue() : -rotateRandom.getValue());
            }

            if (!rotateLimit.getValue().equals(Limit.NONE)) {
                // difference between the new yaw and the server yaw
                float yawDifference = MathHelper.wrapDegrees(packetAngles[0] - ((IEntityPlayerSP) mc.player).getLastReportedYaw());

                // if it's greater than 55, we need to limit our yaw and skip a tick
                if (Math.abs(yawDifference) > 55 && !yawLimit) {
                    packetAngles[0] = ((IEntityPlayerSP) mc.player).getLastReportedYaw();
                    strictTicks++;
                    yawLimit = true;
                }

                // if our yaw ticks has passed clearance
                if (strictTicks <= 0) {
                    // if still need to limit our rotation, clamp them to the rotation limit
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
            // cancel the model rendering for rotations, we'll set it to our values
            event.setCanceled(true);

            // find the angles from our interaction
            float[] packetAngles = AngleUtil.calculateAngles(attackVector);
            if (rotateRandom.getValue() > 0) {
                Random randomAngle = new Random();
                packetAngles[0] += randomAngle.nextFloat() * (randomAngle.nextBoolean() ? rotateRandom.getValue() : -rotateRandom.getValue());
            }

            // set our model angles; visual
            event.setYaw(packetAngles[0]);
            event.setPitch(packetAngles[1]);
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketHeldItemChange) {
            // we just switched, so reset our time
            switchTimer.resetTime();
        }
    }

    public enum Delay {
        /**
         * Vanilla swing delay for maximum damage
         */
        SWING,

        /**
         * Custom delay in milliseconds
         */
        MILLISECONDS,

        /**
         * Custom delay in ticks
         */
        TICK,

        /**
         * Times attacks based on server TPS
         */
        TPS
    }

    public enum Timing {
        /**
         * Times the attacks based on entity updates
         */
        VANILLA,

        /**
         * Times the attacks based on ticks
         */
        SEQUENTIAL
    }

    public enum Weapon {
        /**
         * Sword is the preferred weapon
         */
        SWORD(Items.DIAMOND_SWORD),

        /**
         * Axe is the preferred weapon
         */
        AXE(Items.DIAMOND_AXE),

        /**
         * Pickaxe is the preferred weapon
         */
        PICKAXE(Items.DIAMOND_PICKAXE);

        private final Item item;

        Weapon(Item item) {
            this.item = item;
        }

        /**
         * Gets the preferred item
         * @return The preferred item
         */
        public Item getItem() {
            return item;
        }
    }

    public enum Limit {
        /**
         * Skips ticks based on yaw limit
         */
        NORMAL,

        /**
         * Limits yaw and skips ticks based on yaw limit
         */
        STRICT,

        /**
         * Doesn't limit yaw
         */
        NONE
    }

    public enum Bone {
        /**
         * Attack the entity at the eyes
         */
        EYES,

        /**
         * Attack the entity at the torso
         */
        BODY,

        /**
         * Attack the entity at the feet
         */
        FEET
    }
}