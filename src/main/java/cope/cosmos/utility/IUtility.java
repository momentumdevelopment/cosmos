package cope.cosmos.utility;

import cope.cosmos.client.Cosmos;
import net.minecraft.client.Minecraft;

public interface IUtility {

	Minecraft mc = Minecraft.getMinecraft();

	default boolean nullCheck() {
		return mc.player != null || mc.world != null;
	}

	default Cosmos getCosmos() {
		return Cosmos.INSTANCE;
	}
}
