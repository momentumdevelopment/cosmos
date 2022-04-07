package cope.cosmos.client.features.modules.combat;

import cope.cosmos.asm.mixins.accessor.IPlayerControllerMP;
import cope.cosmos.client.events.combat.TotemPopEvent;
import cope.cosmos.client.events.entity.player.RotationUpdateEvent;
import cope.cosmos.client.events.entity.player.interact.RightClickItemEvent;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.events.render.entity.RenderRotationsEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.InventoryManager.Switch;
import cope.cosmos.client.manager.managers.SocialManager.Relationship;
import cope.cosmos.client.manager.managers.TickManager.TPS;
import cope.cosmos.util.combat.EnemyUtil;
import cope.cosmos.util.entity.EntityUtil;
import cope.cosmos.util.entity.InterpolationUtil;
import cope.cosmos.util.holder.Rotation;
import cope.cosmos.util.holder.Rotation.Rotate;
import cope.cosmos.util.math.Timer;
import cope.cosmos.util.math.Timer.Format;
import cope.cosmos.util.player.AngleUtil;
import cope.cosmos.util.player.InventoryUtil;
import cope.cosmos.util.player.PlayerUtil;
import cope.cosmos.util.render.RenderBuilder;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.string.ColorUtil;
import cope.cosmos.util.string.StringFormatter;
import cope.cosmos.util.world.RaytraceUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.network.play.client.*;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;

/**
 * @author linustouchtips
 * @since 10/05/2021
 */
public class AuraModule extends Module {
    public static AuraModule INSTANCE;

    public AuraModule() {
        super("Aura", Category.COMBAT, "Attacks nearby entities", () -> StringFormatter.formatEnum(target.getValue()));
        INSTANCE = this;
    }

    // **************************** anticheat ****************************

    public static Setting<Interact> interact = new Setting<>("Interact", Interact.VANILLA)
            .setDescription("Changes how you attack the target");

    public static Setting<Boolean> swing = new Setting<>("Swing", true)
            .setDescription("Swings the players hand when attacking");

    // TODO: fix rotation resetting
    public static Setting<Rotate> rotate = new Setting<>("Rotate", Rotate.NONE)
            .setDescription("Rotate to the current process");

    public static Setting<Double> maxAngle = new Setting<>("MaxAngle", 1.0, 180.0, 180.0, 0)
            .setDescription("Max angle to rotate in one tick")
            .setVisible(() -> !rotate.getValue().equals(Rotate.NONE));

    public static Setting<Double> visibilityTicks = new Setting<>("VisibilityTicks", 0.0, 0.0, 5.0, 0)
            .setDescription("How many ticks you need to stay looking at the current process before continuing")
            .setVisible(() -> !rotate.getValue().equals(Rotate.NONE));
    
    public static Setting<Bone> rotateBone = new Setting<>("Bone", Bone.EYES)
            .setDescription("What body part to rotate to");
    
    public static Setting<Boolean> stopSprint = new Setting<>("StopSprint", false)
            .setDescription("Stops sprinting before attacking");
    
    public static Setting<Boolean> stopSneak = new Setting<>("StopSneak", false)
            .setDescription("Stops sneaking before attacking");

    // **************************** general ****************************
    
    public static Setting<Double> iterations = new Setting<>("Iterations", 0.0, 1.0, 5.0, 0)
            .setDescription("Attacks per iteration");
    
    public static Setting<Double> variation = new Setting<>("Variation", 0.0, 100.0, 100.0, 0)
            .setDescription("Probability of your attacks doing damage");
    
    public static Setting<Double> range = new Setting<>("Range", 0.0, 6.0, 7.0, 1)
            .setDescription("Range to attack entities");
    
    public static Setting<Double> wallsRange = new Setting<>("WallsRange", 0.0, 6.0, 7.0, 1)
            .setDescription("Range to attack entities through walls");

    // **************************** timing ****************************
    
    public static Setting<Timing> timing = new Setting<>("Timing", Timing.SEQUENTIAL)
            .setDescription("Mode for timing attacks");
    
    public static Setting<Delay> delayMode = new Setting<>("Delay", Delay.SWING)
            .setDescription("Mode for timing units");
    
    public static Setting<Double> delayFactor = new Setting<>("Factor", 0.0, 1.0, 1.0, 2)
            .setDescription("Vanilla attack factor")
            .setVisible(() -> delayMode.getValue().equals(Delay.SWING));
    
