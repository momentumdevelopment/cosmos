package cope.cosmos.client.features.modules.combat;

import cope.cosmos.asm.mixins.accessor.ICPacketUseEntity;
import cope.cosmos.asm.mixins.accessor.IEntityPlayerSP;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.events.RenderRotationsEvent;
import cope.cosmos.client.events.RotationUpdateEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.SocialManager.Relationship;
import cope.cosmos.util.client.ColorUtil;
import cope.cosmos.util.combat.EnemyUtil;
import cope.cosmos.util.combat.ExplosionUtil;
import cope.cosmos.util.combat.TargetUtil.Target;
import cope.cosmos.util.player.InventoryUtil;
import cope.cosmos.util.player.InventoryUtil.Inventory;
import cope.cosmos.util.player.InventoryUtil.Switch;
import cope.cosmos.util.player.PlayerUtil;
import cope.cosmos.util.player.Rotation;
import cope.cosmos.util.player.Rotation.Rotate;
import cope.cosmos.util.render.RenderBuilder;
import cope.cosmos.util.render.RenderBuilder.Box;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.system.MathUtil;
import cope.cosmos.util.system.Timer;
import cope.cosmos.util.system.Timer.Format;
import cope.cosmos.util.world.AngleUtil;
import cope.cosmos.util.world.BlockUtil;
import cope.cosmos.util.world.RaytraceUtil;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author linustouchtips
 * @since 12/04/2021
 */
@SuppressWarnings("unused")
public class AutoCrystal extends Module {
    public static AutoCrystal INSTANCE;

    public AutoCrystal() {
        super("AutoCrystal", Category.COMBAT, "Places and explodes crystals", () -> {

            // placement info
            StringBuilder info = new StringBuilder();

            // calculate the heuristic
            double heuristic = 0;
            if (placePosition != null) {
                heuristic = MathUtil.roundDouble((placePosition.getTargetDamage() - placePosition.getLocalDamage() / 10), 2) ;
            }

            info.append(heuristic).append(", ");

            // response time
            double ping = MathUtil.roundDouble(responseTime / 10, 3);

            // clamp
            if (ping >= 10) {
                ping = 9.99;
            }

            info.append(ping);

            return info.toString();
        });

        INSTANCE = this;
    }

    // explode category
    public static Setting<Boolean> explode = new Setting<>("Explode", true).setDescription("Explode crystals");
    public static Setting<Double> explodeRange = new Setting<>("Range", 0.0, 6.0, 8.0, 1).setDescription("Range to explode crystals").setParent(explode);
    public static Setting<Double> explodeWall = new Setting<>("WallRange", 0.0, 3.5, 8.0, 1).setDescription("Range to explode crystals through walls").setParent(explode);
    public static Setting<Double> explodeDelay = new Setting<>("Delay", 0.0, 60.0, 500.0, 0).setDescription("Delay to explode crystals").setParent(explode);
    public static Setting<Double> explodeRandom = new Setting<>("RandomDelay", 0.0, 0.0, 500.0, 0).setDescription("Randomize the delay slightly to simulate real explosions").setParent(explode);
    public static Setting<Double> explodeSwitch = new Setting<>("SwitchDelay", 0.0, 0.0, 500.0, 0).setDescription("Delay to wait after switching").setParent(explode);
    public static Setting<Double> explodeTicksExisted = new Setting<>("TicksExisted", 0.0, 0.0, 5.0, 0).setDescription("The minimum age of the crystal to explode").setParent(explode);
    public static Setting<Double> explodeDamage = new Setting<>("Damage", 0.0, 5.0, 36.0, 1).setDescription("Required damage to explode a crystal").setParent(explode);
    public static Setting<Double> explodeLocal = new Setting<>("LocalDamage", 0.0, 5.0, 36.0, 1).setDescription("Maximum allowed local damage to the player").setParent(explode);
    public static Setting<Double> explodeLimit = new Setting<>("Limit", 0.0, 10.0, 10.0, 0).setDescription("Limits attacks per crystal").setParent(explode);
    public static Setting<Boolean> explodePacket = new Setting<>("Packet", true).setDescription("Explode with packets").setParent(explode);
    public static Setting<Boolean> explodeInhibit = new Setting<>("Inhibit", false).setDescription("Prevents attacks on crystals that would already be exploded").setParent(explode);
    public static Setting<Hand> explodeHand = new Setting<>("Hand", Hand.SYNC).setDescription("Hand to swing when exploding crystals").setParent(explode);
    public static Setting<Switch> explodeWeakness = new Setting<>("Weakness", Switch.NONE).setDescription("Switch to a tool when weakness is active").setParent(explode);

    // place category
    public static Setting<Boolean> place = new Setting<>("Place", true).setDescription("Place Crystals");
    public static Setting<Double> placeRange = new Setting<>("Range", 0.0, 5.0, 8.0, 1).setDescription("Range to place crystals").setParent(place);
    public static Setting<Double> placeWall = new Setting<>("WallRange", 0.0, 3.5, 8.0, 1).setDescription("Range to place crystals through walls").setParent(place);
    public static Setting<Double> placeDelay = new Setting<>("Delay", 0.0, 20.0, 500.0, 0).setDescription("Delay to place crystals").setParent(place);
    public static Setting<Double> placeDamage = new Setting<>("Damage", 0.0, 5.0, 36.0, 1).setDescription("Required damage to be considered for placement").setParent(place);
    public static Setting<Double> placeLocal = new Setting<>("LocalDamage", 0.0, 5.0, 36.0, 1).setDescription("Maximum allowed local damage to the player").setParent(place);
    public static Setting<Boolean> placePacket = new Setting<>("Packet", true).setDescription("Place with packets").setParent(place);
    public static Setting<Interact> placeInteraction = new Setting<>("Interact", Interact.NORMAL).setDescription("Limits the direction of placements").setParent(place);
    public static Setting<Raytrace> placeRaytrace = new Setting<>("Raytrace", Raytrace.DOUBLE).setDescription("Mode to verify placements through walls").setParent(place);
    public static Setting<Hand> placeHand = new Setting<>("Hand", Hand.SYNC).setDescription("Hand to swing when placing crystals").setParent(place);
    public static Setting<Switch> placeSwitch = new Setting<>("Switch", Switch.NONE).setDescription("Mode to use when switching to a crystal").setParent(place);

    // pause category
    public static Setting<Boolean> pause = new Setting<>("Pause", true).setDescription("When to pause");
    public static Setting<Double> pauseHealth = new Setting<>("Health", 0.0, 10.0, 36.0, 0).setDescription("Pause below this health").setParent(pause);
    public static Setting<Boolean> pauseSafety = new Setting<>("Safety", true).setDescription("Pause when the current crystal will kill you").setParent(pause);
    public static Setting<Boolean> pauseEating = new Setting<>("Eating", false).setDescription("Pause when eating").setParent(pause);
    public static Setting<Boolean> pauseMining = new Setting<>("Mining", false).setDescription("Pause when mining").setParent(pause);
    public static Setting<Boolean> pauseMending = new Setting<>("Mending", false).setDescription("Pause when mending").setParent(pause);

