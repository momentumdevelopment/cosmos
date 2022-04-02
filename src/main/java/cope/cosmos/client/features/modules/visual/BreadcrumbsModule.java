package cope.cosmos.client.features.modules.visual;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.string.ColorUtil;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Wolfsurge, linustouchtips
 * @since 01/04/22
 */
public class BreadcrumbsModule extends Module {
    public static BreadcrumbsModule INSTANCE;

    public BreadcrumbsModule() {
        super("Breadcrumbs", Category.VISUAL, "Draws a trail behind you");
        INSTANCE = this;
    }

    // **************************** general ****************************

    public static Setting<Boolean> infinite = new Setting<>("Infinite", false)
            .setDescription("Makes breadcrumbs last forever");

    public static Setting<Float> lifespan = new Setting<>("Lifespan", 1F, 2F, 10F, 0)
            .setDescription("The lifespan of the positions in seconds")
            .setVisible(() -> !infinite.getValue());

    public static Setting<Float> width = new Setting<>("Width", 0.1F, 3F, 5F, 1)
            .setDescription("The width of the lines");

    // List of positions
    private final Map<Vec3d, Long> positions = new ConcurrentHashMap<>();

    @Override
    public void onDisable() {

        // Clear positions
        positions.clear();
    }

    @Override
    public void onUpdate() {
        if (!nullCheck() || mc.player.ticksExisted <= 20) {
            // We may have just loaded into a world, so we need to clear the positions
            positions.clear();
            return;
        }

        // Add the player's position
        // We are adding the player's last position so it is just behind the player, and will not be obvious on the screen (especially when elytra flying)
        positions.put(new Vec3d(mc.player.lastTickPosX, mc.player.lastTickPosY, mc.player.lastTickPosZ), System.currentTimeMillis());

        // Remove positions that are too old
        positions.forEach((position, time) -> {

            // passed lifespan
            if (System.currentTimeMillis() - time >= lifespan.getValue() * 1000) {
                positions.remove(position);
            }
        });
    }

    @Override
    public void onRender3D() {
        glPushMatrix();
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);
        glEnable(GL_BLEND);
        glDisable(GL_DEPTH_TEST);
        glLineWidth(width.getValue());

        // disable render lighting
        mc.entityRenderer.disableLightmap();

        // Render positions
        positions.forEach((position, time) -> {

            glBegin(GL_LINE_STRIP);

            // Set line colour
            glColor4f(ColorUtil.getPrimaryColor().getRed() / 255F, ColorUtil.getPrimaryColor().getGreen() / 255F, ColorUtil.getPrimaryColor().getBlue() / 255F, MathHelper.clamp((System.currentTimeMillis() - time) / lifespan.getValue(), 0, 1));

            // draw line
            glVertex3d(position.x - mc.getRenderManager().viewerPosX, position.y - mc.getRenderManager().viewerPosY, position.z - mc.getRenderManager().viewerPosZ);

            // Reset colour
            glColor4d(1, 1, 1, 1);

            glEnd();
        });

        glEnable(GL_DEPTH_TEST);
        glDisable(GL_LINE_SMOOTH);
        glDisable(GL_BLEND);
        glEnable(GL_TEXTURE_2D);
        glPopMatrix();
    }
}