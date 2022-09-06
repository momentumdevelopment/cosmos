package cope.cosmos.client.manager.managers;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.client.ui.util.animation.Animation;
import net.minecraft.potion.PotionEffect;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linustouchtips
 * @since 04/19/2022
 */
public class PotionManager extends Manager {

    // active potion effects
    private final Map<PotionEffect, Animation> activePotions = new ConcurrentHashMap<>();

    public PotionManager() {
        super("PotionManager", "Manages player's potion effects");

        // subscribe to event bus
        Cosmos.EVENT_BUS.register(this);
    }

    /*
    @Override
    public void onTick() {

        // clean
        activePotions.forEach((potionEffect, animation) -> {
            if (animation.getAnimationFactor() <= 0.05) {
                activePotions.remove(potionEffect);
            }
        });
    }

    @SubscribeEvent
    public void onPotionAdd(PotionEffectEvent.PotionAdd event) {

        // we'll manually add potions
        event.setCanceled(true);

        // active potion
        PotionEffect potioneffect = ((IEntityLivingBase) mc.player).getActivePotionMap().get(event.getPotionEffect().getPotion());

        // is active?
        if (potioneffect == null) {
            ((IEntityLivingBase) mc.player).getActivePotionMap().put(event.getPotionEffect().getPotion(), event.getPotionEffect());

            // anim
            activePotions.put(event.getPotionEffect(), new Animation(300, false));
            activePotions.get(event.getPotionEffect()).setState(true);

            // apply effect
            ((IEntityLivingBase) mc.player).hookOnNewPotionEffect(event.getPotionEffect());
        }

        // combine
        else {

            // combine potion effects
            potioneffect.combine(event.getPotionEffect());
            ((IEntityLivingBase) mc.player).hookOnChangedPotionEffect(potioneffect, true);
        }
    }

    @SubscribeEvent
    public void onPotionRemove(PotionEffectEvent.PotionRemove event) {

        // reverse anim
        activePotions.forEach((potionEffect, animation) -> {
            if (event.getPotionEffect().getEffectName().equals(potionEffect.getEffectName())) {
                animation.setState(false);
            }
        });

        // ??????
        event.setCanceled(false);
    }
     */

    /**
     * Gets the player's active potion effects
     * @return A map of the player's active potion effects
     */
    public Map<PotionEffect, Animation> getActivePotions() {
        return activePotions;
    }
}
