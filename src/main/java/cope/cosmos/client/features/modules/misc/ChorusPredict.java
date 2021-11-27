package cope.cosmos.features.modules.misc;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.client.ColorUtil;
import cope.cosmos.util.render.RenderBuilder;
import cope.cosmos.util.render.RenderBuilder.Box;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.client.events.PacketEvent;

import net.minecraft.entity.Entity;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.math.BlockPos;



@SuppressWarnings("unused")
public class ChorusPredict extends Module {
  public static ChorusPredict INSTANCE;
  
  public ChorusPredict() {
        super("ChorusPredict", Category.MISC, "Predicts Where Someone will teleport when eating chorus fruit");
        INSTANCE = this;
        setExempt(true);
    }

    public static Setting<Color> renderColor = new Setting<>("TP Color", "Color of the Position they will tp to", new Color(255, 0, 0, 45)).setParent(colors);


    public void onUpdate() {
        if (tpPos == null) return;
        for (Entity entity : mc.world.loadedEntityList) {
            if (entity.getDistance(tpPos.getX(), tpPos.getY(), tpPos.getZ()) < 1) {
                tpPos = null;
                break;
            }
        }

    }

    public void onRender3D() {
        if (tpPos != null) {
            RenderUtil.drawBox(new RenderBuilder().position(tpPos).color(renderColor).box(Box.FILL).setup().line(1.5F).depth(true).blend().texture());

        }
    }

    @Listener
    public void onUpdate(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketSoundEffect) {
            if (((SPacketSoundEffect) event.getPacket()).getSound() == SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT) {
                tpPos = new BlockPos(((SPacketSoundEffect) event.getPacket()).getX(), ((SPacketSoundEffect) event.getPacket()).getY(), ((SPacketSoundEffect) event.getPacket()).getZ());
            }
        }
    }
}
