package cope.cosmos.client.features.modules.visual;

import cope.cosmos.client.events.*;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.PlayerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketParticles;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class NoRender extends Module {
    public static NoRender INSTANCE;

    public NoRender() {
        super("NoRender", Category.VISUAL, "Prevents certain things from rendering");
        INSTANCE = this;
    }

    public static Setting<Boolean> overlays = new Setting<>("Overlays", "Prevents overlays from rendering", true);
    public static Setting<Boolean> overlayFire = new Setting<>("Fire", "Prevents fire overlay from rendering", true).setParent(overlays);
    public static Setting<Boolean> overlayLiquid = new Setting<>("Liquid", "Prevents liquid overlay from rendering", true).setParent(overlays);
    public static Setting<Boolean> overlayBlock = new Setting<>("Block", "Prevents block overlay from rendering", true).setParent(overlays);
    public static Setting<Boolean> overlayBoss = new Setting<>("Boss", "Prevents boss bar overlay from rendering", true).setParent(overlays);

    public static Setting<Boolean> fog = new Setting<>("Fog", "Prevents fog from rendering", true);
    public static Setting<Boolean> fogLiquid = new Setting<>("LiquidVision", "Clears fog in liquid", true).setParent(fog);
    public static Setting<Double> fogDensity = new Setting<>("Density", "Density of the fog", 0.0, 0.0, 20.0, 0).setParent(fog);

    public static Setting<Boolean> armor = new Setting<>("Armor", "Prevents armor from rendering", true);
    public static Setting<Boolean> items = new Setting<>("Items", "Prevents dropped items from rendering", false);
    public static Setting<Boolean> particles = new Setting<>("Particles", "Prevents laggy particles from rendering", false);
    public static Setting<Boolean> tileEntities = new Setting<>("TileEntities", "Prevents tile entity effects (enchantment table books, beacon beams, etc.) from rendering", false);
    public static Setting<Boolean> maps = new Setting<>("Maps", "Prevents maps from rendering", false);
    public static Setting<Boolean> skylight = new Setting<>("Skylight", "Prevents skylight updates from rendering", false);
    public static Setting<Boolean> hurtCamera = new Setting<>("HurtCamera", "Removes the hurt camera effect", true);
    public static Setting<Boolean> witherSkull = new Setting<>("WitherSkull", "Prevents flying wither skulls from rendering", true);
    public static Setting<Boolean> potion = new Setting<>("Potion", "Removes certain potion effects", false);
    public static Setting<Boolean> fov = new Setting<>("FOV", "Removes the FOV modifier effect", true);

    @Override
    public void onUpdate() {
        if (items.getValue()) {
            for (Entity entity : new ArrayList<>(mc.world.loadedEntityList)) {
                if (entity instanceof EntityItem)
                    mc.world.removeEntity(entity);
            }
        }

        if (potion.getValue()) {
            if (mc.player.isPotionActive(MobEffects.BLINDNESS))
                mc.player.removePotionEffect(MobEffects.BLINDNESS);

            if (mc.player.isPotionActive(MobEffects.NAUSEA))
                mc.player.removePotionEffect(MobEffects.NAUSEA);
        }
    }
    
    @SubscribeEvent
    public void onRenderBlockOverlay(RenderBlockOverlayEvent event) {
        if (nullCheck() && overlays.getValue()) {
            if (event.getOverlayType().equals(RenderBlockOverlayEvent.OverlayType.FIRE) && overlayFire.getValue())
                event.setCanceled(true);

            if (event.getOverlayType().equals(RenderBlockOverlayEvent.OverlayType.WATER) && overlayLiquid.getValue())
                event.setCanceled(true);

            if (event.getOverlayType().equals(RenderBlockOverlayEvent.OverlayType.BLOCK) && overlayBlock.getValue())
                event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderBossOverlay(BossOverlayEvent event) {
        event.setCanceled(nullCheck() && overlayBoss.getValue());
    }

    @SubscribeEvent
    public void onRenderEnchantmentTableBook(RenderEnchantmentTableBookEvent event) {
        event.setCanceled(nullCheck() && tileEntities.getValue());
    }

    @SubscribeEvent
    public void onRenderBeaconBeam(RenderBeaconBeamEvent event) {
        event.setCanceled(nullCheck() && tileEntities.getValue());
    }

    @SubscribeEvent
    public void onRenderSkylight(RenderSkylightEvent event) {
        event.setCanceled(nullCheck() && skylight.getValue());
    }

    @SubscribeEvent
    public void onRenderMap(RenderMapEvent event) {
        event.setCanceled(nullCheck() && maps.getValue());
    }

    @SubscribeEvent
    public void onLayerArmor(LayerArmorEvent event) {
        if (nullCheck() && armor.getValue()) {
            event.setCanceled(true);
            switch (event.getEntityEquipmentSlot()) {
                case HEAD:
                    event.getModelBiped().bipedHead.showModel = false;
                    event.getModelBiped().bipedHeadwear.showModel = false;
                    break;
                case CHEST:
                    event.getModelBiped().bipedBody.showModel = false;
                    event.getModelBiped().bipedRightArm.showModel = false;
                    event.getModelBiped().bipedLeftArm.showModel = false;
                    break;
                case LEGS:
                    event.getModelBiped().bipedBody.showModel = false;
                    event.getModelBiped().bipedRightLeg.showModel = false;
                    event.getModelBiped().bipedLeftLeg.showModel = false;
                    break;
                case FEET:
                    event.getModelBiped().bipedRightLeg.showModel = false;
                    event.getModelBiped().bipedLeftLeg.showModel = false;
                    break;
                case MAINHAND:
                case OFFHAND:
                	break;
            }
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {
        if (event.getPacket() instanceof SPacketParticles && ((SPacketParticles) event.getPacket()).getParticleCount() > 200)
            event.setCanceled(true);
    }

    @SubscribeEvent
    public void onHurtCamera(HurtCameraEvent event) {
        event.setCanceled(nullCheck() && hurtCamera.getValue());
    }

    @SubscribeEvent
    public void onRenderWitherSkull(RenderWitherSkullEvent event) {
        event.setCanceled(nullCheck() && witherSkull.getValue());
    }
    
    @SubscribeEvent
    public void onRenderFog(EntityViewRenderEvent.FogDensity event) {
        if (nullCheck() && fog.getValue()) {
            if (!PlayerUtil.isInLiquid() && fogLiquid.getValue())
                return;

            event.setDensity(fogDensity.getValue().floatValue());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onFOVModifier(ModifyFOVEvent event) {
        event.setCanceled(nullCheck() && fov.getValue());
    }
}
