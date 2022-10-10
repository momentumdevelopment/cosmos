package cope.cosmos.client.features.modules.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.asm.mixins.accessor.IEntityPlayerSP;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.InventoryManager.InventoryRegion;
import cope.cosmos.client.manager.managers.InventoryManager.Switch;
import cope.cosmos.util.holder.Rotation.Rotate;
import cope.cosmos.util.player.PlayerUtil;
import cope.cosmos.util.world.BlockUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author bon55, linustouchtips
 * @since 08/18/2022
 */
public class SelfFillModule extends Module {
	public static SelfFillModule INSTANCE;

	public SelfFillModule() {
		super("SelfFill", new String[] {"Burrow", "RubberFill"}, Category.COMBAT, "Glitches you into a block");
		INSTANCE = this;
	}

	// **************************** anticheat ****************************

	public static Setting<Double> offset = new Setting<>("Offset", -10.0, 2.2, 10.0, 1)
			.setDescription("How high to rubberband");

	public static Setting<Rotate> rotate = new Setting<>("Rotation", Rotate.NONE)
			.setAlias("Rotate")
			.setDescription("Mode for attack rotations");

	public static Setting<Boolean> strict = new Setting<>("Strict", false)
			.setDescription("Strict interactions");

	// **************************** general ****************************

	public static Setting<Mode> mode = new Setting<>("Mode", Mode.DYNAMIC)
			.setDescription("Block to use when burrowing");

	public static Setting<Completion> completion = new Setting<>("Completion", Completion.SHIFT)
			.setDescription("When to disable the module");

	public static Setting<Switch> autoSwitch = new Setting<>("Switch", Switch.NORMAL)
			.setAlias("AutoSwitch", "Swap", "AutoSwap")
			.setDescription("How to switch when placing blocks");

	// start info
	private BlockPos start;

	@Override
	public void onEnable() {
		super.onEnable();

		// mark our starting height
		start = PlayerUtil.getPosition();
	}

