package cope.cosmos.util;

import cope.cosmos.client.Cosmos;
import net.minecraft.client.Minecraft;

/**
 * @author bon55, linustouchtips
 * @since 05/13/2021
 */
public interface Wrapper {

	// minecraft instance
	Minecraft mc = Minecraft.getMinecraft();

	/**
	 * Checks if the world and the player are not null (usually unsafe to run operations if these are null)
	 * @return Whether the world and the player are not null
	 */
	default boolean nullCheck() {
		return mc.player != null && mc.world != null;
	}

	/**
	 * Gets the client instance
	 * @return The client instance
	 */
	default Cosmos getCosmos() {
		return Cosmos.INSTANCE;
	}
}
