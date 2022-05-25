package cope.cosmos.client.features.modules.combat;

import com.mojang.realmsclient.util.Pair;
import cope.cosmos.asm.mixins.accessor.ICPacketUseEntity;
import cope.cosmos.asm.mixins.accessor.IEntityPlayerSP;
import cope.cosmos.client.events.entity.EntityWorldEvent;
import cope.cosmos.client.events.entity.player.RotationUpdateEvent;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.events.render.entity.RenderRotationsEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemEndCrystal;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.client.CPacketUseEntity.Action;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linustouchtips
 * @since 05/08/2022
 */
public class AutoCrystalModule extends Module {
    public static AutoCrystalModule INSTANCE;

    public AutoCrystalModule() {
        super("AutoCrystal", Category.COMBAT, "Places and explodes crystals");
        INSTANCE = this;
    }

    // **************************** anticheat settings ****************************

    public static Setting<Boolean> multiTask = new Setting<>("MultiTask", true)
            .setDescription("Explodes only if we are not preforming any actions with our hands");

    public static Setting<Boolean> whileMining = new Setting<>("WhileMining", true)
            .setDescription("Explodes only if we are not mining");

    public static Setting<Boolean> swing = new Setting<>("Swing", true)
            .setDescription("Swings the players hand when attacking and placing");

    public static Setting<Interact> interact = new Setting<>("Interact", Interact.VANILLA)
            .setDescription("Interaction with blocks and crystals");

    // TODO: fix rotation priority
    public static Setting<Rotate> rotate = new Setting<>("Rotate", Rotate.NONE)
            .setDescription("Rotate to the current process");

    // **************************** general settings ****************************

    public static Setting<Raytrace> raytrace = new Setting<>("Raytrace", Raytrace.LENIENT)
            .setDescription("Verifies placements through walls");

    public static Setting<Double> offset = new Setting<>("Offset", 1.0, 2.0, 2.0, 0)
            .setDescription("Crystal placement offset");

    // **************************** explode settings ****************************

    public static Setting<Boolean> explode = new Setting<>("Explode", true)
            .setDescription("Explodes crystals");

    public static Setting<Double> explodeSpeed = new Setting<>("ExplodeSpeed", 1.0, 20.0, 20.0, 1)
            .setDescription("Speed to explode crystals")
            .setVisible(() -> explode.getValue());

    public static Setting<Double> explodeRange = new Setting<>("ExplodeRange", 1.0, 5.0, 6.0, 1)
            .setDescription("Range to explode crystals")
            .setVisible(() -> explode.getValue());

    public static Setting<Double> explodeWallRange = new Setting<>("ExplodeWallRange", 1.0, 3.5, 6.0, 1)
            .setDescription("Range to explode crystals through walls")
            .setVisible(() -> explode.getValue());

    public static Setting<Double> ticksExisted = new Setting<>("TicksExisted", 0.0, 0.0, 5.0, 0)
            .setDescription("Minimum age of the crystal")
            .setVisible(() -> explode.getValue());

    public static Setting<Boolean> inhibit = new Setting<>("Inhibit", true)
            .setDescription("Prevents excessive attacks on crystals")
            .setVisible(() -> explode.getValue());

    public static Setting<Boolean> await = new Setting<>("Await", true)
            .setDescription("Runs delays on packet time")
            .setVisible(() -> explode.getValue());

    // **************************** place settings ****************************

    public static Setting<Boolean> place = new Setting<>("Place", true)
            .setDescription("Places crystals");

    public static Setting<Placements> placements = new Setting<>("Placements", Placements.NATIVE)
            .setDescription("Placement calculations for current version")
            .setVisible(() -> place.getValue());

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
            .setVisible(() -> place.getValue());

    // **************************** damage settings ****************************

    public static Setting<Double> damage = new Setting<>("Damage", 2.0, 4.0, 10.0, 1)
            .setDescription("Minimum damage done by an action");

    public static Setting<Double> lethalMultiplier = new Setting<>("LethalMultiplier", 0.0, 1.0, 5.0, 1)
            .setDescription("Will override damages if we can kill the target in this many crystals");

    public static Setting<Boolean> armorBreaker = new Setting<>("ArmorBreaker", true)
            .setDescription("Attempts to break enemy armor with crystals");

    public static Setting<Double> armorScale = new Setting<>("ArmorScale", 0.0, 5.0, 40.0, 0)
            .setDescription("Will override damages if we can break the target's armor")
            .setVisible(() -> armorBreaker.getValue());

    public static Setting<Safety> safety = new Setting<>("Safety", Safety.NONE)
            .setDescription("Safety check for processes");

