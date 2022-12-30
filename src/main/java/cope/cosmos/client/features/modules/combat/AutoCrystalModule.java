package cope.cosmos.client.features.modules.combat;

import com.mojang.realmsclient.util.Pair;
import cope.cosmos.asm.mixins.accessor.ICPacketUseEntity;
import cope.cosmos.asm.mixins.accessor.IEntityLivingBase;
import cope.cosmos.asm.mixins.accessor.IEntityPlayerSP;
import cope.cosmos.asm.mixins.accessor.IPlayerControllerMP;
import cope.cosmos.client.events.entity.EntityWorldEvent;
import cope.cosmos.client.events.entity.player.RotationUpdateEvent;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.events.render.entity.RenderCrystalEvent;
import cope.cosmos.client.events.render.entity.RenderRotationsEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.ServiceModule;
import cope.cosmos.client.features.modules.exploits.SwingModule;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.InventoryManager.InventoryRegion;
import cope.cosmos.client.manager.managers.InventoryManager.Switch;
import cope.cosmos.client.manager.managers.SocialManager.Relationship;
import cope.cosmos.util.combat.DamageUtil;
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
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemEndCrystal;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.client.CPacketUseEntity.Action;
import net.minecraft.network.play.server.*;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linustouchtips
 * @since 05/08/2022
 */
public class AutoCrystalModule extends ServiceModule<EntityEnderCrystal> {
    public static AutoCrystalModule INSTANCE;

    public AutoCrystalModule() {
        super("AutoCrystal", new String[] {"CrystalAura", "CA"}, Category.COMBAT, "Places and explodes crystals", () -> debug.getValue() ? getDebug() : "");
        INSTANCE = this;
    }

    // **************************** anticheat settings ****************************

    public static Setting<Boolean> multitask = new Setting<>("Multitask", true)
            .setAlias("PauseEating", "PauseEat")
            .setDescription("Explodes only if we are not preforming any actions with our hands");

    public static Setting<Boolean> whileMining = new Setting<>("WhileMining", true)
            .setAlias("PauseMining", "PauseMine")
            .setDescription("Explodes only if we are not mining");

    public static Setting<Boolean> swing = new Setting<>("Swing", true)
            .setDescription("Swings the players hand when attacking and placing");

    public static Setting<Interact> interact = new Setting<>("Interact", Interact.VANILLA)
            .setAlias("StrictDirection")
            .setDescription("Interaction with blocks and crystals");

    public static Setting<Rotate> rotate = new Setting<>("Rotation", Rotate.NONE)
            .setAlias("Rotate")
            .setDescription("Rotate to the current process");

    public static Setting<YawStep> yawStep = new Setting<>("YawStep", YawStep.NONE)
            .setDescription("Limits yaw rotations")
            .setVisible(() -> !rotate.getValue().equals(Rotate.NONE));

    public static Setting<Double> yawStepThreshold = new Setting<>("YawStepThreshold", 1.0, 180.0, 180.0, 0)
            .setDescription("Max angle to rotate in one tick")
            .setVisible(() -> !rotate.getValue().equals(Rotate.NONE) && !yawStep.getValue().equals(YawStep.NONE));

    // **************************** general settings ****************************

    public static Setting<Boolean> raytrace = new Setting<>("Raytrace", false)
            .setAlias("Walls")
            .setDescription("Restricts placements through walls");

    public static Setting<Double> offset = new Setting<>("Offset", 1.0, 2.0, 2.0, 0)
            .setDescription("Crystal placement offset");

    // **************************** explode settings ****************************

    public static Setting<Boolean> explode = new Setting<>("Explode", true)
            .setAlias("Break", "Attack")
            .setDescription("Explodes crystals");

    public static Setting<Double> explodeSpeed = new Setting<>("ExplodeSpeed", 1.0, 20.0, 20.0, 1)
            .setAlias("BreakSpeed", "AttackSpeed")
            .setDescription("Speed to explode crystals")
            .setVisible(() -> explode.getValue());

    public static Setting<Double> attackDelay = new Setting<>("AttackDelay", 0.0, 0.0, 5.0, 1)
            .setAlias("BreakDelay", "ExplodeDelay")
            .setDescription("Speed to explode crystals using old delays")
            .setVisible(() -> explode.getValue());

    public static Setting<Double> explodeRange = new Setting<>("ExplodeRange", 1.0, 5.0, 6.0, 1)
            .setAlias("BreakRange", "AttackRange")
            .setDescription("Range to explode crystals")
            .setVisible(() -> explode.getValue());

    public static Setting<Double> explodeWallRange = new Setting<>("ExplodeWallRange", 1.0, 3.5, 6.0, 1)
            .setAlias("BreakWallRange", "AttackWallRange")
            .setDescription("Range to explode crystals through walls")
            .setVisible(() -> explode.getValue() && !raytrace.getValue());

    public static Setting<Boolean> rangeEye = new Setting<>("RangeEye", false)
            .setAlias("ExplodeRangeEye", "BreakRangeEye", "AttackRangeEye")
            .setDescription("Calculates ranges to the entity's eye")
            .setVisible(() -> explode.getValue());

    public static Setting<Double> ticksExisted = new Setting<>("TicksExisted", 0.0, 0.0, 5.0, 0)
            .setDescription("Minimum age of the crystal")
            .setVisible(() -> explode.getValue());

    public static Setting<Double> explodeSwitchDelay = new Setting<>("SwitchDelay", 0.0, 0.0, 10.0, 1)
            .setAlias("BreakSwitchDelay", "AttackSwitchDelay")
            .setDescription("Delay to pause after switching items")
            .setVisible(() -> explode.getValue());

    // public static Setting<Switch> antiWeakness = new Setting<>("AntiWeakness", Switch.NONE)
    //        .setDescription("Switches to a tool before breaking when player has weakness effect")
    //        .setVisible(() -> explode.getValue());

    public static Setting<Inhibit> inhibit = new Setting<>("Inhibit", Inhibit.SEMI)
            .setAlias("Limit")
            .setDescription("Prevents excessive attacks on crystals")
            .setVisible(() -> explode.getValue());

    public static Setting<Double> inhibitFactor = new Setting<>("inhibitFactor", 0.0, 1.0, 5.0, 1)
            .setAlias("LimitFactor")
            .setDescription("Time to wait after inhibiting")
            .setVisible(() -> explode.getValue() && inhibit.getValue().equals(Inhibit.FULL));

    public static Setting<Boolean> await = new Setting<>("Await", true)
            .setDescription("Runs delays on packet time")
            .setVisible(() -> explode.getValue());

    public static Setting<Double> yieldProtection = new Setting<>("YieldProtection", 0.0, 2.0, 5.0, 1)
            .setDescription("Inhibit factor")
            .setVisible(() -> explode.getValue() && await.getValue() && !inhibit.getValue().equals(Inhibit.NONE));

    // **************************** place settings ****************************

    public static Setting<Boolean> place = new Setting<>("Place", true)
            .setDescription("Places crystals");

    public static Setting<Placements> placements = new Setting<>("Placements", Placements.NATIVE)
            .setDescription("Placement calculations for current version")
            .setVisible(() -> place.getValue());

    @SuppressWarnings("unused")
    public static Setting<Sequential> sequential = new Setting<>("Sequential", Sequential.NORMAL)
            .setDescription("Timing for placements")
            .setVisible(() -> place.getValue());

    public static Setting<Double> placeSpeed = new Setting<>("PlaceSpeed", 1.0, 20.0, 20.0, 1)
            .setDescription("Speed to place crystals")
            .setVisible(() -> place.getValue());

    public static Setting<Double> placeRange = new Setting<>("PlaceRange", 1.0, 5.0, 6.0, 1)
            .setDescription("Range to place crystals")
            .setVisible(() -> place.getValue());

    public static Setting<Double> placeWallRange = new Setting<>("PlaceWallRange", 1.0, 3.5, 6.0, 1)
            .setDescription("Range to place crystals through walls")
            .setVisible(() -> place.getValue() && !raytrace.getValue());

