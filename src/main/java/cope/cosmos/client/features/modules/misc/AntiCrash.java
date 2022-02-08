package cope.cosmos.client.features.modules.misc;

import cope.cosmos.asm.mixins.accessor.IRenderGlobal;
import cope.cosmos.client.events.entity.EntityWorldEvent;
import cope.cosmos.client.events.client.ExceptionThrownEvent;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.events.render.world.RenderSkylightEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.InventoryUtil;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.server.SPacketParticles;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

/**
 * @author linustouchtips
 * @since 12/25/2021
 */
public class AntiCrash extends Module {
    public static AntiCrash INSTANCE;

    public AntiCrash() {
        super("AntiCrash", Category.MISC,"Prevents you from being kicked/crashed by various exploits");
        INSTANCE = this;
    }

    // crash/kick exploits
    public static Setting<Boolean> packets = new Setting<>("Packet", false).setDescription("Prevents you from getting kicked for invalid packets");
    public static Setting<Boolean> offhand = new Setting<>("Offhand", false).setDescription("Prevents you from getting crashed from item equip sounds");
    public static Setting<Boolean> fireworks = new Setting<>("Fireworks", true).setDescription("Prevents firework spam from crashing you");
    public static Setting<Boolean> skylight = new Setting<>("Skylight", true).setDescription("Prevents skylight updates from crashing you");
    public static Setting<Boolean> particles = new Setting<>("Particles", false).setDescription("Prevents laggy particles from crashing you");
    public static Setting<Boolean> slime = new Setting<>("Slime", false).setDescription("Prevents large slime entities from crashing you");
    public static Setting<Boolean> signs = new Setting<>("Signs", false).setDescription("Prevents signs from kicking you when broken");

    // sign info
    private final List<BlockPos> placedSigns = new ArrayList<>();

    @Override
    public void onUpdate() {
        // we are in a sign gui
        if (mc.currentScreen instanceof GuiEditSign) {
           if (signs.getValue()) {
               // check if the sign is being broken
               ((IRenderGlobal) mc.renderGlobal).getDamagedBlocks().forEach((integer, destroyBlockProgress) -> {

                   // sign we placed
                   if (placedSigns.contains(destroyBlockProgress.getPosition())) {

                       // close the gui screen
                       mc.player.closeScreen();
                   }
               });
           }
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (slime.getValue()) {
            mc.world.loadedEntityList.forEach(entity -> {
                // remove if the slime size is too large
                if (entity instanceof EntitySlime && ((EntitySlime) entity).getSlimeSize() > 4) {
                    mc.world.removeEntityDangerously(entity);
                }
            });
        }
    }

    @SubscribeEvent
    public void onExceptionThrown(ExceptionThrownEvent event) {
        // prevent the exception from being thrown
        if (event.getException() instanceof NullPointerException || event.getException() instanceof IOException || event.getException() instanceof ConcurrentModificationException) {
            if (packets.getValue()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
            // player is placing a sign
            if (InventoryUtil.isHolding(Items.SIGN)) {
                placedSigns.add(((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getPos());
            }
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {
        if (event.getPacket() instanceof SPacketSoundEffect && ((SPacketSoundEffect) event.getPacket()).getSound().equals(SoundEvents.ITEM_ARMOR_EQUIP_GENERIC)) {
            // prevent item equip sounds from being played
            if (offhand.getValue()) {
                event.setCanceled(true);
            }
        }

        // packet for particle spawns (for 254n_m's crash plugin)
        if (event.getPacket() instanceof SPacketParticles) {
            if (particles.getValue()) {
                // cancels particles from rendering
                if (((SPacketParticles) event.getPacket()).getParticleCount() > 200) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntitySpawn(EntityWorldEvent.EntitySpawnEvent event) {
        if (event.getEntity() instanceof EntityFireworkRocket) {
            // prevent firework rocket entities from spawning
            if (fireworks.getValue() && !((EntityFireworkRocket) event.getEntity()).isAttachedToEntity()) {
                event.setCanceled(true);
            }
        }

        if (event.getEntity() instanceof EntitySlime && ((EntitySlime) event.getEntity()).getSlimeSize() > 4) {
            // prevent the entity from spawning if the slime size is too large
            if (slime.getValue()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onEntityUpdate(EntityWorldEvent.EntityUpdateEvent event) {
        if (event.getEntity() instanceof EntityFireworkRocket) {
            // prevent firework rocket entities from updating
            if (fireworks.getValue() && !((EntityFireworkRocket) event.getEntity()).isAttachedToEntity()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onRenderSkylight(RenderSkylightEvent event) {
        // cancels skylight updates
        if (skylight.getValue()) {
            event.setCanceled(true);
        }
    }
}
