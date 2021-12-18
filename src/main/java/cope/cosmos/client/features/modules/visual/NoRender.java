package cope.cosmos.client.features.modules.visual;

import cope.cosmos.client.events.*;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.PlayerUtil;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketAnimation;
import net.minecraft.network.play.server.SPacketParticles;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips
 * @since 06/08/2021
 */
@SuppressWarnings("unused")
public class NoRender extends Module {
    public static NoRender INSTANCE;

    public NoRender() {
        super("NoRender", Category.VISUAL, "Prevents certain things from rendering");
        INSTANCE = this;
    }

    // overlays
    public static Setting<Boolean> overlays = new Setting<>("Overlays", true).setDescription("Prevents overlays from rendering");
    public static Setting<Boolean> overlayFire = new Setting<>("Fire", true).setDescription("Prevents fire overlay from rendering").setParent(overlays);
    public static Setting<Boolean> overlayLiquid = new Setting<>("Liquid", true).setDescription("Prevents liquid overlay from rendering").setParent(overlays);
    public static Setting<Boolean> overlayBlock = new Setting<>("Block", true).setDescription("Prevents block overlay from rendering").setParent(overlays);
    public static Setting<Boolean> overlayBoss = new Setting<>("Boss", true).setDescription("Prevents boss bar overlay from rendering").setParent(overlays);

    // fog
    public static Setting<Boolean> fog = new Setting<>("Fog", true).setDescription("Prevents fog from rendering");
    public static Setting<Boolean> fogLiquid = new Setting<>("LiquidVision", true).setDescription("Clears fog in liquid").setParent(fog);
    public static Setting<Double> fogDensity = new Setting<>("Density", 0.0, 0.0, 20.0, 0).setDescription("Density of the fog").setParent(fog);

    // misc
    public static Setting<Boolean> armor = new Setting<>("Armor", true).setDescription("Prevents armor from rendering");
    public static Setting<Boolean> items = new Setting<>("Items", false).setDescription("Prevents dropped items from rendering");
    public static Setting<Boolean> fireworks = new Setting<>("Fireworks", false).setDescription("Prevents fireworks entities from rendering");
    public static Setting<Boolean> particles = new Setting<>("Particles", false).setDescription("Prevents laggy particles from rendering");
    public static Setting<Boolean> tileEntities = new Setting<>("TileEntities", false).setDescription("Prevents tile entity effects (enchantment table books, beacon beams, etc.) from rendering");
    public static Setting<Boolean> maps = new Setting<>("Maps", false).setDescription("Prevents maps from rendering");
    public static Setting<Boolean> skylight = new Setting<>("Skylight", false).setDescription("Prevents skylight updates from rendering");
    public static Setting<Boolean> hurtCamera = new Setting<>("HurtCamera", true).setDescription("Removes the hurt camera effect");
    public static Setting<Boolean> witherSkull = new Setting<>("WitherSkull", true).setDescription("Prevents flying wither skulls from rendering");
    public static Setting<Boolean> potion = new Setting<>("Potion", false).setDescription("Removes certain potion effects");
    public static Setting<Boolean> fov = new Setting<>("FOV", true).setDescription("Removes the FOV modifier effect");
    public static Setting<Boolean> swing = new Setting<>("Swing", false).setDescription("Prevents other player's swing animations from rendering");

    @Override
    public void onUpdate() {
        if (items.getValue()) {
            // remove all items from world
            mc.world.loadedEntityList.forEach(entity -> {
                if (entity instanceof EntityItem) {
                    mc.world.removeEntity(entity);
                }
            });
        }

        // remove blinding potion effects
        if (potion.getValue()) {
            if (mc.player.isPotionActive(MobEffects.BLINDNESS)) {
                mc.player.removePotionEffect(MobEffects.BLINDNESS);
            }

            // nausea blurs screen
            if (mc.player.isPotionActive(MobEffects.NAUSEA)) {
                mc.player.removePotionEffect(MobEffects.NAUSEA);
            }
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderOverlayEvent event) {
        if (overlays.getValue()) {
            // cancels fire hud overlay
            if (event.getOverlayType().equals(RenderBlockOverlayEvent.OverlayType.FIRE) && overlayFire.getValue()) {
                event.setCanceled(true);
            }

            // cancel water hud overlay
            if (event.getOverlayType().equals(RenderBlockOverlayEvent.OverlayType.WATER) && overlayLiquid.getValue()) {
                event.setCanceled(true);
            }

            // cancel water block overlay
            if (event.getOverlayType().equals(RenderBlockOverlayEvent.OverlayType.BLOCK) && overlayBlock.getValue()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onRenderBossOverlay(BossOverlayEvent event) {
        // cancel boss hud overlay
        event.setCanceled(overlayBoss.getValue());
    }

    @SubscribeEvent
    public void onRenderEnchantmentTableBook(RenderEnchantmentTableBookEvent event) {
        // cancels rendering of enchantment table books
        if (tileEntities.getValue()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderBeaconBeam(RenderBeaconBeamEvent event) {
        // cancels rendering of beacon beams
        if (tileEntities.getValue()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderSkylight(RenderSkylightEvent event) {
        // cancels skylight updates
        if (skylight.getValue()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderMap(RenderMapEvent event) {
        // cancels maps from rendering
        if (maps.getValue()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onLayerArmor(LayerArmorEvent event) {
        // cancels armor rendering
        if (armor.getValue()) {
            event.setCanceled(true);

            // removes model rendering
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
        // packet for particle spawns
        if (event.getPacket() instanceof SPacketParticles) {
            if (particles.getValue()) {
                // cancels particles from rendering
                if (((SPacketParticles) event.getPacket()).getParticleCount() > 200) {
                    event.setCanceled(true);
                }
            }
        }

        // packet for player swings
        if (event.getPacket() instanceof SPacketAnimation) {
            if (swing.getValue()) {
                // prevent server from rendering swing animations
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onHurtCamera(HurtCameraEvent event) {
        // cancels the hurt camera effect
        if (hurtCamera.getValue()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderWitherSkull(RenderWitherSkullEvent event) {
        // cancels wither skulls from rendering
        if (witherSkull.getValue()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderFog(RenderFogEvent event) {
        // cancels fog from rendering
        if (fog.getValue()) {

            // cancels fog from rendering in liquids
            if (!PlayerUtil.isInLiquid() && fogLiquid.getValue()) {
                return;
            }

            // sets the density of the fog
            if (fogDensity.getValue() > 0) {
                event.setDensity(fogDensity.getValue().floatValue());
            }

            else {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onFOVModifier(ModifyFOVEvent event) {
        // cancels fov modifier effects (from speed potions)
        if (fov.getValue()) {
            event.setCanceled(true);
        }
    }
}