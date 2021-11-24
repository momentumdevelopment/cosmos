package cope.cosmos.client.features.modules.client;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.manager.managers.PresenceManager;
import cope.cosmos.util.client.ChatUtil;

public class DiscordPresence extends Module {
    public static DiscordPresence INSTANCE;

    public DiscordPresence() {
        super("DiscordPresence", Category.CLIENT, "Displays a custom presence on Discord");
        setDrawn(false);
        setExempt(true);
        enable();
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        PresenceManager.startPresence();
        ChatUtil.sendMessage("Starting Discord Presence!");
    }

    @Override
    public void onDisable() {
        super.onDisable();
        PresenceManager.interruptPresence();
        ChatUtil.sendMessage("Shutting down Discord Presence!");
    }
}
