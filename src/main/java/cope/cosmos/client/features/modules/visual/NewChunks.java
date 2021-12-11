package cope.cosmos.client.features.modules.visual;

import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.event.annotation.Subscription;
import cope.cosmos.util.render.RenderBuilder;
import cope.cosmos.util.render.RenderUtil;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.util.math.BlockPos;

import java.awt.*;
import java.util.Set;

public class NewChunks extends Module {
    public NewChunks() {
        super("NewChunks", Category.VISUAL, "Highlights newly generated chunks", () -> String.valueOf(newChunks.size()));
    }

    public static final Setting<LimitedBox> box = new Setting<>("Box", LimitedBox.OUTLINE).setDescription("How to draw the box");

    public static final Setting<Double> outlineWidth = new Setting<>("Width", 0.0, 1.5, 3.0, 1).setDescription("Line width of the outline render").setVisible(() -> box.getValue().equals(LimitedBox.FILLED) || box.getValue().equals(LimitedBox.BOTH) || box.getValue().equals(LimitedBox.OUTLINE) || box.getValue().equals(LimitedBox.CLAW));
    public static final Setting<Color> color = new Setting<>("Color", new Color(255, 0, 0, 45)).setDescription("The highlight color");

    private static final Set<Chunk> newChunks = new ConcurrentSet<>();

    @Override
    public void onDisable() {
        super.onDisable();

        newChunks.clear();
    }

    @Override
    public void onRender3D() {
        for (Chunk chunk : newChunks) {
            RenderUtil.drawBox(new RenderBuilder().position(new BlockPos(chunk.x, 0, chunk.z)).box(box.getValue().box).length(16).height(0.0).width(outlineWidth.getValue()).color(color.getValue()).blend().depth(true).texture());
        }
    }

    @Subscription
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {
        if (event.getPacket() instanceof SPacketChunkData) {
            SPacketChunkData packet = (SPacketChunkData) event.getPacket();
            if (!packet.isFullChunk()) {
                newChunks.add(new Chunk(packet.getChunkX() * 16, packet.getChunkZ() * 16));
            }
        }
    }

    private static class Chunk {
        private final int x, z;

        public Chunk(int x, int y) {
            this.x = x;
            this.z = y;
        }
    }

    // we dont want to use glow or reverse box modes, so..
    public enum LimitedBox {
        OUTLINE(RenderBuilder.Box.OUTLINE),
        FILLED(RenderBuilder.Box.FILL),
        BOTH(RenderBuilder.Box.BOTH),
        CLAW(RenderBuilder.Box.CLAW),
        NONE(RenderBuilder.Box.NONE);

        private final RenderBuilder.Box box;
        LimitedBox(RenderBuilder.Box box) {
            this.box = box;
        }
    }
}
