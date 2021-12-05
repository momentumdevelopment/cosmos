package cope.cosmos.client.features.modules.player;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.utility.player.InventoryUtil;
import cope.cosmos.utility.player.InventoryUtil.Inventory;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;

public class NoFall extends Module {
    public static NoFall INSTANCE;

    public NoFall() {
        super("NoFall", Category.PLAYER, "Attempts to negate fall damage", () -> Setting.formatEnum(mode.getValue()));
        INSTANCE = this;
    }

    public static Setting<Mode> mode = new Setting<>("Mode", "How to negate fall damage", Mode.PACKET);
    public static Setting<Double> distance = new Setting<>("Distance", "The minimum fall distance before doing anything", 1.0, 2.0, 5.0, 1);

    public static Setting<Swap> swap = new Setting<>("Swap", "How to swap to a water bucket", Swap.LEGIT);
    public static Setting<Double> glideSpeed = new Setting<>("GlideSpeed", "The speed to glide at", 0.1, 1.5, 5.0, 1);

    private EnumHand hand = EnumHand.MAIN_HAND;
    private int oldSlot = -1;

    @Override
    public void onDisable() {
        super.onDisable();
        swapBack();
    }

    @Override
    public void onUpdate() {
        if (mc.player.fallDistance >= distance.getValue()) {
            switch (mode.getValue()) {
                case PACKET: {
                    mc.player.connection.sendPacket(new CPacketPlayer(true));
                    break;
                }

                case GLIDE: {
                    mc.player.motionY /= glideSpeed.getValue();
                    mc.player.connection.sendPacket(new CPacketPlayer(true));
                    break;
                }

                case WATER: {
                    doWaterBucket();
                    break;
                }

                case RUBBERBAND: {
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, 0, mc.player.posZ, true));
                    break;
                }
            }
        }

        else {
            swapBack();
        }
    }

    private void doWaterBucket() {
        if (!InventoryUtil.isHolding(Items.WATER_BUCKET)) {
            int slot = InventoryUtil.getItemSlot(Items.WATER_BUCKET, Inventory.HOTBAR);
            if (slot == -1) {
                return; // get shit on
            }

            hand = slot == 45 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
            if (hand == EnumHand.MAIN_HAND) {
                oldSlot = mc.player.inventory.currentItem;
                InventoryUtil.switchToSlot(slot, swap.getValue() == Swap.LEGIT ? InventoryUtil.Switch.NORMAL : InventoryUtil.Switch.PACKET);
            }

            mc.player.setActiveHand(hand);
        }

        mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw, -90, false));
        mc.playerController.processRightClick(mc.player, mc.world, hand);
    }

    private void swapBack() {
        if (oldSlot != -1) {
            InventoryUtil.switchToSlot(oldSlot, swap.getValue() == Swap.LEGIT ? InventoryUtil.Switch.NORMAL : InventoryUtil.Switch.PACKET);
            oldSlot = -1;
        }

        hand = EnumHand.MAIN_HAND;
    }

    public enum Mode {
        PACKET, GLIDE, WATER, RUBBERBAND
    }

    public enum Swap {
        LEGIT, SILENT
    }
}
