package cope.cosmos.client.manager.managers;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.events.Render3DEvent;
import cope.cosmos.client.events.TotemPopEvent;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.util.Wrapper;
import cope.cosmos.util.client.ChatUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.lwjgl.input.Keyboard;

import cope.cosmos.client.Cosmos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import org.lwjgl.opengl.GL11;

@SuppressWarnings("unused")
public class EventManager extends Manager implements Wrapper {
	public EventManager() {
		super("EventManager", "Manages Forge events", 3);
	}

	public static final EventManager INSTANCE = new EventManager();
	
	@SubscribeEvent
	public void onUpdate(LivingEvent.LivingUpdateEvent event) {
		ModuleManager.getAllModules().forEach(mod -> {
			if (event.getEntity().getEntityWorld().isRemote && event.getEntityLiving().equals(mc.player) && (nullCheck() || Cosmos.INSTANCE.getNullSafeMods().contains(mod)) && mod.isEnabled()) {
				try {
					mod.onUpdate();
				} catch (Exception e) { e.printStackTrace(); }
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
				try { mod.onRender2d(); }
				catch (Exception e) { e.printStackTrace(); }
			}
		});
	}
	
	@SubscribeEvent
	public void onRender3D(RenderWorldLastEvent event) {
		if (event.isCanceled()) {
			return;
		}
		mc.mcProfiler.startSection("cosmos-render");
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.disableDepth();
		GlStateManager.glLineWidth(1f);
		Render3DEvent render3dEvent = new Render3DEvent(event.getPartialTicks());
		ModuleManager.onRender3D(render3dEvent);
		GlStateManager.glLineWidth(1f);
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();
		GlStateManager.enableDepth();
		GlStateManager.enableCull();
		GlStateManager.enableCull();
		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.enableDepth();
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
				Cosmos.INSTANCE.getCommandDispatcher().execute(Cosmos.INSTANCE.getCommandDispatcher().parse(event.getOriginalMessage().substring(1), 1));
			} catch (Exception exception) {
				exception.printStackTrace();
				ChatUtil.sendHoverableMessage(ChatFormatting.RED + "An error occured!", "No such command was found");
			}
		}
	}

	@SubscribeEvent
	public void onTotemPop(PacketEvent.PacketReceiveEvent event) {
		if (event.getPacket() instanceof SPacketEntityStatus && ((SPacketEntityStatus) event.getPacket()).getOpCode() == 35) {
			TotemPopEvent totemPopEvent = new TotemPopEvent(((SPacketEntityStatus) event.getPacket()).getEntity(mc.world));
			MinecraftForge.EVENT_BUS.post(totemPopEvent);

			if (totemPopEvent.isCanceled())
				event.setCanceled(true);
		}
	}
}
