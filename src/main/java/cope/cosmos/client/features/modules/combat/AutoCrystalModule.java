package cope.cosmos.client.features.modules.combat;

import com.mojang.realmsclient.util.Pair;
import cope.cosmos.asm.mixins.accessor.ICPacketUseEntity;
import cope.cosmos.asm.mixins.accessor.INetworkManager;
import cope.cosmos.asm.mixins.accessor.IPlayerControllerMP;
import cope.cosmos.client.events.combat.TotemPopEvent;
import cope.cosmos.client.events.entity.EntityWorldEvent;
import cope.cosmos.client.events.entity.player.RotationUpdateEvent;
import cope.cosmos.client.events.entity.player.interact.RightClickItemEvent;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.events.render.entity.RenderRotationsEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.combat.AutoCrystalModule.Violation.ViolationTag;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.InventoryManager.Switch;
import cope.cosmos.client.manager.managers.SocialManager.Relationship;
import cope.cosmos.util.combat.EnemyUtil;
import cope.cosmos.util.combat.ExplosionUtil;
import cope.cosmos.util.entity.EntityUtil;
import cope.cosmos.util.holder.Rotation;
import cope.cosmos.util.holder.Rotation.Rotate;
import cope.cosmos.util.math.MathUtil;
import cope.cosmos.util.math.Timer;
import cope.cosmos.util.math.Timer.Format;
import cope.cosmos.util.player.AngleUtil;
import cope.cosmos.util.player.InventoryUtil;
import cope.cosmos.util.player.PlayerUtil;
import cope.cosmos.util.render.RenderBuilder;
import cope.cosmos.util.render.RenderBuilder.Box;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.string.ColorUtil;
import cope.cosmos.util.world.BlockUtil;
import cope.cosmos.util.world.RaytraceUtil;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.*;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.client.CPacketUseEntity.Action;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author linustouchtips
 * @since 02/26/2022
 */
public class AutoCrystalModule extends Module {
    public static AutoCrystalModule INSTANCE;

    public AutoCrystalModule() {
        super("AutoCrystal", Category.COMBAT, "Places and explodes crystals", () -> {

            // **************************** TESTING INFO ****************************

            // response time total
            float responseTime = MathUtil.roundFloat(ping / 10F, 1);

            // INFO
            return String.valueOf(responseTime);
        });

        INSTANCE = this;
    }

    // **************************** anticheat settings ****************************

    public static Setting<Boolean> multiTask = new Setting<>("MultiTask", true)
            .setDescription("Explodes only if we are not preforming any actions with our hands");

    public static Setting<Boolean> swing = new Setting<>("Swing", true)
            .setDescription("Swings the players hand when attacking and placing");

    public static Setting<Timing> timing = new Setting<>("Timing", Timing.SEQUENTIAL)
            .setDescription("Timing for processes");

    // TODO: fix rotation resetting
    public static Setting<Rotate> rotate = new Setting<>("Rotate", Rotate.NONE)
            .setDescription("Rotate to the current process");

    public static Setting<Double> maxAngle = new Setting<>("MaxAngle", 1.0, 180.0, 180.0, 0)
            .setDescription("Max angle to rotate in one tick")
            .setVisible(() -> !rotate.getValue().equals(Rotate.NONE));

    public static Setting<Double> visibilityTicks = new Setting<>("VisibilityTicks", 0.0, 0.0, 5.0, 0)
            .setDescription("How many ticks you need to stay looking at the current process before continuing")
            .setVisible(() -> !rotate.getValue().equals(Rotate.NONE));

    public static Setting<Double> switchDelay = new Setting<>("SwitchDelay", 0.0, 0.0, 10.0, 1)
            .setDescription("Delay to pause after switching items");

    // **************************** general settings ****************************

    // TODO: Is this even necessary?? Kinda, but is it worth the trouble of implementing??
    // public static Setting<Trace> trace = new Setting<>("Trace", Trace.FULL)
    //        .setDescription("How to checks visibility");

    public static Setting<Raytrace> raytrace = new Setting<>("Raytrace", Raytrace.LENIENT)
            .setDescription("Verifies placements through walls");

    public static Setting<Merge> merge = new Setting<>("Merge", Merge.CONFIRM)
            .setDescription("How to synchronize crystal explosions");

    public static Setting<Double> offset = new Setting<>("Offset", 1.0, 2.0, 2.0, 0)
            .setDescription("Crystal placement offset");

    // **************************** damage settings ****************************

    // TODO: adjust preferred damages when on MINIMAX
    public static Setting<Heuristic> heuristic = new Setting<>("Heuristic", Heuristic.MAX)
            .setDescription("Heuristic for damage algorithm");

    public static Setting<Double> damage = new Setting<>("Damage", 2.0, 4.0, 10.0, 1)
            .setDescription("Minimum damage done by an action");

    public static Setting<Double> lethalMultiplier = new Setting<>("LethalMultiplier", 0.0, 1.0, 5.0, 1)
            .setDescription("Will override damages if we can kill the target in this many crystals");

    public static Setting<Double> armorScale = new Setting<>("ArmorScale", 0.0, 10.0, 100.0, 0)
            .setDescription("Will override damages if we can break the target's armor");

    public static Setting<AtomicInteger> force = new Setting<>("Force", new AtomicInteger(Keyboard.KEY_NONE))
            .setDescription("Will force damage override when key is pressed");

    // TODO: make AntiSuicide function well, should never kill/pop you
    public static Setting<Safety> safety = new Setting<>("Safety", Safety.BALANCE)
            .setDescription("When to consider actions safe");

    public static Setting<Double> safetyBalance = new Setting<>("SafetyBalance", 0.1, 1.1, 3.0, 1)
            .setDescription("Multiplier for actions considered unsafe")
            .setVisible(() -> safety.getValue().equals(Safety.BALANCE));

    public static Setting<Boolean> blockDestruction = new Setting<>("BlockDestruction", false)
            .setDescription("Ignores terrain that can be exploded when calculating damages");

    // **************************** explode settings ****************************

    public static Setting<Boolean> explode = new Setting<>("Explode", true)
            .setDescription("Explodes crystals");

    public static Setting<Double> explodeSpeed = new Setting<>("ExplodeSpeed", 1.0, 20.0, 20.0, 1)
            .setDescription("Speed to explode crystals")
            .setVisible(() -> explode.getValue());

    public static Setting<Double> explodeFactor = new Setting<>("ExplodeFactor", 0.0, 3.0, 5.0, 0)
            .setDescription("Factor to explode crystals")
            .setVisible(() -> explode.getValue() && timing.getValue().equals(Timing.VANILLA));

    public static Setting<Double> explodeRange = new Setting<>("ExplodeRange", 1.0, 5.0, 6.0, 1)
            .setDescription("Range to explode crystals")
            .setVisible(() -> explode.getValue());

    public static Setting<Double> explodeWallRange = new Setting<>("ExplodeWallRange", 1.0, 3.5, 6.0, 1)
            .setDescription("Range to explode crystals through walls")
            .setVisible(() -> explode.getValue());

    public static Setting<Double> maxCrystals = new Setting<>("MaxCrystals", 1.0, 1.0, 5.0, 0)
            .setDescription("Maximum number of crystals allowed to be exploded in one tick")
            .setVisible(() -> explode.getValue());

