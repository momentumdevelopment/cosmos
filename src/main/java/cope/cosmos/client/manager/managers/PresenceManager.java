package cope.cosmos.client.manager.managers;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.features.modules.combat.AutoCrystalModule;
import cope.cosmos.client.manager.Manager;

import java.util.Random;

/**
 * @author linustouchtips, bon55
 * @since 06/08/2021
 */
public class PresenceManager extends Manager {

    // discord stuff
    private static final DiscordRPC discordPresence = DiscordRPC.INSTANCE;
    private static final DiscordRichPresence richPresence = new DiscordRichPresence();
    private static final DiscordEventHandlers presenceHandlers = new DiscordEventHandlers();

    // discord presence thread
    private static Thread presenceThread;

    public PresenceManager() {
        super("PresenceManager", "Manages the client Discord RPC");
    }

    // funy
    private static final String[] presenceDetails = {
            "Asking linus for config help",
            "Begging bon to make a new GUI",
            "Dogging on skids",
            "Taking the dogs on a walk",
            "Owning spawn",
            "Grooming ops",
            "Biggest player by weight",
            "Putting on femboy socks",
            "Sending CPacketDoTroll",
            "Removing konas from mods folder",
            "Installing trojan",
            "RIP CumbiaNarcos",
            "Playing on 2b2t.org, Packetflying at 630 kmh",
            "Regearing ...",
            "Nomming on some corn",
            "Selling cosmos vouches for discord nitro!",
            "Watching GrandOlive through his webcam",
            "Forcing PapaQuill to make packs",
            "Leaking Spartan's alts",
            "Deleting Lence's configs",
            "Backdooring impurity.me",
            "Cracking latest konas :yawn:",
            "Autoduping on eliteanarchy.org",
            "Tater",
            "I am a " + ((AutoCrystalModule.damage.getValue() < 4) ? "faggot." : "good config person :)"),
            "Injecting estrogen",
            "Releasing Pyro 1.5",
            "Stealing Quill's pyro account",
            "Releasing velocity",
            "Small amounts of tomfoolery",
            "Stealing future beta",
            "My game is going sicko mode",
            "Overdosing on crack cocaine",
            "Enjoying gondal.club",
            "Pasting phobos",
            "Crying and coping",
            "Sending credentials to webhook",
            "Dumping cosmos (215/331) classes",
            "Consuming soy products",
            "/killing ...",
            "FontRenderer extends ClassLoader",
            "Live-Action Role Playing",
            "Running Bruce client beta",
            "java.lang.NullPointerException",
            "Becoming overweight",
            "Ordering SevJ6 pizzas",
            "Putting 29 on the bin",
            "Caught exception from Cosmos",
            "SSing my address",
            "Thank you Ionar for SalHack!",
            "me.ionar.salhack",
            "candice",
            "tulips",
            "981 in queue",
            "Loading class -> FontRenderer.class",
            "Trashing 5 auto32k fags",
            "Packetflying on crystalpvp.cc",
            "Sending double the packets to do double the damage",
            "Sending shift packets",
            "Dueling in vanilla cpvp",
            "Larping about staircases",
            "Average cosmos user"
    };

    /**
     * Starts the discord presence
     */
    public static void startPresence() {

        // startup
        discordPresence.Discord_Initialize("832656395609440277", presenceHandlers, true, "");
        richPresence.startTimestamp = System.currentTimeMillis() / 1000L;
        discordPresence.Discord_UpdatePresence(richPresence);

        // run on new thread LOL
        presenceThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    richPresence.largeImageKey = "galaxy";
                    richPresence.largeImageText = Cosmos.NAME;
                    richPresence.smallImageKey = "cosmos";
                    richPresence.smallImageText = Cosmos.VERSION;
                    richPresence.details = mc.isIntegratedServerRunning() ? "SinglePlayer" : (mc.getCurrentServerData() != null ? mc.getCurrentServerData().serverIP.toLowerCase() : "Menus");
                    richPresence.state = presenceDetails[new Random().nextInt(presenceDetails.length - 1)];
                    discordPresence.Discord_UpdatePresence(richPresence);

                    // update every 3 seconds
                    Thread.sleep(3000);
                } catch (Exception ignored) {

                }
            }
        });

        presenceThread.start();
    }

    /**
     * Stops the discord presence
     */
    public static void interruptPresence() {
        if (presenceThread != null && !presenceThread.isInterrupted()) {
            presenceThread.interrupt();
        }

        // shutdown
        discordPresence.Discord_Shutdown();
        discordPresence.Discord_ClearPresence();
    }

    /**
     * Gets the presence details
     * @return The presence details
     */
    public static String[] getPresenceDetails() {
        return presenceDetails;
    }
}
