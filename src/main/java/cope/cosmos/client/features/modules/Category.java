package cope.cosmos.client.features.modules;

import net.minecraft.util.ResourceLocation;

public enum Category {
	CLIENT(new ResourceLocation("panels", "textures/icons/client.png")),
	COMBAT(new ResourceLocation("panels", "textures/icons/combat.png")),
	VISUAL(new ResourceLocation("panels", "textures/icons/visual.png")),
	PLAYER(new ResourceLocation("panels", "textures/icons/player.png")),
	MOVEMENT(new ResourceLocation("panels", "textures/icons/movement.png")),
	MISC(new ResourceLocation("panels", "textures/icons/misc.png")),
	HIDDEN(new ResourceLocation("panels", "textures/icons/client.png"));

	private final ResourceLocation resourceLocation;

	Category(ResourceLocation resourceLocation) {
		this.resourceLocation = resourceLocation;
	}

	public ResourceLocation getResourceLocation() {
		return this.resourceLocation;
	}
}
