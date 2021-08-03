package cope.cosmos.client.features.modules.combat;

import cope.cosmos.asm.mixins.accessor.ICPacketPlayer;
import cope.cosmos.asm.mixins.accessor.ICPacketUseEntity;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.manager.managers.SocialManager.Relationship;
import cope.cosmos.client.manager.managers.TickManager.TPS;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.util.combat.EnemyUtil;
import cope.cosmos.util.combat.ExplosionUtil;
import cope.cosmos.util.combat.TargetUtil.Target;
import cope.cosmos.util.player.InventoryUtil;
import cope.cosmos.util.player.InventoryUtil.*;
import cope.cosmos.util.player.PlayerUtil;
import cope.cosmos.util.player.PlayerUtil.Hand;
import cope.cosmos.util.player.Rotation;
import cope.cosmos.util.player.Rotation.Rotate;
import cope.cosmos.util.render.RenderBuilder;
import cope.cosmos.util.render.RenderBuilder.Box;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.system.MathUtil;
import cope.cosmos.util.system.Timer;
import cope.cosmos.util.system.Timer.Format;
import cope.cosmos.util.world.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.*;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("unused")
public class AutoCrystal extends Module {
    public static AutoCrystal INSTANCE;

    public AutoCrystal() {
        super("AutoCrystal", Category.COMBAT, "Places and explodes crystals", () -> AutoCrystal.INSTANCE.getRenderInfo());
        INSTANCE = this;
    }

    public static Setting<Boolean> explode = new Setting<>("Explode", "Explode crystals", true);
    public static Setting<Double> explodeRange = new Setting<>("Range", "Range to explode crystals", 0.0, 6.0, 8.0, 1).setParent(explode);
    public static Setting<Double> explodeWall = new Setting<>("WallRange", "Range to explode crystals through walls", 0.0, 3.5, 8.0, 1).setParent(explode);
    public static Setting<Double> explodeDelay = new Setting<>("Delay", "Delay to explode crystals", 0.0, 60.0, 500.0, 0).setParent(explode);
    public static Setting<Double> explodeSwitch = new Setting<>("SwitchDelay", "Delay to wait after switching", 0.0, 0.0, 500.0, 0).setParent(explode);
    public static Setting<Double> explodeDamage = new Setting<>("Damage", "Required damage to explode a crystal", 0.0, 5.0, 36.0, 1).setParent(explode);
    public static Setting<Double> explodeLocal = new Setting<>("LocalDamage", "Maximum allowed local damage to the player", 0.0, 5.0, 36.0, 1).setParent(explode);
    public static Setting<Double> explodeAttacks = new Setting<>("Attacks", "Attacks per crystal", 1.0, 1.0, 5.0, 0).setParent(explode);
    public static Setting<Double> explodeLimit = new Setting<>("Limit", "Attacks per crystal limiter", 0.0, 10.0, 10.0, 0).setParent(explode);
    public static Setting<Boolean> explodePacket = new Setting<>("Packet", "Explode with packets", true).setParent(explode);
    public static Setting<Hand> explodeHand = new Setting<>("Hand", "Hand to swing when exploding crystals", Hand.MAINHAND).setParent(explode);
    public static Setting<Switch> explodeWeakness = new Setting<>("Weakness", "Switch to a tool when weakness is active", Switch.NONE).setParent(explode);

