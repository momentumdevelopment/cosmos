package cope.cosmos.client.features.modules;

/**
 * @author bon55
 * @since 05/05/2021
 */
public enum Category {

	/**
	 * Modules associated with client processes
	 */
	CLIENT,

	/**
	 * Modules used for combat (Ex: KillAura, AutoCrystal, etc.)
	 */
	COMBAT,

	/**
	 * Modules that are visual modifications (Ex: ESP, Nametags, HoleESP, etc.)
	 */
	VISUAL,

	/**
	 * Modules that are modifications to player characteristics (Ex: AntiHunger, SpeedMine, FastUse, etc.)
	 */
	PLAYER,

	/**
	 * Modules that allow the player to move in unnatural ways (Ex: Flight, Speed, ReverseStep, etc.)
	 */
	MOVEMENT,

	/**
	 * Modules that don't fit in the other categories
	 */
	MISC,

	/**
	 * Modules hidden from the ClickGUI
	 */
	HIDDEN
}
