package cope.cosmos.client.features.modules.combat;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.manager.managers.NotificationManager.*;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.InventoryUtil;
import cope.cosmos.util.player.PlayerUtil;
import cope.cosmos.util.player.PlayerUtil.Hand;
import cope.cosmos.util.player.InventoryUtil.*;
import cope.cosmos.util.world.BlockUtil;
import cope.cosmos.util.world.TeleportUtil;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;

import java.util.concurrent.ThreadLocalRandom;

public class Burrow extends Module {
	public static Burrow INSTANCE;
	
	public Burrow() {
		super("Burrow", Category.COMBAT, "Instantly burrow into a block.");
		INSTANCE = this;
	}

	public static Setting<Mode> mode = new Setting<>("Mode", "Block to prefer",  Mode.OBSIDIAN);
	public static Setting<Hand> swing = new Setting<>("Swing", "Hand to swing when placing", Hand.MAINHAND);
	public static Setting<Double> offset = new Setting<>("Offset", "How high to rubberband", -10.0, 2.2, 10.0, 1);
	public static Setting<Boolean> packet = new Setting<>("Packet", "Place with packets", true);

	@Override
	public void onEnable() {
		super.onEnable();

		Cosmos.INSTANCE.getTickManager().setClientTicks(10);

		mc.player.connection.sendPacket(new CPacketPlayer(ThreadLocalRandom.current().nextBoolean()));

		BlockPos originalPos = new BlockPos(mc.player.getPositionVector());
		int block = -1;

		switch (mode.getValue()) {
			case OBSIDIAN : {
				block = InventoryUtil.getBlockSlot(Blocks.OBSIDIAN, Inventory.INVENTORY, false);
				break;
			}

			case E_CHEST : {
				block = InventoryUtil.getBlockSlot(Blocks.ENDER_CHEST, Inventory.INVENTORY, false);
				break;
			}

			case ANVIL: {
				block = InventoryUtil.getBlockSlot(Blocks.ANVIL, Inventory.INVENTORY, false);
				break;
			}

			case CHEST: {
				block = InventoryUtil.getBlockSlot(Blocks.CHEST, Inventory.INVENTORY, false);
				break;
			}
		}

		if (!mc.world.getBlockState(originalPos).getMaterial().isReplaceable() || !mc.world.isAirBlock(mc.player.getPosition().add(0, 3, 0)) || block == -1) {
			Cosmos.INSTANCE.getNotificationManager().pushNotification(new Notification("Unable to burrow!", Type.WARNING));
			Cosmos.INSTANCE.getTickManager().setClientTicks(2500);
			disable();
			return;
		}

		mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.41999998688698, mc.player.posZ, true));
		mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.7531999805211997, mc.player.posZ, true));
		mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.00133597911214, mc.player.posZ, true));
		mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.16610926093821, mc.player.posZ, true));

		TeleportUtil.teleportPlayerNoPacket(mc.player.posX, mc.player.posY + 1.16610926093821, mc.player.posZ);

		int oldSlot = mc.player.inventory.currentItem;

		mc.player.inventory.currentItem = block;
		mc.player.connection.sendPacket(new CPacketHeldItemChange(block));

		BlockUtil.placeBlock(originalPos, packet.getValue(), false);
		PlayerUtil.swingArm(swing.getValue());

		TeleportUtil.teleportPlayerNoPacket(mc.player.posX, mc.player.posY - 1.16610926093821, mc.player.posZ);

		mc.player.inventory.currentItem = oldSlot;
		mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));

		mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + offset.getValue(), mc.player.posZ, false));

		Cosmos.INSTANCE.getTickManager().setClientTicks(2500);
		disable();
	}

	@Override
	public boolean isActive() {
		return isEnabled() && mc.world.getBlockState(mc.player.getPosition()).getMaterial().isReplaceable();
	}

	private enum Mode {
		OBSIDIAN, E_CHEST, ANVIL, CHEST
	}
}
