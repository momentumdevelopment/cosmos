package cope.cosmos.client.features.modules.visual;

import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.client.Colors;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.event.annotation.Subscription;
import cope.cosmos.util.render.RenderBuilder;
import cope.cosmos.util.render.RenderUtil;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec2f;

import java.util.Set;

import static cope.cosmos.util.render.RenderBuilder.Box;

public class NewChunks extends Module {
    public static NewChunks INSTANCE;

    public NewChunks() {
        super("NewChunks", Category.VISUAL, "Highlights newly generated chunks");
        INSTANCE = this;
    }

    public static final Setting<LimitedBox> box = new Setting<>("Render", LimitedBox.OUTLINE).setDescription("How to draw the box");
    public static final Setting<Double> height = new Setting<>("Height", 0.0, 0.0, 3.0, 1).setDescription("The height to render the new chunk at");
    public static final Setting<Double> outlineWidth = new Setting<>("Width", 0.0, 1.5, 3.0, 1).setDescription("Line width of the outline render").setVisible(() -> box.getValue().equals(LimitedBox.BOTH) || box.getValue().equals(LimitedBox.OUTLINE) || box.getValue().equals(LimitedBox.CLAW));

    private final Set<Vec2f> newChunks = new ConcurrentSet<>();

    @Override
    public void onDisable() {
        super.onDisable();

        newChunks.clear();
    }

    @Override
    public void onRender3D() {
        newChunks.forEach((chunk) -> {
            AxisAlignedBB axisAlignedBB = new AxisAlignedBB(chunk.x, 0, chunk.y, chunk.x + 16.0, height.getValue(), chunk.y + 16.0);
            if (RenderUtil.isBoundingBoxInFrustum(mc.getRenderViewEntity(), axisAlignedBB)) {
                RenderUtil.drawBox(new RenderBuilder().position(axisAlignedBB).box(box.getValue().box).width(outlineWidth.getValue()).color(Colors.color.getValue()).blend().depth(true).texture());
            }
        });
    }

    @Subscription
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {
        if (event.getPacket() instanceof SPacketChunkData) {
            SPacketChunkData packet = (SPacketChunkData) event.getPacket();
            if (!packet.isFullChunk()) {
                newChunks.add(new Vec2f(packet.getChunkX() * 16.0f, packet.getChunkZ() * 16.0f));
            }
        }
    }

    // we dont want to use glow or reverse box modes, so..
    public enum LimitedBox {
        OUTLINE(Box.OUTLINE),
        FILLED(Box.FILL),
        BOTH(Box.BOTH),
        CLAW(Box.CLAW);

        private final Box box;

        LimitedBox(Box box) {
            this.box = box;
        }
    }
}
