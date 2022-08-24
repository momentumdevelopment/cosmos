package cope.cosmos.client.features.modules.combat;

import cope.cosmos.asm.mixins.accessor.IEntityPlayerSP;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.InventoryManager.Switch;
import cope.cosmos.client.manager.managers.InventoryManager.InventoryRegion;
import cope.cosmos.util.entity.EntityUtil;
import cope.cosmos.util.holder.Rotation;
import cope.cosmos.util.math.Timer;
import cope.cosmos.util.player.AngleUtil;
import cope.cosmos.util.world.BlockUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketAnimation;
import cope.cosmos.util.holder.Rotation.Rotate;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

/**
 * @author bon55, linustouchtips
 * @since 08/18/2022
 */
public class BurrowModule extends Module {
	public static BurrowModule INSTANCE;

	public BurrowModule() {
		super("Burrow", new String[] {"SelfFill", "RubberFill"}, Category.COMBAT, "Glitches you into a block");
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

	public static Setting<Switch> autoSwitch = new Setting<>("Switch", Switch.NORMAL)
			.setAlias("AutoSwitch", "Swap", "AutoSwap")
			.setDescription("How to switch when placing blocks");

	// clear
	private final Timer clearTimer = new Timer();

	@Override
	public void onEnable() {
		super.onEnable();

		// original block position
		BlockPos origin = new BlockPos(mc.player.posX, Math.round(mc.player.posY), mc.player.posZ);

		// only burrow on ground
		if (mc.player.onGround) {

			// check if we are already in a burrow
			if (!mc.world.getBlockState(origin).getMaterial().blocksMovement() && mc.player.collidedVertically) {

				// clear placement
				attackEntities(origin);

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
			}
		}

		// auto disabling module
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


	/**
	 * Attacks all entities that intersect (block) placements
	 * @param in The placement
	 */
	public void attackEntities(BlockPos in) {

		// player ping
		int ping = mc.player.connection.getPlayerInfo(mc.player.getUniqueID()).getResponseTime();

		// check unsafe entities and clear if necessary
		for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(in))) {

			// can be placed on
			if (entity == null || entity instanceof EntityItem || entity instanceof EntityXPOrb) {
				continue;
			}

			// make sure we aren't attacking too fast TODO: normalize delays based on packets in the future maybe???
			if (clearTimer.passedTime(ping <= 50 ? 75 : 100, Timer.Format.MILLISECONDS)) {

				// rotation to entity
				Rotation rotation = AngleUtil.calculateAngles(entity.getPositionVector());

				// rotate to block
				if (!rotate.getValue().equals(Rotate.NONE) && rotation.isValid()) {
					// rotate via packet, server should confirm instantly?
					switch (rotate.getValue()) {
						case CLIENT:
							mc.player.rotationYaw = rotation.getYaw();
							mc.player.rotationYawHead = rotation.getYaw();
							mc.player.rotationPitch = rotation.getPitch();
							break;
						case PACKET:

							// force a rotation - should this be done?
							mc.player.connection.sendPacket(new CPacketPlayer.Rotation(rotation.getYaw(), rotation.getPitch(), mc.player.onGround));

							// submit to rotation manager
							// getCosmos().getRotationManager().setRotation(blockAngles);

							// ((IEntityPlayerSP) mc.player).setLastReportedYaw(blockAngles[0]);
							// ((IEntityPlayerSP) mc.player).setLastReportedPitch(blockAngles[1]);
							break;
					}
				}

				// attack crystals that are in the way
				if (entity instanceof EntityEnderCrystal) {

					// player sprint state
					boolean sprintState = mc.player.isSprinting();

					// stop sprint
					if (strict.getValue()) {

						// stop sprinting when attacking an entity
						if (sprintState) {
							mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
						}
					}

					// attack
					mc.player.connection.sendPacket(new CPacketUseEntity(entity));
					mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));

					// stop sprint
					if (strict.getValue()) {

						// reset sprint state
						if (sprintState) {
							mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
						}
					}

					clearTimer.resetTime();
					break;
				}

				// attack vehicles 3 times
				else if (EntityUtil.isVehicleMob(entity)) {

					// attack
					for (int i = 0; i < 3; i++) {
						mc.player.connection.sendPacket(new CPacketUseEntity(entity));
						mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
					}

					clearTimer.resetTime();
					break;
				}
			}
		}
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