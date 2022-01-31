package cope.cosmos.client;

import com.mojang.brigadier.CommandDispatcher;
import cope.cosmos.client.features.Feature;
import cope.cosmos.client.features.modules.client.Colors;
import cope.cosmos.client.features.modules.client.DiscordPresence;
import cope.cosmos.client.features.modules.client.Font;
import cope.cosmos.client.features.modules.client.Social;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.client.manager.managers.*;
import cope.cosmos.client.ui.clickgui.ClickGUIScreen;
import cope.cosmos.util.render.FontUtil;
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

/**
 * @author bon55, linustouchtips
 * @since 05/05/2021
 */
@SuppressWarnings("unused")
@Mod(modid = Cosmos.MOD_ID, name = Cosmos.NAME, version = Cosmos.VERSION, acceptedMinecraftVersions = "[1.12.2]")
public class Cosmos {

    // mod info
    public static final String MOD_ID = "cosmos";
    public static final String NAME = "Cosmos";
    public static final String VERSION = "1.2.0";

    // client event bus
    public static EventBus EVENT_BUS = MinecraftForge.EVENT_BUS;

    // the client's command prefix
    public static String PREFIX = "*";

    // tracks whether or not the client has already run for the first time
    public static boolean SETUP = false;

    // client instance
    @Mod.Instance
    public static Cosmos INSTANCE;

    // the client gui
    private ClickGUIScreen clickGUI;

    // list of managers
    private final List<Manager> managers = new ArrayList<>();

    // all client managers
    private ModuleManager moduleManager;
    private CommandManager commandManager;
    private EventManager eventManager;
    private TickManager tickManager;
    private SocialManager socialManager;
    private PresetManager presetManager;
    private RotationManager rotationManager;
    private ThreadManager threadManager;
    private HoleManager holeManager;
    private FontManager fontManager;
    private NotificationManager notificationManager;
    private ReloadManager reloadManager;
    private PatchManager patchManager;
    private PopManager popManager;
    private InteractionManager interactionManager;
    private InventoryManager inventoryManager;
    private ChangelogManager changelogManager;
    private SoundManager soundManager;
    private ChatManager chatManager;
    private CommandDispatcher<Object> commandDispatcher;
    
    public Cosmos() {
    	INSTANCE = this;
    }
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // load the client custom font
        FontUtil.load();
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        // Progress Manager
        ProgressManager.ProgressBar progressManager = ProgressManager.push("Cosmos", 19);

        // load the modules
        moduleManager = new ModuleManager();
        managers.add(moduleManager);
        progressManager.step("Loading Modules");

        // Register the event manager
        eventManager = new EventManager();
        managers.add(eventManager);
        progressManager.step("Registering Events");

        // load the commands (Mojang's Brigadier )
        commandDispatcher = new CommandDispatcher<>();
        commandManager = new CommandManager();
        managers.add(commandManager);
        progressManager.step("Loading Commands");

        // sets up the tick manager
        tickManager = new TickManager();
        managers.add(tickManager);
        progressManager.step("Setting up Tick Manager");

        // sets up the rotation manager
        rotationManager = new RotationManager();
        managers.add(rotationManager);
        progressManager.step("Setting up Rotation Manager");

        // sets up the social manager
        socialManager = new SocialManager();
        managers.add(socialManager);
        progressManager.step("Setting up Social Manager");

        // sets up the preset manager
        presetManager = new PresetManager();
        managers.add(presetManager);
        progressManager.step("Setting up Config Manager");

        // sets up the GUI
        clickGUI = new ClickGUIScreen();
        progressManager.step("Setting up GUI's");

        // sets up the reload manager
        reloadManager = new ReloadManager();
        managers.add(reloadManager);
        progressManager.step("Setting up Reload Manager");

        // sets up the notification manager
        notificationManager = new NotificationManager();
        managers.add(notificationManager);
        progressManager.step("Setting up Notification Manager");

        // sets up the patch manager
        patchManager = new PatchManager();
        managers.add(patchManager);
        progressManager.step("Setting up Patch Helper");

        // sets up the pop manager
        popManager = new PopManager();
        managers.add(popManager);
        progressManager.step("Setting up Pop Manager");

        // sets up the thread manager
        threadManager = new ThreadManager();
        managers.add(threadManager);
        progressManager.step("Setting up Threads");

        // sets up the hole manager
        holeManager = new HoleManager();
        managers.add(holeManager);
        progressManager.step("Setting up Hole Manager");

        // sets up the interaction manager
        interactionManager = new InteractionManager();
        managers.add(interactionManager);
        progressManager.step("Setting up Interaction Manager");

        // sets up the inventory manager
        inventoryManager = new InventoryManager();
        managers.add(inventoryManager);
        progressManager.step("Setting up Inventory Manager");

        // sets up the changelog manager
        changelogManager = new ChangelogManager();
        managers.add(changelogManager);
        progressManager.step("Setting up Changelog Manager");

        // sets up the sound manager
        soundManager = new SoundManager();
        managers.add(soundManager);
        progressManager.step("Setting up Sound System");

        // sets up the sound manager
        chatManager = new ChatManager();
        managers.add(chatManager);
        progressManager.step("Setting up Chat Manager");

        ProgressManager.pop(progressManager);
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        Display.setTitle(NAME + " " + VERSION);

        // start the discord presence on startup
        PresenceManager.startPresence();
    }

    /**
     * Gets all client managers
     * @return List of client managers
     */
    public List<Manager> getManagers() {
        return managers;
    }

    /**
     * Gets the Window GUI screen
     * @return The Window GUI screen
     */
    public ClickGUIScreen getClickGUI() {
        return clickGUI;
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
     * Gets the client command dispatcher
     * @return The client command dispatcher
     */
    public CommandDispatcher<Object> getCommandDispatcher() {
        return commandDispatcher;
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
     * Gets the client configuration manager
     * @return The client configuration manager
     */
    public PresetManager getPresetManager() {
        return presetManager;
    }

    /**
     * Gets the client font manager
     * @return The client font manager
     */
    public FontManager getFontManager() {
        return fontManager;
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
    public PopManager getPopManager() {
        return popManager;
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
     * Gets the client notification manager
     * @return The client notification manager
     */
    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    /**
     * Gets a list of features that are safe to run while the world is null
     * @return List of features that are safe to run while the world is null
     */
    public List<Feature> getNullSafeFeatures() {
    	return Arrays.asList(
                DiscordPresence.INSTANCE,
                Colors.INSTANCE,
                Font.INSTANCE,
                Social.INSTANCE,
                Font.INSTANCE
        );
    }
}
