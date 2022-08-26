package cope.cosmos.client.features.modules.client;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.render.gui.RenderAdvancementEvent;
import cope.cosmos.client.events.render.gui.RenderPotionHUDEvent;
import cope.cosmos.client.events.render.other.CapeLocationEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.movement.SpeedModule;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.TickManager.TPS;
import cope.cosmos.util.math.MathUtil;
import cope.cosmos.util.player.MotionUtil;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.string.ColorUtil;
import cope.cosmos.util.string.StringFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

import java.util.Comparator;
import java.util.Objects;

/**
 * TODO: make this code clean
 * @author linustouchtips
 * @since 06/04/2021
 */
public class HUDModule extends Module {
    public static HUDModule INSTANCE;

    public HUDModule() {
        super("HUD", Category.CLIENT, "Displays the HUD");
        setDrawn(false);
        setExempt(true);
        INSTANCE = this;
    }

    // **************************** HUD ****************************

    public static Setting<Boolean> watermark = new Setting<>("Watermark", true)
            .setDescription("Displays a client watermark");

    public static Setting<Boolean> cape = new Setting<>("Cape", true)
            .setDescription("Displays the client capes");

    public static Setting<Boolean> activeModules = new Setting<>("ActiveModules", true)
            .setDescription("Displays all enabled modules");
    
    public static Setting<Rendering> rendering = new Setting<>("Rendering", Rendering.UP)
            .setDescription("Rendering position of the elements");

    public static Setting<Boolean> coordinates = new Setting<>("Coordinates", true)
            .setAlias("Coords")
            .setDescription("Displays the user's coordinates");

    public static Setting<Boolean> direction = new Setting<>("Direction", true)
            .setDescription("Displays the user's facing direction");

    public static Setting<Boolean> speed = new Setting<>("Speed", true)
            .setAlias("KHM", "BPS")
            .setDescription("Displays the user's speed");

    public static Setting<Boolean> ping = new Setting<>("Ping", true)
            .setDescription("Displays the user's server connection speed");

    public static Setting<Boolean> fps = new Setting<>("FPS", true)
            .setDescription("Displays the current FPS");

    public static Setting<Boolean> tps = new Setting<>("TPS", true)
            .setDescription("Displays the server TPS");

    public static Setting<Boolean> armor = new Setting<>("Armor", true)
            .setDescription("Displays the player's armor");

    public static Setting<Boolean> tabGUI = new Setting<>("TabGUI", false)
            .setDescription("Displays the client's TabGUI");

    // **************************** vanilla HUD ****************************

    public static Setting<Boolean> potionEffects = new Setting<>("PotionEffects", true)
            .setDescription("Displays the player's active potion effects");

    public static Setting<Boolean> potionHUD = new Setting<>("PotionHUD", false)
            .setDescription("Displays the vanilla potion effect hud");

    public static Setting<Boolean> advancements = new Setting<>("Advancements", false)
            .setDescription("Displays the vanilla advancement notification");


    // offsets
    private int globalOffset;
    private float listOffset;
    private int armorOffset;

    // bottom offsets
    private float bottomRight = 10;
    private float topRight = 10;
    private float bottomLeft = 10;

    // test for my two way animation manager, will put this into hud editor if it gets made

