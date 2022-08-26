package cope.cosmos.util.render;

import cope.cosmos.client.Cosmos;
import cope.cosmos.util.Wrapper;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author bon55, linustouchtips, Ethius
 * @since 05/21/2021
 */
public class RenderUtil implements Wrapper {

	// renderers
	public static Tessellator tessellator = Tessellator.getInstance();
	public static BufferBuilder bufferBuilder = tessellator.getBuffer();

	// resolution
	private static ScaledResolution resolution = new ScaledResolution(mc);

	static {
		Cosmos.EVENT_BUS.register(new Object() {

			@SubscribeEvent
			public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {

				// update resolution
				RenderUtil.resolution = event.getResolution();
			}
		});
	}

	// ********************************** 3d ************************************** //

	/**
	 * Draws a box based on requirements specified by a {@link RenderBuilder} render bufferBuilder
	 * @param renderBuilder The requirements of the box
	 */
	public static void drawBox(RenderBuilder renderBuilder) {

		// check if the viewing entity exists
		if (mc.getRenderViewEntity() != null) {

			// render bounding box
			AxisAlignedBB axisAlignedBB = renderBuilder.getAxisAlignedBB()
					.offset(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);

			// draw box
			switch (renderBuilder.getBox()) {
				case FILL:
					drawSelectionBox(axisAlignedBB, renderBuilder.getHeight(), renderBuilder.getLength(), renderBuilder.getWidth(), renderBuilder.getColor());
					break;
				case OUTLINE:
					drawSelectionBoundingBox(axisAlignedBB, renderBuilder.getHeight(), renderBuilder.getLength(), renderBuilder.getWidth(), new Color(renderBuilder.getColor().getRed(), renderBuilder.getColor().getGreen(), renderBuilder.getColor().getBlue(), 144));
					break;
				case BOTH:
					drawSelectionBox(axisAlignedBB, renderBuilder.getHeight(), renderBuilder.getLength(), renderBuilder.getWidth(), renderBuilder.getColor());
					drawSelectionBoundingBox(axisAlignedBB, renderBuilder.getHeight(), renderBuilder.getLength(), renderBuilder.getWidth(), new Color(renderBuilder.getColor().getRed(), renderBuilder.getColor().getGreen(), renderBuilder.getColor().getBlue(), 144));
					break;
				case GLOW:
					drawSelectionGlowFilledBox(axisAlignedBB, renderBuilder.getHeight(), renderBuilder.getLength(), renderBuilder.getWidth(), renderBuilder.getColor(), new Color(renderBuilder.getColor().getRed(), renderBuilder.getColor().getGreen(), renderBuilder.getColor().getBlue(), 0));
					break;
				case REVERSE:
					drawSelectionGlowFilledBox(axisAlignedBB, renderBuilder.getHeight(), renderBuilder.getLength(), renderBuilder.getWidth(), new Color(renderBuilder.getColor().getRed(), renderBuilder.getColor().getGreen(), renderBuilder.getColor().getBlue(), 0), renderBuilder.getColor());
					break;
				case CLAW:
					drawClawBox(axisAlignedBB, renderBuilder.getHeight(), renderBuilder.getLength(), renderBuilder.getWidth(), new Color(renderBuilder.getColor().getRed(), renderBuilder.getColor().getGreen(), renderBuilder.getColor().getBlue(), 255));
					break;
			}

			// build the render
			renderBuilder.build();
		}
	}

	/**
	 * Draws a box with specified dimensions at a specified position
	 * @param axisAlignedBB The specified position
	 * @param height The height of the box
	 * @param length The length of the box
	 * @param width The width of the box
	 * @param color The color of the box
	 */
	public static void drawSelectionBox(AxisAlignedBB axisAlignedBB, double height, double length, double width, Color color) {

		// begin the bufferBuilder bufferBuilder (fill)
		bufferBuilder.begin(GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);

		// add box vertices
		addChainedFilledBoxVertices(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, axisAlignedBB.maxX + length, axisAlignedBB.maxY + height, axisAlignedBB.maxZ + width, color);

		// draw the box
		tessellator.draw();
	}

