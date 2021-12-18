package cope.cosmos.client.features.modules.visual;

import cope.cosmos.asm.mixins.accessor.IRenderManager;
import cope.cosmos.client.events.RenderNametagEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.SocialManager.Relationship;
import cope.cosmos.util.combat.EnemyUtil;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.system.MathUtil;
import cope.cosmos.util.world.InterpolationUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
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
    public static Setting<Boolean> ping = new Setting<>("Ping", true).setDescription("Displays the player's ping");
    public static Setting<Boolean> gamemode = new Setting<>("Gamemode", false).setDescription("Displays the player's gamemode");
    public static Setting<Boolean> totemPops = new Setting<>("TotemPops", true).setDescription("Displays the number of totems that the player popped");

    public static Setting<Boolean> background = new Setting<>("Background", true).setDescription("Displays a background behind the nametags");
    public static Setting<Double> scale = new Setting<>("Scale", 0.1, 0.3, 3.0, 1).setDescription("The scaling of the nametag");

    // map of all nametag info
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
                    // get our render offsets.
                    double renderX = ((IRenderManager) mc.getRenderManager()).getRenderX();
                    double renderY = ((IRenderManager) mc.getRenderManager()).getRenderY();
                    double renderZ = ((IRenderManager) mc.getRenderManager()).getRenderZ();

                    // interpolate the player's position. if we were to use static positions, the nametags above the player would jitter and would not look good.
                    Vec3d pos = InterpolationUtil.getInterpolatedPos(player, mc.getRenderPartialTicks());

                    // width of the background
                    int width = FontUtil.getStringWidth(info);
                    float halfWidth = width / 2F;

                    // get the distance from the current render view entity to the player we are rendering the nametag of.
                    double distance = mc.getRenderViewEntity().getDistance(pos.x, pos.y, pos.z);

                    // figure out the scaling from the player.
                    // we have a static value because if we get too close to the player, the nametag will be so small you are unable to see it.
                    double scaling = Math.max(scale.getValue() * 3, scale.getValue() * distance) / 50;

                    // offset the background and text by player view
                    GlStateManager.pushMatrix();
                    RenderHelper.enableStandardItemLighting();
                    GlStateManager.enablePolygonOffset();
                    GlStateManager.doPolygonOffset(1, -1500000);
                    GlStateManager.disableLighting();
                    GlStateManager.translate(pos.x - renderX, ((pos.y + player.height) + (player.isSneaking() ? 0.05 : 0.08)) - renderY, pos.z - renderZ);
                    GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0, 1, 0);
                    GlStateManager.rotate(mc.getRenderManager().playerViewX, (mc.gameSettings.thirdPersonView == 2) ? -1 : 1, 0, 0);
                    GlStateManager.scale(-scaling, -scaling, scaling);
                    GlStateManager.disableDepth();
                    GlStateManager.enableBlend();

                    if (background.getValue()) {
                        GlStateManager.enableBlend();

                        // draw the background
                        RenderUtil.drawRect(-halfWidth - 1, -FontUtil.getFontHeight() + 1, width, FontUtil.getFontHeight() + 2, new Color(0, 0, 0, 100));

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

    @SubscribeEvent
    public void onRenderNametag(RenderNametagEvent event) {
        // cancel vanilla nametag rendering
        event.setCanceled(true);
    }
}
