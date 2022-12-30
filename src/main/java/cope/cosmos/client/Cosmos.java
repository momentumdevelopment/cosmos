package cope.cosmos.client;

import cope.cosmos.client.features.Feature;
import cope.cosmos.client.features.modules.client.ColorsModule;
import cope.cosmos.client.features.modules.client.DiscordPresenceModule;
import cope.cosmos.client.features.modules.client.FontModule;
import cope.cosmos.client.features.modules.client.SocialModule;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.client.manager.managers.*;
import cope.cosmos.client.ui.clickgui.ClickGUIScreen;
import cope.cosmos.client.ui.tabgui.TabGUI;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.lwjgl.opengl.Display;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author bon55, linustouchtips
 * @since 05/05/2021
 */
@Mod(modid = Cosmos.MOD_ID, name = Cosmos.NAME, version = Cosmos.VERSION, acceptedMinecraftVersions = "[1.12.2]")
public class Cosmos {

    // client instance
    @Mod.Instance
    public static Cosmos INSTANCE;

    // mod info
    public static final String MOD_ID = "cosmos";
    public static final String NAME = "Cosmos";
    public static final String VERSION = "1.5.0-beta";
    public static final ClientType CLIENT_TYPE = ClientType.DEVELOPMENT;

    // client event bus
    public static EventBus EVENT_BUS = MinecraftForge.EVENT_BUS;

    // the client's command prefix
    public static String PREFIX = "*";

    // tracks whether or not the client has already run for the first time
    public static boolean SETUP = false;

    // start up time
    private long startupTime;

    // the client gui
    private ClickGUIScreen clickGUI;

    // The client's TabGUI
    private TabGUI tabGUI;

    // list of managers
    private final List<Manager> managers = new ArrayList<>();

    // all client managers
    private ModuleManager moduleManager;
    private CommandManager commandManager;
    private EventManager eventManager;
    private TickManager tickManager;
    private SocialManager socialManager;
    private AltManager altManager;
    private ConfigManager configManager;
    private RotationManager rotationManager;
    private ThreadManager threadManager;
    private HoleManager holeManager;
    private FontManager fontManager;
    private NotificationManager notificationManager;
    private ReloadManager reloadManager;
    private PatchManager patchManager;
    private TotemManager totemManager;
    private InteractionManager interactionManager;
    private InventoryManager inventoryManager;
    private ItemManager itemManager;
    private ChangelogManager changelogManager;
    private SoundManager soundManager;
    private ChatManager chatManager;
    private PotionManager potionManager;
    private CapeManager capeManager;
    private WaypointManager waypointManager;
    
    public Cosmos() {
    	INSTANCE = this;
    }
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        // just started to initialize
        startupTime = System.currentTimeMillis();

        // load fonts
        fontManager = new FontManager();
        managers.add(fontManager);
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        // Progress Manager
        ProgressManager.ProgressBar progressManager = ProgressManager.push("Cosmos", 24);

        // load the modules
        moduleManager = new ModuleManager();
        managers.add(moduleManager);
        progressManager.step("[Cosmos] Loading Modules");

        // Register the event manager
        eventManager = new EventManager();
        managers.add(eventManager);
        progressManager.step("[Cosmos] Registering Events");

        // load the commands
        commandManager = new CommandManager();
        managers.add(commandManager);
        progressManager.step("[Cosmos] Loading Commands");

        // sets up the tick manager
        tickManager = new TickManager();
        managers.add(tickManager);
        progressManager.step("[Cosmos] Setting up Tick Manager");

        // sets up the rotation manager
        rotationManager = new RotationManager();
        managers.add(rotationManager);
        progressManager.step("[Cosmos] Setting up Rotation Manager");

        // sets up the social manager
        socialManager = new SocialManager();
        managers.add(socialManager);
        progressManager.step("[Cosmos] Setting up Social Manager");

        // sets up the alt manager
        altManager = new AltManager();
        managers.add(altManager);
        progressManager.step("[Cosmos] Setting up Alt Manager");

        // sets up the preset manager
        configManager = new ConfigManager();
        managers.add(configManager);
        progressManager.step("[Cosmos] Setting up Config Manager");

