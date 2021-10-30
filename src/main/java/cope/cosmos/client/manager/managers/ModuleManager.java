package cope.cosmos.client.manager.managers;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.client.*;
import cope.cosmos.client.features.modules.combat.*;
import cope.cosmos.client.features.modules.misc.*;
import cope.cosmos.client.features.modules.movement.*;
import cope.cosmos.client.features.modules.player.*;
import cope.cosmos.client.features.modules.visual.*;
import cope.cosmos.client.manager.Manager;

public class ModuleManager extends Manager {
	public ModuleManager() {
		super("ModuleManager", "Manages all the client modules");
	}

	private static final List<Module> modules = Arrays.asList(
			//Client
			new ClickGUI(),
			new Colors(),
			new DiscordPresence(),
			new Font(),
			new Social(),
			new HUD(),

			//Combat
			new Aura(),
			new AutoBowRelease(),
			new AutoCrystal(),
			// new AutoTrap(),
			new Burrow(),
			new Criticals(),
			new FastProjectile(),
			new HoleFill(),
			new Offhand(),
			new Surround(),
			
			//Misc
			new AntiAim(),
			new ChatModifications(),
			new FakePlayer(),
			new Notifier(),
			new Portal(),
			new Timer(),
			new XCarry(),
			
			//Movement
			// new BlockPhase(),
			new ElytraFlight(),
			new NoSlow(),
			new PacketFlight(),
			new ReverseStep(),
			new Sprint(),
			new Velocity(),

			//Player
			new AntiHunger(),
			new Blink(),
			new FastUse(),
			new Freecam(),
			new Interact(),
			new NoFall(),
			new PingSpoof(),
			new SpeedMine(),

			//Visual
			new CameraClip(),
			new Chams(),
			new ESP(),
			new FullBright(),
			new HoleESP(),
			new NoRender()
	);

	public static List<Module> getAllModules() {
		return ModuleManager.modules;
	}
	
	public static List<Module> getModules(Predicate<? super Module> predicate) {
		return ModuleManager.modules.stream().filter(predicate).collect(Collectors.toList());
	}
	
	public static Module getModule(Predicate<? super Module> predicate) {
		return ModuleManager.modules.stream().filter(predicate).findFirst().orElse(null);
	}
}