    @Override
    public void onRender2D() {

        // resolutions
        int SCREEN_WIDTH = new ScaledResolution(mc).getScaledWidth();
        int SCREEN_HEIGHT = new ScaledResolution(mc).getScaledHeight();

        // reset offsets
        globalOffset = 0;
        topRight = 0;
        bottomLeft = 10;

        if (rendering.getValue().equals(Rendering.UP)) {
            bottomRight = 10;
        }

        else {
            bottomRight = 0;
        }

        // offset chat box height
        if (mc.currentScreen instanceof GuiChat) {
            bottomLeft += 14;

            if (rendering.getValue().equals(Rendering.UP)) {
                bottomRight += 14;
            }

            else {
                topRight += 14;
            }
        }

        if (watermark.getValue()) {
            FontUtil.drawStringWithShadow(Cosmos.NAME + TextFormatting.WHITE + " " + Cosmos.VERSION, 2, 2, ColorUtil.getPrimaryColor(globalOffset).getRGB());
        }

        if (mc.currentScreen == null || mc.currentScreen instanceof GuiChat) {

            // arraylist
            if (activeModules.getValue()) {
                listOffset = 0;

                getCosmos().getModuleManager()
                        .getModules(module -> module.isDrawn())
                        .stream()
                        .filter(module -> module.getAnimation().getAnimationFactor() > 0.05)
                        .sorted(Comparator.comparing(module -> FontUtil.getStringWidth(module.getName() + (!module.getInfo().equals("") ? " [" + module.getInfo() + "]": "")) * -1))
                        .forEach(module -> {

                    // formatted string
                    StringBuilder moduleString = new StringBuilder(module.getName());

                    if (!module.getInfo().equals("")) {
                        moduleString.append(TextFormatting.GRAY).append(" [").append(TextFormatting.WHITE).append(module.getInfo()).append(TextFormatting.GRAY).append("]");
                    }

                    // draw string
                    FontUtil.drawStringWithShadow(moduleString.toString(), (float) (SCREEN_WIDTH - (((FontUtil.getStringWidth(moduleString.toString()) * module.getAnimation().getAnimationFactor()))) - 1), rendering.getValue().equals(Rendering.UP) ? 2 + listOffset : SCREEN_HEIGHT - 10 - listOffset - topRight, ColorUtil.getPrimaryColor(globalOffset).getRGB());

                    // offset
                    listOffset += (mc.fontRenderer.FONT_HEIGHT + 1) * module.getAnimation().getAnimationFactor();
                    globalOffset++;
                });
            }

            if (potionEffects.getValue()) {

                // active potions
                mc.player.getActivePotionEffects().forEach(potionEffect -> {

                    // if (animation.getAnimationFactor() > 0.05) {

                        // potion name
                        String potionName = I18n.format(potionEffect.getEffectName());

                        // potion effects associated with modules
                        if (!potionName.equals("FullBright") && !potionName.equals("SpeedMine")) {

                            // potion formatted
                            StringBuilder potionFormatted = new StringBuilder(potionName);

                            // potion formatted
                            potionFormatted.append(" ")
                                    .append(potionEffect.getAmplifier() + 1)
                                    .append(ChatFormatting.WHITE)
                                    .append(" ")
                                    .append(Potion.getPotionDurationString(potionEffect, 1F));

                            // draw string
                            FontUtil.drawStringWithShadow(potionFormatted.toString(), (float) (SCREEN_WIDTH - (FontUtil.getStringWidth(potionFormatted.toString()) + 2)), rendering.getValue().equals(Rendering.UP) ? SCREEN_HEIGHT - bottomRight : 2 + bottomRight, potionEffect.getPotion().getLiquidColor());

                            // offset
                            bottomRight += FontUtil.getFontHeight() + 1;
                            globalOffset++;
                        }
                   //  }
                });
            }

            if (speed.getValue()) {

                // distance travelled
                double distanceX = mc.player.posX - mc.player.prevPosX;
                double distanceZ = mc.player.posZ - mc.player.prevPosZ;

                // speed in kmh
                float speed = MathUtil.roundFloat(((MathHelper.sqrt(StrictMath.pow(distanceX, 2) + StrictMath.pow(distanceZ, 2)) / 1000) / (0.05F / 3600) * (50 / getCosmos().getTickManager().getTickLength())), 1);

                // FUTURE CLIENT :tm: moment
                if (MotionUtil.isMoving() && SpeedModule.INSTANCE.isEnabled()) {
                    speed += 2;
                }

                // formatted string
                String speedString = "Speed " + TextFormatting.WHITE + speed + " kmh";

                // draw string
                FontUtil.drawStringWithShadow(speedString, SCREEN_WIDTH - FontUtil.getStringWidth(speedString) - 2, rendering.getValue().equals(Rendering.UP) ? SCREEN_HEIGHT - bottomRight : 2 + bottomRight, ColorUtil.getPrimaryColor(globalOffset).getRGB());

                // offset
                bottomRight += FontUtil.getFontHeight() + 1;
                globalOffset++;
            }

            if (ping.getValue()) {
                String pingDisplay = "Ping " + TextFormatting.WHITE + (!mc.isSingleplayer() ? Objects.requireNonNull(mc.getConnection()).getPlayerInfo(mc.player.getUniqueID()).getResponseTime() : 0) + "ms";
                FontUtil.drawStringWithShadow(pingDisplay, SCREEN_WIDTH - FontUtil.getStringWidth(pingDisplay) - 2, rendering.getValue().equals(Rendering.UP) ? SCREEN_HEIGHT - bottomRight : 2 + bottomRight, ColorUtil.getPrimaryColor(globalOffset).getRGB());

                // offset
                bottomRight += FontUtil.getFontHeight() + 1;
                globalOffset++;
            }

            if (tps.getValue()) {
                String tpsDisplay = "TPS " + TextFormatting.WHITE + Cosmos.INSTANCE.getTickManager().getTPS(TPS.AVERAGE);
                FontUtil.drawStringWithShadow(tpsDisplay, SCREEN_WIDTH - FontUtil.getStringWidth(tpsDisplay) - 2, rendering.getValue().equals(Rendering.UP) ? SCREEN_HEIGHT - bottomRight : 2 + bottomRight, ColorUtil.getPrimaryColor(globalOffset).getRGB());

                // offset
                bottomRight += FontUtil.getFontHeight() + 1;
                globalOffset++;
            }

            if (fps.getValue()) {
                String tpsDisplay = "FPS " + TextFormatting.WHITE + Minecraft.getDebugFPS();
                FontUtil.drawStringWithShadow(tpsDisplay, SCREEN_WIDTH - FontUtil.getStringWidth(tpsDisplay) - 2, rendering.getValue().equals(Rendering.UP) ? SCREEN_HEIGHT - bottomRight : 2 + bottomRight, ColorUtil.getPrimaryColor(globalOffset).getRGB());

                // offset
                bottomRight += FontUtil.getFontHeight() + 1;
                globalOffset++;
            }

            if (coordinates.getValue()) {

                // string for the coords
                StringBuilder coordinateString = new StringBuilder();

                // checks if the player is in the nether
                boolean inNether = (mc.world.getBiome(mc.player.getPosition()).getBiomeName().equalsIgnoreCase("Hell"));

                // format
                coordinateString.append("XYZ (")
                        .append(TextFormatting.WHITE)
                        .append(MathUtil.roundFloat(mc.player.posX, 1)) // overworld
                        .append(", ")
                        .append(MathUtil.roundFloat(mc.player.posY, 1))
                        .append(", ")
                        .append(MathUtil.roundFloat(mc.player.posZ, 1))
                        .append(TextFormatting.RESET)
                        .append(") [")
                        .append(TextFormatting.WHITE)
                        .append(MathUtil.roundFloat(inNether ? mc.player.posX * 8 : mc.player.posX / 8, 1)) // nether
                        .append(", ")
                        .append(MathUtil.roundFloat(mc.player.posY, 1))
                        .append(", ")
                        .append(MathUtil.roundFloat(inNether ? mc.player.posZ * 8 : mc.player.posZ / 8, 1))
                        .append(TextFormatting.RESET)
                        .append("]");

                FontUtil.drawStringWithShadow(coordinateString.toString(), 2, SCREEN_HEIGHT - bottomLeft, ColorUtil.getPrimaryColor(globalOffset).getRGB());
                bottomLeft += FontUtil.getFontHeight() + 1;
                globalOffset++;
            }

            if (direction.getValue()) {

                // facing stuff
                EnumFacing direction = mc.player.getHorizontalFacing();
                EnumFacing.AxisDirection axisDirection = direction.getAxisDirection();

                FontUtil.drawStringWithShadow( StringFormatter.capitalise(direction.getName()) + " (" + TextFormatting.WHITE + StringFormatter.formatEnum(direction.getAxis()) + (axisDirection.equals(EnumFacing.AxisDirection.POSITIVE) ? "+" : "-") + TextFormatting.RESET + ")", 2, SCREEN_HEIGHT - bottomLeft, ColorUtil.getPrimaryColor(globalOffset).getRGB());
                bottomLeft += FontUtil.getFontHeight() + 1;
                globalOffset++;
            }

            if (armor.getValue()) {
                armorOffset = 0;
                mc.player.inventory.armorInventory.forEach(itemStack -> {
                    if (!itemStack.isEmpty()) {

                        // y offset
                        int yScaled = 0;

                        // if we're in the water, then the armor should render above the bubbles
                        if (mc.player.isInWater()) {
                            yScaled = 10;
                        }

                        // if we're in creative, we have no hunger bar, so we should render lower
                        if (mc.player.capabilities.isCreativeMode) {
                            yScaled = -15;
                        }

                        // draw armor item
                        GlStateManager.pushMatrix();
                        RenderHelper.enableGUIStandardItemLighting();

                        mc.getRenderItem().zLevel = 200;
                        mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, (SCREEN_WIDTH / 2) + ((9 - armorOffset) * 16) - 78, SCREEN_HEIGHT - yScaled - 55);
                        mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, (SCREEN_WIDTH / 2) + ((9 - armorOffset) * 16) - 78, SCREEN_HEIGHT - yScaled - 55, "");
                        mc.getRenderItem().zLevel = 0;

                        RenderHelper.disableStandardItemLighting();
                        GlStateManager.popMatrix();
                    }

                    armorOffset++;
                });
            }
        }

        if (tabGUI.getValue()) {
            getCosmos().getTabGUI().render();
        }
    }

    @SubscribeEvent
    public void onCapeLocationEvent(CapeLocationEvent event) {

        // render capes
        if (cape.getValue()) {

            // Check the player is caped
            if (getCosmos().getCapeManager().getCapedPlayers().containsKey(event.getPlayerName())) {

                // Overwrite the cape location
                event.setLocation(getCosmos().getCapeManager().getCapedPlayers().get(event.getPlayerName()).getPath());
            }
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (mc.currentScreen == null) {

            // tab gui event
            if (tabGUI.getValue()) {
                getCosmos().getTabGUI().onKeyPress(event);
            }
        }
    }

    @SubscribeEvent
    public void onRenderPotionHUD(RenderPotionHUDEvent event) {
        if (!potionHUD.getValue()) {

            // cancel vanilla potion hud from rendering
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderAdvancement(RenderAdvancementEvent event) {
        if (!advancements.getValue()) {

            // cancel vanilla advancement notification from rendering
            event.setCanceled(true);
        }
    }

    public enum Rendering {

        /**
         * Renders the arraylist at the top and the info at the bottom
         */
        UP,

        /**
         * Renders the arraylist at the bottom and the info at the top
         */
        DOWN
    }
}