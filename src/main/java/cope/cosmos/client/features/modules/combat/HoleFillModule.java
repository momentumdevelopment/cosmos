package cope.cosmos.client.features.modules.combat;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.HoleManager.Hole;
import cope.cosmos.client.manager.managers.HoleManager.Type;
import cope.cosmos.client.manager.managers.InventoryManager.Switch;
import cope.cosmos.client.manager.managers.SocialManager.Relationship;
import cope.cosmos.util.combat.EnemyUtil;
import cope.cosmos.util.holder.Rotation.Rotate;
import cope.cosmos.util.player.InventoryUtil;
import cope.cosmos.util.render.RenderBuilder;
import cope.cosmos.util.render.RenderBuilder.Box;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.string.ColorUtil;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.Set;

/**
 * @author linustouchtips
 * @since 06/08/2021
 */
public class HoleFillModule extends Module {
    public static HoleFillModule INSTANCE;

    public HoleFillModule() {
        super("HoleFill", new String[] {"HoleFiller"}, Category.COMBAT, "Fills in nearby holes");
        INSTANCE = this;
    }

    // **************************** anticheat ****************************

    public static Setting<Boolean> strict = new Setting<>("Strict", false)
            .setDescription("Only places on visible sides");

    public static Setting<Rotate> rotate = new Setting<>("Rotation", Rotate.NONE)
            .setAlias("Rotate")
            .setDescription("Mode for placement rotations");

    // **************************** general ****************************

    public static Setting<Filler> mode = new Setting<>("Mode", Filler.ALL)
            .setAlias("Fill")
            .setDescription("Mode for the filler");

    public static Setting<BlockMode> block = new Setting<>("Block", BlockMode.OBSIDIAN)
            .setAlias("Item")
            .setDescription("Block to use for filling");

    public static Setting<Completion> completion = new Setting<>("Completion", Completion.COMPLETION)
            .setDescription("When to consider the filling complete");

    public static Setting<Double> range = new Setting<>("Range", 0.0, 5.0, 6.0, 1)
            .setDescription("Range to scan for holes");

    public static Setting<Switch> autoSwitch = new Setting<>("Switch", Switch.NORMAL)
            .setAlias("AutoSwitch", "Swap", "AutoSwap")
            .setDescription("Mode for switching to block");

    // WTF DOES THIS EVEN DO? maybe just add delay setting
    public static Setting<Double> blocks = new Setting<>("Blocks", 0.0, 4.0, 10.0, 0)
            .setAlias("BlocksPerTick", "BPT")
            .setDescription("Allowed block placements per tick");

    public static Setting<Boolean> safety = new Setting<>("Safety", false)
            .setDescription("Makes sure you are not the closest player for the current hole fill")
            .setVisible(() -> mode.getValue().equals(Filler.TARGETED));

    public static Setting<Boolean> doubles = new Setting<>("Doubles", true)
            .setAlias("Double")
            .setDescription("Fills in double holes");

    // **************************** targeting ****************************

    public static Setting<Target> target = new Setting<>("Target", Target.CLOSEST)
            .setDescription("Priority for searching target")
            .setVisible(() -> mode.getValue().equals(Filler.TARGETED));

    public static Setting<Double> targetRange = new Setting<>("TargetRange", 0.0, 10.0, 15.0, 0)
            .setAlias("EnemyRange")
            .setDescription("Range to consider a player a target")
            .setVisible(() -> mode.getValue().equals(Filler.TARGETED));

    public static Setting<Double> targetThreshold = new Setting<>("Threshold", 0.0, 3.0, 6.0, 1)
            .setAlias("TargetThreshold", "EnemyThreshold", "Distance")
            .setDescription("Target's distance from hole for it to be considered fill-able")
            .setVisible(() -> mode.getValue().equals(Filler.TARGETED));

    // **************************** render ****************************

    public static Setting<Boolean> render = new Setting<>("Render", true)
            .setDescription("Render a visual of the filling process");

    // fills
    private Set<Hole> fills = new ConcurrentSet<>();

    // block info
    private int blocksPlaced;

    @Override
    public void onThread() {
        fills = searchFills();
    }

