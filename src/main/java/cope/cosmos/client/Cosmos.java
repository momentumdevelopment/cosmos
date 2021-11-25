package cope.cosmos.client;

import com.mojang.brigadier.CommandDispatcher;
import cope.cosmos.client.clickgui.cosmos.CosmosGUI;
import cope.cosmos.client.clickgui.windowed.WindowGUI;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.client.Colors;
import cope.cosmos.client.features.modules.client.DiscordPresence;
import cope.cosmos.client.features.modules.client.Font;
import cope.cosmos.client.features.modules.client.Social;
import cope.cosmos.client.manager.Manager;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
@Mod(modid = Cosmos.MOD_ID, name = Cosmos.NAME, version = Cosmos.VERSION, acceptedMinecraftVersions = "[1.12.2]")
public class Cosmos {

    public static final String MOD_ID = "cosmos";
    public static final String NAME = "Cosmos";
    public static final String VERSION = "1.1.0";

    public static String PREFIX = "*";
    public static boolean SETUP = false;
    
    @Mod.Instance
    public static Cosmos INSTANCE;

    private CosmosGUI cosmosGUI;
    private WindowGUI windowGUI;

    private final List<Manager> managers = new ArrayList<>();

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
    private ChangelogManager changelogManager;
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
        ProgressManager.ProgressBar progressManager = ProgressManager.push("Cosmos", 15);

        MinecraftForge.EVENT_BUS.register(EventManager.INSTANCE);
        progressManager.step("Registering Events");

        commandDispatcher = new CommandDispatcher<>();
        CommandManager.registerCommands();
        progressManager.step("Loading Commands");

        tickManager = new TickManager();
        managers.add(tickManager);
        progressManager.step("Setting up Tick Manager");

        rotationManager = new RotationManager();
        managers.add(rotationManager);
        progressManager.step("Setting up Rotation Manager");

        socialManager = new SocialManager();
        managers.add(socialManager);
        progressManager.step("Setting up Social Manager");

        presetManager = new PresetManager();
        managers.add(presetManager);
        progressManager.step("Setting up Config Manager");

        cosmosGUI = new CosmosGUI();
        windowGUI = new WindowGUI();
        progressManager.step("Setting up GUI's");

        reloadManager = new ReloadManager();
        managers.add(reloadManager);
        progressManager.step("Setting up Reload Manager");

        notificationManager = new NotificationManager();
        managers.add(notificationManager);
        progressManager.step("Setting up Notification Manager");

        patchManager = new PatchManager();
        managers.add(patchManager);
        progressManager.step("Setting up Patch Helper");

        popManager = new PopManager();
        managers.add(popManager);
        progressManager.step("Setting up Pop Manager");

        threadManager = new ThreadManager();
        managers.add(threadManager);
        progressManager.step("Setting up Threads");

        holeManager = new HoleManager();
        managers.add(holeManager);
        progressManager.step("Setting up Hole Manager");

        interactionManager = new InteractionManager();
        managers.add(interactionManager);
        progressManager.step("Setting up Interaction Manager");

        changelogManager = new ChangelogManager();
        managers.add(changelogManager);
        progressManager.step("Setting up Changelog Manager");

        ProgressManager.pop(progressManager);
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        Display.setTitle(NAME + " " + VERSION);

        // start the discord presence on startup
        PresenceManager.startPresence();
    }

    public List<Manager> getManagers() {
        return managers;
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

    public HoleManager getHoleManager() {
        return holeManager;
    }

    public ReloadManager getReloadManager() {
        return reloadManager;
    }

    public PatchManager getPatchManager() {
        return patchManager;
    }

    public PopManager getPopManager() {
        return popManager;
    }

    public InteractionManager getInteractionManager() {
        return interactionManager;
    }

    public ChangelogManager getChangelogManager() {
        return changelogManager;
    }

    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public CommandDispatcher<Object> getCommandDispatcher() {
        return commandDispatcher;
    }
    
    public List<Module> getNullSafeMods() {
    	return Arrays.asList(
                DiscordPresence.INSTANCE,
                Colors.INSTANCE,
                Font.INSTANCE,
                Social.INSTANCE
        );
    }
}
