package cope.cosmos.client.features.modules.combat;

import cope.cosmos.asm.mixins.accessor.INetworkManager;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.movement.FlightModule;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.combat.DamageUtil;
import cope.cosmos.util.string.StringFormatter;
import cope.cosmos.util.combat.ExplosionUtil;
import cope.cosmos.util.player.InventoryUtil;
import cope.cosmos.util.player.MotionUtil;
import cope.cosmos.util.player.PlayerUtil;
import cope.cosmos.util.math.Timer;
import cope.cosmos.util.math.Timer.Format;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraft.network.play.client.CPacketEntityAction;
import org.lwjgl.input.Mouse;

/**
 * @author linustouchtips
 * @since 11/20/2021
 */
public class OffhandModule extends Module {
    public static OffhandModule INSTANCE;

    public OffhandModule() {
        super("Offhand", Category.COMBAT, "Switches items in the offhand to a totem when low on health", () -> StringFormatter.formatEnum(item.getValue()) + ", " + InventoryUtil.getItemCount(item.getValue().getItem()));
        INSTANCE = this;
    }

    // **************************** anticheat ****************************

    public static Setting<Timing> timing = new Setting<>("Timing", Timing.LINEAR)
            .setDescription("How to timing when switching");

    public static Setting<Boolean> inventoryStrict = new Setting<>("InventoryStrict", false)
            .setDescription("Opens inventory serverside before switching");

    public static Setting<Boolean> motionStrict = new Setting<>("MotionStrict", false)
            .setDescription("Stops motion before switching");

    // **************************** general ****************************

    public static Setting<OffhandItem> item = new Setting<>("Item", OffhandItem.CRYSTAL)
            .setDescription("Item to use when not at critical health");

    public static Setting<OffhandItem> fallBack = new Setting<>("FallBack", OffhandItem.GAPPLE)
            .setDescription("Item to use if you don't have the chosen item");

    public static Setting<Gapple> gapple = new Setting<>("Gapple", Gapple.SWORD)
            .setDescription("When to dynamically switch to a golden apple");

    public static Setting<Safety> safety = new Setting<>("Safety", Safety.NONE)
            .setDescription("When to consider the situation unsafe");

    public static Setting<Double> health = new Setting<>("Health", 0.0D, 16.0D, 36.0D, 1)
            .setDescription("Health considered as critical health");

    public static Setting<Double> delay = new Setting<>("Delay", 0.0D, 0.0D, 20.0D, 1)
            .setDescription("Delay when switching items");

    public static Setting<Boolean> recursive = new Setting<>("Recursive", false)
            .setDescription("Allow hotbar items to be moved to the offhand");


    // offhand stage
    private Stage stage = Stage.IDLE;

    // offhand delay
    private final Timer offhandTimer = new Timer();

    @Override
    public void onDisable() {
        super.onDisable();
        stage = Stage.IDLE;
    }

