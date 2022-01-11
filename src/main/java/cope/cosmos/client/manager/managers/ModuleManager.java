package cope.cosmos.client.manager.managers;

import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.client.*;
import cope.cosmos.client.features.modules.combat.*;
import cope.cosmos.client.features.modules.misc.*;
import cope.cosmos.client.features.modules.movement.*;
import cope.cosmos.client.features.modules.player.*;
import cope.cosmos.client.features.modules.visual.*;
import cope.cosmos.client.manager.Manager;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author bon55
 * @since 05/05/2021
 */
public class ModuleManager extends Manager {
	public ModuleManager() {
		super("ModuleManager", "Manages all the client modules");
	}

	// list of all modules
	private static final List<Module> modules = Arrays.asList(
			// client
			new ClickGUI(),
			new Colors(),
			new DiscordPresence(),
			new Font(),
			new Social(),
			new HUD(),

			// combat
			new Aura(),
			new AutoCrystal(),
			// new AutoTrap(),
			new Burrow(),
			new Criticals(),
			new FastProjectile(),
			new HoleFill(),
			new Offhand(),
			new Surround(),
			
			// misc
			new AntiAFK(),
			new AntiAim(),
			new AntiCrash(),
			new ChatModifications(),
			new ExtraTab(),
			new FakePlayer(),
			new MiddleClick(),
			new MultiTask(),
			new Notifier(),
			new Portal(),
			new Timer(),
			new XCarry(),
			
			// movement
			new ElytraFlight(),
			new LongJump(),
			new NoSlow(),
			new PacketFlight(),
			new ReverseStep(),
			// new Scaffold(),
			new Speed(),
			new Sprint(),
			new Step(),
			new Velocity(),

			// player
			new AntiHunger(),
			new AntiVoid(),
			new Blink(),
			new FastUse(),
			new Interact(),
			new NoFall(),
			new NoRotate(),
			new PingSpoof(),
			new Reach(),
			new SpeedMine(),

			// visual
			new CameraClip(),
			new Chams(),
			new ESP(),
			new FullBright(),
			new HoleESP(),
			new Nametags(),
			new NewChunks(),
			new NoRender(),
			new SkyColor()
	);

	/**
	 * Gets a list of all the client's modules
	 * @return List of all the client's modules
	 */
	public static List<Module> getAllModules() {
		return modules;
	}

	/**
	 * Gets a list of all the client's modules that fulfill a specified condition
	 * @param predicate The specified condition
	 * @return List of all the client's modules that fulfill the specified condition
	 */
	public static List<Module> getModules(Predicate<? super Module> predicate) {
		return modules.stream()
				.filter(predicate)
				.collect(Collectors.toList());
	}

	/**
	 * Gets the first module that fulfills a specified condition
	 * @param predicate The specified condition
	 * @return The first module that fulfills the specified condition
	 */
	public static Module getModule(Predicate<? super Module> predicate) {
		return modules.stream()
				.filter(predicate)
				.findFirst()
				.orElse(null);
	}
}
