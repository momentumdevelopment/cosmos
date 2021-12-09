package cope.cosmos.client.features.modules.visual;

import cope.cosmos.asm.mixins.accessor.IRenderManager;
import cope.cosmos.client.events.RenderNametagEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.SocialManager.*;
import cope.cosmos.event.annotation.Subscription;
import cope.cosmos.util.combat.EnemyUtil;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.system.MathUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.Color;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public class Nametags extends Module {
    public static Nametags INSTANCE;

    public Nametags() {
        super("Nametags", Category.VISUAL, "Renders a descriptive nametag on players");
        INSTANCE = this;
    }

    public static Setting<Boolean> health = new Setting<>("Health", true).setDescription("Displays the player's health");
    public static Setting<Boolean> ping = new Setting<>("Ping", true).setDescription("Displays the player's ping");
    public static Setting<Boolean> gamemode = new Setting<>("Gamemode", false).setDescription("Displays the player's gamemode");
    public static Setting<Boolean> totemPops = new Setting<>("TotemPops", true).setDescription("Displays the number of totems that the player popped");

    public static Setting<Boolean> background = new Setting<>("Background", true).setDescription("Displays a background behind the nametags");

    private Map<EntityPlayer, String> playerInfoMap = new ConcurrentHashMap<>();

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
                    // width of the background
                    int width = FontUtil.getStringWidth(info);
                    float halfWidth = width / 2F;

                    // offset the background and text by player view
                    GlStateManager.pushMatrix();
                    RenderHelper.enableStandardItemLighting();
                    GlStateManager.enablePolygonOffset();
                    GlStateManager.doPolygonOffset(1, -1500000);
                    GlStateManager.disableLighting();
                    GlStateManager.translate(player.posX - ((IRenderManager) mc.getRenderManager()).getRenderX(), (player.posY + player.height) - ((IRenderManager) mc.getRenderManager()).getRenderY() + (player.isSneaking() ? 0 : 0.08) + 2.05, player.posZ - ((IRenderManager) mc.getRenderManager()).getRenderZ());
                    GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0, 1, 0);
                    GlStateManager.rotate(mc.getRenderManager().playerViewX, (mc.gameSettings.thirdPersonView == 2) ? -1 : 1, 0, 0);
                    // GlStateManager.scale(-0.02, -0.02, 0.02);
                    GlStateManager.disableDepth();
                    GlStateManager.enableBlend();

                    if (background.getValue()) {
                        GlStateManager.enableBlend();

                        // draw the background
                        RenderUtil.drawRect(-halfWidth - 1, -FontUtil.getFontHeight() + 1, width + 3, FontUtil.getFontHeight() + 2, new Color(0, 0, 0, 100));

                        GlStateManager.disableBlend();
                    }

                    // draw the info
                    FontUtil.drawStringWithShadow(info, -halfWidth + 1, -FontUtil.getFontHeight() + 3, -1);

                    // reset the background and text by player view
                    GlStateManager.enableDepth();
                    GlStateManager.disableBlend();
                    GlStateManager.disablePolygonOffset();
                    GlStateManager.doPolygonOffset(1, 1500000);
                    GlStateManager.popMatrix();
                }
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
                    // add the player's gamemode

                    if (gamemode.getValue()) {
                        // first letter of gamemode
                        if (player.isCreative()) {
                            playerInfo.append("[C] ").append(TextFormatting.RESET);
                        }

                        else if (player.isInvisible() || player.isSpectator()) {
                            playerInfo.append("[I] ").append(TextFormatting.RESET);
                        }

                        else {
                            playerInfo.append("[S] ").append(TextFormatting.RESET);
                        }
                    }

                    // if the player is sneaking, highlight their name orange
                    if (player.isSneaking()) {
                        playerInfo.append(TextFormatting.GOLD);
                    }

                    // if the player is a friend, highlight their name aqua
                    if (getCosmos().getSocialManager().getSocial(player.getName()).equals(Relationship.FRIEND)) {
                        playerInfo.append(TextFormatting.AQUA);
                    }

                    // add the player's name
                    playerInfo.append(player.getName()).append(" ").append(TextFormatting.RESET);

                    // add the player's health
                    if (health.getValue()) {
                        playerInfo.append(MathUtil.roundDouble(EnemyUtil.getHealth(player), 1)).append(" ").append(TextFormatting.RESET);
                    }

                    // add the player's ping
                    if (ping.getValue() && mc.getConnection() != null) {
                        playerInfo.append(mc.getConnection().getPlayerInfo(player.getUniqueID()).getResponseTime()).append("ms ").append(TextFormatting.RESET);
                    }

                    // add the player's totem pops
                    if (totemPops.getValue()) {
                        playerInfo.append(getCosmos().getPopManager().getTotemPops(player)).append(" ").append(TextFormatting.RESET);
                    }

                    // add it to the map of the player info
                    searchedInfoMap.put(player, playerInfo.toString());
                }
            }
        });

        return searchedInfoMap;
    }

    @Subscription
    public void onRenderNametag(RenderNametagEvent event) {
        // cancel vanilla nametag rendering
        event.setCanceled(true);
    }
}