    public static Setting<Double> safetyBalance = new Setting<>("SafetyBalance", 0.1, 1.1, 3.0, 1)
            .setDescription("Multiplier for actions considered unsafe")
            .setVisible(() -> safety.getValue().equals(Safety.BALANCE));

    public static Setting<Boolean> blockDestruction = new Setting<>("BlockDestruction", false)
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
            .setDescription("Range to consider an entity as a target");

    // **************************** render settings ****************************

    public static Setting<Boolean> render = new Setting<>("Render", true)
            .setDescription("Renders the current process");

    public static Setting<Text> renderText = new Setting<>("RenderText", Text.NONE)
            .setDescription("Renders the damage of the current process")
            .setVisible(() -> render.getValue());

    // **************************** rotations ****************************

    // vector that holds the angle we are looking at
    private Vec3d angleVector;

    // rotation angels
    private Rotation rotateAngles;

    // **************************** explode ****************************

    // explode timers
    private final Timer explodeTimer = new Timer();
    private boolean explodeClearance;

    // explosion
    private DamageHolder<EntityEnderCrystal> explosion;

    // map of all attacked crystals
    private final Map<Integer, Long> attackedCrystals = new ConcurrentHashMap<>();
    private final List<EntityEnderCrystal> inhibitCrystals = new ArrayList<>();

    // **************************** place ****************************

    // place timers
    private final Timer placeTimer = new Timer();
    private boolean placeClearance;

    // packet times
    private int sequentialTicks;

    // placement
    private DamageHolder<BlockPos> placement;

    // map of all placed crystals
    private final Map<BlockPos, Long> placedCrystals = new ConcurrentHashMap<>();

    @Override
    public void onThread() {

        // search ideal processes
        explosion = getCrystal();
        placement = getPlacement();

        // we found crystals to explode
        if (explosion != null) {

            // check if we have passed the explode time
            if (explodeClearance || explodeSpeed.getValue() >= explodeSpeed.getMax() || explodeTimer.passedTime((long) ((explodeSpeed.getMax() - explodeSpeed.getValue()) * 50), Format.MILLISECONDS)) {

                // face the crystal
                angleVector = explosion.getDamageSource().getPositionVector();

                // attack crystal
                if (attackCrystal(explosion.getDamageSource())) {

                    // add it to our list of attacked crystals
                    attackedCrystals.put(explosion.getDamageSource().getEntityId(), System.currentTimeMillis());
                }

                explodeClearance = false;
                explodeTimer.resetTime();
            }
        }

        // place on the client thread
        if (!sequential.getValue().equals(Sequential.NONE)) {

            // we found a placement
            if (placement != null) {

                // cleared to place
                if (sequentialTicks >= sequential.getValue().getTicks()) {

                    // face the placement
                    angleVector = new Vec3d(placement.getDamageSource()).addVector(0.5, 0.5, 0.5);

                    // place the crystal
                    if (placeCrystal(placement.getDamageSource())) {

                        // add it to our list of attacked crystals
                        placedCrystals.put(placement.getDamageSource(), System.currentTimeMillis());
                    }

                    // do not concurrently place on thread
                    sequentialTicks = 0;
                }
            }
        }
    }

    @Override
    public void onUpdate() {

        // update ticks
        sequentialTicks++;

        // we found a placement
        if (placement != null) {

            // check if we have passed the place time
            if (placeClearance || placeSpeed.getValue() >= placeSpeed.getMax() || placeTimer.passedTime((long) ((placeSpeed.getMax() - placeSpeed.getValue()) * 50), Format.MILLISECONDS)) {

                // face the placement
                angleVector = new Vec3d(placement.getDamageSource()).addVector(0.5, 0.5, 0.5);

                // place the crystal
                if (placeCrystal(placement.getDamageSource())) {

                    // add it to our list of attacked crystals
                    placedCrystals.put(placement.getDamageSource(), System.currentTimeMillis());
                }

                placeClearance = false;
                placeTimer.resetTime();
            }
        }
    }

