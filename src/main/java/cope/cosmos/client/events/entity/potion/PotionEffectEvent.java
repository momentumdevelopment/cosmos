package cope.cosmos.client.events.entity.potion;

import cope.cosmos.util.Wrapper;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called when a potion effect is added/removed to the player
 * @author linustouchtips
 * @since 04/19/2022
 */
@Cancelable
public class PotionEffectEvent extends Event implements Wrapper {

    // added/removed potion
    private final PotionEffect potionEffect;

    public PotionEffectEvent(PotionEffect potionEffect) {
        this.potionEffect = potionEffect;
    }

    /**
     * Gets the added/removed potion effect
     * @return The added/removed potion effect
     */
    public PotionEffect getPotionEffect() {
        return potionEffect;
    }

    public static class PotionAdd extends PotionEffectEvent {
        public PotionAdd(PotionEffect potionEffect) {
            super(potionEffect);
        }
    }

    public static class PotionRemove extends PotionEffectEvent {

        // Remove uses Potion instead of PotionEffect, why you ask, IDFK
        private final Potion potion;

        public PotionRemove(Potion potion) {
            super(mc.player.getActivePotionEffect(potion));
            this.potion = potion;
        }

        public PotionRemove(PotionEffect potionEffect) {
            super(potionEffect);
            this.potion = potionEffect.getPotion();
        }

        /**
         * Gets the removed potion
         * @return The removed potion
         */
        public Potion getPotion() {
            return potion;
        }
    }
}
