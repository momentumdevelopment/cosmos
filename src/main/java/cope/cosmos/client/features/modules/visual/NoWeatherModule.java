package cope.cosmos.client.features.modules.visual;

import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.string.StringFormatter;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips
 * @since 02/19/2022
 */
public class NoWeatherModule extends Module {
    public static NoWeatherModule INSTANCE;

    public NoWeatherModule() {
        super("NoWeather", Category.VISUAL, "Allows you to change the weather", () -> StringFormatter.formatEnum(weather.getValue()));
        INSTANCE = this;
    }

    // **************************** general ****************************

    public static Setting<Weather> weather = new Setting<>("Weather", Weather.CLEAR)
            .setDescription("Sets the world's weather");

    public static Setting<Double> time = new Setting<>("Time", 0.0, 6000.0, 24000.0, 0)
            .setDescription("Sets the world's time");

    @Override
    public void onUpdate() {

        // update weather
        mc.world.setRainStrength(weather.getValue().getWeatherID());

        // update time
        mc.world.setWorldTime(time.getValue().longValue());
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {

        // packet for world time updates
        if (event.getPacket() instanceof SPacketTimeUpdate) {

            // cancel time updates, use module world time
            event.setCanceled(true);
        }
    }

    public enum Weather {

        /**
         * No rain
         */
        CLEAR(0),

        /**
         * Rain
         */
        RAIN(1),

        /**
         * Rain and thunder
         */
        THUNDER(2);

        // weather id
        private final int id;

        Weather(int id) {
            this.id = id;
        }

        /**
         * Gets the weather identifier
         * @return the weather identifier
         */
        public int getWeatherID() {
            return id;
        }
    }
}
