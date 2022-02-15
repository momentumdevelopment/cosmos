package cope.cosmos.client.features.modules.combat;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.HoleManager.Hole;
import cope.cosmos.client.manager.managers.HoleManager.Type;
import cope.cosmos.client.manager.managers.InventoryManager.Switch;
import cope.cosmos.client.manager.managers.SocialManager.Relationship;
import cope.cosmos.util.player.InventoryUtil;
import cope.cosmos.util.holder.Rotation.Rotate;
import cope.cosmos.util.render.RenderBuilder.Box;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;

import java.util.Set;

/**
 * @author linustouchtips
 * @since 06/08/2021
 */
@SuppressWarnings("unused")
public class HoleFill extends Module {
    public static HoleFill INSTANCE;

    public HoleFill() {
        super("HoleFill", Category.COMBAT, "Fills in nearby holes");
        INSTANCE = this;
    }

    public static Setting<Filler> mode = new Setting<>("Mode", Filler.TARGETED).setDescription("Mode for the filler");
    public static Setting<BlockMode> block = new Setting<>("Block", BlockMode.OBSIDIAN).setDescription("Block to use for filling");
    public static Setting<Completion> completion = new Setting<>("Completion", Completion.COMPLETION).setDescription("When to consider the filling complete");
    public static Setting<Double> range = new Setting<>("Range", 0.0, 5.0, 15.0, 1).setDescription("Range to scan for holes");
    public static Setting<Switch> autoSwitch = new Setting<>("Switch", Switch.NORMAL).setDescription("Mode for switching to block");
    public static Setting<Double> blocks = new Setting<>("Blocks", 0.0, 4.0, 10.0, 0).setDescription("Allowed block placements per tick");

    public static Setting<Boolean> strict = new Setting<>("Strict", false).setDescription("Only places on visible sides");
    public static Setting<Boolean> safety = new Setting<>("Safety", false).setDescription("Makes sure you are not the closest player for the current hole fill");
    public static Setting<Boolean> doubles = new Setting<>("Doubles", true).setDescription("Fills in double holes");

    public static Setting<Rotate> rotate = new Setting<>("Rotation", Rotate.NONE).setDescription("Mode for placement rotations");

    public static Setting<Target> target = new Setting<>("Target", Target.CLOSEST).setDescription("Priority for searching target");
    public static Setting<Double> targetRange = new Setting<>("Range", 0.0, 10.0, 15.0, 0).setDescription("Range to consider a player a target").setParent(target);
    public static Setting<Double> targetThreshold = new Setting<>("Threshold", 0.0, 3.0, 15.0, 1).setDescription("Target's distance from hole for it to be considered fill-able").setParent(target).setVisible(() -> mode.getValue().equals(Filler.TARGETED));

    public static Setting<Boolean> render = new Setting<>("Render", true).setDescription("Render a visual of the filling process");
    public static Setting<Box> renderMode = new Setting<>("Mode", Box.FILL).setDescription("Style of the visual").setParent(render);

    // fills
    private Set<Hole> fills = new ConcurrentSet<>();

    // block info
    private int blocksPlaced = 0;

    @Override
    public void onThread() {
        fills = searchFills();
    }

    @Override
    public void onUpdate() {
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

    public Set<Hole> searchFills() {
        // list of found fills
        Set<Hole> searchedFills = new ConcurrentSet<>();

        for (EntityPlayer player : mc.world.playerEntities) {

            // make sure the entity is valid to fill
            if (player == null || player == mc.player || player.isDead || getCosmos().getSocialManager().getSocial(player.getName()).equals(Relationship.FRIEND)) {
                continue;
            }

            // verify that the target is within fill distance
            double targetDistance = mc.player.getDistance(player);
            if (targetDistance > targetRange.getValue()) {
                continue;
            }

            for (Hole hole : getCosmos().getHoleManager().getHoles()) {

                // check if the hole is in range to be filled
                double holeDistance = Math.sqrt(mc.player.getDistanceSq(hole.getHole()));
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
                double holeTargetDistance = Math.sqrt(player.getDistanceSq(hole.getHole()));

                // targeted fill
                if (mode.getValue().equals(Filler.TARGETED)) {

                    // check if the target is near the hole
                    if (holeTargetDistance > targetThreshold.getValue()) {
                        continue;
                    }
                }

                // check safety
                if (holeDistance < holeTargetDistance && safety.getValue()) {
                    continue;
                }

                searchedFills.add(hole);
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
        PRESSURE_PLATE(Blocks.WOODEN_PRESSURE_PLATE),

        /**
         * Fills in holes with webs to prevent people from getting into them
         */
        WEB(Blocks.WEB);

        private final Block block;

        BlockMode(Block block) {
            this.block = block;
        }

        /**
         * Gets the associated block
         * @return The associated block
         */
        public Block getBlock() {
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