    // override category
    public static Setting<Boolean> override = new Setting<>("Override", true).setDescription("When to override minimum damage");
    public static Setting<Double> overrideHealth = new Setting<>("Health", 0.0, 10.0, 36.0, 0).setDescription("Override when target is below this health").setParent(override);
    public static Setting<Double> overrideThreshold = new Setting<>("Threshold", 0.0, 0.0, 4.0, 1).setDescription("Override if we can do lethal damage in this amount of crystals").setParent(override);
    public static Setting<Double> overrideArmor = new Setting<>("Armor", 0.0, 0.0, 100.0, 0).setDescription("Override when target's armor is below this percent").setParent(override);

    // rotate category
    public static Setting<Rotate> rotate = new Setting<>("Rotation", Rotate.NONE).setDescription("Mode for attack and placement rotation");
    public static Setting<Limit> rotateLimit = new Setting<>("Limit", Limit.NONE).setDescription("Mode for when to restrict rotations").setVisible(() -> rotate.getValue().equals(Rotate.PACKET)).setParent(rotate);
    public static Setting<When> rotateWhen = new Setting<>("When", When.BOTH).setDescription("Mode for when to rotate").setParent(rotate);
    public static Setting<Double> rotateRandom = new Setting<>("Random", 0.0, 0.0, 5.0, 1).setDescription("Randomize rotations to simulate real rotations").setParent(rotate);

    // calculations category
    public static Setting<Boolean> calculations = new Setting<>("Calculations", true).setDescription("Preferences for calculations");
    public static Setting<Timing> timing = new Setting<>("Timing", Timing.LINEAR).setDescription("Optimizes process at the cost of anti-cheat compatibility").setParent(calculations);
    public static Setting<Placements> placements = new Setting<>("Placements", Placements.NATIVE).setDescription("Placement calculations for current version").setParent(calculations);
    public static Setting<Logic> logic = new Setting<>("Logic", Logic.DAMAGE).setDescription("Logic for heuristic to prioritize").setParent(calculations);
    public static Setting<Sync> sync = new Setting<>("Sync", Sync.SOUND).setDescription("Sync for broken crystals").setParent(calculations);
    public static Setting<Boolean> prediction = new Setting<>("Prediction", false).setDescription("Attempts to account target's predicted position into the calculations").setParent(calculations);
    public static Setting<Boolean> ignoreTerrain = new Setting<>("IgnoreTerrain", false).setDescription("Ignores terrain when calculating damage").setParent(calculations);

    // target category
    public static Setting<Target> target = new Setting<>("Target", Target.CLOSEST).setDescription("Priority for searching target");
    public static Setting<Double> targetRange = new Setting<>("Range", 0.0, 10.0, 15.0, 1).setDescription("Range to consider an entity as a target").setParent(target);
    public static Setting<Boolean> targetPlayers = new Setting<>("Players", true).setDescription("Target players").setParent(target);
    public static Setting<Boolean> targetPassives = new Setting<>("Passives", false).setDescription("Target passives").setParent(target);
    public static Setting<Boolean> targetNeutrals = new Setting<>("Neutrals", false).setDescription("Target neutrals").setParent(target);
    public static Setting<Boolean> targetHostiles = new Setting<>("Hostiles", false).setDescription("Target hostiles").setParent(target);

    // render category
    public static Setting<Boolean> render = new Setting<>("Render", true).setDescription("Render a visual for calculated placement");
    public static Setting<Box> renderMode = new Setting<>("Mode", Box.BOTH).setDescription("Style for visual").setExclusion(Box.GLOW, Box.REVERSE).setParent(render);
    public static Setting<Text> renderText = new Setting<>("Text", Text.NONE).setDescription("Text for the visual").setParent(render);
    public static Setting<Double> renderWidth = new Setting<>("Width", 0.0, 1.5, 3.0, 1).setDescription( "Line width for the visual").setVisible(() -> renderMode.getValue().equals(Box.BOTH) || renderMode.getValue().equals(Box.CLAW) || renderMode.getValue().equals(Box.OUTLINE)).setParent(render);

    // crystal info
    private Crystal explodeCrystal = new Crystal(null, 0, 0);
    private final Timer explodeTimer = new Timer();
    private final Timer switchTimer = new Timer();
    private final Map<Integer, Integer> attemptedExplosions = new ConcurrentHashMap<>();
    private final Set<EntityEnderCrystal> inhibitExplosions = new ConcurrentSet<>();

    // placement info
    private static CrystalPosition placePosition = new CrystalPosition(BlockPos.ORIGIN, null, 0, 0);
    private final Timer placeTimer = new Timer();
    private final Map<BlockPos, Integer> attemptedPlacements = new ConcurrentHashMap<>();

    // tick clamp
    private int strictTicks;

    // rotation info
    private boolean yawLimit;
    private Vec3d interactVector = Vec3d.ZERO;

    // switch info
    private EnumHand previousHand;
    private int previousSlot = -1;

    // response time
    private long startTime = 0;
    private static double responseTime = 0;

    @Override
    public void onUpdate() {
        // check tick clearance
        if (strictTicks > 0) {
            strictTicks--;
        }

        else {
            // explode our searched crystal and place on our search position
            explodeCrystal();
            placeCrystal();
        }
    }

    @Override
    public void onThread() {
        if (pause.getValue()) {
            // if we are preforming certain actions, reset the process
            if (PlayerUtil.isEating() && pauseEating.getValue() || PlayerUtil.isMining() && pauseMining.getValue() || PlayerUtil.isMending() && pauseMending.getValue()) {
                resetProcess();
                return;
            }

            // if we are below our pause health, reset the process
            if (PlayerUtil.getHealth() < pauseHealth.getValue() && !mc.player.capabilities.isCreativeMode) {
                resetProcess();
                return;
            }

            // make sure we are not already auto-placing
            if (HoleFill.INSTANCE.isActive() || Burrow.INSTANCE.isActive()) {
                resetProcess();
                return;
            }
        }

        // search our crystal and placement on a thread ahead of time
        explodeCrystal = searchCrystal();
        placePosition = searchPosition();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // reset our process for next enable
        resetProcess();
    }

