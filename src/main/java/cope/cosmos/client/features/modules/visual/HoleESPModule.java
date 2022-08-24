package cope.cosmos.client.features.modules.visual;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.HoleManager.Type;
import cope.cosmos.util.string.ColorUtil;
import cope.cosmos.util.render.RenderBuilder;
import cope.cosmos.util.render.RenderBuilder.Box;
import cope.cosmos.util.render.RenderUtil;
import net.minecraft.util.math.BlockPos;

import java.awt.*;

/**
 * @author linustouchtips
 * @since 06/08/2021
 */
public class HoleESPModule extends Module {
    public static HoleESPModule INSTANCE;

    public HoleESPModule() {
        super("HoleESP", Category.VISUAL, "Highlights nearby safe holes");
        INSTANCE = this;
    }

    // **************************** general settings ****************************

    public static Setting<Double> range = new Setting<>("Range", 0.0, 5.0, 20.0, 0).setDescription("Range to scan for holes");

    // **************************** fill settings ****************************
    
    public static Setting<Box> main = new Setting<>("Main", Box.FILL)
            .setDescription("Visual style for the main render")
            .setExclusion(Box.OUTLINE, Box.CLAW, Box.BOTH);
    
    public static Setting<Double> mainHeight = new Setting<>("MainHeight", -1.0, 0.1, 3.0, 1)
            .setDescription("Height of the main render");
    
    public static Setting<Double> mainWidth = new Setting<>("MainWidth", 0.0, 1.5, 3.0, 1)
            .setDescription("Line width of the main render")
            .setVisible(() -> main.getValue().equals(Box.BOTH) || main.getValue().equals(Box.CLAW) || main.getValue().equals(Box.OUTLINE));

    // **************************** outline settings ****************************
    
    public static Setting<Box> outline = new Setting<>("Outline", Box.OUTLINE)
            .setDescription("Visual style for the outline render")
            .setExclusion(Box.GLOW, Box.REVERSE, Box.FILL, Box.BOTH);
    
    public static Setting<Double> outlineHeight = new Setting<>("OutlineHeight", -1.0, 0.1, 3.0, 1)
            .setDescription("Height of the outline render");
    
    public static Setting<Double> outlineWidth = new Setting<>("OutlineWidth", 0.0, 1.5, 3.0, 1)
            .setDescription("Line width of the outline render")
            .setVisible(() -> outline.getValue().equals(Box.BOTH) || outline.getValue().equals(Box.CLAW) || outline.getValue().equals(Box.OUTLINE));

    // **************************** general settings ****************************
    
    public static Setting<Boolean> depth = new Setting<>("Depth", true)
            .setDescription("Enables vanilla depth");

    // type??
    public static Setting<Boolean> doubles = new Setting<>("Doubles", true)
            .setAlias("Double")
            .setDescription("Considers double holes as safe holes");
    
    public static Setting<Boolean> quads = new Setting<>("Quads", true)
            .setAlias("Quad")
            .setDescription("Considers quad holes as safe holes");
    
    public static Setting<Boolean> voids = new Setting<>("Void", false)
            .setDescription("Highlights void and roof holes");

    // **************************** color settings ****************************

    public static Setting<Boolean> colorSync = new Setting<>("ColorSync", true)
            .setAlias("Sync")
            .setDescription("Syncs holes colors to client colors");
    
    public static Setting<Color> obsidianColor = new Setting<>("ObsidianColor", new Color(184, 40, 40,45))
            .setAlias("ObbyColor")
            .setDescription("Color of the obsidian holes");

    public static Setting<Color> mixedColor = new Setting<>("MixedColor", new Color(203, 203, 42,45))
            .setDescription("Color of the mixed holes");

    public static Setting<Color> bedrockColor = new Setting<>("BedrockColor", new Color(48, 179, 67,45))
            .setDescription("Color of the bedrock holes");

    public static Setting<Color> voidColor = new Setting<>("VoidColor", new Color(98, 0, 255, 45))
            .setDescription("Color of the void holes")
            .setVisible(() -> voids.getValue());

