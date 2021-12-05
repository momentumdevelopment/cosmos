package cope.cosmos.client.features.modules.visual;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.HoleManager.Hole;
import cope.cosmos.util.client.ColorUtil;
import cope.cosmos.util.render.RenderBuilder;
import cope.cosmos.util.render.RenderBuilder.Box;
import cope.cosmos.util.render.RenderUtil;

import java.awt.*;

public class HoleESP extends Module {
    public static HoleESP INSTANCE;

    public HoleESP() {
        super("HoleESP", Category.VISUAL, "Highlights nearby safe holes");
        INSTANCE = this;
    }

    public static Setting<Double> range = new Setting<>("Range", "Range to scan for holes", 0.0, 5.0, 20.0, 0);

    public static Setting<Box> main = new Setting<>("Main", "Visual style for the main render", Box.FILL);
    public static Setting<Double> mainHeight = new Setting<>("Height", "Height of the main render", -1.0, 0.1, 3.0, 1).setParent(main);
    public static Setting<Double> mainWidth = new Setting<>(() -> main.getValue().equals(Box.BOTH) || main.getValue().equals(Box.CLAW) || main.getValue().equals(Box.OUTLINE), "Width", "Line width of the main render", 0.0, 1.5, 3.0, 1).setParent(main);

    public static Setting<Box> outline = new Setting<>("Outline", "Visual style for the outline render", Box.OUTLINE);
    public static Setting<Double> outlineHeight = new Setting<>("Height", "Height of the outline render", -1.0, 0.1, 3.0, 1).setParent(outline);
    public static Setting<Double> outlineWidth = new Setting<>(() -> outline.getValue().equals(Box.BOTH) || outline.getValue().equals(Box.CLAW) || outline.getValue().equals(Box.OUTLINE), "Width", "Line width of the outline render", 0.0, 1.5, 3.0, 1).setParent(outline);

    public static Setting<Boolean> depth = new Setting<>("Depth", "Enables vanilla depth", true);
    public static Setting<Boolean> doubles = new Setting<>("Doubles", "Considers double holes as safe holes", true);
    public static Setting<Boolean> voids = new Setting<>("Void", "Highlights void and roof holes", false);

    public static Setting<Boolean> colors = new Setting<>("Colors", "Colors for the rendering", true);
    public static Setting<Color> obsidianColor = new Setting<>("Obsidian", "Color of the obsidian holes", ColorUtil.getPrimaryAlphaColor(45)).setParent(colors);
    public static Setting<Color> mixedColor = new Setting<>("Mixed", "Color of the mixed holes", ColorUtil.getPrimaryAlphaColor(45)).setParent(colors);
    public static Setting<Color> bedrockColor = new Setting<>("Bedrock", "Color of the bedrock holes", ColorUtil.getPrimaryAlphaColor(45)).setParent(colors);
    public static Setting<Color> voidColor = new Setting<>(() -> voids.getValue(), "Void", "Color of the void holes", new Color(255, 0, 0, 45)).setParent(colors);

    @Override
    public void onRender3D() {
        getCosmos().getHoleManager().getHoles().forEach(hole -> {
            if (Math.sqrt(mc.player.getDistanceSq(hole.getHole())) < range.getValue()) {
                drawHole(hole);
            }
        });
    }

