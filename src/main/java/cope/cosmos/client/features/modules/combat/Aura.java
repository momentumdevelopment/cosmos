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
import cope.cosmos.client.manager.managers.InventoryManager.Switch;
import cope.cosmos.client.manager.managers.SocialManager.Relationship;
import cope.cosmos.client.manager.managers.TickManager.TPS;
import cope.cosmos.util.client.ColorUtil;
import cope.cosmos.util.client.StringFormatter;
import cope.cosmos.util.combat.EnemyUtil;
import cope.cosmos.util.player.InventoryUtil;
import cope.cosmos.util.player.PlayerUtil;
import cope.cosmos.util.player.Rotation;
import cope.cosmos.util.player.Rotation.Rotate;
import cope.cosmos.util.render.RenderBuilder;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.system.Timer;
import cope.cosmos.util.system.Timer.Format;
import cope.cosmos.util.world.AngleUtil;
import cope.cosmos.util.world.EntityUtil;
import cope.cosmos.util.world.InterpolationUtil;
import cope.cosmos.util.world.RaytraceUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.*;
import net.minecraft.util.EnumHand;
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
public class Aura extends Module {
    public static Aura INSTANCE;

    public Aura() {
        super("Aura", Category.COMBAT, "Attacks nearby entities", () -> StringFormatter.formatEnum(target.getValue()));
        INSTANCE = this;
    }

    // general settings
    public static Setting<Double> iterations = new Setting<>("Iterations", 0.0, 1.0, 5.0, 0).setDescription("Attacks per iteration");
    public static Setting<Double> variation = new Setting<>("Variation", 0.0, 100.0, 100.0, 0).setDescription("Probability of your hits doing damage");
    public static Setting<Double> range = new Setting<>("Range", 0.0, 6.0, 7.0, 1).setDescription("Range to attack entities");
    public static Setting<Double> wallsRange = new Setting<>("WallsRange", 0.0, 6.0, 7.0, 1).setDescription("Range to attack entities through walls");

    // timing category
    public static Setting<Timing> timing = new Setting<>("Timing", Timing.VANILLA).setDescription("Mode for timing attacks");
    public static Setting<Delay> delayMode = new Setting<>("Delay", Delay.SWING).setDescription("Mode for timing units").setParent(timing);
    public static Setting<Double> delayFactor = new Setting<>("Factor", 0.0, 1.0, 1.0, 2).setDescription("Vanilla attack factor").setVisible(() -> delayMode.getValue().equals(Delay.SWING)).setParent(timing);
    public static Setting<Double> delayMilliseconds = new Setting<>("Milliseconds", 0.0, 1000.0, 2000.0, 0).setDescription("Attack Delay in ms").setVisible(() -> delayMode.getValue().equals(Delay.MILLISECONDS)).setParent(timing);
    public static Setting<Double> delayTicks = new Setting<>("Ticks", 0.0, 15.0, 20.0, 0).setDescription("Attack Delay in ticks").setVisible(() -> delayMode.getValue().equals(Delay.TICK)).setParent(timing);
    public static Setting<TPS> delayTPS = new Setting<>("TPS", TPS.AVERAGE).setDescription("Sync attack timing to server ticks").setVisible(() -> delayMode.getValue().equals(Delay.TPS)).setParent(timing);
    public static Setting<Double> delaySwitch = new Setting<>("Switch", 0.0, 0.0, 500.0, 0).setDescription("Time to delay attacks after switching items").setParent(timing);
    public static Setting<Double> delayRandom = new Setting<>("Random", 0.0, 0.0, 200.0, 0).setDescription("Randomizes delay to simulate vanilla attacks").setParent(timing);
    public static Setting<Double> delayTicksExisted = new Setting<>("TicksExisted", 0.0, 0.0, 50.0, 0).setDescription("The minimum age of the target to attack").setParent(timing);

    // misc. category
    public static Setting<Double> timer = new Setting<>("Timer", 0.0, 1.0, 2.0, 2).setDescription("Client-Side timer");
    public static Setting<Double> fov = new Setting<>("FOV", 1.0, 180.0, 180.0, 0).setDescription("Field of vision for the process to function");

    // weapon category
    public static Setting<Weapon> weapon = new Setting<>("Weapon", Weapon.SWORD).setDescription("Weapon to use for attacking");
    public static Setting<Boolean> weaponOnly = new Setting<>("OnlyWeapon", true).setDescription("Only attack if holding weapon").setParent(weapon);
    public static Setting<Boolean> weaponThirtyTwoK = new Setting<>("32K", false).setDescription("Only attack if holding 32k").setParent(weapon);
    public static Setting<Boolean> weaponBlock = new Setting<>("Block", false).setDescription("Automatically blocks if you're holding a shield").setParent(weapon);

