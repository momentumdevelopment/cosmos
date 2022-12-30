package cope.cosmos.client.features.modules.combat;

import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.combat.DamageUtil;
import cope.cosmos.util.combat.ExplosionUtil;
import cope.cosmos.util.math.Timer;
import cope.cosmos.util.math.Timer.Format;
import cope.cosmos.util.player.InventoryUtil;
import cope.cosmos.util.player.PlayerUtil;
import cope.cosmos.util.string.StringFormatter;
import cope.cosmos.util.world.SneakBlocks;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips
 * @since 11/20/2021
 */
public class AutoTotemModule extends Module {
    public static AutoTotemModule INSTANCE;

    public AutoTotemModule() {
        super("AutoTotem", new String[] {"Offhand", "AutoOffhand"}, Category.COMBAT,"Switches items in the offhand to a totem when low on health", () -> StringFormatter.formatEnum(mode.getValue()) + ", " + InventoryUtil.getItemCount(mode.getValue().getItem()));
        INSTANCE = this;
    }

    // **************************** general ****************************

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.TOTEM)
            .setAlias("Item")
            .setDescription("Item to use when not at critical health");

    public static Setting<Double> health = new Setting<>("Health", 0.0D, 16.0D, 20.0D, 1)
            .setDescription("Critical health to switch to a totem");

    public static Setting<Double> speed = new Setting<>("Speed", 0.0D, 20.0D, 20.0D, 1)
            .setDescription("Speed when switching items");

    public static Setting<Boolean> fast = new Setting<>("Fast", false)
            .setDescription("Performs all actions in one cycle");

    public static Setting<Boolean> lethal = new Setting<>("Lethal", true)
            .setAlias("SafetyCheck")
            .setDescription("Takes damage sources into account when switching");

    public static Setting<Boolean> hotbar = new Setting<>("Hotbar", false)
            .setAlias("Recursive")
            .setDescription("Allow hotbar items to be moved to the offhand");

    public static Setting<Boolean> crapple = new Setting<>("Crapple", false)
            .setDescription("Uses a crapple in the offhand");

    public static Setting<Boolean> offhandOverride = new Setting<>("OffhandOverride", true)
            .setAlias("SwordGap", "SwordGapple", "GappleOverride", "RightClickGap", "RightClickGapple")
            .setDescription("Switches offhand items in non-lethal scenarios");

    // offhand delay
    private final Timer offhandTimer = new Timer();

    @Override
    public void onTick() {

        // can't switch while we are in a screen
        if (mc.currentScreen == null) {

            // item we are switching to
            Item item = mode.getValue().getItem();

            // check if offhand should be overridden
            if (offhandOverride.getValue()) {

                // holding a sword and interacting
                if (InventoryUtil.isHolding(ItemSword.class) && mc.gameSettings.keyBindUseItem.isKeyDown()) {

                    // block we are interacting with
                    Block interactBlock = mc.world.getBlockState(mc.objectMouseOver.getBlockPos()).getBlock();

                    // check if it gets activated or if it is a button/lever
                    if (!SneakBlocks.contains(interactBlock) && !interactBlock.equals(Blocks.STONE_BUTTON) && !interactBlock.equals(Blocks.WOODEN_BUTTON) && !interactBlock.equals(Blocks.LEVER)) {
                        item = Items.GOLDEN_APPLE;
                    }
                }
            }

            // make sure we can actually take damage
            if (DamageUtil.canTakeDamage()) {

                // player health
                double playerHealth = PlayerUtil.getHealth();

                // check lethal scenarios
                if (lethal.getValue()) {

                    // SCENARIO #1: fall damage
                    float fallDamage = ((mc.player.fallDistance - 3) / 2F) + 3.5F;

                    // fall damage will kill us
                    if (playerHealth - fallDamage < 0.5 && !mc.player.isOverWater()) {
                        item = Items.TOTEM_OF_UNDYING;
                    }

                    // SCENARIO #2: flight damage
                    if (PlayerUtil.isFlying()) {
                        item = Items.TOTEM_OF_UNDYING;
                    }

                    // SCENARIO #3: crystal damage
                    for (Entity entity : mc.world.loadedEntityList) {

                        // make sure the entity exists
                        if (entity == null || entity.isDead) {
                            continue;
                        }

                        // make sure crystal is in range
                        double crystalRange = mc.player.getDistance(entity);
                        if (crystalRange > 6) {
                            continue;
                        }

                        if (entity instanceof EntityEnderCrystal) {

                            // damage from crystal
                            double crystalDamage = ExplosionUtil.getDamageFromExplosion(mc.player, entity.getPositionVector(), false);

                            // crystal will kill us
                            if (playerHealth - crystalDamage < 0.5) {
                                item = Items.TOTEM_OF_UNDYING;
                                break;
                            }
                        }
                    }
                }

                // make sure we are not below our critical health
                if (playerHealth <= health.getValue()) {
                    item = Items.TOTEM_OF_UNDYING;
                }

                // item slot
                // find our item in our inventory
                int itemSlot = -1;

                // gapple slots
                int gappleSlot = -1;
                int crappleSlot = -1;

                // search inventory
                for (int i = 9; i < (hotbar.getValue() ? 45 : 36); i++) {

                    // check item
                    if (mc.player.inventoryContainer.getSlot(i).getStack().getItem().equals(item)) {

                        // golden apple
                        if (item.equals(Items.GOLDEN_APPLE)) {

                            // item stack
                            ItemStack stack = mc.player.inventoryContainer.getSlot(i).getStack();

                            // god apple
                            if (stack.hasEffect()) {
                                gappleSlot = i;
                            }

                            // crapple
                            else {
                                crappleSlot = i;
                            }
                        }

                        else {
                            itemSlot = i;
                            break;
                        }
                    }
                }

                // since there are two types of gapples we need to sort them
                if (item.equals(Items.GOLDEN_APPLE)) {

                    // use crapples
                    if (crapple.getValue()) {

                        // player has absorption hearts
                        if (mc.player.isPotionActive(MobEffects.ABSORPTION)) {

                            // use a crapple
                            // in 1.12.2 this will restore all of our absorption hearts
                            if (crappleSlot != -1) {
                                itemSlot = crappleSlot;
                            }

                            // if we don't have crapples then use gapples
                            else if (gappleSlot != -1) {
                                itemSlot = gappleSlot;
                            }
                        }

                        // if we don't have absorption hearts then the crapple won't restore us back to full absorption hearts
                        else if (gappleSlot != -1) {
                            itemSlot = gappleSlot;
                        }
                    }

                    // don't use crapples
                    else {

                        // prefer gapples
                        if (gappleSlot != -1) {
                            itemSlot = gappleSlot;
                        }

                        // fall back to crapples
                        else if (crappleSlot != -1) {
                            itemSlot = crappleSlot;
                        }
                    }
                }

                // found our item
                if (itemSlot != -1) {

                    // already in offhand
                    if (!isOffhand(mc.player.inventoryContainer.getSlot(itemSlot).getStack())) {

                            // switch to items in one cycle
                        if (fast.getValue()) {

                            // calculate if we have passed delays
                            // offhand delay based on offhand speeds
                            long offhandDelay = (long) ((speed.getMax() - speed.getValue()) * 50);

                            // we have waited the proper time ???
                            boolean delayed = speed.getValue() >= speed.getMax() || offhandTimer.passedTime(offhandDelay, Format.MILLISECONDS);

                            // passed delay
                            if (delayed) {

                                // pickup
                                mc.playerController.windowClick(0, itemSlot < 9 ? itemSlot + 36 : itemSlot, 0, ClickType.PICKUP, mc.player);

                                // move the item to the offhand
                                mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);

                                // if we didn't get any item to swap
                                if (mc.player.inventory.getItemStack().isEmpty()) {

                                    // reset
                                    offhandTimer.resetTime();
                                    return;
                                }

                                // find a slot to return to
                                int returnSlot = -1;
                                for (int i = 0; i < 36; i++) {
                                    if (mc.player.inventory.getStackInSlot(i).isEmpty()) {
                                        returnSlot = i;
                                        break;
                                    }
                                }

                                // move the item in the offhand to the return slot
                                if (returnSlot != -1) {
                                    mc.playerController.windowClick(0, returnSlot, 0, ClickType.PICKUP, mc.player);
                                    mc.playerController.updateController();
                                }

                                // reset
                                offhandTimer.resetTime();
                            }
                        }

                        // switch to item in multiple cycles
                        else {

                            // calculate if we have passed delays
                            // offhand delay based on offhand speeds
                            long offhandDelay = (long) ((speed.getMax() - speed.getValue()) * 50);

                            // we have waited the proper time ???
                            boolean delayedFirst = speed.getValue() >= speed.getMax() || offhandTimer.passedTime(offhandDelay, Format.MILLISECONDS);

                            // passed delay
                            if (delayedFirst) {

                                // stop active hand prevents failing
                                mc.player.stopActiveHand();

                                // pickup
                                mc.playerController.windowClick(0, itemSlot < 9 ? itemSlot + 36 : itemSlot, 0, ClickType.PICKUP, mc.player);

                                // we have waited the proper time ???
                                boolean delayedSecond = speed.getValue() >= speed.getMax() || offhandTimer.passedTime(offhandDelay * 2, Format.MILLISECONDS);

                                // passed delay
                                if (delayedSecond) {

                                    // stop active hand prevents failing
                                    mc.player.stopActiveHand();

                                    // move the item to the offhand
                                    mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);

                                    // if we didn't get any item to swap
                                    if (mc.player.inventory.getItemStack().isEmpty()) {

                                        // reset
                                        offhandTimer.resetTime();
                                        return;
                                    }

                                    // we have waited the proper time ???
                                    boolean delayedThird = speed.getValue() >= speed.getMax() || offhandTimer.passedTime(offhandDelay * 3, Format.MILLISECONDS);

                                    // passed delay
                                    if (delayedThird) {

                                        // find a slot to return to
                                        int returnSlot = -1;
                                        for (int i = 0; i < 36; i++) {
                                            if (mc.player.inventory.getStackInSlot(i).isEmpty()) {
                                                returnSlot = i;
                                                break;
                                            }
                                        }

                                        // move the item in the offhand to the return slot
                                        if (returnSlot != -1) {

                                            // stop active hand prevents failing
                                            mc.player.stopActiveHand();

                                            // click
                                            mc.playerController.windowClick(0, returnSlot, 0, ClickType.PICKUP, mc.player);
                                            mc.playerController.updateController();
                                        }

                                        // reset
                                        offhandTimer.resetTime();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {
        if (nullCheck()) {

            // packet for totem pops
            if (event.getPacket() instanceof SPacketEntityStatus && ((SPacketEntityStatus) event.getPacket()).getOpCode() == 35) {

                // entity that popped
                Entity entity = ((SPacketEntityStatus) event.getPacket()).getEntity(mc.world);

                // player has popped
                if (entity != null && entity.equals(mc.player)) {

                    // item slot
                    // find our item in our inventory
                    int itemSlot = -1;
                    for (int i = 9; i < (hotbar.getValue() ? 45 : 36); i++) {
                        if (mc.player.inventoryContainer.getSlot(i).getStack().getItem().equals(Items.TOTEM_OF_UNDYING)) {
                            itemSlot = i;
                            break;
                        }
                    }

                    // found our item
                    if (itemSlot != -1) {

                        // switch to items in one cycle
                        if (fast.getValue()) {

                            // calculate if we have passed delays
                            // offhand delay based on offhand speeds
                            long offhandDelay = (long) ((speed.getMax() - speed.getValue()) * 50);

                            // we have waited the proper time ???
                            boolean delayed = speed.getValue() >= speed.getMax() || offhandTimer.passedTime(offhandDelay, Format.MILLISECONDS);

                            // passed delay
                            if (delayed) {

                                // pickup
                                mc.playerController.windowClick(0, itemSlot < 9 ? itemSlot + 36 : itemSlot, 0, ClickType.PICKUP, mc.player);

                                // move the item to the offhand
                                mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);

                                // if we didn't get any item to swap
                                if (mc.player.inventory.getItemStack().isEmpty()) {

                                    // reset
                                    offhandTimer.resetTime();
                                    return;
                                }

                                // find a slot to return to
                                int returnSlot = -1;
                                for (int i = 0; i < 36; i++) {
                                    if (mc.player.inventory.getStackInSlot(i).isEmpty()) {
                                        returnSlot = i;
                                        break;
                                    }
                                }

                                // move the item in the offhand to the return slot
                                if (returnSlot != -1) {
                                    mc.playerController.windowClick(0, returnSlot, 0, ClickType.PICKUP, mc.player);
                                    mc.playerController.updateController();
                                }

                                // reset
                                offhandTimer.resetTime();
                            }
                        }

                        // switch to item in multiple cycles
                        else {

                            // calculate if we have passed delays
                            // offhand delay based on offhand speeds
                            long offhandDelay = (long) ((speed.getMax() - speed.getValue()) * 50);

                            // we have waited the proper time ???
                            boolean delayedFirst = speed.getValue() >= speed.getMax() || offhandTimer.passedTime(offhandDelay, Format.MILLISECONDS);

                            // passed delay
                            if (delayedFirst) {

                                // stop active hand prevents failing
                                mc.player.stopActiveHand();

                                // pickup
                                mc.playerController.windowClick(0, itemSlot < 9 ? itemSlot + 36 : itemSlot, 0, ClickType.PICKUP, mc.player);

                                // we have waited the proper time ???
                                boolean delayedSecond = speed.getValue() >= speed.getMax() || offhandTimer.passedTime(offhandDelay * 2, Format.MILLISECONDS);

                                // passed delay
                                if (delayedSecond) {

                                    // stop active hand prevents failing
                                    mc.player.stopActiveHand();

                                    // move the item to the offhand
                                    mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);

                                    // if we didn't get any item to swap
                                    if (mc.player.inventory.getItemStack().isEmpty()) {

                                        // reset
                                        offhandTimer.resetTime();
                                        return;
                                    }

                                    // we have waited the proper time ???
                                    boolean delayedThird = speed.getValue() >= speed.getMax() || offhandTimer.passedTime(offhandDelay * 3, Format.MILLISECONDS);

                                    // passed delay
                                    if (delayedThird) {

                                        // find a slot to return to
                                        int returnSlot = -1;
                                        for (int i = 0; i < 36; i++) {
                                            if (mc.player.inventory.getStackInSlot(i).isEmpty()) {
                                                returnSlot = i;
                                                break;
                                            }
                                        }

                                        // move the item in the offhand to the return slot
                                        if (returnSlot != -1) {

                                            // stop active hand prevents failing
                                            mc.player.stopActiveHand();

                                            // click
                                            mc.playerController.windowClick(0, returnSlot, 0, ClickType.PICKUP, mc.player);
                                            mc.playerController.updateController();
                                        }

                                        // reset
                                        offhandTimer.resetTime();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks if a given item is already in the offhand
     * @param in The given item
     * @return Whether a given item is already in the offhand
     */
    public boolean isOffhand(ItemStack in) {

        // item in the offhand
        ItemStack offhandItem = mc.player.getHeldItemOffhand();

        // two types of gapples so we need to check each one
        if (in.getItem().equals(Items.GOLDEN_APPLE)) {

            // holding golden apple
            if (offhandItem.getItem().equals(in.getItem())) {

                // given item is a gapple ?
                boolean gapple = in.hasEffect();

                // check if equal
                return gapple == offhandItem.hasEffect();
            }
        }

        // check if they are equal
        else {
            return offhandItem.getItem().equals(in.getItem());
        }

        return false;
    }

    public enum Mode {

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

        Mode(Item item) {
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
}