	@Override
	public void onUpdate() {

		// we are no long in the same spot
		if (!PlayerUtil.getPosition().equals(start) && completion.getValue().equals(Completion.SHIFT)) {
			toggle();
			return;
		}

		// original block position
		BlockPos origin = new BlockPos(mc.player.posX, Math.round(mc.player.posY), mc.player.posZ);

		// only burrow on ground
		if (mc.player.onGround) {

			// check if we are already in a burrow
			if (!mc.world.getBlockState(origin).getMaterial().blocksMovement() && mc.player.collidedVertically) {

				// clear placement
				AutoCrystalModule.INSTANCE.call(() -> {

					// check if the AutoCrystal is busy
					if (!AutoCrystalModule.INSTANCE.isRunningTask(false)) {

						// check unsafe entities and clear if necessary
						for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(origin))) {

							// can be placed on
							if (entity == null || entity instanceof EntityItem || entity instanceof EntityXPOrb) {
								continue;
							}

							// attack crystals
							if (entity instanceof EntityEnderCrystal) {

								// queue attack
								AutoCrystalModule.INSTANCE.queue((EntityEnderCrystal) entity);
								break;
							}
						}
					}
				});

				// send fake jump packets
				mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.41999998688698, mc.player.posZ, true));
				mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.7531999805211997, mc.player.posZ, true));
				mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.00133597911214, mc.player.posZ, true));
				mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.16610926093821, mc.player.posZ, true));

				// set our position
				mc.player.setPosition(mc.player.posX, mc.player.posY + 1.16610926093821, mc.player.posZ);
				((IEntityPlayerSP) mc.player).setLastReportedPosY(mc.player.posY + 1.16610926093821);

				// log previous slot, we'll switch back to this item
				int previousSlot = mc.player.inventory.currentItem;

				// switch to block before placing
				if (!autoSwitch.getValue().equals(Switch.NONE)) {

					// slot to switch to
					int obsidianSlot = getCosmos().getInventoryManager().searchSlot(Item.getItemFromBlock(Blocks.OBSIDIAN), InventoryRegion.HOTBAR);
					int echestSlot = getCosmos().getInventoryManager().searchSlot(Item.getItemFromBlock(Blocks.ENDER_CHEST), InventoryRegion.HOTBAR);

					// prefer obsidian over echests
					if (obsidianSlot != -1) {
						getCosmos().getInventoryManager().switchToSlot(obsidianSlot, autoSwitch.getValue());
					}

					// fallback if we don't have obsidian
					else if (echestSlot != -1) {
						getCosmos().getInventoryManager().switchToSlot(echestSlot, autoSwitch.getValue());
					}

					else {
						getCosmos().getChatManager().sendClientMessage(ChatFormatting.RED + "No valid blocks!", -200);
						return;
					}
				}

				// place block
				if (placeBlock(origin)) {

					// reset our position, since we've already placed
					mc.player.setPosition(mc.player.posX, mc.player.posY - 1.16610926093821, mc.player.posZ);

					// getCosmos().getChatManager().sendClientMessage("placed");
					// offset
					double rubberbandOffset = getOffset();

					// don't send a rubberband packet
					if (!mode.getValue().equals(Mode.STALL)) {

						// send an out of bounds packet, ideally NCP will rubberband us back and we will be inside the block position
						mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + rubberbandOffset, mc.player.posZ, false));
					}
				}

				// switch back to previous slot
				if (previousSlot != -1) {
					getCosmos().getInventoryManager().switchToSlot(previousSlot, autoSwitch.getValue());
				}

				// auto disabling module
				if (completion.getValue().equals(Completion.FILLED)) {
					disable(true);
				}
			}

			else if (completion.getValue().equals(Completion.FILLED)) {

				// auto disabling module
				disable(true);
			}
		}

		else if (completion.getValue().equals(Completion.FILLED)) {

			// auto disabling module
			disable(true);
		}
	}

	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {

		// disable on logout
		disable(true);
	}

	/**
	 * Gets the offset of the rubberband
	 * @return The offset of the rubberband
	 */
	public double getOffset() {

		// dynamic offsets
		if (mode.getValue().equals(Mode.DYNAMIC)) {

			// start
			double rubberband = Math.abs(offset.getValue());

			// check all blocks within the height
			for (double height = 0; height < offset.getValue(); height += 0.01) {

				// upward rubberband
				if (offset.getValue() > 0) {

					// check if the offset area is empty
					if (!mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0, height, 0)).isEmpty()) {

						// max height
						rubberband = height;
					}
				}

				// downward rubberband
				else {

					// check if the offset area is empty
					if (!mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0, -height, 0)).isEmpty()) {

						// min height
						rubberband = height;
					}
				}
			}

			// return clamped offset
			return offset.getValue() > 0 ? Math.max(rubberband, 2.2) : Math.max(-rubberband, -2.2);
		}

		// preset offset by user input
		else if (mode.getValue().equals(Mode.STATIC)) {

			// set offset
			return offset.getValue();
		}

		return -1000;
	}

	/**
	 * Places a block at this position
	 * @param in the position
	 */
	private boolean placeBlock(BlockPos in) {

		// check if block is replaceable
		if (BlockUtil.isReplaceable(in)) {

			// place block
			getCosmos().getInteractionManager().placeBlock(in, rotate.getValue(), strict.getValue());

			// block placement was successful
			return true;
		}

		return false;
	}

	public enum Completion {

		/**
		 * Toggles the module when you have moved out of the block
		 */
		SHIFT,

		/**
		 * Toggles the module if the player is in a burrow
		 */
		FILLED,

		/**
		 * Does not dynamically toggle the module
		 */
		PERSISTENT
	}

	public enum Mode {

		/**
		 * Glitches up a pre-set offset
		 */
		STATIC,

		/**
		 * Dynamically determines the highest possible glitch height
		 */
		DYNAMIC,

		/**
		 * Burrow bait without the laggy glitching part
		 */
		STALL
	}
}