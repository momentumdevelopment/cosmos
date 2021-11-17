package cope.cosmos.client.features.modules.combat;

import cope.cosmos.util.client.ColorUtil;
import cope.cosmos.util.combat.EnemyUtil;
import cope.cosmos.util.combat.TargetUtil.Target;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.combat.TargetUtil;
import cope.cosmos.util.player.InventoryUtil;
import cope.cosmos.util.player.InventoryUtil.*;
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

    public static Setting<Filler> mode = new Setting<>("Mode", "Mode for the filler", Filler.TARGETED);
    public static Setting<Block> block = new Setting<>("Block", "Block to use for filling", Block.OBSIDIAN);
    public static Setting<Completion> completion = new Setting<>("Completion", "When to consider the filling complete", Completion.COMPLETION);
    public static Setting<Double> range = new Setting<>("Range", "Range to scan for holes", 0.0, 5.0, 15.0, 1);
    public static Setting<Double> threshold = new Setting<>(() -> mode.getValue().equals(Filler.TARGETED), "Threshold", "Target's distance from hole for it to be considered fill-able", 0.0, 3.0, 15.0, 1);
    public static Setting<Switch> autoSwitch = new Setting<>("Switch", "Mode for switching to block", Switch.NORMAL);

    public static Setting<Boolean> safety = new Setting<>("Safety", "Makes sure you are not the closest player for the current hole fill", false);
    public static Setting<Boolean> doubles = new Setting<>("Doubles", "Fills in double holes", true);

    public static Setting<Rotate> rotate = new Setting<>("Rotation", "Mode for placement rotations", Rotate.NONE);

    public static Setting<Target> target = new Setting<>("Target", "Priority for searching target", Target.CLOSEST);
    public static Setting<Double> targetRange = new Setting<>("Range", "Range to consider a player a target", 0.0, 10.0, 15.0, 0).setParent(target);

    public static Setting<Boolean> render = new Setting<>("Render", "Render a visual of the filling process", true);
    public static Setting<Box> renderMode = new Setting<>("Mode", "Style of the visual", Box.FILL).setParent(render);

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
            getCosmos().getInteractionManager().placeBlock(fillPosition, rotate.getValue());
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