	/**
	 * Add box vertices to the bufferBuilder bufferBuilder
	 * @param minX The min x of the box
	 * @param minY The min y of the box
	 * @param minZ The min z of the box
	 * @param maxX The max x of the box
	 * @param maxY The max y of the box
	 * @param maxZ The max z of the box
	 * @param color The color of the box
	 */
	public static void addChainedFilledBoxVertices(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Color color) {

		// min, min, min, max, max, max -> max, max, max, min, min, min
		bufferBuilder.pos(minX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(minX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(minX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(minX, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(minX, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(minX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(minX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(minX, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(maxX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(maxX, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(maxX, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(maxX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(maxX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(maxX, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(maxX, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(maxX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(minX, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(minX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(minX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(maxX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(minX, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(maxX, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(maxX, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(minX, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(minX, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(minX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(maxX, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(maxX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(maxX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(maxX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
	}

	/**
	 * Draws an outlined box with specified dimensions at a specified position
	 * @param axisAlignedBB The specified position
	 * @param height The height of the box
	 * @param length The length of the box
	 * @param width The width of the box
	 * @param color The color of the box
	 */
	public static void drawSelectionBoundingBox(AxisAlignedBB axisAlignedBB, double height, double length, double width, Color color) {

		// begin the buffer bufferBuilder (outline)
		bufferBuilder.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);

		// add outline box vertices
		addChainedBoundingBoxVertices(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, axisAlignedBB.maxX + length, axisAlignedBB.maxY + height, axisAlignedBB.maxZ + width, color);

		// draw the outlined box
		tessellator.draw();
	}

	/**
	 * Add outlined box vertices to the bufferBuilder bufferBuilder
	 * @param minX The min x of the box
	 * @param minY The min y of the box
	 * @param minZ The min z of the box
	 * @param maxX The max x of the box
	 * @param maxY The max y of the box
	 * @param maxZ The max z of the box
	 * @param color The color of the box
	 */
	public static void addChainedBoundingBoxVertices(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Color color) {

		// min, min, min, max, max, max -> max, max, max, min, min, min (alpha 0 for inner)
		bufferBuilder.pos(minX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		bufferBuilder.pos(minX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(maxX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(maxX, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(minX, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(minX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(minX, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(maxX, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(maxX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(minX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(minX, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(minX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		bufferBuilder.pos(minX, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(maxX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		bufferBuilder.pos(maxX, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(maxX, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		bufferBuilder.pos(maxX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(maxX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
	}

	/**
	 * Draws a glowing (gradient) box with specified dimensions at a specified position
	 * @param axisAlignedBB The specified position
	 * @param height The height of the box
	 * @param length The length of the box
	 * @param width The width of the box
	 * @param startColor The first color of the box
	 * @param endColor The second color of the box
	 */
	public static void drawSelectionGlowFilledBox(AxisAlignedBB axisAlignedBB, double height, double length, double width, Color startColor, Color endColor) {

		// begin the buffer bufferBuilder (glow, QUADS)
		bufferBuilder.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

		// add glow box vertices
		addChainedGlowBoxVertices(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, axisAlignedBB.maxX + length, axisAlignedBB.maxY + height, axisAlignedBB.maxZ + width, startColor, endColor);

		// draw the glow box
		tessellator.draw();
	}

	/**
	 * Add glow box vertices to the bufferBuilder bufferBuilder
	 * @param minX The min x of the box
	 * @param minY The min y of the box
	 * @param minZ The min z of the box
	 * @param maxX The max x of the box
	 * @param maxY The max y of the box
	 * @param maxZ The max z of the box
	 * @param startColor The first color of the box
	 * @param endColor The second color of the box
	 */
	public static void addChainedGlowBoxVertices(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Color startColor, Color endColor) {

		// min, min, min, max, max, max (alpha 255) -> max, max, max, min, min, min (alpha 0)
		bufferBuilder.pos(minX, minY, minZ).color(startColor.getRed() / 255F, startColor.getGreen() / 255F, startColor.getBlue() / 255F, startColor.getAlpha() / 255F).endVertex();
		bufferBuilder.pos(maxX, minY, minZ).color(startColor.getRed() / 255F, startColor.getGreen() / 255F, startColor.getBlue() / 255F, startColor.getAlpha() / 255F).endVertex();
		bufferBuilder.pos(maxX, minY, maxZ).color(startColor.getRed() / 255F, startColor.getGreen() / 255F, startColor.getBlue() / 255F, startColor.getAlpha() / 255F).endVertex();
		bufferBuilder.pos(minX, minY, maxZ).color(startColor.getRed() / 255F, startColor.getGreen() / 255F, startColor.getBlue() / 255F, startColor.getAlpha() / 255F).endVertex();
		bufferBuilder.pos(minX, maxY, minZ).color(endColor.getRed() / 255F, endColor.getGreen() / 255F, endColor.getBlue() / 255F, endColor.getAlpha() / 255F).endVertex();
		bufferBuilder.pos(minX, maxY, maxZ).color(endColor.getRed() / 255F, endColor.getGreen() / 255F, endColor.getBlue() / 255F, endColor.getAlpha() / 255F).endVertex();
		bufferBuilder.pos(maxX, maxY, maxZ).color(endColor.getRed() / 255F, endColor.getGreen() / 255F, endColor.getBlue() / 255F, endColor.getAlpha() / 255F).endVertex();
		bufferBuilder.pos(maxX, maxY, minZ).color(endColor.getRed() / 255F, endColor.getGreen() / 255F, endColor.getBlue() / 255F, endColor.getAlpha() / 255F).endVertex();
		bufferBuilder.pos(minX, minY, minZ).color(startColor.getRed() / 255F, startColor.getGreen() / 255F, startColor.getBlue() / 255F, startColor.getAlpha() / 255F).endVertex();
		bufferBuilder.pos(minX, maxY, minZ).color(endColor.getRed() / 255F, endColor.getGreen() / 255F, endColor.getBlue() / 255F, endColor.getAlpha() / 255F).endVertex();
		bufferBuilder.pos(maxX, maxY, minZ).color(endColor.getRed() / 255F, endColor.getGreen() / 255F, endColor.getBlue() / 255F, endColor.getAlpha() / 255F).endVertex();
		bufferBuilder.pos(maxX, minY, minZ).color(startColor.getRed() / 255F, startColor.getGreen() / 255F, startColor.getBlue() / 255F, startColor.getAlpha() / 255F).endVertex();
		bufferBuilder.pos(maxX, minY, minZ).color(startColor.getRed() / 255F, startColor.getGreen() / 255F, startColor.getBlue() / 255F, startColor.getAlpha() / 255F).endVertex();
		bufferBuilder.pos(maxX, maxY, minZ).color(endColor.getRed() / 255F, endColor.getGreen() / 255F, endColor.getBlue() / 255F, endColor.getAlpha() / 255F).endVertex();
		bufferBuilder.pos(maxX, maxY, maxZ).color(endColor.getRed() / 255F, endColor.getGreen() / 255F, endColor.getBlue() / 255F, endColor.getAlpha() / 255F).endVertex();
		bufferBuilder.pos(maxX, minY, maxZ).color(startColor.getRed() / 255F, startColor.getGreen() / 255F, startColor.getBlue() / 255F, startColor.getAlpha() / 255F).endVertex();
		bufferBuilder.pos(minX, minY, maxZ).color(startColor.getRed() / 255F, startColor.getGreen() / 255F, startColor.getBlue() / 255F, startColor.getAlpha() / 255F).endVertex();
		bufferBuilder.pos(maxX, minY, maxZ).color(startColor.getRed() / 255F, startColor.getGreen() / 255F, startColor.getBlue() / 255F, startColor.getAlpha() / 255F).endVertex();
		bufferBuilder.pos(maxX, maxY, maxZ).color(endColor.getRed() / 255F, endColor.getGreen() / 255F, endColor.getBlue() / 255F, endColor.getAlpha() / 255F).endVertex();
		bufferBuilder.pos(minX, maxY, maxZ).color(endColor.getRed() / 255F, endColor.getGreen() / 255F, endColor.getBlue() / 255F, endColor.getAlpha() / 255F).endVertex();
		bufferBuilder.pos(minX, minY, minZ).color(startColor.getRed() / 255F, startColor.getGreen() / 255F, startColor.getBlue() / 255F, startColor.getAlpha() / 255F).endVertex();
		bufferBuilder.pos(minX, minY, maxZ).color(startColor.getRed() / 255F, startColor.getGreen() / 255F, startColor.getBlue() / 255F, startColor.getAlpha() / 255F).endVertex();
		bufferBuilder.pos(minX, maxY, maxZ).color(endColor.getRed() / 255F, endColor.getGreen() / 255F, endColor.getBlue() / 255F, endColor.getAlpha() / 255F).endVertex();
		bufferBuilder.pos(minX, maxY, minZ).color(endColor.getRed() / 255F, endColor.getGreen() / 255F, endColor.getBlue() / 255F, endColor.getAlpha() / 255F).endVertex();
	}

	/**
	 * Draws a claw (ends are shorter) box with specified dimensions at a specified position
	 * @param axisAlignedBB The specified position
	 * @param height The height of the box
	 * @param length The length of the box
	 * @param width The width of the box
	 * @param color The color of the box
	 */
	public static void drawClawBox(AxisAlignedBB axisAlignedBB, double height, double length, double width, Color color) {

		// begin the buffer bufferBuilder (claw)
		bufferBuilder.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);

		// add claw box vertices
		addChainedClawBoxVertices(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, axisAlignedBB.maxX + length, axisAlignedBB.maxY + height, axisAlignedBB.maxZ + width, color);

		// draw the glow box
		tessellator.draw();
	}

	/**
	 * Add claw box vertices to the bufferBuilder bufferBuilder
	 * @param minX The min x of the box
	 * @param minY The min y of the box
	 * @param minZ The min z of the box
	 * @param maxX The max x of the box
	 * @param maxY The max y of the box
	 * @param maxZ The max z of the box
	 * @param color The color of the box
	 */
	public static void addChainedClawBoxVertices(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Color color) {

		// min - 0.8, min - 0.8, min - 0.8, max + 0.8, max+ 0.8, max + 0.8 -> max - 0.2, max - 0.2, max - 0.2, min + 0.2, min + 0.2, min + 0.2
		bufferBuilder.pos(minX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		bufferBuilder.pos(minX, minY, maxZ - 0.8).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(minX, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		bufferBuilder.pos(minX, minY, minZ + 0.8).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(maxX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		bufferBuilder.pos(maxX, minY, maxZ - 0.8).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(maxX, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		bufferBuilder.pos(maxX, minY, minZ + 0.8).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(minX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		bufferBuilder.pos(maxX - 0.8, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(minX, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		bufferBuilder.pos(maxX - 0.8, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(maxX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		bufferBuilder.pos(minX + 0.8, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(maxX, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		bufferBuilder.pos(minX + 0.8, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(minX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		bufferBuilder.pos(minX, minY + 0.2, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(minX, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		bufferBuilder.pos(minX, minY + 0.2, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(maxX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		bufferBuilder.pos(maxX, minY + 0.2, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(maxX, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		bufferBuilder.pos(maxX, minY + 0.2, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(minX, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		bufferBuilder.pos(minX, maxY, maxZ - 0.8).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(minX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		bufferBuilder.pos(minX, maxY, minZ + 0.8).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(maxX, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		bufferBuilder.pos(maxX, maxY, maxZ - 0.8).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(maxX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		bufferBuilder.pos(maxX, maxY, minZ + 0.8).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(minX, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		bufferBuilder.pos(maxX - 0.8, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(minX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		bufferBuilder.pos(maxX - 0.8, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(maxX, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		bufferBuilder.pos(minX + 0.8, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(maxX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		bufferBuilder.pos(minX + 0.8, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(minX, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		bufferBuilder.pos(minX, maxY - 0.2, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(minX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		bufferBuilder.pos(minX, maxY - 0.2, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(maxX, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		bufferBuilder.pos(maxX, maxY - 0.2, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		bufferBuilder.pos(maxX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		bufferBuilder.pos(maxX, maxY - 0.2, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
	}

	public static void drawCircle(RenderBuilder renderBuilder, Vec3d vec3d, double radius, double height, Color color) {
		renderCircle(bufferBuilder, vec3d, radius, height, color);
		renderBuilder.build();
	}

	public static void renderCircle(BufferBuilder bufferBuilder, Vec3d vec3d, double radius, double height, Color color) {
		GlStateManager.disableCull();
		GlStateManager.disableAlpha();
		GlStateManager.shadeModel(GL_SMOOTH);
		bufferBuilder.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);

		for (int i = 0; i < 361; i++) {
			bufferBuilder.pos((vec3d.x) + Math.sin(Math.toRadians(i)) * radius - mc.getRenderManager().viewerPosX, vec3d.y + height - mc.getRenderManager().viewerPosY, ((vec3d.z) + Math.cos(Math.toRadians(i)) * radius) - mc.getRenderManager().viewerPosZ).color((float) color.getRed() / 255, (float) color.getGreen() / 255, (float) color.getBlue() / 255, 1).endVertex();
		}

		tessellator.draw();

		GlStateManager.enableCull();
		GlStateManager.enableAlpha();
		GlStateManager.shadeModel(GL_FLAT);
	}

	public static void drawNametag(Vec3d vec3d, String text) {
		GlStateManager.pushMatrix();
		glBillboardDistanceScaled((float) vec3d.x, (float) vec3d.y, (float) vec3d.z, mc.player, 1);
		GlStateManager.disableDepth();
		GlStateManager.translate(-(mc.fontRenderer.getStringWidth(text) / 2.0), 0.0, 0.0);
		FontUtil.drawStringWithShadow(text, 0, 0, -1);
		GlStateManager.popMatrix();
	}

	public static void drawNametag(BlockPos blockPos, float height, String text) {
		GlStateManager.pushMatrix();
		glBillboardDistanceScaled(blockPos.getX() + 0.5f, blockPos.getY() + height, blockPos.getZ() + 0.5f, mc.player, 1);
		GlStateManager.disableDepth();
		GlStateManager.translate(-(mc.fontRenderer.getStringWidth(text) / 2.0), 0.0, 0.0);
		FontUtil.drawStringWithShadow(text, 0, 0, -1);
		GlStateManager.popMatrix();
	}

	public static void glBillboardDistanceScaled(float x, float y, float z, EntityPlayer player, float scale) {
		glBillboard(x, y, z);
		int distance = (int) player.getDistance(x, y, z);
		float scaleDistance = distance / 2F / (2 + (2 - scale));

		if (scaleDistance < 1)
			scaleDistance = 1;

		GlStateManager.scale(scaleDistance, scaleDistance, scaleDistance);
	}

	public static void glBillboard(float x, float y, float z) {
		float scale = 0.02666667f;

		GlStateManager.translate(x - mc.getRenderManager().viewerPosX, y - mc.getRenderManager().viewerPosY, z - mc.getRenderManager().viewerPosZ);
		GlStateManager.glNormal3f(0, 1, 0);
		GlStateManager.rotate(-mc.player.rotationYaw, 0, 1, 0);
		GlStateManager.rotate(mc.player.rotationPitch, (mc.gameSettings.thirdPersonView == 2) ? -1 : 1, 0, 0);
		GlStateManager.scale(-scale, -scale, scale);
	}

	public static void drawLine3D(Vec3d from, Vec3d to, Color color, double lineWidth) {

		// Enable depth
		glDepthMask(false);
		glDisable(GL_DEPTH_TEST);

		glDisable(GL_ALPHA_TEST);
		glEnable(GL_BLEND);
		glDisable(GL_TEXTURE_2D);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glEnable(GL_LINE_SMOOTH);
		glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
		glLineWidth(0.1F);

		// Colour line
		glColor4f(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);

		// Set line width
		glLineWidth((float) lineWidth);
		glBegin(GL_CURRENT_BIT);

		// Draw line
		glVertex3d(from.x, from.y, from.z);
		glVertex3d(to.x, to.y, to.z);

		glEnd();

		// Disable depth
		glDepthMask(true);
		glEnable(GL_DEPTH_TEST);

		glEnable(GL_TEXTURE_2D);
		glDisable(GL_BLEND);
		glEnable(GL_ALPHA_TEST);
		glDisable(GL_LINE_SMOOTH);

		// Reset colour
		glColor4f(1, 1, 1, 1);
	}

	// ********************************** 2d ************************************** //

	public static void drawRect(float x, float y, float width, float height, int color) {
		Color c = new Color(color, true);
		glPushMatrix();
		glDisable(GL_TEXTURE_2D);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glShadeModel(GL_SMOOTH);
		glBegin(GL_QUADS);
		glColor4f((float) c.getRed() / 255, (float) c.getGreen() / 255, (float) c.getBlue() / 255, (float) c.getAlpha() / 255);
		glVertex2f(x, y);
		glVertex2f(x, y + height);
		glVertex2f(x + width, y + height);
		glVertex2f(x + width, y);
		glColor4f(0, 0, 0, 1);
		glEnd();
		glEnable(GL_TEXTURE_2D);
		glDisable(GL_BLEND);
		glPopMatrix();
	}
	
	public static void drawRect(float x, float y, float width, float height, Color color) {
		glPushMatrix();
		glDisable(GL_TEXTURE_2D);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glShadeModel(GL_SMOOTH);
		glBegin(GL_QUADS);
		glColor4f((float) color.getRed() / 255, (float) color.getGreen() / 255, (float) color.getBlue() / 255, (float) color.getAlpha() / 255);
		glVertex2f(x, y);
		glVertex2f(x, y + height);
		glVertex2f(x + width, y + height);
		glVertex2f(x + width, y);
		glColor4f(0, 0, 0, 1);
		glEnd();
		glEnable(GL_TEXTURE_2D);
		glDisable(GL_BLEND);
		glPopMatrix();
	}
	
	public static void drawBorder(float x, float y, float width, float height, Color color) {
		RenderUtil.drawRect(x - 0.5F, y - 0.5F, 0.5F, height + 1, color);
		RenderUtil.drawRect(x + width, y - 0.5F, 0.5F, height + 1, color);
		RenderUtil.drawRect(x, y - 0.5F, width, 0.5F, color);
		RenderUtil.drawRect(x, y + height, width, 0.5F, color);
	}

	public static void drawRoundedRect(double x, double y, double width, double height, double radius, Color color) {
		glPushAttrib(GL_POINTS);

		glScaled(0.5, 0.5, 0.5); {
			x *= 2;
			y *= 2;
			width *= 2;
			height *= 2;

			width += x;
			height += y;

			glEnable(GL_BLEND);
			glDisable(GL_TEXTURE_2D);
			glColor4f(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
			glEnable(GL_LINE_SMOOTH);
			glBegin(GL_POLYGON);

			int i;
			for (i = 0; i <= 90; i++) {
				glVertex2d(x + radius + Math.sin(i * Math.PI / 180.0D) * radius * -1.0D, y + radius + Math.cos(i * Math.PI / 180.0D) * radius * -1.0D);
			}

			for (i = 90; i <= 180; i++) {
				glVertex2d(x + radius + Math.sin(i * Math.PI / 180.0D) * radius * -1.0D, height - radius + Math.cos(i * Math.PI / 180.0D) * radius * -1.0D);
			}

			for (i = 0; i <= 90; i++) {
				glVertex2d(width - radius + Math.sin(i * Math.PI / 180.0D) * radius, height - radius + Math.cos(i * Math.PI / 180.0D) * radius);
			}

			for (i = 90; i <= 180; i++) {
				glVertex2d(width - radius + Math.sin(i * Math.PI / 180.0D) * radius, y + radius + Math.cos(i * Math.PI / 180.0D) * radius);
			}

			glEnd();
			glEnable(GL_TEXTURE_2D);
			glDisable(GL_BLEND);
			glDisable(GL_LINE_SMOOTH);
			glDisable(GL_BLEND);
			glEnable(GL_TEXTURE_2D);
		}

		glScaled(2, 2, 2);
		glPopAttrib();
	}

	public static void drawPolygon(double x, double y, float radius, int sides, Color color) {
		glEnable(GL_BLEND);
		glDisable(GL_TEXTURE_2D);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glColor4f((float) color.getRed() / 255, (float) color.getGreen() / 255, (float) color.getBlue() / 255, (float) color.getAlpha() / 255);
		bufferBuilder.begin(GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
		bufferBuilder.pos(x, y, 0).endVertex();
		double TWICE_PI = Math.PI * 2;

		for (int i = 0; i <= sides; i++) {
			double angle = (TWICE_PI * i / sides) + Math.toRadians(180);
			bufferBuilder.pos(x + Math.sin(angle) * radius, y + Math.cos(angle) * radius, 0).endVertex();
		}
		
		tessellator.draw();
		glEnable(GL_TEXTURE_2D);
		glDisable(GL_BLEND);
	}

	public static double getDisplayWidth() {
		return resolution.getScaledWidth_double();
	}

	public static double getDisplayHeight() {
		return resolution.getScaledHeight_double();
	}
}
