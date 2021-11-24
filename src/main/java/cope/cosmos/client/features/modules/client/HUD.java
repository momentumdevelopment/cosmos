package cope.cosmos.client.features.modules.client;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.ModuleManager;
import cope.cosmos.client.manager.managers.TickManager.TPS;
import cope.cosmos.util.client.ColorUtil;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.system.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;

import java.awt.*;
import java.util.Comparator;
import java.util.Objects;

public class HUD extends Module {
    public static HUD INSTANCE;

    public HUD() {
        super("HUD", Category.CLIENT, "Displays the HUD");
        setDrawn(false);
        setExempt(true);
        INSTANCE = this;
    }

    public static Setting<Boolean> watermark = new Setting<>("Watermark", "Displays a client watermark", true);
    public static Setting<Boolean> activeModules = new Setting<>("ActiveModules", "Displays all enabled modules", true);
    public static Setting<Boolean> coordinates = new Setting<>("Coordinates", "Displays the user's coordinates", true);
    public static Setting<Boolean> speed = new Setting<>("Speed", "Displays the user's speed", true);
    public static Setting<Boolean> ping = new Setting<>("Ping", "Displays the user's server connection speed", true);
    public static Setting<Boolean> fps = new Setting<>("FPS", "Displays the current FPS", true);
    public static Setting<Boolean> tps = new Setting<>("TPS", "Displays the server TPS", true);
    public static Setting<Boolean> armor = new Setting<>("Armor", "Displays the player's armor", true);

    private float listOffset;
    private int armorOffset;

    // test for my two way animation manager, will put this into hud editor once it gets made

    @Override
    public void onRender2D() {
        int SCREEN_WIDTH = new ScaledResolution(mc).getScaledWidth();
        int SCREEN_HEIGHT = new ScaledResolution(mc).getScaledHeight();

        if (watermark.getValue()) {
            FontUtil.drawStringWithShadow(Cosmos.NAME + TextFormatting.WHITE + " " + Cosmos.VERSION, 2, 2, ColorUtil.getPrimaryColor().getRGB());
        }

        if (mc.currentScreen == null) {
            if (activeModules.getValue()) {
                listOffset = 0;

                ModuleManager.getAllModules().stream().filter(Module::isDrawn).filter(module -> module.getAnimation().getAnimationFactor() > 0.05).sorted(Comparator.comparing(module -> FontUtil.getStringWidth(module.getName() + (!module.getInfo().equals("") ? " " + module.getInfo() : "")) * -1)).forEach(module -> {
                    FontUtil.drawStringWithShadow(module.getName() + TextFormatting.WHITE + (!module.getInfo().equals("") ? " " + module.getInfo() : ""), (float) (new ScaledResolution(mc).getScaledWidth() - ((FontUtil.getStringWidth(module.getName() + (!module.getInfo().equals("") ? " " + module.getInfo() : "")) + 2) * MathHelper.clamp(module.getAnimation().getAnimationFactor(), 0, 1))), 2 + listOffset, ColorUtil.getPrimaryColor().getRGB());
                    listOffset += (mc.fontRenderer.FONT_HEIGHT + 1) * MathHelper.clamp(module.getAnimation().getAnimationFactor(), 0, 1);
                });
            }

            if (speed.getValue()) {
                double distanceX = mc.player.posX - mc.player.prevPosX;
                double distanceZ = mc.player.posZ - mc.player.prevPosZ;
                String speedDisplay = "Speed " + TextFormatting.WHITE + MathUtil.roundFloat((MathHelper.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceZ, 2)) / 1000) / (0.05F / 3600), 1) + " kmh";
                FontUtil.drawStringWithShadow(speedDisplay, SCREEN_WIDTH - FontUtil.getStringWidth(speedDisplay) - 2, SCREEN_HEIGHT - 10, ColorUtil.getPrimaryColor().getRGB());
            }