    public static Setting<Double> delayMilliseconds = new Setting<>("Milliseconds", 0.0, 1000.0, 2000.0, 0)
            .setDescription("Attack Delay in ms")
            .setVisible(() -> delayMode.getValue().equals(Delay.MILLISECONDS));
    
    public static Setting<Double> delayTicks = new Setting<>("Ticks", 0.0, 15.0, 20.0, 0)
            .setDescription("Attack Delay in ticks")
            .setVisible(() -> delayMode.getValue().equals(Delay.TICK));
    
    public static Setting<TPS> delayTPS = new Setting<>("TPS", TPS.AVERAGE)
            .setDescription("Sync attack timing to server ticks");
    //        .setVisible(() -> delayMode.getValue().equals(Delay.TPS));
    
    public static Setting<Double> delaySwitch = new Setting<>("SwitchDelay", 0.0, 0.0, 500.0, 0)
            .setDescription("Time to delay attacks after switching items");
    
    public static Setting<Double> delayRandom = new Setting<>("RandomDelay", 0.0, 0.0, 200.0, 0)
            .setDescription("Randomizes delay to simulate vanilla attacks");
    
    public static Setting<Double> delayTicksExisted = new Setting<>("TicksExisted", 0.0, 0.0, 50.0, 0)
            .setDescription("The minimum age of the target to attack");

    // **************************** misc (maybe UNNECESSARY) ****************************
    
    public static Setting<Double> fov = new Setting<>("FOV", 1.0, 180.0, 180.0, 0)
            .setDescription("Field of vision for the process to function");

    public static Setting<Boolean> packet = new Setting<>("Packet", true)
            .setDescription("Attack with packets");
    
    public static Setting<Boolean> teleport = new Setting<>("Teleport", false)
            .setDescription("Vanilla teleport to target");

    // TODO: make not chinese bon code
    public static Setting<Boolean> reactive = new Setting<>("Reactive", false)
            .setDescription("Spams attacks when target pops a totem");
    
    // **************************** weapon ****************************

    public static Setting<Weapon> weapon = new Setting<>("Weapon", Weapon.SWORD)
            .setDescription("Weapon to use for attacking");
    
    public static Setting<Boolean> weaponOnly = new Setting<>("OnlyWeapon", true)
            .setDescription("Only attack if holding weapon");
    
    public static Setting<Boolean> weaponThirtyTwoK = new Setting<>("32K", false)
            .setDescription("Only attack if holding 32k");
    
    public static Setting<Boolean> weaponBlock = new Setting<>("Block", false)
            .setDescription("Automatically blocks if you're holding a shield");

    public static Setting<Switch> autoSwitch = new Setting<>("Switch", Switch.NORMAL)
            .setDescription("Mode for switching to weapon");

    // **************************** pause ****************************
    
    public static Setting<Double> pauseHealth = new Setting<>("PauseHealth", 0.0, 2.0, 36.0, 0)
            .setDescription("Pause when below this health");
    
    public static Setting<Boolean> pauseEating = new Setting<>("PauseEating", false)
            .setDescription("Pause when eating");
    
    public static Setting<Boolean> pauseMining = new Setting<>("PauseMining", true)
            .setDescription("Pause when mining");
    
    public static Setting<Boolean> pauseMending = new Setting<>("PauseMending", false)
            .setDescription("Pause when mending");

    // **************************** targeting ****************************
    
    public static Setting<Target> target = new Setting<>("Target", Target.CLOSEST)
            .setDescription("Priority for searching target");
    
    public static Setting<Boolean> targetPlayers = new Setting<>("TargetPlayers", true)
            .setDescription("Target players");

    public static Setting<Boolean> targetPassives = new Setting<>("TargetPassives", false)
            .setDescription("Target passives");

    public static Setting<Boolean> targetNeutrals = new Setting<>("TargetNeutrals", false)
            .setDescription("Target neutrals");

    public static Setting<Boolean> targetHostiles = new Setting<>("TargetHostiles", false)
            .setDescription("Target hostiles");

    // **************************** render ****************************

    public static Setting<Boolean> render = new Setting<>("Render", true)
            .setDescription("Render a visual over the target");

    // **************************** targets ****************************

    // attack target
    private Entity auraTarget;

    // **************************** timers ****************************