    public void drawHole(Hole hole) {
        switch (hole.getType()) {
            case VOID:
                RenderUtil.drawBox(new RenderBuilder().position(hole.getHole()).color(voidColor.getValue()).box(Box.FILL).setup().line(1.5F).depth(true).blend().texture());
                break;
            case OBSIDIAN:
                RenderUtil.drawBox(new RenderBuilder().position(hole.getHole()).height(mainHeight.getValue() - 1).length(0).width(0).color(obsidianColor.getValue()).box(main.getValue()).setup().line(mainWidth.getValue().floatValue()).cull(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).shade(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).alpha(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).depth(depth.getValue()).blend().texture());
                RenderUtil.drawBox(new RenderBuilder().position(hole.getHole()).height(outlineHeight.getValue() - 1).length(0).width(0).color(obsidianColor.getValue()).box(outline.getValue()).setup().line(outlineWidth.getValue().floatValue()).cull(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).shade(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).alpha(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).depth(depth.getValue()).blend().texture());
                break;
            case MIXED:
                RenderUtil.drawBox(new RenderBuilder().position(hole.getHole()).height(mainHeight.getValue() - 1).length(0).width(0).color(mixedColor.getValue()).box(main.getValue()).setup().line(mainWidth.getValue().floatValue()).cull(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).shade(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).alpha(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).depth(depth.getValue()).blend().texture());
                RenderUtil.drawBox(new RenderBuilder().position(hole.getHole()).height(outlineHeight.getValue() - 1).length(0).width(0).color(mixedColor.getValue()).box(outline.getValue()).setup().line(outlineWidth.getValue().floatValue()).cull(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).shade(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).alpha(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).depth(depth.getValue()).blend().texture());
                break;
            case BEDROCK:
                RenderUtil.drawBox(new RenderBuilder().position(hole.getHole()).height(mainHeight.getValue() - 1).length(0).width(0).color(bedrockColor.getValue()).box(main.getValue()).setup().line(mainWidth.getValue().floatValue()).cull(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).shade(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).alpha(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).depth(depth.getValue()).blend().texture());
                RenderUtil.drawBox(new RenderBuilder().position(hole.getHole()).height(outlineHeight.getValue() - 1).length(0).width(0).color(bedrockColor.getValue()).box(outline.getValue()).setup().line(outlineWidth.getValue().floatValue()).cull(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).shade(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).alpha(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).depth(depth.getValue()).blend().texture());
                break;
            case DOUBLEOBSIDIANX:
                if (doubles.getValue()) {
                    RenderUtil.drawBox(new RenderBuilder().position(hole.getHole()).height(mainHeight.getValue() - 1).length(1).width(0).color(obsidianColor.getValue()).box(main.getValue()).setup().line(mainWidth.getValue().floatValue()).cull(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).shade(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).alpha(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).depth(depth.getValue()).blend().texture());
                    RenderUtil.drawBox(new RenderBuilder().position(hole.getHole()).height(outlineHeight.getValue() - 1).length(1).width(0).color(obsidianColor.getValue()).box(outline.getValue()).setup().line(outlineWidth.getValue().floatValue()).cull(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).shade(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).alpha(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).depth(depth.getValue()).blend().texture());
                }

                break;
            case DOUBLEMIXEDX:
                if (doubles.getValue()) {
                    RenderUtil.drawBox(new RenderBuilder().position(hole.getHole()).height(mainHeight.getValue() - 1).length(1).width(0).color(mixedColor.getValue()).box(main.getValue()).setup().line(mainWidth.getValue().floatValue()).cull(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).shade(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).alpha(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).depth(depth.getValue()).blend().texture());
                    RenderUtil.drawBox(new RenderBuilder().position(hole.getHole()).height(outlineHeight.getValue() - 1).length(1).width(0).color(mixedColor.getValue()).box(outline.getValue()).setup().line(outlineWidth.getValue().floatValue()).cull(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).shade(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).alpha(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).depth(depth.getValue()).blend().texture());
                }

                break;
            case DOUBLEBEDROCKX:
                if (doubles.getValue()) {
                    RenderUtil.drawBox(new RenderBuilder().position(hole.getHole()).height(mainHeight.getValue() - 1).length(1).width(0).color(bedrockColor.getValue()).box(main.getValue()).setup().line(mainWidth.getValue().floatValue()).cull(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).shade(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).alpha(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).depth(depth.getValue()).blend().texture());
                    RenderUtil.drawBox(new RenderBuilder().position(hole.getHole()).height(outlineHeight.getValue() - 1).length(1).width(0).color(bedrockColor.getValue()).box(outline.getValue()).setup().line(outlineWidth.getValue().floatValue()).cull(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).shade(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).alpha(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).depth(depth.getValue()).blend().texture());
                }

                break;
            case DOUBLEOBSIDIANZ:
                if (doubles.getValue()) {
                    RenderUtil.drawBox(new RenderBuilder().position(hole.getHole()).height(mainHeight.getValue() - 1).length(0).width(1).color(obsidianColor.getValue()).box(main.getValue()).setup().line(mainWidth.getValue().floatValue()).cull(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).shade(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).alpha(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).depth(depth.getValue()).blend().texture());
                    RenderUtil.drawBox(new RenderBuilder().position(hole.getHole()).height(outlineHeight.getValue() - 1).length(0).width(1).color(obsidianColor.getValue()).box(outline.getValue()).setup().line(outlineWidth.getValue().floatValue()).cull(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).shade(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).alpha(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).depth(depth.getValue()).blend().texture());
                }

                break;
            case DOUBLEMIXEDZ:
                if (doubles.getValue()) {
                    RenderUtil.drawBox(new RenderBuilder().position(hole.getHole()).height(mainHeight.getValue() - 1).length(0).width(1).color(mixedColor.getValue()).box(main.getValue()).setup().line(mainWidth.getValue().floatValue()).cull(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).shade(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).alpha(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).depth(depth.getValue()).blend().texture());
                    RenderUtil.drawBox(new RenderBuilder().position(hole.getHole()).height(outlineHeight.getValue() - 1).length(0).width(1).color(mixedColor.getValue()).box(outline.getValue()).setup().line(outlineWidth.getValue().floatValue()).cull(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).shade(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).alpha(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).depth(depth.getValue()).blend().texture());
                }

                break;
            case DOUBLEBEDROCKZ:
                if (doubles.getValue()) {
                    RenderUtil.drawBox(new RenderBuilder().position(hole.getHole()).height(mainHeight.getValue() - 1).length(0).width(1).color(bedrockColor.getValue()).box(main.getValue()).setup().line(mainWidth.getValue().floatValue()).cull(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).shade(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).alpha(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).depth(depth.getValue()).blend().texture());
                    RenderUtil.drawBox(new RenderBuilder().position(hole.getHole()).height(outlineHeight.getValue() - 1).length(0).width(1).color(bedrockColor.getValue()).box(outline.getValue()).setup().line(outlineWidth.getValue().floatValue()).cull(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).shade(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).alpha(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).depth(depth.getValue()).blend().texture());
                }

                break;
        }
    }
}