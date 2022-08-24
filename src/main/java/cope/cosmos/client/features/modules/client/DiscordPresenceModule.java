package cope.cosmos.client.features.modules.client;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.manager.managers.PresenceManager;

/**
 * @author linustouchtips
 * @since 07/01/2021
 */
public class DiscordPresenceModule extends Module {
    public static DiscordPresenceModule INSTANCE;

    public DiscordPresenceModule() {
        super("DiscordPresence", new String[] {"DiscordRPC", "RPC"}, Category.CLIENT, "Displays a custom presence on Discord");
        INSTANCE = this;

        setDrawn(false);
        setExempt(true);
        enable(true);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        // start presence
        PresenceManager.startPresence();
        
        // notify user
        if (nullCheck()) {
            getCosmos().getChatManager().sendClientMessage("Starting Discord Presence!");
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // end presence
        PresenceManager.interruptPresence();
        
        // notify user
        if (nullCheck()) {
            getCosmos().getChatManager().sendClientMessage("Shutting down Discord Presence!");
        }
    }
}
