package cope.cosmos.client.features.modules;

import net.minecraft.util.ResourceLocation;

public enum Category {
	CLIENT(new ResourceLocation("cosmos", "textures/icons/client.png")),
	COMBAT(new ResourceLocation("cosmos", "textures/icons/combat.png")),
	VISUAL(new ResourceLocation("cosmos", "textures/icons/visual.png")),
	PLAYER(new ResourceLocation("cosmos", "textures/icons/player.png")),
	MOVEMENT(new ResourceLocation("cosmos", "textures/icons/movement.png")),
	MISC(new ResourceLocation("cosmos", "textures/icons/misc.png")),
	HIDDEN(new ResourceLocation("cosmos", "textures/icons/client.png"));

	private final ResourceLocation resourceLocation;

	Category(ResourceLocation resourceLocation) {
		this.resourceLocation = resourceLocation;
	}

	public ResourceLocation getResourceLocation() {
		return this.resourceLocation;
	}
}
