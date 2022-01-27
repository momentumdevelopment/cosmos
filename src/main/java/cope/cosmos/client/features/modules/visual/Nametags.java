package cope.cosmos.client.features.modules.visual;

import cope.cosmos.asm.mixins.accessor.IRenderManager;
import cope.cosmos.client.events.RenderNametagEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.SocialManager.Relationship;
import cope.cosmos.util.string.ColorUtil;
import cope.cosmos.util.combat.EnemyUtil;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.math.MathUtil;
import cope.cosmos.util.entity.InterpolationUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linustouchtips
 * @since 12/02/2021
 */
@SuppressWarnings("unused")
public class Nametags extends Module {
    public static Nametags INSTANCE;

    public Nametags() {
        super("Nametags", Category.VISUAL, "Renders a descriptive nametag on players");
        INSTANCE = this;
    }

    public static Setting<Boolean> health = new Setting<>("Health", true).setDescription("Displays the player's health");
    public static Setting<Boolean> healthBar = new Setting<>("HealthBar", false).setDescription("Visualizes the player's health").setParent(health);

    public static Setting<Boolean> ping = new Setting<>("Ping", true).setDescription("Displays the player's ping");
    public static Setting<Boolean> gamemode = new Setting<>("Gamemode", false).setDescription("Displays the player's gamemode");
    public static Setting<Boolean> totemPops = new Setting<>("TotemPops", true).setDescription("Displays the number of totems that the player popped");

    public static Setting<Boolean> armor = new Setting<>("Armor", true).setDescription("Displays the player's armor");

    public static Setting<Boolean> enchantments = new Setting<>("Enchantments", true).setDescription("Displays the player's item enchantments");
    public static Setting<Boolean> simpleEnchantments = new Setting<>("SimpleEnchantments", false).setDescription("Simplify enchantment display").setParent(enchantments);

    public static Setting<Boolean> durability = new Setting<>("Durability", true).setDescription("Displays the player's item durability");
    public static Setting<Boolean> mainhand = new Setting<>("Mainhand", true).setDescription("Displays the player's mainhand item");
    public static Setting<Boolean> offhand = new Setting<>("Offhand", true).setDescription("Displays the player's offhand item");
    public static Setting<Boolean> itemName = new Setting<>("ItemName", false).setDescription("Displays the player's mainhand item's name");

    public static Setting<Boolean> background = new Setting<>("Background", true).setDescription("Displays a background behind the nametags");
    public static Setting<Boolean> outline = new Setting<>("Outline", false).setDescription("Outlines the background");
    public static Setting<Double> scale = new Setting<>("Scale", 0.1, 0.2, 3.0, 1).setDescription("The scaling of the nametag");
    public static Setting<Boolean> distanceScale = new Setting<>("DistanceScale", false).setDescription("Scales the nametags size by the distance from the entity").setParent(scale);

    // map of all nametag info
    private Map<EntityPlayer, String> playerInfoMap = new ConcurrentHashMap<>();

    // offsets
    private float itemOffset;
    private float enchantOffset;

    // enchantment info
    private int enchantmentSize;
    private int maxEnchantment;

    @Override
    public void onUpdate() {
        // search the player info
        playerInfoMap = searchPlayerInfo();
    }

