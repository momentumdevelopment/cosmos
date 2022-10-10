package cope.cosmos.client.features.modules.combat;

import cope.cosmos.asm.mixins.accessor.IEntityLivingBase;
import cope.cosmos.client.events.entity.player.RotationUpdateEvent;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.exploits.SwingModule;
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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPacketAnimation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * @author linustouchtips
 * @since 10/05/2021
 */
public class AuraModule extends Module {
    public static AuraModule INSTANCE;

    public AuraModule() {
        super("Aura", new String[] {"KillAura", "ForceField", "KA"}, Category.COMBAT, "Attacks nearby entities", () -> StringFormatter.formatEnum(target.getValue()));
        INSTANCE = this;
    }

    // **************************** anticheat ****************************

    public static Setting<Rotate> rotate = new Setting<>("Rotation", Rotate.NONE)
            .setAlias("Rotate")
            .setDescription("Rotate to the current attack");

    public static Setting<Boolean> yawStep = new Setting<>("YawStep", false)
            .setDescription("Limits yaw rotations")
            .setVisible(() -> !rotate.getValue().equals(Rotate.NONE));

    public static Setting<Double> yawStepThreshold = new Setting<>("YawStepThreshold", 1.0, 180.0, 180.0, 0)
            .setDescription("Max angle to rotate in one tick")
            .setVisible(() -> !rotate.getValue().equals(Rotate.NONE) && yawStep.getValue());

    public static Setting<Boolean> stopSprint = new Setting<>("StopSprint", false)
            .setDescription("Stops sprinting and sneaking before attacking");

    public static Setting<Boolean> swing = new Setting<>("Swing", true)
            .setDescription("Swings the players hand when attacking");

    public static Setting<Raytrace> raytrace = new Setting<>("Raytrace", Raytrace.EYES)
            .setDescription("Trace vector");

    // **************************** general ****************************

    public static Setting<Boolean> attackDelay = new Setting<>("AttackDelay", true)
            .setAlias("HitDelay")
            .setDescription("Delays attacks according to minecraft hit delays for maximum damage per attack");

    public static Setting<Double> attackSpeed = new Setting<>("AttackSpeed", 1.0, 20.0, 20.0, 1)
            .setDescription("Speed to attack")
            .setVisible(() -> !attackDelay.getValue());

    public static Setting<Double> switchDelay = new Setting<>("SwitchDelay", 0.0, 0.0, 10.0, 1)
            .setDescription("Delay to pause after switching items");

    public static Setting<TPS> tps = new Setting<>("TPS", TPS.NONE)
            .setDescription("Server TPS factor");

    public static Setting<Double> range = new Setting<>("Range", 0.0, 5.0, 6.0, 1)
            .setDescription("Range to attack entities");

    public static Setting<Double> wallsRange = new Setting<>("WallsRange", 0.0, 3.5, 6.0, 1)
            .setDescription("Range to attack entities through walls")
            .setVisible(() -> !raytrace.getValue().equals(Raytrace.NONE));

    // **************************** weapon ****************************

    public static Setting<Weapon> weapon = new Setting<>("Weapon", Weapon.SWORD)
            .setDescription("Weapon to use for attacking");

    public static Setting<Boolean> weaponOnly = new Setting<>("OnlyWeapon", true)
            .setAlias("WeaponOnly")
            .setDescription("Only attack if holding weapon");

    public static Setting<Switch> autoSwitch = new Setting<>("Switch", Switch.NORMAL)
            .setAlias("AutoSwitch", "Swap", "AutoSwap")
            .setDescription("Mode for switching to weapon");

    public static Setting<Boolean> autoBlock = new Setting<>("AutoBlock", false)
            .setAlias("Block", "AutoShield")
            .setDescription("Automatically blocks with a shield");

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

    // target that we are attacking
    private Entity attackTarget;

    // **************************** timers****************************

    // timer for attack delays
    private final Timer attackTimer = new Timer();

    // switch timers
    private final Timer switchTimer = new Timer();
    private final Timer autoSwitchTimer = new Timer();

    // **************************** rotation ****************************

    // vector that holds the angle we are looking at
    private Vec3d angleVector;

    // rotation angels
    private Rotation rotateAngles;

    // ticks to pause the process
    private int rotateTicks;