    /**
     * Explodes the ideal crystal
     */
    public void explodeCrystal() {
        if (explodeCrystal != null) {
            if (!rotate.getValue().equals(Rotate.NONE) && (rotateWhen.getValue().equals(When.BREAK) || rotateWhen.getValue().equals(When.BOTH))) {
                // our last interaction will be the attack on the crystal
                interactVector = explodeCrystal.getCrystal().getPositionVector();

                if (rotate.getValue().equals(Rotate.CLIENT)) {
                    float[] explodeAngles = AngleUtil.calculateAngles(interactVector);

                    // update our players rotation
                    mc.player.rotationYaw = explodeAngles[0];
                    mc.player.rotationYawHead = explodeAngles[0];
                    mc.player.rotationPitch = explodeAngles[1];
                }
            }

            if (!explodeWeakness.getValue().equals(Switch.NONE)) {
                // strength and weakness effects on the player
                PotionEffect weaknessEffect = mc.player.getActivePotionEffect(MobEffects.WEAKNESS);
                PotionEffect strengthEffect = mc.player.getActivePotionEffect(MobEffects.STRENGTH);

                // verify that we cannot break the crystal due to weakness
                if (weaknessEffect != null && (strengthEffect == null || strengthEffect.getAmplifier() < weaknessEffect.getAmplifier())) {
                    // find the slots of our tools
                    int swordSlot = InventoryUtil.getItemSlot(Items.DIAMOND_SWORD, Inventory.HOTBAR);
                    int pickSlot = InventoryUtil.getItemSlot(Items.DIAMOND_SWORD, Inventory.HOTBAR);

                    if (!InventoryUtil.isHolding(Items.DIAMOND_SWORD) || !InventoryUtil.isHolding(Items.DIAMOND_PICKAXE)) {
                        // log the previous slot
                        previousSlot = mc.player.inventory.currentItem;

                        // prefer the sword over a pickaxe
                        if (swordSlot != -1) {
                            InventoryUtil.switchToSlot(swordSlot, explodeWeakness.getValue());
                        }

                        else if (pickSlot != -1) {
                            InventoryUtil.switchToSlot(pickSlot, explodeWeakness.getValue());
                        }
                    }
                }
            }

            if (explodeTimer.passedTime(explodeDelay.getValue().longValue() + ThreadLocalRandom.current().nextInt(explodeRandom.getValue().intValue() + 1), Format.MILLISECONDS) && switchTimer.passedTime(explodeSwitch.getValue().longValue(), Format.MILLISECONDS)) {
                // explode the crystal
                explodeCrystal(explodeCrystal.getCrystal(), explodePacket.getValue());
                swingArm(explodeHand.getValue());

                explodeTimer.resetTime();

                // add crystal to our list of attempted explosions and resetTime the clearance
                attemptedExplosions.put(explodeCrystal.getCrystal().getEntityId(), attemptedExplosions.containsKey(explodeCrystal.getCrystal().getEntityId()) ? attemptedExplosions.get(explodeCrystal.getCrystal().getEntityId()) + 1 : 1);

                // remove the crystal after we break -> i.e. instantly
                if (sync.getValue().equals(Sync.INSTANT)) {
                    mc.world.removeEntityDangerously(explodeCrystal.getCrystal());
                }

                // switch to our previous slot
                if (previousSlot != -1) {
                    InventoryUtil.switchToSlot(previousSlot, explodeWeakness.getValue());

                    // reset our previous slot
                    previousSlot = -1;
                }
            }
        }
    }

