package cope.cosmos.client.features.modules.client;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.TabOverlayEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.SocialManager.Relationship;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Social extends Module {
    public static Social INSTANCE;

    public Social() {
        super("Social", Category.CLIENT, "Allows the social system to function");
        INSTANCE = this;
        setExempt(true);
        setDrawn(false);

        // enable by default
        enable();
    }

    public static Setting<Boolean> friends = new Setting<>("Friends", true).setDescription("Allow friends system to function");

    @SubscribeEvent
    public void onTabOverlay(TabOverlayEvent event) {
        if (nullCheck() && Cosmos.INSTANCE.getSocialManager().getSocial(event.getInformation()).equals(Relationship.FRIEND) && friends.getValue()) {
            event.setCanceled(true);
            event.setInformation(TextFormatting.AQUA + event.getInformation());
        }
    }
}
