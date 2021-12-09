package cope.cosmos.client.manager.managers;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.events.TotemPopEvent;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.event.annotation.Subscription;
import cope.cosmos.util.Wrapper;
import cope.cosmos.util.client.ChatUtil;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import org.lwjgl.input.Keyboard;

@SuppressWarnings("unused")
public class EventManager extends Manager implements Wrapper {

	public static final EventManager INSTANCE = new EventManager();

	public EventManager() {
		super("EventManager", "Manages Forge events");
	}

	@SubscribeEvent
	public void onUpdate(LivingEvent.LivingUpdateEvent event) {
		ModuleManager.getAllModules().forEach(mod -> {
			if (event.getEntity().getEntityWorld().isRemote && event.getEntityLiving().equals(mc.player) && (nullCheck() || getCosmos().getNullSafeMods().contains(mod)) && mod.isEnabled()) {
				try {
					mod.onUpdate();
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

		/*
		if (mc.currentScreen instanceof GuiMainMenu && !Cosmos.SETUP)
			mc.displayGuiScreen(new SetUpGUI());
		 */
	}
	
	@SubscribeEvent
	public void onRender2d(RenderGameOverlayEvent.Text event) {
		ModuleManager.getAllModules().forEach(mod -> {
			if (nullCheck() && mod.isEnabled()) {
				try {
					mod.onRender2D();
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

		ModuleManager.getAllModules().forEach(mod -> {
			if (nullCheck() && mod.isEnabled()) {
				try {
					mod.onRender3D();
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
		ModuleManager.getAllModules().forEach(mod -> {
			if (Keyboard.isKeyDown(mod.getKey()) && !Keyboard.isKeyDown(Keyboard.KEY_NONE)) {
				mod.toggle();
			}
		});
	}

	@SubscribeEvent
	public void onChatInput(ClientChatEvent event) {
		if (event.getMessage().startsWith(Cosmos.PREFIX)) {
			event.setCanceled(true);

			try {
				getCosmos().getCommandDispatcher().execute(Cosmos.INSTANCE.getCommandDispatcher().parse(event.getOriginalMessage().substring(1), 1));
			} catch (Exception exception) {
				// exception.printStackTrace();
				ChatUtil.sendHoverableMessage(ChatFormatting.RED + "An error occured!", "No such command was found");
			}
		}
	}

	@Subscription
	public void onTotemPop(PacketEvent.PacketReceiveEvent event) {
		if (event.getPacket() instanceof SPacketEntityStatus && ((SPacketEntityStatus) event.getPacket()).getOpCode() == 35) {
			TotemPopEvent totemPopEvent = new TotemPopEvent(((SPacketEntityStatus) event.getPacket()).getEntity(mc.world));
			Cosmos.EVENT_BUS.dispatch(totemPopEvent);

			if (totemPopEvent.isCanceled()) {
				event.setCanceled(true);
			}
		}
	}
}
