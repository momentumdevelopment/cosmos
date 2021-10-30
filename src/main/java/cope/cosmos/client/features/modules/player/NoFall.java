package cope.cosmos.client.features.modules.player;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.InventoryUtil;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;

public class NoFall extends Module {
    public static final Setting<Mode> mode = new Setting<>("Mode", "How to negate fall damage", Mode.PACKET);
    public static final Setting<Double> distance = new Setting<>("Distance", "The minimum fall distance before doing anything", 1.0, 2.0, 256.0, 1);

    public static final Setting<Swap> swap = new Setting<>("Swap", "How to swap to a water bucket", Swap.LEGIT);
    public static final Setting<Double> rubberband = new Setting<>("Rubberband", "How much to rubberband", 1.0, 5.0, 15.0, 1);
    public static final Setting<Double> glideSpeed = new Setting<>("GlideSpeed", "The speed to glide at", 0.1, 1.5, 5.0, 1);

    private EnumHand hand = EnumHand.MAIN_HAND;
    private int oldSlot = -1;

    public NoFall() {
        super("NoFall", Category.PLAYER, "Attempts to negate fall damage", () -> Setting.formatEnum(mode.getValue()));
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (nullCheck()) {
            this.swapBack();
        }
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
                    this.doWaterBucket();
                    break;
                }

                case RUBBERBAND: {
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + rubberband.getValue(), mc.player.posZ, true));
                    break;
                }
            }
        } else {
            this.swapBack();
        }
    }

    private void doWaterBucket() {
        if (!InventoryUtil.isHolding(Items.WATER_BUCKET)) {
            int slot = InventoryUtil.getItemSlot(Items.WATER_BUCKET, InventoryUtil.Inventory.HOTBAR, true);
            if (slot == -1) {
                return; // get shit on
            }

            this.hand = slot == 45 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
            if (this.hand == EnumHand.MAIN_HAND) {
                this.oldSlot = mc.player.inventory.currentItem;
                InventoryUtil.switchToSlot(slot, swap.getValue() == Swap.LEGIT ? InventoryUtil.Switch.NORMAL : InventoryUtil.Switch.PACKET);
            }

            mc.player.setActiveHand(this.hand);
        }

        // so if this is done with the rotation manager, it kicks you for "Invalid move player packet received" on vanilla???
        // tf did you do to my rotation manager linus
        mc.player.rotationPitch = 90.0f;
        mc.playerController.processRightClick(mc.player, mc.world, this.hand);
    }

    private void swapBack() {
        if (this.oldSlot != -1) {
            InventoryUtil.switchToSlot(this.oldSlot, swap.getValue() == Swap.LEGIT ? InventoryUtil.Switch.NORMAL : InventoryUtil.Switch.PACKET);
            this.oldSlot = -1;
        }

        this.hand = EnumHand.MAIN_HAND;
    }

    public enum Mode {
        PACKET, GLIDE, WATER, RUBBERBAND
    }

    public enum Swap {
        LEGIT, SILENT
    }
}
