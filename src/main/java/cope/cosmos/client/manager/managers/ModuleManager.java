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
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author bon55
 * @since 05/05/2021
 */
public class ModuleManager extends Manager {

	// list of all modules
	private final List<Module> modules;

	public ModuleManager() {
		super("ModuleManager", "Manages all the client modules");

		// add all modules
		modules = Arrays.asList(

				// client
				new ClickGUIModule(),
				new ColorsModule(),
				new DiscordPresenceModule(),
				new FontModule(),
				new HUDModule(),
				new SocialModule(),
				new StreamerModeModule(),

				// combat
				new AuraModule(),
				new AutoArmorModule(),
				new AutoCrystalModule(),
				// new AutoTrap(),
				new BurrowModule(),
				new CriticalsModule(),
				new FastProjectileModule(),
				new HoleFillModule(),
				new OffhandModule(),
				new SurroundModule(),

				// misc
				new AntiAFKModule(),
				new AntiAimModule(),
				new AntiCrashModule(),
				new AutoDisconnectModule(),
				new ChatModificationsModule(),
				new ExtraTabModule(),
				new FakePlayerModule(),
				new MiddleClickModule(),
				new MultiTaskModule(),
				new NotifierModule(),
				new PortalModule(),
				new TimerModule(),
				new XCarryModule(),

				// movement
				new ElytraFlightModule(),
				new EntitySpeedModule(),
				new FastFallModule(),
				new FlightModule(),
				new JesusModule(),
				new LongJumpModule(),
				new NoSlowModule(),
				new PacketFlightModule(),
				// new Scaffold(),
				new SpeedModule(),
				new SprintModule(),
				new StepModule(),
				new VelocityModule(),

				// player
				new AntiHungerModule(),
				new AntiVoidModule(),
				new BlinkModule(),
				new EntityControlModule(),
				new FastUseModule(),
				new InteractModule(),
				new NoFallModule(),
				new NoRotateModule(),
				new PingSpoofModule(),
				new ReachModule(),
				new ReplenishModule(),
				new SpeedMineModule(),

				// visual
				new BlockHighlightModule(),
				new BreadcrumbsModule(), // TODO: I need to fix this
				new BreakHighlightModule(),
				new CameraClipModule(),
				new ChamsModule(),
				new ESPModule(),
				new FullBrightModule(),
				new HoleESPModule(),
				new NametagsModule(),
				new NewChunksModule(),
				new NoRenderModule(),
				new NoWeatherModule(),
				new SkyColorModule(),
				new TracersModule(),
				new ViewModelModule(),
				new WallhackModule()
		);
	}

	/**
	 * Gets a list of all the client's modules
	 * @return List of all the client's modules
	 */
	public List<Module> getAllModules() {
		return modules;
	}

	/**
	 * Gets a list of all the client's modules that fulfill a specified condition
	 * @param predicate The specified condition
	 * @return List of all the client's modules that fulfill the specified condition
	 */
	public List<Module> getModules(Predicate<? super Module> predicate) {
		return modules.stream()
				.filter(predicate)
				.collect(Collectors.toList());
	}

	/**
	 * Gets the first module that fulfills a specified condition
	 * @param predicate The specified condition
	 * @return The first module that fulfills the specified condition
	 */
	public Module getModule(Predicate<? super Module> predicate) {
		return modules.stream()
				.filter(predicate)
				.findFirst()
				.orElse(null);
	}
}