    @Override
    public void onRender3D() {

        // render our current placement
        if (render.getValue() && placement != null) {

            // only render if we are holding crystals
            if (InventoryUtil.isHolding(Items.END_CRYSTAL)) {

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
        sequentialTicks = 0;
        explodeTimer.resetTime();
        placeTimer.resetTime();
        attackedCrystals.clear();
        inhibitCrystals.clear();
        placedCrystals.clear();
    }

    @Override
    public boolean isActive() {
        return isEnabled() && (explosion != null || placement != null);
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {

        // packet for crystal spawns
        if (event.getPacket() instanceof SPacketSpawnObject && ((SPacketSpawnObject) event.getPacket()).getType() == 51) {

            // position of the spawned crystal
            BlockPos spawnPosition = new BlockPos(((SPacketSpawnObject) event.getPacket()).getX(), ((SPacketSpawnObject) event.getPacket()).getY(), ((SPacketSpawnObject) event.getPacket()).getZ());

            // since it's been confirmed that the crystal spawned, we can move on to our next process
            if (placedCrystals.containsKey(spawnPosition.down())) {

                // clear timer
                if (await.getValue()) {
                    explodeClearance = true;
                }

                // no longer needs to be accounted for
                placedCrystals.remove(spawnPosition.down());
            }
        }

        // packet that confirms crystal removal
        if (event.getPacket() instanceof SPacketSoundEffect && ((SPacketSoundEffect) event.getPacket()).getSound().equals(SoundEvents.ENTITY_GENERIC_EXPLODE) && ((SPacketSoundEffect) event.getPacket()).getCategory().equals(SoundCategory.BLOCKS)) {

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
                if (soundRange > 6) {
                    continue;
                }

                // don't attack these crystals they're going to be exploded anyways
                inhibitCrystals.add((EntityEnderCrystal) crystal);

                // the world sets the crystal dead one tick after this packet, but we can speed up the placements by setting it dead here
                crystal.setDead();
            }
        }
    }

    @SubscribeEvent
    public void onEntityRemove(EntityWorldEvent.EntityRemoveEvent event) {

        // crystal being removed from world
        if (event.getEntity() instanceof EntityEnderCrystal) {

            // remove crystal from our attacked crystals list
            attackedCrystals.remove(event.getEntity().getEntityId());
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
                    rotateAngles = AngleUtil.calculateAngles(angleVector);

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

            // make sure the crystal has existed in the world for a certain number of ticks before it's a viable target
            if (crystal.ticksExisted < ticksExisted.getValue() && !inhibit.getValue()) {
                continue;
            }

            // make sure the crystal isn't already being exploded, prevent unnecessary attacks
            if (inhibitCrystals.contains(crystal) && inhibit.getValue()) {
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
            double localDamage = ExplosionUtil.getDamageFromExplosion(mc.player, crystal.getPositionVector(), blockDestruction.getValue());

            // search all targets
            for (Entity entity : new ArrayList<>(mc.world.loadedEntityList)) {

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

                // if the block above the one we can't see through is air, then NCP won't flag us for placing at normal ranges
                boolean isNotVisible = RaytraceUtil.isNotVisible(position, raytrace.getValue().getOffset());

                // check if placement can be placed on through a wall
                if (isNotVisible) {
                    if (placementRange > placeWallRange.getValue() || placementRange > explodeWallRange.getValue()) {
                        continue;
                    }
                }

                // local damage done by the placement
                double localDamage = ExplosionUtil.getDamageFromExplosion(mc.player, new Vec3d(position).addVector(0.5, 1, 0.5), blockDestruction.getValue());

                // search all targets
                for (Entity entity : new ArrayList<>(mc.world.loadedEntityList)) {

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

        // check whether a crystal is in the offhand
        boolean offhand = mc.player.getHeldItemOffhand().getItem() instanceof ItemEndCrystal;

        // must be not doing anything
        if ((PlayerUtil.isEating() && !offhand) && !multiTask.getValue()) {
            return false;
        }

        // must be not mining
        if ((PlayerUtil.isMining() && !offhand) && !whileMining.getValue()) {
            return false;
        }

        // player sprint state
        boolean sprintState = false;

        // on strict anticheat configs, you need to stop sprinting before attacking (keeping consistent with vanilla behavior)
        if (interact.getValue().equals(Interact.STRICT)) {

            // update sprint state
            sprintState = mc.player.isSprinting() || ((IEntityPlayerSP) mc.player).getServerSprintState();

            // stop sprinting when attacking an entity
            if (sprintState) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
            }
        }

        // packet for attacking the given endcrystal
        CPacketUseEntity attackPacket = new CPacketUseEntity();
        ((ICPacketUseEntity) attackPacket).setAction(Action.ATTACK);
        ((ICPacketUseEntity) attackPacket).setID(in);

        // send attack packet
        mc.player.connection.sendPacket(attackPacket);

        // swing the player's arm
        if (swing.getValue()) {
            mc.player.swingArm(offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
        }

        // swing with packets
        else {
            mc.player.connection.sendPacket(new CPacketAnimation(offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND));
        }

        // reset sprint state
        if (sprintState) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
        }

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

        // make sure we are holding a crystal
        if (!InventoryUtil.isHolding(Items.END_CRYSTAL)) {
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

        // placement was successful
        return true;
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