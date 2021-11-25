package cope.cosmos.client.features.modules.combat;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.NotificationManager.Notification;
import cope.cosmos.client.manager.managers.NotificationManager.Type;
import cope.cosmos.util.player.InventoryUtil;
import cope.cosmos.util.player.InventoryUtil.Inventory;
import cope.cosmos.util.player.Rotation.Rotate;
import cope.cosmos.util.world.TeleportUtil;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;

import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("unused")
public class Burrow extends Module {
	public static Burrow INSTANCE;
	
	public Burrow() {
		super("Burrow", Category.COMBAT, "Instantly burrow into a block.");
		INSTANCE = this;
	}

	public static Setting<Mode> mode = new Setting<>("Mode", "Block to prefer",  Mode.OBSIDIAN);
	public static Setting<Rotate> rotate = new Setting<>("Rotation", "Mode for attack rotations", Rotate.NONE);
	public static Setting<Double> offset = new Setting<>("Offset", "How high to rubberband", -10.0, 2.2, 10.0, 1);

	@Override
	public void onEnable() {
		super.onEnable();

		Cosmos.INSTANCE.getTickManager().setClientTicks(10);

		mc.player.connection.sendPacket(new CPacketPlayer(ThreadLocalRandom.current().nextBoolean()));

		BlockPos originalPos = new BlockPos(mc.player.getPositionVector());

		// the slot to switch to
		int blockSlot = -1;

		// find the item in our hotbar
		for (Block block : mode.getValue().getBlocks()) {
			int potentialSlot = InventoryUtil.getBlockSlot(block, Inventory.HOTBAR);

			if (potentialSlot != -1) {
				blockSlot = potentialSlot;
				break;
			}
		}

		if (!mc.world.getBlockState(originalPos).getMaterial().isReplaceable() || !mc.world.isAirBlock(mc.player.getPosition().add(0, 3, 0)) || blockSlot == -1) {
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

		mc.player.inventory.currentItem = blockSlot;
		mc.player.connection.sendPacket(new CPacketHeldItemChange(blockSlot));

		getCosmos().getInteractionManager().placeBlock(originalPos, rotate.getValue(), false);

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
		/**
		 * Obsidian
		 */
		OBSIDIAN(Blocks.OBSIDIAN),

		/**
		 * Chests, Blocks with 0.8 height
		 */
		CHESTS(Blocks.ENDER_CHEST, Blocks.CHEST, Blocks.TRAPPED_CHEST),

		/**
		 * Blocks that have gravity applied to them (i.e. fall)
		 */
		FALLING(Blocks.ANVIL, Blocks.GRAVEL, Blocks.SAND),

		/**
		 * EndRods
		 */
		END_ROD(Blocks.END_ROD),

		/**
		 * Fences, Blocks with 0.2 width
		 */
		FENCE(Blocks.ACACIA_FENCE, Blocks.DARK_OAK_FENCE, Blocks.BIRCH_FENCE, Blocks.JUNGLE_FENCE, Blocks.OAK_FENCE, Blocks.NETHER_BRICK_FENCE, Blocks.SPRUCE_FENCE_GATE);

		private final Block[] blocks;

		Mode(Block... block) {
			this.blocks = block;
		}

		/**
		 * Get the blocks associated with the mode
		 * @return The blocks associated with the mode
		 */
		public Block[] getBlocks() {
			return blocks;
		}
	}
}