    public static Setting<Boolean> place = new Setting<>("Place", "Place Crystals", true);
    public static Setting<Double> placeRange = new Setting<>("Range", "Range to place crystals", 0.0, 5.0, 8.0, 1).setParent(place);
    public static Setting<Double> placeWall = new Setting<>("WallRange", "Range to place crystals through walls", 0.0, 3.5, 8.0, 1).setParent(place);
    public static Setting<Double> placeDelay = new Setting<>("Delay", "Delay to place crystals", 0.0, 20.0, 500.0, 0).setParent(place);
    public static Setting<Double> placeRandom = new Setting<>("RandomDelay", "Randomize the delay slightly to simulate real placements", 0.0, 0.0, 500.0, 0).setParent(place);
    public static Setting<Double> placeDamage = new Setting<>("Damage", "Required damage to be considered for placement", 0.0, 5.0, 36.0, 1).setParent(place);
    public static Setting<Double> placeLocal = new Setting<>("LocalDamage", "Maximum allowed local damage to the player", 0.0, 5.0, 36.0, 1).setParent(place);
    public static Setting<Double> placeAttempts = new Setting<>("Attempts", "Place attempts per cycle", 1.0, 1.0, 5.0, 0).setParent(place);
    public static Setting<Boolean> placePacket = new Setting<>("Packet", "Place with packets", true).setParent(place);
    public static Setting<Boolean> placeDirection = new Setting<>("StrictDirection", "Limits the direction of placements to only upward facing", false).setParent(place);
    public static Setting<Raytrace> placeRaytrace = new Setting<>("Raytrace", "Mode to verify placements through walls", Raytrace.DOUBLE).setParent(place);
    public static Setting<Hand> placeHand = new Setting<>("Hand", "Hand to swing when placing crystals", Hand.MAINHAND).setParent(place);
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
    public static Setting<Float> rotateStep = new Setting<>("Step", "Number of divisions when sending rotation packets", 1.0F, 1.0F, 10.0F, 0).setParent(rotate);
    public static Setting<Double> rotateRandom = new Setting<>("Random", "Randomize rotations to simulate real rotations", 0.0, 4.0, 10.0, 1).setParent(rotate);
    public static Setting<Boolean> rotateCenter = new Setting<>("Center", "Center rotations on target", false).setParent(rotate);
    public static Setting<When> rotateWhen = new Setting<>("When", "Mode for when to rotate", When.BOTH).setParent(rotate);

    public static Setting<Boolean> calculations = new Setting<>("Calculations", "Preferences for calculations", true);
    public static Setting<Boolean> prediction = new Setting<>("Prediction", "Attempts to account target's predicted position into the calculations", false).setParent(calculations);
    public static Setting<Boolean> ignoreTerrain = new Setting<>("IgnoreTerrain", "Ignores terrain when calculating damage", false).setParent(calculations);
    public static Setting<Boolean> entityPrediction = new Setting<>("EntityPrediction", "Tries to predict the crystal's entity ID ahead of time", true).setParent(calculations);
    public static Setting<Timing> timing = new Setting<>("Timing", "Optimizes process at the cost of anti-cheat compatibility", Timing.LINEAR).setParent(calculations);
    public static Setting<TPS> tps = new Setting<>("TPS", "Syncs attack timing to current server ticks", TPS.NONE).setParent(calculations);
    public static Setting<Placements> placements = new Setting<>("Placements", "Placement calculations for current version", Placements.NATIVE).setParent(calculations);
    public static Setting<Logic> logic = new Setting<>("Logic", "Logic for heuristic to prioritize", Logic.DAMAGE).setParent(calculations);
    public static Setting<Sync> sync = new Setting<>("Sync", "Sync for broken crystals", Sync.SOUND).setParent(calculations);

    public static Setting<Target> target = new Setting<>("Target", "Priority for searching target", Target.CLOSEST);
    public static Setting<Double> targetRange = new Setting<>("Range", "Range to consider an entity as a target", 0.0, 10.0, 15.0, 1).setParent(target);
    public static Setting<Boolean> targetPlayers = new Setting<>("Players", "Target players", true).setParent(target);
    public static Setting<Boolean> targetPassives = new Setting<>("Passives", "Target passives", false).setParent(target);
    public static Setting<Boolean> targetNeutrals = new Setting<>("Neutrals", "Target neutrals", false).setParent(target);
    public static Setting<Boolean> targetHostiles = new Setting<>("Hostiles", "Target hostiles", false).setParent(target);