    public static Setting<Switch> autoSwitch = new Setting<>("Switch", Switch.NONE)
            .setAlias("AutoSwitch", "Swap", "AutoSwap")
            .setDescription("Switching to crystals before placement")
            .setVisible(() -> place.getValue());

    public static Setting<Switch> antiWeakness = new Setting<>("AntiWeakness", Switch.NONE)
            .setDescription("Switches to a tool when attacking crystals to bypass the weakness effect")
            .setVisible(() -> explode.getValue());

    public static Setting<Boolean> alternativeSwitch = new Setting<>("AlternativeSwitch", false)
            .setAlias("AlternativeSwap")
            .setDescription("Alternative method for switching to crystals")
            .setVisible(() -> place.getValue() && autoSwitch.getValue().equals(Switch.PACKET) || explode.getValue() && antiWeakness.getValue().equals(Switch.PACKET));

    // **************************** damage settings ****************************

    public static Setting<Double> damage = new Setting<>("Damage", 2.0, 4.0, 10.0, 1)
            .setDescription("Minimum damage done by an action");

    public static Setting<Double> lethalMultiplier = new Setting<>("LethalMultiplier", 0.0, 1.0, 5.0, 1)
            .setAlias("FacePlaceMultiplier", "FacePlace", "FacePlaceHealth")
            .setDescription("Will override damages if we can kill the target in this many crystals");

    public static Setting<Boolean> armorBreaker = new Setting<>("ArmorBreaker", true)
            .setDescription("Attempts to break enemy armor with crystals");

    public static Setting<Double> armorScale = new Setting<>("ArmorScale", 0.0, 5.0, 40.0, 0)
            .setDescription("Will override damages if we can break the target's armor")
            .setVisible(() -> armorBreaker.getValue());

    public static Setting<Safety> safety = new Setting<>("Safety", Safety.NONE)
            .setDescription("Safety check for processes");

    public static Setting<Double> safetyBalance = new Setting<>("SafetyBalance", 0.1, 1.1, 3.0, 1)
            .setAlias("MaxLocalDamage")
            .setDescription("Multiplier for actions considered unsafe")
            .setVisible(() -> safety.getValue().equals(Safety.BALANCE));

    public static Setting<Boolean> blockDestruction = new Setting<>("BlockDestruction", false)
            .setAlias("IgnoreTerrain", "TerrainTrace")
            .setDescription("Ignores terrain that can be exploded when calculating damages");

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
            .setAlias("EnemyRange")
            .setDescription("Range to consider an entity as a target");

    // **************************** render settings ****************************

    public static Setting<Boolean> render = new Setting<>("Render", true)
            .setDescription("Renders the current process");

    public static Setting<Boolean> debug = new Setting<>("Debug", false)
            .setDescription("Development info")
            .setVisible(() -> false);

    public static Setting<Text> renderText = new Setting<>("RenderText", Text.NONE)
            .setAlias("RenderInfo")
            .setDescription("Renders the damage of the current process")
            .setVisible(() -> render.getValue());


    // **************************** rotations ****************************

    // vector that holds the angle we are looking at
    private Pair<Vec3d, YawStep> angleVector;

    // rotation angels
    private Rotation rotateAngles;

    // ticks to pause the process
    private int rotateTicks;

    // **************************** explode ****************************

    // explode timers
    private final Timer explodeTimer = new Timer();
    private final Timer switchTimer = new Timer();
    private boolean explodeClearance;

    // explosion
    private DamageHolder<EntityEnderCrystal> explosion;

    // map of all attacked crystals
    private final Map<Integer, Long> attackedCrystals = new ConcurrentHashMap<>();
    private final TreeMap<Long, Integer> spawnedCrystals = new TreeMap<>();

    // crystals that need to be ignored
    private final List<EntityEnderCrystal> inhibitCrystals = new ArrayList<>();
    private final List<Integer> deadCrystals = new ArrayList<>();

    // queue
    private final Set<EntityEnderCrystal> queuedCrystals = new HashSet<>();

    // **************************** place ****************************

    // place timers
    private final Timer placeTimer = new Timer();
    private boolean placeClearance;

    // switch timers
    private final Timer autoSwitchTimer = new Timer();

    // placement
    private DamageHolder<BlockPos> placement;

    // map of all placed crystals
    private final Map<BlockPos, Long> placedCrystals = new ConcurrentHashMap<>();

    // **************************** debug ****************************

    // desync timer
    private final Timer desyncTimer = new Timer();

    // attack flag
    private static long lastAttackTime;
    private static long lastConfirmTime;
    private static final long[] attackTimes = new long[10];

    // cps
    private long lastCrystalCount;
    private final Timer crystalTimer = new Timer();
    private static final long[] crystalCounts = new long[10];

    // **************************** packets ****************************

    // packets
    private final List<BlockPos> placementPackets = new ArrayList<>();
    private final List<Integer> explosionPackets = new ArrayList<>();

