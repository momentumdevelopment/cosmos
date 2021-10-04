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

@SuppressWarnings("unused")
public class Velocity extends Module {
	public static Velocity INSTANCE;

	public Velocity() {
		super("Velocity", Category.MOVEMENT, "Take no knockback.", () -> "H" + horizontal.getValue() + "%, V" + vertical.getValue() + "%");
		INSTANCE = this;
	}
	
	public static Setting<Double> horizontal = new Setting<>("Horizontal", "Horizontal velocity modifier", 0.0, 0.0, 100.0, 2);
	public static Setting<Double> vertical = new Setting<>("Vertical", "Vertical velocity modifier", 0.0, 0.0, 100.0, 2);

	public static Setting<Boolean> noPush = new Setting<>("NoPush", "Prevents being pushed", true);
	public static Setting<Boolean> entities = new Setting<>("Entities", "Prevents being pushed by entities", true).setParent(noPush);
	public static Setting<Boolean> blocks = new Setting<>("Blocks", "Prevents being pushed out of blocks", true).setParent(noPush);
	public static Setting<Boolean> liquid = new Setting<>("Liquid", "Prevents being pushed by liquids", true).setParent(noPush);

	float collisionReduction;

	@Override
	public void onUpdate() {
		if (noPush.getValue()) {
			mc.player.entityCollisionReduction = 1;
		}
	}

	@Override
	public void onEnable() {
		super.onEnable();
		collisionReduction = mc.player.entityCollisionReduction;
	}

	@Override
	public void onDisable() {
		super.onDisable();
		mc.player.entityCollisionReduction = collisionReduction;
	}

	@SubscribeEvent
	public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {
		if (nullCheck()) {
			if (event.getPacket() instanceof SPacketEntityVelocity) {
				if (horizontal.getValue() == 0 && vertical.getValue() == 0) {
					event.setCanceled(true);
					return;
				}

				SPacketEntityVelocity packet = (SPacketEntityVelocity) event.getPacket();
				if (packet.getEntityID() == mc.player.getEntityId()) {
					((ISPacketEntityVelocity) packet).setMotionX((((((ISPacketEntityVelocity) packet).getMotionX() / 100) * horizontal.getValue().intValue())));
					((ISPacketEntityVelocity) packet).setMotionY((((((ISPacketEntityVelocity) packet).getMotionY() / 100) * vertical.getValue().intValue())));
					((ISPacketEntityVelocity) packet).setMotionZ((((((ISPacketEntityVelocity) packet).getMotionZ() / 100) * horizontal.getValue().intValue())));
				}
			}

			if (event.getPacket() instanceof SPacketExplosion) {
				if (horizontal.getValue() == 0 && vertical.getValue() == 0) {
					event.setCanceled(true);
					return;
				}

				SPacketExplosion packet = (SPacketExplosion) event.getPacket();
				((ISPacketExplosion) packet).setMotionX((float) (((((ISPacketExplosion) packet).getMotionX() / 100) * horizontal.getValue())));
				((ISPacketExplosion) packet).setMotionY((float) (((((ISPacketExplosion) packet).getMotionY() / 100) * vertical.getValue())));
				((ISPacketExplosion) packet).setMotionZ((float) (((((ISPacketExplosion) packet).getMotionZ() / 100) * horizontal.getValue())));
			}
		}
	}

	@SubscribeEvent
	public void onPush(PlayerSPPushOutOfBlocksEvent event) {
		event.setCanceled(nullCheck() && noPush.getValue() && blocks.getValue());
	}

	@SubscribeEvent
	public void onKnockback(LivingKnockBackEvent event) {
		event.setCanceled(nullCheck() && horizontal.getValue() == 0 && vertical.getValue() == 0);
	}

	@SubscribeEvent
	public void onEntityCollision(EntityCollisionEvent event) {
		event.setCanceled(nullCheck() && noPush.getValue() && entities.getValue());
	}

	@SubscribeEvent
	public void onWaterCollision(WaterCollisionEvent event) {
		event.setCanceled(nullCheck() && noPush.getValue() && liquid.getValue());
	}
}
