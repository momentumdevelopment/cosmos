package cope.cosmos.client.features.modules.visual;

import cope.cosmos.client.events.entity.PotionEvent;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.events.render.entity.LayerArmorEvent;
import cope.cosmos.client.events.render.entity.RenderBeaconBeamEvent;
import cope.cosmos.client.events.render.entity.RenderItemEvent;
import cope.cosmos.client.events.render.entity.RenderWitherSkullEvent;
import cope.cosmos.client.events.render.gui.BossOverlayEvent;
import cope.cosmos.client.events.render.gui.RenderOverlayEvent;
import cope.cosmos.client.events.render.other.RenderEnchantmentTableBookEvent;
import cope.cosmos.client.events.render.other.RenderMapEvent;
import cope.cosmos.client.events.render.player.CrosshairBobEvent;
import cope.cosmos.client.events.render.player.HurtCameraEvent;
import cope.cosmos.client.events.render.player.ModifyFOVEvent;
import cope.cosmos.client.events.render.world.RenderFogEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.PlayerUtil;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketAnimation;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips
 * @since 06/08/2021
 */
public class NoRenderModule extends Module {
    public static NoRenderModule INSTANCE;

    public NoRenderModule() {
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
    public static Setting<Boolean> tileEntities = new Setting<>("TileEntities", false).setDescription("Prevents tile entity effects (enchantment table books, beacon beams, etc.) from rendering");
    public static Setting<Boolean> maps = new Setting<>("Maps", false).setDescription("Prevents maps from rendering");
    public static Setting<Boolean> hurtCamera = new Setting<>("HurtCamera", true).setDescription("Removes the hurt camera effect");
    public static Setting<Boolean> witherSkull = new Setting<>("WitherSkull", true).setDescription("Prevents flying wither skulls from rendering");
    public static Setting<Boolean> potion = new Setting<>("Potion", false).setDescription("Removes certain potion effects");
    public static Setting<Boolean> fov = new Setting<>("FOV", true).setDescription("Removes the FOV modifier effect");
    public static Setting<Boolean> swing = new Setting<>("Swing", false).setDescription("Prevents other player's swing animations from rendering");
    public static Setting<Boolean> noBob = new Setting<>("NoBob", true).setDescription("Let's you have view bobbing on without the crosshair bobbing as well");

    @SubscribeEvent
    public void onRenderItem(RenderItemEvent event) {
        // prevent dropped items from rendering
        if (items.getValue()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPotion(PotionEvent event) {
        // remove blinding potion effects
        if (potion.getValue()) {
            if (event.getPotionEffect().getPotion().equals(MobEffects.BLINDNESS) && mc.player.isPotionActive(MobEffects.BLINDNESS)) {
                mc.player.removePotionEffect(MobEffects.BLINDNESS);
            }

            // nausea blurs screen
            if (event.getPotionEffect().getPotion().equals(MobEffects.NAUSEA) && mc.player.isPotionActive(MobEffects.NAUSEA)) {
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
        if (nullCheck()) {

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
    }

    @SubscribeEvent
    public void onFOVModifier(ModifyFOVEvent event) {
        // cancels fov modifier effects (from speed potions)
        if (fov.getValue()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onCrosshairBob(CrosshairBobEvent event) {
        // Lets the hand bobbing animation run without the crosshair bobbing as well
        if (noBob.getValue()) {
            event.setCanceled(true);
        }
    }
}