    public static Setting<Boolean> render = new Setting<>("Render", "Render a visual for calculated placement", true);
    public static Setting<Box> renderMode = new Setting<>("Mode", "Style for visual", Box.BOTH).setParent(render);
    public static Setting<Text> renderText = new Setting<>("Text", "Text for the visual", Text.NONE).setParent(render);
    public static Setting<Info> renderInfo = new Setting<>("Info", "Arraylist information", Info.NONE).setParent(render);
    public static Setting<Double> renderWidth = new Setting<>(() -> renderMode.getValue().equals(Box.BOTH) || renderMode.getValue().equals(Box.CLAW) || renderMode.getValue().equals(Box.OUTLINE), "Width", "Line width for the visual", 0.0, 1.5, 3.0, 1).setParent(render);
    public static Setting<Color> renderColor = new Setting<>("Color", "Color for the visual", new Color(144, 0, 255, 45)).setParent(render);

    private final Timer explodeTimer = new Timer();
    private final Timer switchTimer = new Timer();
    public static Crystal explodeCrystal = new Crystal(null, 0, 0);
    public static Map<Integer, Integer> attemptedExplosions = new HashMap<>();

    private final Timer placeTimer = new Timer();
    public static CrystalPosition placePosition = new CrystalPosition(BlockPos.ORIGIN, null, 0, 0);

    private EnumHand previousHand = null;
    private int previousSlot = -1;

    private Rotation crystalRotation;

    @Override
    public void onUpdate() {
        explodeCrystal();
        placeCrystal();
    }

    @Override
    public void onThread() {
        if (pause.getValue()) {
            if (PlayerUtil.isEating() && pauseEating.getValue() || PlayerUtil.isMining() && pauseMining.getValue() || PlayerUtil.isMending() && pauseMending.getValue())
                return;

            if (PlayerUtil.getHealth() < pauseHealth.getValue() && !mc.player.capabilities.isCreativeMode)
                return;
        }

        explodeCrystal = searchCrystal();
        placePosition = searchPosition();
    }

    public void explodeCrystal() {
        if (explodeCrystal != null) {
            if (!rotate.getValue().equals(Rotate.NONE) && (rotateWhen.getValue().equals(When.BREAK) || rotateWhen.getValue().equals(When.BOTH))) {
                // find the angles and update the rotation
                float[] explodeAngles = rotateCenter.getValue() ? AngleUtil.calculateCenter(explodeCrystal.getCrystal()) : AngleUtil.calculateAngles(explodeCrystal.getCrystal());
                crystalRotation = new Rotation((float) (explodeAngles[0] + ThreadLocalRandom.current().nextDouble(-rotateRandom.getValue(), rotateRandom.getValue())), (float) (explodeAngles[1] + ThreadLocalRandom.current().nextDouble(-rotateRandom.getValue(), rotateRandom.getValue())), rotate.getValue());

                // update the player model, not necessary but looks way cooler
                if (!Float.isNaN(crystalRotation.getYaw()) && !Float.isNaN(crystalRotation.getPitch()))
                    crystalRotation.updateModelRotations();
            }

            if (!explodeWeakness.getValue().equals(Switch.NONE)) {
                // strength and weakness effects on the player
                PotionEffect weaknessEffect = mc.player.getActivePotionEffect(MobEffects.WEAKNESS);
                PotionEffect strengthEffect = mc.player.getActivePotionEffect(MobEffects.STRENGTH);

                // verify that we cannot break the crystal due to weakness
                if (weaknessEffect != null && (strengthEffect == null || strengthEffect.getAmplifier() < weaknessEffect.getAmplifier())) {
                    int swordSlot = InventoryUtil.getItemSlot(Items.DIAMOND_SWORD, Inventory.INVENTORY, true);
                    int pickSlot = InventoryUtil.getItemSlot(Items.DIAMOND_SWORD, Inventory.INVENTORY, true);

                    if (!InventoryUtil.isHolding(Items.DIAMOND_SWORD) || !InventoryUtil.isHolding(Items.DIAMOND_PICKAXE)) {
                        // prefer the sword over a pickaxe
                        if (swordSlot != -1)
                            InventoryUtil.switchToSlot(swordSlot, explodeWeakness.getValue());

                        else if (pickSlot != -1)
                            InventoryUtil.switchToSlot(pickSlot, explodeWeakness.getValue());
                    }
                }
            }

            long scaledDelay = explodeDelay.getValue().longValue();
            if (!tps.getValue().equals(TPS.NONE)) {
                // scale the delay by the current server tps
                scaledDelay *= (80 * (1 - (Cosmos.INSTANCE.getTickManager().getTPS(tps.getValue()) / 20)));
            }

            if (explodeTimer.passed(scaledDelay, Format.SYSTEM) && switchTimer.passed((long) ((double) explodeSwitch.getValue()), Format.SYSTEM)) {
                // explode the crystal
                if (explodeAttacks.getValue() > 1) {
                    for (int explodeAttack = 0; explodeAttack < explodeAttacks.getValue(); explodeAttack++) {
                        explodeCrystal(explodeCrystal.getCrystal(), explodePacket.getValue());
                    }
                }

                else {
                    explodeCrystal(explodeCrystal.getCrystal(), explodePacket.getValue());
                }

                PlayerUtil.swingArm(explodeHand.getValue());

                explodeTimer.reset();

                // add crystal to our list of attempted explosions
                attemptedExplosions.put(explodeCrystal.getCrystal().getEntityId(), attemptedExplosions.containsKey(explodeCrystal.getCrystal().getEntityId()) ? attemptedExplosions.get(explodeCrystal.getCrystal().getEntityId()) + 1 : 1);

                if (sync.getValue().equals(Sync.INSTANT))
                    explodeCrystal.getCrystal().setDead();
            }
        }
    }

