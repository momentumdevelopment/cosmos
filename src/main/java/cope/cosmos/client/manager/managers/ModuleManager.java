package cope.cosmos.client.manager.managers;

import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.client.*;
import cope.cosmos.client.features.modules.combat.*;
import cope.cosmos.client.features.modules.exploits.*;
import cope.cosmos.client.features.modules.miscellaneous.*;
import cope.cosmos.client.features.modules.movement.*;
import cope.cosmos.client.features.modules.world.*;
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

				// combat
				new AuraModule(),
				new AutoArmorModule(),
				new AutoBowReleaseModule(),
				new AutoCrystalModule(),
				new AutoDisconnectModule(),
				new AutoTotemModule(),
				// new AutoTrapModule(),
				new CriticalsModule(),
				new HoleFillModule(),
				new ReplenishModule(),
				// new SelfBowModule(),
				new SelfFillModule(),
				new SurroundModule(),

				// exploits
				new AntiHungerModule(),
				new ChorusControlModule(),
				new ClickTPModule(),
				new FastProjectileModule(),
				new NewChunksModule(),
				new PacketFlightModule(),
				new PingSpoofModule(),
				new PortalModule(),
				new ReachModule(),
				new SwingModule(),

				// miscellaneous
				new AnnouncerModule(),
				new AntiAFKModule(),
				new AntiAimModule(),
				new AntiCrashModule(),
				new AutoEatModule(),
				new AutoFishModule(),
				new AutoRespawnModule(),
				new ChatModificationsModule(),
				new FakePlayerModule(),
				new MiddleClickModule(),
				new NotifierModule(),
				new SneakModule(),
				new TimerModule(),
				new XCarryModule(),

				// movement
				new BlinkModule(),
				new ElytraFlightModule(),
				new EntityControlModule(),
				new EntitySpeedModule(),
				new FastFallModule(),
				new FlightModule(),
				new HighJumpModule(),
				new JesusModule(),
				new LongJumpModule(),
				new NoFallModule(),
				new NoSlowModule(),
				new ParkourModule(),
				new SpeedModule(),
				new SprintModule(),
				new StepModule(),
				new TickShiftModule(),
				new VelocityModule(),
				new YawModule(),

				// visual
				new BlockHighlightModule(),
				new BreadcrumbsModule(),
				new BreakHighlightModule(),
				new CameraClipModule(),
				new ChamsModule(),
				new ESPModule(),
				new ExtraTabModule(),
				new FreecamModule(),
				new FullBrightModule(),
				new HoleESPModule(),
				new NametagsModule(),
				new NoRenderModule(),
				new NoRotateModule(),
				new NoWeatherModule(),
				new SkyboxModule(),
				new TooltipsModule(),
				new TracersModule(),
				new ViewModelModule(),
				new WaypointsModule(),

				// world
				new AvoidModule(),
				new FastUseModule(),
				new InteractModule(),
				new MultiTaskModule(),
				new ScaffoldModule(),
				new SpeedMineModule(),
				new WallhackModule(),

				// client
				new ClickGUIModule(),
				new ColorsModule(),
				new DiscordPresenceModule(),
				new FontModule(),
				new HUDModule(),
				new SocialModule(),
				new StreamerModeModule()
		);
	}

	@Override
	public void onThread() {

		// check all modules for potential keybind presses
		for (Module module : modules) {

			// if the keybind is pressed
			if (module.getBind().getValue().isPressed()) {

				// toggle the module
				module.toggle();
			}
		}
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