    // rotate category
    public static Setting<Rotate> rotate = new Setting<>("Rotation", Rotate.NONE).setDescription("Mode for attack rotations");
    public static Setting<Limit> rotateLimit = new Setting<>("Limit", Limit.NONE).setDescription("Mode for when to restrict rotations").setVisible(() -> rotate.getValue().equals(Rotate.PACKET)).setParent(rotate);
    public static Setting<Bone> rotateBone = new Setting<>("Bone", Bone.EYES).setDescription("What body part to rotate to");
    public static Setting<Double> rotateRandom = new Setting<>("Random", 0.0, 0.0, 5.0, 1).setDescription("Randomize rotations to simulate real rotations").setParent(rotate);

    // anti-cheat category
    public static Setting<Hand> swing = new Setting<>("Swing", Hand.MAINHAND).setDescription("Hand to swing");
    public static Setting<Boolean> raytrace = new Setting<>("Raytrace", false).setDescription("Verify if target is visible");
    public static Setting<Boolean> packet = new Setting<>("Packet", true).setDescription("Attack with packets");
    public static Setting<Boolean> teleport = new Setting<>("Teleport", false).setDescription("Vanilla teleport to target");
    public static Setting<Boolean> reactive = new Setting<>("Reactive", false).setDescription("Spams attacks when target pops a totem");
    public static Setting<Boolean> stopSprint = new Setting<>("StopSprint", false).setDescription("Stops sprinting before attacking");
    public static Setting<Boolean> stopSneak = new Setting<>("StopSneak", false).setDescription("Stops sneaking before attacking");

    // pause category
    public static Setting<Boolean> pause = new Setting<>("Pause", true).setDescription("When to pause");
    public static Setting<Double> pauseHealth = new Setting<>("Health", 0.0, 2.0, 36.0, 0).setDescription("Pause when below this health").setParent(pause);
    public static Setting<Boolean> pauseEating = new Setting<>("Eating", false).setDescription("Pause when eating").setParent(pause);
    public static Setting<Boolean> pauseMining = new Setting<>("Mining", true).setDescription("Pause when mining").setParent(pause);
    public static Setting<Boolean> pauseMending = new Setting<>("Mending", false).setDescription("Pause when mending").setParent(pause);

    // switch category
    public static Setting<Switch> autoSwitch = new Setting<>("Switch", Switch.NORMAL).setDescription("Mode for switching to weapon");

    // target category
    public static Setting<Target> target = new Setting<>("Target", Target.CLOSEST).setDescription("Priority for searching target");
    public static Setting<Boolean> targetPlayers = new Setting<>("Players", true).setDescription("Target players").setParent(target);
    public static Setting<Boolean> targetPassives = new Setting<>("Passives", false).setDescription("Target passives").setParent(target);
    public static Setting<Boolean> targetNeutrals = new Setting<>("Neutrals", false).setDescription("Target neutrals").setParent(target);
    public static Setting<Boolean> targetHostiles = new Setting<>("Hostiles", false).setDescription("Target hostiles").setParent(target);

    // render category
    public static Setting<Boolean> render = new Setting<>("Render", true).setDescription("Render a visual over the target");

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

                // make sure the entity is valid to attack
                if (entity == null || entity.equals(mc.player) || EnemyUtil.isDead(entity) || getCosmos().getSocialManager().getSocial(entity.getName()).equals(Relationship.FRIEND)) {
                    continue;
                }

                // crystal aura should be delegated to the AutoCrystal
                if (entity instanceof EntityEnderCrystal) {
                    continue;
                }

                // should not be attacking items
                if (entity instanceof EntityItem || entity instanceof EntityExpBottle || entity instanceof EntityXPOrb) {
                    continue;
                }

                // verify target
                if (entity instanceof EntityPlayer && !targetPlayers.getValue() || EntityUtil.isPassiveMob(entity) && !targetPassives.getValue() || EntityUtil.isNeutralMob(entity) && !targetNeutrals.getValue() || EntityUtil.isHostileMob(entity) && !targetHostiles.getValue()) {
                    continue;
                }

                // distance to the entity
                double distance = mc.player.getDistance(entity);

                // vector to trace to
                double traceOffset = 0;