    @Override
    public void onThread() {

        // search ideal processes
        DamageHolder<EntityEnderCrystal> searchExplosion = getCrystal();
        DamageHolder<BlockPos> searchPlacement = getPlacement();

        // only search when enabled
        if (isEnabled() && searchExplosion != null) {

            // update
            explosion = searchExplosion;
        }

        // check queue
        else if (!queuedCrystals.isEmpty()) {

            // get first item in queue
            DamageHolder<EntityEnderCrystal> next = new DamageHolder<>(queuedCrystals.stream().findFirst().orElse(null), null, 0, 0);

            // set explosion & update queue
            if (next.getDamageSource() != null) {
                explosion = next;
            }
        }

        else {
            explosion = null;
        }

        // update placement
        if (isEnabled()) {
           placement = searchPlacement;
        }

        // check number of crystals in the last second
        if (crystalTimer.passedTime(1, Format.SECONDS)) {

            // make space for new val
            if (crystalCounts.length - 1 >= 0) {
                System.arraycopy(crystalCounts, 1, crystalCounts, 0, crystalCounts.length - 1);
            }

            // add to crystal counts
            crystalCounts[crystalCounts.length - 1] = lastCrystalCount;

            // reset
            lastCrystalCount = 0;
            crystalTimer.resetTime();
        }

        // we are cleared to process our calculations
        if (rotateTicks <= 0) {

            // needs the extra wait time
            if (inhibitFactor.getValue() > inhibitFactor.getMin() && inhibit.getValue().equals(Inhibit.FULL)) {

                // spawned crystal
                Map.Entry<Long, Integer> latestSpawn = spawnedCrystals.firstEntry();

                // attack latest spawn if waited
                if (latestSpawn != null) {

                    // calculate if we have passed delays (old delays)
                    // place delay based on place speeds
                    double explodeDelay = inhibitFactor.getValue() * 50;

                    // switch delay based on switch delays (NCP; some servers don't allow attacking right after you've switched your held item)
                    long switchDelay = explodeSwitchDelay.getValue().longValue() * 25L;

                    // we have waited the proper time ???
                    boolean delayed = System.currentTimeMillis() - latestSpawn.getKey() >= explodeDelay && switchTimer.passedTime(switchDelay, Format.MILLISECONDS);

                    // check if we have passed the explode time
                    if (explodeClearance || delayed) {

                        // face the crystal
                        angleVector = Pair.of(mc.world.getEntityByID(latestSpawn.getValue()).getPositionVector(), YawStep.FULL);

                        // attack crystal
                        if (attackCrystal(latestSpawn.getValue())) {

                            // add it to our list of attacked crystals
                            attackedCrystals.put(latestSpawn.getValue(), System.currentTimeMillis());

                            // clamp
                            if (lastAttackTime <= 0) {
                                lastAttackTime = System.currentTimeMillis();
                            }

                            // make space for new val
                            if (attackTimes.length - 1 >= 0) {
                                System.arraycopy(attackTimes, 1, attackTimes, 0, attackTimes.length - 1);
                            }

                            // add to attack times
                            attackTimes[attackTimes.length - 1] = System.currentTimeMillis() - lastAttackTime;

                            // mark attack flag
                            lastAttackTime = System.currentTimeMillis();

                            // clear
                            explodeClearance = false;
                            explodeTimer.resetTime();

                            // reset spawned crystals
                            spawnedCrystals.clear();
                        }
                    }
                }
            }

            // place on thread for faster response time
            else if (attackDelay.getValue() > attackDelay.getMin()) {

                // we found crystals to explode
                if (explosion != null) {

                    // calculate if we have passed delays (old delays)
                    // place delay based on place speeds
                    long explodeDelay = (long) (attackDelay.getValue() * 25);

                    // switch delay based on switch delays (NCP; some servers don't allow attacking right after you've switched your held item)
                    long switchDelay = explodeSwitchDelay.getValue().longValue() * 25L;

                    // we have waited the proper time ???
                    boolean delayed = explodeTimer.passedTime(explodeDelay, Format.MILLISECONDS) && switchTimer.passedTime(switchDelay, Format.MILLISECONDS);

                    // check if we have passed the explode time
                    if (explodeClearance || delayed) {

                        // face the crystal
                        angleVector = Pair.of(explosion.getDamageSource().getPositionVector(), YawStep.FULL);

                        // attack crystal
                        if (attackCrystal(explosion.getDamageSource())) {

                            // add it to our list of attacked crystals
                            attackedCrystals.put(explosion.getDamageSource().getEntityId(), System.currentTimeMillis());

                            // clamp
                            if (lastAttackTime <= 0) {
                                lastAttackTime = System.currentTimeMillis();
                            }

                            // make space for new val
                            if (attackTimes.length - 1 >= 0) {
                                System.arraycopy(attackTimes, 1, attackTimes, 0, attackTimes.length - 1);
                            }

                            // add to attack times
                            attackTimes[attackTimes.length - 1] = System.currentTimeMillis() - lastAttackTime;

                            // mark attack flag
                            lastAttackTime = System.currentTimeMillis();

                            // clear
                            explodeClearance = false;
                            explodeTimer.resetTime();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onUpdate() {

        // 2b2t
        if (interact.getValue().equals(Interact.STRICT) && inhibit.getValue().equals(Inhibit.FULL)) {

            // if we are desynced, attempt to resync
            if (isDesynced()) {

                // don't spam resync packets
                if (desyncTimer.passedTime(5, Format.SECONDS)) {

                    // resync?? TODO: find better resync method
                    // mc.playerController.windowClick(0, mc.player.inventory.currentItem + 36, 0, ClickType.PICKUP, mc.player);
                    // mc.playerController.windowClick(0, mc.player.inventory.currentItem + 36, 0, ClickType.PICKUP, mc.player);

                    // restart timer
                    desyncTimer.resetTime();

                    // notify
                    getCosmos().getChatManager().sendClientMessage("[AutoCrystal] Re-synced!");
                }
            }
        }

        // we are cleared to process our calculations
        if (rotateTicks <= 0) {

            // place on thread for more consistency
            if (attackDelay.getValue() <= attackDelay.getMin() || inhibitFactor.getValue() > inhibitFactor.getMin() && inhibit.getValue().equals(Inhibit.FULL)) {

                // we found crystals to explode
                if (explosion != null) {

                    // calculate if we have passed delays
                    // place delay based on place speeds
                    long explodeDelay = (long) ((explodeSpeed.getMax() - explodeSpeed.getValue()) * 50);

                    // prevent attacks faster than our ping would allow
                    if (await.getValue()) {
                        explodeDelay = (long) (getAverageWaitTime() + (50 * yieldProtection.getValue()));
                    }

                    // switch delay based on switch delays (NCP; some servers don't allow attacking right after you've switched your held item)
                    long switchDelay = explodeSwitchDelay.getValue().longValue() * 25L;

                    // we have waited the proper time ???
                    boolean delayed = explodeTimer.passedTime(explodeDelay, Format.MILLISECONDS) && switchTimer.passedTime(switchDelay, Format.MILLISECONDS);

                    // check if we have passed the explode time
                    if (explodeClearance || delayed) {

                        // check attack flag
                        // face the crystal
                        angleVector = Pair.of(explosion.getDamageSource().getPositionVector(), YawStep.FULL);

                        // attack crystal
                        if (attackCrystal(explosion.getDamageSource())) {

                            // add it to our list of attacked crystals
                            attackedCrystals.put(explosion.getDamageSource().getEntityId(), System.currentTimeMillis());

                            // clamp
                            if (lastAttackTime <= 0) {
                                lastAttackTime = System.currentTimeMillis();
                            }

                            // make space for new val
                            if (attackTimes.length - 1 >= 0) {
                                System.arraycopy(attackTimes, 1, attackTimes, 0, attackTimes.length - 1);
                            }

                            // add to attack times
                            attackTimes[attackTimes.length - 1] = System.currentTimeMillis() - lastAttackTime;

                            // mark attack flag
                            lastAttackTime = System.currentTimeMillis();

                            // clear
                            explodeClearance = false;
                            explodeTimer.resetTime();
                        }
                    }
                }
            }

            // we found a placement
            if (placement != null) {

                // calculate if we have passed delays
                // place delay based on place speeds
                long placeDelay = (long) ((placeSpeed.getMax() - placeSpeed.getValue()) * 50);

                // we have waited the proper time ???
                boolean delayed = placeSpeed.getValue() >= placeSpeed.getMax() || placeTimer.passedTime(placeDelay, Format.MILLISECONDS);

                // check if we have passed the place time
                if (placeClearance || delayed) {

                    // face the placement
                    angleVector = Pair.of(new Vec3d(placement.getDamageSource()).addVector(0.5, 0.5, 0.5), YawStep.NONE);

                    // place the crystal
                    if (placeCrystal(placement.getDamageSource())) {

                        // add it to our list of attacked crystals
                        placedCrystals.put(placement.getDamageSource(), System.currentTimeMillis());  // place on the client thread

                        // clear
                        placeClearance = false;
                        placeTimer.resetTime();
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

        // render our current placement
        if (render.getValue() && placement != null) {

            // only render if we are holding crystals
            if (isHoldingCrystal()) {

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
    public void onEnable() {
        super.onEnable();

        // cleared on enable
        explodeClearance = false;
        placeClearance = false;
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // clear lists and reset variables
        explosion = null;
        placement = null;
        angleVector = null;
        rotateAngles = null;
        rotateTicks = 0;
        // sequentialTicks = 0;
        // explodeTimer.resetTime();
        // placeTimer.resetTime();
        // attackedCrystals.clear();
        inhibitCrystals.clear();
        deadCrystals.clear();
        // placedCrystals.clear();
        spawnedCrystals.clear();
    }

    @Override
    public boolean isActive() {
        return isEnabled() && (explosion != null || placement != null) && isHoldingCrystal();
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
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {

        // check if there has been any external explosions (i.e. crystals not broken by the local player)
        boolean externalExplosion = false;

        // packet that confirms crystal removal
        if (event.getPacket() instanceof SPacketSoundEffect && ((SPacketSoundEffect) event.getPacket()).getCategory().equals(SoundCategory.BLOCKS) && ((SPacketSoundEffect) event.getPacket()).getSound().equals(SoundEvents.ENTITY_GENERIC_EXPLODE)) {

            // crystal entities within the packet position
            // List<EntityEnderCrystal> soundCrystals = mc.world.getEntitiesWithinAABB(EntityEnderCrystal.class, new AxisAlignedBB(new BlockPos(((SPacketSoundEffect) event.getPacket()).getX(), ((SPacketSoundEffect) event.getPacket()).getY(), ((SPacketSoundEffect) event.getPacket()).getZ())));

            // check all entities
            // for (EntityEnderCrystal crystal : soundCrystals) {

                // check if we have already counted this explosion
                // if (!inhibitCrystals.contains(crystal)) {
                //    externalExplosion = true;
                //
            // }

            // with this on the main thread there's no reason
            // why we need all the concurrency stuff...?
            mc.addScheduledTask(() -> {

                // attempt to clear crystals
                for (Entity crystal : new ArrayList<>(mc.world.loadedEntityList)) {

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
                    if (soundRange > 11) {
                        continue;
                    }

                    // don't attack these crystals they're going to be exploded anyways
                    inhibitCrystals.add((EntityEnderCrystal) crystal);

                    // the world sets the crystal dead one tick after this packet, but we can speed up the placements by setting it dead here
                    crystal.setDead();

                    // force remove entity
                    mc.world.removeEntity(crystal);

                    // ignore
                    if (sequential.getValue().equals(Sequential.STRICT)) {
                        deadCrystals.add(crystal.getEntityId());
                    }

                    else {
                        mc.world.removeEntityDangerously(crystal);
                    }
                }
            });
        }

        // packet for general explosions
        if (event.getPacket() instanceof SPacketExplosion) {

            // crystal entities within the packet position
            List<EntityEnderCrystal> explosionCrystals = mc.world.getEntitiesWithinAABB(EntityEnderCrystal.class, new AxisAlignedBB(new BlockPos(((SPacketExplosion) event.getPacket()).getX(), ((SPacketExplosion) event.getPacket()).getY(), ((SPacketExplosion) event.getPacket()).getZ())));

            // check all entities
            for (EntityEnderCrystal crystal : explosionCrystals) {

                // check if we have already counted this explosion
                if (!inhibitCrystals.contains(crystal)) {
                    externalExplosion = true;
                }
            }

            // with this on the main thread there's no reason
            // why we need all the concurrency stuff...?
            mc.addScheduledTask(() -> {

                // attempt to clear crystals
                for (Entity crystal : new ArrayList<>(mc.world.loadedEntityList)) {

                    // make sure the entity actually exists
                    if (crystal == null || crystal.isDead) {
                        continue;
                    }

                    // make sure it's a crystal
                    if (!(crystal instanceof EntityEnderCrystal)) {
                        continue;
                    }

                    // entity distance from sound
                    double soundRange = crystal.getDistance(((SPacketExplosion) event.getPacket()).getX() + 0.5, ((SPacketExplosion) event.getPacket()).getY() + 0.5, ((SPacketExplosion) event.getPacket()).getZ() + 0.5);

                    // make sure the crystal is in range from the sound to be destroyed
                    if (soundRange > ((SPacketExplosion) event.getPacket()).getStrength()) {
                        continue;
                    }

                    // don't attack these crystals they're going to be exploded anyways
                    inhibitCrystals.add((EntityEnderCrystal) crystal);

                    // the world sets the crystal dead one tick after this packet, but we can speed up the placements by setting it dead here
                    crystal.setDead();

                    // force remove entity
                    mc.world.removeEntity(crystal);

                    // ignore
                    if (sequential.getValue().equals(Sequential.STRICT)) {
                        deadCrystals.add(crystal.getEntityId());
                    }

                    else {
                        mc.world.removeEntityDangerously(crystal);
                    }
                }
            });
        }

        // packet for destroyed entities
        if (event.getPacket() instanceof SPacketDestroyEntities) {

            // check all entities being destroyed by the packet
            for (int entityId : ((SPacketDestroyEntities) event.getPacket()).getEntityIDs()) {

                // get entity from id
                Entity crystal = mc.world.getEntityByID(entityId);

                // make sure its a crystal
                if (crystal instanceof EntityEnderCrystal) {

                    // check if we have already counted this explosion
                    if (!inhibitCrystals.contains(crystal)) {
                        externalExplosion = true;
                    }

                    crystal.setDead();

                    // remove quicker to make the autocrystal look faster (as the world will remove these entities anyway
                    mc.addScheduledTask(() -> {
                        mc.world.removeEntity(crystal);
                        mc.world.removeEntityDangerously(crystal);
                    });
                }
            }
        }

        // if there's been an external explosion then we can place again
        if (externalExplosion) {

            // clear place
            if (sequential.getValue().equals(Sequential.NORMAL)) {

                // we found a placement
                if (placement != null) {

                    // face the placement
                    angleVector = Pair.of(new Vec3d(placement.getDamageSource()).addVector(0.5, 0.5, 0.5), YawStep.NONE);

                    // place the crystal
                    if (placeCrystal(placement.getDamageSource())) {

                        // add it to our list of attacked crystals
                        placedCrystals.put(placement.getDamageSource(), System.currentTimeMillis());
                    }
                }
            }
        }

        // packet for crystal spawns
        if (event.getPacket() instanceof SPacketSpawnObject && ((SPacketSpawnObject) event.getPacket()).getType() == 51) {

            // position of the spawned crystal
            BlockPos spawnPosition = new BlockPos(((SPacketSpawnObject) event.getPacket()).getX() - 0.5, ((SPacketSpawnObject) event.getPacket()).getY(), ((SPacketSpawnObject) event.getPacket()).getZ() - 0.5);

            // clear timer
            if (await.getValue()) {

                // face the crystal
                angleVector = Pair.of(new Vec3d(spawnPosition), YawStep.FULL);

                // check if we have placed this crystal
                if (placementPackets.contains(spawnPosition.down())) {

                    // prevents attacks faster than our ping will al;ow
                    if (inhibitFactor.getValue() > inhibitFactor.getMin() && inhibit.getValue().equals(Inhibit.FULL)) {

                        // add to map of spawned crystals
                        spawnedCrystals.put(System.currentTimeMillis(), ((SPacketSpawnObject) event.getPacket()).getEntityID());
                    }

                    // mark it as our current explosion
                    else if (attackDelay.getValue() > attackDelay.getMin()) {

                        // since it's been confirmed that the crystal spawned, we can move on to our next process
                        explodeClearance = true;
                    }

                    // clear the next explosion
                    else {

                        // attack spawned crystal
                        if (attackCrystal(((SPacketSpawnObject) event.getPacket()).getEntityID())) {

                            // add it to our list of attacked crystals
                            attackedCrystals.put(((SPacketSpawnObject) event.getPacket()).getEntityID(), System.currentTimeMillis());

                            // clamp
                            if (lastAttackTime <= 0) {
                                lastAttackTime = System.currentTimeMillis();
                            }

                            // make space for new val
                            if (attackTimes.length - 1 >= 0) {
                                System.arraycopy(attackTimes, 1, attackTimes, 0, attackTimes.length - 1);
                            }

                            // add to attack times
                            attackTimes[attackTimes.length - 1] = System.currentTimeMillis() - lastAttackTime;

                            // mark attack flag
                            lastAttackTime = System.currentTimeMillis();

                            explodeClearance = false;
                            explodeTimer.resetTime();
                        }
                    }

                    // reset
                    placementPackets.clear();

                    // accounted for
                    placedCrystals.remove(spawnPosition.down());
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderCrystal(RenderCrystalEvent.RenderCrystalPreEvent event) {

        // don't render "dead" crystals
        if (deadCrystals.contains(event.getEntity().getEntityId())) {
            // event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onEntityRemove(EntityWorldEvent.EntityRemoveEvent event) {

        // crystal being removed from world
        if (event.getEntity() instanceof EntityEnderCrystal) {

            // check if it is a crystal we have attacked
            if (attackedCrystals.containsKey(event.getEntity().getEntityId())) {

                // remove crystal from our attacked crystals list
                lastConfirmTime =  System.currentTimeMillis() - attackedCrystals.remove(event.getEntity().getEntityId());

                // recently broke a crystal
                lastCrystalCount++;
            }

            // check if it is a crystal we have sent a packet for
            if (explosionPackets.contains(event.getEntity().getEntityId())) {

                // clear
                explosionPackets.clear();
            }

            inhibitCrystals.remove((EntityEnderCrystal) event.getEntity());
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
                    rotateAngles = AngleUtil.calculateAngles(angleVector.first());

                    // yaw step requires slower rotations, so we ease into the target rotation, requires some silly math
                    if (!yawStep.getValue().equals(YawStep.NONE)) {

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

                            // adjust yaw, since this is not the true angle difference until we rotate again
                            float adjust = angleDifference > 0 ? -360 : 360;
                            angleDifference += adjust;
                        }

                        // use absolute angle diff
                        // rotating too fast
                        if (Math.abs(angleDifference) > yawStepThreshold.getValue()) {

                            // check if we need to yaw step
                            if (yawStep.getValue().equals(YawStep.FULL) || (yawStep.getValue().equals(YawStep.SEMI) && angleVector.second().equals(YawStep.FULL))) {

                                // ideal rotation direction, so we don't turn in the wrong direction
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
                        }

                        else {

                            // update player rotations
                            if (rotate.getValue().equals(Rotate.CLIENT)) {
                                mc.player.rotationYaw = rotateAngles.getYaw();
                                mc.player.rotationYawHead = rotateAngles.getYaw();
                                mc.player.rotationPitch = rotateAngles.getPitch();
                            }

                            // add our rotation to our client rotations, AutoCrystal has priority over all other rotations
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

                        // add our rotation to our client rotations, AutoCrystal has priority over all other rotations
                        getCosmos().getRotationManager().setRotation(rotateAngles);
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

    /**
     * Finds the best explode-able crystal for this tick
     * @return The best explode-able crystal
     */
    public DamageHolder<EntityEnderCrystal> getCrystal() {

        /*
         * Map of valid crystals
         * Sorted by natural ordering of keys
         * Using tree map allows time complexity of O(logN)
         */
        TreeMap<Double, DamageHolder<EntityEnderCrystal>> validCrystals = new TreeMap<>();

        // iterate all crystals in the world
        for (Entity crystal : new ArrayList<>(mc.world.loadedEntityList)) {

            // make sure the entity actually exists
            if (crystal == null || crystal.isDead) {
                continue;
            }

            // check if the entity is a crystal
            if (!(crystal instanceof EntityEnderCrystal)) {
                continue;
            }

            // time elapsed since we placed this crystal (if we did place it)
            long elapsedTime = System.currentTimeMillis() - placedCrystals.getOrDefault(new BlockPos(crystal.getPositionVector()).down(), System.currentTimeMillis());

            // make sure the crystal has existed in the world for a certain number of ticks before it's a viable target
            if ((crystal.ticksExisted < ticksExisted.getValue() && (elapsedTime / 50F) < ticksExisted.getValue()) && !inhibit.getValue().equals(Inhibit.NONE)) {
                continue;
            }

            // make sure the crystal isn't already being exploded, prevent unnecessary attacks
            if (inhibitCrystals.contains(crystal) && !inhibit.getValue().equals(Inhibit.NONE)) {
                continue;
            }

            // distance to crystal
            double crystalRange = mc.player.getDistance(crystal.posX, rangeEye.getValue() ? crystal.posY + crystal.getEyeHeight() : crystal.posY, crystal.posZ);

            // check if the entity is in range
            if (crystalRange > explodeRange.getValue()) {
                continue;
            }

            // check if crystal is behind a wall
            boolean isNotVisible = RaytraceUtil.isNotVisible(crystal, crystal.getEyeHeight());

            // check if entity can be attacked through wall
            if (isNotVisible) {
                if (crystalRange > explodeWallRange.getValue() || raytrace.getValue()) {
                    continue;
                }
            }

            // local damage done by the crystal
            double localDamage = ExplosionUtil.getDamageFromExplosion(mc.player, crystal.getPositionVector(), blockDestruction.getValue());

            // search all targets
            for (Entity entity : new ArrayList<>(mc.world.loadedEntityList)) {

                // make sure the entity actually exists
                if (entity == null || entity.equals(mc.player) || entity.getEntityId() < 0 || EnemyUtil.isDead(entity) || getCosmos().getSocialManager().getSocial(entity.getName()).equals(Relationship.FRIEND)) {
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
                double safetyIndex = 1;

                // check if we can take damage
                if (DamageUtil.canTakeDamage()) {

                    // local health
                    double health = PlayerUtil.getHealth();

                    // incredibly unsafe
                    if (localDamage + 0.5 > health) {
                        safetyIndex = -9999;
                    }

                    // unsafe -> if local damage is greater than target damage
                    else if (safety.getValue().equals(Safety.STABLE)) {

                        // target damage and local damage scaled
                        double efficiency = targetDamage - localDamage;

                        // too small, we'll be fine :>
                        if (efficiency < 0 && Math.abs(efficiency) < 0.25) {
                            efficiency = 0;
                        }

                        safetyIndex = efficiency;
                    }

                    // unsafe -> if local damage is greater than balanced target damage
                    else if (safety.getValue().equals(Safety.BALANCE)) {

                        // balanced target damage
                        double balance = targetDamage * safetyBalance.getValue();

                        // balanced damage, should be proportionate to local damage
                        safetyIndex = balance - localDamage;
                    }
                }

                // crystal is unsafe
                if (safetyIndex < 0) {
                    continue;
                }

                // add to map
                validCrystals.put(targetDamage, new DamageHolder<>((EntityEnderCrystal) crystal, entity, targetDamage, localDamage));
            }
        }

        // make sure we actually have some valid crystals
        if (!validCrystals.isEmpty()) {

            // best crystal in the map, in a TreeMap this is the last entry
            DamageHolder<EntityEnderCrystal> bestCrystal = validCrystals.lastEntry().getValue();

            // no crystal under 1.5 damage is worth exploding
            if (bestCrystal.getTargetDamage() > 1.5) {

                // lethality of the crystal
                boolean lethal = false;

                // target health
                double health = EnemyUtil.getHealth(bestCrystal.getTarget());

                // can kill the target very quickly
                if (health <= 2) {
                    lethal = true;
                }

                // attempt to break armor; considered lethal
                if (armorBreaker.getValue()) {
                    if (bestCrystal.getTarget() instanceof EntityPlayer) {

                        // check durability for each piece of armor
                        for (ItemStack armor : bestCrystal.getTarget().getArmorInventoryList()) {
                            if (armor != null && !armor.getItem().equals(Items.AIR)) {

                                // durability of the armor
                                float armorDurability = ((armor.getMaxDamage() - armor.getItemDamage()) / (float) armor.getMaxDamage()) * 100;

                                // find lowest durability
                                if (armorDurability < armorScale.getValue()) {
                                    lethal = true; // check if armor damage is significant
                                    break;
                                }
                            }
                        }
                    }
                }

                // lethality factor of the crystal
                double lethality = bestCrystal.getTargetDamage() * lethalMultiplier.getValue();

                // will kill the target
                if (health - lethality < 0.5) {
                    lethal = true;
                }

                // check if the damage meets our requirements
                if (lethal || bestCrystal.getTargetDamage() > damage.getValue()) {

                    // mark it as our current explosion
                    return bestCrystal;
                }
            }
        }

        // we were not able to find any explode-able crystals
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

            // check all positions in range
            for (BlockPos position : BlockUtil.getBlocksInArea(mc.player, new AxisAlignedBB(
                    -placeRange.getValue(), -placeRange.getValue(), -placeRange.getValue(), placeRange.getValue(), placeRange.getValue(), placeRange.getValue() // area in range of blocks
            ))) {

                // check if a crystal can be placed at this position
                if (!canPlaceCrystal(position)) {
                    continue;
                }

                // distance to placement
                double placementRange = BlockUtil.getDistanceToCenter(mc.player, position);

                // check if the placement is within range
                if (placementRange > placeRange.getValue() || placementRange > explodeRange.getValue()) {
                    continue;
                }

                // if the visibility for the expected crystal position is visible, then NCP won't flag us for placing at normal ranges
                boolean isNotVisible = RaytraceUtil.isNotVisible(position, 2.70000004768372);

                // check if placement can be placed on through a wall
                if (isNotVisible) {
                    if (placementRange > placeWallRange.getValue() || placementRange > explodeWallRange.getValue() || raytrace.getValue()) {
                        continue;
                    }
                }

                // local damage done by the placement
                double localDamage = ExplosionUtil.getDamageFromExplosion(mc.player, new Vec3d(position).addVector(0.5, 1, 0.5), blockDestruction.getValue());

                // search all targets
                for (Entity entity : new ArrayList<>(mc.world.loadedEntityList)) {

                    // make sure the entity actually exists
                    if (entity == null || entity.equals(mc.player) || entity.getEntityId() < 0 || EnemyUtil.isDead(entity) || getCosmos().getSocialManager().getSocial(entity.getName()).equals(Relationship.FRIEND)) {
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
                    double safetyIndex = 1;

                    // check if we can take damage
                    if (DamageUtil.canTakeDamage()) {

                        // local health
                        double health = PlayerUtil.getHealth();

                        // incredibly unsafe
                        if (localDamage + 0.5 > health) {
                            safetyIndex = -9999;
                        }

                        // unsafe -> if local damage is greater than target damage
                        else if (safety.getValue().equals(Safety.STABLE)) {

                            // target damage and local damage scaled
                            double efficiency = targetDamage - localDamage;

                            // too small, we'll be fine :>
                            if (efficiency < 0 && Math.abs(efficiency) < 0.25) {
                                efficiency = 0;
                            }

                            safetyIndex = efficiency;
                        }

                        // unsafe -> if local damage is greater than balanced target damage
                        else if (safety.getValue().equals(Safety.BALANCE)) {

                            // balanced target damage
                            double balance = targetDamage * safetyBalance.getValue();

                            // balanced damage, should be proportionate to local damage
                            safetyIndex = balance - localDamage;
                        }
                    }

                    // placement is unsafe
                    if (safetyIndex < 0) {
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

                    // lethality of the placement
                    boolean lethal = false;

                    // target health
                    double health = EnemyUtil.getHealth(bestPlacement.getTarget());

                    // can kill the target very quickly
                    if (health <= 2) {
                        lethal = true;
                    }

                    // attempt to break armor; considered lethal
                    if (armorBreaker.getValue()) {
                        if (bestPlacement.getTarget() instanceof EntityPlayer) {

                            // check durability for each piece of armor
                            for (ItemStack armor : bestPlacement.getTarget().getArmorInventoryList()) {
                                if (armor != null && !armor.getItem().equals(Items.AIR)) {

                                    // durability of the armor
                                    float armorDurability = ((armor.getMaxDamage() - armor.getItemDamage()) / (float) armor.getMaxDamage()) * 100;

                                    // find lowest durability
                                    if (armorDurability < armorScale.getValue()) {
                                        lethal = true;  // check if armor damage is significant
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    // lethality factor of the placement
                    double lethality = bestPlacement.getTargetDamage() * lethalMultiplier.getValue();

                    // will kill the target
                    if (health - lethality < 0.5) {
                        lethal = true;
                    }

                    // check if the damage meets our requirements
                    if (lethal || bestPlacement.getTargetDamage() > damage.getValue()) {

                        // mark it as our current placement
                        return bestPlacement;
                    }
                }
            }
        }

        // we were not able to find any placements
        return null;
    }

    /**
     * Attacks a given endcrystal
     * @param in The given endcrystal
     * @return Whether or not the attack was successful
     */
    public boolean attackCrystal(EntityEnderCrystal in) {
        return attackCrystal(in.getEntityId());
    }

    /**
     * Attacks a given endcrystal
     * @param in The entity id of the given endcrystal
     * @return Whether or not the attack was successful
     */
    @SuppressWarnings("all")
    public boolean attackCrystal(int in) {

        // strength and weakness effects on the player
        PotionEffect weaknessEffect = mc.player.getActivePotionEffect(MobEffects.WEAKNESS);
        PotionEffect strengthEffect = mc.player.getActivePotionEffect(MobEffects.STRENGTH);

        // check whether a crystal is in the offhand
        boolean offhand = mc.player.getHeldItemOffhand().getItem() instanceof ItemEndCrystal;

        // must be not doing anything
        if ((PlayerUtil.isEating() && !offhand) && !multitask.getValue()) {
            return false;
        }

        // must be not mining
        if ((PlayerUtil.isMining() && !offhand) && !whileMining.getValue()) {
            return false;
        }

        // mark previous slot
        int previousSlot = -1;

        // tool slots
        int swordSlot = getCosmos().getInventoryManager().searchSlot(ItemSword.class, InventoryRegion.INVENTORY);
        int pickSlot = getCosmos().getInventoryManager().searchSlot(ItemPickaxe.class, InventoryRegion.INVENTORY);

        // antiweakness switches to a tool slot to bypass the weakness effect
        if (!antiWeakness.getValue().equals(Switch.NONE)) {

            // verify that we cannot break the crystal due to weakness
            if (weaknessEffect != null && (strengthEffect == null || strengthEffect.getAmplifier() < weaknessEffect.getAmplifier())) {

                // check if we are holding a tool
                if (!InventoryUtil.isHolding(ItemSword.class) || !InventoryUtil.isHolding(ItemPickaxe.class)) {

                    // previous held item
                    previousSlot = mc.player.inventory.currentItem;

                    // alt switch
                    if (antiWeakness.getValue().equals(Switch.PACKET) && alternativeSwitch.getValue()) {

                        // prefer the sword over a pickaxe
                        if (swordSlot != -1) {

                            // swap
                            mc.playerController.windowClick(0, swordSlot + 36, mc.player.inventory.currentItem, ClickType.SWAP, mc.player);
                        }

                        else if (pickSlot != -1) {

                            // swap
                            mc.playerController.windowClick(0, pickSlot + 36, mc.player.inventory.currentItem, ClickType.SWAP, mc.player);
                        }
                    }

                    else {

                        // prefer the sword over a pickaxe
                        if (swordSlot != -1) {
                            getCosmos().getInventoryManager().switchToSlot(swordSlot, antiWeakness.getValue());
                        }

                        else if (pickSlot != -1) {
                            getCosmos().getInventoryManager().switchToSlot(pickSlot, antiWeakness.getValue());
                        }
                    }
                }
            }
        }

        // player sprint state
        boolean sprintState = false;

        // on strict anticheat configs, you need to stop sprinting before attacking (keeping consistent with vanilla behavior)
        if (interact.getValue().equals(Interact.STRICT)) {

            // update sprint state
            sprintState = mc.player.isSprinting() || ((IEntityPlayerSP) mc.player).getServerSprintState();

            // stop sprinting when attacking an entity
            if (sprintState) {
                // mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
            }
        }

        // packet for attacking the given endcrystal
        CPacketUseEntity attackPacket = new CPacketUseEntity();
        ((ICPacketUseEntity) attackPacket).setAction(Action.ATTACK);
        ((ICPacketUseEntity) attackPacket).setID(in);

        // send attack packet
        mc.player.connection.sendPacket(attackPacket);

        // count packets
        explosionPackets.add(in);

        // swing the player's arm
        if (swing.getValue()) {

            // held item stack
            ItemStack stack = mc.player.getHeldItem(offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);

            // check stack
            if (!stack.isEmpty()) {
                if (!stack.getItem().onEntitySwing(mc.player, stack)) {

                    // apply swing progress
                    if (!mc.player.isSwingInProgress || mc.player.swingProgressInt >= ((IEntityLivingBase) mc.player).hookGetArmSwingAnimationEnd() / 2 || mc.player.swingProgressInt < 0) {
                        mc.player.swingProgressInt = -1;
                        mc.player.isSwingInProgress = true;
                        mc.player.swingingHand = SwingModule.INSTANCE.isEnabled() ? SwingModule.INSTANCE.getHand() : (offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);

                        // send animation packet
                        if (mc.player.world instanceof WorldServer) {
                            ((WorldServer) mc.player.world).getEntityTracker().sendToTracking(mc.player, new SPacketAnimation(mc.player, offhand ? 3 : 0));
                        }
                    }
                }
            }
        }

        // swing with packets
        mc.player.connection.sendPacket(new CPacketAnimation(!offhand || weaknessEffect != null && (strengthEffect == null || strengthEffect.getAmplifier() < weaknessEffect.getAmplifier()) ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND));

        // reset sprint state
        if (sprintState) {
            // mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
        }

        // check if we have previous slot
        if (previousSlot != -1) {

            // switch back
            if (antiWeakness.getValue().equals(Switch.PACKET)) {

                // alt swap
                if (alternativeSwitch.getValue()) {

                    // prefer the sword over a pickaxe
                    if (swordSlot != -1) {

                        // swap
                        mc.playerController.windowClick(0, swordSlot + 36, mc.player.inventory.currentItem, ClickType.SWAP, mc.player);
                    }

                    else if (pickSlot != -1) {

                        // swap
                        mc.playerController.windowClick(0, pickSlot + 36, mc.player.inventory.currentItem, ClickType.SWAP, mc.player);
                    }

                    // sync
                    // mc.playerController.windowClick(0, mc.player.inventory.currentItem, 0, ClickType.PICKUP, mc.player);
                    // mc.playerController.windowClick(0, mc.player.inventory.currentItem, 0, ClickType.PICKUP, mc.player);
                }

                else {
                    getCosmos().getInventoryManager().switchToSlot(previousSlot, Switch.PACKET);
                }
            }
        }

        // ignore
        if (sequential.getValue().equals(Sequential.NORMAL)) {
            deadCrystals.add(in);
        }

        // clear queue
        queuedCrystals.clear();

        // attack was successful
        return true;
    }

    /**
     * Places a crystal at a given position
     * @param in The position to place the crystal on
     * @return Whether or not the placement was successful
     */
    public boolean placeCrystal(BlockPos in) {

        // make sure the position actually exits
        if (in == null) {
            return false;
        }

        // pause switch to account for actions
        if (PlayerUtil.isEating() || PlayerUtil.isMending() || PlayerUtil.isMining()) {
            autoSwitchTimer.resetTime();
        }

        // previous held item
        int previousSlot = -1;

        // slot of item (based on slot ids from : https://c4k3.github.io/wiki.vg/images/1/13/Inventory-slots.png)
        int swapSlot = getCosmos().getInventoryManager().searchSlot(Items.END_CRYSTAL, InventoryRegion.HOTBAR) + 36;

        // previous slot info
        Slot previousHeldSlot = mc.player.inventoryContainer.inventorySlots.get(mc.player.inventory.currentItem + 36);
        Slot previousSwapSlot = mc.player.inventoryContainer.inventorySlots.get(swapSlot);

        // previous item stack info
        ItemStack heldStack = mc.player.getHeldItemMainhand();
        ItemStack swapStack = previousSwapSlot.getStack();

        // switches to a crystal before attempting to place
        if (!autoSwitch.getValue().equals(Switch.NONE)) {

            // if we are not holding a crystal
            if (!InventoryUtil.isHolding(Items.END_CRYSTAL)) {

                // previous held item
                previousSlot = mc.player.inventory.currentItem;

                // alt switch
                if (autoSwitch.getValue().equals(Switch.PACKET) && alternativeSwitch.getValue()) {

                    // silent
                    if (InventoryUtil.isInHotbar(Items.END_CRYSTAL)) {

                        // alt swap
                        mc.playerController.windowClick(0, swapSlot, mc.player.inventory.currentItem, ClickType.SWAP, mc.player);

                        // force set
                        previousHeldSlot.putStack(swapStack);
                        previousSwapSlot.putStack(heldStack);

                        // ???
                        mc.player.inventory.markDirty();
                    }
                }

                // switch to a crystal
                else {

                    // normal switch requires pausing
                    if (autoSwitch.getValue().equals(Switch.NORMAL)) {

                        // wait for switch pause
                        if (autoSwitchTimer.passedTime(500, Format.MILLISECONDS)) {

                            // switch
                            getCosmos().getInventoryManager().switchToItem(Items.END_CRYSTAL, Switch.NORMAL);
                        }
                    }

                    else {
                        getCosmos().getInventoryManager().switchToItem(Items.END_CRYSTAL, Switch.NORMAL);
                    }
                }
            }
        }

        // sync item
        ((IPlayerControllerMP) mc.playerController).hookSyncCurrentPlayItem();

        // make sure we are holding a crystal before trying to place
        if (!isHoldingCrystal()) {
            return false;
        }

        // directions of placement
        double facingX = 0;
        double facingY = 0;
        double facingZ = 0;

        // assume the face is visible
        EnumFacing facingDirection = EnumFacing.UP;

        // the angles to the last interaction
        Rotation vectorAngles = AngleUtil.calculateAngles(angleVector.first());

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

                                // distance to face
                                double directionDistance = mc.player.getDistance(traceVector.x, traceVector.y, traceVector.z);

                                // if the face is the closest to the player and trace distance is reasonably close, then we have found a new ideal visible side to place against
                                if (directionDistance < closestDirection.first()) {

                                    // check visibility, raytrace to the current point
                                    RayTraceResult strictResult = mc.world.rayTraceBlocks(mc.player.getPositionEyes(1), traceVector, false, true, false);

                                    // if our raytrace is a block, check distances
                                    if (strictResult != null && strictResult.typeOfHit.equals(Type.BLOCK)) {
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

        // count packets
        placementPackets.add(in);

        // swing the player's arm
        if (swing.getValue()) {

            // held item stack
            ItemStack stack = mc.player.getHeldItem(offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);

            // check stack
            if (!stack.isEmpty()) {
                if (!stack.getItem().onEntitySwing(mc.player, stack)) {

                    // apply swing progress
                    if (!mc.player.isSwingInProgress || mc.player.swingProgressInt >= ((IEntityLivingBase) mc.player).hookGetArmSwingAnimationEnd() / 2 || mc.player.swingProgressInt < 0) {
                        mc.player.swingProgressInt = -1;
                        mc.player.isSwingInProgress = true;
                        mc.player.swingingHand = SwingModule.INSTANCE.isEnabled() ? SwingModule.INSTANCE.getHand() : (offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);

                        // send animation packet
                        if (mc.player.world instanceof WorldServer) {
                            ((WorldServer) mc.player.world).getEntityTracker().sendToTracking(mc.player, new SPacketAnimation(mc.player, offhand ? 3 : 0));
                        }
                    }
                }
            }
        }

        // swing with packets
        mc.player.connection.sendPacket(new CPacketAnimation(offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND));

        // update item stack info
        heldStack = previousSwapSlot.getStack();
        swapStack = mc.player.getHeldItemMainhand();

        // switch back after placing, should only switch serverside
        if (autoSwitch.getValue().equals(Switch.PACKET)) {

            // alt switch
            if (alternativeSwitch.getValue()) {

                // predicted stack
                // previousSwapStack.shrink(1);

                // alt swap
                mc.playerController.windowClick(0, swapSlot, mc.player.inventory.currentItem, ClickType.SWAP, mc.player);

                // force set
                previousHeldSlot.putStack(heldStack);
                previousSwapSlot.putStack(swapStack);

                // ???
                mc.player.inventory.markDirty();

                // sync
                // mc.playerController.windowClick(0, mc.player.inventory.currentItem, 0, ClickType.PICKUP, mc.player);
                // mc.playerController.windowClick(0, mc.player.inventory.currentItem, 0, ClickType.PICKUP, mc.player);

                // confirm packets
                // mc.player.connection.sendPacket(new CPacketConfirmTransaction(mc.player.inventoryContainer.windowId, nextTransactionID, true));
            }

            // switch back
            else {

                // check if our previous held item exists
                if (previousSlot != -1) {
                    getCosmos().getInventoryManager().switchToSlot(previousSlot, Switch.NORMAL);
                }
            }
        }

        // placement was successful
        return true;
    }

    /**
     * Checks if the player is facing a certain vector
     * @return Whether the player is facing a certain vector
     */
    public boolean isFacing(Vec3d in) {

        // yaw and pitch that we've sent to the server
        Rotation serverRotation = getCosmos().getRotationManager().getServerRotation();

        // target rotation
        Rotation facingRotation = AngleUtil.calculateAngles(in);

        // rotation diffs
        float yaw = Math.abs(serverRotation.getYaw() - facingRotation.getYaw());
        float pitch = Math.abs(serverRotation.getPitch() - facingRotation.getPitch());

        // both yaw and pitch must be nearly equal to facing rotation
        return yaw <= 0.1 & pitch <= 0.1;
    }

    /**
     * Checks if the AutoCrystal is desynced from the server
     * @return Whether the AutoCrystal is desynced from the server
     */
    public boolean isDesynced() {

        // cannot be desynced in singleplayer
        if (mc.isSingleplayer()) {
            return false;
        }

        // sent too many packets with no response (40 seems like a good number may change in the future)
        return explosionPackets.size() > 40 || placementPackets.size() > 40;
    }

    /**
     * Gets the dev info
     * @return The dev info
     */
    public static String getDebug() {

        // dev info
        // add average wait time
        String debugInfo = getAverageWaitTime() +
                "ms, " +
                getConfirmTime() +
                ", " +
                getAverageCrystalsPerSecond(); // add crystals per second

        return debug.getValue() ? debugInfo : "";
    }

    static String placeDebug = "none";

    /**
     * Gets the number of crystals in the last second
     * @return The number of crystals in the last second
     */
    private static int getAverageCrystalsPerSecond() {

        // average value
        return (int) getAverage(crystalCounts);
    }

    /**
     * Gets the time it took for the server to confirm the crystal
     * @return The time it took for the server to confirm the crystal
     */
    private static float getConfirmTime() {

        // clamp
        if (lastConfirmTime > 500) {
            lastConfirmTime = 0;
        }

        // average value
        return MathUtil.roundFloat(lastConfirmTime / 50F, 1);
    }

    /**
     * Gets the average attack wait time
     * @return the average attack wait time
     */
    private static long getAverageWaitTime() {

        // average value
        float average = getAverage(attackTimes);

        if (average > 500) {
            average = 0;
        }

        // 10 slots
        return (long) average;
    }

    /**
     * Gets the average value of an array
     * @param in The array
     * @return The average value of the array
     */
    private static float getAverage(long[] in) {

        // average value
        float avg = 0;

        for (long time : in) {
            avg += time;
        }

        // average time
        return avg / 10F;
    }

    /**
     * Checks whether or not the player is holding a crystal
     * @return Whether or not the player is holding a crystal
     */
    public boolean isHoldingCrystal() {
        return InventoryUtil.isHolding(Items.END_CRYSTAL) || autoSwitch.getValue().equals(Switch.PACKET) && InventoryUtil.isInHotbar(Items.END_CRYSTAL);
    }

    /**
     * Finds whether or not a crystal can be placed on a specified block
     * @param position The specified block to check if a crystal can be placed
     * @return Whether or not a crystal can be placed at the location
     */
    public boolean canPlaceCrystal(BlockPos position) {

        // block that we are placing on
        Block placeBlock = mc.world.getBlockState(position).getBlock();

        // crystals can only be placed on Obsidian and Bedrock
        if (!placeBlock.equals(Blocks.BEDROCK) && !placeBlock.equals(Blocks.OBSIDIAN)) {
            return false;
        }

        // the relative positions to check for air or fire, crystals can be placed on fire
        BlockPos nativePosition = position.up();
        BlockPos updatedPosition = nativePosition.up();

        // block that is above the one we are placing on
        Block nativeBlock = mc.world.getBlockState(nativePosition).getBlock();

        // check if the native position is air or fire
        if (!nativeBlock.equals(Blocks.AIR) && !nativeBlock.equals(Blocks.FIRE)) {
            return false;
        }

        // two block height needed for 1.12.2
        if (placements.getValue().equals(Placements.NATIVE)) {

            // block that is above the air block
            Block updatedBlock = mc.world.getBlockState(updatedPosition).getBlock();

            // check if the updated position is air or fire
            if (!updatedBlock.equals(Blocks.AIR) && !updatedBlock.equals(Blocks.FIRE)) {
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
            if (entity == null || entity.isDead || deadCrystals.contains(entity.getEntityId())) {
                placeDebug = "dead";
                continue;
            }

            // we can place on these entities
            if (entity instanceof EntityXPOrb) {
                continue;
            }

            // if the entity is crystal, check it's on the same position
            if (entity instanceof EntityEnderCrystal) {

                // we've attacked and haven't "failed" to break yet
                if (attackedCrystals.containsKey(entity.getEntityId()) && entity.ticksExisted < 20) {
                    placeDebug = "attacked";
                    continue;
                }

                // distance to crystal
                double crystalRange = mc.player.getDistance(entity.posX, rangeEye.getValue() ? entity.posY + entity.getEyeHeight() : entity.posY, entity.posZ);

                // check if the entity is in range
                if (crystalRange <= explodeRange.getValue()) {
                    continue;
                }

                // local damage done by the crystal
                double localDamage = ExplosionUtil.getDamageFromExplosion(mc.player, entity.getPositionVector(), blockDestruction.getValue());

                // best damage
                double idealDamage = 0;

                // search all targets
                for (Entity target : new ArrayList<>(mc.world.loadedEntityList)) {

                    // make sure the entity actually exists
                    if (target == null || target.equals(mc.player) || target.getEntityId() < 0 || EnemyUtil.isDead(target) || getCosmos().getSocialManager().getSocial(target.getName()).equals(Relationship.FRIEND)) {
                        continue;
                    }

                    // ignore crystals, they can't be targets
                    if (target instanceof EntityEnderCrystal) {
                        continue;
                    }

                    // don't attack our riding entity
                    if (target.isBeingRidden() && target.getPassengers().contains(mc.player)) {
                        continue;
                    }

                    // verify that the entity is a target
                    if (target instanceof EntityPlayer && !targetPlayers.getValue() || EntityUtil.isPassiveMob(target) && !targetPassives.getValue() || EntityUtil.isNeutralMob(target) && !targetNeutrals.getValue() || EntityUtil.isHostileMob(target) && !targetHostiles.getValue()) {
                        continue;
                    }

                    // distance to target
                    double entityRange = mc.player.getDistance(target);

                    // check if the target is in range
                    if (entityRange > targetRange.getValue()) {
                        continue;
                    }

                    // target damage done by the placement
                    double targetDamage = ExplosionUtil.getDamageFromExplosion(target, entity.getPositionVector(), blockDestruction.getValue());

                    // check the safety of the placement
                    double safetyIndex = 1;

                    // check if we can take damage
                    if (DamageUtil.canTakeDamage()) {

                        // local health
                        double health = PlayerUtil.getHealth();

                        // incredibly unsafe
                        if (localDamage + 0.5 > health) {
                            safetyIndex = -9999;
                        }

                        // unsafe -> if local damage is greater than target damage
                        else if (safety.getValue().equals(Safety.STABLE)) {

                            // target damage and local damage scaled
                            double efficiency = targetDamage - localDamage;

                            // too small, we'll be fine :>
                            if (efficiency < 0 && Math.abs(efficiency) < 0.25) {
                                efficiency = 0;
                            }

                            safetyIndex = efficiency;
                        }

                        // unsafe -> if local damage is greater than balanced target damage
                        else if (safety.getValue().equals(Safety.BALANCE)) {

                            // balanced target damage
                            double balance = targetDamage * safetyBalance.getValue();

                            // balanced damage, should be proportionate to local damage
                            safetyIndex = balance - localDamage;
                        }
                    }

                    // placement is unsafe
                    if (safetyIndex < 0) {
                        continue;
                    }

                    // update ideal damage
                    if (targetDamage > idealDamage) {
                        idealDamage = targetDamage;
                    }
                }

                // we will attack the crystal soon
                if (idealDamage > damage.getValue()) {
                    continue;
                }
            }

            unsafeEntities++;
        }

        // make sure there are not unsafe entities at the place position
        return unsafeEntities <= 0;
    }

    /**
     * Queues a crystal to be exploded
     */
    public void queue(EntityEnderCrystal in) {
        queuedCrystals.add(in);
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

    public enum YawStep {

        /**
         * Yaw steps when breaking and placing
         */
        FULL,

        /**
         * Yaw steps when breaking
         */
        SEMI,

        /**
         * Does not yaw step
         */
        NONE
    }

    public enum Inhibit {

        /**
         * Adds an additional delay for stricter servers
         */
        FULL,

        /**
         * Does not wait and attacks straight away
         */
        SEMI,

        /**
         * Does not inhibit
         */
        NONE
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

    public enum Sequential {

        /**
         * One tick, quick adjust (balances out the timing)
         */
        NORMAL(1),

        /**
         * Two ticks, slower adjust (balances out the timing)
         */
        STRICT(2),

        /**
         * No timing adjustment
         */
        NONE(1000);

        // ticks
        private final int ticks;

        Sequential(int ticks) {
            this.ticks = ticks;
        }

        /**
         * Gets the ticks
         * @return The ticks
         */
        public double getTicks() {
            return ticks;
        }
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