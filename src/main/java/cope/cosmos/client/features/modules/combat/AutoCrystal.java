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
import cope.cosmos.client.manager.managers.TickManager.TPS;
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

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("unused")
public class AutoCrystal extends Module {
    public static AutoCrystal INSTANCE;

    public AutoCrystal() {
        super("AutoCrystal", Category.COMBAT, "Places and explodes crystals");
        INSTANCE = this;
    }

    public static Setting<Boolean> explode = new Setting<>("Explode", "Explode crystals", true);
    public static Setting<Double> explodeRange = new Setting<>("Range", "Range to explode crystals", 0.0, 6.0, 8.0, 1).setParent(explode);
    public static Setting<Double> explodeWall = new Setting<>("WallRange", "Range to explode crystals through walls", 0.0, 3.5, 8.0, 1).setParent(explode);
    public static Setting<Double> explodeDelay = new Setting<>("Delay", "Delay to explode crystals", 0.0, 60.0, 500.0, 0).setParent(explode);
    public static Setting<Double> explodeRandom = new Setting<>("RandomDelay", "Randomize the delay slightly to simulate real explosions", 0.0, 0.0, 500.0, 0).setParent(explode);
    public static Setting<Double> explodeSwitch = new Setting<>("SwitchDelay", "Delay to wait after switching", 0.0, 0.0, 500.0, 0).setParent(explode);
    public static Setting<Double> explodeTicksExisted = new Setting<>("TicksExisted", "The minimum age of the crystal to explode", 0.0, 0.0, 5.0, 0).setParent(explode);
    public static Setting<Double> explodeDamage = new Setting<>("Damage", "Required damage to explode a crystal", 0.0, 5.0, 36.0, 1).setParent(explode);
    public static Setting<Double> explodeLocal = new Setting<>("LocalDamage", "Maximum allowed local damage to the player", 0.0, 5.0, 36.0, 1).setParent(explode);
    public static Setting<Double> explodeLimit = new Setting<>("Limit", "Attacks per crystal limiter", 0.0, 10.0, 10.0, 0).setParent(explode);
    public static Setting<Boolean> explodePacket = new Setting<>("Packet", "Explode with packets", true).setParent(explode);
    public static Setting<Boolean> explodeInhibit = new Setting<>("Inhibit", "Prevents attacks on crystals that would already be exploded", false).setParent(explode);
    public static Setting<Hand> explodeHand = new Setting<>("Hand", "Hand to swing when exploding crystals", Hand.SYNC).setParent(explode);
    public static Setting<Switch> explodeWeakness = new Setting<>("Weakness", "Switch to a tool when weakness is active", Switch.NONE).setParent(explode);

    public static Setting<Boolean> place = new Setting<>("Place", "Place Crystals", true);
    public static Setting<Double> placeRange = new Setting<>("Range", "Range to place crystals", 0.0, 5.0, 8.0, 1).setParent(place);
    public static Setting<Double> placeWall = new Setting<>("WallRange", "Range to place crystals through walls", 0.0, 3.5, 8.0, 1).setParent(place);
    public static Setting<Double> placeDelay = new Setting<>("Delay", "Delay to place crystals", 0.0, 20.0, 500.0, 0).setParent(place);
    public static Setting<Double> placeDamage = new Setting<>("Damage", "Required damage to be considered for placement", 0.0, 5.0, 36.0, 1).setParent(place);
    public static Setting<Double> placeLocal = new Setting<>("LocalDamage", "Maximum allowed local damage to the player", 0.0, 5.0, 36.0, 1).setParent(place);
    public static Setting<Boolean> placePacket = new Setting<>("Packet", "Place with packets", true).setParent(place);
    public static Setting<Interact> placeInteraction = new Setting<>("Interact", "Limits the direction of placements", Interact.NORMAL).setParent(place);
    public static Setting<Raytrace> placeRaytrace = new Setting<>("Raytrace", "Mode to verify placements through walls", Raytrace.DOUBLE).setParent(place);
    public static Setting<Hand> placeHand = new Setting<>("Hand", "Hand to swing when placing crystals", Hand.SYNC).setParent(place);
    public static Setting<Switch> placeSwitch = new Setting<>("Switch", "Mode to use when switching to a crystal", Switch.NONE).setParent(place);