    @Override
    public void onUpdate() {

        // should not function when the ca is active
        if (AutoCrystalModule.INSTANCE.isActive()) {
            return;
        }

        // search ideal targets
        attackTarget = getTarget();

        // we are cleared to process our calculations
        if (rotateTicks <= 0) {

            // we found a target to attack
            if (attackTarget != null) {

                // we have waited the proper time ???
                boolean delayed;

                // should delay attack
                if (attackDelay.getValue()) {

                    // ticks to adjust (based on server's TPS)
                    float adjustTicks = 20 - getCosmos().getTickManager().getTPS(tps.getValue());

                    // cooldown between attacks
                    float cooldown = mc.player.getCooledAttackStrength(adjustTicks);

                    // switch delay based on switch delays (NCP; some servers don't allow attacking right after you've switched your held item)
                    long swapDelay = switchDelay.getValue().longValue() * 25L;

                    // we have waited the proper time ???
                    delayed = cooldown >= 1 && switchTimer.passedTime(swapDelay, Format.MILLISECONDS);
                }

                // custom delays (based on millis instead of vanilla attack delay)
                else {

                    // calculate if we have passed delays
                    // attack delay based on attack speeds
                    long attackDelay = (long) ((attackSpeed.getMax() - attackSpeed.getValue()) * 50);

                    // switch delay based on switch delays (NCP; some servers don't allow attacking right after you've switched your held item)
                    long swapDelay = switchDelay.getValue().longValue() * 25L;

                    // custom delay
                    delayed = attackTimer.passedTime(attackDelay, Format.MILLISECONDS) && switchTimer.passedTime(swapDelay, Format.MILLISECONDS);
                }

                // check if we have passed the place time
                if (delayed) {

                    // face the target
                    angleVector = attackTarget.getPositionVector();

                    // attack the target
                    if (attackTarget(attackTarget)) {

                        // clear
                        attackTimer.resetTime();
                    }
                }
            }
        }

        else {
            rotateTicks--;
        }
    }

