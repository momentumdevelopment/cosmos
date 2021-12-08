package cope.cosmos.client.features.modules.movement;

import cope.cosmos.asm.mixins.accessor.ISPacketEntityVelocity;
import cope.cosmos.asm.mixins.accessor.ISPacketExplosion;
import cope.cosmos.client.events.EntityCollisionEvent;
import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.events.WaterCollisionEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraftforge.client.event.PlayerSPPushOutOfBlocksEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author bon55, linustouchtips
 * @since 04/06/2021
 */
@SuppressWarnings("unused")
public class Velocity extends Module {
	public static Velocity INSTANCE;

	public Velocity() {
		super("Velocity", Category.MOVEMENT, "Take no knockback.", () -> "H" + horizontal.getValue() + "%, V" + vertical.getValue() + "%");
		INSTANCE = this;
	}
	
	public static Setting<Double> horizontal = new Setting<>("Horizontal", 0.0, 0.0, 100.0, 2).setDescription("Horizontal velocity modifier");
	public static Setting<Double> vertical = new Setting<>("Vertical", 0.0, 0.0, 100.0, 2).setDescription("Vertical velocity modifier");

	public static Setting<Boolean> noPush = new Setting<>("NoPush", true).setDescription("Prevents being pushed");
	public static Setting<Boolean> entities = new Setting<>("Entities", true).setParent(noPush).setDescription("Prevents being pushed by entities");
	public static Setting<Boolean> blocks = new Setting<>("Blocks", true).setParent(noPush).setDescription("Prevents being pushed out of blocks");
	public static Setting<Boolean> liquid = new Setting<>("Liquid", true).setParent(noPush).setDescription("Prevents being pushed by liquids");

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
		// packet for velocity caused by factors that are not explosions
		if (event.getPacket() instanceof SPacketEntityVelocity) {
			// if our settings are 0, then we can cancel this packet
			if (horizontal.getValue() == 0 && vertical.getValue() == 0) {
				event.setCanceled(true);
			}

			else {
				// if we want to modify the velocity, then we update the packet's values
				SPacketEntityVelocity packet = (SPacketEntityVelocity) event.getPacket();
				if (packet.getEntityID() == mc.player.getEntityId()) {
					((ISPacketEntityVelocity) packet).setMotionX((((((ISPacketEntityVelocity) packet).getMotionX() / 100) * horizontal.getValue().intValue())));
					((ISPacketEntityVelocity) packet).setMotionY((((((ISPacketEntityVelocity) packet).getMotionY() / 100) * vertical.getValue().intValue())));
					((ISPacketEntityVelocity) packet).setMotionZ((((((ISPacketEntityVelocity) packet).getMotionZ() / 100) * horizontal.getValue().intValue())));
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
				((ISPacketExplosion) packet).setMotionX((((((ISPacketExplosion) packet).getMotionX() / 100) * horizontal.getValue().floatValue())));
				((ISPacketExplosion) packet).setMotionY((((((ISPacketExplosion) packet).getMotionY() / 100) * vertical.getValue().floatValue())));
				((ISPacketExplosion) packet).setMotionZ((((((ISPacketExplosion) packet).getMotionZ() / 100) * horizontal.getValue().floatValue())));
			}
		}
	}

	@SubscribeEvent
	public void onPush(PlayerSPPushOutOfBlocksEvent event) {
		// cancel velocity from blocks
		if (noPush.getValue() && blocks.getValue()) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onKnockback(LivingKnockBackEvent event) {
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
