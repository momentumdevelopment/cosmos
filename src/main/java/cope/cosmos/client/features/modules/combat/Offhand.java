package cope.cosmos.client.features.modules.combat;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.features.modules.movement.ReverseStep;
import cope.cosmos.util.player.InventoryUtil;
import cope.cosmos.util.player.InventoryUtil.Inventory;
import cope.cosmos.util.player.MotionUtil;
import cope.cosmos.util.player.PlayerUtil;
import cope.cosmos.util.system.Timer;
import cope.cosmos.util.system.Timer.Format;
import cope.cosmos.util.world.HoleUtil;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Mouse;

public class Offhand extends Module {
    public static Offhand INSTANCE;

    public Offhand() {
        super("Offhand", Category.COMBAT, "Switches items in the offhand to a totem when low on health", () -> Setting.formatEnum(offHandItem.getValue()));
        INSTANCE = this;
    }

    public static Setting<OffhandItem> offHandItem = new Setting<>("Item", "Item to use when not at critical health", OffhandItem.CRYSTAL);
    public static Setting<OffhandItem> fallBack = new Setting<>("FallBack", "Item to use if you don't have the chosen item", OffhandItem.GAPPLE);

    public static Setting<OffhandItem> hole = new Setting<>("Hole", "Item to use when in hole and at critical health", OffhandItem.CRYSTAL);
    public static Setting<Double> holeHealth = new Setting<>("Health", "Health that is considered critical hole health", 0.0D, 6.0D, 36.0D, 0).setParent(hole);

    public static Setting<Sync> sync = new Setting<>("Sync", "Syncs the offhand switch to client processes", Sync.NONE);

    public static Setting<Double> delay = new Setting<>("Delay", "Delay when switching items", 0.0D, 0.0D, 1000.0D, 0);
    public static Setting<Double> health = new Setting<>("Health", "Health considered as critical health", 0.0D, 16.0D, 36.0D, 0);

    public static Setting<Boolean> swordGapple = new Setting<>("SwordGapple", "Use a gapple when holding a sword", true);
    public static Setting<Boolean> forceGapple = new Setting<>("ForceGapple", "Use a gapple when holding left click", false);
    public static Setting<Boolean> patchGapple = new Setting<>("StrictGapple", "Partial Bypass for offhand patched servers", false);

    public static Setting<Boolean> recursive = new Setting<>("Recursive", "Allow the use of hotbar items", false);
    public static Setting<Boolean> motionStrict = new Setting<>("MotionStrict", "Stop motion when switching items", false);
    public static Setting<Boolean> armorSafe = new Setting<>("ArmorSafe", "Swaps to a totem when you have armor slots empty, prevents totem fails", false);

    public static Setting<Boolean> pause = new Setting<>("Pause", "When to pause and use a totem", true);
    public static Setting<Boolean> pauseLiquid = new Setting<>("Liquid", "When in liquid", false).setParent(pause);
    public static Setting<Boolean> pauseAir = new Setting<>("Air", "When falling or flying", true).setParent(pause);
    public static Setting<Boolean> pauseElytra = new Setting<>("Elytra", "When elytra flying", true).setParent(pause);

    Timer offhandTimer = new Timer();

    @Override
    public void onUpdate() {
        Item offhandItem = offHandItem.getValue().getItem();

        if (mc.currentScreen != null)
            return;

        if (InventoryUtil.getItemCount(offhandItem) == 0 && !InventoryUtil.isSwitching())
            offhandItem = fallBack.getValue().getItem();

        if (PlayerUtil.getHealth() <= health.getValue() || !isSynced() || patchGapple.getValue() && mc.player.getHeldItemMainhand().getItem().equals(Items.GOLDEN_APPLE) || handlePause())
            offhandItem = Items.TOTEM_OF_UNDYING;

        if (armorSafe.getValue()) {
            for (ItemStack stack : mc.player.getArmorInventoryList()) {
                if (stack == null || stack.getItem() == Items.AIR) {
                    offhandItem = Items.TOTEM_OF_UNDYING;
                    break;
                }
            }
        }

        if (InventoryUtil.isHolding(Items.DIAMOND_SWORD) && swordGapple.getValue() && !forceGapple.getValue() || InventoryUtil.isHolding(Items.DIAMOND_SWORD) && forceGapple.getValue() && Mouse.isButtonDown(1))
            offhandItem = Items.GOLDEN_APPLE;

        if (HoleUtil.isInHole(mc.player) && PlayerUtil.getHealth() < holeHealth.getValue())
            offhandItem = hole.getValue().getItem();

        if (InventoryUtil.getItemSlot(offhandItem, Inventory.INVENTORY, recursive.getValue()) != -1 && !mc.player.getHeldItemOffhand().getItem().equals(offhandItem) && offhandTimer.passed((long) ((double) delay.getValue()), Format.SYSTEM)) {
            if (motionStrict.getValue() && MotionUtil.hasMoved()) {
                mc.player.motionX = 0;
                mc.player.motionY = 0;
                mc.player.motionZ = 0;
                mc.player.setVelocity(0, 0, 0);
                return;
            }

            InventoryUtil.moveItemToOffhand(offhandItem, !recursive.getValue());
            offhandTimer.reset();
        }
    }

    public boolean handlePause() {
        if (pause.getValue()) {
            if (PlayerUtil.isInLiquid() && pauseLiquid.getValue())
                return true;

            else if (mc.player.isElytraFlying() && pauseElytra.getValue())
                return true;

            return mc.player.fallDistance > 5 && pauseAir.getValue();
        }

        return false;
    }

    public boolean isSynced() {
        switch (sync.getValue()) {
            case NONE:
            default:
                return true;
            case AUTOCRYSTAL:
                return AutoCrystal.INSTANCE.isEnabled();
            case INTERACT:
                return Mouse.isButtonDown(2);
        }
    }

    @SuppressWarnings("unused")
    public enum OffhandItem {
        CRYSTAL(Items.END_CRYSTAL), GAPPLE(Items.GOLDEN_APPLE), TOTEM(Items.TOTEM_OF_UNDYING);

        private final Item item;

        OffhandItem(Item item) {
            this.item = item;
        }

        public Item getItem() {
            return this.item;
        }
    }

    public enum Sync {
        AUTOCRYSTAL, INTERACT, NONE
    }
}