                // scale by bone
                switch (rotateBone.getValue()) {
                    case EYES:
                        traceOffset = entity.getEyeHeight();
                        break;
                    case BODY:
                        traceOffset = (entity.height / 2);
                        break;
                    case FEET:
                        break;
                }

                // check if it's in range
                boolean wallAttack = RaytraceUtil.isNotVisible(entity, traceOffset) && raytrace.getValue();
                if (distance > (wallAttack ? wallsRange.getValue() : range.getValue())) {
                    continue;
                }

                // make sure the entity is truly visible, useful for strict anticheats
                Rotation attackAngles = AngleUtil.calculateAngles(entity.getPositionVector());
                if ((Math.abs(mc.player.rotationYaw - attackAngles.getYaw()) % 180) > fov.getValue()) {
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

                // calculate priority (minimize)
                double heuristic = 0;
                switch (target.getValue()) {
                    case LOWEST_HEALTH:
                        heuristic = EnemyUtil.getHealth(entity);
                        break;
                    case LOWEST_ARMOR:
                        heuristic = EnemyUtil.getArmor(entity);
                        break;
                    case CLOSEST:
                        heuristic = distance;
                        break;
                }

                // add potential target to our map
                attackTargets.put(heuristic, entity);
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
                    // best target is the first entry
                    auraTarget = attackTargets.firstEntry().getValue();
                }
            }

            // if we found a target to attack, then attack
            if (auraTarget != null) {

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

                /*
                 * check our distance to the entity as it could have changed since we last calculated our target
                 * we also check if the Aura target is dead, which will also make the target invalid
                 */
                boolean wallAttack = RaytraceUtil.isNotVisible(auraTarget, traceOffset) && raytrace.getValue();
                if (auraTarget.isDead || mc.player.getDistance(auraTarget) > (wallAttack ? wallsRange.getValue() : range.getValue())) {
                    auraTarget = null; // set our target to null, as it is now invalid
                    return;
                }

                // switch to our weapon
                getCosmos().getInventoryManager().switchToItem(weapon.getValue().getItem(), autoSwitch.getValue());

                // make sure we are holding our weapon
                if (!InventoryUtil.isHolding(weapon.getValue().getItem()) && weaponOnly.getValue() || InventoryUtil.getHighestEnchantLevel() <= 1000 && weaponThirtyTwoK.getValue()) {
                    return;
                }

                // set the client ticks
                getCosmos().getTickManager().setClientTicks(timer.getValue().floatValue());

                // teleport to our target, rarely works on an actual server
                if (teleport.getValue()) {
                    mc.player.setVelocity(0, 0, 0);
                    mc.player.setPosition(auraTarget.posX, auraTarget.posY, auraTarget.posZ);
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(auraTarget.posX, auraTarget.posY, auraTarget.posZ, true));
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
                        Rotation attackAngles = AngleUtil.calculateAngles(attackVector);

                        // update our players rotation
                        mc.player.rotationYaw = attackAngles.getYaw();
                        mc.player.rotationYawHead = attackAngles.getYaw();
                        mc.player.rotationPitch = attackAngles.getPitch();
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
                        attackCleared = auraTimer.passedTime(delayMilliseconds.getValue().longValue() + randomFactor, Format.MILLISECONDS);
                        break;
                    case TICK:
                        attackCleared = auraTimer.passedTime(delayTicks.getValue().longValue() + randomFactor, Format.TICKS);
                        break;
                    case NONE:
                        attackCleared = true;
                        break;
                }

                // check hurt resistance time
                if (timing.getValue().equals(Timing.SEQUENTIAL)) {
                    if (auraTarget.hurtResistantTime > 0) {
                        return;
                    }
                }

                // if we are cleared to attack, then attack
                if (attackCleared) {
                    // make sure our switch timer has cleared it's time, attacking right after switching flags Updated NCP
                    if (switchTimer.passedTime(delaySwitch.getValue().longValue(), Format.MILLISECONDS)) {

                        // if we passed our critical time, then we can attempt a critical attack
                        if (criticalTimer.passedTime(300, Format.MILLISECONDS)) {

                            // spoof our fall state to simulate a critical attack
                            mc.player.fallDistance = 0.1F;
                            mc.player.onGround = false;

                            // make sure we only try to land a critical attack every 300 milliseconds
                            criticalTimer.resetTime();
                        }

                        // attack the target
                        for (int i = 0; i < iterations.getValue(); i++) {
                            getCosmos().getInteractionManager().attackEntity(auraTarget, packet.getValue(), variation.getValue());
                        }

                        // swing our hand
                        switch (swing.getValue()) {
                            case MAINHAND:
                                mc.player.swingArm(EnumHand.MAIN_HAND);
                                break;
                            case OFFHAND:
                                mc.player.swingArm(EnumHand.OFF_HAND);
                                break;
                            case PACKET:
                                mc.player.connection.sendPacket(new CPacketAnimation(mc.player.getHeldItemMainhand().getItem().equals(Items.END_CRYSTAL) ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND));
                                break;
                            case NONE:
                                break;
                        }

                        // reset fall state
                        mc.player.fallDistance = fallDistance;
                        mc.player.onGround = onGround;
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
                    .texture(), InterpolationUtil.getInterpolatedPosition(auraTarget, 1), auraTarget.width, auraTarget.height * (0.5 * (Math.sin((mc.player.ticksExisted * 3.5) * (Math.PI / 180)) + 1)), ColorUtil.getPrimaryColor());
        }
    }

    @Override
    public boolean isActive() {
        return isEnabled() && auraTarget != null;
    }

    @SubscribeEvent
    public void onTotemPop(TotemPopEvent event) {
        if (event.getPopEntity().equals(auraTarget) && reactive.getValue()) {
            new Thread(() -> {
                // spam attacks a player when they pop a totem, useful for insta-killing people on 32k servers - thanks bon55
                for (int i = 0; i < 5; i++) {
                    getCosmos().getInteractionManager().attackEntity(auraTarget, true, 100);
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
            Rotation packetAngles = AngleUtil.calculateAngles(attackVector);

            // add random values to our rotations to simulate vanilla rotations
            if (rotateRandom.getValue() > 0) {
                Random randomAngle = new Random();
                packetAngles.setYaw(packetAngles.getYaw() + (randomAngle.nextFloat() * (randomAngle.nextBoolean() ? rotateRandom.getValue().floatValue() : -rotateRandom.getValue().floatValue())));
            }

            if (!rotateLimit.getValue().equals(Limit.NONE)) {
                // difference between the new yaw and the server yaw
                float yawDifference = MathHelper.wrapDegrees(packetAngles.getYaw() - ((IEntityPlayerSP) mc.player).getLastReportedYaw());

                // if it's greater than 55, we need to limit our yaw and skip a tick
                if (Math.abs(yawDifference) > 55 && !yawLimit) {
                    packetAngles.setYaw(((IEntityPlayerSP) mc.player).getLastReportedYaw());
                    strictTicks++;
                    yawLimit = true;
                }

                // if our yaw ticks has passed clearance
                if (strictTicks <= 0) {
                    // if still need to limit our rotation, clamp them to the rotation limit
                    if (rotateLimit.getValue().equals(Limit.STRICT)) {
                        packetAngles.setYaw(((IEntityPlayerSP) mc.player).getLastReportedYaw() + (yawDifference > 0 ? Math.min(Math.abs(yawDifference), 55) : -Math.min(Math.abs(yawDifference), 55)));
                    }

                    yawLimit = false;
                }
            }

            // add our rotation to our client rotations
            getCosmos().getRotationManager().addRotation(new Rotation(packetAngles.getYaw(), packetAngles.getPitch()), 1000);
        }
    }

    @SubscribeEvent
    public void onRenderRotations(RenderRotationsEvent event) {
        if (isActive() && rotate.getValue().equals(Rotate.PACKET)) {
            // cancel the model rendering for rotations, we'll set it to our values
            event.setCanceled(true);

            // find the angles from our interaction
            Rotation packetAngles = AngleUtil.calculateAngles(attackVector);

            // set our model angles; visual
            event.setYaw(packetAngles.getYaw());
            event.setPitch(packetAngles.getPitch());
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
        TPS,

        /**
         * No delay between attacks
         */
        NONE
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

    public enum Hand {

        /**
         * Swings the mainhand
         */
        MAINHAND,

        /**
         * Swings the offhand
         */
        OFFHAND,

        /**
         * Swings via packets, should be silent client-side
         */
        PACKET,

        /**
         * Does not swing
         */
        NONE
    }

    public enum Target {

        /**
         * Finds the closest entity to the player
         */
        CLOSEST,

        /**
         * Finds the entity with the lowest health
         */
        LOWEST_HEALTH,

        /**
         * Finds the entity with the lowest armor durability
         */
        LOWEST_ARMOR
    }
}