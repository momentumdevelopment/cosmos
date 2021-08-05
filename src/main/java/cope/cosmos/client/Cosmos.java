package cope.cosmos.client;

import com.mojang.brigadier.CommandDispatcher;
import cope.cosmos.client.clickgui.cosmos.CosmosGUI;
import cope.cosmos.client.clickgui.windowed.WindowGUI;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.manager.managers.*;
import cope.cosmos.util.render.FontUtil;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.lwjgl.opengl.Display;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
@Mod(modid = Cosmos.MOD_ID, name = Cosmos.NAME, version = Cosmos.VERSION, acceptedMinecraftVersions = "[1.12.2]")
public class Cosmos {

    public static final String MOD_ID = "cosmos";
    public static final String NAME = "Cosmos";
    public static final String VERSION = "1.0.1";

    public static String PREFIX = "*";
    public static boolean SETUP = false;
    
    @Mod.Instance
    public static Cosmos INSTANCE;

    private CosmosGUI cosmosGUI;
    private WindowGUI windowGUI;
    
    private TickManager tickManager;
    private SocialManager socialManager;
    private PresetManager presetManager;
    private RotationManager rotationManager;
    private ThreadManager threadManager;
    private FontManager fontManager;
    private NotificationManager notificationManager;
    private ReloadManager reloadManager;
    private SafetyHelperManager safetyHelperManager;
    private CommandDispatcher<Object> commandDispatcher;
    
    public Cosmos() {
    	INSTANCE = this;
    }
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        FontUtil.load();
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        ProgressManager.ProgressBar progressManager = ProgressManager.push("Cosmos", 11);

        MinecraftForge.EVENT_BUS.register(EventManager.INSTANCE);
        progressManager.step("Registering Events");

        commandDispatcher = new CommandDispatcher<>();
        CommandManager.registerCommands();
        progressManager.step("Loading Commands");

        tickManager = new TickManager();
        progressManager.step("Setting up Tick Manager");

        rotationManager = new RotationManager();
        progressManager.step("Setting up Rotation Manager");

        socialManager = new SocialManager();
        progressManager.step("Setting up Social Manager");

        presetManager = new PresetManager();
        progressManager.step("Setting up Config Manager");

        cosmosGUI = new CosmosGUI();
        windowGUI = new WindowGUI();
        progressManager.step("Setting up GUI's");

        reloadManager = new ReloadManager();
        progressManager.step("Setting up Reload Manager");

        notificationManager = new NotificationManager();
        progressManager.step("Setting up Notification Manager");

        safetyHelperManager = new SafetyHelperManager();
        progressManager.step("Setting up Safety Helper");

        threadManager = new ThreadManager();
        progressManager.step("Setting up Threads");

        ProgressManager.pop(progressManager);
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        Display.setTitle(NAME + " " + VERSION);

        PresenceManager.startPresence();
    }
    
    public CosmosGUI getCosmosGUI() {
    	return cosmosGUI;
    }
    
    public WindowGUI getWindowGUI() {
        return windowGUI;
    }

    public TickManager getTickManager() {
        return tickManager;
    }

    public SocialManager getSocialManager() {
        return socialManager;
    }

    public PresetManager getPresetManager() {
        return presetManager;
    }

    public FontManager getFontManager() {
        return fontManager;
    }

    public RotationManager getRotationManager() {
        return rotationManager;
    }

    public ThreadManager getThreadManager() {
        return threadManager;
    }

    public ReloadManager getReloadManager() {
        return reloadManager;
    }

    public SafetyHelperManager getSafetyHelperManager() {
        return safetyHelperManager;
    }

    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public CommandDispatcher<Object> getCommandDispatcher() {
        return commandDispatcher;
    }
    
    public List<Module> getNullSafeMods() {
    	return Arrays.asList(

        );
    }
}