    @Override
    public void onRender3D() {

        // get the holes
        getCosmos().getHoleManager().getHoles().forEach(hole -> {

            // the position of the hole
            BlockPos holePosition = hole.getHole();

            // check if they are in range
            if (mc.player.getDistance(holePosition.getX(), holePosition.getY(), holePosition.getZ()) < range.getValue()) {

                // void holes
                if (voids.getValue()) {
                    if (hole.getType().equals(Type.VOID)) {
                        RenderUtil.drawBox(new RenderBuilder()
                                .position(hole.getHole())
                                .color(voidColor.getValue())
                                .box(Box.FILL)
                                .setup()
                                .line(1.5F)
                                .depth(true)
                                .blend()
                                .texture()
                        );
                    }
                }

                // draw the hole
                switch (hole.getType()) {
                    case OBSIDIAN:
                        RenderUtil.drawBox(new RenderBuilder()
                                .position(hole.getHole())
                                .height(mainHeight.getValue() - 1)
                                .length(0)
                                .width(0)
                                .color(colorSync.getValue() ? ColorUtil.getPrimaryAlphaColor(45) : obsidianColor.getValue())
                                .box(main.getValue()).setup()
                                .line(mainWidth.getValue().floatValue())
                                .cull(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                .shade(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                .alpha(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                .depth(depth.getValue())
                                .blend()
                                .texture()
                        );

                        RenderUtil.drawBox(new RenderBuilder()
                                .position(hole.getHole())
                                .height(outlineHeight.getValue() - 1)
                                .length(0)
                                .width(0)
                                .color(colorSync.getValue() ? ColorUtil.getPrimaryAlphaColor(45) : obsidianColor.getValue())
                                .box(outline.getValue())
                                .setup()
                                .line(outlineWidth.getValue().floatValue())
                                .cull(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                .shade(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                .alpha(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                .depth(depth.getValue())
                                .blend()
                                .texture()
                        );

                        break;
                    case MIXED:
                        RenderUtil.drawBox(new RenderBuilder()
                                .position(hole.getHole())
                                .height(mainHeight.getValue() - 1)
                                .length(0)
                                .width(0)
                                .color(colorSync.getValue() ? ColorUtil.getPrimaryAlphaColor(45) : mixedColor.getValue())
                                .box(main.getValue())
                                .setup()
                                .line(mainWidth.getValue().floatValue())
                                .cull(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                .shade(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                .alpha(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                .depth(depth.getValue())
                                .blend()
                                .texture()
                        );

                        RenderUtil.drawBox(new RenderBuilder()
                                .position(hole.getHole())
                                .height(outlineHeight.getValue() - 1)
                                .length(0)
                                .width(0)
                                .color(colorSync.getValue() ? ColorUtil.getPrimaryAlphaColor(45) : mixedColor.getValue())
                                .box(outline.getValue())
                                .setup()
                                .line(outlineWidth.getValue().floatValue())
                                .cull(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                .shade(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                .alpha(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                .depth(depth.getValue())
                                .blend()
                                .texture()
                        );

                        break;
                    case BEDROCK:
                        RenderUtil.drawBox(new RenderBuilder()
                                .position(hole.getHole())
                                .height(mainHeight.getValue() - 1)
                                .length(0)
                                .width(0)
                                .color(colorSync.getValue() ? ColorUtil.getPrimaryAlphaColor(45) : bedrockColor.getValue())
                                .box(main.getValue())
                                .setup()
                                .line(mainWidth.getValue().floatValue())
                                .cull(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                .shade(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                .alpha(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                .depth(depth.getValue())
                                .blend()
                                .texture()
                        );

                        RenderUtil.drawBox(new RenderBuilder()
                                .position(hole.getHole())
                                .height(outlineHeight.getValue() - 1)
                                .length(0)
                                .width(0)
                                .color(colorSync.getValue() ? ColorUtil.getPrimaryAlphaColor(45) : bedrockColor.getValue())
                                .box(outline.getValue())
                                .setup()
                                .line(outlineWidth.getValue().floatValue())
                                .cull(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                .shade(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                .alpha(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                .depth(depth.getValue())
                                .blend()
                                .texture()
                        );

                        break;
                }

                // draw double holes, scale length and width
                if (doubles.getValue()) {
                    switch (hole.getType()) {
                        case DOUBLE_OBSIDIAN_X:
                            RenderUtil.drawBox(new RenderBuilder()
                                    .position(hole.getHole())
                                    .height(mainHeight.getValue() - 1)
                                    .length(1)
                                    .width(0)
                                    .color(colorSync.getValue() ? ColorUtil.getPrimaryAlphaColor(45) : obsidianColor.getValue())
                                    .box(main.getValue())
                                    .setup()
                                    .line(mainWidth.getValue().floatValue())
                                    .cull(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .shade(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .alpha(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .depth(depth.getValue())
                                    .blend()
                                    .texture()
                            );

                            RenderUtil.drawBox(new RenderBuilder()
                                    .position(hole.getHole())
                                    .height(outlineHeight.getValue() - 1)
                                    .length(1)
                                    .width(0)
                                    .color(colorSync.getValue() ? ColorUtil.getPrimaryAlphaColor(45) : obsidianColor.getValue())
                                    .box(outline.getValue())
                                    .setup()
                                    .line(outlineWidth.getValue().floatValue())
                                    .cull(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .shade(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .alpha(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .depth(depth.getValue())
                                    .blend()
                                    .texture()
                            );

                            break;
                        case DOUBLE_MIXED_X:
                            RenderUtil.drawBox(new RenderBuilder()
                                    .position(hole.getHole())
                                    .height(mainHeight.getValue() - 1)
                                    .length(1)
                                    .width(0)
                                    .color(colorSync.getValue() ? ColorUtil.getPrimaryAlphaColor(45) : mixedColor.getValue())
                                    .box(main.getValue())
                                    .setup()
                                    .line(mainWidth.getValue().floatValue())
                                    .cull(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .shade(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .alpha(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .depth(depth.getValue())
                                    .blend()
                                    .texture()
                            );

                            RenderUtil.drawBox(new RenderBuilder()
                                    .position(hole.getHole())
                                    .height(outlineHeight.getValue() - 1)
                                    .length(1)
                                    .width(0)
                                    .color(colorSync.getValue() ? ColorUtil.getPrimaryAlphaColor(45) : mixedColor.getValue())
                                    .box(outline.getValue())
                                    .setup()
                                    .line(outlineWidth.getValue().floatValue())
                                    .cull(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .shade(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .alpha(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .depth(depth.getValue())
                                    .blend()
                                    .texture()
                            );

                            break;
                        case DOUBLE_BEDROCK_X:
                            RenderUtil.drawBox(new RenderBuilder()
                                    .position(hole.getHole())
                                    .height(mainHeight.getValue() - 1)
                                    .length(1)
                                    .width(0)
                                    .color(colorSync.getValue() ? ColorUtil.getPrimaryAlphaColor(45) : bedrockColor.getValue())
                                    .box(main.getValue())
                                    .setup()
                                    .line(mainWidth.getValue().floatValue())
                                    .cull(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .shade(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .alpha(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .depth(depth.getValue())
                                    .blend()
                                    .texture()
                            );

                            RenderUtil.drawBox(new RenderBuilder()
                                    .position(hole.getHole())
                                    .height(outlineHeight.getValue() - 1)
                                    .length(1)
                                    .width(0)
                                    .color(colorSync.getValue() ? ColorUtil.getPrimaryAlphaColor(45) : bedrockColor.getValue())
                                    .box(outline.getValue())
                                    .setup()
                                    .line(outlineWidth.getValue().floatValue())
                                    .cull(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .shade(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .alpha(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .depth(depth.getValue())
                                    .blend()
                                    .texture()
                            );

                            break;
                        case DOUBLE_OBSIDIAN_Z:
                            RenderUtil.drawBox(new RenderBuilder()
                                    .position(hole.getHole())
                                    .height(mainHeight.getValue() - 1)
                                    .length(0)
                                    .width(1)
                                    .color(colorSync.getValue() ? ColorUtil.getPrimaryAlphaColor(45) : obsidianColor.getValue())
                                    .box(main.getValue())
                                    .setup()
                                    .line(mainWidth.getValue().floatValue())
                                    .cull(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .shade(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .alpha(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .depth(depth.getValue())
                                    .blend()
                                    .texture()
                            );

                            RenderUtil.drawBox(new RenderBuilder()
                                    .position(hole.getHole())
                                    .height(outlineHeight.getValue() - 1)
                                    .length(0)
                                    .width(1)
                                    .color(colorSync.getValue() ? ColorUtil.getPrimaryAlphaColor(45) : obsidianColor.getValue())
                                    .box(outline.getValue())
                                    .setup()
                                    .line(outlineWidth.getValue().floatValue())
                                    .cull(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .shade(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .alpha(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .depth(depth.getValue())
                                    .blend()
                                    .texture()
                            );

                            break;
                        case DOUBLE_MIXED_Z:
                            RenderUtil.drawBox(new RenderBuilder()
                                    .position(hole.getHole())
                                    .height(mainHeight.getValue() - 1)
                                    .length(0)
                                    .width(1)
                                    .color(colorSync.getValue() ? ColorUtil.getPrimaryAlphaColor(45) : mixedColor.getValue())
                                    .box(main.getValue())
                                    .setup()
                                    .line(mainWidth.getValue().floatValue())
                                    .cull(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .shade(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .alpha(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .depth(depth.getValue())
                                    .blend()
                                    .texture()
                            );

                            RenderUtil.drawBox(new RenderBuilder()
                                    .position(hole.getHole())
                                    .height(outlineHeight.getValue() - 1)
                                    .length(0)
                                    .width(1)
                                    .color(colorSync.getValue() ? ColorUtil.getPrimaryAlphaColor(45) : mixedColor.getValue())
                                    .box(outline.getValue())
                                    .setup()
                                    .line(outlineWidth.getValue().floatValue())
                                    .cull(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .shade(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .alpha(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .depth(depth.getValue())
                                    .blend()
                                    .texture()
                            );

                            break;
                        case DOUBLE_BEDROCK_Z:
                            RenderUtil.drawBox(new RenderBuilder()
                                    .position(hole.getHole())
                                    .height(mainHeight.getValue() - 1)
                                    .length(0)
                                    .width(1)
                                    .color(colorSync.getValue() ? ColorUtil.getPrimaryAlphaColor(45) : bedrockColor.getValue())
                                    .box(main.getValue())
                                    .setup()
                                    .line(mainWidth.getValue().floatValue())
                                    .cull(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .shade(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .alpha(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .depth(depth.getValue())
                                    .blend()
                                    .texture()
                            );

                            RenderUtil.drawBox(new RenderBuilder()
                                    .position(hole.getHole())
                                    .height(outlineHeight.getValue() - 1)
                                    .length(0)
                                    .width(1)
                                    .color(colorSync.getValue() ? ColorUtil.getPrimaryAlphaColor(45) : bedrockColor.getValue())
                                    .box(outline.getValue())
                                    .setup()
                                    .line(outlineWidth.getValue().floatValue())
                                    .cull(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .shade(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .alpha(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .depth(depth.getValue())
                                    .blend()
                                    .texture()
                            );

                            break;
                    }
                }

                // draw quad holes, scale length and width
                if (quads.getValue()) {
                    switch (hole.getType()) {
                        case QUAD_OBSIDIAN:
                            RenderUtil.drawBox(new RenderBuilder()
                                    .position(hole.getHole())
                                    .height(mainHeight.getValue() - 1)
                                    .length(1)
                                    .width(1)
                                    .color(colorSync.getValue() ? ColorUtil.getPrimaryAlphaColor(45) : obsidianColor.getValue())
                                    .box(main.getValue())
                                    .setup()
                                    .line(mainWidth.getValue().floatValue())
                                    .cull(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .shade(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .alpha(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .depth(depth.getValue())
                                    .blend()
                                    .texture()
                            );

                            RenderUtil.drawBox(new RenderBuilder()
                                    .position(hole.getHole())
                                    .height(outlineHeight.getValue() - 1)
                                    .length(1)
                                    .width(1)
                                    .color(colorSync.getValue() ? ColorUtil.getPrimaryAlphaColor(45) : obsidianColor.getValue())
                                    .box(outline.getValue())
                                    .setup()
                                    .line(outlineWidth.getValue().floatValue())
                                    .cull(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .shade(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .alpha(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .depth(depth.getValue())
                                    .blend()
                                    .texture()
                            );

                            break;
                        case QUAD_BEDROCK:
                            RenderUtil.drawBox(new RenderBuilder()
                                    .position(hole.getHole())
                                    .height(mainHeight.getValue() - 1)
                                    .length(1)
                                    .width(1)
                                    .color(colorSync.getValue() ? ColorUtil.getPrimaryAlphaColor(45) : bedrockColor.getValue())
                                    .box(main.getValue())
                                    .setup()
                                    .line(mainWidth.getValue().floatValue())
                                    .cull(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .shade(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .alpha(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .depth(depth.getValue())
                                    .blend()
                                    .texture()
                            );

                            RenderUtil.drawBox(new RenderBuilder()
                                    .position(hole.getHole())
                                    .height(outlineHeight.getValue() - 1)
                                    .length(1)
                                    .width(1)
                                    .color(colorSync.getValue() ? ColorUtil.getPrimaryAlphaColor(45) : bedrockColor.getValue())
                                    .box(outline.getValue())
                                    .setup()
                                    .line(outlineWidth.getValue().floatValue())
                                    .cull(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .shade(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .alpha(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .depth(depth.getValue())
                                    .blend()
                                    .texture()
                            );

                            break;
                        case QUAD_MIXED:
                            RenderUtil.drawBox(new RenderBuilder()
                                    .position(hole.getHole())
                                    .height(mainHeight.getValue() - 1)
                                    .length(1)
                                    .width(1)
                                    .color(colorSync.getValue() ? ColorUtil.getPrimaryAlphaColor(45) : mixedColor.getValue())
                                    .box(main.getValue())
                                    .setup()
                                    .line(mainWidth.getValue().floatValue())
                                    .cull(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .shade(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .alpha(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .depth(depth.getValue())
                                    .blend()
                                    .texture()
                            );

                            RenderUtil.drawBox(new RenderBuilder()
                                    .position(hole.getHole())
                                    .height(outlineHeight.getValue() - 1)
                                    .length(1)
                                    .width(1)
                                    .color(colorSync.getValue() ? ColorUtil.getPrimaryAlphaColor(45) : mixedColor.getValue())
                                    .box(outline.getValue())
                                    .setup()
                                    .line(outlineWidth.getValue().floatValue())
                                    .cull(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .shade(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .alpha(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE))
                                    .depth(depth.getValue())
                                    .blend()
                                    .texture()
                            );

                            break;
                    }
                }
            }
        });
    }
}