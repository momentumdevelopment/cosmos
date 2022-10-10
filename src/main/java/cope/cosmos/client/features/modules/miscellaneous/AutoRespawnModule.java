package cope.cosmos.client.features.modules.miscellaneous;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.combat.EnemyUtil;
import cope.cosmos.util.math.Timer;
import cope.cosmos.util.math.Timer.Format;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author aesthetical
 * @since 06/20/2022
 */
public class AutoRespawnModule extends Module {
    public static AutoRespawnModule INSTANCE;

    public AutoRespawnModule() {
        super("AutoRespawn", Category.MISCELLANEOUS, "Automatically respawns you");
        INSTANCE = this;
    }

    // **************************** general ****************************

    public static Setting<Double> delay = new Setting<>("Delay", 0.0, 0.5, 5.0, 1)
            .setDescription("The delay in seconds to hold off sending a respawn packet");

    // timers
    private final Timer respawnTimer = new Timer();
    private boolean awaitRespawn = false;

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {

        // we have just died and the respawn screen has been displayed
        if (event.getGui() instanceof GuiGameOver) {
            respawnTimer.resetTime();
            awaitRespawn = true;
        }
    }

    @Override
    public void onTick() {

        // player is dead
        if (EnemyUtil.isDead(mc.player)) {

            // wait for respawn screen
            if (awaitRespawn) {

                // passed our respawn delay
                if (respawnTimer.passedTime((long) (delay.getValue() * 1000), Format.MILLISECONDS)) {

                    // respawn player
                    mc.player.respawnPlayer();
                    awaitRespawn = false;
                }
            }
        }
    }
}
