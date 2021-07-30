package cope.cosmos.util;

import net.minecraft.client.Minecraft;

public interface Wrapper {

	Minecraft mc = Minecraft.getMinecraft();

	default boolean nullCheck() {
		return mc.player != null || mc.world != null;
	}
}