    @Override
    public void onRender3D() {

        if (render.getValue()) {

            // render a visual around the target
            if (isActive()) {

                // circle (anim based on sin wave)
                RenderUtil.drawCircle(new RenderBuilder()
                        .setup()
                        .line(1.5F)
                        .depth(true)
                        .blend()
                        .texture(), InterpolationUtil.getInterpolatedPosition(attackTarget, 1), attackTarget.width, attackTarget.height * (0.5 * (Math.sin((mc.player.ticksExisted * 3.5) * (Math.PI / 180)) + 1)), ColorUtil.getPrimaryColor());
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // clear lists and reset variables
        attackTarget = null;
        angleVector = null;
        rotateAngles = null;
    }

    @Override
    public boolean isActive() {
        return isEnabled() && attackTarget != null && (isHoldingWeapon() || !weaponOnly.getValue()) && !AutoCrystalModule.INSTANCE.isActive();
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {

        // packet for switching held item
        if (event.getPacket() instanceof CPacketHeldItemChange) {

            // reset our switch time, we just switched
            switchTimer.resetTime();

            // pause switch if item we switched to is not a crystal
            autoSwitchTimer.resetTime();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRotationUpdate(RotationUpdateEvent event) {

        // rotate
        if (!rotate.getValue().equals(Rotate.NONE)) {

            // manipulate packets if process are trying to complete
            if (isActive()) {

                // rotate only if we have an interaction vector to rotate to
                if (angleVector != null) {

                    // cancel the existing rotations, we'll send our own
                    event.setCanceled(true);

                    // yaw and pitch to the angle vector
                    rotateAngles = AngleUtil.calculateAngles(angleVector);

                    // yaw step requires slower rotations, so we ease into the target rotation, requires some silly math
                    if (yawStep.getValue()) {

                        // rotation that we have serverside
                        Rotation serverRotation = getCosmos().getRotationManager().getServerRotation();

                        // wrapped yaw value
                        float yaw = MathHelper.wrapDegrees(serverRotation.getYaw());

                        // difference between current and upcoming rotation
                        float angleDifference = rotateAngles.getYaw() - yaw;

                        // should never be over 180 since the angles are at max 180 and if it's greater than 180 this means we'll be doing a less than ideal turn
                        // (i.e current = 180, required = -180 -> the turn will be 360 degrees instead of just no turn since 180 and -180 are equivalent)
                        // at worst scenario, current = 90, required = -90 creates a turn of 180 degrees, so this will be our max
                        if (Math.abs(angleDifference) > 180) {

                            // adjust yaw
                            float adjust = angleDifference > 0 ? -360 : 360;
                            angleDifference += adjust;
                        }

                        // use absolute angle diff
                        // rotating too fast
                        if (Math.abs(angleDifference) > yawStepThreshold.getValue()) {

                            // ideal rotation direction
                            int rotationDirection = angleDifference > 0 ? 1 : -1;

                            // add max angle
                            yaw += yawStepThreshold.getValue() * rotationDirection;

                            // update rotation
                            rotateAngles = new Rotation(yaw, rotateAngles.getPitch());

                            // update player rotations
                            if (rotate.getValue().equals(Rotate.CLIENT)) {
                                mc.player.rotationYaw = rotateAngles.getYaw();
                                mc.player.rotationYawHead = rotateAngles.getYaw();
                                mc.player.rotationPitch = rotateAngles.getPitch();
                            }

                            // add our rotation to our client rotations, AutoCrystal has priority over all other rotations
                            getCosmos().getRotationManager().setRotation(rotateAngles);

                            // we need to wait till we reach our rotation
                            rotateTicks++;
                        }

                        else {

                            // update player rotations
                            if (rotate.getValue().equals(Rotate.CLIENT)) {
                                mc.player.rotationYaw = rotateAngles.getYaw();
                                mc.player.rotationYawHead = rotateAngles.getYaw();
                                mc.player.rotationPitch = rotateAngles.getPitch();
                            }

                            // add our rotation to our client rotations
                            getCosmos().getRotationManager().setRotation(rotateAngles);
                        }
                    }

                    // rotate to target instantly
                    else {

                        // update player rotations
                        if (rotate.getValue().equals(Rotate.CLIENT)) {
                            mc.player.rotationYaw = rotateAngles.getYaw();
                            mc.player.rotationYawHead = rotateAngles.getYaw();
                            mc.player.rotationPitch = rotateAngles.getPitch();
                        }

                        // add our rotation to our client rotations
                        getCosmos().getRotationManager().setRotation(rotateAngles);
                    }
                }
            }
        }
    }

    /**
     * Gets the attack target
     * @return The target to attack for the given tick
     */
    public Entity getTarget() {

        /*
         * Map of valid targets
         * Sorted by natural ordering of keys
         * Using tree map allows time complexity of O(logN)
         */
        TreeMap<Double, Entity> validTargets = new TreeMap<>();

        // iterate all entities in the world
        for (Entity entity : new ArrayList<>(mc.world.loadedEntityList)) {

            // make sure the entity actually exists
            if (entity == null || entity.equals(mc.player) || entity.getEntityId() < 0 || EnemyUtil.isDead(entity) || getCosmos().getSocialManager().getSocial(entity.getName()).equals(Relationship.FRIEND)) {
                continue;
            }

            // ignore crystals, they can't be targets (attack crystals is delegated to the AutoCrystal)
            if (entity instanceof EntityEnderCrystal) {
                continue;
            }

            // don't attack our riding entity
            if (entity.isBeingRidden() && entity.getPassengers().contains(mc.player)) {
                continue;
            }

            // verify that the entity is a target
            if (entity instanceof EntityPlayer && !targetPlayers.getValue() || EntityUtil.isPassiveMob(entity) && !targetPassives.getValue() || EntityUtil.isNeutralMob(entity) && !targetNeutrals.getValue() || EntityUtil.isHostileMob(entity) && !targetHostiles.getValue()) {
                continue;
            }

            // distance to target
            double entityRange = mc.player.getDistance(entity);

            // check if the target is in range
            if (entityRange > range.getValue()) {
                continue;
            }

            // check if crystal is behind a wall
            boolean isNotVisible = RaytraceUtil.isNotVisible(entity, raytrace.getValue().getModifier(entity));

            // check if entity can be attacked through wall
            if (isNotVisible) {
                if (entityRange > wallsRange.getValue()) {
                    continue;
                }
            }

            // add to map
            validTargets.put(target.getValue().getModifier(entity), entity);
        }

        // make sure we actually have some valid targets
        if (!validTargets.isEmpty()) {

            // best target in the map, in a TreeMap this is the last entry
            Entity bestTarget = validTargets.lastEntry().getValue();

            // check if the entity hasn't died since the calculation
            if (!EnemyUtil.isDead(bestTarget)){

                // mark it as our current target
                return bestTarget;
            }
        }

        // we were not able to find any attack-able targets
        return null;
    }

    /**
     * Attacks the given entity
     * @param in the given entity
     */
    public boolean attackTarget(Entity in) {

        // make sure the target actually exists
        if (in == null || in.isDead) {
            return false;
        }

        // pause switch to account for actions
        if (PlayerUtil.isEating() || PlayerUtil.isMending() || PlayerUtil.isMining()) {
            autoSwitchTimer.resetTime();
        }

        // switch to weapon if not holding weapon
        if (!isHoldingWeapon()) {

            // wait for switch pause
            if (autoSwitchTimer.passedTime(500, Format.MILLISECONDS)) {

                // switch
                getCosmos().getInventoryManager().switchToItem(weapon.getValue().getItem(), autoSwitch.getValue());
            }
        }

        // only attack if holding weapon
        if (weaponOnly.getValue()) {

            // if we are not holding a weapon we cannot attack
            if (!isHoldingWeapon()) {
                return false;
            }
        }

        // player shield state
        boolean shieldState = false;

        // stop blocking with a shield
        if (autoBlock.getValue()) {

            // update shield state
            shieldState = mc.player.getHeldItemOffhand().getItem() instanceof ItemShield && mc.player.isActiveItemStackBlocking();

            if (shieldState) {
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, new BlockPos(mc.player), EnumFacing.getFacingFromVector((float) mc.player.posX, (float) mc.player.posY, (float) mc.player.posZ)));
            }
        }

        // player sprint state
        boolean sprintState = false;

        // on strict anticheat configs, you need to stop sprinting before attacking (keeping consistent with vanilla behavior)
        if (stopSprint.getValue()) {

            // update sprint state
            sprintState = mc.player.isSprinting();

            // stop sprinting when attacking an entity
            if (sprintState) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                mc.player.setSprinting(false);
            }
        }

        // send attack packet
        mc.player.connection.sendPacket(new CPacketUseEntity(in));
        mc.player.resetCooldown();

        // swing the player's arm
        if (swing.getValue()) {

            // held item stack
            ItemStack stack = mc.player.getHeldItem(EnumHand.MAIN_HAND);

            // check stack
            if (!stack.isEmpty()) {
                if (!stack.getItem().onEntitySwing(mc.player, stack)) {

                    // apply swing progress
                    if (!mc.player.isSwingInProgress || mc.player.swingProgressInt >= ((IEntityLivingBase) mc.player).hookGetArmSwingAnimationEnd() / 2 || mc.player.swingProgressInt < 0) {
                        mc.player.swingProgressInt = -1;
                        mc.player.isSwingInProgress = true;
                        mc.player.swingingHand = SwingModule.INSTANCE.isEnabled() ? SwingModule.INSTANCE.getHand() : EnumHand.MAIN_HAND;

                        // send animation packet
                        if (mc.player.world instanceof WorldServer) {
                            ((WorldServer) mc.player.world).getEntityTracker().sendToTracking(mc.player, new SPacketAnimation(mc.player, 0));
                        }
                    }
                }
            }
        }

        // swing with packets
        mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));

        // reset shield state
        if (shieldState && isHoldingWeapon()) {
            mc.playerController.processRightClick(mc.player, mc.world, EnumHand.OFF_HAND);
        }

        // reset sprint state
        if (sprintState) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
            mc.player.setSprinting(true);
        }

