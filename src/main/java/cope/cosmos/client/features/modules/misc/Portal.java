package cope.cosmos.client.features.modules.misc;

import cope.cosmos.asm.mixins.accessor.IEntity;
import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@SuppressWarnings("unused")
public class Portal extends Module {
    public static Portal INSTANCE;

    public Portal() {
        super("Portal", Category.MISC, "Modifies portal behavior");
        INSTANCE = this;
    }

    public static Setting<Boolean> godMode = new Setting<>("GodMode", "Cancels teleport packets", false);
    public static Setting<Boolean> screens = new Setting<>("Screens", "Allow the use of screens in portals", true);
    public static Setting<Boolean> effect = new Setting<>("Effect", "Cancels the portal overlay effect", false);
    public static Setting<Boolean> sounds = new Setting<>("Sounds", "Cancels portal sounds", false);

    @Override
    public void onUpdate() {
        ((IEntity) mc.player).setInPortal(!screens.getValue() && ((IEntity) mc.player).getInPortal());
        GuiIngameForge.renderPortal = !effect.getValue();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        GuiIngameForge.renderPortal = true;
    }

    @SubscribeEvent
    public void onSound(PlaySoundEvent event) {
        if (sounds.getValue() && (event.getName().equals("block.portal.ambient") || event.getName().equals("block.portal.travel") || event.getName().equals("block.portal.trigger")))
            event.setResultSound(null);
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        event.setCanceled(nullCheck() && event.getPacket() instanceof CPacketConfirmTeleport && mc.player.timeInPortal > 0 && godMode.getValue());
    }
}
