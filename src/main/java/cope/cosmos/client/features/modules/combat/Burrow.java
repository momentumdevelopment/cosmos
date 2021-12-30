package cope.cosmos.client.features.modules.combat;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.InventoryUtil;
import cope.cosmos.util.player.InventoryUtil.Inventory;
import cope.cosmos.util.player.InventoryUtil.Switch;
import cope.cosmos.util.player.Rotation.Rotate;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author bon55, linustouchtips
 * @since 06/08/2021
 */
@SuppressWarnings("unused")
public class Burrow extends Module {
	public static Burrow INSTANCE;

	public Burrow() {
		super("Burrow", Category.COMBAT, "Instantly burrow into a block.");
		INSTANCE = this;
	}

	public static Setting<Mode> mode = new Setting<>("Mode", Mode.OBSIDIAN).setDescription("Block to prefer");
	public static Setting<Rotate> rotate = new Setting<>("Rotation", Rotate.NONE).setDescription("Mode for attack rotations");
	public static Setting<Double> offset = new Setting<>("Offset", -10.0, 2.2, 10.0, 1).setDescription("How high to rubberband");

	@Override
	public void onEnable() {
		super.onEnable();

		// set timer to 10
		getCosmos().getTickManager().setClientTicks(10);

		// send a random on ground packet
		mc.player.connection.sendPacket(new CPacketPlayer(ThreadLocalRandom.current().nextBoolean()));

		// save our current position
		BlockPos previousPosition = new BlockPos(mc.player.getPositionVector());

		// send fake jump packets
		mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.41999998688698, mc.player.posZ, true));
		mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.7531999805211997, mc.player.posZ, true));
		mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.00133597911214, mc.player.posZ, true));
		mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.16610926093821, mc.player.posZ, true));

		// sync serverside position to client side position, since we need to place a block at our previous position
		mc.player.setPosition(mc.player.posX, mc.player.posY + 1.16610926093821, mc.player.posZ);

		// save our previous slot
		int previousSlot = mc.player.inventory.currentItem;

		// find the item in our hotbar
		int blockSlot = -1;
		for (Block block : mode.getValue().getBlocks()) {
			// get the slot of the current block
			int potentialSlot = InventoryUtil.getBlockSlot(block, Inventory.HOTBAR);

			// we found a potential block to switch to
			if (potentialSlot != -1) {
				blockSlot = potentialSlot;
				break;
			}
		}

		// switch to our block slot
		InventoryUtil.switchToSlot(blockSlot, Switch.NORMAL);

		// place at our previous position
		getCosmos().getInteractionManager().placeBlock(previousPosition, rotate.getValue(), false);

		// reset our position, since we've already placed
		mc.player.setPosition(mc.player.posX, mc.player.posY - 1.16610926093821, mc.player.posZ);

		// switch back to our previous slot
		InventoryUtil.switchToSlot(previousSlot, Switch.NORMAL);

		// send an out of bounds packet, ideally NCP will rubberband us back and we will be inside the block position
		mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + offset.getValue(), mc.player.posZ, false));

		// update our timer
		getCosmos().getTickManager().setClientTicks(2500);
		disable();
	}

	@Override
	public boolean isActive() {
		return isEnabled();
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
		FENCE(Blocks.ACACIA_FENCE, Blocks.DARK_OAK_FENCE, Blocks.BIRCH_FENCE, Blocks.JUNGLE_FENCE, Blocks.OAK_FENCE, Blocks.NETHER_BRICK_FENCE, Blocks.SPRUCE_FENCE_GATE),

		/**
		 * Skulls, Blocks with 0.2 height
		 */
		SKULL(Blocks.SKULL);

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
