package cope.cosmos.client.features.modules.visual;

import cope.cosmos.asm.mixins.accessor.IRenderManager;
import cope.cosmos.client.events.render.entity.RenderNametagEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.SocialManager.Relationship;
import cope.cosmos.util.combat.EnemyUtil;
import cope.cosmos.util.entity.InterpolationUtil;
import cope.cosmos.util.math.MathUtil;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.string.ColorUtil;
import cope.cosmos.util.world.WorldUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author linustouchtips
 * @since 12/02/2021
 */
public class NametagsModule extends Module {
    public static NametagsModule INSTANCE;

    public NametagsModule() {
        super("Nametags", Category.VISUAL, "Renders a descriptive nametag on players");
        INSTANCE = this;
    }

    // **************************** general settings ****************************

    public static Setting<Boolean> health = new Setting<>("Health", true)
            .setDescription("Displays the player's health");

    public static Setting<Boolean> ping = new Setting<>("Ping", true)
            .setDescription("Displays the player's ping");

    public static Setting<Boolean> gamemode = new Setting<>("Gamemode", false)
            .setDescription("Displays the player's gamemode");

    public static Setting<Boolean> entityID = new Setting<>("EntityID", false)
            .setDescription("Displays the player's entity ID");

    public static Setting<Boolean> totemPops = new Setting<>("TotemPops", true)
            .setAlias("Pops")
            .setDescription("Displays the number of totems that the player popped");

    public static Setting<Boolean> armor = new Setting<>("Armor", true)
            .setDescription("Displays the player's armor");

    public static Setting<Boolean> enchantments = new Setting<>("Enchantments", true)
            .setAlias("Enchants", "Enchant")
            .setDescription("Displays the player's item enchantments");

    public static Setting<Boolean> simpleEnchantments = new Setting<>("SimpleEnchantments", false)
            .setAlias("SimpleEnchants", "SimpleEnchant")
            .setDescription("Simplify enchantment display")
            .setVisible(() -> enchantments.getValue());

    public static Setting<Boolean> durability = new Setting<>("Durability", true)
            .setAlias("Dura")
            .setDescription("Displays the player's item durability");

    public static Setting<Boolean> mainhand = new Setting<>("Mainhand", true)
            .setDescription("Displays the player's mainhand item");

    public static Setting<Boolean> offhand = new Setting<>("Offhand", true)
            .setDescription("Displays the player's offhand item");

    public static Setting<Boolean> itemName = new Setting<>("ItemName", false)
            .setDescription("Displays the player's mainhand item's name");

    // **************************** render settings ****************************

    public static Setting<Boolean> background = new Setting<>("Background", true)
            .setDescription("Displays a background behind the nametags");

    public static Setting<Boolean> outline = new Setting<>("Outline", false)
            .setDescription("Outlines the background");

    public static Setting<Double> scale = new Setting<>("Scale", 0.001, 0.003, 0.01, 3)
            .setDescription("The scaling of the nametag");

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

