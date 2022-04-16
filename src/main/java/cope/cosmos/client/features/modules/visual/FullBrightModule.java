package cope.cosmos.client.features.modules.visual;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

/**
 * @author linustouchtips
 * @since 07/21/2021
 */
public class FullBrightModule extends Module {
    public static FullBrightModule INSTANCE;

    public FullBrightModule() {
        super("FullBright", Category.VISUAL, "Brightens up the world");
        INSTANCE = this;
    }

    // previous brightness info
    private float previousBright;
    private int previousNightVision;

    @Override
    public void onEnable() {
        super.onEnable();

        // save previous brightness
        previousBright = mc.gameSettings.gammaSetting;

        // save previous night vision
        if (mc.player.isPotionActive(MobEffects.NIGHT_VISION)) {
            previousNightVision = mc.player.getActivePotionEffect(MobEffects.NIGHT_VISION).getDuration();
        }

        // apply brightness
        mc.gameSettings.gammaSetting = 100;
    }

    @Override
    public void onUpdate() {

        // apply night vision potion effect
        mc.player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION.setPotionName("FullBright"), 80950, 1, false, false));
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // remove night vision
        mc.player.removePotionEffect(MobEffects.NIGHT_VISION);

        // reapply previous night vision
        if (previousNightVision > 0) {
            mc.player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, previousNightVision));
        }

        // reset brightness
        mc.gameSettings.gammaSetting = previousBright;
    }
}
