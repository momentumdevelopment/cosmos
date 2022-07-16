package cope.cosmos.util.combat;

import cope.cosmos.util.Wrapper;
import net.minecraft.world.World;

/**
 * Calculates general damages
 * @author aesthetical, linustouchtips
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

        // scale damage based on difficulty
        switch (mc.world.getDifficulty()) {
            case PEACEFUL:
                return 0;
            case EASY:
                return Math.min(damage / 2.0F + 1.0F, damage);
            case NORMAL:
            default:
                return damage;
            case HARD:
                return damage * 3.0F / 2.0F;
        }
    }

    /**
     * Checks whether the player can actually take damage
     * @return Whether the player can actually take damage
     */
    public static boolean canTakeDamage() {
        return !mc.player.capabilities.isCreativeMode;
    }
}