    /**
     * Places a crystal at the searched placement position
     */
    public void placeCrystal() {
        if (placePosition != null) {
            if (!rotate.getValue().equals(Rotate.NONE) && (rotateWhen.getValue().equals(When.PLACE) || rotateWhen.getValue().equals(When.BOTH))) {
                // our last interaction will be the placement on the block
                interactVector = new Vec3d(placePosition.getPosition()).addVector(0.5, 0.5, 0.5);

                if (rotate.getValue().equals(Rotate.CLIENT)) {
                    float[] placeAngles = AngleUtil.calculateAngles(interactVector);

                    // update our players rotation
                    mc.player.rotationYaw = placeAngles[0];
                    mc.player.rotationYawHead = placeAngles[0];
                    mc.player.rotationPitch = placeAngles[1];
                }
            }

            // log our previous slot and hand, we'll switch back after placing
            if (placeSwitch.getValue().equals(Switch.PACKET)) {
                previousSlot = mc.player.inventory.currentItem;

                if (mc.player.isHandActive()) {
                    previousHand = mc.player.getActiveHand();
                }
            }

            // switch to crystals if needed
            InventoryUtil.switchToSlot(Items.END_CRYSTAL, placeSwitch.getValue());

            if (placeTimer.passedTime(placeDelay.getValue().longValue(), Format.MILLISECONDS) && (InventoryUtil.isHolding(Items.END_CRYSTAL) || placeSwitch.getValue().equals(Switch.PACKET))) {
                // directions of placement
                double facingX = 0;
                double facingY = 0;
                double facingZ = 0;

                // assume the face is visible
                EnumFacing facingDirection = EnumFacing.UP;

                // the angles to the last interaction
                float[] vectorAngles = AngleUtil.calculateAngles(interactVector);

                // vector from the angles
                Vec3d placeVector = AngleUtil.getVectorForRotation(new Rotation(vectorAngles[0], vectorAngles[1]));
                RayTraceResult vectorResult = mc.world.rayTraceBlocks(mc.player.getPositionEyes(1), mc.player.getPositionEyes(1).addVector(placeVector.x * placeRange.getValue(), placeVector.y * placeRange.getValue(), placeVector.z * placeRange.getValue()), false, false, true);

                // make sure the direction we are facing is consistent with our rotations
                switch (placeInteraction.getValue()) {
                    case NONE:
                        facingDirection = EnumFacing.DOWN;
                        facingX = 0.5;
                        facingY = 0.5;
                        facingZ = 0.5;
                        break;
                    case NORMAL:
                        // find the direction to place against
                        RayTraceResult laxResult = mc.world.rayTraceBlocks(mc.player.getPositionEyes(1), interactVector);

                        if (laxResult != null && laxResult.typeOfHit.equals(RayTraceResult.Type.BLOCK)) {
                            facingDirection = laxResult.sideHit;

                            // if we're at world height, we can still place a crystal if we interact with the bottom of the block, this doesn't work on strict servers
                            if (placePosition.getPosition().getY() >= (mc.world.getHeight() - 1)) {
                                facingDirection = EnumFacing.DOWN;
                            }
                        }

                        // find rotations based on the placement
                        if (vectorResult != null && vectorResult.hitVec != null) {
                            facingX = vectorResult.hitVec.x - placePosition.getPosition().getX();
                            facingY = vectorResult.hitVec.y - placePosition.getPosition().getY();
                            facingZ = vectorResult.hitVec.z - placePosition.getPosition().getZ();
                        }

                        break;
                    case STRICT:
                        // if the place position is likely out of sight
                        if (placePosition.getPosition().getY() > mc.player.posY + mc.player.getEyeHeight()) {
                            // the place vectors lowest bounds
                            Vec3d strictVector = new Vec3d(placePosition.getPosition());

                            // our nearest visible face
                            double nearestFace = Double.MAX_VALUE;

                            // iterate through all points on the block
                            for (float x = 0; x <= 1; x += 0.05) {
                                for (float y = 0; y <= 1; y += 0.05) {
                                    for (float z = 0; z <= 1; z += 0.05) {
                                        // find the vector to raytrace to
                                        Vec3d traceVector = strictVector.addVector(x, y, z);

                                        // check visibility, raytrace to the current point
                                        RayTraceResult strictResult = mc.world.rayTraceBlocks(mc.player.getPositionEyes(1), traceVector, false, true, false);

                                        // if our raytrace is a block, check distances
                                        if (strictResult != null && strictResult.typeOfHit.equals(RayTraceResult.Type.BLOCK)) {
                                            // distance to face
                                            double faceDistance = mc.player.getDistance(traceVector.x, traceVector.y, traceVector.z);

                                            // if the face is the closest to the player and trace distance is reasonably close, then we have found a new ideal visible side to place against
                                            if (faceDistance < nearestFace) {
                                                facingDirection = strictResult.sideHit;
                                                nearestFace = faceDistance;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // find rotations based on the placement
                        if (vectorResult != null && vectorResult.hitVec != null) {
                            facingX = vectorResult.hitVec.x - placePosition.getPosition().getX();
                            facingY = vectorResult.hitVec.y - placePosition.getPosition().getY();
                            facingZ = vectorResult.hitVec.z - placePosition.getPosition().getZ();
                        }

                        break;
                }

                // place the crystal
                placeCrystal(placePosition.getPosition(), facingDirection, new Vec3d(facingX, facingY, facingZ), placePacket.getValue());
                swingArm(placeHand.getValue());

                // switch back after placing, should only switch serverside
                if (placeSwitch.getValue().equals(Switch.PACKET)) {
                    InventoryUtil.switchToSlot(previousSlot, placeSwitch.getValue());

                    if (previousHand != null) {
                        mc.player.setActiveHand(previousHand);
                    }

                    // reset previous slot
                    previousSlot = -1;
                }

                placeTimer.resetTime();

                // add placement to our list of attempted placement and resetTime clearance
                attemptedPlacements.put(placePosition.getPosition(), attemptedPlacements.containsKey(placePosition.getPosition()) ? attemptedPlacements.get(placePosition.getPosition()) + 1 : 1);
            }
        }
    }

    /**
     * Searches the ideal crystal explosion
     * @return The ideal crystal explosion
     */
    public Crystal searchCrystal() {
        if (explode.getValue()) {
            // map of viable crystals
            TreeMap<Float, Crystal> crystalMap = new TreeMap<>();

            for (Entity calculatedCrystal : mc.world.loadedEntityList) {
                // make sure it's a viable crystal
                if (!(calculatedCrystal instanceof EntityEnderCrystal) || calculatedCrystal.isDead) {
                    continue;
                }

                // make sure it's in range
                float distance = mc.player.getDistance(calculatedCrystal);
                if (distance > explodeRange.getValue() || (!mc.player.canEntityBeSeen(calculatedCrystal) && distance > explodeWall.getValue())) {
                    continue;
                }

                // crystals that would already be exploded, don't need to be attacked
                if (inhibitExplosions.contains(calculatedCrystal) && explodeInhibit.getValue()) {
                    continue;
                }

                // make sure the crystal has existed in the world for a certain number of ticks before it's a viable target
                if (calculatedCrystal.ticksExisted < explodeTicksExisted.getValue().intValue()) {
                    continue;
                }

                // make sure it doesn't do too much dmg to us or kill us
                float localDamage = mc.player.capabilities.isCreativeMode ? 0 : ExplosionUtil.getDamageFromExplosion(calculatedCrystal.posX, calculatedCrystal.posY, calculatedCrystal.posZ, mc.player, ignoreTerrain.getValue(), false);
                if (localDamage > explodeLocal.getValue() || (localDamage + 1 > PlayerUtil.getHealth() && pauseSafety.getValue())) {
                    continue;
                }

                // check if we've attacked this crystal too many times
                if (attemptedExplosions.containsKey(calculatedCrystal.getEntityId()) && explodeLimit.getValue() < 10) {
                    if (attemptedExplosions.get(calculatedCrystal.getEntityId()) > explodeLimit.getValue()) {
                        continue;
                    }
                }

                for (EntityPlayer calculatedTarget : mc.world.playerEntities) {
                    // make sure the target is not dead, a friend, or the local player
                    if (calculatedTarget.equals(mc.player) || EnemyUtil.isDead(calculatedTarget) || Cosmos.INSTANCE.getSocialManager().getSocial(calculatedTarget.getName()).equals(Relationship.FRIEND)) {
                        continue;
                    }

                    // make sure target's within our specified target range
                    float targetDistance = mc.player.getDistance(calculatedTarget);
                    if (targetDistance > targetRange.getValue()) {
                        continue;
                    }

                    // calculate the damage this crystal will do to each target, we can verify if it meets our requirements later
                    float targetDamage = ExplosionUtil.getDamageFromExplosion(calculatedCrystal.posX, calculatedCrystal.posY, calculatedCrystal.posZ, calculatedTarget, ignoreTerrain.getValue(), prediction.getValue());

                    // scale based on our damage heuristic
                    float damageHeuristic;
                    switch (logic.getValue()) {
                        case DAMAGE:
                        default:
                            damageHeuristic = targetDamage;
                            break;
                        case MINIMAX:
                            damageHeuristic = targetDamage - localDamage;
                            break;
                        case UNIFORM:
                            damageHeuristic = targetDamage - localDamage - distance;
                            break;
                    }

                    // add it to our list of viable crystals
                    crystalMap.put(damageHeuristic, new Crystal((EntityEnderCrystal) calculatedCrystal, targetDamage, localDamage));
                }
            }

            if (!crystalMap.isEmpty()) {
                // in the map, the best crystal will be the last entry
                Crystal idealCrystal = crystalMap.lastEntry().getValue();

                // verify if the ideal crystal meets our requirements, if it doesn't it automatically rules out all other crystals
                if (idealCrystal.getTargetDamage() >= explodeDamage.getValue()) {
                    return idealCrystal;
                }
            }
        }

        // we did not find a crystal
        return null;
    }

    /**
     * Searches the ideal crystal placement
     * @return The ideal crystal placement
     */
    public CrystalPosition searchPosition() {
        if (place.getValue()) {
            // map of viable positions
            TreeMap<Float, CrystalPosition> positionMap = new TreeMap<>();

            for (BlockPos calculatedPosition : BlockUtil.getSurroundingBlocks(mc.player, placeRange.getValue(), false)) {
                // make sure it's actually a viable position
                if (!canPlaceCrystal(calculatedPosition)) {
                    continue;
                }

                // make sure it doesn't do too much dmg to us or kill us
                float localDamage = mc.player.capabilities.isCreativeMode ? 0 : ExplosionUtil.getDamageFromExplosion(calculatedPosition.getX() + 0.5, calculatedPosition.getY() + 1, calculatedPosition.getZ() + 0.5, mc.player, ignoreTerrain.getValue(), false);
                if (localDamage > placeLocal.getValue() || (localDamage + 1 > PlayerUtil.getHealth() && pauseSafety.getValue())) {
                    continue;
                }

                // if the block above the one we can't see through is air, then NCP won't flag us for placing at normal ranges
                boolean wallPlacement = !placeRaytrace.getValue().equals(Raytrace.NONE) && RaytraceUtil.raytraceBlock(calculatedPosition, placeRaytrace.getValue());

                // position to calculate distances to
                Vec3d distancePosition = new Vec3d(calculatedPosition.getX() + 0.5, calculatedPosition.getY() + 1, calculatedPosition.getZ() + 0.5);

                // if the block is higher than the player, use the bottom of the block for ranges
                if (calculatedPosition.getY() > mc.player.posY + mc.player.eyeHeight) {
                    distancePosition = new Vec3d(calculatedPosition.getX() + 0.5, calculatedPosition.getY(), calculatedPosition.getZ() + 0.5);
                }

                // if it is a wall placement, use our wall ranges
                double distance = mc.player.getDistance(distancePosition.x, distancePosition.y, distancePosition.z);
                if (distance > placeWall.getValue() && wallPlacement) {
                    continue;
                }

                for (EntityPlayer calculatedTarget : mc.world.playerEntities) {
                    // make sure the target is not dead, a friend, or the local player
                    if (calculatedTarget.equals(mc.player) || EnemyUtil.isDead(calculatedTarget) || Cosmos.INSTANCE.getSocialManager().getSocial(calculatedTarget.getName()).equals(Relationship.FRIEND)) {
                        continue;
                    }

                    // make sure target's within our specified target range
                    float targetDistance = mc.player.getDistance(calculatedTarget);
                    if (targetDistance > targetRange.getValue()) {
                        continue;
                    }

                    // calculate the damage this position will do to each target, we can verify if it meets our requirements later
                    float targetDamage = ExplosionUtil.getDamageFromExplosion(calculatedPosition.getX() + 0.5, calculatedPosition.getY() + 1, calculatedPosition.getZ() + 0.5, calculatedTarget, ignoreTerrain.getValue(), prediction.getValue());

                    // scale based on our damage heuristic
                    float damageHeuristic;
                    switch (logic.getValue()) {
                        case DAMAGE:
                        default:
                            damageHeuristic = targetDamage;
                            break;
                        case MINIMAX:
                            damageHeuristic = targetDamage - localDamage;
                            break;
                        case UNIFORM:
                            damageHeuristic = (float) (targetDamage - localDamage - distance);
                            break;
                    }

                    // add it to our list of viable positions
                    positionMap.put(damageHeuristic, new CrystalPosition(calculatedPosition, calculatedTarget, targetDamage, localDamage));
                }
            }

            if (!positionMap.isEmpty()) {
                // in the map, the best position will be the last entry
                CrystalPosition idealPosition = positionMap.lastEntry().getValue();

                // required damage for it the placement to be continued
                double requiredDamage = placeDamage.getValue();

                // find out if we need to override our min dmg, 2 sounds like a good number for face placing but might need to be lower
                if (override.getValue()) {
                    if (idealPosition.getTargetDamage() * overrideThreshold.getValue() >= EnemyUtil.getHealth(idealPosition.getPlaceTarget())) {
                        requiredDamage = 0.5;
                    }

                    if (getCosmos().getHoleManager().isInHole(idealPosition.getPlaceTarget())) {
                        if (EnemyUtil.getHealth(idealPosition.getPlaceTarget()) < overrideHealth.getValue()) {
                            requiredDamage = 0.5;
                        }

                        if (EnemyUtil.getArmor(idealPosition.getPlaceTarget(), overrideArmor.getValue())) {
                            requiredDamage = 0.5;
                        }
                    }
                }

                // verify if the ideal position meets our requirements, if it doesn't it automatically rules out all other placements
                if (idealPosition.getTargetDamage() > requiredDamage) {
                    return idealPosition;
                }
            }
        }

        // we did not find a placement
        return null;
    }

    @Override
    public boolean isActive() {
        return isEnabled() && (placePosition != null || explodeCrystal != null);
    }

    @Override
    public void onRender3D() {
        if (render.getValue() && placePosition != null && !placePosition.getPosition().equals(BlockPos.ORIGIN) && (InventoryUtil.isHolding(Items.END_CRYSTAL) || placeSwitch.getValue().equals(Switch.PACKET))) {
            // draw a box at the position
            RenderUtil.drawBox(new RenderBuilder()
                    .position(placePosition.getPosition())
                    .color(ColorUtil.getPrimaryAlphaColor(60))
                    .box(renderMode.getValue())
                    .setup()
                    .line(renderWidth.getValue().floatValue())
                    .cull(renderMode.getValue().equals(Box.GLOW) || renderMode.getValue().equals(Box.REVERSE))
                    .shade(renderMode.getValue().equals(Box.GLOW) || renderMode.getValue().equals(Box.REVERSE))
                    .alpha(renderMode.getValue().equals(Box.GLOW) || renderMode.getValue().equals(Box.REVERSE))
                    .depth(true)
                    .blend()
                    .texture()
            );

            // placement nametags
            if (!renderText.getValue().equals(Text.NONE)) {

                // placement info
                String placementInfo;

                // damage info rounded
                double targetDamageRounded = MathUtil.roundDouble(placePosition.getTargetDamage(), 1);
                double localDamageRounded = MathUtil.roundDouble(placePosition.getLocalDamage(), 1);

                // get placement text
                switch (renderText.getValue()) {
                    case TARGET:
                        placementInfo = String.valueOf(targetDamageRounded);
                        break;
                    case SELF:
                        placementInfo = String.valueOf(localDamageRounded);
                        break;
                    case BOTH:
                        placementInfo = "Target: " + targetDamageRounded + ", Self: " + localDamageRounded;
                        break;
                    case NONE:
                    default:
                        placementInfo = "";
                        break;
                }

                // draw the placement info
                RenderUtil.drawNametag(placePosition.getPosition(), 0.5F, placementInfo);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRotationUpdate(RotationUpdateEvent event) {
        if (isActive() && rotate.getValue().equals(Rotate.PACKET)) {
            // cancel the existing rotations, we'll send our own
            event.setCanceled(true);

            // angles to the interactVector
            float[] packetAngles = AngleUtil.calculateAngles(interactVector);

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
            getCosmos().getRotationManager().addRotation(new Rotation(packetAngles[0], packetAngles[1]), Integer.MAX_VALUE);
        }
    }

    @SubscribeEvent
    public void onRenderRotations(RenderRotationsEvent event) {
        if (isActive() && rotate.getValue().equals(Rotate.PACKET)) {
            // cancel the model rendering for rotations, we'll set it to our values
            event.setCanceled(true);

            // find the angles from our interaction
            float[] packetAngles = AngleUtil.calculateAngles(interactVector);
            if (rotateRandom.getValue() > 0) {
                Random randomAngle = new Random();
                packetAngles[0] += randomAngle.nextFloat() * (randomAngle.nextBoolean() ? rotateRandom.getValue() : -rotateRandom.getValue());
            }

            // set our model angles; visual
            event.setYaw(packetAngles[0]);
            event.setPitch(packetAngles[1]);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketHeldItemChange) {
            // reset our switch time, we just switched
            switchTimer.resetTime();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {
        // packet for crystal spawns
        if (event.getPacket() instanceof SPacketSpawnObject && ((SPacketSpawnObject) event.getPacket()).getType() == 51) {

            // position of the placed crystal
            BlockPos linearPosition = new BlockPos(((SPacketSpawnObject) event.getPacket()).getX(), ((SPacketSpawnObject) event.getPacket()).getY(), ((SPacketSpawnObject) event.getPacket()).getZ());

            // our place position
            if (attemptedPlacements.containsKey(linearPosition.down())) {
                // mark the place time
                startTime = System.currentTimeMillis();

                // since it's been confirmed that the crystal spawned, we can move on to our next process
                if (timing.getValue().equals(Timing.SEQUENTIAL)) {
                    // clear our timer
                    if (!explodeTimer.passedTime(explodeDelay.getValue().longValue(), Format.MILLISECONDS)) {
                        explodeTimer.setTime(explodeDelay.getValue().longValue(), Format.MILLISECONDS);
                    }
                }

                // clear our attempts
                attemptedPlacements.clear();
            }

            if ((timing.getValue().equals(Timing.LINEAR) || timing.getValue().equals(Timing.UNIFORM)) && explode.getValue()) {
                // if the block above the one we can't see through is air, then NCP won't flag us for placing at normal ranges
                boolean wallLinear = !placeRaytrace.getValue().equals(Raytrace.NONE) && RaytraceUtil.raytraceBlock(linearPosition, placeRaytrace.getValue());

                // if it is a wall placement, use our wall ranges
                double distance = mc.player.getDistance(linearPosition.getX() + 0.5, linearPosition.getY() + 1, linearPosition.getZ() + 0.5);
                if (distance > explodeWall.getValue() && wallLinear) {
                    return;
                }

                // make sure it doesn't do too much dmg to us or kill us
                float localDamage = ExplosionUtil.getDamageFromExplosion(linearPosition.getX() + 0.5, linearPosition.getY() + 1, linearPosition.getZ() + 0.5, mc.player, ignoreTerrain.getValue(), false);
                if (localDamage > explodeLocal.getValue() || (localDamage + 1 > PlayerUtil.getHealth() && pauseSafety.getValue())) {
                    return;
                }

                TreeMap<Float, Integer> linearMap = new TreeMap<>();
                for (EntityPlayer calculatedTarget : mc.world.playerEntities) {
                    // make sure the target is not dead or the local player
                    if (calculatedTarget.equals(mc.player) || EnemyUtil.isDead(calculatedTarget)) {
                        continue;
                    }

                    // make sure target's within our specified target range
                    float targetDistance = mc.player.getDistance(calculatedTarget);
                    if (targetDistance > targetRange.getValue()) {
                        continue;
                    }

                    // calculate the damage this crystal will do to each target, we can verify if it meets our requirements later
                    float targetDamage = ExplosionUtil.getDamageFromExplosion(linearPosition.getX() + 0.5, linearPosition.getY() + 1, linearPosition.getZ() + 0.5, calculatedTarget, ignoreTerrain.getValue(), false);

                    // scale based on our damage heuristic
                    float damageHeuristic;
                    switch (logic.getValue()) {
                        case DAMAGE:
                        default:
                            damageHeuristic = targetDamage;
                            break;
                        case MINIMAX:
                            damageHeuristic = targetDamage - localDamage;
                            break;
                        case UNIFORM:
                            damageHeuristic = (float) (targetDamage - localDamage - distance);
                            break;
                    }

                    // add the linear crystal to our map
                    linearMap.put(damageHeuristic, ((SPacketSpawnObject) event.getPacket()).getEntityID());
                }

                if (!linearMap.isEmpty()) {
                    Map.Entry<Float, Integer> idealLinear = linearMap.lastEntry();

                    // make sure it meets requirements
                    if (idealLinear.getKey() > explodeDamage.getValue()) {
                        if (!rotate.getValue().equals(Rotate.NONE) && (rotateWhen.getValue().equals(When.BREAK) || rotateWhen.getValue().equals(When.BOTH))) {
                            // our last interaction will be the attack on the crystal
                            interactVector = new Vec3d(linearPosition).addVector(0.5, 0.5, 0.5);

                            if (rotate.getValue().equals(Rotate.CLIENT)) {
                                float[] linearAngles = AngleUtil.calculateAngles(interactVector);

                                // update our players rotation
                                mc.player.rotationYaw = linearAngles[0];
                                mc.player.rotationYawHead = linearAngles[0];
                                mc.player.rotationPitch = linearAngles[1];
                            }
                        }

                        if (!explodeWeakness.getValue().equals(Switch.NONE)) {
                            // strength and weakness effects on the player
                            PotionEffect weaknessEffect = mc.player.getActivePotionEffect(MobEffects.WEAKNESS);
                            PotionEffect strengthEffect = mc.player.getActivePotionEffect(MobEffects.STRENGTH);

                            // verify that we cannot break the crystal due to weakness
                            if (weaknessEffect != null && (strengthEffect == null || strengthEffect.getAmplifier() < weaknessEffect.getAmplifier())) {
                                // find the slots of our tools
                                int swordSlot = InventoryUtil.getItemSlot(Items.DIAMOND_SWORD, Inventory.HOTBAR);
                                int pickSlot = InventoryUtil.getItemSlot(Items.DIAMOND_SWORD, Inventory.HOTBAR);

                                if (!InventoryUtil.isHolding(Items.DIAMOND_SWORD) || !InventoryUtil.isHolding(Items.DIAMOND_PICKAXE)) {
                                    // log the previous slot
                                    previousSlot = mc.player.inventory.currentItem;

                                    // prefer the sword over a pickaxe
                                    if (swordSlot != -1) {
                                        InventoryUtil.switchToSlot(swordSlot, explodeWeakness.getValue());
                                    }

                                    else if (pickSlot != -1) {
                                        InventoryUtil.switchToSlot(pickSlot, explodeWeakness.getValue());
                                    }
                                }
                            }
                        }

                        // explode the linear crystal
                        explodeCrystal(idealLinear.getValue());
                        swingArm(explodeHand.getValue());

                        // add crystal to our list of attempted explosions
                        attemptedExplosions.put(((SPacketSpawnObject) event.getPacket()).getEntityID(), attemptedExplosions.containsKey(((SPacketSpawnObject) event.getPacket()).getEntityID()) ? attemptedExplosions.get(((SPacketSpawnObject) event.getPacket()).getEntityID()) + 1 : 1);

                        // remove the crystal after we break -> i.e. instantly
                        if (sync.getValue().equals(Sync.INSTANT)) {
                            mc.world.removeEntityFromWorld(idealLinear.getValue());
                        }

                        // switch to our previous slot
                        if (previousSlot != -1) {
                            InventoryUtil.switchToSlot(previousSlot, explodeWeakness.getValue());

                            // reset our previous slot
                            previousSlot = -1;
                        }
                    }
                }
            }
        }

        // packet that confirms entity removal
        if (event.getPacket() instanceof SPacketDestroyEntities) {
            for (int entityId : ((SPacketDestroyEntities) event.getPacket()).getEntityIDs()) {
                if (attemptedExplosions.containsKey(entityId)) {
                    // time since place
                    responseTime = System.currentTimeMillis() - startTime;

                    // since it's been confirmed that the crystal exploded, we can move on to our next process
                    if (timing.getValue().equals(Timing.SEQUENTIAL) || timing.getValue().equals(Timing.UNIFORM)) {
                        // clear our timer
                        if (!placeTimer.passedTime(placeDelay.getValue().longValue(), Format.MILLISECONDS)) {
                            placeTimer.setTime(placeDelay.getValue().longValue(), Format.MILLISECONDS);
                        }
                    }

                    // clear our attempts
                    attemptedExplosions.clear();
                    break;
                }
            }
        }

        // packet for crystal explosions
        if (event.getPacket() instanceof SPacketSoundEffect && ((SPacketSoundEffect) event.getPacket()).getSound().equals(SoundEvents.ENTITY_GENERIC_EXPLODE) && ((SPacketSoundEffect) event.getPacket()).getCategory().equals(SoundCategory.BLOCKS)) {
            // clear our old inhibit entities
            inhibitExplosions.clear();

            // schedule to main mc thread
            mc.addScheduledTask(() -> {
                for (Iterator<Entity> entityList = mc.world.loadedEntityList.iterator(); entityList.hasNext();) {
                    // next entity in the world
                    Entity entity = entityList.next();

                    // make sure it's a crystal
                    if (!(entity instanceof EntityEnderCrystal) || entity.isDead) {
                        continue;
                    }

                    // make sure the crystal is in range from the sound to be destroyed
                    double soundDistance = entity.getDistance(((SPacketSoundEffect) event.getPacket()).getX(), ((SPacketSoundEffect) event.getPacket()).getY(), ((SPacketSoundEffect) event.getPacket()).getZ());
                    if (soundDistance > 6) {
                        continue;
                    }

                    // going to be exploded anyway, so don't attempt explosion
                    if (explodeInhibit.getValue()) {
                        inhibitExplosions.add((EntityEnderCrystal) entity);
                    }

                    // the world sets the crystal dead one tick after this packet, but we can speed up the placements by setting it dead here
                    if (sync.getValue().equals(Sync.SOUND)) {
                        mc.world.removeEntityDangerously(entity);
                    }
                }
            });
        }
    }

    /**
     * Places a crystal at a specified position
     * @param position The position to place on
     * @param facing The block direction to place against
     * @param vector The vector to face when placing
     * @param packet Whether or not to place with a packet
     */
    public void placeCrystal(BlockPos position, EnumFacing facing, Vec3d vector, boolean packet) {
        if (position != null) {
            if (packet) {
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(position, facing, mc.player.getHeldItemMainhand().getItem().equals(Items.END_CRYSTAL) || placeSwitch.getValue().equals(Switch.PACKET) ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND, (float) vector.x, (float) vector.y, (float) vector.z));
            }

            else {
                mc.playerController.processRightClickBlock(mc.player, mc.world, position, facing, vector, mc.player.getHeldItemMainhand().getItem().equals(Items.END_CRYSTAL) || placeSwitch.getValue().equals(Switch.PACKET) ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND);
            }
        }
    }

    /**
     * Explodes a crystal
     * @param crystal The crystal to explode
     * @param packet Whether or not to explode with packet
     */
    public void explodeCrystal(EntityEnderCrystal crystal, boolean packet) {
        if (crystal != null) {
            if (packet) {
                mc.player.connection.sendPacket(new CPacketUseEntity(crystal));
            }

            else {
                mc.playerController.attackEntity(mc.player, crystal);
            }
        }
    }

    /**
     * Attacks an entity based on the entityID
     * @param entityID The entityID of the entity to attack, always a packet attack
     */
    @SuppressWarnings("all")
    public void explodeCrystal(int entityID) {
        CPacketUseEntity attackPacket = new CPacketUseEntity();
        ((ICPacketUseEntity) attackPacket).setID(entityID);
        ((ICPacketUseEntity) attackPacket).setAction(CPacketUseEntity.Action.ATTACK);
        mc.player.connection.sendPacket(attackPacket);
    }

    /**
     * Swings the player's arm
     * @param hand The hand to swing
     */
    public void swingArm(Hand hand) {
        switch (hand) {
            // swing arm based on hand
            case SYNC:
                if (mc.player.getHeldItemMainhand().getItem().equals(Items.END_CRYSTAL) || placeSwitch.getValue().equals(Switch.PACKET)) {
                    mc.player.swingArm(EnumHand.MAIN_HAND);
                }

                else if (mc.player.getHeldItemOffhand().getItem().equals(Items.END_CRYSTAL)) {
                    mc.player.swingArm(EnumHand.OFF_HAND);
                }

                break;
            case MAINHAND:
                mc.player.swingArm(EnumHand.MAIN_HAND);
                break;
            case OFFHAND:
                mc.player.swingArm(EnumHand.OFF_HAND);
                break;
            case PACKET:
                mc.player.connection.sendPacket(new CPacketAnimation(mc.player.getHeldItemMainhand().getItem().equals(Items.END_CRYSTAL) || placeSwitch.getValue().equals(Switch.PACKET) ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND));
                break;
            case NONE:
                break;
        }
    }

    /**
     * Finds whether or not a crystal can be placed on a specified block
     * @param position The specified block to check if a crystal can be placed
     * @return Whether or not a crystal can be placed at the location
     */
    public boolean canPlaceCrystal(BlockPos position) {
        // crystals can only be placed on Obsidian and Bedrock
        if (!mc.world.getBlockState(position).getBlock().equals(Blocks.BEDROCK) && !mc.world.getBlockState(position).getBlock().equals(Blocks.OBSIDIAN)) {
            return false;
        }

        // the relative positions to check for air or fire, crystals can be placed on fire
        BlockPos nativePosition = position.add(0, 1, 0);
        BlockPos updatedPosition = position.add(0, 2, 0);

        // check if the native position is air or fire
        if (!mc.world.isAirBlock(nativePosition) && !mc.world.getBlockState(nativePosition).getBlock().equals(Blocks.FIRE)) {
            return false;
        }

        // check if the updated position is air or fire
        if (placements.getValue().equals(Placements.NATIVE)) {
            if (!mc.world.isAirBlock(updatedPosition) && !mc.world.getBlockState(updatedPosition).getBlock().equals(Blocks.FIRE)) {
                return false;
            }
        }

        // check for any unsafe entities in the position
        int unsafeEntities = 0;
        for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(nativePosition))) {
            // if the entity is crystal, check it's on the same position
            if (entity instanceof EntityEnderCrystal && entity.getPosition().equals(nativePosition)) {
                continue;
            }

            // if the entity will be removed the next tick, we can still place here
            if (entity.isDead) {
                continue;
            }

            unsafeEntities++;
        }

        // make sure there are not unsafe entities at the place position
        return unsafeEntities <= 0;
    }

    /**
     * Reset all variables, timers, and lists
     */
    public void resetProcess() {
        explodeCrystal = new Crystal(null, 0, 0);
        placePosition = new CrystalPosition(BlockPos.ORIGIN, null, 0, 0);
        interactVector = Vec3d.ZERO;
        yawLimit = false;
        previousHand = null;
        previousSlot = -1;
        strictTicks = 0;
        startTime = 0;
        responseTime = 0;
        placeTimer.resetTime();
        explodeTimer.resetTime();
        attemptedExplosions.clear();
        attemptedPlacements.clear();
        inhibitExplosions.clear();
    }

    public enum Placements {

        /**
         * Crystal placements for version 1.12.2
         */
        NATIVE,

        /**
         * Crystal placements for version 1.13 and above
         */
        UPDATED
    }

    public enum Timing {

        /**
         * Times the explosions based on when the crystal spawns
         */
        LINEAR,

        /**
         * Times the explosions & process based on when the crystal spawns
         */
        UNIFORM,

        /**
         * Times the explosions based on when the last process has completed
         */
        SEQUENTIAL,

        /**
         * No timing, just default break and place delays
         */
        TICK
    }

    public enum Sync {

        /**
         * Syncs the crystal removal to the explosion sound
         */
        SOUND,

        /**
         * Syncs the crystal removal to the attack
         */
        INSTANT,

        /**
         * Does not manually remove crystals
         */
        NONE
    }

    public enum Logic {

        /**
         * Heuristic: Best position is the one that deals the most damage
         */
        DAMAGE,

        /**
         * Heuristic: Best position is the one that maximizes damage to the target and minimizes damage to the player
         */
        MINIMAX,

        /**
         * Heuristic: Best position is the one that maximizes damage to the target and distance from the player, and also minimizes damage to the player
         */
        UNIFORM
    }

    public enum When {

        /**
         * Rotate when breaking
         */
        BREAK,

        /**
         * Rotate when placing
         */
        PLACE,

        /**
         * Rotate when breaking and placing
         */
        BOTH
    }

    public enum Text {

        /**
         * Render the damage done to the target
         */
        TARGET,

        /**
         * Render the damage done to the player
         */
        SELF,

        /**
         * Render the damage done to the target and the damage done to the player
         */
        BOTH,

        /**
         * No damage render
         */
        NONE
    }

    public enum Hand {

        /**
         * Sync the hand to the interacting hand
         */
        SYNC,

        /**
         * Swing with the mainhand
         */
        MAINHAND,

        /**
         * Swing with the offhand
         */
        OFFHAND,

        /**
         * Swing with packets
         */
        PACKET,

        /**
         * No swing
         */
        NONE
    }

    public enum Interact {

        /**
         * Places on the closest face, regardless of visibility, Allows placements at world borders
         */
        NORMAL,

        /**
         * Places on the closest visible face
         */
        STRICT,

        /**
         * Places on the top block face, no facing directions
         */
        NONE
    }

    public enum Raytrace {

        /**
         * Raytrace to the center of the block
         */
        BASE(0.5),

        /**
         * Raytrace to the center of the expected crystal position
         */
        NORMAL(1.5),

        /**
         * Raytrace to the highest position of the expected crystal, wall ranges will be more accurate
         */
        DOUBLE(2.5),

        /**
         * Wall ranges will be ignored, unless extreme circumstances
         */
        TRIPLE(3.5),

        /**
         * No raytrace to the position
         */
        NONE(-1);

        private final double offset;

        Raytrace(double offset) {
            this.offset = offset;
        }

        /**
         * Gets the offset from a position
         * @return The offset from the position
         */
        public double getOffset() {
            return offset;
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

    public static class CrystalPosition {

        // place info
        private final BlockPos blockPos;
        private final EntityPlayer placeTarget;

        // damage info
        private final double targetDamage;
        private final double localDamage;

        public CrystalPosition(BlockPos blockPos, EntityPlayer placeTarget, double targetDamage, double localDamage) {
            this.blockPos = blockPos;
            this.placeTarget = placeTarget;
            this.targetDamage = targetDamage;
            this.localDamage = localDamage;
        }

        /**
         * Gets the position of a placement
         * @return The {@link BlockPos} position of the placement
         */
        public BlockPos getPosition() {
            return blockPos;
        }

        /**
         * Gets the target of a placement
         * @return The {@link EntityPlayer} target of the placement
         */
        public EntityPlayer getPlaceTarget() {
            return placeTarget;
        }

        /**
         * Gets the damage to a target
         * @return The damage to the target
         */
        public double getTargetDamage() {
            return targetDamage;
        }

        /**
         * Gets the damage to the player
         * @return The damage to the player
         */
        public double getLocalDamage() {
            return localDamage;
        }
    }

    public static class Crystal {

        // crystal info
        private final EntityEnderCrystal crystal;

        // damage info
        private final double targetDamage;
        private final double localDamage;

        public Crystal(EntityEnderCrystal crystal, double targetDamage, double localDamage) {
            this.crystal = crystal;
            this.targetDamage = targetDamage;
            this.localDamage = localDamage;
        }

        /**
         * Gets the crystal entity
         * @return The {@link EntityEnderCrystal} crystal entity
         */
        public EntityEnderCrystal getCrystal() {
            return crystal;
        }

        /**
         * Gets the damage to a target
         * @return The damage to the target
         */
        public double getTargetDamage() {
            return targetDamage;
        }

        /**
         * Gets the damage to the player
         * @return The damage to the player
         */
        public double getLocalDamage() {
            return localDamage;
        }
    }
}