            if (ping.getValue()) {
                String pingDisplay = "Ping " + TextFormatting.WHITE + (!mc.isSingleplayer() ? Objects.requireNonNull(mc.getConnection()).getPlayerInfo(mc.player.getUniqueID()).getResponseTime() : 0) + "ms";
                FontUtil.drawStringWithShadow(pingDisplay, SCREEN_WIDTH - FontUtil.getStringWidth(pingDisplay) - 2, SCREEN_HEIGHT - mc.fontRenderer.FONT_HEIGHT - 11, ColorUtil.getPrimaryColor().getRGB());
            }

            if (tps.getValue()) {
                String tpsDisplay = "TPS " + TextFormatting.WHITE + Cosmos.INSTANCE.getTickManager().getTPS(TPS.AVERAGE);
                FontUtil.drawStringWithShadow(tpsDisplay, SCREEN_WIDTH - FontUtil.getStringWidth(tpsDisplay) - 2, SCREEN_HEIGHT - (2 * mc.fontRenderer.FONT_HEIGHT) - 12, ColorUtil.getPrimaryColor().getRGB());
            }

            if (fps.getValue()) {
                String tpsDisplay = "FPS " + TextFormatting.WHITE + Minecraft.getDebugFPS();
                FontUtil.drawStringWithShadow(tpsDisplay, SCREEN_WIDTH - FontUtil.getStringWidth(tpsDisplay) - 2, SCREEN_HEIGHT - (3 * mc.fontRenderer.FONT_HEIGHT) - 13, ColorUtil.getPrimaryColor().getRGB());
            }

            if (coordinates.getValue()) {
                String overWorldCoords = mc.player.dimension != -1 ? "XYZ " + TextFormatting.WHITE + MathUtil.roundFloat(mc.player.posX, 1) + " " + MathUtil.roundFloat(mc.player.posY, 1) + " " + MathUtil.roundFloat(mc.player.posZ, 1) : "XYZ " + TextFormatting.WHITE + MathUtil.roundFloat(mc.player.posX * 8, 1) + " " + MathUtil.roundFloat(mc.player.posY * 8, 1) + " " + MathUtil.roundFloat(mc.player.posZ * 8, 1);
                String netherCoords = mc.player.dimension == -1 ? "XYZ " + TextFormatting.WHITE + MathUtil.roundFloat(mc.player.posX, 1) + " " + MathUtil.roundFloat(mc.player.posY, 1) + " " + MathUtil.roundFloat(mc.player.posZ, 1) : TextFormatting.RED + "XYZ " + TextFormatting.WHITE + MathUtil.roundFloat(mc.player.posX / 8, 1) + " " + MathUtil.roundFloat(mc.player.posY / 8, 1) + " " + MathUtil.roundFloat(mc.player.posZ / 8, 1);

                FontUtil.drawStringWithShadow(overWorldCoords, 2, SCREEN_HEIGHT - 10, ColorUtil.getPrimaryColor().getRGB());
                FontUtil.drawStringWithShadow(netherCoords, 2, SCREEN_HEIGHT - mc.fontRenderer.FONT_HEIGHT - 11, new Color(255, 0, 0).getRGB());
            }

            if (armor.getValue()) {
                armorOffset = 0;
                mc.player.inventory.armorInventory.forEach(itemStack -> {
                    if (!itemStack.isEmpty()) {
                        GlStateManager.pushMatrix();
                        RenderHelper.enableGUIStandardItemLighting();

                        mc.getRenderItem().zLevel = 200;
                        mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, (SCREEN_WIDTH / 2) + ((9 - armorOffset) * 16) - 78, SCREEN_HEIGHT - (mc.player.isInWater() ? 10 : 0) - 55);
                        mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, (SCREEN_WIDTH / 2) + ((9 - armorOffset) * 16) - 78, SCREEN_HEIGHT - (mc.player.isInWater() ? 10 : 0) - 55, "");
                        mc.getRenderItem().zLevel = 0;

                        RenderHelper.disableStandardItemLighting();
                        GlStateManager.popMatrix();
                    }

                    armorOffset++;
                });
            }
        }
    }
}