        // make sure the render engine exists
        if (mc.renderEngine != null && mc.getRenderManager().options != null) {

            // make sure the view entity exists
            if (mc.getRenderViewEntity() != null) {

                // interpolate the player's position
                Vec3d playerInterpolatedPosition = InterpolationUtil.getInterpolatedPosition(mc.getRenderViewEntity(), mc.getRenderPartialTicks());

                // get our render offsets.
                double renderX = ((IRenderManager) mc.getRenderManager()).getRenderX();
                double renderY = ((IRenderManager) mc.getRenderManager()).getRenderY();
                double renderZ = ((IRenderManager) mc.getRenderManager()).getRenderZ();

                // render for each player info
                playerInfoMap.forEach((player, info) -> {

                    // interpolate the player's position. if we were to use static positions, the nametags above the player would jitter and would not look good.
                    Vec3d interpolatedPosition = InterpolationUtil.getInterpolatedPosition(player, mc.getRenderPartialTicks());

                    // width of the background
                    int width = FontUtil.getStringWidth(info);
                    float halfWidth = width / 2F;

                    // distance from local player
                    double x = (playerInterpolatedPosition.x - renderX) - (interpolatedPosition.x - renderX);
                    double y = (playerInterpolatedPosition.y - renderY) - (interpolatedPosition.y - renderY);
                    double z = (playerInterpolatedPosition.z - renderZ) - (interpolatedPosition.z - renderZ);
                    double distance = Math.sqrt(x * x + y * y + z * z);

                    // figure out the scaling from the player, scaling only starts when the local player is 8 blocks away from the nametag
                    double scaleDistance = Math.max(distance - 8, 0);
                    double scaling = 0.0245 + (scale.getValue() * scaleDistance);

                    // offset the background and text by player view
                    GlStateManager.pushMatrix();
                    RenderHelper.enableStandardItemLighting();
                    GlStateManager.enablePolygonOffset();
                    GlStateManager.doPolygonOffset(1, -1500000);
                    GlStateManager.disableLighting();
                    GlStateManager.translate(interpolatedPosition.x - renderX, ((interpolatedPosition.y + player.height) + (player.isSneaking() ? 0.22 : 0.25)) - renderY, interpolatedPosition.z - renderZ);
                    GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0, 1, 0);
                    GlStateManager.rotate(mc.getRenderManager().playerViewX, (mc.gameSettings.thirdPersonView == 2) ? -1 : 1, 0, 0);
                    GlStateManager.scale(-scaling, -scaling, scaling);
                    GlStateManager.disableDepth();
                    GlStateManager.enableBlend();
                    GlStateManager.enableBlend();

                    // draw the background
                    if (background.getValue()) {
                        RenderUtil.drawRect(-halfWidth - 1, -FontUtil.getFontHeight() - 2, width, FontUtil.getFontHeight() + 2, new Color(0, 4, 0, 92));
                    }

                    // draw the outline
                    if (outline.getValue()) {
                        RenderUtil.drawBorder(-halfWidth - 1, -FontUtil.getFontHeight() - 2, width, FontUtil.getFontHeight() + 2, ColorUtil.getPrimaryColor());
                    }

                    GlStateManager.disableBlend();

                    // player social relation
                    Relationship relationship = getCosmos().getSocialManager().getSocial(player.getName());

                    // color of the player info
                    Color infoColor = Color.WHITE;

                    // if the player is sneaking, highlight their name orange
                    if (player.isSneaking()) {
                        infoColor = new Color(255, 153, 0);
                    }

                    // if the player is invisible, highlight their name red
                    if (player.isInvisible()) {
                        infoColor = new Color(255, 37, 0);
                    }

                    // if the player is a friend, highlight their name aqua
                    if (relationship.equals(Relationship.FRIEND)) {
                        infoColor = new Color(102, 255, 255);
                    }

                    // highlight fake player
                    if (WorldUtil.isFakePlayer(player)) {
                        infoColor = new Color(239, 1, 71);
                    }

                    // draw the info
                    FontUtil.drawStringWithShadow(info, -halfWidth + 1, -FontUtil.getFontHeight(), infoColor.getRGB());

                    // item rendering
                    // float backgroundY = -FontUtil.getFontHeight() - (5 + );

                    // display items
                    List<ItemStack> displayItems = new CopyOnWriteArrayList<>();

                    // add player's offhand item to display items
                    if (!player.getHeldItemOffhand().isEmpty()) {
                        displayItems.add(player.getHeldItemOffhand());
                    }

                    // add all armor pieces to display items
                    player.getArmorInventoryList().forEach(armorStack -> {
                        if (!armorStack.isEmpty()) {
                            displayItems.add(armorStack);
                        }
                    });

                    // add player's mainhand item to display items
                    if (!player.getHeldItemMainhand().isEmpty()) {
                        displayItems.add(player.getHeldItemMainhand());
                    }

                    // reverse armor list, we want the order to be mainhand, helmet -> boots, offhand
                    Collections.reverse(displayItems);

                    // check each enchantment
                    enchantmentSize = 0;

                    // find max enchantments
                    if (enchantments.getValue()) {

                        // check each display item's enchantments
                        displayItems.forEach(itemStack -> {

                            // enchantment size
                            int size = EnchantmentHelper.getEnchantments(itemStack).size();

                            // number of enchants on the item
                            if (size > enchantmentSize) {
                                enchantmentSize = size;
                            }
                        });

                        // simple enchantments only have one "enchantment" tag
                        if (simpleEnchantments.getValue()) {
                            enchantmentSize = 4;
                        }
                    }

                    // clamp size
                    enchantmentSize = Math.max(enchantmentSize, 4);

                    // no items, lower
                    if ((!offhand.getValue() || player.getHeldItemOffhand().isEmpty()) && (!mainhand.getValue() || player.getHeldItemMainhand().isEmpty()) && (!armor.getValue() || !player.getArmorInventoryList().iterator().hasNext())) {
                        enchantmentSize = 0;
                    }

                    // render display items
                    itemOffset = -8 * displayItems.size();
                    displayItems.forEach(itemStack -> {

                        // check item
                        if (offhand.getValue() && itemStack.equals(player.getHeldItemOffhand()) || armor.getValue() && itemStack.getItem() instanceof ItemArmor || mainhand.getValue() && itemStack.equals(player.getHeldItemMainhand())) {

                            // begin render
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

                            // render item
                            mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, (int) itemOffset, (int) (-FontUtil.getFontHeight() + (enchantmentSize * -4.75F) + 1));
                            mc.getRenderItem().renderItemOverlays(mc.fontRenderer, itemStack, (int) itemOffset, (int) (-FontUtil.getFontHeight() + (enchantmentSize * -4.75F) + 1));

                            // reset item z
                            mc.getRenderItem().zLevel = 0;

                            // reset lighting
                            RenderHelper.disableStandardItemLighting();
                            GlStateManager.enableCull();
                            GlStateManager.enableAlpha();
                            GlStateManager.disableDepth();
                            GlStateManager.enableDepth();
                            GlStateManager.popMatrix();
                        }

                        // display durability of item
                        if (durability.getValue()) {

                            // only tools and armor have durability
                            if (itemStack.getItem().isDamageable()) {

                                // durability (percent) of the item
                                int durability = (int) ((itemStack.getMaxDamage() - itemStack.getItemDamage()) / ((float) itemStack.getMaxDamage()) * 100);

                                // should be above the point
                                enchantOffset = -FontUtil.getFontHeight();

                                // scale to 1/2
                                GlStateManager.pushMatrix();
                                GlStateManager.scale(0.5, 0.5, 0.5);

                                // rescale position
                                float xScaled = (itemOffset + 1) * 2;
                                float yScaled = (-FontUtil.getFontHeight() + (enchantmentSize * -4.75F)) * 2;

                                // draw durability
                                FontUtil.drawStringWithShadow(durability + "%", xScaled, yScaled + enchantOffset - (enchantmentSize == 0 ? 3 : 0), itemStack.getItem().getRGBDurabilityForDisplay(itemStack));

                                // update offset
                                enchantOffset += FontUtil.getFontHeight() - 0.5;

                                // reset scale
                                GlStateManager.scale(2, 2, 2);
                                GlStateManager.popMatrix();
                            }

                            else {
                                enchantOffset = 0;
                            }
                        }

                        else {
                            enchantOffset = 0;
                        }

                        // check item
                        if (offhand.getValue() && itemStack.equals(player.getHeldItemOffhand()) || armor.getValue() && itemStack.getItem() instanceof ItemArmor || mainhand.getValue() && itemStack.equals(player.getHeldItemMainhand())) {

                            // display enchantments
                            if (enchantments.getValue()) {

                                // simpler enchantment display that prioritizes readability
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
                                        float xScaled = (itemOffset + 1) * 2;
                                        float yScaled = (-FontUtil.getFontHeight() + (enchantmentSize * -4.75F)) * 2;

                                        // draw enchants
                                        FontUtil.drawStringWithShadow("Max", xScaled, yScaled, -1);

                                        GlStateManager.scale(2, 2, 2);
                                        GlStateManager.popMatrix();
                                    }
                                }

                                else {

                                    GlStateManager.pushMatrix();
                                    GlStateManager.scale(0.5, 0.5, 0.5);

                                    // mark as god apple
                                    if (itemStack.getItem() instanceof ItemAppleGold) {

                                        // god apple
                                        if (itemStack.hasEffect()) {

                                            // display info
                                            // rescale position
                                            float xScaled = (itemOffset - 1) * 2;
                                            float yScaled = (-FontUtil.getFontHeight() + (enchantmentSize * -4.75F) + 2) * 2;

                                            // draw enchants
                                            FontUtil.drawStringWithShadow("God", xScaled, yScaled + enchantOffset, new Color(195, 77, 65).getRGB());

                                            // update offset
                                            enchantOffset += FontUtil.getFontHeight() - 0.5;
                                        }
                                    }

                                    // check if the item is enchanted
                                    if (itemStack.isItemEnchanted()) {

                                        // draw each enchantment
                                        EnchantmentHelper.getEnchantments(itemStack).forEach((enchantment, integer) -> {

                                            // formatted enchantment name
                                            StringBuilder enchantFormat = new StringBuilder();

                                            // enchant name -> translated format
                                            int enchantmentLevel = EnchantmentHelper.getEnchantmentLevel(enchantment, itemStack);

                                            // curse of vanishing effect
                                            if (enchantment.getTranslatedName(enchantmentLevel).contains("Vanish")) {

                                                // format name
                                                enchantFormat.append("Van");
                                            }

                                            // curse of binding effect
                                            else if (enchantment.getTranslatedName(enchantmentLevel).contains("Bind")) {

                                                // format name
                                                enchantFormat.append("Bind");
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
                                                    if (enchantmentLevel > 99) {
                                                        enchantFormat.append("99+");
                                                    }

                                                    else {
                                                        enchantFormat.append(enchantmentLevel);
                                                    }
                                                }
                                            }

                                            // rescale position
                                            float xScaled = (itemOffset + 1) * 2;
                                            float yScaled = (-FontUtil.getFontHeight() + (enchantmentSize * -4.75F)) * 2;

                                            // draw enchants
                                            FontUtil.drawStringWithShadow(enchantFormat.toString(), xScaled, yScaled + enchantOffset, -1);

                                            // update offset
                                            enchantOffset += FontUtil.getFontHeight() - 0.5;
                                        });
                                    }

                                    GlStateManager.scale(2, 2, 2);
                                    GlStateManager.popMatrix();
                                }
                            }
                        }

                        itemOffset += 16;
                    });

                    if (itemName.getValue()) {

                        // make sure the player is holding something
                        if (!player.getHeldItemMainhand().isEmpty() && !player.getHeldItemMainhand().getItem().equals(Items.AIR)) {

                            // name of the item held in the mainhand
                            String itemName = player.getHeldItemMainhand().getDisplayName();

                            GlStateManager.pushMatrix();
                            GlStateManager.scale(0.5, 0.5, 0.5);

                            // width of the item name
                            float itemWidth = FontUtil.getStringWidth(itemName) * 0.5F;
                            float itemHalfWidth = itemWidth / 2F;

                            // scaled size
                            int scaledSize = enchantmentSize + 1;

                            // no armor
                            if (enchantmentSize == 0) {
                                scaledSize = 1;
                            }

                            // give space for dura
                            if (durability.getValue()) {
                                scaledSize += 1;
                            }

                            // rescale position
                            float xScaled = (-itemHalfWidth + 1) * 2;
                            float yScaled = (-FontUtil.getFontHeight() + (scaledSize * -4.75F) - (enchantmentSize == 0 ? 3 : 0)) * 2;

                            // draw item name
                            FontUtil.drawStringWithShadow(itemName, xScaled, yScaled, -1);

                            GlStateManager.scale(2, 2, 2);
                            GlStateManager.popMatrix();
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
    }

    /**
     * Searches the player info of all entities in the world
     * @return A map of all the player info of all entities in the world
     */
    public Map<EntityPlayer, String> searchPlayerInfo() {

        // map of all the player's info
        Map<EntityPlayer, String> searchedInfoMap = new ConcurrentHashMap<>();

        // make sure view entity exists
        if (mc.getRenderViewEntity() != null) {

            // search for all players
            mc.world.playerEntities.forEach(player -> {

                // make sure the player isn't the user
                if (!mc.getRenderViewEntity().equals(player)) {

                    // the player's info
                    StringBuilder playerInfo = new StringBuilder();

                    // make sure the player is not dead
                    if (!EnemyUtil.isDead(player) && mc.getConnection() != null) {

                        // add the player's name
                        playerInfo.append(player.getName()).append(" ");

                        // add the player's entity ID
                        if (entityID.getValue()) {

                            // entity ID
                            playerInfo.append("ID: ")
                                    .append(player.getEntityId())
                                    .append(" ");
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

                        // add the player's ping
                        if (ping.getValue()) {

                            // IDK WHY THIS WOULD BE NULL BUT IT IS
                            if (!WorldUtil.isFakePlayer(player)) {

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

                                playerInfo.append(responseTime).append("ms ");
                            }
                        }

                        // add the player's health
                        if (health.getValue()) {

                            // player health with absorption
                            float health = MathUtil.roundFloat(EnemyUtil.getHealth(player), 1);

                            // absorption health
                            TextFormatting color = TextFormatting.GREEN;

                            // decent health
                            if (health <= 18 && health > 16) {
                                color = TextFormatting.DARK_GREEN;
                            }

                            // decent health
                            else if (health <= 16 && health > 12) {
                                color = TextFormatting.YELLOW;
                            }

                            // low health
                            else if (health <= 12 && health > 8) {
                                color = TextFormatting.GOLD;
                            }

                            // danger health
                            else if (health <= 8 && health > 4) {
                                color = TextFormatting.RED;
                            }

                            else if (health <= 4) {
                                color = TextFormatting.DARK_RED;
                            }

                            playerInfo.append(color).append(health).append(" ");
                        }

                        // add the player's totem pops
                        if (totemPops.getValue()) {

                            // totem pops for the entity
                            int pops = getCosmos().getPopManager().getTotemPops(player);

                            // only render if player has popped at least once
                            if (pops > 0) {

                                // no pops
                                TextFormatting color = TextFormatting.GREEN;

                                if (pops > 1) {
                                    color = TextFormatting.DARK_GREEN;
                                }

                                // 4+ pops
                                if (pops > 2) {
                                    color = TextFormatting.YELLOW;
                                }

                                // 6+ pops
                                if (pops > 3) {
                                    color = TextFormatting.GOLD;
                                }

                                // 8+ pops ...
                                if (pops > 4) {
                                    color = TextFormatting.RED;
                                }

                                // 8+ pops ...
                                if (pops > 5) {
                                    color = TextFormatting.DARK_RED;
                                }

                                playerInfo.append(color).append("-").append(pops).append(" ");
                            }
                        }

                        // add it to the map of the player info
                        searchedInfoMap.put(player, playerInfo.toString());
                    }
                }
            });
        }

        // map of player info
        return searchedInfoMap;
    }

    @SubscribeEvent
    public void onRenderNametag(RenderNametagEvent event) {

        // cancel vanilla nametag rendering
        event.setCanceled(true);
    }
}