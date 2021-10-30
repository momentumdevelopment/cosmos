package cope.cosmos.client.features.modules.visual;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.HoleManager;
import cope.cosmos.util.client.ColorUtil;
import cope.cosmos.util.render.RenderBuilder;
import cope.cosmos.util.render.RenderBuilder.Box;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.system.Timer;
import cope.cosmos.util.system.Timer.Format;
import cope.cosmos.util.world.BlockUtil;
import cope.cosmos.util.world.HoleUtil;
import net.minecraft.util.math.BlockPos;

import java.awt.Color;
import java.util.*;

public class HoleESP extends Module {
    public static HoleESP INSTANCE;

    public HoleESP() {
        super("HoleESP", Category.VISUAL, "Highlights nearby safe holes", () -> Setting.formatEnum(main.getValue()) + ", " + Setting.formatEnum(outline.getValue()));
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
    public static Setting<Color> bedrockColor = new Setting<>("Bedrock", "Color of the bedrock holes", ColorUtil.getPrimaryAlphaColor(45)).setParent(colors);
    public static Setting<Color> voidColor = new Setting<>(() -> voids.getValue(), "Void", "Color of the void holes", new Color(255, 0, 0, 45)).setParent(colors);

    public static Map<HoleManager.Hole, Color> holes = new HashMap<>();
    public static Timer holeTimer = new Timer();

    @Override
    public void onRender3D() {
        for (Map.Entry<HoleManager.Hole, Color> holeEntry : new HashSet<>(holes.entrySet())) {
            renderHole(holeEntry.getKey(), holeEntry.getValue());
        }
    }

    public void renderHole(HoleManager.Hole hole, Color color) {
        if (hole.getType().equals(HoleManager.Type.VOID)) {
            RenderUtil.drawBox(new RenderBuilder().position(hole.getHole()).color(color).box(Box.FILL).setup().line(1.5F).depth(true).blend().texture());
        }

        else {
            RenderUtil.drawBox(new RenderBuilder().position(hole.getHole()).height(mainHeight.getValue() - 1).length((hole.getType().equals(HoleManager.Type.DOUBLEBEDROCKX) || hole.getType().equals(HoleManager.Type.DOUBLEOBSIDIANX)) ? 1 : 0).width((hole.getType().equals(HoleManager.Type.DOUBLEBEDROCKZ) || hole.getType().equals(HoleManager.Type.DOUBLEOBSIDIANZ)) ? 1 : 0).color(color).box(main.getValue()).setup().line((float) ((double) mainWidth.getValue())).cull(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).shade(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).alpha(main.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).depth(depth.getValue()).blend().texture());
            RenderUtil.drawBox(new RenderBuilder().position(hole.getHole()).height(outlineHeight.getValue() - 1).length((hole.getType().equals(HoleManager.Type.DOUBLEBEDROCKX) || hole.getType().equals(HoleManager.Type.DOUBLEOBSIDIANX)) ? 1 : 0).width((hole.getType().equals(HoleManager.Type.DOUBLEBEDROCKZ) || hole.getType().equals(HoleManager.Type.DOUBLEOBSIDIANZ)) ? 1 : 0).color(color).box(outline.getValue()).setup().line((float) ((double) outlineWidth.getValue())).cull(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).shade(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).alpha(outline.getValue().equals(Box.GLOW) || main.getValue().equals(Box.REVERSE)).depth(depth.getValue()).blend().texture());
        }
    }

    public static void addHole(HoleManager.Hole newHole, Color color) {
        boolean unique = true;
        for (Map.Entry<HoleManager.Hole, Color> holeEntry : holes.entrySet()) {
            if (newHole.getHole().equals(holeEntry.getKey().getHole())) {
                unique = false;
                break;
            }
        }

        if (unique) {
            holes.put(newHole, color);
        }
    }

    @Override
    public void onThread() {
        if (holeTimer.passed(1000, Format.SYSTEM)) {
            for (Map.Entry<HoleManager.Hole, Color> holeEntry : holes.entrySet()) {
                if (mc.player.getDistanceSq(holeEntry.getKey().getHole()) >= Math.pow(range.getValue(), 2)) {
                    holes.remove(holeEntry.getKey());
                }

                if (!HoleUtil.isObsidianHole(holeEntry.getKey().getHole()) || !HoleUtil.isBedRockHole(holeEntry.getKey().getHole()) || !HoleUtil.isDoubleObsidianHoleX(holeEntry.getKey().getHole()) || !HoleUtil.isDoubleObsidianHoleZ(holeEntry.getKey().getHole()) || !HoleUtil.isDoubleBedrockHoleX(holeEntry.getKey().getHole()) || !HoleUtil.isDoubleBedrockHoleZ(holeEntry.getKey().getHole())) {
                    holes.remove(holeEntry.getKey());
                }
            }

            holeTimer.reset();
        }

        for (BlockPos potentialHole : BlockUtil.getSurroundingBlocks(mc.player, range.getValue(), false)) {
            if (HoleUtil.isVoidHole(potentialHole.down()) && voids.getValue()) {
                addHole(new HoleManager.Hole(potentialHole.down(), HoleManager.Type.VOID), voidColor.getValue());
                return;
            }

            if (HoleUtil.isBedRockHole(potentialHole)) {
                addHole(new HoleManager.Hole(potentialHole, HoleManager.Type.BEDROCK), bedrockColor.getValue());
            }

            else if (HoleUtil.isObsidianHole(potentialHole)) {
                addHole(new HoleManager.Hole(potentialHole, HoleManager.Type.OBSIDIAN), obsidianColor.getValue());
            }

            if (doubles.getValue()) {
                if (HoleUtil.isDoubleBedrockHoleX(potentialHole.west())) {
                    addHole(new HoleManager.Hole(potentialHole.west(), HoleManager.Type.DOUBLEBEDROCKX), bedrockColor.getValue());
                }

                else if (HoleUtil.isDoubleBedrockHoleZ(potentialHole.north())) {
                    addHole(new HoleManager.Hole(potentialHole.north(), HoleManager.Type.DOUBLEBEDROCKZ), bedrockColor.getValue());
                }

                else if (HoleUtil.isDoubleObsidianHoleX(potentialHole.west())) {
                    addHole(new HoleManager.Hole(potentialHole.west(), HoleManager.Type.DOUBLEOBSIDIANX), obsidianColor.getValue());
                }

                else if (HoleUtil.isDoubleObsidianHoleZ(potentialHole.north())) {
                    addHole(new HoleManager.Hole(potentialHole.north(), HoleManager.Type.DOUBLEOBSIDIANZ), obsidianColor.getValue());
                }
            }
        }
    }
}