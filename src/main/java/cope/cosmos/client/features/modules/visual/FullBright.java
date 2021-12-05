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

    // old brightness configuration
    private float previousBright;
    private int previousNightVision;

    @Override
    public void onEnable() {
        super.onEnable();

        // save old night vision effect
        if (mc.player.isPotionActive(MobEffects.NIGHT_VISION)) {
            previousNightVision = mc.player.getActivePotionEffect(MobEffects.NIGHT_VISION).getDuration();
        }

        // save old brightness & apply brightness
        previousBright = mc.gameSettings.gammaSetting;
        mc.gameSettings.gammaSetting = 100;
    }

    @Override
    public void onUpdate() {
        // apply night vision effect
        mc.player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION.setPotionName("FullBright"), 80950, 1, false, false));
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // remove night vision effect
        if (mc.player.isPotionActive(MobEffects.NIGHT_VISION)) {
            mc.player.removePotionEffect(MobEffects.NIGHT_VISION);
        }

        if (previousNightVision > 0) {
            // reapply old night vision
            mc.player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, previousNightVision));
        }

        // restore old brightness
        mc.gameSettings.gammaSetting = previousBright;
    }
}
