package cope.cosmos.util.render;

import cope.cosmos.util.Wrapper;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

@SuppressWarnings("unused")
public class RenderUtil implements Wrapper {

	public static Tessellator tessellator = Tessellator.getInstance();
	public static BufferBuilder bufferbuilder = tessellator.getBuffer();
	public static Frustum frustum = new Frustum();

	// 3d

	public static void drawBox(RenderBuilder renderBuilder) {
		AxisAlignedBB axisAlignedBB = renderBuilder.getAxisAlignedBB().offset(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);

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

		renderBuilder.build();
	}

	public static void drawSelectionBox(AxisAlignedBB axisAlignedBB, double height, double length, double width, Color color) {
		bufferbuilder.begin(GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
		addChainedFilledBoxVertices(bufferbuilder, axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, axisAlignedBB.maxX + length, axisAlignedBB.maxY + height, axisAlignedBB.maxZ + width, color);
		tessellator.draw();
	}

	public static void addChainedFilledBoxVertices(BufferBuilder builder, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Color color) {
		builder.pos(minX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		builder.pos(minX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		builder.pos(minX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		builder.pos(minX, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		builder.pos(minX, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		builder.pos(minX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		builder.pos(minX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		builder.pos(minX, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		builder.pos(maxX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		builder.pos(maxX, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		builder.pos(maxX, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		builder.pos(maxX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		builder.pos(maxX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		builder.pos(maxX, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		builder.pos(maxX, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		builder.pos(maxX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		builder.pos(minX, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		builder.pos(minX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		builder.pos(minX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		builder.pos(maxX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		builder.pos(minX, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		builder.pos(maxX, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		builder.pos(maxX, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		builder.pos(minX, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		builder.pos(minX, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		builder.pos(minX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		builder.pos(maxX, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		builder.pos(maxX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		builder.pos(maxX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		builder.pos(maxX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
	}

	public static void drawSelectionBoundingBox(AxisAlignedBB axisAlignedBB, double height, double length, double width, Color color) {
		bufferbuilder.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
		addChainedBoundingBoxVertices(bufferbuilder, axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, axisAlignedBB.maxX + length, axisAlignedBB.maxY + height, axisAlignedBB.maxZ + width, color);
		tessellator.draw();
	}

	public static void addChainedBoundingBoxVertices(BufferBuilder buffer, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Color color) {
		buffer.pos(minX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		buffer.pos(minX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(maxX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(maxX, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(minX, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(minX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(minX, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(maxX, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(maxX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(minX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(minX, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(minX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		buffer.pos(minX, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(maxX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		buffer.pos(maxX, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(maxX, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		buffer.pos(maxX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(maxX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
	}

	public static void drawSelectionGlowFilledBox(AxisAlignedBB axisAlignedBB, double height, double length, double width, Color startColor, Color endColor) {
		bufferbuilder.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		addChainedGlowBoxVertices(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, axisAlignedBB.maxX + length, axisAlignedBB.maxY + height, axisAlignedBB.maxZ + width, startColor, endColor);
		tessellator.draw();
	}

	public static void addChainedGlowBoxVertices(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Color startColor, Color endColor) {
		bufferbuilder.pos(minX, minY, minZ).color(startColor.getRed() / 255.0f, startColor.getGreen() / 255.0f, startColor.getBlue() / 255.0f, startColor.getAlpha() / 255.0f).endVertex();
		bufferbuilder.pos(maxX, minY, minZ).color(startColor.getRed() / 255.0f, startColor.getGreen() / 255.0f, startColor.getBlue() / 255.0f, startColor.getAlpha() / 255.0f).endVertex();
		bufferbuilder.pos(maxX, minY, maxZ).color(startColor.getRed() / 255.0f, startColor.getGreen() / 255.0f, startColor.getBlue() / 255.0f, startColor.getAlpha() / 255.0f).endVertex();
		bufferbuilder.pos(minX, minY, maxZ).color(startColor.getRed() / 255.0f, startColor.getGreen() / 255.0f, startColor.getBlue() / 255.0f, startColor.getAlpha() / 255.0f).endVertex();
		bufferbuilder.pos(minX, maxY, minZ).color(endColor.getRed() / 255.0f, endColor.getGreen() / 255.0f, endColor.getBlue() / 255.0f, endColor.getAlpha() / 255.0f).endVertex();
		bufferbuilder.pos(minX, maxY, maxZ).color(endColor.getRed() / 255.0f, endColor.getGreen() / 255.0f, endColor.getBlue() / 255.0f, endColor.getAlpha() / 255.0f).endVertex();
		bufferbuilder.pos(maxX, maxY, maxZ).color(endColor.getRed() / 255.0f, endColor.getGreen() / 255.0f, endColor.getBlue() / 255.0f, endColor.getAlpha() / 255.0f).endVertex();
		bufferbuilder.pos(maxX, maxY, minZ).color(endColor.getRed() / 255.0f, endColor.getGreen() / 255.0f, endColor.getBlue() / 255.0f, endColor.getAlpha() / 255.0f).endVertex();
		bufferbuilder.pos(minX, minY, minZ).color(startColor.getRed() / 255.0f, startColor.getGreen() / 255.0f, startColor.getBlue() / 255.0f, startColor.getAlpha() / 255.0f).endVertex();
		bufferbuilder.pos(minX, maxY, minZ).color(endColor.getRed() / 255.0f, endColor.getGreen() / 255.0f, endColor.getBlue() / 255.0f, endColor.getAlpha() / 255.0f).endVertex();
		bufferbuilder.pos(maxX, maxY, minZ).color(endColor.getRed() / 255.0f, endColor.getGreen() / 255.0f, endColor.getBlue() / 255.0f, endColor.getAlpha() / 255.0f).endVertex();
		bufferbuilder.pos(maxX, minY, minZ).color(startColor.getRed() / 255.0f, startColor.getGreen() / 255.0f, startColor.getBlue() / 255.0f, startColor.getAlpha() / 255.0f).endVertex();
		bufferbuilder.pos(maxX, minY, minZ).color(startColor.getRed() / 255.0f, startColor.getGreen() / 255.0f, startColor.getBlue() / 255.0f, startColor.getAlpha() / 255.0f).endVertex();
		bufferbuilder.pos(maxX, maxY, minZ).color(endColor.getRed() / 255.0f, endColor.getGreen() / 255.0f, endColor.getBlue() / 255.0f, endColor.getAlpha() / 255.0f).endVertex();
		bufferbuilder.pos(maxX, maxY, maxZ).color(endColor.getRed() / 255.0f, endColor.getGreen() / 255.0f, endColor.getBlue() / 255.0f, endColor.getAlpha() / 255.0f).endVertex();
		bufferbuilder.pos(maxX, minY, maxZ).color(startColor.getRed() / 255.0f, startColor.getGreen() / 255.0f, startColor.getBlue() / 255.0f, startColor.getAlpha() / 255.0f).endVertex();
		bufferbuilder.pos(minX, minY, maxZ).color(startColor.getRed() / 255.0f, startColor.getGreen() / 255.0f, startColor.getBlue() / 255.0f, startColor.getAlpha() / 255.0f).endVertex();
		bufferbuilder.pos(maxX, minY, maxZ).color(startColor.getRed() / 255.0f, startColor.getGreen() / 255.0f, startColor.getBlue() / 255.0f, startColor.getAlpha() / 255.0f).endVertex();
		bufferbuilder.pos(maxX, maxY, maxZ).color(endColor.getRed() / 255.0f, endColor.getGreen() / 255.0f, endColor.getBlue() / 255.0f, endColor.getAlpha() / 255.0f).endVertex();
		bufferbuilder.pos(minX, maxY, maxZ).color(endColor.getRed() / 255.0f, endColor.getGreen() / 255.0f, endColor.getBlue() / 255.0f, endColor.getAlpha() / 255.0f).endVertex();
		bufferbuilder.pos(minX, minY, minZ).color(startColor.getRed() / 255.0f, startColor.getGreen() / 255.0f, startColor.getBlue() / 255.0f, startColor.getAlpha() / 255.0f).endVertex();
		bufferbuilder.pos(minX, minY, maxZ).color(startColor.getRed() / 255.0f, startColor.getGreen() / 255.0f, startColor.getBlue() / 255.0f, startColor.getAlpha() / 255.0f).endVertex();
		bufferbuilder.pos(minX, maxY, maxZ).color(endColor.getRed() / 255.0f, endColor.getGreen() / 255.0f, endColor.getBlue() / 255.0f, endColor.getAlpha() / 255.0f).endVertex();
		bufferbuilder.pos(minX, maxY, minZ).color(endColor.getRed() / 255.0f, endColor.getGreen() / 255.0f, endColor.getBlue() / 255.0f, endColor.getAlpha() / 255.0f).endVertex();
	}
	public static void drawClawBox(AxisAlignedBB axisAlignedBB, double height, double length, double width, Color color) {
		bufferbuilder.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
		addChainedClawBoxVertices(bufferbuilder, axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, axisAlignedBB.maxX + length, axisAlignedBB.maxY + height, axisAlignedBB.maxZ + width, color);
		tessellator.draw();
	}

	public static void addChainedClawBoxVertices(BufferBuilder buffer, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Color color) {
		buffer.pos(minX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		buffer.pos(minX, minY, maxZ - 0.8).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(minX, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		buffer.pos(minX, minY, minZ + 0.8).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(maxX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		buffer.pos(maxX, minY, maxZ - 0.8).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(maxX, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		buffer.pos(maxX, minY, minZ + 0.8).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(minX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		buffer.pos(maxX - 0.8, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(minX, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		buffer.pos(maxX - 0.8, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(maxX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		buffer.pos(minX + 0.8, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(maxX, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		buffer.pos(minX + 0.8, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(minX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		buffer.pos(minX, minY + 0.2, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(minX, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		buffer.pos(minX, minY + 0.2, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(maxX, minY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		buffer.pos(maxX, minY + 0.2, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(maxX, minY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		buffer.pos(maxX, minY + 0.2, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(minX, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		buffer.pos(minX, maxY, maxZ - 0.8).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(minX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		buffer.pos(minX, maxY, minZ + 0.8).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(maxX, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		buffer.pos(maxX, maxY, maxZ - 0.8).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(maxX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		buffer.pos(maxX, maxY, minZ + 0.8).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(minX, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		buffer.pos(maxX - 0.8, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(minX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		buffer.pos(maxX - 0.8, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(maxX, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		buffer.pos(minX + 0.8, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(maxX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		buffer.pos(minX + 0.8, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(minX, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		buffer.pos(minX, maxY - 0.2, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(minX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		buffer.pos(minX, maxY - 0.2, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(maxX, maxY, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		buffer.pos(maxX, maxY - 0.2, minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(maxX, maxY, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 0).endVertex();
		buffer.pos(maxX, maxY - 0.2, maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
	}

	public static void drawCircle(RenderBuilder renderBuilder, Vec3d vec3d, double radius, double height, Color color) {
		renderCircle(bufferbuilder, vec3d, radius, height, color);
		renderBuilder.build();
	}

	public static void renderCircle(BufferBuilder buffer, Vec3d vec3d, double radius, double height, Color color) {
		GlStateManager.disableCull();
		GlStateManager.disableAlpha();
		GlStateManager.shadeModel(GL_SMOOTH);
		bufferbuilder.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);

		for (int i = 0; i < 361; i++) {
			buffer.pos((vec3d.x) + Math.sin(Math.toRadians(i)) * radius - mc.getRenderManager().viewerPosX, vec3d.y + height - mc.getRenderManager().viewerPosY, ((vec3d.z) + Math.cos(Math.toRadians(i)) * radius) - mc.getRenderManager().viewerPosZ).color((float) color.getRed() / 255, (float) color.getGreen() / 255, (float) color.getBlue() / 255, 1).endVertex();
		}

		tessellator.draw();

		GlStateManager.enableCull();
		GlStateManager.enableAlpha();
		GlStateManager.shadeModel(GL_FLAT);
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

	// 2d

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

	public static void drawBorderRect(float x, float y, float width, float height, Color color, Color borderColor) {
		drawRect(x, y, width, height, color);
		drawBorder(x, y, width, height, borderColor);
	}
	
	public static void drawGradientVerticalRect(float x, float y, float width, float height, int topColor, int bottomColor) {
		Color top = new Color(topColor, true);
		Color bottom = new Color(bottomColor, true);
		glPushMatrix();
		glDisable(GL_TEXTURE_2D);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glShadeModel(GL_SMOOTH);
		glBegin(GL_QUADS);
		glColor4f((float) top.getRed() / 255, (float) top.getGreen() / 255, (float) top.getBlue() / 255, (float) top.getAlpha() / 255);
		glVertex2f(x, y);
		glColor4f((float) bottom.getRed() / 255, (float) bottom.getGreen() / 255, (float) bottom.getBlue() / 255, (float) bottom.getAlpha() / 255);
		glVertex2f(x, y + height);
		glVertex2f(x + width, y + height);
		glColor4f((float) top.getRed() / 255, (float) top.getGreen() / 255, (float) top.getBlue() / 255, (float) top.getAlpha() / 255);
		glVertex2f(x + width, y);
		glColor4f(0, 0, 0, 1);
		glEnd();
		glEnable(GL_TEXTURE_2D);
		glDisable(GL_BLEND);
		glPopMatrix();
	}
	
	public static void drawGradientVerticalRect(float x, float y, float width, float height, Color topColor, Color bottomColor) {
		glPushMatrix();
		glDisable(GL_TEXTURE_2D);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glShadeModel(GL_SMOOTH);
		glBegin(GL_QUADS);
		glColor4f((float) topColor.getRed() / 255, (float) topColor.getGreen() / 255, (float) topColor.getBlue() / 255, (float) topColor.getAlpha() / 255);
		glVertex2f(x, y);
		glColor4f((float) bottomColor.getRed() / 255, (float) bottomColor.getGreen() / 255, (float) bottomColor.getBlue() / 255, (float) bottomColor.getAlpha() / 255);
		glVertex2f(x, y + height);
		glVertex2f(x + width, y + height);
		glColor4f((float) topColor.getRed() / 255, (float) topColor.getGreen() / 255, (float) topColor.getBlue() / 255, (float) topColor.getAlpha() / 255);
		glVertex2f(x + width, y);
		glColor4f(0, 0, 0, 1);
		glEnd();
		glEnable(GL_TEXTURE_2D);
		glDisable(GL_BLEND);
		glPopMatrix();
	}
	
	public static void drawGradientHorizontalRect(float x, float y, float width, float height, int rightColor, int leftColor) {
		Color right = new Color(rightColor, true);
		Color left = new Color(leftColor, true);
		glPushMatrix();
		glDisable(GL_TEXTURE_2D);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glShadeModel(GL_SMOOTH);
		glBegin(GL_QUADS);
		glColor4f((float) right.getRed() / 255, (float) right.getGreen() / 255, (float) right.getBlue() / 255, (float) right.getAlpha() / 255);
		glVertex2f(x, y);
		glVertex2f(x, y + height);
		glColor4f((float) left.getRed() / 255, (float) left.getGreen() / 255, (float) left.getBlue() / 255, (float) left.getAlpha() / 255);
		glVertex2f(x + width, y + height);
		glVertex2f(x + width, y);
		glColor4f(0, 0, 0, 1);
		glEnd();
		glEnable(GL_TEXTURE_2D);
		glDisable(GL_BLEND);
		glPopMatrix();
	}
	
	public static void drawGradientHorizontalRect(float x, float y, float width, float height, Color right, Color left) {
		glPushMatrix();
		glDisable(GL_TEXTURE_2D);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glShadeModel(GL_SMOOTH);

		glBegin(GL_QUADS); {
			glColor4f((float) right.getRed() / 255, (float) right.getGreen() / 255, (float) right.getBlue() / 255, 0);
			glVertex2f(x, y);
			glVertex2f(x, y + height);
			glColor4f((float) left.getRed() / 255, (float) left.getGreen() / 255, (float) left.getBlue() / 255, 0);
			glVertex2f(x + width, y + height);
			glVertex2f(x + width, y);
			glColor4f(0, 0, 0, 1);
		}

		glEnd();
		glEnable(GL_TEXTURE_2D);
		glDisable(GL_BLEND);
		glPopMatrix();
	}
	
	public static void drawBorder(float x, float y, float width, float height, int color) {
		RenderUtil.drawRect(x - 1, y - 1, 1, height + 2, color);
		RenderUtil.drawRect(x + width, y - 1, 1, height + 2, color);
		RenderUtil.drawRect(x, y - 1, width, 1, color);
		RenderUtil.drawRect(x, y + height, width, 1, color);
	}
	
	public static void drawBorder(float x, float y, float width, float height, Color color) {
		RenderUtil.drawRect(x - 1, y - 1, 1, height + 2, color);
		RenderUtil.drawRect(x + width, y - 1, 1, height + 2, color);
		RenderUtil.drawRect(x, y - 1, width, 1, color);
		RenderUtil.drawRect(x, y + height, width, 1, color);
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

	public static void drawHalfRoundedRect(double x, double y, double width, double height, double radius, Color color) {
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
				glVertex2d((x + 1) + Math.sin(i * Math.PI / 180.0D) * -1.0D, (height - 1) + Math.cos(i * Math.PI / 180.0D) * -1.0D);
			}

			for (i = 0; i <= 90; i++) {
				glVertex2d((width - 1) + Math.sin(i * Math.PI / 180.0D), (height - 1) + Math.cos(i * Math.PI / 180.0D));
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
		bufferbuilder.begin(GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
		bufferbuilder.pos(x, y, 0).endVertex();
		double TWICE_PI = Math.PI * 2;

		for (int i = 0; i <= sides; i++) {
			double angle = (TWICE_PI * i / sides) + Math.toRadians(180);
			bufferbuilder.pos(x + Math.sin(angle) * radius, y + Math.cos(angle) * radius, 0).endVertex();
		}
		
		tessellator.draw();
		glEnable(GL_TEXTURE_2D);
		glDisable(GL_BLEND);
	}

	public static void drawTriangle(float x, float y, float size, float theta, int color) {
		glTranslated(x, y, 0);
		glRotatef(180 + theta, 0, 0, 1);

		float alpha = (float) (color >> 24 & 255) / 255;
		float red = (float) (color >> 16 & 255) / 255;
		float green = (float) (color >> 8 & 255) / 255;
		float blue = (float) (color & 255) / 255;

		glColor4f(red, green, blue, alpha);
		glEnable(GL_BLEND);
		glDisable(GL_TEXTURE_2D);
		glEnable(GL_LINE_SMOOTH);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glLineWidth(1);
		glBegin(GL_TRIANGLE_FAN);

		glVertex2d(0, (1 * size));
		glVertex2d((1 * size), -(1 * size));
		glVertex2d(-(1 * size), -(1 * size));

		glEnd();
		glDisable(GL_LINE_SMOOTH);
		glEnable(GL_TEXTURE_2D);
		glDisable(GL_BLEND);
		glRotatef(-180 - theta, 0, 0, 1);
		glTranslated(-x, -y, 0);
	}

	public static void scissor(int x, int y, int x2, int y2) {
		glScissor(x * new ScaledResolution(mc).getScaleFactor(), (new ScaledResolution(mc).getScaledHeight() - y2) * new ScaledResolution(mc).getScaleFactor(), (x2 - x) * new ScaledResolution(mc).getScaleFactor(), (y2 - y) * new ScaledResolution(mc).getScaleFactor());
	}

	public static boolean isBoundingBoxInFrustum(Entity renderViewEntity, AxisAlignedBB axisAlignedBB) {
		frustum.setPosition(renderViewEntity.posX, renderViewEntity.posY, renderViewEntity.posZ);
		return frustum.isBoundingBoxInFrustum(axisAlignedBB);
	}
}
