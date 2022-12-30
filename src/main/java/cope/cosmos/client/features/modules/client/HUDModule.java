package cope.cosmos.client.features.modules.client;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.render.gui.RenderAdvancementEvent;
import cope.cosmos.client.events.render.gui.RenderPotionHUDEvent;
import cope.cosmos.client.events.render.other.CapeLocationEvent;
import cope.cosmos.client.features.Feature;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.movement.SpeedModule;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.TickManager.TPS;
import cope.cosmos.util.math.MathUtil;
import cope.cosmos.util.player.MotionUtil;
import cope.cosmos.util.player.PlayerUtil;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.string.ColorUtil;
import cope.cosmos.util.string.StringFormatter;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author linustouchtips
 * @since 06/04/2021
 */
public class HUDModule extends Module {
    public static HUDModule INSTANCE;

    public HUDModule() {
        super("HUD", Category.CLIENT, "Displays the HUD");
        INSTANCE = this;

        // client module
        setDrawn(false);
        setExempt(true);
    }

    // **************************** general ****************************

    public static Setting<Boolean> watermark = new Setting<>("Watermark", true)
            .setDescription("Displays a client watermark");

    public static Setting<Boolean> direction = new Setting<>("Direction", true)
            .setDescription("Displays the user's facing direction");

    public static Setting<Boolean> armor = new Setting<>("Armor", true)
            .setDescription("Displays the player's armor");

    public static Setting<Boolean> potionEffects = new Setting<>("PotionEffects", true)
            .setDescription("Displays the player's active potion effects");

    public static Setting<PotionHud> potionHUD = new Setting<>("PotionHUD", PotionHud.HIDE)
            .setAlias("EffectHUD")
            .setDescription("Displays the vanilla potion effect hud");

    public static Setting<Boolean> cape = new Setting<>("Capes", false)
            .setDescription("Displays the client capes");

    public static Setting<Boolean> serverBrand = new Setting<>("ServerBrand", false)
            .setDescription("Displays the server brand");

    public static Setting<Boolean> tps = new Setting<>("TPS", true)
            .setDescription("Displays the server TPS");

    public static Setting<Boolean> fps = new Setting<>("FPS", true)
            .setDescription("Displays the current FPS");

    public static Setting<Boolean> speed = new Setting<>("Speed", true)
            .setAlias("KHM", "BPS")
            .setDescription("Displays the user's speed");

    public static Setting<Boolean> ping = new Setting<>("Ping", true)
            .setDescription("Displays the user's server connection speed");

    public static Setting<Ordering> ordering = new Setting<>("Ordering", Ordering.LENGTH)
            .setDescription("Ordering of the arraylist");

    public static Setting<Boolean> coordinates = new Setting<>("Coordinates", true)
            .setAlias("Coords")
            .setDescription("Displays the user's coordinates");

    public static Setting<Boolean> netherCoordinates = new Setting<>("NetherCoordinates", true)
            .setAlias("NetherCoords")
            .setDescription("Displays the user's nether coordinates");

    public static Setting<Boolean> durability = new Setting<>("Durability", false)
            .setDescription("Displays the held item's durability");

    public static Setting<Boolean> activeModules = new Setting<>("ActiveModules", true)
            .setDescription("Displays all enabled modules");
    
    public static Setting<Rendering> rendering = new Setting<>("Rendering", Rendering.UP)
            .setDescription("Rendering position of the elements");

    // test for my two way animation manager, will put this into hud editor if it gets made

