package cope.cosmos.client.events.combat;

import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called when a player deals a critical hit
 * @author linustouchtips
 * @since 12/09/2021
 */
public class CriticalModifierEvent extends Event {

    private float damageModifier = 1.5F;

    /**
     * Sets the critical damage modifier
     * @param in The new critical damage modifier
     */
    public void setDamageModifier(float in) {
        damageModifier = in;
    }

    /**
     * Gets the critical damage modifier
     * @return The critical damage modifier
     */
    public float getDamageModifier() {
        return damageModifier;
    }
}
