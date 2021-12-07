package cope.cosmos.client.features.modules.combat;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.client.ColorUtil;
import cope.cosmos.util.combat.EnemyUtil;
import cope.cosmos.util.combat.TargetUtil;
import cope.cosmos.util.combat.TargetUtil.Target;
import cope.cosmos.util.player.InventoryUtil;
import cope.cosmos.util.player.InventoryUtil.Switch;
import cope.cosmos.util.player.Rotation.Rotate;
import cope.cosmos.util.render.RenderBuilder;
import cope.cosmos.util.render.RenderBuilder.Box;
import cope.cosmos.util.render.RenderUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.TreeMap;

@SuppressWarnings("unused")
public class HoleFill extends Module {
    public static HoleFill INSTANCE;

    public HoleFill() {
        super("HoleFill", Category.COMBAT, "Fills in nearby holes");
        INSTANCE = this;
    }

    public static Setting<Filler> mode = new Setting<>("Mode", Filler.TARGETED).setDescription("Mode for the filler");
    public static Setting<Block> block = new Setting<>("Block", Block.OBSIDIAN).setDescription("Block to use for filling");
    public static Setting<Completion> completion = new Setting<>("Completion", Completion.COMPLETION).setDescription("When to consider the filling complete");
    public static Setting<Double> range = new Setting<>("Range", 0.0, 5.0, 15.0, 1).setDescription("Range to scan for holes");
    public static Setting<Double> threshold = new Setting<>("Threshold", 0.0, 3.0, 15.0, 1).setDescription("Target's distance from hole for it to be considered fill-able").setVisible(() -> mode.getValue().equals(Filler.TARGETED));
    public static Setting<Switch> autoSwitch = new Setting<>("Switch", Switch.NORMAL).setDescription("Mode for switching to block");

    public static Setting<Boolean> strict = new Setting<>("Strict", false).setDescription("Only places on visible sides");
    public static Setting<Boolean> safety = new Setting<>("Safety", false).setDescription("Makes sure you are not the closest player for the current hole fill");
    public static Setting<Boolean> doubles = new Setting<>("Doubles", true).setDescription("Fills in double holes");

    public static Setting<Rotate> rotate = new Setting<>("Rotation", Rotate.NONE).setDescription("Mode for placement rotations");

    public static Setting<Target> target = new Setting<>("Target", Target.CLOSEST).setDescription("Priority for searching target");
    public static Setting<Double> targetRange = new Setting<>("Range", 0.0, 10.0, 15.0, 0).setParent(target).setDescription("Range to consider a player a target");

    public static Setting<Boolean> render = new Setting<>("Render", true).setDescription("Render a visual of the filling process");
    public static Setting<Box> renderMode = new Setting<>("Mode", Box.FILL).setParent(render).setDescription("Style of the visual");

    private EntityPlayer fillTarget;
    private BlockPos fillPosition = null;

    private int previousSlot;

    @Override
    public void onThread() {
        fillPosition = searchFill();
    }

    public BlockPos searchFill() {
        fillTarget = (EntityPlayer) TargetUtil.getTargetEntity(targetRange.getValue(), Target.CLOSEST, true, false, false, false);

        if (fillTarget == null || EnemyUtil.isDead(fillTarget))
            return null;

        TreeMap<Double, BlockPos> fillMap = new TreeMap<>();

        getCosmos().getHoleManager().getHoles().forEach(hole -> {
            if (mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(hole.getHole())).isEmpty()) {
                double targetDistance = Math.sqrt(fillTarget.getDistanceSq(hole.getHole()));
                double localDistance = Math.sqrt(mc.player.getDistanceSq(hole.getHole()));

                boolean fillable = true;
                switch (mode.getValue()) {
                    case TARGETED:
                        fillable = targetDistance <= threshold.getValue() && localDistance <= range.getValue();
                        break;
                    case ALL:
                        fillable = localDistance <= range.getValue();
                        break;
                }

                if (localDistance < targetDistance && safety.getValue()) {
                    fillable = false;
                }

                if (!doubles.getValue() && hole.isDouble()) {
                    fillable = false;
                }

                if (fillable) {
                    fillMap.put(mode.getValue().equals(Filler.TARGETED) ? targetDistance : localDistance, hole.getHole());
                }
            }
        });

        switch (completion.getValue()) {
            case COMPLETION:
                if (fillMap.isEmpty()) {
                    disable();
                }

                break;
            case TARGET:
                if (fillTarget == null || EnemyUtil.isDead(fillTarget)) {
                    disable();
                }

                break;
            case PERSISTENT:
                break;
        }

        if (!fillMap.isEmpty()) {
            return fillMap.firstEntry().getValue();
        }

        return null;
    }

    @Override
    public void onUpdate() {
        if (fillPosition == null) {
            fillTarget = null;
            fillPosition = null;
            return;
        }

        previousSlot = mc.player.inventory.currentItem;

        InventoryUtil.switchToSlot(block.getValue().getItem(), autoSwitch.getValue());

        // entity could've gotten in hole/could've been filled from the time it was calculated
        if (fillPosition == null || !mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(fillPosition)).isEmpty())
            return;

        if (InventoryUtil.isHolding(Item.getItemFromBlock(Blocks.OBSIDIAN))) {
            getCosmos().getInteractionManager().placeBlock(fillPosition, rotate.getValue(), strict.getValue());
        }

        InventoryUtil.switchToSlot(previousSlot, Switch.NORMAL);
    }

    @Override
    public boolean isActive() {
        return isEnabled() && (fillPosition != null);
    }

    @Override
    public void onRender3D() {
        if (nullCheck() && fillPosition != null && render.getValue()) {
            RenderUtil.drawBox(new RenderBuilder().position(new BlockPos(fillPosition)).color(ColorUtil.getPrimaryAlphaColor(60)).setup().line(1.5F).cull(renderMode.getValue().equals(Box.GLOW) || renderMode.getValue().equals(Box.REVERSE)).shade(renderMode.getValue().equals(Box.GLOW) || renderMode.getValue().equals(Box.REVERSE)).alpha(renderMode.getValue().equals(Box.GLOW) || renderMode.getValue().equals(Box.REVERSE)).depth(true).blend().texture());
        }
    }

    public enum Filler {
        ALL, TARGETED
    }

    public enum Completion {
        COMPLETION, TARGET, PERSISTENT
    }

    private enum Block {
        OBSIDIAN(Item.getItemFromBlock(Blocks.OBSIDIAN)), ENDER_CHEST(Item.getItemFromBlock(Blocks.ENDER_CHEST)), PRESSURE_PLATE(Item.getItemFromBlock(Blocks.WOODEN_PRESSURE_PLATE)), WEB(Item.getItemFromBlock(Blocks.WEB));

        private final Item item;

        Block(Item item) {
            this.item = item;
        }

        public Item getItem() {
            return item;
        }
    }
}