    public static Setting<Double> ticksExisted = new Setting<>("TicksExisted", 0.0, 0.0, 5.0, 0)
            .setDescription("Minimum age of the crystal")
            .setVisible(() -> explode.getValue());

    // TODO: fix (FIX), WTF IS THIS FUCKING SETTING (need super secret bypass)
    public static Setting<Boolean> inhibit = new Setting<>("Inhibit", false)
            .setDescription("Prevents excessive attacks on crystals")
            .setVisible(() -> explode.getValue());

    // **************************** place settings ****************************

    public static Setting<Boolean> place = new Setting<>("Place", true)
            .setDescription("Places crystals");

    public static Setting<Placements> placements = new Setting<>("Placements", Placements.NATIVE)
            .setDescription("Placement calculations for current version")
            .setVisible(() -> place.getValue());

    public static Setting<Interact> interact = new Setting<>("Interact", Interact.VANILLA)
            .setDescription("Limits the direction of placements")
            .setVisible(() -> place.getValue());

    public static Setting<Double> placeSpeed = new Setting<>("PlaceSpeed", 1.0, 20.0, 20.0, 1)
            .setDescription("Speed to place crystals")
            .setVisible(() -> place.getValue());

    public static Setting<Double> placeRange = new Setting<>("PlaceRange", 1.0, 5.0, 6.0, 1)
            .setDescription("Range to place crystals")
            .setVisible(() -> place.getValue());

    public static Setting<Double> placeWallRange = new Setting<>("PlaceWallRange", 1.0, 3.5, 6.0, 1)
            .setDescription("Range to place crystals through walls")
            .setVisible(() -> place.getValue());

    // lel
    public static Setting<Double> yieldProtection = new Setting<>("YieldProtection", 0.0, 0.0, 3.0, 1)
            .setDescription("Sacrifices consistency for long term maintenance")
            .setVisible(() -> inhibit.getValue());

    public static Setting<Switch> autoSwitch = new Setting<>("Switch", Switch.NONE)
            .setDescription("How to switch to crystals")
            .setVisible(() -> place.getValue());

    public static Setting<Boolean> await = new Setting<>("Await", false)
            .setDescription("Waits for processes to clear before continuing");

    // **************************** target settings ****************************

    public static Setting<Boolean> targetPlayers = new Setting<>("TargetPlayers", true)
            .setDescription("Target players");

    public static Setting<Boolean> targetPassives = new Setting<>("TargetPassives", false)
            .setDescription("Target passives");

    public static Setting<Boolean> targetNeutrals = new Setting<>("TargetNeutrals", false)
            .setDescription("Target neutrals");

    public static Setting<Boolean> targetHostiles = new Setting<>("TargetHostiles", false)
            .setDescription("Target hostiles");

    public static Setting<Double> targetRange = new Setting<>("TargetRange", 0.1, 10.0, 15.0, 1)
            .setDescription("Range to consider an entity as a target");

    // **************************** render settings ****************************

    public static Setting<Boolean> render = new Setting<>("Render", true)
            .setDescription("Renders the current process");

    public static Setting<Text> renderText = new Setting<>("RenderText", Text.NONE)
            .setDescription("Renders the damage of the current process")
            .setVisible(() -> render.getValue());

    // **************************** ping ****************************

    // process ping
    private static long ping;

    // **************************** rotation ****************************

    // vector that holds the angle we are looking at
    private Vec3d angleVector;

    // rotation angels
    private Rotation rotateAngles;

    // rotate wait
    private boolean rotationLimit;

    // **************************** ticks ****************************

    // ticks to pause the process
    private int waitTicks;

    // ticks to wait after switching
    private int switchTicks = 10;

    // **************************** explode ****************************

    // explode timers
    private final Timer explodeTimer = new Timer();
    private final Timer switchTimer = new Timer();
    private final Timer factorTimer = new Timer();

    // last attack time
    private final Timer lastAttackTimer = new Timer();

    // list of explode-able crystals
    private Set<EntityEnderCrystal> explodeCrystals = new ConcurrentSet<>();

    // map of all attacked crystals
    private final Map<Integer, Long> attackedCrystals = new ConcurrentHashMap<>();

    // inhibit
    private final Timer inhibitTimer = new Timer();
    private final Set<Integer> inhibitCrystals = new ConcurrentSet<>();

    // yield
    private final Timer yieldTimer = new Timer();
    private final Timer yieldProtectedTimer = new Timer();

    // how many crystals we've attacked this tick
    private int attackedCrystalCount;

    // **************************** place ****************************

    // place timers
    private final Timer placeTimer = new Timer();

    // placement
    private DamageHolder<BlockPos> placement;

    // map of all placed crystals
    private final Map<BlockPos, Long> placedCrystals = new ConcurrentHashMap<>();
    private final List<BlockPos> manualCrystals = new ArrayList<>();

    // **************************** violation ****************************

    // violation list
    private final List<Violation<?>> violations = new ArrayList<>();

    // **************************** pops ****************************

    // map of all latest totem pop times
    private final Map<Entity, Long> latestTotemPops = new ConcurrentHashMap<>();

    @Override
    public void onThread() {

        // find new crystals and placements
        placement = getPlacement();
        explodeCrystals = getCrystals();

        // find violations
        if (mc.getConnection() != null) {

            // response time projection
            long responseTime = Math.max(mc.getConnection().getPlayerInfo(mc.player.getUniqueID()).getResponseTime() + 50, 100) + 150;

            // check our placed crystals
            placedCrystals.forEach((position, time) -> {

                // check if we passed predicted response time
                if (System.currentTimeMillis() - time >= responseTime) {

                    // add violation
                    violations.add(new Violation<>(position, ViolationTag.PLACE_NO_SPAWN));

                    // remove from placed crystals
                    placedCrystals.remove(position);
                }
            });

            // check our attacked crystals
            attackedCrystals.forEach((crystal, time) -> {

                // check if we passed predicted response time
                if (System.currentTimeMillis() - time >= responseTime) {

                    // add violation
                    violations.add(new Violation<>(crystal, ViolationTag.ATTACK_NO_EXPLODE));

                    // remove from attacked crystals
                    attackedCrystals.remove(crystal);
                }
            });
        }
    }

