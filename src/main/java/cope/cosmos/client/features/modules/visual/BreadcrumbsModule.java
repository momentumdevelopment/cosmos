package cope.cosmos.client.features.modules.visual;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.string.ColorUtil;
import net.minecraft.util.math.Vec3d;
import java.util.LinkedList;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Surge, linustouchtips
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

    public static Setting<Float> lifespan = new Setting<>("Lifespan", 1F, 2F, 10F, 1)
            .setAlias("Delay")
            .setDescription("The lifespan of the positions in seconds")
            .setVisible(() -> !infinite.getValue());

    public static Setting<Float> width = new Setting<>("Width", 0.1F, 2F, 5F, 1)
            .setAlias("LineWidth")
            .setDescription("The width of the lines");

    // List of positions
    // Would prefer to use a map, but ConcurrentHashMap does some weird shit when rendering the line, LinkedHashMap throws ConcurrentModificationExceptions, and there isn't a ConcurrentLinkedHashMap :(
    private final LinkedList<Position> positions = new LinkedList<>();

    @Override
    public void onDisable() {
        super.onDisable();

        // Clear positions
        positions.clear();
    }

    @Override
    public void onTick() {
        if (!nullCheck() || mc.player.ticksExisted <= 20) {

            // We may have just loaded into a world, so we need to clear the positions
            positions.clear();
            return;
        }

        // Add the player's position
        // We are adding the player's last position so it is just behind the player, and will not be obvious on the screen (especially when elytra flying)
        positions.add(new Position(new Vec3d(mc.player.lastTickPosX, mc.player.lastTickPosY, mc.player.lastTickPosZ), System.currentTimeMillis()));

        // Remove positions that are too old
        positions.removeIf(position -> System.currentTimeMillis() - position.getTime() >= lifespan.getValue() * 1000 && !infinite.getValue());
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

        glBegin(GL_LINE_STRIP);

        // Render positions
        positions.forEach(position -> {

            // Set line colour
            glColor4f(ColorUtil.getPrimaryColor().getRed() / 255F, ColorUtil.getPrimaryColor().getGreen() / 255F, ColorUtil.getPrimaryColor().getBlue() / 255F, 1);

            // draw line
            glVertex3d(position.getVec().x - mc.getRenderManager().viewerPosX, position.getVec().y - mc.getRenderManager().viewerPosY, position.getVec().z - mc.getRenderManager().viewerPosZ);
        });

        // Reset colour
        glColor4d(1, 1, 1, 1);

        glEnd();
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_LINE_SMOOTH);
        glDisable(GL_BLEND);
        glEnable(GL_TEXTURE_2D);
        glPopMatrix();
    }

    public static class Position {

        // The position's vector
        private final Vec3d vec;

        // The System.currentTimeMillis() at the time of instantiating the position
        private final long time;

        public Position(Vec3d vec, long time) {
            this.vec = vec;
            this.time = time;
        }

        /**
         * Gets the position's vector
         * @return The position's vector
         */
        public Vec3d getVec() {
            return vec;
        }

        /**
         * Gets the creation time
         * @return The creation time
         */
        public long getTime() {
            return time;
        }
    }
}