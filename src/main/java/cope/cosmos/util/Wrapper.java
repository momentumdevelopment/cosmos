package cope.cosmos.util;

import cope.cosmos.client.Cosmos;
import net.minecraft.client.Minecraft;

public interface Wrapper {

	Minecraft mc = Minecraft.getMinecraft();

	default boolean nullCheck() {
		return mc.player != null && mc.world != null;
	}

	default Cosmos getCosmos() {
		return Cosmos.INSTANCE;
	}
}
