package cope.cosmos.client.events.entity;

import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called when a potion effect is added to an entity
 * @author linustouchtips
 * @since 01/10/2022
 */
@Cancelable
public class PotionEvent extends Event {

    private final PotionEffect potionEffect;

    public PotionEvent(PotionEffect potionEffect) {
        this.potionEffect = potionEffect;
    }

    /**
     * Gets the added potion effect
     * @return The added potion effect
     */
    public PotionEffect getPotionEffect() {
        return potionEffect;
    }
}
