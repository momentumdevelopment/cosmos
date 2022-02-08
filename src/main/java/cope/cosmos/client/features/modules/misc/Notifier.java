package cope.cosmos.client.features.modules.misc;

import cope.cosmos.client.events.client.ModuleToggleEvent;
import cope.cosmos.client.events.combat.TotemPopEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
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
                getCosmos().getChatManager().sendMessage(TextFormatting.DARK_PURPLE + "[Cosmos] " + TextFormatting.RESET + popMessage);
            }
        }
    }

    @SubscribeEvent
    public void onModuleEnable(ModuleToggleEvent.ModuleEnableEvent event) {
        if (enableNotify.getValue()) {
            // make sure the module isn't hidden
            if (!event.getModule().getCategory().equals(Category.HIDDEN)) {

                // send an enable notification
                getCosmos().getChatManager().sendClientMessage(event.getModule());
            }
        }
    }

    @SubscribeEvent
    public void onModuleDisable(ModuleToggleEvent.ModuleDisableEvent event) {
        if (enableNotify.getValue()) {
            // make sure the module isn't hidden
            if (!event.getModule().getCategory().equals(Category.HIDDEN)) {

                // send an disable notification
                getCosmos().getChatManager().sendClientMessage(event.getModule());
            }
        }
    }
}