    public static Setting<Boolean> pause = new Setting<>("Pause", "When to pause", true);
    public static Setting<Double> pauseHealth = new Setting<>("Health", "Pause below this health", 0.0, 10.0, 36.0, 0).setParent(pause);
    public static Setting<Boolean> pauseSafety = new Setting<>("Safety", "Pause when the current crystal will kill you", true).setParent(pause);
    public static Setting<Boolean> pauseEating = new Setting<>("Eating", "Pause when eating", false).setParent(pause);
    public static Setting<Boolean> pauseMining = new Setting<>("Mining", "Pause when mining", false).setParent(pause);
    public static Setting<Boolean> pauseMending = new Setting<>("Mending", "Pause when mending", false).setParent(pause);

    public static Setting<Boolean> override = new Setting<>("Override", "When to override minimum damage", true);
    public static Setting<Double> overrideHealth = new Setting<>("Health", "Override when target is below this health", 0.0, 10.0, 36.0, 0).setParent(override);
    public static Setting<Double> overrideThreshold = new Setting<>("Threshold", "Override if we can do lethal damage in this amount of crystals", 0.0, 0.0, 4.0, 1).setParent(override);
    public static Setting<Double> overrideArmor = new Setting<>("Armor", "Override when target's armor is below this percent", 0.0, 0.0, 100.0, 0).setParent(override);

    public static Setting<Rotate> rotate = new Setting<>("Rotation", "Mode for attack and placement rotation", Rotate.NONE);
    public static Setting<Limit> rotateLimit = new Setting<>(() -> rotate.getValue().equals(Rotate.PACKET), "Limit", "Mode for when to restrict rotations", Limit.NONE).setParent(rotate);
    public static Setting<When> rotateWhen = new Setting<>("When", "Mode for when to rotate", When.BOTH).setParent(rotate);
    public static Setting<Double> rotateRandom = new Setting<>("Random", "Randomize rotations to simulate real rotations", 0.0, 0.0, 5.0, 1).setParent(rotate);

    public static Setting<Boolean> calculations = new Setting<>("Calculations", "Preferences for calculations", true);
    public static Setting<Timing> timing = new Setting<>("Timing", "Optimizes process at the cost of anti-cheat compatibility", Timing.LINEAR).setParent(calculations);
    public static Setting<TPS> tps = new Setting<>("TPS", "Syncs attack timing to current server ticks", TPS.NONE).setParent(calculations);
    public static Setting<Placements> placements = new Setting<>("Placements", "Placement calculations for current version", Placements.NATIVE).setParent(calculations);
    public static Setting<Logic> logic = new Setting<>("Logic", "Logic for heuristic to prioritize", Logic.DAMAGE).setParent(calculations);
    public static Setting<Sync> sync = new Setting<>("Sync", "Sync for broken crystals", Sync.SOUND).setParent(calculations);
    public static Setting<Boolean> prediction = new Setting<>("Prediction", "Attempts to account target's predicted position into the calculations", false).setParent(calculations);
    public static Setting<Boolean> ignoreTerrain = new Setting<>("IgnoreTerrain", "Ignores terrain when calculating damage", false).setParent(calculations);

    public static Setting<Target> target = new Setting<>("Target", "Priority for searching target", Target.CLOSEST);
    public static Setting<Double> targetRange = new Setting<>("Range", "Range to consider an entity as a target", 0.0, 10.0, 15.0, 1).setParent(target);
    public static Setting<Boolean> targetPlayers = new Setting<>("Players", "Target players", true).setParent(target);
    public static Setting<Boolean> targetPassives = new Setting<>("Passives", "Target passives", false).setParent(target);
    public static Setting<Boolean> targetNeutrals = new Setting<>("Neutrals", "Target neutrals", false).setParent(target);
    public static Setting<Boolean> targetHostiles = new Setting<>("Hostiles", "Target hostiles", false).setParent(target);

    public static Setting<Boolean> render = new Setting<>("Render", "Render a visual for calculated placement", true);
    public static Setting<Box> renderMode = new Setting<>("Mode", "Style for visual", Box.BOTH).setParent(render);
    public static Setting<Text> renderText = new Setting<>("Text", "Text for the visual", Text.NONE).setParent(render);
    public static Setting<Double> renderWidth = new Setting<>(() -> renderMode.getValue().equals(Box.BOTH) || renderMode.getValue().equals(Box.CLAW) || renderMode.getValue().equals(Box.OUTLINE), "Width", "Line width for the visual", 0.0, 1.5, 3.0, 1).setParent(render);

