package cope.cosmos.client.manager.managers;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.util.chat.ChatBuilder;
import cope.cosmos.util.chat.ChatUtil;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author linustouchtips
 * @since 01/03/2022
 */
public class ChatManager extends Manager {

    // unique id map
    private final Map<Module, Integer> messageMap = new HashMap<>();

    public ChatManager() {
        super("ChatManager", "Manages client chat messages");

        // assign each module has a unique message id
        getCosmos().getModuleManager().getAllModules().forEach(mod -> messageMap.put(mod, ThreadLocalRandom.current().nextInt(32767)));
    }

    /**
     * Sends a message in chat (only visible to the user)
     * @param in The message
     */
    public void sendMessage(String in) {
        mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new TextComponentString(in), ThreadLocalRandom.current().nextInt(32767));
    }

    /**
     * Sends a message in chat (visible to the server)
     * @param in The message
     */
    public void sendChatMessage(String in) {
        mc.player.connection.sendPacket(new CPacketChatMessage(in));
    }

    /**
     * Sends a message in chat with the client prefix (only visible to the user)
     * @param in The message
     */
    public void sendClientMessage(String in, int identifier) {
        mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new TextComponentString(ChatUtil.getPrefix() + in), identifier);
    }

    /**
     * Sends a message in chat with the client prefix (only visible to the user)
     * @param in The message
     */
    public void sendClientMessage(Number in, int identifier) {
        mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new TextComponentString(ChatUtil.getPrefix() + in), identifier);
    }

    /**
     * Sends a message in chat with the client prefix (only visible to the user)
     * @param in The message
     */
    public void sendClientMessage(String in) {
        sendClientMessage(in, ThreadLocalRandom.current().nextInt(32767));
    }

    /**
     * Sends a message in chat with the client prefix (only visible to the user)
     * @param in The message
     */
    public void sendClientMessage(Number in) {
        sendClientMessage(in, ThreadLocalRandom.current().nextInt(32767));
    }

    /**
     * Sends a message in chat with the client prefix (only visible to the user)
     * @param in The message
     */
    public void sendClientMessage(Module in) {
        mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new TextComponentString(ChatUtil.getPrefix() + in.getName() + (in.isEnabled() ? ChatFormatting.GREEN + " enabled!" : ChatFormatting.RED + " disabled!")), messageMap.get(in));
    }

    /**
     * Sends a hoverable message in chat with the client prefix (only visible to the user)
     * @param in The message
     * @param hoverable The message in the hoverable
     */
    public void sendHoverableMessage(String in, String hoverable) {
        new ChatBuilder().append(ChatUtil.getPrefix() + in, new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatBuilder().append(Cosmos.NAME, new Style().setColor(TextFormatting.DARK_PURPLE)).append("\n" + hoverable, new Style().setColor(TextFormatting.BLUE)).component()))).append(" ", new Style().setColor(TextFormatting.DARK_PURPLE)).push();
    }
}
