package cope.cosmos.client.features.modules.world;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.manager.managers.InventoryManager.Switch;

/**
 * @author linustouchtips, aesthetical
 * @since 09/06/2022
 */
public class ScaffoldModule extends Module {
    public static ScaffoldModule INSTANCE;

    public ScaffoldModule() {
        super("Scaffold", new String[] {"BlockFly"}, Category.WORLD, "Places blocks under you");
        INSTANCE = this;
    }

    public enum Swap {

        /**
         * If to not swap to any block and rely on the player to swap to the block themselves
         */
        NONE(Switch.NONE),

        /**
         * If to use packet swapping
         */
        PACKET(Switch.PACKET),

        /**
         * If to use client-sided swapping
         */
        NORMAL(Switch.NORMAL),

        /**
         * Useful on strict servers
         * NCP Updated has a check in scaffold for quick switches, which is flagged by swapping back and forth.
         * This will keep you on the block in your hotbar.
         */
        KEEP(Switch.NORMAL);

        private final Switch swap;

        Swap(Switch swap) {
            this.swap = swap;
        }
    }
}