    private Crystal explodeCrystal = new Crystal(null, 0, 0);
    private final Timer explodeTimer = new Timer();
    private final Timer switchTimer = new Timer();
    private final Map<Integer, Integer> attemptedExplosions = new ConcurrentHashMap<>();
    private final List<EntityEnderCrystal> blackListExplosions = new CopyOnWriteArrayList<>();

    private CrystalPosition placePosition = new CrystalPosition(BlockPos.ORIGIN, null, 0, 0);
    private final Timer placeTimer = new Timer();
    private final Map<BlockPos, Integer> attemptedPlacements = new ConcurrentHashMap<>();

    private int strictTicks;

    private boolean yawLimit;
    private Vec3d interactVector = Vec3d.ZERO;

    private EnumHand previousHand;
    private int previousSlot = -1;

    @Override
    public void onUpdate() {
        if (pause.getValue()) {
            if (PlayerUtil.isEating() && pauseEating.getValue() || PlayerUtil.isMining() && pauseMining.getValue() || PlayerUtil.isMending() && pauseMending.getValue()) {
                return;
            }

            if (PlayerUtil.getHealth() < pauseHealth.getValue() && !mc.player.capabilities.isCreativeMode) {
                return;
            }
        }

        if (strictTicks > 0) {
            strictTicks--;
        }

        else {
            explodeCrystal();
            placeCrystal();
        }
    }

    @Override
    public void onThread() {
        explodeCrystal = searchCrystal();
        placePosition = searchPosition();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        resetProcess();
    }

    public void explodeCrystal() {
        if (!tps.getValue().equals(TPS.NONE)) {
            // skip ticks based on the current tps
            strictTicks += (int) (20 - getCosmos().getTickManager().getTPS(tps.getValue()));
        }

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
                    int swordSlot = InventoryUtil.getItemSlot(Items.DIAMOND_SWORD, Inventory.HOTBAR);
                    int pickSlot = InventoryUtil.getItemSlot(Items.DIAMOND_SWORD, Inventory.HOTBAR);

                    if (!InventoryUtil.isHolding(Items.DIAMOND_SWORD) || !InventoryUtil.isHolding(Items.DIAMOND_PICKAXE)) {
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

            if (explodeTimer.passed(explodeDelay.getValue().longValue() + (long) ThreadLocalRandom.current().nextDouble(explodeRandom.getValue() + 1), Format.SYSTEM) && switchTimer.passed(explodeSwitch.getValue().longValue(), Format.SYSTEM)) {
                // explode the crystal
                explodeCrystal(explodeCrystal.getCrystal(), explodePacket.getValue());
                swingArm(explodeHand.getValue());

                explodeTimer.reset();

                // add crystal to our list of attempted explosions and reset the clearance
                attemptedExplosions.put(explodeCrystal.getCrystal().getEntityId(), attemptedExplosions.containsKey(explodeCrystal.getCrystal().getEntityId()) ? attemptedExplosions.get(explodeCrystal.getCrystal().getEntityId()) + 1 : 1);

                if (sync.getValue().equals(Sync.INSTANT)) {
                    mc.world.removeEntityDangerously(explodeCrystal.getCrystal());
                }
            }
        }
    }

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