    @Override
    public void onUpdate() {

        // we haven't placed any blocks on this tick
        blocksPlaced = 0;

        // if we already filled in all the holes, then we can disable
        if (fills.isEmpty() && completion.getValue().equals(Completion.COMPLETION)) {
            disable(true);
            return;
        }

        // save the previous slot
        int previousSlot = mc.player.inventory.currentItem;

        // switch to obsidian
        getCosmos().getInventoryManager().switchToBlock(block.getValue().getBlock(), autoSwitch.getValue());

        // fill in each of the holes
        if (InventoryUtil.isHolding(block.getValue().getBlock())) {
            for (Hole hole : fills) {

                // make sure we haven't placed too many blocks this tick
                if (blocksPlaced <= blocks.getValue()) {
                    blocksPlaced++;

                    // place block
                    getCosmos().getInteractionManager().placeBlock(hole.getHole(), rotate.getValue(), strict.getValue());
                }
            }
        }

        // switch back to our previous item
        if (previousSlot != -1) {
            getCosmos().getInventoryManager().switchToSlot(previousSlot, autoSwitch.getValue());
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // reset process
        fills.clear();
        blocksPlaced = 0;
    }

    @Override
    public boolean isActive() {
        return isEnabled() && !fills.isEmpty();
    }

    @Override
    public void onRender3D() {

        // draw all fills
        if (render.getValue()) {
            if (!fills.isEmpty()) {
                fills.forEach(fill -> {

                    // draw box
                    RenderUtil.drawBox(new RenderBuilder()
                            .position(fill.getHole())
                            .color(ColorUtil.getPrimaryAlphaColor(60))
                            .box(Box.FILL)
                            .setup()
                            .line(1.5F)
                            .depth(true)
                            .blend()
                            .texture()
                    );

                    // draw tag
                    RenderUtil.drawNametag(
                            fill.getHole(),
                            0.5F,
                            "Fill"
                    );
                });
            }
        }
    }

    /**
     * Searches for valid hole fills
     * @return Set containing all valid hole fills for the current tick
     */
    public Set<Hole> searchFills() {

        // list of found fills
        Set<Hole> searchedFills = new ConcurrentSet<>();

        if (mode.getValue().equals(Filler.TARGETED)) {

            for (EntityPlayer player : mc.world.playerEntities) {

                // make sure the entity is valid to fill
                if (player == null || player.equals(mc.player) || player.getEntityId() < 0 || EnemyUtil.isDead(player) || getCosmos().getSocialManager().getSocial(player.getName()).equals(Relationship.FRIEND)) {
                    continue;
                }

                // don't fill if the target is already in a hole
                if (getCosmos().getHoleManager().isHole(player.getPosition())) {
                    continue;
                }

                // verify that the target is within fill distance
                double targetDistance = mc.player.getDistance(player);
                if (targetDistance > targetRange.getValue()) {
                    continue;
                }

                for (Hole hole : getCosmos().getHoleManager().getHoles()) {

                    // position for the hole
                    BlockPos holePosition = hole.getHole();

                    // check to see if any entities are in the hole
                    if (mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(holePosition)).isEmpty()) {

                        // check if the hole is in range to be filled
                        double holeDistance = mc.player.getDistance(holePosition.getX(), holePosition.getY(), holePosition.getZ());
                        if (holeDistance > range.getValue()) {
                            continue;
                        }

                        // not worthwhile filling quad holes, just creates more holes
                        if (hole.isQuad() || hole.getType().equals(Type.VOID)) {
                            continue;
                        }

                        // check if the hole is a double hole
                        if (hole.isDouble() && !doubles.getValue()) {
                            continue;
                        }

                        // target's distance from the hole
                        double holeTargetDistance = player.getDistance(holePosition.getX(), holePosition.getY(), holePosition.getZ());

                        // targeted fill, check if the target is near the hole
                        if (holeTargetDistance > targetThreshold.getValue()) {
                            continue;
                        }

                        // check safety
                        if (holeDistance < holeTargetDistance && safety.getValue()) {
                            continue;
                        }

                        searchedFills.add(hole);
                    }
                }
            }
        }

        else if (mode.getValue().equals(Filler.ALL)) {

            for (Hole hole : getCosmos().getHoleManager().getHoles()) {

                // position for the hole
                BlockPos holePosition = hole.getHole();

                // check to see if any entities are in the hole
                if (mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(holePosition)).isEmpty()) {

                    // check if the hole is in range to be filled
                    double holeDistance = mc.player.getDistance(holePosition.getX(), holePosition.getY(), holePosition.getZ());
                    if (holeDistance > range.getValue()) {
                        continue;
                    }

                    // not worthwhile filling quad holes, just creates more holes
                    if (hole.isQuad() || hole.getType().equals(Type.VOID)) {
                        continue;
                    }

                    // check if the hole is a double hole
                    if (hole.isDouble() && !doubles.getValue()) {
                        continue;
                    }

                    searchedFills.add(hole);
                }
            }
        }

        return searchedFills;
    }

    public enum Filler {

        /**
         * Fills in all holes in range
         */
        ALL,

        /**
         * Fills in holes that are near players
         */
        TARGETED
    }

    public enum Completion {

        /**
         * Disables when there are no more holes to fill
         */
        COMPLETION,

        /**
         * Disables when there are no targets
         */
        TARGET,

        /**
         * Doesn't dynamically disable
         */
        PERSISTENT
    }

    private enum BlockMode {

        /**
         * Fills in holes with obsidian, standard
         */
        OBSIDIAN(Blocks.OBSIDIAN),

        /**
         * Fills in holes with ender chests, breaks some client's surrounds
         */
        ENDER_CHEST(Blocks.ENDER_CHEST),

        /**
         * Fills in holes with pressure plates to prevent them from showing up on HoleESP
         */
        PRESSURE_PLATE(Blocks.WOODEN_PRESSURE_PLATE, Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, Blocks.STONE_PRESSURE_PLATE, Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE),

        /**
         * Fills in holes with webs to prevent people from getting into them
         */
        WEB(Blocks.WEB);

        private final Block[] block;

        BlockMode(Block... block) {
            this.block = block;
        }

        /**
         * Gets the associated block
         * @return The associated block
         */
        public Block[] getBlock() {
            return block;
        }
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