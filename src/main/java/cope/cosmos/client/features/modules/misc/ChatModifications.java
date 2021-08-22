package cope.cosmos.client.features.modules.misc;

import cope.cosmos.asm.mixins.accessor.ICPacketChatMessage;
import cope.cosmos.asm.mixins.accessor.ITextComponentString;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.ModuleToggleEvent;
import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.events.TotemPopEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.client.ChatUtil;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings("unused")
public class ChatModifications extends Module {
    public static ChatModifications INSTANCE;

    public ChatModifications() {
        super("ChatModifications", Category.MISC, "Allows you to modify the in-game chat window");
        INSTANCE = this;
    }

    public static Setting<Time> time = new Setting<>("Time", "Time format", Time.NA);
    public static Setting<Boolean> prefix = new Setting<>("Prefix", "Add a cosmos prefix before chat messages", false);
    public static Setting<Boolean> suffix = new Setting<>("Suffix", "Add a cosmos suffix after chat messages", true);
    public static Setting<Boolean> colored = new Setting<>("Colored", "Add a > before public messages", true);

    /*
    public static Setting<Boolean> highlight = new Setting<>("Highlight", true);
    public static Setting<TextFormatting> self = new Setting<>("Self", TextFormatting.DARK_PURPLE).setParent(highlight);
    public static Setting<TextFormatting> friends = new Setting<>("Friends", TextFormatting.AQUA).setParent(highlight);
     */

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketChatMessage) {
            if (((CPacketChatMessage) event.getPacket()).getMessage().startsWith("/") || ((CPacketChatMessage) event.getPacket()).getMessage().startsWith("!") || ((CPacketChatMessage) event.getPacket()).getMessage().startsWith("$") || ((CPacketChatMessage) event.getPacket()).getMessage().startsWith("?") || ((CPacketChatMessage) event.getPacket()).getMessage().startsWith(".") || ((CPacketChatMessage) event.getPacket()).getMessage().startsWith(","))
                return;

            ((ICPacketChatMessage) event.getPacket()).setMessage((colored.getValue() ? "> " : "") + ((CPacketChatMessage) event.getPacket()).getMessage() + (suffix.getValue() ? " \u23d0 " + ChatUtil.toUnicode(Cosmos.NAME) : ""));
        }
    }

    @SubscribeEvent
    public void onPacketRecieve(PacketEvent.PacketReceiveEvent event) {
        if (nullCheck() && event.getPacket() instanceof SPacketChat) {
            if (((SPacketChat) event.getPacket()).getChatComponent() instanceof TextComponentString && !((SPacketChat) event.getPacket()).getType().equals(ChatType.GAME_INFO)) {
                TextComponentString component = (TextComponentString) ((SPacketChat) event.getPacket()).getChatComponent();

                String formattedTime = "";
                switch (time.getValue()) {
                    case NA:
                        formattedTime = new SimpleDateFormat("h:mm a").format(new Date());
                        break;
                    case EU:
                        formattedTime = new SimpleDateFormat("k:mm").format(new Date());
                        break;
                }

                if (component.getText() != null) {
                    String formattedText = (!time.getValue().equals(Time.NONE) ? TextFormatting.GRAY + "[" + formattedTime + "] " + TextFormatting.RESET : "") + (prefix.getValue() ? ChatUtil.getPrefix() : "") + component.getText();

                    /*
                    if (highlight.getValue()) {
                        formattedText = formattedText.replaceAll("(?i)" + mc.player.getName(), self.getValue() + mc.player.getName() + ChatFormatting.RESET);

                        for (EntityPlayer friend : mc.world.playerEntities.stream().filter(entityPlayer -> Cosmos.INSTANCE.getSocialManager().getSocial(entityPlayer.getName()).equals(Relationship.FRIEND)).collect(Collectors.toList())) {
                            formattedText = formattedText.replaceAll("(?i)" + friend.getName(), friends.getValue() + friend.getName() + ChatFormatting.RESET);
                        }
                    }
                     */

                    ((ITextComponentString) component).setText(formattedText);
                }
            }
        }
    }

    public enum Time {
        NA, EU, NONE
    }
}
