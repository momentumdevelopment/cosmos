package cope.cosmos.utility.client;

import cope.cosmos.utility.IUtility;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;

public class ChatBuilder implements IUtility {

    ITextComponent textComponent = new TextComponentString("");

    public ChatBuilder append(String message, Style style) {
        textComponent.appendSibling(new TextComponentString(message).setStyle(style));
        return this;
    }

    public void push() {
        mc.player.sendMessage(textComponent);
    }

    public ITextComponent component() {
        return textComponent;
    }
}
