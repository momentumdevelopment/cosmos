package cope.cosmos.util.combat;

import cope.cosmos.util.Wrapper;
import net.minecraft.world.World;

/**
 * Calculates general damages
 *
 * @author aesthetical
 * @since 3/19/22
 */
public class DamageUtil implements Wrapper {

    /**
     * Gets the scaled damage based off of the difficulty
     * @param damage Damage done
     * @return The scaled damage based off of the difficulty
     */
    public static float getScaledDamage(float damage) {
        World world = mc.world;
        if (world == null) {
            return damage;
        }

        // damage scale
        int diff = world.getDifficulty().getDifficultyId();
        return damage * ((float) diff * 0.5f);
    }
}