    @Override
    public void onTick() {

        // we are not switching right now;
        stage = Stage.IDLE;

        // switching in GUI's causes crash
        if (mc.currentScreen == null) {

            // the item to switch to
            Item switchItem = item.getValue().getItem();

            // wait for previous swap
            if (!stage.equals(Stage.MOVE_TO_OFFHAND)) {

                // offhand item counts
                int itemCount = InventoryUtil.getItemCount(item.getValue().getItem());
                int fallbackCount = InventoryUtil.getItemCount(fallBack.getValue().getItem());

                // if don't have out main item, try our fallback item
                if (itemCount <= 0 && fallbackCount > 0) {
                    switchItem = fallBack.getValue().getItem();
                }
            }

            // dynamically switch to a gapple if needed
            switch (gapple.getValue()) {
                case SWORD:
                    if (InventoryUtil.isHolding(Items.DIAMOND_SWORD)) {
                        switchItem = Items.GOLDEN_APPLE;
                    }

                    break;
                case INTERACT:
                    if (InventoryUtil.isHolding(Items.DIAMOND_SWORD) && Mouse.isButtonDown(1)) {

                        // block we are interacting with
                        Block timingBlock = mc.world.getBlockState(mc.objectMouseOver.getBlockPos()).getBlock();

                        // check if it gets activated
                        if (getCosmos().getInteractionManager().getSneakBlocks().contains(timingBlock)) {
                            break;
                        }

                        // check if its a button/lever
                        if (timingBlock.equals(Blocks.STONE_BUTTON) || timingBlock.equals(Blocks.WOODEN_BUTTON) || timingBlock.equals(Blocks.LEVER)) {
                            break;
                        }

                        switchItem = Items.GOLDEN_APPLE;
                    }

                    break;
            }

            /* make sure we are not in a situation where we need to pause the offhand */

            // make sure we can take damage
            if (DamageUtil.canTakeDamage()) {

                // player health
                double playerHealth = PlayerUtil.getHealth();

                // check possible unsafe situations
                switch (safety.getValue()) {
                    case FULL:

                        // make sure none of our armor pieces are missing
                        for (ItemStack stack : mc.player.getArmorInventoryList()) {

                            // armor stack is empty
                            if (stack == null || stack.getItem().equals(Items.AIR)) {
                                switchItem = Items.TOTEM_OF_UNDYING;
                                break;
                            }
                        }

                        // check nearby crystals
                        for (Entity entity : mc.world.loadedEntityList) {

                            // make sure the entity exists
                            if (entity == null || entity.isDead) {
                                continue;
                            }

                            // make sure crystal is in range
                            if (mc.player.getDistance(entity) > 6) {
                                continue;
                            }

                            if (entity instanceof EntityEnderCrystal) {

                                // damage from crystal
                                double damage = ExplosionUtil.getDamageFromExplosion(mc.player, entity.getPositionVector(), false);

                                // crystal will kill us
                                if (playerHealth - damage < 0.5) {
                                    switchItem = Items.TOTEM_OF_UNDYING;
                                    break;
                                }
                            }
                        }

                        break;
                    case NONE:
                    default:
                        break;
                }

                // fall damage might kill us (TODO: add fall damage calcs)
                if  (mc.player.fallDistance > 5 && !mc.player.isOverWater()) {
                    switchItem = Items.TOTEM_OF_UNDYING;
                }

                // flight friction/collision could possibly kill us
                if (FlightModule.INSTANCE.isActive() || mc.player.isElytraFlying() || mc.player.capabilities.isFlying) {
                    switchItem = Items.TOTEM_OF_UNDYING;
                }

                if (timing.getValue().equals(Timing.DYNAMIC)) {

                    // some anticheats only have offhand patched if the player is holding a gapple, so this is a partial offhand bypass for those servers
                    if (InventoryUtil.isHolding(Items.GOLDEN_APPLE)) {
                        switchItem = Items.TOTEM_OF_UNDYING;
                    }
                }

                // make sure we are not below our critical health
                if (playerHealth < health.getValue()) {
                    switchItem = Items.TOTEM_OF_UNDYING;
                }
            }

            // make sure we've passed our delay and we're not already holding the item
            if (offhandTimer.passedTime(delay.getValue().longValue() * 100, Format.MILLISECONDS) && !mc.player.getHeldItemOffhand().getItem().equals(switchItem) && stage.equals(Stage.IDLE)) {

                // we are now picking up the item
                stage = Stage.PICKUP_ITEM;

                // find our item in our inventory
                int itemSlot = -1;
                for (int i = recursive.getValue() ? 0 : 9; i <= 44; i++) {
                    if (mc.player.inventoryContainer.getSlot(i).getStack().getItem().equals(switchItem)) {
                        itemSlot = i;
                        break;
                    }
                }

                // stop player motion before moving items
                if (motionStrict.getValue() && MotionUtil.hasMoved()) {
                    mc.player.motionX = 0;
                    mc.player.motionZ = 0;
                    mc.player.setVelocity(0, mc.player.motionY, 0);
                    return;
                }

                // pick up the item
                if (itemSlot != -1) {

                    // open inventory via packets
                    if (inventoryStrict.getValue() && mc.getConnection() != null) {
                        ((INetworkManager) mc.getConnection().getNetworkManager()).hookDispatchPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.OPEN_INVENTORY), null);
                    }

                    mc.playerController.windowClick(0, itemSlot, 0, ClickType.PICKUP, mc.player);

                    if (!timing.getValue().equals(Timing.SEQUENTIAL)) {

                        // we are now moving the item to the offhand
                        stage = Stage.MOVE_TO_OFFHAND;

                        // move the item to the offhand
                        mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);

                        // if we didn't get any item to swap
                        if (mc.player.inventory.getItemStack().isEmpty()) {
                            return;
                        }

                        // we are now swapping the old item
                        stage = Stage.SWAP_OLD;

                        // find a slot to return to
                        int returnSlot = -1;
                        for (int i = recursive.getValue() ? 0 : 9; i <= 44; i++) {
                            if (mc.player.inventory.getStackInSlot(i).isEmpty()) {
                                returnSlot = i;
                                break;
                            }
                        }

                        // move the item in the offhand to the return slot
                        if (returnSlot != -1) {
                            mc.playerController.windowClick(0, returnSlot, 0, ClickType.PICKUP, mc.player);
                            mc.playerController.updateController();

                            // close window
                            if (inventoryStrict.getValue() && mc.getConnection() != null) {
                                ((INetworkManager) mc.getConnection().getNetworkManager()).hookDispatchPacket(new CPacketCloseWindow(mc.player.inventoryContainer.windowId), null);
                            }
                        }

                        offhandTimer.resetTime();
                    }
                }
            }

            if (timing.getValue().equals(Timing.SEQUENTIAL)) {
                if (offhandTimer.passedTime(delay.getValue().longValue() * 200, Format.MILLISECONDS) && mc.player.inventory.getItemStack().getItem().equals(switchItem) && stage.equals(Stage.PICKUP_ITEM)) {

                    // we are now moving the item to the offhand
                    stage = Stage.MOVE_TO_OFFHAND;

                    // stop player motion before moving items
                    if (motionStrict.getValue() && MotionUtil.hasMoved()) {
                        mc.player.motionX = 0;
                        mc.player.motionZ = 0;
                        mc.player.setVelocity(0, mc.player.motionY, 0);
                        return;
                    }

                    // move the item to the offhand
                    mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);

                    // if we didn't get any item to swap
                    if (mc.player.inventory.getItemStack().isEmpty()) {
                        return;
                    }
                }

                if (offhandTimer.passedTime(delay.getValue().longValue() * 300, Format.MILLISECONDS) && mc.player.getHeldItemOffhand().getItem().equals(switchItem) && stage.equals(Stage.MOVE_TO_OFFHAND)) {

                    // we are now swapping the old item
                    stage = Stage.SWAP_OLD;

                    // find a slot to return to
                    int returnSlot = -1;
                    for (int i = recursive.getValue() ? 0 : 9; i <= 44; i++) {
                        if (mc.player.inventory.getStackInSlot(i).isEmpty()) {
                            returnSlot = i;
                            break;
                        }
                    }

                    if (returnSlot != -1) {

                        // stop player motion before moving items
                        if (motionStrict.getValue() && MotionUtil.hasMoved()) {
                            mc.player.motionX = 0;
                            mc.player.motionZ = 0;
                            mc.player.setVelocity(0, mc.player.motionY, 0);
                            return;
                        }

                        // move the item in the offhand to the return slot
                        mc.playerController.windowClick(0, returnSlot, 0, ClickType.PICKUP, mc.player);
                        mc.playerController.updateController();

                        // close window
                        if (inventoryStrict.getValue() && mc.getConnection() != null) {
                            ((INetworkManager) mc.getConnection().getNetworkManager()).hookDispatchPacket(new CPacketCloseWindow(mc.player.inventoryContainer.windowId), null);
                        }
                    }

                    offhandTimer.resetTime();
                }
            }
        }
    }

    public enum Stage {

        /**
         * Stage where no switching occurs
         */
        IDLE,

        /**
         * Stage where we pick up our item
         */
        PICKUP_ITEM,

        /**
         * Stage where we place our item in our offhand
         */
        MOVE_TO_OFFHAND,

        /**
         * Stage where we move the old item in our offhand to the return slot
         */
        SWAP_OLD
    }

    public enum OffhandItem {

        /**
         * Switch to an End Crystal
         */
        CRYSTAL(Items.END_CRYSTAL),

        /**
         * Switch to a Golden Apple
         */
        GAPPLE(Items.GOLDEN_APPLE),

        /**
         * Switch to a Totem
         */
        TOTEM(Items.TOTEM_OF_UNDYING);

        private final Item item;

        OffhandItem(Item item) {
            this.item = item;
        }

        /**
         * Gets the item associated with the offhand
         * @return The item associated with the offhand
         */
        public Item getItem() {
            return item;
        }
    }

    public enum Timing {

        /**
         * Interacts at all times
         */
        LINEAR,

        /**
         * Interacts if each process has passed the delay
         */
        SEQUENTIAL,

        /**
         * Interacts when holding certain items
         */
        DYNAMIC
    }

    public enum Gapple {

        /**
         * Switches the offhand to a gapple when right clicking
         */
        INTERACT,

        /**
         * Switches the offhand to a gapple when holding a sword
         */
        SWORD,

        /**
         * Does not dynamically switch gapples to the offhand
         */
        NONE,
    }
    
    public enum Safety {

        /**
         * Swaps when you're armor is missing or too low or when crystal damages will kill you
         */
        FULL,

        /**
         * No safety calculations
         */
        NONE,
    }
}