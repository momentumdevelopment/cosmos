package cope.cosmos.client.features.modules.miscellaneous;

import cope.cosmos.asm.mixins.accessor.ICPacketChatMessage;
import cope.cosmos.asm.mixins.accessor.ITextComponentString;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.chat.ChatUtil;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author linustouchtips
 * @since 06/08/2021
 */
public class ChatModificationsModule extends Module {
    public static ChatModificationsModule INSTANCE;

    public ChatModificationsModule() {
        super("ChatModifications", Category.MISCELLANEOUS, "Allows you to modify the in-game chat window");
        INSTANCE = this;
    }

    // **************************** general settings ****************************

    public static Setting<Time> time = new Setting<>("Time", Time.NA)
            .setDescription("Time format");

    public static Setting<Boolean> prefix = new Setting<>("Prefix", false)
            .setAlias("ChatPrefix")
            .setDescription("Add a cosmos prefix before chat messages");

    public static Setting<Boolean> suffix = new Setting<>("Suffix", true)
            .setAlias("ChatSuffix")
            .setDescription("Add a cosmos suffix after chat messages");

    public static Setting<Boolean> colored = new Setting<>("Colored", true)
            .setAlias("GreenText", "BlueText", "ColorText", "ColoredText")
            .setDescription("Add a > before public messages");

    /*
    public static Setting<Boolean> highlight = new Setting<>("Highlight", true);
    public static Setting<TextFormatting> self = new Setting<>("Self", TextFormatting.DARK_PURPLE).setParent(highlight);
    public static Setting<TextFormatting> friends = new Setting<>("Friends", TextFormatting.AQUA).setParent(highlight);
     */

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        // packet for chat messages
        if (event.getPacket() instanceof CPacketChatMessage) {
            // make sure the message is not a command
            if (!((CPacketChatMessage) event.getPacket()).getMessage().startsWith("/") && !((CPacketChatMessage) event.getPacket()).getMessage().startsWith("!") && !((CPacketChatMessage) event.getPacket()).getMessage().startsWith("$") && !((CPacketChatMessage) event.getPacket()).getMessage().startsWith("?") && !((CPacketChatMessage) event.getPacket()).getMessage().startsWith(".") && !((CPacketChatMessage) event.getPacket()).getMessage().startsWith(",")) {
                // reformat messaged
                StringBuilder formattedMessage = new StringBuilder();

                // colors messages green
                if (colored.getValue()) {
                    formattedMessage.append("> ");
                }

                formattedMessage.append(((CPacketChatMessage) event.getPacket()).getMessage());

                // suffix
                if (suffix.getValue()) {
                    formattedMessage.append(" \u23d0 ").append(toUnicode(Cosmos.NAME));
                }

                // update the message
                ((ICPacketChatMessage) event.getPacket()).setMessage(formattedMessage.toString());
            }
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {
        // packet for server chat messages
        if (event.getPacket() instanceof SPacketChat) {

            // get the text
            if (((SPacketChat) event.getPacket()).getChatComponent() instanceof TextComponentString && !((SPacketChat) event.getPacket()).getType().equals(ChatType.GAME_INFO)) {

                // the chat message
                TextComponentString component = (TextComponentString) ((SPacketChat) event.getPacket()).getChatComponent();

                // timestamp
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
                    // timestamp formatted
                    StringBuilder formattedText = new StringBuilder();

                    // add a prefix before the message
                    if (prefix.getValue()) {
                        formattedText.append(ChatUtil.getPrefix());
                    }

                    // add a timestamp before the message
                    if (!time.getValue().equals(Time.NONE)) {
                        formattedText.append(TextFormatting.GRAY).append("[").append(formattedTime).append("] ").append(TextFormatting.RESET);
                    }

                    formattedText.append(component.getText());

                    /*
                    if (highlight.getValue()) {
                        formattedText = formattedText.replaceAll("(?i)" + mc.player.getName(), self.getValue() + mc.player.getName() + ChatFormatting.RESET);

                        for (EntityPlayer friend : mc.world.playerEntities.stream().filter(entityPlayer -> Cosmos.INSTANCE.getSocialManager().getSocial(entityPlayer.getName()).equals(Relationship.FRIEND)).collect(Collectors.toList())) {
                            formattedText = formattedText.replaceAll("(?i)" + friend.getName(), friends.getValue() + friend.getName() + ChatFormatting.RESET);
                        }
                    }
                     */

                    // replace the chat message
                    ((ITextComponentString) component).setText(formattedText.toString());
                }
            }
        }
    }

    /**
     * Converts a String into unicode characters
     * @param message The String
     * @return The converted String
     */
    public String toUnicode(String message) {
        return message.toLowerCase()
                .replace("a", "\u1d00")
                .replace("b", "\u0299")
                .replace("c", "\u1d04")
                .replace("d", "\u1d05")
                .replace("e", "\u1d07")
                .replace("f", "\ua730")
                .replace("g", "\u0262")
                .replace("h", "\u029c")
                .replace("i", "\u026a")
                .replace("j", "\u1d0a")
                .replace("k", "\u1d0b")
                .replace("l", "\u029f")
                .replace("m", "\u1d0d")
                .replace("n", "\u0274")
                .replace("o", "\u1d0f")
                .replace("p", "\u1d18")
                .replace("q", "\u01eb")
                .replace("r", "\u0280")
                .replace("s", "\ua731")
                .replace("t", "\u1d1b")
                .replace("u", "\u1d1c")
                .replace("v", "\u1d20")
                .replace("w", "\u1d21")
                .replace("x", "\u02e3")
                .replace("y", "\u028f")
                .replace("z", "\u1d22");
    }

    public enum Time {

        /**
         * Display NA time
         */
        NA,

        /**
         * Display EU time
         */
        EU,

        /**
         * No timestamps
         */
        NONE
    }
}