        // sets up the GUI
        clickGUI = new ClickGUIScreen();
        tabGUI = new TabGUI();
        progressManager.step("[Cosmos] Setting up GUI's");

        // This needs to be after the GUIs are loaded, as it needs to be able to set the GUI frames' positions and sizes
        configManager.loadGUI();

        // sets up the reload manager
        reloadManager = new ReloadManager();
        managers.add(reloadManager);
        progressManager.step("[Cosmos] Setting up Reload Manager");

        // sets up the notification manager
        notificationManager = new NotificationManager();
        managers.add(notificationManager);
        progressManager.step("[Cosmos] Setting up Notification Manager");

        // sets up the patch manager
        patchManager = new PatchManager();
        managers.add(patchManager);
        progressManager.step("[Cosmos] Setting up Patch Helper");

        // sets up the pop manager
        totemManager = new TotemManager();
        managers.add(totemManager);
        progressManager.step("[Cosmos] Setting up Pop Manager");

        // sets up the thread manager
        threadManager = new ThreadManager();
        managers.add(threadManager);
        progressManager.step("[Cosmos] Setting up Threads");

        // sets up the hole manager
        holeManager = new HoleManager();
        managers.add(holeManager);
        progressManager.step("[Cosmos] Setting up Hole Manager");

        // sets up the interaction manager
        interactionManager = new InteractionManager();
        managers.add(interactionManager);
        progressManager.step("[Cosmos] Setting up Interaction Manager");

        // sets up the inventory manager
        inventoryManager = new InventoryManager();
        managers.add(inventoryManager);
        progressManager.step("[Cosmos] Setting up Inventory Manager");

        // sets up the inventory manager
        // itemManager = new ItemManager();
        // managers.add(itemManager);
        progressManager.step("[Cosmos] Setting up Item Manager");

        // sets up the changelog manager
        changelogManager = new ChangelogManager();
        managers.add(changelogManager);
        progressManager.step("[Cosmos] Setting up Changelog Manager");

        // sets up the sound manager
        soundManager = new SoundManager();
        managers.add(soundManager);
        progressManager.step("[Cosmos] Setting up Sound System");

        // sets up the sound manager
        chatManager = new ChatManager();
        managers.add(chatManager);
        progressManager.step("[Cosmos] Setting up Chat Manager");

        // sets up the potion manager
        potionManager = new PotionManager();
        managers.add(potionManager);
        progressManager.step("[Cosmos] Setting up Potion Manager");

        // sets up the cape manager
        capeManager = new CapeManager();
        managers.add(capeManager);
        progressManager.step("[Cosmos] Setting up Cape Manager");

        // sets up the waypoint manager
        waypointManager = new WaypointManager();
        managers.add(waypointManager);
        progressManager.step("[Cosmos] Setting up Waypoint Manager");

        ProgressManager.pop(progressManager);
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        Display.setTitle(NAME + " " + VERSION);

        // start the discord presence on startup
        PresenceManager.startPresence();

        // we've run the client for the first time
        SETUP = true;

