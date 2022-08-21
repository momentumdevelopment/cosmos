package cope.cosmos.client.features.modules.misc;

import cope.cosmos.client.events.client.ModuleToggleEvent;
import cope.cosmos.client.events.combat.TotemPopEvent;
import cope.cosmos.client.events.entity.EntityWorldEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.chat.ChatUtil;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips
 * @since 08/22/2021
 */
public class NotifierModule extends Module {
    public static NotifierModule INSTANCE;

    public NotifierModule() {
        super("Notifier", Category.MISC, "Sends notifications in chat");
        INSTANCE = this;
    }

    // **************************** general settings ****************************

    public static Setting<Boolean> enableNotify = new Setting<>("EnableNotify", false)
            .setDescription("Send a chat message when a modules is toggled");

    public static Setting<Boolean> popNotify = new Setting<>("PopNotify", false)
            .setDescription("Send a chat message when a nearby player is popped");

    @SubscribeEvent
    public void onTotemPop(TotemPopEvent event) {
        if (popNotify.getValue()) {

            // if the player is in range
            if (mc.player.getDistance(event.getPopEntity()) < 10) {

                // formatted message for the pop notification
                String popMessage = TextFormatting.DARK_PURPLE + event.getPopEntity().getName() + TextFormatting.RESET + " has popped " + getCosmos().getPopManager().getTotemPops(event.getPopEntity()) + " totems!";

                // send notification
                getCosmos().getChatManager().sendClientMessage(popMessage);
            }
        }
    }

    @SubscribeEvent
    public void onEntityRemove(EntityWorldEvent.EntityRemoveEvent event) {
        if (getCosmos().getPopManager().getTotemPops(event.getEntity()) > 0) {

            // notify the player if necessary
            if (popNotify.getValue()) {
                getCosmos().getChatManager().sendClientMessage(TextFormatting.DARK_PURPLE + event.getEntity().getName() + TextFormatting.RESET + " died after popping " + getCosmos().getPopManager().getTotemPops(event.getEntity()) + " totems!");
            }

            // remove the totem info associated with the entity
            getCosmos().getPopManager().removePops(event.getEntity());
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
