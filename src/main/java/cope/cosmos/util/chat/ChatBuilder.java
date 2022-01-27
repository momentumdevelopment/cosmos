package cope.cosmos.util.chat;

import cope.cosmos.util.Wrapper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;

/**
 * @author milse113
 * @since 05/26/2021
 */
public class ChatBuilder implements Wrapper {

    // chat component
    private final ITextComponent textComponent = new TextComponentString("");

    /**
     * Appends a message to the text component
     * @param message The message
     * @param style The style of the text
     * @return The current chat builder
     */
    public ChatBuilder append(String message, Style style) {
        textComponent.appendSibling(new TextComponentString(message).setStyle(style));
        return this;
    }

    /**
     * Sends the message to the chat
     */
    public void push() {
        mc.player.sendMessage(textComponent);
    }

    /**
     * Gets the current chat component
     * @return The current chat component
     */
    public ITextComponent component() {
        return textComponent;
    }
}
