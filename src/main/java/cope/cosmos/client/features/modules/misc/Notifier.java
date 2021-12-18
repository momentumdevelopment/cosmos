package cope.cosmos.client.features.modules.misc;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.ModuleToggleEvent;
import cope.cosmos.client.events.TotemPopEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.client.ChatUtil;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips
 * @since 08/22/2021
 */
public class Notifier extends Module {
    public static Notifier INSTANCE;

    public Notifier() {
        super("Notifier", Category.MISC, "Sends notifications in chat");
        INSTANCE = this;
    }

    public static Setting<Boolean> enableNotify = new Setting<>("EnableNotify", false).setDescription("Send a chat message when a modules is toggled");
    public static Setting<Boolean> popNotify = new Setting<>("PopNotify", false).setDescription("Send a chat message when a nearby player is popped");

    @SubscribeEvent
    public void onTotemPop(TotemPopEvent event) {
        if (popNotify.getValue()) {
            // if the player is in range
            if (mc.player.getDistance(event.getPopEntity()) < 10) {

                // formatted message for the pop notification
                String popMessage = TextFormatting.DARK_PURPLE + event.getPopEntity().getName() + TextFormatting.RESET + " has popped " + getCosmos().getPopManager().getTotemPops(event.getPopEntity()) + " totems!";

                // send notification
                ChatUtil.sendMessageWithOptionalDeletion(TextFormatting.DARK_PURPLE + "[Cosmos] " + TextFormatting.RESET + popMessage, 101);
            }
        }
    }

    @SubscribeEvent
    public void onModuleEnable(ModuleToggleEvent.ModuleEnableEvent event) {
        if (enableNotify.getValue()) {
            // make sure the module isn't hidden
            if (!event.getModule().getCategory().equals(Category.HIDDEN)) {

                // send an enable notification
                ChatUtil.sendModuleEnableMessage(event.getModule());
            }
        }
    }

    @SubscribeEvent
    public void onModuleDisable(ModuleToggleEvent.ModuleDisableEvent event) {
        if (enableNotify.getValue()) {
            // make sure the module isn't hidden
            if (!event.getModule().getCategory().equals(Category.HIDDEN)) {

                // send an disable notification
                ChatUtil.sendModuleDisableMessage(event.getModule());
            }
        }
    }
}
