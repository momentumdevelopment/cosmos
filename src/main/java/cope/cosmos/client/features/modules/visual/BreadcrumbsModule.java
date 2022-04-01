package cope.cosmos.client.features.modules.visual;

import cope.cosmos.client.events.network.ConnectEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.string.ColorUtil;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.LinkedList;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Wolfsurge
 * @since 01/04/22
 */
public class BreadcrumbsModule extends Module {

    public static BreadcrumbsModule INSTANCE;

    public BreadcrumbsModule() {
        super("Breadcrumbs", Category.VISUAL, "Draws a trail behind you");
        INSTANCE = this;
    }

    // Settings
    public static final Setting<Boolean> infinite = new Setting<>("Infinite", false).setDescription("Makes breadcrumbs last forever");
    public static final Setting<Float> lifespanValue = new Setting<>("Lifespan", 10F, 100F, 1000F, 0).setDescription("The lifespan of the positions in ticks").setVisible(() -> !infinite.getValue());
    public static final Setting<Float> lineWidth = new Setting<>("LineWidth", 0.1F, 3F, 5F, 1).setDescription("The width of the lines");

    // List of positions
    private final ArrayList<Position> positions = new ArrayList<>();

    @Override
    public void onDisable() {
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
        positions.add(new Position(new Vec3d(mc.player.lastTickPosX, mc.player.lastTickPosY, mc.player.lastTickPosZ)));

        // Update lifespan
        positions.forEach(position -> {
            position.update();
        });

        // Remove positions that are too old
        positions.removeIf(p -> !p.isAlive() && !infinite.getValue());
    }

    @Override
    public void onRender3D() {
        glPushMatrix();

        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);
        glEnable(GL_BLEND);
        glDisable(GL_DEPTH_TEST);
        glLineWidth(lineWidth.getValue());
        mc.entityRenderer.disableLightmap();
        glBegin(GL_LINE_STRIP);

        // Set line colour
        glColor4f(ColorUtil.getPrimaryColor().getRed() / 255F, ColorUtil.getPrimaryColor().getGreen() / 255F, ColorUtil.getPrimaryColor().getBlue() / 255F, ColorUtil.getPrimaryColor().getAlpha() / 255F);

        // Render positions
        for (Position pos : positions) {
            glVertex3d(pos.getPosition().x - mc.getRenderManager().viewerPosX, pos.getPosition().y - mc.getRenderManager().viewerPosY, pos.getPosition().z - mc.getRenderManager().viewerPosZ);
        }

        // Reset colour
        glColor4d(1, 1, 1, 1);

        glEnd();
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_LINE_SMOOTH);
        glDisable(GL_BLEND);
        glEnable(GL_TEXTURE_2D);
        glPopMatrix();
    }

    static class Position {
        // The position's position
        private final Vec3d position;

        // The position's lifespan
        private long lifespan = lifespanValue.getValue().longValue();

        public Position(Vec3d position) {
            this.position = position;
        }

        /**
         * Decreases the lifespan of the position
         */
        public void update() {
            lifespan--;
        }

        /**
         * Checks if the position is alive
         * @return If the position is alive
         */
        public boolean isAlive() {
            return lifespan > 0;
        }

        /**
         * Gets the position's position
         * @return The position's position
         */
        public Vec3d getPosition() {
            return position;
        }
    }

}
