package cope.cosmos.client.features.modules.visual;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

public class FullBright extends Module {
    public static FullBright INSTANCE;

    public FullBright() {
        super("FullBright", Category.VISUAL, "Brightens up the world");
        INSTANCE = this;
    }

    float previousBright;

    @Override
    public void onEnable() {
        super.onEnable();

        previousBright = mc.gameSettings.gammaSetting;

        // apply brightness
        mc.player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION.setPotionName("FullBright"), 80950, 1, false, false));
        mc.gameSettings.gammaSetting = 100;
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // remove brightness
        mc.player.removePotionEffect(MobEffects.NIGHT_VISION);
        mc.gameSettings.gammaSetting = previousBright;
    }
}