    public void placeCrystal() {
        if (placePosition != null) {
            if (!rotate.getValue().equals(Rotate.NONE) && (rotateWhen.getValue().equals(When.PLACE) || rotateWhen.getValue().equals(When.BOTH))) {
                // find the angles and update the rotation
                float[] placeAngles = rotateCenter.getValue() ? AngleUtil.calculateCenter(placePosition.getPosition()) : AngleUtil.calculateAngles(placePosition.getPosition());
                crystalRotation = new Rotation((float) (placeAngles[0] + ThreadLocalRandom.current().nextDouble(-rotateRandom.getValue(), rotateRandom.getValue())), (float) (placeAngles[1] + ThreadLocalRandom.current().nextDouble(-rotateRandom.getValue(), rotateRandom.getValue())), rotate.getValue());

                // update the player model, not necessary but looks way cooler
                if (!Float.isNaN(crystalRotation.getYaw()) && !Float.isNaN(crystalRotation.getPitch()))
                    crystalRotation.updateModelRotations();
            }

            // log our previous slot and hand, we'll switch back after placing
            if (placeSwitch.getValue().equals(Switch.PACKET)) {
                previousSlot = mc.player.inventory.currentItem;

                if (mc.player.isHandActive())
                    previousHand = mc.player.getActiveHand();
            }

            // switch to crystals if needed
            InventoryUtil.switchToSlot(Items.END_CRYSTAL, placeSwitch.getValue());

            if (placeTimer.passed(placeDelay.getValue().longValue() + (long) ThreadLocalRandom.current().nextDouble(placeRandom.getValue() + 1), Format.SYSTEM) && (InventoryUtil.isHolding(Items.END_CRYSTAL) || placeSwitch.getValue().equals(Switch.PACKET))) {
                // directions of placement
                EnumFacing placementFacing = EnumFacing.DOWN;

                // if we're not limited to upward placements, we can extend our reach by finding the closest face to place on
                if (!placeDirection.getValue()) {
                    RayTraceResult facingResult = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + (double) mc.player.getEyeHeight(), mc.player.posZ), new Vec3d((double) placePosition.getPosition().getX() + 0.5, (double) placePosition.getPosition().getY() - 0.5, (double) placePosition.getPosition().getZ() + 0.5));
                    placementFacing = facingResult == null || facingResult.sideHit == null ? EnumFacing.UP : facingResult.sideHit;
                }

                // place the crystal
                {
                    if (placeAttempts.getValue() > 1) {
                        for (int placeAttempt = 0; placeAttempt < placeAttempts.getValue(); placeAttempt++) {
                            placeCrystal(placePosition.getPosition(), placeDirection.getValue() ? EnumFacing.UP : placementFacing, placePacket.getValue());
                        }
                    }

                    else {
                        placeCrystal(placePosition.getPosition(), placeDirection.getValue() ? EnumFacing.UP : placementFacing, placePacket.getValue());
                    }
                }

                PlayerUtil.swingArm(placeHand.getValue());

                // switch back after placing, should only switch serverside
                if (placeSwitch.getValue().equals(Switch.PACKET)) {
                    InventoryUtil.switchToSlot(previousSlot, Switch.NORMAL);

                    if (previousHand != null && Mouse.isButtonDown(1))
                        mc.player.setActiveHand(previousHand);
                }

                placeTimer.reset();
            }
        }
    }

    public Crystal searchCrystal() {
        if (explode.getValue()) {
            // map of viable crystals
            TreeMap<Float, Crystal> crystalMap = new TreeMap<>();

            for (Entity calculatedCrystal : mc.world.loadedEntityList) {
                // make sure it's a viable crystal
                if (!(calculatedCrystal instanceof EntityEnderCrystal) || calculatedCrystal.isDead)
                    continue;

                // make sure it's in range
                float distance = mc.player.getDistance(calculatedCrystal);
                if (distance > explodeRange.getValue() || (!mc.player.canEntityBeSeen(calculatedCrystal) && distance > explodeWall.getValue()))
                    continue;

                // make sure it doesn't do too much dmg to us or kill us
                float localDamage = mc.player.capabilities.isCreativeMode ? 0 : ExplosionUtil.getDamageFromExplosion(calculatedCrystal.posX, calculatedCrystal.posY, calculatedCrystal.posZ, mc.player, ignoreTerrain.getValue(), false);
                if (localDamage > explodeLocal.getValue() || (localDamage + 1 > PlayerUtil.getHealth() && pauseSafety.getValue()))
                    continue;

                // check if we've attacked this crystal too many times
                if (attemptedExplosions.containsKey(calculatedCrystal.getEntityId()) && explodeLimit.getValue() < 10) {
                    if (attemptedExplosions.get(calculatedCrystal.getEntityId()) > explodeLimit.getValue())
                        continue;
                }

                for (EntityPlayer calculatedTarget : mc.world.playerEntities) {
                    // make sure the target is not dead, a friend, or the local player
                    if (calculatedTarget.equals(mc.player) || EnemyUtil.isDead(calculatedTarget) || Cosmos.INSTANCE.getSocialManager().getSocial(calculatedTarget.getName()).equals(Relationship.FRIEND))
                        continue;

                    // make sure target's within our specified target range
                    float targetDistance = mc.player.getDistance(calculatedTarget);
                    if (targetDistance > targetRange.getValue())
                        continue;

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

        return null;
    }

    public CrystalPosition searchPosition() {
        if (place.getValue()) {
            // map of viable positions
            TreeMap<Float, CrystalPosition> positionMap = new TreeMap<>();

            for (BlockPos calculatedPosition : BlockUtil.getSurroundingBlocks(mc.player, placeRange.getValue(), false)) {
                // make sure it's actually a viable position
                if (!canPlaceCrystal(calculatedPosition, placements.getValue()))
                    continue;

                // make sure it doesn't do too much dmg to us or kill us
                float localDamage = mc.player.capabilities.isCreativeMode ? 0 : ExplosionUtil.getDamageFromExplosion(calculatedPosition.getX() + 0.5, calculatedPosition.getY() + 1, calculatedPosition.getZ() + 0.5, mc.player, ignoreTerrain.getValue(), false);
                if (localDamage > placeLocal.getValue() || (localDamage + 1 > PlayerUtil.getHealth() && pauseSafety.getValue()))
                    continue;

                // if the block above the one we can't see through is air, then NCP won't flag us for placing at normal ranges
                boolean wallPlacement = !placeRaytrace.getValue().equals(Raytrace.NONE) && RaytraceUtil.raytraceBlock(calculatedPosition, placeRaytrace.getValue());

                // if it is a wall placement, use our wall ranges
                double distance = mc.player.getDistance(calculatedPosition.getX() + 0.5, calculatedPosition.getY() + 1, calculatedPosition.getZ() + 0.5);
                if (distance > placeWall.getValue() && wallPlacement)
                    continue;

                for (EntityPlayer calculatedTarget : mc.world.playerEntities) {
                    // make sure the target is not dead, a friend, or the local player
                    if (calculatedTarget.equals(mc.player) || EnemyUtil.isDead(calculatedTarget) || Cosmos.INSTANCE.getSocialManager().getSocial(calculatedTarget.getName()).equals(Relationship.FRIEND))
                        continue;

                    // make sure target's within our specified target range
                    float targetDistance = mc.player.getDistance(calculatedTarget);
                    if (targetDistance > targetRange.getValue())
                        continue;

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
                    if (idealPosition.getTargetDamage() * overrideThreshold.getValue() >= EnemyUtil.getHealth(idealPosition.getPlaceTarget()))
                        requiredDamage = 0.5;

                    if (HoleUtil.isInHole(idealPosition.getPlaceTarget())) {
                        if (EnemyUtil.getHealth(idealPosition.getPlaceTarget()) < overrideHealth.getValue())
                            requiredDamage = 0.5;

                        if (EnemyUtil.getArmor(idealPosition.getPlaceTarget(), overrideArmor.getValue()))
                            requiredDamage = 0.5;
                    }
                }

                // verify if the ideal position meets our requirements, if it doesn't it automatically rules out all other placements
                if (idealPosition.getTargetDamage() > requiredDamage) {
                    return idealPosition;
                }
            }
        }

        return null;
    }

    @Override
    public void onRender3D() {
        if (render.getValue() && placePosition != null && !placePosition.getPosition().equals(BlockPos.ORIGIN) && (InventoryUtil.isHolding(Items.END_CRYSTAL) || placeSwitch.getValue().equals(Switch.PACKET))) {
            RenderUtil.drawBox(new RenderBuilder().position(placePosition.getPosition()).color(renderColor.getValue()).box(renderMode.getValue()).setup().line((float) ((double) renderWidth.getValue())).cull(renderMode.getValue().equals(Box.GLOW) || renderMode.getValue().equals(Box.REVERSE)).shade(renderMode.getValue().equals(Box.GLOW) || renderMode.getValue().equals(Box.REVERSE)).alpha(renderMode.getValue().equals(Box.GLOW) || renderMode.getValue().equals(Box.REVERSE)).depth(true).blend().texture());
            RenderUtil.drawNametag(placePosition.getPosition(), 0.5F, getText(renderText.getValue()));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if ((event.getPacket() instanceof CPacketPlayer) && crystalRotation != null && rotate.getValue().equals(Rotate.PACKET)) {
            // split up the rotation into multiple packets, NCP flags for quick rotations
            if (Math.abs(crystalRotation.getYaw() - mc.player.rotationYaw) >= 20 || Math.abs(crystalRotation.getPitch() - mc.player.rotationPitch) >= 20) {
                for (float step = rotateStep.getValue() - 1; step > 0; step--) {
                    mc.player.connection.sendPacket(new CPacketPlayer.Rotation(crystalRotation.getYaw() / step + 1, crystalRotation.getPitch() / step + 1, mc.player.onGround));
                }
            }

            ((ICPacketPlayer) event.getPacket()).setYaw(crystalRotation.getYaw());
            ((ICPacketPlayer) event.getPacket()).setPitch(crystalRotation.getPitch());
        }
        
        if (event.getPacket() instanceof CPacketHeldItemChange) {
            switchTimer.reset();
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {
        if (event.getPacket() instanceof SPacketSpawnObject && ((SPacketSpawnObject) event.getPacket()).getType() == 51 && timing.getValue().equals(Timing.LINEAR) && explode.getValue()) {
            // position of the placed crystal
            BlockPos linearPosition = new BlockPos(((SPacketSpawnObject) event.getPacket()).getX(), ((SPacketSpawnObject) event.getPacket()).getY(), ((SPacketSpawnObject) event.getPacket()).getZ());

            // if the block above the one we can't see through is air, then NCP won't flag us for placing at normal ranges
            boolean wallPlacement = !placeRaytrace.getValue().equals(Raytrace.NONE) && RaytraceUtil.raytraceBlock(linearPosition, placeRaytrace.getValue());

            // if it is a wall placement, use our wall ranges
            double distance = mc.player.getDistance(linearPosition.getX() + 0.5, linearPosition.getY() + 1, linearPosition.getZ() + 0.5);
            if (distance > explodeWall.getValue() && wallPlacement)
                return;

            // make sure it doesn't do too much dmg to us or kill us
            float localDamage = ExplosionUtil.getDamageFromExplosion(linearPosition.getX() + 0.5, linearPosition.getY() + 1, linearPosition.getZ() + 0.5, mc.player, ignoreTerrain.getValue(), false);
            if (localDamage > explodeLocal.getValue() || (localDamage + 1 > PlayerUtil.getHealth() && pauseSafety.getValue()))
                return;

            TreeMap<Float, Float> linearMap = new TreeMap<>();
            for (EntityPlayer calculatedTarget : mc.world.playerEntities) {
                // make sure the target is not dead or the local player
                if (calculatedTarget.equals(mc.player) || EnemyUtil.isDead(calculatedTarget))
                    continue;

                // make sure target's within our specified target range
                float targetDistance = mc.player.getDistance(calculatedTarget);
                if (targetDistance > targetRange.getValue())
                    continue;

                // calculate the damage this crystal will do to each target, we can verify if it meets our requirements later
                float targetDamage = calculateLogic(ExplosionUtil.getDamageFromExplosion(linearPosition.getX() + 0.5, linearPosition.getY() + 1, linearPosition.getZ() + 0.5, calculatedTarget, ignoreTerrain.getValue(), false), localDamage, distance);

                linearMap.put(targetDamage, targetDamage);
            }

            if (!linearMap.isEmpty()) {
                float idealLinear = linearMap.lastEntry().getValue();

                // make sure it meets requirements
                if (idealLinear > explodeDamage.getValue()) {
                    // explode the linear crystal
                    explodeCrystal(((SPacketSpawnObject) event.getPacket()).getEntityID());

                    // add crystal to our list of attempted explosions
                    attemptedExplosions.put(((SPacketSpawnObject) event.getPacket()).getEntityID(), attemptedExplosions.containsKey(((SPacketSpawnObject) event.getPacket()).getEntityID()) ? attemptedExplosions.get(((SPacketSpawnObject) event.getPacket()).getEntityID()) + 1 : 1);
                }
            }
        }

        // packet for crystal explosions
        if (event.getPacket() instanceof SPacketSoundEffect && ((SPacketSoundEffect) event.getPacket()).getSound().equals(SoundEvents.ENTITY_GENERIC_EXPLODE) && ((SPacketSoundEffect) event.getPacket()).getCategory().equals(SoundCategory.BLOCKS)) {
            mc.addScheduledTask(() -> new ArrayList<>(mc.world.loadedEntityList).stream().filter(entity -> entity instanceof EntityEnderCrystal).filter(entity -> mc.player.getDistance(entity) < 6).filter(entity -> attemptedExplosions.containsKey(entity.getEntityId())).forEach(entity -> {
                // the world sets the crystal dead one tick after this packet, but we can speed up the placements by setting it dead here
                if (sync.getValue().equals(Sync.SOUND)) {
                    entity.setDead();
                    mc.world.removeEntityFromWorld(entity.getEntityId());
                }
            }));
        }
    }

    public void placeCrystal(BlockPos placePos, EnumFacing enumFacing, boolean packet) {
        if (packet)
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(placePos, enumFacing, mc.player.getHeldItemOffhand().getItem().equals(Items.END_CRYSTAL) ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.5F, 0.5F, 0.5F));
        else
            mc.playerController.processRightClickBlock(mc.player, mc.world, placePos, enumFacing, new Vec3d(0, 0, 0), mc.player.getHeldItemOffhand().getItem().equals(Items.END_CRYSTAL) ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
    }

    public void explodeCrystal(EntityEnderCrystal crystal, boolean packet) {
        if (packet)
            mc.player.connection.sendPacket(new CPacketUseEntity(crystal));
        else
            mc.playerController.attackEntity(mc.player, crystal);
    }

    @SuppressWarnings("all")
    public void explodeCrystal(int entityId) {
        CPacketUseEntity attackPacket = new CPacketUseEntity();
        ((ICPacketUseEntity) attackPacket).setID(entityId);
        ((ICPacketUseEntity) attackPacket).setAction(CPacketUseEntity.Action.ATTACK);
        mc.player.connection.sendPacket(attackPacket);
    }

    public boolean canPlaceCrystal(BlockPos blockPos, Placements placements) {
        try {
            if (!mc.world.getBlockState(blockPos).getBlock().equals(Blocks.BEDROCK) && !mc.world.getBlockState(blockPos).getBlock().equals(Blocks.OBSIDIAN))
                return false;

            for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(blockPos.add(0, 1, 0)))) {
                if (entity.isDead || (entity instanceof EntityEnderCrystal && entity.getPosition().equals(blockPos.add(0, 1, 0))))
                    continue;

                return false;
            }

            switch (placements) {
                case NATIVE:
                default:
                    return mc.world.getBlockState(blockPos.add(0, 2, 0)).getBlock().equals(Blocks.AIR) && mc.world.getBlockState(blockPos.add(0, 1, 0)).getBlock().equals(Blocks.AIR);
                case UPDATED:
                    return mc.world.getBlockState(blockPos.add(0, 1, 0)).getBlock().equals(Blocks.AIR);
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
            case ATOMIC:
                return targetDamage - selfDamage - (float) distance;
            case VOLATILE:
                return targetDamage - (float) distance;
        }
    }

    public String getRenderInfo() {
       switch (renderInfo.getValue()) {
            case DAMAGE:
                return String.valueOf(MathUtil.roundDouble(placePosition.getTargetDamage(), 1));
            case LATENCY:
                return String.valueOf(explodeTimer.getMS(System.nanoTime() - explodeTimer.time) / 100);
            case TARGET:
                return placePosition.getPlaceTarget().getName();
            case NONE:
            default:
                return "";
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
            default:
                return "";
        }
    }

    public enum Placements {
        NATIVE, UPDATED
    }

    public enum Timing {
        LINEAR, SEQUENTIAL, TICK
    }

    public enum Sync {
        SOUND, INSTANT, NONE
    }

    public enum Logic {
        DAMAGE, MINIMAX, ATOMIC, VOLATILE
    }

    public enum When {
        BREAK, PLACE, BOTH
    }

    public enum Info {
        DAMAGE, LATENCY, TARGET, NONE
    }

    public enum Text {
        TARGET, SELF, BOTH, NONE
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