    // attack timers
    private final Timer auraTimer = new Timer();
    private final Timer criticalTimer = new Timer();
    private final Timer switchTimer = new Timer();

    // **************************** ticks ****************************

    // ticks to pause the process
    private int waitTicks;

    // ticks to wait after switching
    private int switchTicks = 10;

    // **************************** rotation ****************************

    // vector that holds the angle we are looking at
    private Vec3d angleVector;

    // rotation angels
    private Rotation rotateAngles;

    // rotate wait
    private boolean rotationLimit;

    @Override
    public void onUpdate() {

        // prefer a player if there is one in range
        boolean playerBias = false;

        // we are cleared to process our calculations
        if (waitTicks <= 0) {

            // pause if the player is doing something else
            if (PlayerUtil.isEating() && pauseEating.getValue() || PlayerUtil.isMining() && pauseMining.getValue() || PlayerUtil.isMending() && pauseMending.getValue()) {
                return;
            }

            // pause if the player is at a critical health
            else if (PlayerUtil.getHealth() <= pauseHealth.getValue()) {
                return;
            }

            // update ticks before switching
            switchTicks++;

            // map for potential targets
            TreeMap<Double, Entity> validTargets = new TreeMap<>();

            // list of all loaded entities
            Iterator<Entity> entityList = mc.world.loadedEntityList.iterator();
            
            // find our target
            while (entityList.hasNext()) {

                // next entity in the world
                Entity entity = entityList.next();
                
                // make sure the entity is valid to attack
                if (entity == null || entity.equals(mc.player) || EnemyUtil.isDead(entity) || getCosmos().getSocialManager().getSocial(entity.getName()).equals(Relationship.FRIEND)) {
                    continue;
                }

                // don't attack our riding entity
                if (entity.isBeingRidden() && entity.getPassengers().contains(mc.player)) {
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

                // check if entity is in range
                double distance = mc.player.getDistance(entity);
                if (distance > range.getValue()) {
                    continue;
                }

                // vector to trace to
                double traceOffset = getTraceHeight(entity);
                
                // visibility to entity
                boolean isNotVisible = RaytraceUtil.isNotVisible(entity, traceOffset);

                // use wall ranges if not visible
                if (isNotVisible) {
                    if (distance > wallsRange.getValue()) {
                        continue;
                    }
                }
                
                // make sure the entity is truly visible, useful for strict anticheats
                Rotation entityAngle = AngleUtil.calculateAngles(entity.getPositionVector());
                Rotation serverAngle = getCosmos().getRotationManager().getServerRotation();
                
                // angle diff
                double angleDifference = MathHelper.wrapDegrees(serverAngle.getYaw()) - entityAngle.getYaw();
                
                // check if entity is within FOV
                if (Math.abs(angleDifference) > fov.getValue()) {
                    continue;
                }

                // make sure the target has existed in the world for at least a certain number of ticks
                if (entity.ticksExisted < delayTicksExisted.getValue()) {
                    continue;
                }

                // there is at least one player that is attackable
                if (entity instanceof EntityPlayer) {
                    if (!playerBias) {
                        playerBias = true;
                    }
                }

                // calculate priority (minimize)
                double heuristic = getHeuristic(entity);

                // skip
                if (heuristic > 999) {
                    continue;
                }

                // add potential target to our map
                validTargets.put(heuristic, entity);
            }

            // make sure there are valid targets
            if (!validTargets.isEmpty()) {
                
                // find the nearest player
                if (playerBias) {

                    // remove all non-player entities from list
                    validTargets.forEach((distance, entity) -> {
                        
                        // remove 
                        if (!(entity instanceof EntityPlayer)) {
                            validTargets.remove(distance, entity);
                        }
                    });
                }

                // best target is the first entry
                auraTarget = validTargets.firstEntry().getValue();
            }

            // if we found a target to attack, then attack
            if (auraTarget != null) {

                // vector to trace to
                double traceOffset = getTraceHeight(auraTarget);

                /*
                 * Check our distance to the entity as it could have changed since we last calculated our target
                 * we also check if the Aura target is dead, which will also make the target invalid
                 */
                boolean wallAttack = RaytraceUtil.isNotVisible(auraTarget, traceOffset);
                if (auraTarget.isDead || mc.player.getDistance(auraTarget) > (wallAttack ? wallsRange.getValue() : range.getValue())) {
                    auraTarget = null; // set our target to null, as it is now invalid
                    return;
                }

                // vector to rotate to
                angleVector = auraTarget.getPositionVector();

                // scale rotation vector by bone
                angleVector.addVector(0, getTraceHeight(auraTarget), 0);

                // save old fall states
                boolean onGround = mc.player.onGround;
                float fallDistance = mc.player.fallDistance;

                // randomized delay
                long randomFactor = 0;

                if (delayRandom.getValue() > 0) {

                    // random factor
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

                // whether or not we are cleared to attack
                boolean attackCleared = false;

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
                if (timing.getValue().equals(Timing.VANILLA)) {
                    if (auraTarget.hurtResistantTime > 11) {
                        return;
                    }
                }

                // if we are cleared to attack, then attack
                if (attackCleared) {

                    // make sure our switch timer has cleared it's time, attacking right after switching flags Updated NCP
                    if (switchTimer.passedTime(delaySwitch.getValue().longValue(), Format.MILLISECONDS)) {

                        // position before attack
                        Vec3d previousPosition = mc.player.getPositionVector();

                        // teleport to our target, rarely works on an actual server
                        if (teleport.getValue()) {

                            // stop player movement
                            mc.player.setVelocity(0, 0, 0);

                            // **************************** pathfinder ****************************

                            // chained path to target
                            List<Vec3d> chainedTeleports = new ArrayList<>();

                            // target position vector
                            Vec3d targetPosition = auraTarget.getPositionVector();

                            // diffs
                            double diffX = previousPosition.x - targetPosition.x;
                            double diffY = previousPosition.y - targetPosition.y;
                            double diffZ = previousPosition.x - targetPosition.x;

                            // MAXIMUMS
                            final float MAX_MOVE = 2.14915679834294F;
                            final float MAX_JUMP = 1.16610926093821F;

                            // fock
                            if (mc.player.getDistance(auraTarget) > MAX_MOVE) {

                                // while position difference exists
                                while (diffX > 0 && diffY > 0 && diffZ > 0) {

                                    // add to teleport chain
                                    chainedTeleports.add(new Vec3d(previousPosition.x + MAX_MOVE, previousPosition.y + MAX_JUMP, previousPosition.z + MAX_MOVE));

                                    // update diffs
                                    diffX -= MAX_MOVE;
                                    diffY -= MAX_JUMP;
                                    diffZ -= MAX_MOVE;
                                }
                            }

                            // close chain
                            chainedTeleports.add(targetPosition);

                            // teleport to complete chain
                            chainedTeleports.forEach(tp -> {

                                mc.player.setPosition(tp.x, tp.y, tp.z);

                                // send position packets to keep server updated to movements
                                mc.player.connection.sendPacket(new CPacketPlayer.Position(tp.x, tp.y, tp.z, true));
                            });
                        }

                        // if holding a shield then automatically block before attacking
                        if (InventoryUtil.isHolding(Items.SHIELD)) {
                            if (weaponBlock.getValue()) {
                                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));
                            }
                        }

                        // pause switch to account for eating
                        if (PlayerUtil.isEating()) {
                            switchTicks = 0;
                        }

                        // sync item
                        ((IPlayerControllerMP) mc.playerController).hookSyncCurrentPlayItem();

                        // switch to our weapon
                        if (!InventoryUtil.isHolding(weapon.getValue().getItem()) && switchTicks > 10) {
                            getCosmos().getInventoryManager().switchToItem(weapon.getValue().getItem(), autoSwitch.getValue());
                        }

                        // make sure we are holding our weapon
                        if (!InventoryUtil.isHolding(weapon.getValue().getItem()) && weaponOnly.getValue() || InventoryUtil.getHighestEnchantLevel() <= 1000 && weaponThirtyTwoK.getValue()) {
                            return;
                        }

                        // stops sprinting before attacking
                        boolean sprint = mc.player.isSprinting();
                        if (stopSprint.getValue()) {
                            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                            // mc.player.setSprinting(false);
                        }

                        // stops sneaking before attacking
                        boolean sneak = mc.player.isSneaking();
                        if (stopSneak.getValue()) {
                            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                            // mc.player.setSneaking(false);
                        }

                        // if we passed our critical time, then we can attempt a critical attack
                        if (interact.getValue().equals(Interact.NORMAL) && criticalTimer.passedTime(300, Format.MILLISECONDS)) {

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

                        // swing the player's arm
                        if (swing.getValue()) {
                            mc.player.swingArm(EnumHand.MAIN_HAND);
                        }

                        // swing with packets
                        else {
                            mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                        }

                        // reset fall state
                        if (interact.getValue().equals(Interact.NORMAL)) {
                            mc.player.fallDistance = fallDistance;
                            mc.player.onGround = onGround;
                        }

                        // reset sneak state
                        if (stopSneak.getValue() && sneak) {
                            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                            // mc.player.setSneaking(true);
                        }

                        // reset sprint state
                        if (stopSprint.getValue() && sprint) {
                            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
                            // mc.player.setSprinting(true);
                        }

                        // reset teleport state
                        if (teleport.getValue()) {

                            // stop player movement
                            mc.player.setVelocity(0, 0, 0);

                            // tp
                            mc.player.setPosition(previousPosition.x, previousPosition.y, previousPosition.z);

                            // send position packets to keep server updated to movements
                            mc.player.connection.sendPacket(new CPacketPlayer.Position(previousPosition.x, previousPosition.y, previousPosition.z, true));
                        }

                        // reset our aura timer
                        auraTimer.resetTime();
                    }
                }
            }
        }

        else {
            waitTicks--;
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // reset our process for next enable
        auraTarget = null;
        rotateAngles = null;
        switchTicks = 10;
        waitTicks = 0;
        rotationLimit = false;
        angleVector = Vec3d.ZERO;
        auraTimer.resetTime();
        criticalTimer.resetTime();
        switchTimer.resetTime();
    }

    @Override
    public void onRender3D() {

        if (render.getValue()) {
            
            // render a visual around the target
            if (auraTarget != null) {

                RenderUtil.drawCircle(new RenderBuilder()
                        .setup()
                        .line(1.5F)
                        .depth(true)
                        .blend()
                        .texture(), InterpolationUtil.getInterpolatedPosition(auraTarget, 1), auraTarget.width, auraTarget.height * (0.5 * (Math.sin((mc.player.ticksExisted * 3.5) * (Math.PI / 180)) + 1)), ColorUtil.getPrimaryColor());
            }
        }
    }

    @Override
    public boolean isActive() {
        return isEnabled() && auraTarget != null;
    }

    @SubscribeEvent
    public void onTotemPop(TotemPopEvent event) {

        // target has popped a totem
        if (event.getPopEntity().equals(auraTarget)) {

            // react to pop
            if (reactive.getValue()) {

                // spam attacks a player when they pop a totem, useful for insta-killing people on 32k servers (thanks bon55)
                for (int i = 0; i < 5; i++) {
                    getCosmos().getInteractionManager().attackEntity(auraTarget, true, 100);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRotationUpdate(RotationUpdateEvent event) {

        // rotate
        if (!rotate.getValue().equals(Rotate.NONE)) {

            // rotate only if we have an interaction vector to rotate to
            if (angleVector != null) {

                // cancel the existing rotations, we'll send our own
                event.setCanceled(true);

                // yaw and pitch to the angle vector
                rotateAngles = AngleUtil.calculateAngles(angleVector);

                // server rotation
                Rotation serverRotation = getCosmos().getRotationManager().getServerRotation();

                // difference between current and upcoming rotation
                float angleDifference = MathHelper.wrapDegrees(serverRotation.getYaw()) - rotateAngles.getYaw();

                // rotating too fast
                if (Math.abs(angleDifference) > maxAngle.getValue()) {

                    // yaw wrapped
                    float yaw = MathHelper.wrapDegrees(serverRotation.getYaw()); // use server rotation, we won't be updating client rotations

                    // add max angle
                    if (angleDifference < 0) {
                        yaw += maxAngle.getValue();
                    }

                    else {
                        yaw -= maxAngle.getValue();
                    }

                    // update rotation
                    rotateAngles = new Rotation(yaw, rotateAngles.getPitch());

                    // update player rotations
                    if (rotate.getValue().equals(Rotate.CLIENT)) {
                        mc.player.rotationYaw = rotateAngles.getYaw();
                        mc.player.rotationYawHead = rotateAngles.getYaw();
                        mc.player.rotationPitch = rotateAngles.getPitch();
                    }

                    // add our rotation to our client rotations, AutoCrystal has priority over all other rotations
                    getCosmos().getRotationManager().addRotation(rotateAngles, Integer.MAX_VALUE);

                    // we need to wait till we reach our rotation
                    waitTicks++;
                }

                else {

                    // we need to face this crystal for this many ticks
                    if (rotationLimit) {
                        waitTicks = visibilityTicks.getValue().intValue();
                        rotationLimit = false;
                    }

                    // update player rotations
                    if (rotate.getValue().equals(Rotate.CLIENT)) {
                        mc.player.rotationYaw = rotateAngles.getYaw();
                        mc.player.rotationYawHead = rotateAngles.getYaw();
                        mc.player.rotationPitch = rotateAngles.getPitch();
                    }

                    // add our rotation to our client rotations, AutoCrystal has priority over all other rotations
                    getCosmos().getRotationManager().addRotation(rotateAngles, Integer.MAX_VALUE);
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderRotations(RenderRotationsEvent event) {

        // packet rotations
        if (rotate.getValue().equals(Rotate.PACKET)) {

            // rotate only if we have an interaction vector to rotate to
            if (rotateAngles != null) {

                // cancel the model rendering for rotations, we'll set it to our values
                event.setCanceled(true);

                // set our model angles; visual
                event.setYaw(rotateAngles.getYaw());
                event.setPitch(rotateAngles.getPitch());
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {

        // packet for switching held items
        if (event.getPacket() instanceof CPacketHeldItemChange) {

            // we just switched, so reset our time
            switchTimer.resetTime();

            // pause switch
            Item switchItem = mc.player.inventory.getStackInSlot(((CPacketHeldItemChange) event.getPacket()).getSlotId()).getItem();
            if (!weapon.getValue().getItem().isInstance(switchItem)) {
                switchTicks = 0;
            }
        }
    }

    @SubscribeEvent
    public void onRightClickItem(RightClickItemEvent event) {

        // don't switch, we are eating
        if (event.getItemStack().getItem() instanceof ItemFood || event.getItemStack().getItem() instanceof ItemPotion) {
            switchTicks = 0;
        }
    }

    /**
     * Gets the height to trace to
     * @param entity The entity we are tracing to
     * @return The height to trace to
     */
    public double getTraceHeight(Entity entity) {
        
        // eyes
        if (rotateBone.getValue().equals(Bone.EYES)) {
            
            // height to eyes
            double eyeHeight = entity.getEyeHeight();

            // endermen lol
            if (eyeHeight > entity.height) {
                return entity.height;
            }
            
            // returns the eye height
            return eyeHeight;
        }
        
        // body
        else if (rotateBone.getValue().equals(Bone.BODY)) {
            
            // height to center of body
            double bodyHeight = entity.height / 2;
            
            // has a hitbox
            if (entity.canBeAttackedWithItem()) {
                
                // hitbox diff
                double boxDifference = entity.getEntityBoundingBox().maxY - entity.getEntityBoundingBox().minY;
                
                // half of hitbox diff
                bodyHeight = boxDifference / 2;
            }
            
            return bodyHeight;
        }
        
        // feet
        return 0;
    }

    /**
     * Gets the sorting heuristic
     * @param entity The entity to find the heuristic for
     * @return The sorting heuristic
     */
    public double getHeuristic(Entity entity) {

        // target distance
        double distance = mc.player.getDistance(entity);

        if (target.getValue().equals(Target.LOWEST_HEALTH)) {

            // target health
            double health = EnemyUtil.getHealth(entity);

            // can kill in one hit
            if (health <= 2) {
                return distance;
            }

            return health;
        }

        else if (target.getValue().equals(Target.LOWEST_ARMOR)) {

            // target armor durability
            double armor = EnemyUtil.getArmor(entity);

            if (armor > 0) {
                return armor;
            }
        }

        return distance;
    }

    public enum Interact {

        /**
         * Attempt to spoof fall state
         */
        NORMAL,

        /**
         * Attacks normally
         */
        VANILLA,
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
        SWORD(ItemSword.class),

        /**
         * Axe is the preferred weapon
         */
        AXE(ItemAxe.class),

        /**
         * Pickaxe is the preferred weapon
         */
        PICKAXE(ItemPickaxe.class);

        // weapon item
        private final Class<? extends Item> item;

        Weapon(Class<? extends Item> item) {
            this.item = item;
        }

        /**
         * Gets the preferred item
         * @return The preferred item
         */
        public Class<? extends Item> getItem() {
            return item;
        }
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