    // element height
    private static final float ELEMENT = FontUtil.getFontHeight() + 1;

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onRender2D() {

        // resolutions
        ScaledResolution resolution = new ScaledResolution(mc);
        int resWidth = resolution.getScaledWidth();
        int resHeight = resolution.getScaledHeight();

        // corners
        float topLeft = 2;
        float topRight = 2;
        float bottomLeft = resHeight - ELEMENT;
        float bottomRight = resHeight - ELEMENT;

        // effect hud is displayed
        if (potionHUD.getValue().equals(PotionHud.MOVE)) {

            // active potion effects
            Collection<PotionEffect> potionEffects = mc.player.getActivePotionEffects();

            // remove client potion effects
            potionEffects.removeIf(potionEffect -> potionEffect.getEffectName().equalsIgnoreCase("FullBright") || potionEffect.getEffectName().equalsIgnoreCase("SpeedMine"));

            // move top right corner to account for effect hud
            if (!potionEffects.isEmpty()) {
                topRight += 27;
            }
        }

        // chat gui is displayed
        if (mc.currentScreen instanceof GuiChat) {
            
            // offset to account for chat box height
            bottomLeft -= 14;
            bottomRight -= 14;
        }

        // display client name and version
        if (watermark.getValue()) {

            // build display string
            StringBuilder watermarkString = new StringBuilder()
                    .append(Cosmos.NAME)
                    .append(TextFormatting.WHITE)
                    .append(" ")
                    .append(Cosmos.VERSION);

            // draw string
            FontUtil.drawStringWithShadow(watermarkString.toString(), 2, topLeft, ColorUtil.getPrimaryColor().getRGB());

            // offset corner
            topLeft += ELEMENT;
        }

        // don't render while in gui's
        if (mc.currentScreen == null || mc.currentScreen instanceof GuiChat) {

            // arraylist
            if (activeModules.getValue()) {

                // sorted list of modules
                List<Module> sorted = getCosmos().getModuleManager().getAllModules();

                // sort
                switch (ordering.getValue()) {
                    case LENGTH:
                        sorted = sorted.stream().sorted(Comparator.comparing(module -> FontUtil.getStringWidth(module.getFormattedName()) * -1)).collect(Collectors.toList());
                        break;
                    case ALPHABETICAL:
                        sorted = sorted.stream().sorted(Comparator.comparing(Feature::getName)).collect(Collectors.toList());
                        break;
                }

                // render arraylist
                for (Module module : sorted) {

                    // ignore modules that aren't drawn to the arraylist
                    if (!module.isDrawn()) {
                        continue;
                    }

                    // check "enabled" state
                    if (module.getAnimation().getAnimationFactor() < 0.05) {
                        continue;
                    }

                    // width of the element
                    float width = (float) (FontUtil.getStringWidth(module.getFormattedName()) * module.getAnimation().getAnimationFactor());

                    // draw string
                    FontUtil.drawStringWithShadow(module.getFormattedName(), resWidth - 1 - width, rendering.getValue().equals(Rendering.UP) ? topRight : bottomRight, ColorUtil.getPrimaryColor().getRGB());

                    // height of the element
                    double height = ELEMENT * module.getAnimation().getAnimationFactor();

                    // offset top right
                    if (rendering.getValue().equals(Rendering.UP)) {
                        topRight += height;
                    }

                    // offset bottom right
                    else {
                        bottomRight -= height;
                    }
                }
            }

            // display active potion effects
            if (potionEffects.getValue()) {

                // active potions
                for (PotionEffect potionEffect : mc.player.getActivePotionEffects()) {

                    // check anim factor
                    // if (animation.getAnimationFactor() > 0.05) {

                    // potion name
                    String potionName = I18n.format(potionEffect.getEffectName());

                    // potion effects associated with modules
                    if (!potionName.equals("FullBright") && !potionName.equals("SpeedMine")) {

                        // potion formatted
                        StringBuilder potionString = new StringBuilder(potionName);

                        // potion effect amplifier
                        int amplifier = potionEffect.getAmplifier() + 1;

                        // potion formatted
                        potionString.append(" ")
                                .append(amplifier > 1 ? amplifier + " " : "")
                                .append(ChatFormatting.WHITE)
                                .append(Potion.getPotionDurationString(potionEffect, 1F));

                        // width of the element
                        float width = FontUtil.getStringWidth(potionString.toString());

                        // draw string
                        FontUtil.drawStringWithShadow(potionString.toString(), resWidth - 1 - width, rendering.getValue().equals(Rendering.UP) ? bottomRight : topRight, potionEffect.getPotion().getLiquidColor());

                        // offset bottom right
                        if (rendering.getValue().equals(Rendering.UP)) {
                            bottomRight -= ELEMENT;
                        }

                        // offset top right
                        else {
                            topRight += ELEMENT;
                        }
                    }
                    //  }
                }
            }

            // display server's "brand"
            if (serverBrand.getValue()) {

                // formatted string
                StringBuilder brandString = new StringBuilder(mc.player.getServerBrand());

                // width of the element
                float width = FontUtil.getStringWidth(brandString.toString());

                // draw string
                FontUtil.drawStringWithShadow(brandString.toString(), resWidth - 1 - width, rendering.getValue().equals(Rendering.UP) ? bottomRight : topRight, ColorUtil.getPrimaryColor().getRGB());

                // offset bottom right
                if (rendering.getValue().equals(Rendering.UP)) {
                    bottomRight -= ELEMENT;
                }

                // offset top right
                else {
                    topRight += ELEMENT;
                }
            }

            // display player movement speed
            if (speed.getValue()) {

                // distance travelled
                double x = mc.player.posX - mc.player.prevPosX;
                double z = mc.player.posZ - mc.player.prevPosZ;

                // speed in kmh
                float speed = MathUtil.roundFloat(((MathHelper.sqrt(x * x + z * z) / 1000) / (0.05F / 3600) * (50 / getCosmos().getTickManager().getTickLength())), 1);

                // FUTURE CLIENT :tm: moment
                if (MotionUtil.isMoving() && SpeedModule.INSTANCE.isEnabled()) {
                    speed += 2;
                }

                // formatted string
                StringBuilder speedString = new StringBuilder()
                        .append("Speed ")
                        .append(TextFormatting.WHITE)
                        .append(speed)
                        .append("k/mh");

                // width of the element
                float width = FontUtil.getStringWidth(speedString.toString());

                // draw string
                FontUtil.drawStringWithShadow(speedString.toString(), resWidth - 1 - width, rendering.getValue().equals(Rendering.UP) ? bottomRight : topRight, ColorUtil.getPrimaryColor().getRGB());

                // offset bottom right
                if (rendering.getValue().equals(Rendering.UP)) {
                    bottomRight -= ELEMENT;
                }

                // offset top right
                else {
                    topRight += ELEMENT;
                }
            }

            // display player's held item's durability
            if (durability.getValue()) {

                // current held item
                ItemStack held = mc.player.getHeldItemMainhand();

                // check if held item is valid
                if (!held.isEmpty() && held.getItem().isDamageable()) {

                    // current item durability
                    int durability = held.getMaxDamage() - held.getItemDamage();
                    
                    // scaled durability
                    float scaledDurability = (durability / (float) held.getMaxDamage()) * 100;

                    // damage color
                    TextFormatting damage = TextFormatting.GREEN;

                    // decent damage
                    if (scaledDurability <= 80 && scaledDurability > 60) {
                        damage = TextFormatting.DARK_GREEN;
                    }

                    // decent damage
                    else if (scaledDurability <= 60 && scaledDurability > 40) {
                        damage = TextFormatting.YELLOW;
                    }

                    // low damage
                    else if (scaledDurability <= 40 && scaledDurability > 20) {
                        damage = TextFormatting.GOLD;
                    }

                    // danger damage
                    else if (scaledDurability <= 20 && scaledDurability > 10) {
                        damage = TextFormatting.RED;
                    }

                    // broken damage
                    else if (scaledDurability <= 10) {
                        damage = TextFormatting.DARK_RED;
                    }

                    // formatted string
                    StringBuilder durabilityString = new StringBuilder()
                            .append("Durability ")
                            .append(damage)
                            .append(durability);

                    // width of the element
                    float width = FontUtil.getStringWidth(durabilityString.toString());

                    // draw string
                    FontUtil.drawStringWithShadow(durabilityString.toString(), resWidth - 1 - width, rendering.getValue().equals(Rendering.UP) ? bottomRight : topRight, ColorUtil.getPrimaryColor().getRGB());

                    // offset bottom right
                    if (rendering.getValue().equals(Rendering.UP)) {
                        bottomRight -= ELEMENT;
                    }

                    // offset top right
                    else {
                        topRight += ELEMENT;
                    }
                }
            }

            // display server response time
            if (ping.getValue()) {

                // server response time
                int responseTime = 0;

                // check if the player connection exists
                if (mc.getConnection() != null) {

                    // player -> server response time
                    NetworkPlayerInfo networkInfo = mc.getConnection().getPlayerInfo(mc.player.getUniqueID());

                    // network info is available
                    if (networkInfo != null) {

                        // update server response time
                        responseTime = networkInfo.getResponseTime();
                    }
                }

                // formatted string
                StringBuilder pingString = new StringBuilder()
                        .append("Ping ")
                        .append(TextFormatting.WHITE)
                        .append(responseTime)
                        .append("ms");

                // width of the element
                float width = FontUtil.getStringWidth(pingString.toString());
                
                // draw string
                FontUtil.drawStringWithShadow(pingString.toString(), resWidth - 1 - width, rendering.getValue().equals(Rendering.UP) ? bottomRight : topRight, ColorUtil.getPrimaryColor().getRGB());

                // offset bottom right
                if (rendering.getValue().equals(Rendering.UP)) {
                    bottomRight -= ELEMENT;
                }

                // offset top right
                else {
                    topRight += ELEMENT;
                }
            }

            // display the server TPS
            if (tps.getValue()) {

                // tps values
                float current = getCosmos().getTickManager().getTPS(TPS.CURRENT);
                float average = getCosmos().getTickManager().getTPS(TPS.AVERAGE);

                // build display string
                StringBuilder tpsString = new StringBuilder("TPS ")
                        .append(TextFormatting.WHITE)
                        .append(current)
                        .append(" ")
                        .append(TextFormatting.GRAY)
                        .append("[")
                        .append(TextFormatting.WHITE)
                        .append(average)
                        .append(TextFormatting.GRAY)
                        .append("]");
                
                // width of the element
                float width = FontUtil.getStringWidth(tpsString.toString());

                // draw to HUD
                FontUtil.drawStringWithShadow(tpsString.toString(), resWidth - 1 - width, rendering.getValue().equals(Rendering.UP) ? bottomRight : topRight, ColorUtil.getPrimaryColor().getRGB());

                // offset bottom right
                if (rendering.getValue().equals(Rendering.UP)) {
                    bottomRight -= ELEMENT;
                }

                // offset top right
                else {
                    topRight += ELEMENT;
                }
            }

            if (fps.getValue()) {
                
                // formatted string
                StringBuilder fpsString = new StringBuilder()
                        .append("FPS ")
                        .append(TextFormatting.WHITE)
                        .append(Minecraft.getDebugFPS());

                // width of the element
                float width = FontUtil.getStringWidth(fpsString.toString());

                // draw string
                FontUtil.drawStringWithShadow(fpsString.toString(), resWidth - 1 - width, rendering.getValue().equals(Rendering.UP) ? bottomRight : topRight, ColorUtil.getPrimaryColor().getRGB());

                // offset bottom right
                if (rendering.getValue().equals(Rendering.UP)) {
                    bottomRight -= ELEMENT;
                }

                // offset top right
                else {
                    topRight += ELEMENT;
                }
            }

            // display player coordinates in the world
            if (coordinates.getValue()) {

                // format coordinates
                StringBuilder coordinateString = new StringBuilder()
                        .append("XYZ (")
                        .append(TextFormatting.WHITE)
                        .append(MathUtil.roundFloat(mc.player.posX, 1)) // overworld
                        .append(", ")
                        .append(MathUtil.roundFloat(mc.player.posY, 1))
                        .append(", ")
                        .append(MathUtil.roundFloat(mc.player.posZ, 1))
                        .append(TextFormatting.RESET)
                        .append(")");
                
                // display players coordinates in the nether/overworld
                if (netherCoordinates.getValue()) {

                    // checks if the player is in the nether
                    boolean nether = mc.world.getBiome(PlayerUtil.getPosition()).getBiomeName().equalsIgnoreCase("Hell");

                    // format nether coordinates
                    coordinateString.append(" [")
                            .append(TextFormatting.WHITE)
                            .append(MathUtil.roundFloat(nether ? mc.player.posX * 8 : mc.player.posX / 8, 1)) // nether
                            .append(", ")
                            .append(MathUtil.roundFloat(nether ? mc.player.posZ * 8 : mc.player.posZ / 8, 1))
                            .append(TextFormatting.RESET)
                            .append("]");
                }

                // draw string
                FontUtil.drawStringWithShadow(coordinateString.toString(), 2, bottomLeft, ColorUtil.getPrimaryColor().getRGB());
                
                // offset bottom left
                bottomLeft -= ELEMENT;
            }

            // display player's axis direction
            if (direction.getValue()) {

                // facing stuff
                EnumFacing direction = mc.player.getHorizontalFacing();
                EnumFacing.AxisDirection axisDirection = direction.getAxisDirection();
                
                // formatted string
                StringBuilder directionString = new StringBuilder()
                        .append(StringFormatter.capitalise(direction.getName()))
                        .append(" (")
                        .append(TextFormatting.WHITE)
                        .append(StringFormatter.formatEnum(direction.getAxis()))
                        .append((axisDirection.equals(EnumFacing.AxisDirection.POSITIVE) ? "+" : "-"))
                        .append(TextFormatting.RESET)
                        .append(")");

                // draw string
                FontUtil.drawStringWithShadow( directionString.toString(), 2, bottomLeft, ColorUtil.getPrimaryColor().getRGB());
                
                // offset bottom left
                bottomLeft -= ELEMENT;
            }

            // display player's equipped armor
            if (armor.getValue()) {
                
                // armor render offset
                int armorOffset = 0;
                
                // display each armor piece
                for (ItemStack armor : mc.player.inventory.armorInventory) {

                    // check if armor exists
                    if (!armor.isEmpty()) {

                        // y offset
                        int y = 0;

                        // if we're in the water, then the armor should render above the bubbles
                        if (mc.player.isInsideOfMaterial(Material.WATER)) {
                            y = 10;
                        }

                        // if we're in creative, we have no hunger bar, so we should render lower
                        if (mc.player.capabilities.isCreativeMode) {
                            y = -15;
                        }

                        // start
                        GlStateManager.pushMatrix();
                        RenderHelper.enableGUIStandardItemLighting();

                        // render item
                        mc.getRenderItem().zLevel = 200;
                        mc.getRenderItem().renderItemAndEffectIntoGUI(armor, (resWidth / 2) + ((9 - armorOffset) * 16) - 78, resHeight - y - 55);
                        mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, armor, (resWidth / 2) + ((9 - armorOffset) * 16) - 78, resHeight - y - 55, "");
                        mc.getRenderItem().zLevel = 0;

                        // reset
                        RenderHelper.disableStandardItemLighting();
                        GlStateManager.popMatrix();
                    }

                    // update armor offset
                    armorOffset++;
                }
            }
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
    public void onRenderPotionHUD(RenderPotionHUDEvent event) {

        // hide effect HUD
        if (potionHUD.getValue().equals(PotionHud.HIDE)) {

            // cancel vanilla potion hud from rendering
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderAdvancement(RenderAdvancementEvent event) {

        // cancel vanilla advancement notification from rendering
        event.setCanceled(true);
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

    public enum Ordering {

        /**
         * Arraylist is ordered by length
         */
        LENGTH,

        /**
         * Arraylist is ordered by alphabet
         */
        ALPHABETICAL
    }

    public enum PotionHud {

        /**
         * Hides the effect HUD
         */
        HIDE,

        /**
         * Moves the hud elements around the effect HUD
         */
        MOVE,

        /**
         * Keeps the effect HUD
         */
        KEEP
    }
}