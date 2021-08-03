package cope.cosmos.client.features.modules.combat;

import cope.cosmos.asm.mixins.accessor.ICPacketPlayer;
import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.util.combat.EnemyUtil;
import cope.cosmos.util.combat.TargetUtil.Target;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.combat.TargetUtil;
import cope.cosmos.util.player.InventoryUtil;
import cope.cosmos.util.player.InventoryUtil.*;
import cope.cosmos.util.player.PlayerUtil;
import cope.cosmos.util.player.PlayerUtil.Hand;
import cope.cosmos.util.player.Rotation;
import cope.cosmos.util.player.Rotation.Rotate;
import cope.cosmos.util.render.RenderBuilder;
import cope.cosmos.util.render.RenderBuilder.Box;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.world.AngleUtil;
import cope.cosmos.util.world.BlockUtil;
import cope.cosmos.util.world.HoleUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

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
    public static Setting<Hand> swing = new Setting<>("Swing", "Hand to swing when placing", Hand.MAINHAND);
    public static Setting<Switch> autoSwitch = new Setting<>("Switch", "Mode for switching to block", Switch.NORMAL);

    public static Setting<Boolean> safety = new Setting<>("Safety", "Makes sure you are not the closest player for the current hole fill", false);
    public static Setting<Boolean> doubles = new Setting<>("Doubles", "Fills in double holes", true);
    public static Setting<Boolean> packet = new Setting<>("Packet", "Place with packets", true);
    public static Setting<Boolean> confirm = new Setting<>("Confirm", "Confirm the placement", false);

    public static Setting<Rotate> rotate = new Setting<>("Rotation", "Mode for placement rotations", Rotate.NONE);
    public static Setting<Boolean> rotateCenter = new Setting<>("Center", "Center rotations on target", false).setParent(rotate);
    public static Setting<Boolean> rotateRandom = new Setting<>("Random", "Randomize rotations to simulate real rotations", false).setParent(rotate);
    
    public static Setting<Target> target = new Setting<>("Target", "Priority for searching target", Target.CLOSEST);
    public static Setting<Double> targetRange = new Setting<>("Range", "Range to consider a player a target", 0.0, 10.0, 15.0, 0).setParent(target);

    public static Setting<Boolean> render = new Setting<>("Render", "Render a visual of the filling process", true);
    public static Setting<Box> renderMode = new Setting<>("Mode", "Style of the visual", Box.FILL).setParent(render);
    public static Setting<Color> renderColor = new Setting<>("Color", "Color for the visual", new Color(250, 0, 250, 50)).setParent(render);

    int previousSlot;
    EntityPlayer fillTarget;
    public static BlockPos fillPosition = BlockPos.ORIGIN;
    Rotation fillRotation = new Rotation(Float.NaN, Float.NaN, rotate.getValue());

    @Override
    public void onThread() {
        fillPosition = searchFill();
    }

    public BlockPos searchFill() {
        fillTarget = (EntityPlayer) TargetUtil.getTargetEntity(targetRange.getValue(), Target.CLOSEST, true, false, false, false);

        if (fillTarget == null || EnemyUtil.isDead(fillTarget))
            return null;

        TreeMap<Double, BlockPos> fillMap = new TreeMap<>();
        Iterator<BlockPos> potentialHoles = null;

        switch (mode.getValue()) {
            case TARGETED:
                potentialHoles = BlockUtil.getNearbyBlocks(fillTarget, threshold.getValue(), false);
                break;
            case ALL:
                potentialHoles = BlockUtil.getNearbyBlocks(mc.player, range.getValue(), false);
                break;
        }

        EntityPlayer distanceTarget = mode.getValue().equals(Filler.TARGETED) ? fillTarget : mc.player;

        while (potentialHoles.hasNext()) {
            BlockPos calculatedHole = potentialHoles.next();

            if (!mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(calculatedHole)).isEmpty())
                continue;

            double targetDistance = distanceTarget.getDistanceSq(calculatedHole);
            double localDistance = mc.player.getDistanceSq(calculatedHole);

            if (localDistance < targetDistance && safety.getValue())
                continue;

            if (HoleUtil.isBedRockHole(calculatedHole) || HoleUtil.isObsidianHole(calculatedHole))
                fillMap.put(targetDistance, calculatedHole);

            if (doubles.getValue() && (HoleUtil.isDoubleBedrockHoleZ(calculatedHole) || HoleUtil.isDoubleBedrockHoleX(calculatedHole) || HoleUtil.isDoubleObsidianHoleZ(calculatedHole) || HoleUtil.isDoubleObsidianHoleX(calculatedHole)))
                fillMap.put(targetDistance, calculatedHole);
        }

        switch (completion.getValue()) {
            case COMPLETION:
                if (fillMap.isEmpty())
                    disable();

                break;
            case TARGET:
                if (fillTarget == null || EnemyUtil.isDead(fillTarget))
                    disable();

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
        if (fillPosition.equals(BlockPos.ORIGIN) || fillPosition == null) {
            fillTarget = null;
            fillPosition = null;
            return;
        }

        previousSlot = mc.player.inventory.currentItem;

        InventoryUtil.switchToSlot(block.getValue().getItem(), autoSwitch.getValue());

        if (fillPosition != null && !rotate.getValue().equals(Rotate.NONE)) {
            float[] fillAngles = rotateCenter.getValue() ? AngleUtil.calculateCenter(fillPosition) : AngleUtil.calculateAngles(fillPosition);
            fillRotation = new Rotation((float) (fillAngles[0] + (rotateRandom.getValue() ? ThreadLocalRandom.current().nextDouble(-4, 4) : 0)), (float) (fillAngles[1] + (rotateRandom.getValue() ? ThreadLocalRandom.current().nextDouble(-4, 4) : 0)), rotate.getValue());

            if (!Float.isNaN(fillRotation.getYaw()) && !Float.isNaN(fillRotation.getPitch()))
                fillRotation.updateModelRotations();
        }

        // entity could've gotten in hole/could've been filled from the time it was calculated
        if (fillPosition == null || !mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(fillPosition)).isEmpty())
            return;

        if (fillPosition != BlockPos.ORIGIN && InventoryUtil.isHolding(Item.getItemFromBlock(Blocks.OBSIDIAN))) {
            BlockUtil.placeBlock(fillPosition, packet.getValue(), confirm.getValue());
            PlayerUtil.swingArm(swing.getValue());
        }

        InventoryUtil.switchToSlot(previousSlot, Switch.NORMAL);
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketPlayer && !Float.isNaN(fillRotation.getYaw()) && !Float.isNaN(fillRotation.getPitch()) && rotate.getValue().equals(Rotate.PACKET)) {
            ((ICPacketPlayer) event.getPacket()).setYaw(fillRotation.getYaw());
            ((ICPacketPlayer) event.getPacket()).setPitch(fillRotation.getPitch());
        }
    }

    @Override
    public void onRender3D() {
        if (nullCheck() && fillPosition != BlockPos.ORIGIN && render.getValue()) {
            RenderUtil.drawBox(new RenderBuilder().position(new BlockPos(fillPosition)).color(renderColor.getValue()).setup().line(1.5F).cull(renderMode.getValue().equals(Box.GLOW) || renderMode.getValue().equals(Box.REVERSE)).shade(renderMode.getValue().equals(Box.GLOW) || renderMode.getValue().equals(Box.REVERSE)).alpha(renderMode.getValue().equals(Box.GLOW) || renderMode.getValue().equals(Box.REVERSE)).depth(true).blend().texture());
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
            return this.item;
        }
    }
}
