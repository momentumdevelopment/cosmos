package cope.cosmos.client.features.modules.misc;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.ModuleToggleEvent;
import cope.cosmos.client.events.TotemPopEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.event.annotation.Subscription;
import cope.cosmos.util.client.ChatUtil;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@SuppressWarnings("unused")
public class Notifier extends Module {
    public static Notifier INSTANCE;

    public Notifier() {
        super("Notifier", Category.MISC, "Sends notifications in chat");
        INSTANCE = this;
    }

    public static Setting<Boolean> enableNotify = new Setting<>("EnableNotify", false).setDescription("Send a chat message when a modules is toggled");
    public static Setting<Boolean> popNotify = new Setting<>("PopNotify", false).setDescription("Send a chat message when a nearby player is popped");

    @Subscription
    public void onTotemPop(TotemPopEvent event) {
        if (mc.player.getDistance(event.getPopEntity()) < 10 && popNotify.getValue()) {
            ChatUtil.sendMessage(TextFormatting.DARK_PURPLE + event.getPopEntity().getName() + TextFormatting.RESET + " has popped " + Cosmos.INSTANCE.getPopManager().getTotemPops(event.getPopEntity()) + " totems!");
        }
    }

    @Subscription
    public void onModuleEnable(ModuleToggleEvent.ModuleEnableEvent event) {
        if (enableNotify.getValue() && event.getModule().getCategory() != Category.HIDDEN) {
            ChatUtil.sendModuleEnableMessage(event.getModule());
        }
    }

    @Subscription
    public void onModuleDisable(ModuleToggleEvent.ModuleDisableEvent event) {
        if (enableNotify.getValue() && event.getModule().getCategory() != Category.HIDDEN) {
            ChatUtil.sendModuleDisableMessage(event.getModule());
        }
    }
}
