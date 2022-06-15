package cope.cosmos.client.features.modules.visual;

import cope.cosmos.asm.mixins.accessor.IRenderGlobal;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.render.RenderBuilder;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.string.ColorUtil;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

import static cope.cosmos.util.render.RenderBuilder.Box;

/**
 * @author Wolfsurge
 * @since 04/06/22
 */
public class BreakHighlightModule extends Module {
    public static BreakHighlightModule INSTANCE;

    public BreakHighlightModule() {
        super("BreakHighlight", Category.VISUAL, "Highlights blocks that are being broken");
        INSTANCE = this;
    }

    // **************************** render ****************************

    public static Setting<Box> renderMode = new Setting<>("RenderMode", Box.BOTH)
            .setDescription("How to render the highlight");

    public static Setting<Float> lineWidth = new Setting<>("Width", 0.1f, 1.0f, 3f, 1)
            .setDescription("The width of the outline")
            .setVisible(() -> !renderMode.getValue().equals(Box.FILL));

    public static Setting<Boolean> percent = new Setting<>("Percent", true)
            .setDescription("Show the percentage the block has been broken by");

    public static Setting<Boolean> colorFade = new Setting<>("ColorFade", true)
            .setDescription("Whether the color should fade between red and green depending on the progress");

    public static Setting<Float> alpha = new Setting<>("Alpha", 0f, 100f, 255f, 0)
            .setDescription("The alpha of the color");

    // **************************** other ****************************

    public static Setting<Float> range = new Setting<>("Range", 1f, 20f, 50f, 1)
            .setDescription("The maximum distance a highlighted block can be");

    @Override
    public void onRender3D() {

        // Iterate through all blocks being broken
        ((IRenderGlobal) mc.renderGlobal).getDamagedBlocks().forEach((pos, progress) -> {
            if (progress != null) {

                // Get the block being broken
                BlockPos blockPos = progress.getPosition();

                // Don't care about air
                if (mc.world.getBlockState(blockPos).getBlock().equals(Blocks.AIR)) {
                    return;
                }

                // Check block is within range
                if (blockPos.getDistance((int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ) <= range.getValue()) {

                    // Block damage. Clamping this as it can go above 8 for other players, breaking the colour and throwing an exception
                    int damage = MathHelper.clamp(progress.getPartialBlockDamage(), 0, 8);

                    // Block bounding box
                    AxisAlignedBB bb = mc.world.getBlockState(blockPos).getSelectedBoundingBox(mc.world, blockPos);

                    // Render values
                    double x = bb.minX + (bb.maxX - bb.minX) / 2;
                    double y = bb.minY + (bb.maxY - bb.minY) / 2;
                    double z = bb.minZ + (bb.maxZ - bb.minZ) / 2;

                    double sizeX = damage * ((bb.maxX - x) / 8);
                    double sizeY = damage * ((bb.maxY - y) / 8);
                    double sizeZ = damage * ((bb.maxZ - z) / 8);

                    // The colour factor (for a transition between red and green (looks cool))
                    int colourFactor = (damage * 255) / 8;

                    // The colour of the highlight
                    Color colour = colorFade.getValue() ? new Color(255 - colourFactor, colourFactor, 0, alpha.getValue().intValue()) : ColorUtil.getPrimaryAlphaColor(alpha.getValue().intValue());

                    // Draw highlight
                    RenderUtil.drawBox(new RenderBuilder()
                            .position(new AxisAlignedBB(x - sizeX, y - sizeY, z - sizeZ, x + sizeX, y + sizeY, z + sizeZ))
                            .color(colour)
                            .box(renderMode.getValue())
                            .setup()
                            .line(lineWidth.getValue())
                            .depth(true)
                            .blend()
                            .texture());

                    // Draw the percentage
                    if (percent.getValue()) {
                        RenderUtil.drawNametag(blockPos, 0.5f, (damage * 100 / 8) + "%");
                    }
                }
            }
        });
    }
}