            if (placeTimer.passed(placeDelay.getValue().longValue(), Format.SYSTEM) && (InventoryUtil.isHolding(Items.END_CRYSTAL) || placeSwitch.getValue().equals(Switch.PACKET))) {
                // directions of placement
                double facingX = 0;
                double facingY = 0;
                double facingZ = 0;
                EnumFacing facingDirection = EnumFacing.UP;

                // the vector and angles to the last interaction
                float[] vectorAngles = AngleUtil.calculateAngles(interactVector);
                Vec3d placeVector = AngleUtil.getVectorForRotation(new Rotation(vectorAngles[0], vectorAngles[1]));
                RayTraceResult vectorResult = mc.world.rayTraceBlocks(mc.player.getPositionEyes(1), mc.player.getPositionEyes(1).addVector(placeVector.x * placeRange.getValue(), placeVector.y * placeRange.getValue(), placeVector.z * placeRange.getValue()), false, false, true);

                // make sure the direction we are facing is consistent with our rotations
                switch (placeInteraction.getValue()) {
                    case NONE:
                        facingX = 0.5;
                        facingY = 0.5;
                        facingZ = 0.5;
                        break;
                    case NORMAL:
                        RayTraceResult laxResult = mc.world.rayTraceBlocks(mc.player.getPositionEyes(1), interactVector);
                        facingDirection = (laxResult == null || laxResult.sideHit == null) ? EnumFacing.UP : laxResult.sideHit;

                        // if we're at world height, we can still place a crystal if we interact with the bottom of the block, this doesn't work on strict servers
                        if (placePosition.getPosition().getY() >= (mc.world.getHeight() - 1)) {
                            facingDirection = EnumFacing.DOWN;
                        }

                        if (vectorResult != null && vectorResult.hitVec != null) {
                            facingX = vectorResult.hitVec.x - placePosition.getPosition().getX();
                            facingY = vectorResult.hitVec.y - placePosition.getPosition().getY();
                            facingZ = vectorResult.hitVec.z - placePosition.getPosition().getZ();
                        }

                        break;
                    case STRICT:
                        // if the placement is higher than us, we need to place on the lowest/closest visible side
                        RayTraceResult strictResult = mc.world.rayTraceBlocks(mc.player.getPositionEyes(1), interactVector, false, true, false);

                        if (strictResult != null && strictResult.typeOfHit.equals(RayTraceResult.Type.BLOCK)) {
                            facingDirection = strictResult.getBlockPos().equals(placePosition.getPosition()) ? strictResult.sideHit : EnumFacing.UP;
                        }

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
                }

                placeTimer.reset();

                // add placement to our list of attempted placement and reset clearance
                attemptedPlacements.put(placePosition.getPosition(), attemptedPlacements.containsKey(placePosition.getPosition()) ? attemptedPlacements.get(placePosition.getPosition()) + 1 : 1);
            }
        }
    }

    public Crystal searchCrystal() {
        if (explode.getValue()) {
            // map of viable crystals
            TreeMap<Float, Crystal> crystalMap = new TreeMap<>();

            for (Entity calculatedCrystal : new ArrayList<>(mc.world.loadedEntityList)) {
                // make sure it's a viable crystal
                if (!(calculatedCrystal instanceof EntityEnderCrystal) || calculatedCrystal.isDead) {
                    continue;
                }

                // make sure it's in range
                float distance = mc.player.getDistance(calculatedCrystal);
                if (distance > explodeRange.getValue() || (!mc.player.canEntityBeSeen(calculatedCrystal) && distance > explodeWall.getValue())) {
                    continue;
                }

                if (blackListExplosions.contains(calculatedCrystal) && explodeInhibit.getValue()) {
                    continue;
                }

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
                    float targetDamage = calculateLogic(ExplosionUtil.getDamageFromExplosion(calculatedCrystal.posX, calculatedCrystal.posY, calculatedCrystal.posZ, calculatedTarget, ignoreTerrain.getValue(), prediction.getValue()), localDamage, distance);

                    // add it to our list of viable crystals
                    crystalMap.put(targetDamage, new Crystal((EntityEnderCrystal) calculatedCrystal, targetDamage, localDamage));
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

    public CrystalPosition searchPosition() {
        if (place.getValue()) {
            // map of viable positions
            TreeMap<Float, CrystalPosition> positionMap = new TreeMap<>();

            for (BlockPos calculatedPosition : BlockUtil.getSurroundingBlocks(mc.player, placeRange.getValue(), false)) {
                // make sure it's actually a viable position
                if (!canPlaceCrystal(calculatedPosition, placements.getValue())) {
                    continue;
                }

                // make sure it doesn't do too much dmg to us or kill us
                float localDamage = mc.player.capabilities.isCreativeMode ? 0 : ExplosionUtil.getDamageFromExplosion(calculatedPosition.getX() + 0.5, calculatedPosition.getY() + 1, calculatedPosition.getZ() + 0.5, mc.player, ignoreTerrain.getValue(), false);
                if (localDamage > placeLocal.getValue() || (localDamage + 1 > PlayerUtil.getHealth() && pauseSafety.getValue())) {
                    continue;
                }

                // if the block above the one we can't see through is air, then NCP won't flag us for placing at normal ranges
                boolean wallPlacement = !placeRaytrace.getValue().equals(Raytrace.NONE) && RaytraceUtil.raytraceBlock(calculatedPosition, placeRaytrace.getValue());

                // if it is a wall placement, use our wall ranges
                double distance = mc.player.getDistance(calculatedPosition.getX() + 0.5, calculatedPosition.getY() + 1, calculatedPosition.getZ() + 0.5);
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
                    float targetDamage = calculateLogic(ExplosionUtil.getDamageFromExplosion(calculatedPosition.getX() + 0.5, calculatedPosition.getY() + 1, calculatedPosition.getZ() + 0.5, calculatedTarget, ignoreTerrain.getValue(), prediction.getValue()), localDamage, distance);

                    // add it to our list of viable positions
                    positionMap.put(targetDamage, new CrystalPosition(calculatedPosition, calculatedTarget, targetDamage, localDamage));
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

                    if (getCosmos().getHoleManager().isHoleEntity(idealPosition.getPlaceTarget())) {
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
            RenderUtil.drawBox(new RenderBuilder().position(placePosition.getPosition()).color(new Color(ColorUtil.getPrimaryColor().getRed(), ColorUtil.getPrimaryColor().getGreen(), ColorUtil.getPrimaryColor().getBlue(), 60)).box(renderMode.getValue()).setup().line(renderWidth.getValue().floatValue()).cull(renderMode.getValue().equals(Box.GLOW) || renderMode.getValue().equals(Box.REVERSE)).shade(renderMode.getValue().equals(Box.GLOW) || renderMode.getValue().equals(Box.REVERSE)).alpha(renderMode.getValue().equals(Box.GLOW) || renderMode.getValue().equals(Box.REVERSE)).depth(true).blend().texture());
            RenderUtil.drawNametag(placePosition.getPosition(), 0.5F, getText(renderText.getValue()));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRotationUpdate(RotationUpdateEvent event) {
        if (isActive() && rotate.getValue().equals(Rotate.PACKET)) {
            // cancel the existing rotations, we'll send our own
            event.setCanceled(true);

            float[] packetAngles = AngleUtil.calculateAngles(interactVector);

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
            getCosmos().getRotationManager().addRotation(new Rotation(packetAngles[0], packetAngles[1]), Integer.MAX_VALUE);
        }
    }

    @SubscribeEvent
    public void onRenderRotations(RenderRotationsEvent event) {
        if (isActive() && rotate.getValue().equals(Rotate.PACKET)) {
            event.setCanceled(true);

            float[] packetAngles = AngleUtil.calculateAngles(interactVector);
            if (rotateRandom.getValue() > 0) {
                Random randomAngle = new Random();
                packetAngles[0] += randomAngle.nextFloat() * (randomAngle.nextBoolean() ? rotateRandom.getValue() : -rotateRandom.getValue());
            }

            event.setYaw(packetAngles[0]);
            event.setPitch(packetAngles[1]);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketHeldItemChange) {
            switchTimer.reset();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {
        if (event.getPacket() instanceof SPacketSpawnObject && ((SPacketSpawnObject) event.getPacket()).getType() == 51) {
            // position of the placed crystal
            BlockPos linearPosition = new BlockPos(((SPacketSpawnObject) event.getPacket()).getX(), ((SPacketSpawnObject) event.getPacket()).getY(), ((SPacketSpawnObject) event.getPacket()).getZ());

            if (timing.getValue().equals(Timing.SEQUENTIAL)) {
                // since it's been confirmed that the crystal spawned, we can move on to our next process
                if (attemptedPlacements.containsKey(linearPosition.down())) {
                    if (!explodeTimer.passed(explodeDelay.getValue().longValue(), Format.SYSTEM)) {
                        explodeTimer.setTime(explodeDelay.getValue().longValue());
                    }

                    attemptedPlacements.clear();
                }
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
                    float targetDamage = calculateLogic(ExplosionUtil.getDamageFromExplosion(linearPosition.getX() + 0.5, linearPosition.getY() + 1, linearPosition.getZ() + 0.5, calculatedTarget, ignoreTerrain.getValue(), false), localDamage, distance);

                    linearMap.put(targetDamage, ((SPacketSpawnObject) event.getPacket()).getEntityID());
                }

                if (!linearMap.isEmpty()) {
                    Map.Entry<Float, Integer> idealLinear = linearMap.lastEntry();

                    // make sure it meets requirements
                    if (idealLinear.getKey() > explodeDamage.getValue()) {
                        // explode the linear crystal
                        explodeCrystal(idealLinear.getValue());
                        swingArm(explodeHand.getValue());

                        // add crystal to our list of attempted explosions
                        attemptedExplosions.put(((SPacketSpawnObject) event.getPacket()).getEntityID(), attemptedExplosions.containsKey(((SPacketSpawnObject) event.getPacket()).getEntityID()) ? attemptedExplosions.get(((SPacketSpawnObject) event.getPacket()).getEntityID()) + 1 : 1);
                    }
                }
            }
        }

        if (event.getPacket() instanceof SPacketDestroyEntities && (timing.getValue().equals(Timing.SEQUENTIAL) || timing.getValue().equals(Timing.UNIFORM))) {
            // since it's been confirmed that the crystal exploded, we can move on to our next process
            for (int entityId : ((SPacketDestroyEntities) event.getPacket()).getEntityIDs()) {
                if (attemptedExplosions.containsKey(entityId)) {
                    if (!placeTimer.passed(placeDelay.getValue().longValue(), Format.SYSTEM)) {
                        placeTimer.setTime(placeDelay.getValue().longValue());
                    }

                    attemptedExplosions.clear();
                    break;
                }
            }
        }

        // packet for crystal explosions
        if (event.getPacket() instanceof SPacketSoundEffect && ((SPacketSoundEffect) event.getPacket()).getSound().equals(SoundEvents.ENTITY_GENERIC_EXPLODE) && ((SPacketSoundEffect) event.getPacket()).getCategory().equals(SoundCategory.BLOCKS)) {
            mc.addScheduledTask(() -> new ArrayList<>(mc.world.loadedEntityList).stream().filter(entity -> entity instanceof EntityEnderCrystal).filter(entity -> entity.getDistance(((SPacketSoundEffect) event.getPacket()).getX(), ((SPacketSoundEffect) event.getPacket()).getY(), ((SPacketSoundEffect) event.getPacket()).getZ()) < 6).forEach(entity -> {
                // going to be exploded anyway, so don't attempt explosion
                if (explodeInhibit.getValue()) {
                    blackListExplosions.add((EntityEnderCrystal) entity);
                }

                // the world sets the crystal dead one tick after this packet, but we can speed up the placements by setting it dead here
                if (sync.getValue().equals(Sync.SOUND)) {
                    mc.world.removeEntityDangerously(entity);
                }
            }));
        }
    }

    public void placeCrystal(BlockPos placePos, EnumFacing enumFacing, Vec3d vector, boolean packet) {
        new Thread(() -> {
            if (packet) {
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(placePos, enumFacing, mc.player.getHeldItemMainhand().getItem().equals(Items.END_CRYSTAL) || placeSwitch.getValue().equals(Switch.PACKET) ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND, (float) vector.x, (float) vector.y, (float) vector.z));
            }

            else {
                mc.playerController.processRightClickBlock(mc.player, mc.world, placePos, enumFacing, vector, mc.player.getHeldItemMainhand().getItem().equals(Items.END_CRYSTAL) || placeSwitch.getValue().equals(Switch.PACKET) ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND);
            }
        }).start();
    }

    public void explodeCrystal(EntityEnderCrystal crystal, boolean packet) {
        new Thread(() -> {
            if (packet) {
                mc.player.connection.sendPacket(new CPacketUseEntity(crystal));
            }

            else {
                mc.playerController.attackEntity(mc.player, crystal);
            }
        }).start();
    }

    @SuppressWarnings("all")
    public void explodeCrystal(int entityId) {
        CPacketUseEntity attackPacket = new CPacketUseEntity();
        ((ICPacketUseEntity) attackPacket).setID(entityId);
        ((ICPacketUseEntity) attackPacket).setAction(CPacketUseEntity.Action.ATTACK);
        mc.player.connection.sendPacket(attackPacket);
    }

    public static void swingArm(Hand hand) {
        switch (hand) {
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

    public boolean canPlaceCrystal(BlockPos blockPos, Placements placements) {
        try {
            if (!mc.world.getBlockState(blockPos).getBlock().equals(Blocks.BEDROCK) && !mc.world.getBlockState(blockPos).getBlock().equals(Blocks.OBSIDIAN))
                return false;

            for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(blockPos.add(0, 1, 0)))) {
                if (entity.isDead || (entity instanceof EntityEnderCrystal && entity.getPosition().equals(blockPos.add(0, 1, 0)))) {
                    continue;
                }

                return false;
            }

            switch (placements) {
                case NATIVE:
                default:
                    return mc.world.getBlockState(blockPos.add(0, 2, 0)).getBlock().equals(Blocks.AIR) && (mc.world.getBlockState(blockPos.add(0, 1, 0)).getBlock().equals(Blocks.AIR) || mc.world.getBlockState(blockPos.add(0, 1, 0)).getBlock().equals(Blocks.FIRE));
                case UPDATED:
                    return mc.world.getBlockState(blockPos.add(0, 1, 0)).getBlock().equals(Blocks.AIR) || mc.world.getBlockState(blockPos.add(0, 1, 0)).getBlock().equals(Blocks.FIRE);
            }
        } catch (Exception ignored) {
            return false;
        }
    }

    public float calculateLogic(float targetDamage, float selfDamage, double distance) {
        switch (logic.getValue()) {
            case DAMAGE:
            default:
                return targetDamage;
            case MINIMAX:
                return targetDamage - selfDamage;
            case UNIFORM:
                return targetDamage - selfDamage - (float) distance;
        }
    }

    public String getText(Text text) {
        switch (text) {
            case TARGET:
                return String.valueOf(MathUtil.roundDouble(placePosition.getTargetDamage(), 1));
            case SELF:
                return String.valueOf(MathUtil.roundDouble(placePosition.getSelfDamage(), 1));
            case BOTH:
                return "Target: " + MathUtil.roundDouble(placePosition.getTargetDamage(), 1) + ", Self: " + MathUtil.roundDouble(placePosition.getSelfDamage(), 1);
            case NONE:
            default:
                return "";
        }
    }

    public void resetProcess() {
        explodeCrystal = new Crystal(null, 0, 0);
        placePosition = new CrystalPosition(BlockPos.ORIGIN, null, 0, 0);
        interactVector = Vec3d.ZERO;
        yawLimit = false;
        previousHand = null;
        previousSlot = -1;
        strictTicks = 0;
        placeTimer.reset();
        explodeTimer.reset();
        attemptedExplosions.clear();
        attemptedPlacements.clear();
        blackListExplosions.clear();
    }

    public enum Placements {
        NATIVE, UPDATED
    }

    public enum Timing {
        LINEAR, UNIFORM, SEQUENTIAL, TICK
    }

    public enum Sync {
        SOUND, INSTANT, NONE
    }

    public enum Logic {
        DAMAGE, MINIMAX, UNIFORM
    }

    public enum When {
        BREAK, PLACE, BOTH
    }

    public enum Text {
        TARGET, SELF, BOTH, NONE
    }

    public enum Hand {
        SYNC, MAINHAND, OFFHAND, PACKET, NONE
    }

    public enum Interact {
        NORMAL, STRICT, NONE
    }

    public enum Raytrace {
        NONE(-1), BASE(0.5), NORMAL(1.5), DOUBLE(2.5), TRIPLE(3.5);

        private final double offset;

        Raytrace(double offset) {
            this.offset = offset;
        }

        public double getOffset() {
            return offset;
        }
    }

    public enum Limit {
        NORMAL, STRICT, NONE
    }

    public static class CrystalPosition {

        private final BlockPos blockPos;
        private final EntityPlayer placeTarget;
        private final double targetDamage;
        private final double selfDamage;

        public CrystalPosition(BlockPos blockPos, EntityPlayer placeTarget, double targetDamage, double selfDamage) {
            this.blockPos = blockPos;
            this.placeTarget = placeTarget;
            this.targetDamage = targetDamage;
            this.selfDamage = selfDamage;
        }

        public BlockPos getPosition() {
            return blockPos;
        }

        public EntityPlayer getPlaceTarget() {
            return placeTarget;
        }

        public double getTargetDamage() {
            return targetDamage;
        }

        public double getSelfDamage() {
            return selfDamage;
        }
    }

    public static class Crystal {

        private final EntityEnderCrystal crystal;
        private final double targetDamage;
        private final double selfDamage;

        public Crystal(EntityEnderCrystal crystal, double targetDamage, double selfDamage) {
            this.crystal = crystal;
            this.targetDamage = targetDamage;
            this.selfDamage = selfDamage;
        }

        public EntityEnderCrystal getCrystal() {
            return crystal;
        }

        public double getTargetDamage() {
            return targetDamage;
        }

        public double getSelfDamage() {
            return selfDamage;
        }
    }
}
