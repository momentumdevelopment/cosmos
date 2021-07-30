package cope.cosmos.util.client;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import com.mojang.realmsclient.gui.ChatFormatting;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.manager.managers.ModuleManager;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.util.Wrapper;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;

public class ChatUtil implements Wrapper {

	private static Map<Module, Integer> messageMap = new HashMap<>();
	
	public static String getPrefix() {
		return ChatFormatting.BLUE + "<" + ChatFormatting.DARK_PURPLE + "Cosmos" + ChatFormatting.BLUE + "> " + ChatFormatting.GRAY;
	}
	
	public static void sendMessage(String message) {
		mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString(ChatUtil.getPrefix() + message));
	}

	public static void sendRawMessage(String message) {
		mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
	}

	public static void sendHoverableMessage(String message, String hoverable) {
		new ChatBuilder().append(ChatUtil.getPrefix() + message, new Style().setColor(TextFormatting.DARK_PURPLE).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatBuilder().append(Cosmos.NAME, new Style().setColor(TextFormatting.DARK_PURPLE)).append("\n" + hoverable, new Style().setColor(TextFormatting.BLUE)).component()))).append(" ", new Style().setColor(TextFormatting.DARK_PURPLE)).push();
	}
	
	public static void sendModuleEnableMessage(Module m) {
		mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new TextComponentString(ChatUtil.getPrefix() + m.getName() + ChatFormatting.GREEN + " enabled."), ChatUtil.messageMap.get(m));
	}
	
	public static void sendModuleDisableMessage(Module m) {
		mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new TextComponentString(ChatUtil.getPrefix() + m.getName() + ChatFormatting.RED + " disabled."), ChatUtil.messageMap.get(m));
	}

	public static String toUnicode(String message) {
		return message.toLowerCase().replace("a", "\u1d00").replace("b", "\u0299").replace("c", "\u1d04").replace("d", "\u1d05").replace("e", "\u1d07").replace("f", "\ua730").replace("g", "\u0262").replace("h", "\u029c").replace("i", "\u026a").replace("j", "\u1d0a").replace("k", "\u1d0b").replace("l", "\u029f").replace("m", "\u1d0d").replace("n", "\u0274").replace("o", "\u1d0f").replace("p", "\u1d18").replace("q", "\u01eb").replace("r", "\u0280").replace("s", "\ua731").replace("t", "\u1d1b").replace("u", "\u1d1c").replace("v", "\u1d20").replace("w", "\u1d21").replace("x", "\u02e3").replace("y", "\u028f").replace("z", "\u1d22");
	}
	
	static {
		ModuleManager.getAllModules().forEach(mod -> ChatUtil.messageMap.put(mod, ThreadLocalRandom.current().nextInt(32767)));
	}
}
