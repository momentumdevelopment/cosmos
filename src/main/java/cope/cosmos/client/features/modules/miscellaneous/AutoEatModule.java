package cope.cosmos.client.features.modules.miscellaneous;

import cope.cosmos.asm.mixins.accessor.IMinecraft;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.combat.AuraModule;
import cope.cosmos.client.features.modules.combat.AutoCrystalModule;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.InventoryManager.*;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemChorusFruit;
import net.minecraft.item.ItemFishFood;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.FoodStats;

/**
 * @author linustouchtips
 * @since 10/12/2022
 */
public class AutoEatModule extends Module {
    public static AutoEatModule INSTANCE;

    public AutoEatModule() {
        super("AutoEat", Category.MISCELLANEOUS, "Automatically eats when hunger is low");
        INSTANCE = this;
    }

    // **************************** general ****************************

    public static Setting<Double> hunger = new Setting<>("Hunger",  0.0, 10.0, 20.0, 1)
            .setDescription("Hunger level");

    // item info
    private int previousSlot = -1;
    private boolean eat;

    @Override
    public void onDisable() {
        super.onDisable();

        // stop eating
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);

        // reset
        previousSlot = -1;
        eat = false;
    }

    @Override
    public void onTick() {

        // combat modules have priority
        if (AutoCrystalModule.INSTANCE.isActive() || AuraModule.INSTANCE.isActive()) {
            return;
        }

        // no longer eating
        if (eat && !mc.player.isHandActive()) {

            // switch back
            if (previousSlot != -1) {
                getCosmos().getInventoryManager().switchToSlot(previousSlot, Switch.NORMAL);
                previousSlot = -1;
            }

            // no longer eating
            eat = false;
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
            return;
        }

        // already eating
        if (eat) {
            return;
        }

        // player food stats
        FoodStats foodStats = mc.player.getFoodStats();

        // hunger is lower than specified level
        if (foodStats.getFoodLevel() <= hunger.getValue()) {

            // find best food
            int food = getFood();

            // check if food exists
            if (food != -1) {

                // offhand food
                if (food == -9) {

                    // set hand to offhand
                    mc.player.setActiveHand(EnumHand.OFF_HAND);
                }

                // hotbar food
                else {

                    // mark previous slot
                    previousSlot = mc.player.inventory.currentItem;

                    // switch to food
                    getCosmos().getInventoryManager().switchToSlot(food, Switch.NORMAL);
                }

                // eat
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), true);
                ((IMinecraft) mc).hookRightClickMouse();
            }
        }
    }

    /**
     * Gets the best food in the hotbar
     * @return The best food in the hotbar
     */
    public int getFood() {

        // slot of the food
        int food = -1;
        int level = -1;

        // search hotbar
        for (int i = 0; i < 9; i++) {

            // item
            ItemStack item = mc.player.inventory.getStackInSlot(i);

            // item is food
            if (item.getItem() instanceof ItemFood) {

                // do not eat pufferfish
                if (item.getItem() instanceof ItemFishFood && ItemFishFood.FishType.byItemStack(item).equals(ItemFishFood.FishType.PUFFERFISH)) {
                    continue;
                }

                // do not eat chorus fruit
                if (item.getItem() instanceof ItemChorusFruit) {
                    continue;
                }

                // hunger heal amount
                int heal = ((ItemFood) item.getItem()).getHealAmount(item);

                // check if it is the best food
                if (heal > level) {

                    // update food
                    food = i;
                    level = heal;
                }
            }
        }

        // offhand item
        ItemStack offhand = mc.player.getHeldItemOffhand();

        // offhand item is food
        if (offhand.getItem() instanceof ItemFood) {

            // hunger heal amount
            int heal = ((ItemFood) offhand.getItem()).getHealAmount(offhand);

            // do not eat pufferfish
            if (offhand.getItem() instanceof ItemFishFood && ItemFishFood.FishType.byItemStack(offhand).equals(ItemFishFood.FishType.PUFFERFISH)) {
                return food;
            }

            // do not eat chorus fruit
            if (offhand.getItem() instanceof ItemChorusFruit) {
                return food;
            }

            // check if it is the best food
            if (heal > level) {

                // update food
                food = -9;
            }
        }

        return food;
    }
}