        return true;
    }

    /**
     * Checks if the player is holding the required weapon
     * @return Whether the player is holding the required weapon
     */
    public boolean isHoldingWeapon() {

        // weapon item
        Class<? extends Item> weaponItem = weapon.getValue().getItem();

        // check if player is holding weapon
        return InventoryUtil.isHolding(weaponItem);
    }

    public enum Raytrace {

        /**
         * Attack the entity at the eyes
         */
        EYES((in) ->
                (double) in.getEyeHeight()
        ),

        /**
         * Attack the entity at the torso
         */
        TORSO((in) ->
                in.height / 2D
        ),

        /**
         * Attack the entity at the feet
         */
        FEET((in) ->
                0
        ),

        /**
         * No attacks through walls
         */
        NONE(null);

        // modifier for the entity height
        private final EntityModifier modifier;

        Raytrace(EntityModifier modifier) {
            this.modifier = modifier;
        }

        /**
         * Gets the modifier for the trace
         * @param in The entity
         * @return The modified raytrace offset
         */
        public double getModifier(Entity in) {
            return modifier == null ? -1000 : modifier.modify(in);
        }
    }

    public enum Target {

        /**
         * Finds the closest entity to the player
         */
        CLOSEST((in) ->
            -mc.player.getDistance(in)
        ),

        /**
         * Finds the entity with the lowest health
         */
        LOWEST_HEALTH((in) ->
            -EnemyUtil.getHealth(in)
        ),

        /**
         * Finds the entity with the lowest armor durability
         */
        LOWEST_ARMOR((in) ->
                -EnemyUtil.getArmor(in)
        );

        // modifier for the heuristic
        private final EntityModifier modifier;

        Target(EntityModifier modifier) {
            this.modifier = modifier;
        }

        /**
         * Gets the modifier for the heuristic
         * @param in The entity
         * @return The modified heuristic value
         */
        public double getModifier(Entity in) {
            return modifier == null ? -1000 : modifier.modify(in);
        }
    }

    @FunctionalInterface
    public interface EntityModifier {

        /**
         * Gets the modified value based on the entity's attribute
         * @param in The entity
         * @return The modified value
         */
        double modify(Entity in);
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
}