    @Override
    public void onUpdate() {

        // we haven't attacked any crystals this tick
        attackedCrystalCount = 0;

        // we are cleared to process our calculations
        if (waitTicks <= 0) {

            // update ticks before switching
            switchTicks++;

            // we are no longer waiting
            rotationLimit = true;

            // we found crystals to explode
            if (explodeCrystals != null && !explodeCrystals.isEmpty()) {

                // passed explode delay??
                if ((explodeSpeed.getValue() >= explodeSpeed.getMax() || explodeTimer.passedTime((explodeSpeed.getMax().longValue() - explodeSpeed.getValue().longValue()) * 50, Format.MILLISECONDS)) && switchTimer.passedTime(switchDelay.getValue().longValue() * 25L, Format.MILLISECONDS)) {

                    // attack crystals
                    explodeCrystals.forEach(crystal -> {

                        // face the crystal
                        angleVector = crystal.getPositionVector();

                        if (attackCrystal(crystal.getEntityId())) {

                            // update attack count
                            attackedCrystalCount++;

                            // add it to our list of attacked crystals
                            attackedCrystals.put(crystal.getEntityId(), System.currentTimeMillis());
                        }
                    });

                    // we attempted to attack these crystals
                    explodeTimer.resetTime();
                }
            }

            // we found a placement
            if (placement != null) {

                // passed place delay??
                if (placeSpeed.getValue() >= placeSpeed.getMax() || placeTimer.passedTime((placeSpeed.getMax().longValue() - placeSpeed.getValue().longValue()) * 50, Format.MILLISECONDS)) {

                    // face the placement
                    angleVector = new Vec3d(placement.getDamageSource()).addVector(0.5, 0.5, 0.5);

                    // place the crystal
                    if (placeCrystal(placement.getDamageSource())) {

                        // add it to our list of attacked crystals
                        placedCrystals.put(placement.getDamageSource(), System.currentTimeMillis());
                    }

                    // we attempted to place on these positions
                    placeTimer.resetTime();
                }
            }
        }

        else {
            waitTicks--;
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();

        // no yield on enable
        yieldTimer.resetTime();
        // yieldProtectedTimer.resetTime();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // clear lists and reset variables
        angleVector = null;
        rotateAngles = null;
        placement = null;
        rotationLimit = false;
        waitTicks = 0;
        attackedCrystalCount = 0;
        violations.clear();
        attackedCrystals.clear();
        placedCrystals.clear();
        manualCrystals.clear();

        // make sure it exists
        if (explodeCrystals != null) {
            explodeCrystals.clear();
        }

        else {
            explodeCrystals = new ConcurrentSet<>();
        }

        inhibitCrystals.clear();
        explodeTimer.resetTime();
        switchTimer.resetTime();
        factorTimer.resetTime();
        lastAttackTimer.resetTime();
        placeTimer.resetTime();
        inhibitTimer.resetTime();
        yieldTimer.resetTime();
        yieldProtectedTimer.resetTime();
    }

    @Override
    public void onRender3D() {

        // render our current placement
        if (render.getValue() && placement != null) {

            // only render if we are holding crystals
            if (InventoryUtil.isHolding(Items.END_CRYSTAL) || autoSwitch.getValue().equals(Switch.PACKET)) {

                // draw a box at the position
                RenderUtil.drawBox(new RenderBuilder()
                        .position(placement.getDamageSource())
                        .color(ColorUtil.getPrimaryAlphaColor(60))
                        .box(Box.BOTH)
                        .setup()
                        .line(1.5F)
                        .depth(true)
                        .blend()
                        .texture()
                );

                // placement nametags
                if (!renderText.getValue().equals(Text.NONE)) {

                    // damage info rounded
                    double targetDamageRounded = MathUtil.roundDouble(placement.getTargetDamage(), 1);
                    double localDamageRounded = MathUtil.roundDouble(placement.getLocalDamage(), 1);

                    if (renderText.getValue().equals(Text.BOTH)) {

                        // placement info
                        String placementInfoLineUpper = "Target: " + targetDamageRounded;
                        String placementInfoLineLower = "Local: " + localDamageRounded;

                        // draw the upper info
                        RenderUtil.drawNametag(
                                placement.getDamageSource(),
                                0.7F,
                                placementInfoLineUpper
                        );

                        // draw the lower info
                        RenderUtil.drawNametag(
                                placement.getDamageSource(),
                                0.3F,
                                placementInfoLineLower
                        );
                    }

                    else {

                        // placement info
                        String placementInfo;

                        // get placement text
                        switch (renderText.getValue()) {
                            case TARGET:
                            default:
                                placementInfo = String.valueOf(targetDamageRounded);
                                break;
                            case LOCAL:
                                placementInfo = String.valueOf(localDamageRounded);
                                break;
                        }

                        // draw the placement info
                        RenderUtil.drawNametag(
                                placement.getDamageSource(),
                                0.5F,
                                placementInfo
                        );
                    }
                }
            }
        }
    }

    @Override
    public boolean isActive() {
        return isEnabled() && (explodeCrystals != null && !explodeCrystals.isEmpty()) || placement != null;
    }

    @SuppressWarnings("all")
    @SubscribeEvent
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {

        // list of entities in world
        // Iterator<Entity> entityList = mc.world.loadedEntityList.iterator();

        // packet for crystal spawns
        if (event.getPacket() instanceof SPacketSpawnObject && ((SPacketSpawnObject) event.getPacket()).getType() == 51) {

            // position of the spawned crystal
            BlockPos spawnPosition = new BlockPos(((SPacketSpawnObject) event.getPacket()).getX(), ((SPacketSpawnObject) event.getPacket()).getY(), ((SPacketSpawnObject) event.getPacket()).getZ());

            // **************************** TIMING ****************************

            // since it's been confirmed that the crystal spawned, we can move on to our next process
            if (timing.getValue().equals(Timing.SEQUENTIAL)) {

                // clear
                if (await.getValue()) {

                    // clear our timers
                    explodeTimer.setTime((explodeSpeed.getMax().longValue() - explodeSpeed.getValue().longValue()) * 50, Format.MILLISECONDS);
                    factorTimer.setTime((explodeFactor.getMax().longValue() - explodeFactor.getValue().longValue()) * 50, Format.MILLISECONDS);
                }
            }

            else if (timing.getValue().equals(Timing.VANILLA)) {

                // explode when crystal spawns
                if (explode.getValue()) {

                    /*
                     * Map of valid crystals
                     * Sorted by natural ordering of keys
                     * Using tree map allows time complexity of O(logN)
                     */
                    TreeMap<Double, DamageHolder<Integer>> validCrystals = new TreeMap<>();

                    // yield protected; update timer
                    {
                        if (isProtectedByYield()) {
                            yieldProtectedTimer.resetTime();
                        }

                        // unprotected yield, don't clear timer
                        else {
                            yieldProtectedTimer.setTime(1000, Format.SECONDS);
                        }
                    }

                    // make sure the crystal isn't already set to be exploded; inhibit
                    if ((attackedCrystals.containsKey(((SPacketSpawnObject) event.getPacket()).getEntityID()) || inhibitCrystals.contains(((SPacketSpawnObject) event.getPacket()).getEntityID()))) {
                        if (inhibit.getValue() && !yieldProtectedTimer.passedTime(yieldProtection.getValue().longValue() * 500L, Format.MILLISECONDS)) {
                            return;
                        }
                    }

                    // there is no chance this crystal has existed for a tick in the world
                    if (ticksExisted.getValue() > 0) {
                        return;
                    }

                    // distance to crystal
                    double crystalRange = BlockUtil.getDistanceToCenter(mc.player, spawnPosition);

                    // check if the entity is in range
                    if (crystalRange > explodeRange.getValue()) {
                        return;
                    }

                    // check if crystal is behind a wall
                    boolean isNotVisible = RaytraceUtil.isNotVisible(spawnPosition, 0.5);

                    // check if entity can be attacked through wall
                    if (isNotVisible) {
                        if (crystalRange > explodeWallRange.getValue()) {
                            return;
                        }
                    }

                    // local damage done by the crystal
                    double localDamage = mc.player.capabilities.isCreativeMode ? 0 : ExplosionUtil.getDamageFromExplosion(mc.player, new Vec3d(spawnPosition).addVector(0.5, 0, 0.5), blockDestruction.getValue());

                    // search all targets
                    for (Entity entity : new ArrayList<>(mc.world.loadedEntityList)) {

                        // next entity in the world
                        // Entity entity = entityList.next();

                        // make sure the entity actually exists
                        if (entity == null || entity.equals(mc.player) || EnemyUtil.isDead(entity) || getCosmos().getSocialManager().getSocial(entity.getName()).equals(Relationship.FRIEND)) {
                            continue;
                        }

                        // ignore crystals, they can't be targets
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
                        if (entityRange > targetRange.getValue()) {
                            continue;
                        }

                        // target damage done by the crystal
                        double targetDamage = ExplosionUtil.getDamageFromExplosion(entity, new Vec3d(spawnPosition).addVector(0.5, 0, 0.5), blockDestruction.getValue());

                        // check the safety of the crystal
                        double crystalSafety = getSafetyIndex(targetDamage, localDamage);

                        // crystal is very unsafe (latter case will kill us)
                        if (crystalSafety < 0) {
                            continue;
                        }

                        // add to map
                        validCrystals.put(targetDamage, new DamageHolder<>(((SPacketSpawnObject) event.getPacket()).getEntityID(), entity, targetDamage, localDamage));
                    }

                    // make sure we actually have some valid crystals
                    if (!validCrystals.isEmpty()) {

                        // best crystal in the map, in a TreeMap this is the last entry
                        DamageHolder<Integer> bestCrystal = validCrystals.lastEntry().getValue();

                        // no crystal under 1.5 damage is worth exploding
                        if (bestCrystal.getTargetDamage() > 1.5) {

                            // check lethality of crystal
                            boolean lethal = getLethality(bestCrystal.getTarget(), bestCrystal.getTargetDamage()) || willFailTotem(bestCrystal.getTarget(), bestCrystal.getTargetDamage());

                            // check if the damage meets our requirements
                            if (lethal || bestCrystal.getTargetDamage() > damage.getValue()) {

                                // passed factor delay??
                                if (explodeFactor.getValue() <= explodeFactor.getMin() || factorTimer.passedTime((explodeFactor.getMax().longValue() - explodeFactor.getValue().longValue()) * 100, Format.MILLISECONDS)) {

                                    // face the crystal
                                    angleVector = new Vec3d(spawnPosition).addVector(0.5, 0, 0.5);

                                    if (attackCrystal(bestCrystal.getDamageSource())) {

                                        // update attack count
                                        attackedCrystalCount++;

                                        // add it to our list of attacked crystals
                                        attackedCrystals.put(bestCrystal.getDamageSource(), System.currentTimeMillis());
                                    }

                                    // we attempted to attack on this crystal
                                    factorTimer.resetTime();
                                }
                            }
                        }
                    }
                }
            }
        }

        // packet that confirms entity removal
        if (event.getPacket() instanceof SPacketDestroyEntities) {

            // check entities
            for (int entityId : ((SPacketDestroyEntities) event.getPacket()).getEntityIDs()) {
                if (attackedCrystals.containsKey(entityId)) {

                    // since it's been confirmed that the crystal exploded, we can move on to our next process
                    if (timing.getValue().equals(Timing.SEQUENTIAL)) {

                        // clear
                        if (await.getValue()) {

                            // clear our timer
                            placeTimer.setTime((placeSpeed.getMax().longValue() - placeSpeed.getValue().longValue()) * 50, Format.MILLISECONDS);
                        }
                    }

                    break;
                }
            }
        }

        // packet for crystal explosions
        if (event.getPacket() instanceof SPacketSoundEffect && ((SPacketSoundEffect) event.getPacket()).getSound().equals(SoundEvents.ENTITY_GENERIC_EXPLODE) && ((SPacketSoundEffect) event.getPacket()).getCategory().equals(SoundCategory.BLOCKS)) {

            // schedule to main mc thread
            mc.addScheduledTask(() -> {

                // check all entities in the world
                for (Entity crystal : new ArrayList<>(mc.world.loadedEntityList)) {

                    // next entity in the world
                    // Entity crystal = entityList.next();

                    // make sure the entity actually exists
                    if (crystal == null || crystal.isDead) {
                        continue;
                    }

                    // make sure it's a crystal
                    if (!(crystal instanceof EntityEnderCrystal)) {
                        continue;
                    }

                    // entity distance from sound
                    double soundRange = crystal.getDistance(((SPacketSoundEffect) event.getPacket()).getX() + 0.5, ((SPacketSoundEffect) event.getPacket()).getY() + 0.5, ((SPacketSoundEffect) event.getPacket()).getZ() + 0.5);

                    // make sure the crystal is in range from the sound to be destroyed
                    if (soundRange > 6) {
                        continue;
                    }

                    // going to be exploded anyway, so don't attempt explosion
                    inhibitCrystals.add(crystal.getEntityId());

                    // the world sets the crystal dead one tick after this packet, but we can speed up the placements by setting it dead here
                    if (merge.getValue().equals(Merge.CONFIRM)) {
                        crystal.setDead();
                        mc.world.removeEntityDangerously(crystal);
                    }
                }
            });
        }

        if (event.getPacket() instanceof SPacketExplosion) {

            // check all entities in the world
            for (Entity crystal : new ArrayList<>(mc.world.loadedEntityList)) {

                // next entity in the world
                // Entity crystal = entityList.next();

                // make sure the entity actually exists
                if (crystal == null || crystal.isDead) {
                    continue;
                }

                // make sure it's a crystal
                if (!(crystal instanceof EntityEnderCrystal)) {
                    continue;
                }

                // entity distance from explosion
                double explosionRange = crystal.getDistance(((SPacketExplosion) event.getPacket()).getX(), ((SPacketExplosion) event.getPacket()).getY(), ((SPacketExplosion) event.getPacket()).getZ());

                // make sure the crystal is in range from the explosion to be destroyed
                if (explosionRange > ((SPacketExplosion) event.getPacket()).getStrength()) {
                    continue;
                }

                // going to be exploded anyway, so don't attempt explosion
                inhibitCrystals.add(crystal.getEntityId());

                // the world sets the crystal dead one tick after this packet, but we can speed up the placements by setting it dead here
                if (merge.getValue().equals(Merge.CONFIRM)) {
                    crystal.setDead();
                    mc.world.removeEntityDangerously(crystal);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {

        // packet for placing on a block
        if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {

            // check if we are placing a crystal
            if (mc.player.getHeldItem(((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getHand()).getItem() instanceof ItemEndCrystal) {

                // add to list of manually placed crystals
                manualCrystals.add(((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getPos());
            }
        }

        // packet for attacking
        if (event.getPacket() instanceof CPacketUseEntity) {

            // entity being attacked
            Entity attackEntity = ((CPacketUseEntity) event.getPacket()).getEntityFromWorld(mc.world);

            // make sure the attacked entity exists in the world
            if (attackEntity != null && !attackEntity.isDead) {

                // check if the attacked entity was a crystal
                if (attackEntity instanceof EntityEnderCrystal) {

                    // mark last attack time
                    lastAttackTimer.resetTime();
                }
            }
        }

        // packet for switching held item
        if (event.getPacket() instanceof CPacketHeldItemChange) {

            // reset our switch time, we just switched
            switchTimer.resetTime();

            // pause switch if item we switched to is not a crystal
            if (!(mc.player.inventory.getStackInSlot(((CPacketHeldItemChange) event.getPacket()).getSlotId()).getItem() instanceof ItemEndCrystal)) {
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

                    // server rotation
                    Rotation serverRotation = getCosmos().getRotationManager().getServerRotation();

                    // difference between current and upcoming rotation
                    float angleDifference = Math.abs(MathHelper.wrapDegrees(serverRotation.getYaw()) - rotateAngles.getYaw());

                    // rotating too fast
                    if (angleDifference > maxAngle.getValue()) {

                        // yaw wrapped
                        float yaw = MathHelper.wrapDegrees(serverRotation.getYaw()); // use server rotation, we won't be updating client rotations

                        // add max angle
                        if (rotateAngles.getYaw() >= MathHelper.wrapDegrees(serverRotation.getYaw())) {
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
    }

    @SubscribeEvent
    public void onRenderRotations(RenderRotationsEvent event) {

        // packet rotations
        if (rotate.getValue().equals(Rotate.PACKET)) {

            // render angles if rotating
            if (isActive()) {

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
    }

    @SubscribeEvent
    public void onEntitySpawn(EntityWorldEvent.EntitySpawnEvent event) {

        // crystal being added to the world
        if (event.getEntity() instanceof EntityEnderCrystal) {

            // positions of spawn
            BlockPos spawnPosition = event.getEntity().getPosition();

            // remove position from our placed crystals list
            placedCrystals.remove(spawnPosition.down());

            // manually placed crystals should ideally be placed with intention and should therefore always be exploded
            // check if this is a crystal that we manually placed
            if (manualCrystals.contains(spawnPosition.down())) {

                if (explodeCrystals != null) {

                    // add to explode crystals, we should explode crystals we manually placed
                    explodeCrystals.add((EntityEnderCrystal) event.getEntity());
                }

                // remove from manual placements
                manualCrystals.remove(spawnPosition.down());
            }

            // remove all violations associated with this position
            violations.removeIf(violation -> violation.getViolator().equals(spawnPosition.down()));
        }
    }

    @SubscribeEvent
    public void onEntityRemove(EntityWorldEvent.EntityRemoveEvent event) {

        // crystal being removed from world
        if (event.getEntity() instanceof EntityEnderCrystal) {

            // check if we've attacked this crystal
            if (attackedCrystals.containsKey(event.getEntity().getEntityId())) {

                // reset inhibit state
                inhibitTimer.resetTime();

                // remove crystal from our attacked crystals list
                ping = System.currentTimeMillis() - attackedCrystals.remove(event.getEntity().getEntityId());

                // no longer inhibited
                inhibitCrystals.remove(event.getEntity().getEntityId());
            }

            // remove all violations associated with this crystal
            violations.removeIf(violation -> violation.getViolator().equals(event.getEntity()));
        }
    }

    @SubscribeEvent
    public void onTotemPop(TotemPopEvent event) {

        // update latest totem pop time for entity
        latestTotemPops.put(event.getPopEntity(), System.currentTimeMillis());
    }

    /**
     * Attempts an attack on a specified crystal
     * @param in The crystal to attack
     * @return If we were able to attack the crystal
     */
    @SuppressWarnings("all")
    public boolean attackCrystal(int in) {

        // if we've already attack enough crystals this tick, we won't allow another attack
        if (attackedCrystalCount >= maxCrystals.getValue()) {
            return false;
        }

        // check whether a crystal is in the offhand
        boolean offhand = mc.player.getHeldItemOffhand().getItem() instanceof ItemEndCrystal;

        // must be not doing anything
        if ((PlayerUtil.isEating() && !offhand) && !multiTask.getValue()) {
            return false;
        }

        // attack packet
        CPacketUseEntity packet = new CPacketUseEntity();
        ((ICPacketUseEntity) packet).setID(in);
        ((ICPacketUseEntity) packet).setAction(Action.ATTACK);

        if (mc.getConnection() != null) {

            // player sprint state
            boolean sprintState = mc.player.isSprinting();

            // stop sprinting when attacking an entity
            if (sprintState) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
            }

            // response time projection
            long responseTime = Math.max(mc.getConnection().getPlayerInfo(mc.player.getUniqueID()).getResponseTime() + 50, 100) + 150;

            // limit attacks
            if (inhibit.getValue()) {

                // check inhibiting factor
                if (inhibitTimer.passedTime(responseTime, Format.MILLISECONDS)) {

                    // attack the crystal
                    mc.getConnection().getNetworkManager().sendPacket(packet);
                }

                 else {
                    ((INetworkManager) mc.getConnection().getNetworkManager()).hookDispatchPacket(packet, null);
                }
            }

            else {

                // attack the crystal
                mc.getConnection().getNetworkManager().sendPacket(packet);
            }

            // swing the player's arm
            if (swing.getValue()) {
                mc.player.swingArm(offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
            }

            // swing with packets
            else {
                mc.getConnection().getNetworkManager().sendPacket(new CPacketAnimation(offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND));
            }

            // remove the crystal after we break -> i.e. instantly
            if (merge.getValue().equals(Merge.FAST)) {
                mc.world.removeEntityFromWorld(in);
            }

            mc.player.resetCooldown();

            // reset sprint state
            if (sprintState) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
            }

            // we did attempt an attack, we can verify if it actually did anything later
            return true;
        }

        return false;
    }

    /**
     * Places a crystal at a specified position
     * @param in The position to place on
     * @return If we were able to place the crystal
     */
    public boolean placeCrystal(BlockPos in) {

        // make sure the position actually exits
        if (in == null) {
            return false;
        }

        // pause switch to account for eating
        if (PlayerUtil.isEating()) {
            switchTicks = 0;
        }

        // if we are not holding a crystal
        if (!InventoryUtil.isHolding(Items.END_CRYSTAL)) {

            if (autoSwitch.getValue().equals(Switch.NORMAL)) {

                // wait for switch pause
                if (switchTicks <= 10) {
                    return false;
                }
            }

            // switch to a crystal
            getCosmos().getInventoryManager().switchToItem(Items.END_CRYSTAL, autoSwitch.getValue());

            // sync item
            if (autoSwitch.getValue().equals(Switch.PACKET)) {
                ((IPlayerControllerMP) mc.playerController).hookSyncCurrentPlayItem();
            }
        }

        // make sure we are holding a crystal
        if (!InventoryUtil.isHolding(Items.END_CRYSTAL) && !autoSwitch.getValue().equals(Switch.PACKET)) {
            return false;
        }

        // directions of placement
        double facingX = 0;
        double facingY = 0;
        double facingZ = 0;

        // assume the face is visible
        EnumFacing facingDirection = EnumFacing.UP;

        // the angles to the last interaction
        Rotation vectorAngles = AngleUtil.calculateAngles(angleVector);

        // vector from the angles
        Vec3d placeVector = AngleUtil.getVectorForRotation(new Rotation(vectorAngles.getYaw(), vectorAngles.getPitch()));

        // interact vector
        RayTraceResult interactVector = mc.world.rayTraceBlocks(mc.player.getPositionEyes(1), mc.player.getPositionEyes(1).addVector(placeVector.x * placeRange.getValue(), placeVector.y * placeRange.getValue(), placeVector.z * placeRange.getValue()), false, false, true);

        // make sure the direction we are facing is consistent with our rotations
        switch (interact.getValue()) {
            case NONE:
                facingDirection = EnumFacing.DOWN;
                facingX = 0.5;
                facingY = 0.5;
                facingZ = 0.5;
                break;
            case VANILLA:

                // find the direction to place against
                RayTraceResult laxResult = mc.world.rayTraceBlocks(mc.player.getPositionEyes(1), new Vec3d(in).addVector(0.5, 0.5, 0.5));

                if (laxResult != null && laxResult.typeOfHit.equals(Type.BLOCK)) {
                    facingDirection = laxResult.sideHit;

                    // if we're at world height, we can still place a crystal if we interact with the bottom of the block, this doesn't work on strict servers
                    if (in.getY() >= (mc.world.getActualHeight() - 1)) {
                        facingDirection = EnumFacing.DOWN;
                    }
                }

                // find rotations based on the placement
                if (interactVector != null && interactVector.hitVec != null) {
                    facingX = interactVector.hitVec.x - in.getX();
                    facingY = interactVector.hitVec.y - in.getY();
                    facingZ = interactVector.hitVec.z - in.getZ();
                }

                break;
            case STRICT:

                // if the place position is likely out of sight
                if (in.getY() > mc.player.posY + mc.player.getEyeHeight()) {

                    // our nearest visible face
                    Pair<Double, EnumFacing> closestDirection = Pair.of(Double.MAX_VALUE, EnumFacing.UP);

                    // iterate through all points on the block
                    for (float x = 0; x <= 1; x += 0.05) {
                        for (float y = 0; y <= 1; y += 0.05) {
                            for (float z = 0; z <= 1; z += 0.05) {

                                // find the vector to raytrace to
                                Vec3d traceVector = new Vec3d(in).addVector(x, y, z);

                                // check visibility, raytrace to the current point
                                RayTraceResult strictResult = mc.world.rayTraceBlocks(mc.player.getPositionEyes(1), traceVector, false, true, false);

                                // if our raytrace is a block, check distances
                                if (strictResult != null && strictResult.typeOfHit.equals(Type.BLOCK)) {

                                    // distance to face
                                    double directionDistance = mc.player.getDistance(traceVector.x, traceVector.y, traceVector.z);

                                    // if the face is the closest to the player and trace distance is reasonably close, then we have found a new ideal visible side to place against
                                    if (directionDistance < closestDirection.first()) {
                                        closestDirection = Pair.of(directionDistance, strictResult.sideHit);
                                    }
                                }
                            }
                        }
                    }

                    facingDirection = closestDirection.second();
                }

                // find rotations based on the placement
                if (interactVector != null && interactVector.hitVec != null) {
                    facingX = interactVector.hitVec.x - in.getX();
                    facingY = interactVector.hitVec.y - in.getY();
                    facingZ = interactVector.hitVec.z - in.getZ();
                }

                break;
        }

        // check whether a crystal is in the offhand
        boolean offhand = mc.player.getHeldItemOffhand().getItem() instanceof ItemEndCrystal;

        // place the crystal
        mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(in, facingDirection, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, (float) facingX, (float) facingY, (float) facingZ));

        // swing the player's arm
        if (swing.getValue()) {
            mc.player.swingArm(offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
        }

        // swing with packets
        else {
            mc.player.connection.sendPacket(new CPacketAnimation(offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND));
        }

        // switch back after placing, should only switch serverside
        if (autoSwitch.getValue().equals(Switch.PACKET)) {
            getCosmos().getInventoryManager().switchToSlot(mc.player.inventory.currentItem, Switch.PACKET);
        }

        // we did attempt to place
        return true;
    }

    /**
     * Finds all explode-able crystals for this tick
     * @return A list of explode-able crystals
     */
    public Set<EntityEnderCrystal> getCrystals() {

        // find explode-able crystals
        if (explode.getValue()) {

            /*
             * Map of valid crystals
             * Sorted by natural ordering of keys
             * Using tree map allows time complexity of O(logN)
             */
            TreeMap<Double, DamageHolder<EntityEnderCrystal>> validCrystals = new TreeMap<>();

            // list of entities in the world
            // Iterator<Entity> entityList = mc.world.loadedEntityList.iterator();

            // check all entities in the world
            for (Entity crystal : new ArrayList<>(mc.world.loadedEntityList)) {

                // next entity in the world
                // Entity crystal = entityList.next();

                // make sure the entity actually exists
                if (crystal == null || crystal.isDead) {
                    continue;
                }

                // check if the entity is a crystal
                if (!(crystal instanceof EntityEnderCrystal)) {
                    continue;
                }

                // yield protected; update timer
                {
                    if (isProtectedByYield()) {
                        yieldProtectedTimer.resetTime();
                    }

                    // unprotected yield, don't clear timer
                    else {
                        yieldProtectedTimer.setTime(1000, Format.SECONDS);
                    }
                }

                // make sure the crystal isn't already set to be exploded; inhibit
                if ((attackedCrystals.containsKey(crystal.getEntityId()) || inhibitCrystals.contains(crystal.getEntityId()))) {
                    if (inhibit.getValue() && !yieldProtectedTimer.passedTime(yieldProtection.getValue().longValue() * 500L, Format.MILLISECONDS)) {
                        continue;
                    }
                }

                // make sure the crystal has existed in the world for a certain number of ticks before it's a viable target
                if (crystal.ticksExisted < ticksExisted.getValue()) {
                    continue;
                }

                // distance to crystal
                double crystalRange = mc.player.getDistance(crystal);

                // check if the entity is in range
                if (crystalRange > explodeRange.getValue()) {
                    continue;
                }

                // check if crystal is behind a wall
                boolean isNotVisible = RaytraceUtil.isNotVisible(crystal, crystal.getEyeHeight());

                // check if entity can be attacked through wall
                if (isNotVisible) {
                    if (crystalRange > explodeWallRange.getValue()) {
                        continue;
                    }
                }

                // local damage done by the crystal
                double localDamage = mc.player.capabilities.isCreativeMode ? 0 : ExplosionUtil.getDamageFromExplosion(mc.player, crystal.getPositionVector(), blockDestruction.getValue());

                // search all targets
                for (Entity entity : new ArrayList<>(mc.world.loadedEntityList)) {

                    // next entity in the world
                    // Entity entity = entityList.next();

                    // make sure the entity actually exists
                    if (entity == null || entity.equals(mc.player) || EnemyUtil.isDead(entity) || getCosmos().getSocialManager().getSocial(entity.getName()).equals(Relationship.FRIEND)) {
                        continue;
                    }

                    // ignore crystals, they can't be targets
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
                    if (entityRange > targetRange.getValue()) {
                        continue;
                    }

                    // target damage done by the crystal
                    double targetDamage = ExplosionUtil.getDamageFromExplosion(entity, crystal.getPositionVector(), blockDestruction.getValue());

                    // check the safety of the crystal
                    double crystalSafety = getSafetyIndex(targetDamage, localDamage);

                    // crystal is very unsafe (latter case will kill us)
                    if (crystalSafety < 0) {
                        continue;
                    }

                    // add to map
                    validCrystals.put(targetDamage, new DamageHolder<>((EntityEnderCrystal) crystal, entity, targetDamage, localDamage));
                }
            }

            // make sure we actually have some valid crystals
            if (!validCrystals.isEmpty()) {

                // filtered list of crystals sorted by best crystals
                Set<EntityEnderCrystal> bestCrystals = new ConcurrentSet<>();

                // find best crystal
                if (maxCrystals.getValue() <= 1) {

                    // best crystal in the map, in a TreeMap this is the last entry
                    DamageHolder<EntityEnderCrystal> bestCrystal = validCrystals.lastEntry().getValue();

                    // no crystal under 1.5 damage is worth exploding
                    if (bestCrystal.getTargetDamage() > 1.5) {

                        // check lethality of crystal
                        boolean lethal = getLethality(bestCrystal.getTarget(), bestCrystal.getTargetDamage()) || willFailTotem(bestCrystal.getTarget(), bestCrystal.getTargetDamage());

                        // check if the damage meets our requirements
                        if (lethal || bestCrystal.getTargetDamage() > damage.getValue()) {

                            // add it to our list
                            bestCrystals.add(bestCrystal.getDamageSource());
                        }
                    }
                }

                // find best crystals
                else {
                    for (int i = 0; i < maxCrystals.getValue(); i++) {

                        // best crystal in the map, in a TreeMap this is the last entry
                        DamageHolder<EntityEnderCrystal> bestCrystal = validCrystals.lastEntry().getValue();

                        // no crystal under 1.5 damage is worth exploding
                        if (bestCrystal.getTargetDamage() > 1.5) {

                            // check lethality of crystal
                            boolean lethal = getLethality(bestCrystal.getTarget(), bestCrystal.getTargetDamage()) || willFailTotem(bestCrystal.getTarget(), bestCrystal.getTargetDamage());

                            // check if the damage meets our requirements
                            if (lethal || bestCrystal.getTargetDamage() > damage.getValue()) {

                                // add it to our list
                                bestCrystals.add(bestCrystal.getDamageSource());
                                validCrystals.remove(bestCrystal.getTargetDamage(), bestCrystal);
                            }
                        }
                    }
                }

                // update list of best crystals
                return bestCrystals;
            }
        }

        return null;
    }

    /**
     * Gets the best placement for this tick
     * @return The best placement for this tick
     */
    public DamageHolder<BlockPos> getPlacement() {

        // find place-able positions
        if (place.getValue()) {

            /*
             * Map of valid placements
             * Sorted by natural ordering of keys
             * Using tree map allows time complexity of O(logN)
             */
            TreeMap<Double, DamageHolder<BlockPos>> validPlacements = new TreeMap<>();

            // list of entities in the world
            // Iterator<Entity> entityList = mc.world.loadedEntityList.iterator();

            // check all positions in range
            for (BlockPos position : BlockUtil.getBlocksInArea(mc.player, new AxisAlignedBB(
                    -placeRange.getValue(), -placeRange.getValue(), -placeRange.getValue(), placeRange.getValue(), placeRange.getValue(), placeRange.getValue() // area in range of blocks
            ))) {

                // check if a crystal can be placed at this position
                if (!canPlaceCrystal(position)) {
                    continue;
                }

                // violations associated with this placement
                long violationCount = violations
                        .stream()
                        .filter(violation -> violation.getViolator().equals(position))
                        .count();

                // distance to placement
                double placementRange = BlockUtil.getDistanceToCenter(mc.player, position);

                // check if the placement is within range
                if (placementRange > placeRange.getValue() || placementRange > explodeRange.getValue()) {
                    continue;
                }

                // if the block above the one we can't see through is air, then NCP won't flag us for placing at normal ranges
                boolean isNotVisible = RaytraceUtil.isNotVisible(position, raytrace.getValue().getOffset());

                // check if placement can be placed on through a wall
                if (isNotVisible) {
                    if (placementRange > placeWallRange.getValue() || placementRange > explodeWallRange.getValue()) {
                        continue;
                    }
                }

                // local damage done by the placement
                double localDamage = mc.player.capabilities.isCreativeMode ? 0 : ExplosionUtil.getDamageFromExplosion(mc.player, new Vec3d(position).addVector(0.5, 1, 0.5), blockDestruction.getValue());

                // search all targets
                for (Entity entity : new ArrayList<>(mc.world.loadedEntityList)) {

                    // next entity in the world
                    // Entity entity = entityList.next();

                    // make sure the entity actually exists
                    if (entity == null || entity.equals(mc.player) || EnemyUtil.isDead(entity) || getCosmos().getSocialManager().getSocial(entity.getName()).equals(Relationship.FRIEND)) {
                        continue;
                    }

                    // ignore crystals, they can't be targets
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
                    if (entityRange > targetRange.getValue()) {
                        continue;
                    }

                    // target damage done by the placement
                    double targetDamage = ExplosionUtil.getDamageFromExplosion(entity, new Vec3d(position).addVector(0.5, 1, 0.5), blockDestruction.getValue());

                    // check the safety of the placement
                    double placementSafety = getSafetyIndex(targetDamage, localDamage);

                    // placement is very unsafe (latter case will kill us)
                    if (placementSafety < 0) {
                        continue;
                    }

                    // add to map
                    validPlacements.put(targetDamage, new DamageHolder<>(position, entity, targetDamage, localDamage));
                }
            }

            // make sure we actually have some valid placements
            if (!validPlacements.isEmpty()) {

                // best placement in the map, in a TreeMap this is the last entry
                DamageHolder<BlockPos> bestPlacement = validPlacements.lastEntry().getValue();

                // no placement under 1.5 damage is worth placing
                if (bestPlacement.getTargetDamage() > 1.5) {

                    // check lethality of placement
                    boolean lethal = getLethality(bestPlacement.getTarget(), bestPlacement.getTargetDamage()) || willFailTotem(bestPlacement.getTarget(), bestPlacement.getTargetDamage());

                    // check if the damage meets our requirements
                    if (lethal || bestPlacement.getTargetDamage() > damage.getValue()) {

                        // mark it as our current placement
                        return bestPlacement;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Gets the safety index for a specific process
     * @param targetDamage The damage done to the target
     * @param localDamage The damage done to the local player
     * @return The safety index for the specified process
     */
    public double getSafetyIndex(double targetDamage, double localDamage) {

        // local health
        double health = PlayerUtil.getHealth();

        // incredibly unsafe
        if (health - localDamage <= 1) {
            return -9999;
        }

        // unsafe -> if local damage is greater than target damage
        else if (safety.getValue().equals(Safety.STABLE)) {

            // target damage and local damage scaled
            double efficiency = targetDamage - localDamage;

            // too small, we'll be fine :>
            if (efficiency < 0 && Math.abs(efficiency) < 0.25) {
                efficiency = 0;
            }

            return efficiency;
        }

        // unsafe -> if local damage is greater than balanced target damage
        else if (safety.getValue().equals(Safety.BALANCE)) {

            // balanced target damage
            double balance = targetDamage * safetyBalance.getValue();

            // balanced damage, should be proportionate to local damage
            return balance - localDamage;
        }

        // safe
        return 1;
    }

    /**
     * Gets the lethality of a given process
     * @param target The target
     * @param targetDamage The damage done to the target
     * @return The lethality of a given process
     */
    public boolean getLethality(Entity target, double targetDamage) {

        // target health
        double health = EnemyUtil.getHealth(target);

        // can kill the target very quickly
        if (health <= 2) {
            return true;
        }

        if (target instanceof EntityPlayer) {

            // total durability
            float lowestDurability = 100;

            // check durability for each piece of armor
            for (ItemStack armor : target.getArmorInventoryList()) {
                if (armor != null && !armor.getItem().equals(Items.AIR)) {

                    // durability of the armor
                    float armorDurability = (armor.getMaxDamage() - armor.getItemDamage() / (float) armor.getMaxDamage()) * 100;

                    // find lowest durability
                    if (armorDurability < lowestDurability) {
                        lowestDurability = armorDurability;
                    }
                }
            }

            // check if armor damage is significant
            if (lowestDurability <= armorScale.getValue()) {
                return true;
            }
        }

        // force key
        int forceKey = force.getValue().get();

        // check if we are holding force key
        if (Keyboard.isKeyDown(forceKey)) {
            return true;
        }

        // lethality of the current process
        double lethality = targetDamage * lethalMultiplier.getValue();

        // will kill the target
        return health - lethality <= 0.5;
    }

    /**
     * Checks whether or not a target will totem fail
     * @param entity The target to check
     * @return Whether or not a target will totem fail
     */
    public boolean willFailTotem(Entity entity, double targetDamage) {

        // target health
        double health = EnemyUtil.getHealth(entity);

        // greater than pop health
        if (health > 11) {
            return false;
        }

        // will not kill the target
        if (health > targetDamage) {
            return false;
        }

        // time elapsed since last totem pop
        long timeSinceLastPop = System.currentTimeMillis() - latestTotemPops.getOrDefault(entity, 0L);

        // check if time is within constraint
        return timeSinceLastPop <= 500L;
    }

    /**
     * Checks the yield protection
     * @return Whether the yield protection exists
     */
    public boolean isProtectedByYield() {

        if (mc.getConnection() != null) {

            // time for yield to pass threshold
            long yieldTime = Math.min(Math.max(mc.getConnection().getPlayerInfo(mc.player.getUniqueID()).getResponseTime(), 30) / 5L, 8);

            // passed yield time???
            if (yieldTimer.passedTime(yieldTime, Format.SECONDS)) {

                // reset time
                yieldTimer.resetTime();

                // check last attack time
                return !lastAttackTimer.passedTime(1, Format.SECONDS);
            }
        }

        return yieldProtection.getValue() <= yieldProtection.getMin();
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
        BlockPos nativePosition = position.up();
        BlockPos updatedPosition = nativePosition.up();

        // check if the native position is air or fire
        if (!mc.world.isAirBlock(nativePosition) && !mc.world.getBlockState(nativePosition).getMaterial().isReplaceable()) {
            return false;
        }

        // check if the updated position is air or fire
        if (placements.getValue().equals(Placements.NATIVE)) {
            if (!mc.world.isAirBlock(updatedPosition) && !mc.world.getBlockState(updatedPosition).getMaterial().isReplaceable()) {
                return false;
            }
        }

        // check for any unsafe entities in the position
        int unsafeEntities = 0;

        // check all entities in the bounding box
        for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(
                nativePosition.getX(), position.getY(), nativePosition.getZ(), nativePosition.getX() + 1, nativePosition.getY() + offset.getValue(), nativePosition.getZ() + 1 // offset for crystal bounding box, crystalpvp.cc allows you to place at a lower offset
        ))) {

            // if the entity will be removed the next tick, we can still place here
            if (entity == null || entity.isDead) {
                continue;
            }

            // if the entity is crystal, check it's on the same position
            if (entity instanceof EntityEnderCrystal && entity.getPosition().equals(nativePosition) || attackedCrystals.containsKey(entity.getEntityId()) && entity.ticksExisted < 20) {
                continue;
            }

            unsafeEntities++;
        }

        // make sure there are not unsafe entities at the place position
        return unsafeEntities <= 0;
    }

    public enum Timing {

        /**
         * Times the explosions based on when the crystal spawns
         */
        SEQUENTIAL,

        /**
         * Times the explosions based on when the last process has completed
         */
        VANILLA
    }

    public enum Safety {

        /**
         * Considers an action unsafe if it does more damage than the multiplier
         */
        BALANCE,

        /**
         * Considers an action unsafe if it does more damage to the player than an enemy
         */
        STABLE,

        /**
         * Actions are always considered safe
         */
        NONE
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

    public enum Interact {

        /**
         * Places on the closest face, regardless of visibility, Allows placements at world borders
         */
        VANILLA,

        /**
         * Places on the closest visible face
         */
        STRICT,

        /**
         * Places on the top block face, no facing directions
         */
        NONE
    }

    public enum Heuristic {

        /**
         * Heuristic: Best position is the one that deals the most damage
         */
        MAX,

        /**
         * Heuristic: Best position is the one that maximizes damage to the target and minimizes damage to the player
         */
        MINIMAX,
    }

    public enum Merge {

        /**
         * Syncs crystals when they are attacked
         */
        FAST,

        /**
         * Syncs crystals based on explosion packets
         */
        CONFIRM,

        /**
         * Does not sync crystal explosions
         */
        NONE
    }

    public enum Raytrace {

        /**
         * Raytrace to the center of the expected crystal position
         */
        EXPECTED(1.5),

        /**
         * Raytrace to the highest position of the expected crystal, wall ranges will be more accurate
         */
        LENIENT(2.5),

        /**
         * No raytrace to the position
         */
        NONE(-1000);

        // offset
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

    // TODO: implement without reducing FPS significantly; angle resolver
    @SuppressWarnings("unused")
    public enum Trace {

        /**
         * Traces to all points
         */
        FULL,

        /**
         * Traces to center
         */
        LAZY
    }

    public enum Text {

        /**
         * Render the damage done to the target
         */
        TARGET,

        /**
         * Render the damage done to the player
         */
        LOCAL,

        /**
         * Render the damage done to the target and the damage done to the player
         */
        BOTH,

        /**
         * No damage render
         */
        NONE
    }

    /**
     * Violation System:
     *
     * Violations added to failed processes, certain violations have more weightage than others
     * After certain number of violations, the process skips
     *
     * Violation Tags -> crystal.no_explode, place.no_spawn
     */
    public static class Violation<T> {

        // violator
        private final T violator;

        // tag
        private final ViolationTag violationTag;

        public Violation(T violator, ViolationTag violationTag) {
            this.violator = violator;
            this.violationTag = violationTag;
        }

        /**
         * Gets the damage source that has caused this violation
         * @return The damage source that has caused this violation
         */
        public T getViolator() {
            return violator;
        }

        /**
         * Gets the violation tag associated with this violation
         * @return The violation tag associated with this violation
         */
        public ViolationTag getViolationTag() {
            return violationTag;
        }

        public enum ViolationTag {

            /**
             * We have attempted to place here, but the crystal never spawned
             */
            PLACE_NO_SPAWN("place.no_spawn"),

            /**
             * We have attacked a crystal, but the crystal never exploded
             */
            ATTACK_NO_EXPLODE("crystal.no_explode");

            // identity tag
            private final String identifier;

            ViolationTag(String identifier) {
                this.identifier = identifier;
            }

            /**
             * Gets the identifier used to identify this violation tag
             * @return The identifier used to identify this violation tag
             */
            @Override
            public String toString() {
                return identifier;
            }
        }
    }

    public static class DamageHolder<T> {

        // damager
        private final T damageSource;

        private final Entity target;

        // damage info
        private final double targetDamage, localDamage;

        public DamageHolder(T damageSource, Entity target, double targetDamage, double localDamage) {
            this.damageSource = damageSource;

            // target
            this.target = target;

            // damage
            this.targetDamage = targetDamage;
            this.localDamage = localDamage;
        }

        /**
         * Gets the damage source
         * @return The damage source
         */
        public T getDamageSource() {
            return damageSource;
        }

        /**
         * Gets the target
         * @return The target
         */
        public Entity getTarget() {
            return target;
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