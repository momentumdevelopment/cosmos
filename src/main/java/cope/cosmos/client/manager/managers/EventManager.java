package cope.cosmos.client.manager.managers;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.*;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.util.Wrapper;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;

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
		// runs on the entity update method
		if (event.getEntity().getEntityWorld().isRemote && event.getEntityLiving().equals(mc.player)) {
			getCosmos().getModuleManager().getAllModules().forEach(module -> {
				if ((nullCheck() || getCosmos().getNullSafeFeatures().contains(module)) && module.isEnabled()) {
					try {
						module.onUpdate();
					} catch (Exception exception) {
						exception.printStackTrace();
					}
				}
			});

			getCosmos().getManagers().forEach(manager -> {
				if (nullCheck()) {
					try {
						manager.onUpdate();
					} catch (Exception exception) {
						exception.printStackTrace();
					}
				}
			});
		}

		/*
		if (mc.currentScreen instanceof GuiMainMenu && !Cosmos.SETUP)
			mc.displayGuiScreen(new SetUpGUI());
		 */
	}

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event) {
		getCosmos().getModuleManager().getAllModules().forEach(module -> {
			if ((nullCheck() || getCosmos().getNullSafeFeatures().contains(module)) && module.isEnabled()) {
				try {
					module.onTick();
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
		});

		getCosmos().getManagers().forEach(manager -> {
			try {
				if (nullCheck()) {
					manager.onTick();
				}
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		});
	}
	
	@SubscribeEvent
	public void onRender2d(RenderGameOverlayEvent.Text event) {
		getCosmos().getModuleManager().getAllModules().forEach(module -> {
			if (nullCheck() && module.isEnabled()) {
				try {
					module.onRender2D();
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
		});

		getCosmos().getManagers().forEach(manager -> {
			if (nullCheck()) {
				try {
					manager.onRender2D();
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
		});
	}
	
	@SubscribeEvent
	public void onRender3D(RenderWorldLastEvent event) {
		mc.mcProfiler.startSection("cosmos-render");

		getCosmos().getModuleManager().getAllModules().forEach(module -> {
			if (nullCheck() && module.isEnabled()) {
				try {
					module.onRender3D();
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
		});

		getCosmos().getManagers().forEach(manager -> {
			if (nullCheck()) {
				try {
					manager.onRender3D();
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
		});

		mc.mcProfiler.endSection();
	}
	
	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {
		// pressed key
		int key = Keyboard.getEventKey();

		// toggle
		if (key != 0 && Keyboard.getEventKeyState()) {
			getCosmos().getModuleManager().getAllModules().forEach(moduleule -> {
				if (moduleule.getKey() == key) {
					moduleule.toggle();
				}
			});
		}
	}

	@SubscribeEvent
	public void onChatInput(ClientChatEvent event) {
		// event the user send a command
		if (event.getMessage().startsWith(Cosmos.PREFIX)) {
			event.setCanceled(true);

			try {
				getCosmos().getCommandDispatcher().execute(getCosmos().getCommandDispatcher().parse(event.getOriginalMessage().substring(1), 1));
			} catch (Exception exception) {
				// exception.printStackTrace();
				getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "An error occurred!", "No such command was found");
			}
		}
	}

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

	// converter

	@SubscribeEvent
	public void onCriticalHit(CriticalHitEvent event) {
		CriticalModifierEvent criticalModifierEvent = new CriticalModifierEvent();
		Cosmos.EVENT_BUS.post(criticalModifierEvent);

		// update damage moduleifier
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
	public void onFogDensity(EntityViewRenderEvent.FogDensity event) {
		RenderFogEvent renderFogEvent = new RenderFogEvent(event.getDensity());
		Cosmos.EVENT_BUS.post(renderFogEvent);

		event.setDensity(renderFogEvent.getDensity());

		if (renderFogEvent.isCanceled()) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onFogColor(EntityViewRenderEvent.FogColors event) {
		RenderFogColorEvent fogColorEvent = new RenderFogColorEvent(new Color(event.getRed(), event.getGreen(), event.getBlue()));
		Cosmos.EVENT_BUS.post(fogColorEvent);

		event.setRed(fogColorEvent.getColor().getRed());
		event.setGreen(fogColorEvent.getColor().getGreen());
		event.setBlue(fogColorEvent.getColor().getBlue());
	}
}