    @Override
    public void onRender3D() {
        if (mc.renderEngine != null && mc.getRenderManager().options != null) {
            playerInfoMap.forEach((player, info) -> {
                if (mc.getRenderViewEntity() != null) {
                    // get our render offsets.
                    double renderX = ((IRenderManager) mc.getRenderManager()).getRenderX();
                    double renderY = ((IRenderManager) mc.getRenderManager()).getRenderY();
                    double renderZ = ((IRenderManager) mc.getRenderManager()).getRenderZ();

                    // interpolate the player's position. if we were to use static positions, the nametags above the player would jitter and would not look good.
                    Vec3d interpolatedPosition = InterpolationUtil.getInterpolatedPosition(player, mc.getRenderPartialTicks());

                    // width of the background
                    int width = FontUtil.getStringWidth(info);
                    float halfWidth = width / 2F;

                    // get the distance from the current render view entity to the player we are rendering the nametag of.
                    double distance = mc.getRenderViewEntity().getDistance(interpolatedPosition.x, interpolatedPosition.y, interpolatedPosition.z);

                    // figure out the scaling from the player.
                    double scaling = scale.getValue() / 10;

                    // we have a static value because if we get too close to the player, the nametag will be so small you are unable to see it.
                    if (distanceScale.getValue()) {
                        scaling = Math.max(scale.getValue() * 5, scale.getValue() * distance) / 50;
                    }

                    // offset the background and text by player view
                    GlStateManager.pushMatrix();
                    RenderHelper.enableStandardItemLighting();
                    GlStateManager.enablePolygonOffset();
                    GlStateManager.doPolygonOffset(1, -1500000);
                    GlStateManager.disableLighting();
                    GlStateManager.translate(interpolatedPosition.x - renderX, ((interpolatedPosition.y + player.height) + (player.isSneaking() ? 0.05 : 0.08)) - renderY, interpolatedPosition.z - renderZ);
                    GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0, 1, 0);
                    GlStateManager.rotate(mc.getRenderManager().playerViewX, (mc.gameSettings.thirdPersonView == 2) ? -1 : 1, 0, 0);
                    GlStateManager.scale(-scaling, -scaling, scaling);
                    GlStateManager.disableDepth();
                    GlStateManager.enableBlend();

                    if (background.getValue() || healthBar.getValue()) {
                        GlStateManager.enableBlend();

                        if (background.getValue()) {
                            // draw the background
                            if (outline.getValue()) {
                                RenderUtil.drawBorderRect(-halfWidth - 1, -FontUtil.getFontHeight() - (7 + (healthBar.getValue() ? 2 : 0)), width, FontUtil.getFontHeight() + 2, new Color(0, 0, 0, 100), ColorUtil.getPrimaryColor());
                            }

                            else {
                                RenderUtil.drawRect(-halfWidth - 1, -FontUtil.getFontHeight() - (7 + (healthBar.getValue() ? 2 : 0)), width, FontUtil.getFontHeight() + 2, new Color(0, 0, 0, 100));
                            }
                        }

                        if (healthBar.getValue()) {

                            // player health with absorption
                            float health = MathUtil.roundFloat(EnemyUtil.getHealth(player), 1);

                            // absorption health
                            Color color = Color.GREEN;

                            // friend health :D
                            if (getCosmos().getSocialManager().getSocial(player.getName()).equals(Relationship.FRIEND)) {
                                color = Color.CYAN;
                            }

                            else {
                                // decent health
                                if (health <= 16 && health > 12) {
                                    color = Color.YELLOW;
                                }

                                // low health
                                else if (health <= 12 && health > 8) {
                                    color = Color.ORANGE;
                                }

                                // danger health
                                else if (health <= 8) {
                                    color = Color.RED;
                                }
                            }

                            // scale width by health
                            if (outline.getValue()) {
                                RenderUtil.drawRect(-halfWidth - 2, -7, (width + 1) * (health / 36), 2, color);
                            }

                            else {
                                RenderUtil.drawRect(-halfWidth - 1, -7, width * (health / 36), 2, color);
                            }
                        }

                        GlStateManager.disableBlend();
                    }

                    // draw the info
                    FontUtil.drawStringWithShadow(info, -halfWidth + 1, -FontUtil.getFontHeight() - (5 + (healthBar.getValue() ? 2 : 0)), -1);

                    // item rendering

                    // display items
                    List<ItemStack> displayItems = new ArrayList<>();

                    // add player's offhand item to display items
                    if (offhand.getValue()) {
                        if (!player.getHeldItemOffhand().isEmpty()) {
                            displayItems.add(player.getHeldItemOffhand());
                        }
                    }

                    // add all armor pieces to display items
                    if (armor.getValue()) {
                        player.getArmorInventoryList().forEach(armorStack -> {
                            if (!armorStack.isEmpty()) {
                                displayItems.add(armorStack);
                            }
                        });
                    }

                    // add player's mainhand item to display items
                    if (mainhand.getValue()) {
                        if (!player.getHeldItemMainhand().isEmpty()) {
                            displayItems.add(player.getHeldItemMainhand());
                        }
                    }

                    // reverse armor list, we want the order to be mainhand, helmet -> boots, offhand
                    Collections.reverse(displayItems);

                    // find max enchantments
                    if (enchantments.getValue()) {
                        enchantmentSize = 0;
                        displayItems.forEach(itemStack -> {
                            // number of enchants on the item
                            if (EnchantmentHelper.getEnchantments(itemStack).size() > enchantmentSize) {
                                enchantmentSize = EnchantmentHelper.getEnchantments(itemStack).size();
                            }
                        });
                    }

                    else {
                        enchantmentSize = 4;
                    }

                    if (simpleEnchantments.getValue()) {
                        enchantmentSize = 4;
                    }

                    // :D
                    if (enchantmentSize < 4) {
                        enchantmentSize = 4;
                    }

                    // render display items
                    itemOffset = -8 * displayItems.size();
                    displayItems.forEach(itemStack -> {

                        GlStateManager.pushMatrix();
                        GlStateManager.depthMask(true);
                        GlStateManager.clear(256);

                        // enable item lighting
                        RenderHelper.enableStandardItemLighting();

                        // set item z
                        mc.getRenderItem().zLevel = -150;

                        GlStateManager.disableAlpha();
                        GlStateManager.enableDepth();
                        GlStateManager.disableCull();

                        mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, (int) itemOffset, (enchantmentSize * -7) - 5);
                        mc.getRenderItem().renderItemOverlays(mc.fontRenderer, itemStack, (int) itemOffset, (enchantmentSize * -7) - 5);

                        // reset item z
                        mc.getRenderItem().zLevel = 0;

                        // reset lighting
                        RenderHelper.disableStandardItemLighting();

                        GlStateManager.enableCull();
                        GlStateManager.enableAlpha();
                        GlStateManager.disableDepth();
                        GlStateManager.enableDepth();
                        GlStateManager.popMatrix();

                        if (durability.getValue()) {

                            // only tools and armor have durability
                            if (itemStack.getItem() instanceof ItemArmor || itemStack.getItem() instanceof ItemPickaxe || itemStack.getItem() instanceof ItemSword || itemStack.getItem() instanceof ItemTool) {

                                // should be above the point
                                enchantOffset = -FontUtil.getFontHeight() - 1;

                                GlStateManager.pushMatrix();
                                GlStateManager.scale(0.5, 0.5, 0.5);

                                // durability (percent) of the item
                                int durability = (int) MathUtil.roundFloat(((itemStack.getMaxDamage() - itemStack.getItemDamage()) / ((float) itemStack.getMaxDamage())) * 100, 0);

                                // color for the durability
                                TextFormatting color = TextFormatting.GREEN;

                                // decent durability
                                if (durability <= 60 && durability > 40) {
                                    color = TextFormatting.YELLOW;
                                }

                                // bad durability
                                else if (durability <= 40 && durability > 20) {
                                    color = TextFormatting.GOLD;
                                }

                                // nearly broken durability
                                else if (durability <= 20) {
                                    color = TextFormatting.RED;
                                }

                                // rescale position
                                float xScaled = (itemOffset + 3) * 2;
                                float yScaled = ((enchantmentSize * -7) - 5) * 2;

                                // draw durability
                                FontUtil.drawStringWithShadow(String.valueOf(color) + durability + "%", xScaled, yScaled + enchantOffset, -1);

                                enchantOffset += FontUtil.getFontHeight() + 1;

                                GlStateManager.scale(2, 2, 2);
                                GlStateManager.popMatrix();
                            }
                        }

                        else {
                            enchantOffset = 0;
                        }

                        if (enchantments.getValue()) {
                            if (simpleEnchantments.getValue()) {

                                // search enchantment lvls
                                maxEnchantment = 0;
                                EnchantmentHelper.getEnchantments(itemStack).forEach((enchantment, integer) -> {
                                    int enchantmentLevel = EnchantmentHelper.getEnchantmentLevel(enchantment, itemStack);

                                    // find highest enchantment lvl
                                    if (enchantmentLevel > maxEnchantment) {
                                        maxEnchantment = enchantmentLevel;
                                    }
                                });

                                // reasonable to assume it's maxed
                                if (maxEnchantment >= 4) {
                                    GlStateManager.pushMatrix();
                                    GlStateManager.scale(0.5, 0.5, 0.5);

                                    // rescale position
                                    float xScaled = (itemOffset + 3) * 2;
                                    float yScaled = ((enchantmentSize * -7) - 5) * 2;

                                    // draw enchants
                                    FontUtil.drawStringWithShadow("Max", xScaled, yScaled, -1);

                                    GlStateManager.scale(2, 2, 2);
                                    GlStateManager.popMatrix();
                                }
                            }

                            else {
                                // draw each enchantment
                                EnchantmentHelper.getEnchantments(itemStack).forEach((enchantment, integer) -> {

                                    GlStateManager.pushMatrix();
                                    GlStateManager.scale(0.5, 0.5, 0.5);

                                    // formatted enchantment name
                                    StringBuilder enchantFormat = new StringBuilder();

                                    // mark as god apple
                                    if (itemStack.getItem() instanceof ItemAppleGold) {
                                        if (itemStack.hasEffect()) {
                                            // mark as enchanted
                                            // enchantFormat.append(TextFormatting.DARK_RED);

                                            // display info
                                            // enchantFormat.append("God");
                                        }
                                    }

                                    else if (itemStack.getItem() instanceof ItemArmor || itemStack.getItem() instanceof ItemPickaxe || itemStack.getItem() instanceof ItemSword || itemStack.getItem() instanceof ItemTool) {

                                        // enchant name -> translated format
                                        int enchantmentLevel = EnchantmentHelper.getEnchantmentLevel(enchantment, itemStack);

                                        // curse of vanishing effect
                                        if (enchantment.getTranslatedName(enchantmentLevel).contains("Vanish")) {
                                            // mark as bad enchantment
                                            enchantFormat.append(TextFormatting.DARK_RED);

                                            // format name
                                            enchantFormat.append("Van");
                                        }

                                        // curse of binding effect
                                        else if (enchantment.getTranslatedName(enchantmentLevel).contains("Bind")) {
                                            // mark as bad enchantment
                                            enchantFormat.append(TextFormatting.DARK_RED);

                                            // format name
                                            enchantFormat.append("Bind");
                                        }

                                        // sharpness effect & greater than lvl 100
                                        else if (enchantment.getTranslatedName(enchantmentLevel).contains("Sharp") && enchantmentLevel > 100) {
                                            // mark as 32k
                                            enchantFormat.append(TextFormatting.DARK_PURPLE);

                                            // format name
                                            enchantFormat.append("32k");
                                        }

                                        else {
                                            // enchant name formatted
                                            String formattedName = enchantment.getTranslatedName(enchantmentLevel);

                                            // cut off last letters
                                            int translatedScaled = (enchantmentLevel > 1) ? 2 : 3;
                                            if (formattedName.length() > translatedScaled) {
                                                formattedName = formattedName.substring(0, translatedScaled);
                                            }

                                            // enchant name + lvl
                                            enchantFormat.append(formattedName);

                                            // lvl must be greater than 1
                                            if (enchantmentLevel > 1) {
                                                // 32ks
                                                if (enchantmentLevel >= 100) {
                                                    enchantFormat.append("+");
                                                }

                                                else {
                                                    enchantFormat.append(enchantmentLevel);
                                                }
                                            }
                                        }
                                    }

                                    // rescale position
                                    float xScaled = (itemOffset + 3) * 2;
                                    float yScaled = ((enchantmentSize * -7) - 5) * 2;

                                    // draw enchants
                                    FontUtil.drawStringWithShadow(enchantFormat.toString(), xScaled, yScaled + enchantOffset, -1);

                                    enchantOffset += FontUtil.getFontHeight() + 1;

                                    GlStateManager.scale(2, 2, 2);
                                    GlStateManager.popMatrix();
                                });
                            }
                        }

                        itemOffset += 16;
                    });

                    if (itemName.getValue()) {

                        // make sure the player is holding something
                        if (!player.getHeldItemMainhand().isEmpty()) {

                            // name of the item held in the mainhand
                            String itemName = player.getHeldItemMainhand().getDisplayName();

                            GlStateManager.pushMatrix();
                            GlStateManager.scale(0.3, 0.3, 0.3);

                            // rescale position
                            float xScaled = ((-8 * displayItems.size()) + 16) * 3.3333334F;
                            float yScaled = ((enchantmentSize * -6) - FontUtil.getFontHeight() - 12) * 3.3333334F;

                            // draw item name
                            FontUtil.drawStringWithShadow(itemName, xScaled, yScaled, -1);

                            GlStateManager.scale(3.33334, 3.33334, 3.33334);
                            GlStateManager.popMatrix();
                        }
                    }
                }

                // reset the background and text by player view
                GlStateManager.enableDepth();
                GlStateManager.disableBlend();
                GlStateManager.disablePolygonOffset();
                GlStateManager.doPolygonOffset(1, 1500000);
                GlStateManager.popMatrix();
            });
        }
    }

    /**
     * Searches the player info of all entities in the world
     * @return A map of all the player info of all entities in the world
     */
    public Map<EntityPlayer, String> searchPlayerInfo() {
        // map of all the player's info
        Map<EntityPlayer, String> searchedInfoMap = new ConcurrentHashMap<>();

        mc.world.playerEntities.forEach(player -> {
            // make sure the player isn't the user
            if (!mc.player.equals(player)) {
                // the player's info
                StringBuilder playerInfo = new StringBuilder();

                // make sure the player is not dead
                if (!EnemyUtil.isDead(player)) {
                    // if the player is sneaking, highlight their name orange
                    if (player.isSneaking()) {
                        playerInfo.append(TextFormatting.GOLD);
                    }

                    // if the player is a friend, highlight their name aqua
                    if (getCosmos().getSocialManager().getSocial(player.getName()).equals(Relationship.FRIEND)) {
                        playerInfo.append(TextFormatting.AQUA);
                    }

                    // add the player's gamemode
                    if (gamemode.getValue()) {
                        // first letter of gamemode
                        if (player.isCreative()) {
                            playerInfo.append("[C] ");
                        }

                        else if (player.isInvisible() || player.isSpectator()) {
                            playerInfo.append("[I] ");
                        }

                        else {
                            playerInfo.append("[S] ");
                        }
                    }

                    // add the player's name
                    playerInfo.append(player.getName()).append(" ");

                    // add the player's ping
                    if (ping.getValue() && mc.getConnection() != null) {

                        if (mc.getConnection().getPlayerInfo(player.getUniqueID()) != null) {

                            // player -> server response time
                            int responseTime = mc.getConnection().getPlayerInfo(player.getUniqueID()).getResponseTime();

                            // godly ping
                            TextFormatting color = TextFormatting.GREEN;

                            // friend ping :D
                            if (getCosmos().getSocialManager().getSocial(player.getName()).equals(Relationship.FRIEND)) {
                                color = TextFormatting.AQUA;
                            }

                            else {
                                // decent ping
                                if (responseTime >= 70 && responseTime < 120) {
                                    color = TextFormatting.YELLOW;
                                }

                                // bad ping
                                else if (responseTime >= 120 && responseTime < 150) {
                                    color = TextFormatting.GOLD;
                                }

                                // awful ping
                                else if (responseTime >= 150) {
                                    color = TextFormatting.RED;
                                }
                            }

                            playerInfo.append(color).append(responseTime).append("ms ");
                        }
                    }

                    // add the player's health
                    if (health.getValue()) {

                        // player health with absorption
                        float health = MathUtil.roundFloat(EnemyUtil.getHealth(player), 1);

                        // absorption health
                        TextFormatting color = TextFormatting.GREEN;

                        // decent health
                        if (health <= 16 && health > 12) {
                            color = TextFormatting.YELLOW;
                        }

                        // low health
                        else if (health <= 12 && health > 8) {
                            color = TextFormatting.GOLD;
                        }

                        // danger health
                        else if (health <= 8) {
                            color = TextFormatting.RED;
                        }

                        playerInfo.append(color).append(health).append(" ");
                    }

                    // add the player's totem pops
                    if (totemPops.getValue()) {

                        // totem pops for the entity
                        int pops = getCosmos().getPopManager().getTotemPops(player);

                        if (pops > 0) {
                            // no pops
                            TextFormatting color = TextFormatting.GREEN;

                            // 4+ pops
                            if (pops <= 4) {
                                color = TextFormatting.YELLOW;
                            }

                            // 6+ pops
                            if (pops <= 7) {
                                color = TextFormatting.GOLD;
                            }

                            // 8+ pops ...
                            if (pops > 7) {
                                color = TextFormatting.RED;
                            }

                            playerInfo.append(color).append("-").append(pops).append(" ");
                        }
                    }

                    // add it to the map of the player info
                    searchedInfoMap.put(player, playerInfo.toString());
                }
            }
        });

        return searchedInfoMap;
    }

    @SubscribeEvent
    public void onRenderNametag(RenderNametagEvent event) {
        // cancel vanilla nametag rendering
        event.setCanceled(true);
    }
}