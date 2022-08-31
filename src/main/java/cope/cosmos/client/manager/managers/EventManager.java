package cope.cosmos.client.manager.managers;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.Cosmos.ClientType;
import cope.cosmos.client.events.block.LeftClickBlockEvent;
import cope.cosmos.client.events.combat.CriticalModifierEvent;
import cope.cosmos.client.events.combat.DeathEvent;
import cope.cosmos.client.events.combat.TotemPopEvent;
import cope.cosmos.client.events.entity.player.interact.EntityUseItemEvent;
import cope.cosmos.client.events.entity.player.interact.ItemInputUpdateEvent;
import cope.cosmos.client.events.entity.player.interact.RightClickItemEvent;
import cope.cosmos.client.events.entity.potion.PotionEffectEvent;
import cope.cosmos.client.events.motion.movement.KnockBackEvent;
import cope.cosmos.client.events.motion.movement.PushOutOfBlocksEvent;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.events.render.gui.RenderOverlayEvent;
import cope.cosmos.client.events.render.world.RenderFogColorEvent;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.ServiceModule;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.util.Wrapper;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * @author bon55, linustouchtips
 * @since 05/05/2021
 */
public class EventManager extends Manager implements Wrapper {

	public EventManager() {
		super("EventManager", "Manages Forge events");

		// register to event bus
		Cosmos.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onUpdate(LivingEvent.LivingUpdateEvent event) {

		// check if the update event is for the local player
		if (event.getEntity().getEntityWorld().isRemote && event.getEntityLiving().equals(mc.player)) {

			// runs on the entity update method
			mc.mcProfiler.startSection("cosmos-update");

			// module onUpdate
			for (Module module : getCosmos().getModuleManager().getAllModules()) {

				// check if the module is safe to run
				if (nullCheck() || getCosmos().getNullSafeFeatures().contains(module)) {

					// check if module should run
					if (module.isEnabled() || module instanceof ServiceModule) {

						// run
						try {
							module.onUpdate();
						} catch (Exception exception) {

							// print stacktrace if in dev environment
							if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
								exception.printStackTrace();
							}
						}
					}
				}
			}

			// manager onUpdate
			for (Manager manager : getCosmos().getAllManagers()) {

				// check if the manager is safe to run
				if (nullCheck() || getCosmos().getNullSafeFeatures().contains(manager)) {

					// run
					try {
						manager.onUpdate();
					} catch (Exception exception) {

						// print stacktrace if in dev environment
						if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
							exception.printStackTrace();
						}
					}
				}
			}

			// end section
			mc.mcProfiler.endSection();
		}
	}

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event) {

		// runs on game (root) tick
		mc.mcProfiler.startSection("cosmos-root-tick");

		// module onTick
		for (Module module : getCosmos().getModuleManager().getAllModules()) {

			// check if the module is safe to run
			if (nullCheck() || getCosmos().getNullSafeFeatures().contains(module)) {

				// check if module should run
				if (module.isEnabled() || module instanceof ServiceModule) {

					// run
					try {
						module.onTick();
					} catch (Exception exception) {

						// print stacktrace if in dev environment
						if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
							exception.printStackTrace();
						}
					}
				}
			}
		}

		// manager onTick
		for (Manager manager : getCosmos().getAllManagers()) {

			// check if the manager is safe to run
			if (nullCheck() || getCosmos().getNullSafeFeatures().contains(manager)) {

				// run
				try {
					manager.onTick();
				} catch (Exception exception) {

					// print stacktrace if in dev environment
					if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
						exception.printStackTrace();
					}
				}
			}
		}

		// end section
		mc.mcProfiler.endSection();
	}
	
	@SubscribeEvent
	public void onRender2d(RenderGameOverlayEvent.Text event) {

		// runs on every frame
		mc.mcProfiler.startSection("cosmos-render-2D");

		// module onRender2D
		for (Module module : getCosmos().getModuleManager().getAllModules()) {

			// check if the module is safe to run
			if (nullCheck() || getCosmos().getNullSafeFeatures().contains(module)) {

				// check if module should run
				if (module.isEnabled()) {

					// run
					try {
						module.onRender2D();
					} catch (Exception exception) {

						// print stacktrace if in dev environment
						if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
							exception.printStackTrace();
						}
					}
				}
			}
		}

		// manager onRender2D
		for (Manager manager : getCosmos().getAllManagers()) {

			// check if the manager is safe to run
			if (nullCheck() || getCosmos().getNullSafeFeatures().contains(manager)) {

				// run
				try {
					manager.onRender2D();
				} catch (Exception exception) {

					// print stacktrace if in dev environment
					if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
						exception.printStackTrace();
					}
				}
			}
		}

		// end section
		mc.mcProfiler.endSection();
	}
	
	@SubscribeEvent
	public void onRender3D(RenderWorldLastEvent event) {

		// runs on every frame
		mc.mcProfiler.startSection("cosmos-render-3D");

		// module onRender3D
		for (Module module : getCosmos().getModuleManager().getAllModules()) {

			// check if the module is safe to run
			if (nullCheck() || getCosmos().getNullSafeFeatures().contains(module)) {

				// check if module should run
				if (module.isEnabled()) {

					// run
					try {
						module.onRender3D();
					} catch (Exception exception) {

						// print stacktrace if in dev environment
						if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
							exception.printStackTrace();
						}
					}
				}
			}
		}

		// manager onRender3D
		for (Manager manager : getCosmos().getAllManagers()) {

			// check if the manager is safe to run
			if (nullCheck() || getCosmos().getNullSafeFeatures().contains(manager)) {

				// run
				try {
					manager.onRender3D();
				} catch (Exception exception) {

					// print stacktrace if in dev environment
					if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
						exception.printStackTrace();
					}
				}
			}
		}

		// end section
		mc.mcProfiler.endSection();
	}

	// **************************** EVENTS ****************************

	@SubscribeEvent
	public void onTotemPop(PacketEvent.PacketReceiveEvent event) {
		if (event.getPacket() instanceof SPacketEntityStatus && ((SPacketEntityStatus) event.getPacket()).getOpCode() == 35) {
			TotemPopEvent totemPopEvent = new TotemPopEvent(((SPacketEntityStatus) event.getPacket()).getEntity(mc.world));
			Cosmos.EVENT_BUS.post(totemPopEvent);

			if (totemPopEvent.isCanceled()) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void onCriticalHit(CriticalHitEvent event) {
		CriticalModifierEvent criticalModifierEvent = new CriticalModifierEvent();
		Cosmos.EVENT_BUS.post(criticalModifierEvent);

		// update damage modifier
		event.setDamageModifier(criticalModifierEvent.getDamageModifier());
	}

	@SubscribeEvent
	public void onInputUpdate(InputUpdateEvent event) {
		ItemInputUpdateEvent itemInputUpdateEvent = new ItemInputUpdateEvent(event.getMovementInput());
		Cosmos.EVENT_BUS.post(itemInputUpdateEvent);
	}

	@SubscribeEvent
	public void onLivingEntityUseItem(LivingEntityUseItemEvent event) {
		EntityUseItemEvent entityUseItemEvent = new EntityUseItemEvent();
		Cosmos.EVENT_BUS.post(entityUseItemEvent);
	}

	@SubscribeEvent
	public void onKnockback(LivingKnockBackEvent event) {
		KnockBackEvent knockBackEvent = new KnockBackEvent();
		Cosmos.EVENT_BUS.post(knockBackEvent);

		if (knockBackEvent.isCanceled()) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
		if (event.getEntityPlayer().equals(mc.player)) {
			RightClickItemEvent rightClickItemEvent = new RightClickItemEvent(event.getItemStack());
			Cosmos.EVENT_BUS.post(rightClickItemEvent);

			if (rightClickItemEvent.isCanceled()) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		LeftClickBlockEvent leftClickBlockEvent = new LeftClickBlockEvent(event.getPos(), event.getFace());
		Cosmos.EVENT_BUS.post(leftClickBlockEvent);
	}

	@SubscribeEvent
	public void onPushOutOfBlocks(PlayerSPPushOutOfBlocksEvent event) {
		if (event.getEntity().equals(mc.player)) {
			PushOutOfBlocksEvent pushOutOfBlocksEvent = new PushOutOfBlocksEvent();
			Cosmos.EVENT_BUS.post(pushOutOfBlocksEvent);

			if (pushOutOfBlocksEvent.isCanceled()) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void onDeath(LivingDeathEvent event) {
		DeathEvent deathEvent = new DeathEvent(event.getEntity());
		Cosmos.EVENT_BUS.post(deathEvent);
	}

	@SubscribeEvent
	public void onRenderBlockOverlay(RenderBlockOverlayEvent event) {
		RenderOverlayEvent renderOverlayEvent = new RenderOverlayEvent(event.getOverlayType());
		Cosmos.EVENT_BUS.post(renderOverlayEvent);

		if (renderOverlayEvent.isCanceled()) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onFogColor(EntityViewRenderEvent.FogColors event) {
		RenderFogColorEvent fogColorEvent = new RenderFogColorEvent();
		Cosmos.EVENT_BUS.post(fogColorEvent);

		// update fog colors
		if (fogColorEvent.isCanceled()) {
			event.setRed(fogColorEvent.getColor().getRed() / 255F);
			event.setGreen(fogColorEvent.getColor().getGreen() / 255F);
			event.setBlue(fogColorEvent.getColor().getBlue() / 255F);
		}
	}

	@SubscribeEvent
	public void onPotionAdd(PotionEvent.PotionApplicableEvent event) {

		if (nullCheck() && mc.player.equals(event.getEntity())) {
			PotionEffectEvent.PotionAdd potionAddEvent = new PotionEffectEvent.PotionAdd(event.getPotionEffect());
			Cosmos.EVENT_BUS.post(potionAddEvent);

			if (potionAddEvent.isCanceled()) {
				event.setResult(Result.DENY);
			}
		}
	}

	@SubscribeEvent
	public void onPotionRemove(PotionEvent.PotionRemoveEvent event) {

		if (nullCheck() && mc.player.equals(event.getEntity())) {

			PotionEffectEvent.PotionRemove potionRemoveEvent = new PotionEffectEvent.PotionRemove(event.getPotion());
			Cosmos.EVENT_BUS.post(potionRemoveEvent);
		}
	}

	@SubscribeEvent
	public void onPotionExpiry(PotionEvent.PotionExpiryEvent event) {

		if (nullCheck() && mc.player.equals(event.getEntity())) {

			PotionEffectEvent.PotionRemove potionRemoveEvent = new PotionEffectEvent.PotionRemove(event.getPotionEffect());
			Cosmos.EVENT_BUS.post(potionRemoveEvent);
		}
	}
}
