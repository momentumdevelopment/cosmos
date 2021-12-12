package cope.cosmos.client.features.modules.visual;

import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.client.ColorUtil;
import cope.cosmos.util.render.RenderBuilder;
import cope.cosmos.util.render.RenderBuilder.Box;
import cope.cosmos.util.render.RenderUtil;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec2f;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Set;

/**
 * @author aesthetical
 * @since 12/11/2021
 */
@SuppressWarnings("unused")
public class NewChunks extends Module {
    public static NewChunks INSTANCE;

    public NewChunks() {
        super("NewChunks", Category.VISUAL, "Highlights newly generated chunks");
        INSTANCE = this;
    }

    public static Setting<Box> box = new Setting<>("Render", Box.OUTLINE).setDescription("Style for the visual").setExclusion(Box.GLOW, Box.REVERSE);
    public static Setting<Double> height = new Setting<>("Height", 0.0, 0.0, 3.0, 0).setDescription("The height to render the new chunk at");
    public static Setting<Double> width = new Setting<>("Width", 0.0, 1.5, 3.0, 1).setDescription("Line width of the render").setVisible(() -> box.getValue().equals(Box.BOTH) || box.getValue().equals(Box.OUTLINE) || box.getValue().equals(Box.CLAW));

    // new chunks
    private final Set<Vec2f> chunks = new ConcurrentSet<>();

    @Override
    public void onDisable() {
        super.onDisable();

        // reset our chunks
        chunks.clear();
    }

    @Override
    public void onRender3D() {

        // render the new chunks
        chunks.forEach((chunk) -> {

            // make sure the chunk is within render distance
            if (getDistance(chunk) <= mc.gameSettings.renderDistanceChunks) {
                RenderUtil.drawBox(new RenderBuilder()
                        .position(new AxisAlignedBB(chunk.x, 0, chunk.y, chunk.x + 16, height.getValue(), chunk.y + 16))
                        .box(box.getValue())
                        .width(width.getValue())
                        .color(ColorUtil.getPrimaryColor())
                        .blend()
                        .depth(true)
                        .texture()
                );
            }
        });
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {
        // packet for chunk data
        if (event.getPacket() instanceof SPacketChunkData) {
            // add it to our set if it's not been newly generated
            if (!((SPacketChunkData) event.getPacket()).isFullChunk()) {
                chunks.add(new Vec2f(((SPacketChunkData) event.getPacket()).getChunkX() * 16, ((SPacketChunkData) event.getPacket()).getChunkZ() * 16));
            }
        }
    }

    /**
     * Gets the player's distance to a chunk
     * @param chunk The chunk to get the distance to
     * @return The player's distance to the chunk
     */
    public int getDistance(Vec2f chunk) {
        // x and z distance to the chunk
        double xDistance = Math.abs(mc.player.posX - chunk.x);
        double zDistance = Math.abs(mc.player.posZ - chunk.y);

        // pythag divided by 16 (size of one chunk)
        return (int) (Math.sqrt(Math.pow(xDistance, 2) + Math.pow(zDistance, 2)) / 16);
    }
}
