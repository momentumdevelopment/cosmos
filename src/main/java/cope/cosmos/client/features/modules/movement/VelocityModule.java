package cope.cosmos.client.features.modules.movement;

import cope.cosmos.asm.mixins.accessor.ISPacketEntityVelocity;
import cope.cosmos.asm.mixins.accessor.ISPacketExplosion;
import cope.cosmos.client.events.motion.collision.EntityCollisionEvent;
import cope.cosmos.client.events.motion.movement.KnockBackEvent;
import cope.cosmos.client.events.motion.movement.PushOutOfBlocksEvent;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.events.block.WaterCollisionEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author bon55, linustouchtips
 * @since 04/06/2021
 */
public class VelocityModule extends Module {
	public static VelocityModule INSTANCE;

	public VelocityModule() {
		super("Velocity", new String[] {"AntiKnockback", "AntiKB"}, Category.MOVEMENT, "Take no knockback.", () -> "H" + horizontal.getValue() + "%, V" + vertical.getValue() + "%");
		INSTANCE = this;
	}

	// **************************** modifiers ****************************

	public static Setting<Double> horizontal = new Setting<>("Horizontal", 0.0, 0.0, 100.0, 2)
			.setDescription("Horizontal velocity modifier");

	public static Setting<Double> vertical = new Setting<>("Vertical", 0.0, 0.0, 100.0, 2)
			.setDescription("Vertical velocity modifier");

	// **************************** no push ****************************

	public static Setting<Boolean> noPush = new Setting<>("NoPush", true)
			.setDescription("Prevents being pushed");

	public static Setting<Boolean> entities = new Setting<>("Entities", true)
			.setAlias("NoPushEntities")
			.setDescription("Prevents being pushed by entities")
			.setVisible(() -> noPush.getValue());

	public static Setting<Boolean> blocks = new Setting<>("Blocks", true)
			.setAlias("NoPushBlocks")
			.setDescription("Prevents being pushed out of blocks")
			.setVisible(() -> noPush.getValue());

	public static Setting<Boolean> liquid = new Setting<>("Liquid", true)
			.setAlias("NoPushLiquids")
			.setDescription("Prevents being pushed by liquids")
			.setVisible(() -> noPush.getValue());

	public static Setting<Boolean> fishHook = new Setting<>("Fishhooks", true)
			.setAlias("Bobbers")
			.setDescription("Prevents being pulled by fishhooks")
			.setVisible(() -> noPush.getValue());

	// previous collision reduction
	private float collisionReduction;

	@Override
	public void onUpdate() {
		if (noPush.getValue() && entities.getValue()) {

			// remove collision reduction
			mc.player.entityCollisionReduction = 1;
		}
	}

	@Override
	public void onEnable() {
		super.onEnable();

		// save previous collision reduction
		collisionReduction = mc.player.entityCollisionReduction;
	}

	@Override
	public void onDisable() {
		super.onDisable();

		// reapply previous collision reduction
		mc.player.entityCollisionReduction = collisionReduction;
	}

	@SubscribeEvent
	public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {

		if (nullCheck()) {

			// packet for velocity caused by factors that are not explosions
			if (event.getPacket() instanceof SPacketEntityVelocity) {

				// only apply to own player
				if (((SPacketEntityVelocity) event.getPacket()).getEntityID() == mc.player.getEntityId()) {

					// if our settings are 0, then we can cancel this packet
					if (horizontal.getValue() == 0 && vertical.getValue() == 0) {
						event.setCanceled(true);
					}

					else {

						// if we want to modify the velocity, then we update the packet's values
						SPacketEntityVelocity packet = (SPacketEntityVelocity) event.getPacket();
						if (packet.getEntityID() == mc.player.getEntityId()) {

							// motion from the packet
							int motionX = ((ISPacketEntityVelocity) packet).getMotionX() / 100;
							int motionY = ((ISPacketEntityVelocity) packet).getMotionY() / 100;
							int motionZ = ((ISPacketEntityVelocity) packet).getMotionZ() / 100;

							// modify motion
							((ISPacketEntityVelocity) packet).setMotionX(motionX * horizontal.getValue().intValue());
							((ISPacketEntityVelocity) packet).setMotionY(motionY * vertical.getValue().intValue());
							((ISPacketEntityVelocity) packet).setMotionZ(motionZ * horizontal.getValue().intValue());
						}
					}
				}
			}

			// packet for velocity caused by explosions
			if (event.getPacket() instanceof SPacketExplosion) {

				// if our settings are 0, then we can cancel this packet
				if (horizontal.getValue() == 0 && vertical.getValue() == 0) {
					event.setCanceled(true);
				}

				else {
					// if we want to modify the velocity, then we update the packet's values
					SPacketExplosion packet = (SPacketExplosion) event.getPacket();

					// motion from the packet
					float motionX = ((ISPacketExplosion) packet).getMotionX() / 100;
					float motionY = ((ISPacketExplosion) packet).getMotionY() / 100;
					float motionZ = ((ISPacketExplosion) packet).getMotionZ() / 100;

					// modify motion
					((ISPacketExplosion) packet).setMotionX(motionX * horizontal.getValue().floatValue());
					((ISPacketExplosion) packet).setMotionY(motionY * vertical.getValue().floatValue());
					((ISPacketExplosion) packet).setMotionZ(motionZ * horizontal.getValue().floatValue());
				}
			}

			// packet for being pulled by fishhooks
			if (event.getPacket() instanceof SPacketEntityStatus && ((SPacketEntityStatus) event.getPacket()).getOpCode() == 31) {
				if (fishHook.getValue()) {

					// get the entity that is pulling us
					Entity entity = ((SPacketEntityStatus) event.getPacket()).getEntity(mc.world);

					// check if it's a fishhook
					if (entity instanceof EntityFishHook) {

						// cancel the pull
						EntityFishHook entityFishHook = (EntityFishHook) entity;
						if (entityFishHook.caughtEntity.equals(mc.player)) {
							event.setCanceled(true);
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onPushOutOfBlocks(PushOutOfBlocksEvent event) {

		// cancel velocity from blocks
		if (noPush.getValue() && blocks.getValue()) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onKnockback(KnockBackEvent event) {

		// cancel velocity from knockback
		if (horizontal.getValue() == 0 && vertical.getValue() == 0) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onEntityCollision(EntityCollisionEvent event) {

		// cancel velocity from entities
		if (noPush.getValue() && entities.getValue()) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onWaterCollision(WaterCollisionEvent event) {

		// cancel velocity from liquids
		if (noPush.getValue() && liquid.getValue()) {
			event.setCanceled(true);
		}
	}
}