        // client is done starting
        long initializeTime = System.currentTimeMillis() - startupTime;
        System.out.println("[Cosmos] Initialized in " + initializeTime + "ms!");
    }

    /**
     * Gets the Window GUI screen
     * @return The Window GUI screen
     */
    public ClickGUIScreen getClickGUI() {
        return clickGUI;
    }

    /**
     * Gets the TabGUI
     * @return The Tab GUI
     */
    public TabGUI getTabGUI() {
        return tabGUI;
    }

    /**
     * Gets the client module manager
     * @return The client module manager
     */
    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    /**
     * Gets the client command manager
     * @return The client command manager
     */
    public CommandManager getCommandManager() {
        return commandManager;
    }

    /**
     * Gets the client event manager
     * @return The client event manager
     */
    public EventManager getEventManager() {
        return eventManager;
    }

    /**
     * Gets the client tick manager
     * @return The client tick manager
     */
    public TickManager getTickManager() {
        return tickManager;
    }

    /**
     * Gets the client social manager
     * @return The client social manager
     */
    public SocialManager getSocialManager() {
        return socialManager;
    }

    /**
     * Gets the client alt manager
     * @return The client alt manager
     */
    public AltManager getAltManager() {
        return altManager;
    }

    /**
     * Gets the client configuration manager
     * @return The client configuration manager
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Gets the client rotation manager
     * @return The client rotation manager
     */
    public RotationManager getRotationManager() {
        return rotationManager;
    }

    /**
     * Gets the client thread manager
     * @return The client thread manager
     */
    public ThreadManager getThreadManager() {
        return threadManager;
    }

    /**
     * Gets the client hole manager
     * @return The client hole manager
     */
    public HoleManager getHoleManager() {
        return holeManager;
    }

    /**
     * Gets the client font manager
     * @return The client font manager
     */
    public FontManager getFontManager() {
        return fontManager;
    }

    /**
     * Gets the client reload manager
     * @return The client reload manager
     */
    public ReloadManager getReloadManager() {
        return reloadManager;
    }

    /**
     * Gets the client patch manager
     * @return The client patch manager
     */
    public PatchManager getPatchManager() {
        return patchManager;
    }

    /**
     * Gets the client pop manager
     * @return The client pop manager
     */
    public TotemManager getPopManager() {
        return totemManager;
    }

    /**
     * Gets the client interaction manager
     * @return The client interaction manager
     */
    public InteractionManager getInteractionManager() {
        return interactionManager;
    }

    /**
     * Gets the client inventory manager
     * @return The client inventory manager
     */
    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    /**
     * Gets the client item manager
     * @return The client item manager
     */
    public ItemManager getItemManager() {
        return itemManager;
    }

    /**
     * Gets the client changelog manager
     * @return The client changelog manager
     */
    public ChangelogManager getChangelogManager() {
        return changelogManager;
    }

    /**
     * Gets the client sound manager
     * @return The client sound manager
     */
    public SoundManager getSoundManager() {
        return soundManager;
    }

    /**
     * Gets the client chat manager
     * @return The client chat manager
     */
    public ChatManager getChatManager() {
        return chatManager;
    }

    /**
     * Gets the client potion manager
     * @return The client potion manager
     */
    public PotionManager getPotionManager() {
        return potionManager;
    }

    /**
     * Gets the client cape manager
     * @return The client cape manager
     */
    public CapeManager getCapeManager() {
        return capeManager;
    }

    /**
     * Gets the client waypoint manager
     * @return The client waypoint manager
     */
    public WaypointManager getWaypointManager() {
        return waypointManager;
    }

    /**
     * Gets the client notification manager
     * @return The client notification manager
     */
    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    /**
     * Gets a list of the client managers
     * @return List of the client managers
     */
    public List<Manager> getAllManagers() {
        return managers;
    }

    /**
     * Gets a list of all the client managers that fulfill a specified condition
     * @param predicate The specified condition
     * @return List of all the client managers that fulfill the specified condition
     */
    public List<Manager> getManagers(Predicate<? super Manager> predicate) {
        return managers.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * Gets the first client managers that fulfills a specified condition
     * @param predicate The specified condition
     * @return The first client managers that fulfills the specified condition
     */
    public Manager getManager(Predicate<? super Manager> predicate) {
        return managers.stream()
                .filter(predicate)
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets a list of features that are safe to run while the world is null
     * @return List of features that are safe to run while the world is null
     */
    public List<Feature> getNullSafeFeatures() {
    	return Arrays.asList(
                DiscordPresenceModule.INSTANCE,
                ColorsModule.INSTANCE,
                FontModule.INSTANCE,
                SocialModule.INSTANCE,
                FontModule.INSTANCE
        );
    }


    public enum ClientType {

        /**
         * Released version of client
         */
        RELEASE(""),

        /**
         * Beta release of client for testing
         */
        BETA("-beta"),

        /**
         * Development version of client
         */
        DEVELOPMENT("-dev");

        // version tag
        private final String tag;

        ClientType(String tag) {
            this.tag = tag;
        }

        /**
         * Gets the tag
         * @return The tag
         */
        @Override
        public String toString() {
            return tag;
        }
    }
}
