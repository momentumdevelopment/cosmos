package cope.cosmos.client.features.modules.visual;

import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips
 * @since 02/19/2022
 */
public class NoWeatherModule extends Module {
    public static NoWeatherModule INSTANCE;

    public NoWeatherModule() {
        super("NoWeather", Category.VISUAL, "Allows you to change the weather");
        INSTANCE = this;
    }

    public static Setting<Weather> weather = new Setting<>("Weather", Weather.CLEAR).setDescription("Sets the world's weather");
    public static Setting<Double> time = new Setting<>("Time", 0.0, 6000.0, 24000.0, 0).setDescription("Sets the world's time");

    @Override
    public void onUpdate() {
        // update weather
        switch (weather.getValue()) {
            case CLEAR:
                mc.world.setRainStrength(0);
                break;
            case RAIN:
                mc.world.setRainStrength(1);
                break;
            case THUNDER:
                mc.world.setRainStrength(2);
                break;
        }

        // update time
        mc.world.setWorldTime(time.getValue().longValue());
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {
        if (event.getPacket() instanceof SPacketTimeUpdate) {

            // cancel time updates
            event.setCanceled(true);
        }
    }

    public enum Weather {

        /**
         * No rain
         */
        CLEAR,

        /**
         * Rain
         */
        RAIN,

        /**
         * Rain and thunder
         */
        THUNDER
    }
}
