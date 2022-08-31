package cope.cosmos.client.features.modules.client;

import cope.cosmos.client.events.render.gui.TabOverlayEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.PersistentModule;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.SocialManager.Relationship;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips
 * @since 06/08/2021
 */
public class SocialModule extends PersistentModule {
    public static SocialModule INSTANCE;

    public SocialModule() {
        super("Social", Category.CLIENT, "Allows the social system to function");
        INSTANCE = this;

        setExempt(true);
        setDrawn(false);
    }

    // **************************** general ****************************

    public static Setting<Boolean> friends = new Setting<>("Friends", true)
            .setDescription("Allow friends system to function");

    @SubscribeEvent
    public void onTabOverlay(TabOverlayEvent event) {
        if (friends.getValue()) {

            // friend found in tab
            if (getCosmos().getSocialManager().getSocial(event.getInformation()).equals(Relationship.FRIEND)) {

                // highlight friend names in the tab
                event.setCanceled(true);
                event.setInformation(TextFormatting.AQUA + event.getInformation());
            }
        }
    }
}
