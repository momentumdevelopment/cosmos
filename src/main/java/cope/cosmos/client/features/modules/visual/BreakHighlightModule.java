package cope.cosmos.client.features.modules.visual;

import cope.cosmos.asm.mixins.accessor.IRenderGlobal;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.render.RenderBuilder;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.string.ColorUtil;
import cope.cosmos.util.world.BlockUtil;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import static cope.cosmos.util.render.RenderBuilder.Box;

/**
 * @author Surge
 * @since 04/06/22
 */
public class BreakHighlightModule extends Module {
    public static BreakHighlightModule INSTANCE;

    public BreakHighlightModule() {
        super("BreakHighlight", new String[] {"BreakESP"}, Category.VISUAL, "Highlights blocks that are being broken");
        INSTANCE = this;
    }

    // **************************** render ****************************

    public static Setting<Box> renderMode = new Setting<>("RenderMode", Box.BOTH)
            .setDescription("How to render the highlight")
            .setExclusion(Box.GLOW, Box.REVERSE, Box.NONE);

    public static Setting<Float> lineWidth = new Setting<>("Width", 0.1F, 1.0F, 3F, 1)
            .setAlias("LineWidth")
            .setDescription("The width of the outline")
            .setVisible(() -> !renderMode.getValue().equals(Box.FILL));

    public static Setting<Boolean> percent = new Setting<>("Percent", false)
            .setDescription("Show the percentage the block has been broken by");

    @Override
    public void onRender3D() {

        // Iterate through all blocks being broken
        ((IRenderGlobal) mc.renderGlobal).getDamagedBlocks().forEach((pos, progress) -> {
            if (progress != null) {

                // Get the block being broken
                BlockPos position = progress.getPosition();

                // Don't care about air
                if (mc.world.getBlockState(position).getBlock().equals(Blocks.AIR)) {
                    return;
                }

                // Check block is within range
                if (BlockUtil.getDistanceToCenter(mc.player, position) <= 20) {

                    // Block damage. Clamping this as it can go above 8 for other players, breaking the colour and throwing an exception
                    int damage = MathHelper.clamp(progress.getPartialBlockDamage(), 0, 8);

                    // Block bounding box
                    AxisAlignedBB bb = mc.world.getBlockState(position).getSelectedBoundingBox(mc.world, position);

                    // Render values
                    double x = bb.minX + (bb.maxX - bb.minX) / 2;
                    double y = bb.minY + (bb.maxY - bb.minY) / 2;
                    double z = bb.minZ + (bb.maxZ - bb.minZ) / 2;

                    double sizeX = damage * ((bb.maxX - x) / 8);
                    double sizeY = damage * ((bb.maxY - y) / 8);
                    double sizeZ = damage * ((bb.maxZ - z) / 8);

                    // Draw highlight
                    RenderUtil.drawBox(new RenderBuilder()
                            .position(new AxisAlignedBB(x - sizeX, y - sizeY, z - sizeZ, x + sizeX, y + sizeY, z + sizeZ))
                            .color(ColorUtil.getPrimaryAlphaColor(120))
                            .box(renderMode.getValue())
                            .setup()
                            .line(lineWidth.getValue())
                            .depth(true)
                            .blend()
                            .texture());

                    // Draw the percentage
                    if (percent.getValue()) {
                        RenderUtil.drawNametag(position, 0.5f, (damage * 100 / 8) + "%");
                    }
                }
            }
        });
    }
}
