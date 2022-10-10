package cope.cosmos.client.features.modules.visual;

import cope.cosmos.client.events.entity.potion.PotionEffectEvent;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.events.render.entity.LayerArmorEvent;
import cope.cosmos.client.events.render.entity.RenderBeaconBeamEvent;
import cope.cosmos.client.events.render.entity.RenderItemEvent;
import cope.cosmos.client.events.render.entity.RenderWitherSkullEvent;
import cope.cosmos.client.events.render.gui.BossOverlayEvent;
import cope.cosmos.client.events.render.gui.RenderOverlayEvent;
import cope.cosmos.client.events.render.other.RenderEnchantmentTableBookEvent;
import cope.cosmos.client.events.render.other.RenderMapEvent;
import cope.cosmos.client.events.render.other.RenderParticleEvent;
import cope.cosmos.client.events.render.player.CrosshairBobEvent;
import cope.cosmos.client.events.render.player.HurtCameraEvent;
import cope.cosmos.client.events.render.player.ModifyFOVEvent;
import cope.cosmos.client.events.render.player.RenderItemActivationEvent;
import cope.cosmos.client.events.render.world.RenderFogEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketAnimation;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips, Surge
 * @since 06/08/2021
 */
public class NoRenderModule extends Module {
    public static NoRenderModule INSTANCE;

    public NoRenderModule() {
        super("NoRender", Category.VISUAL, "Prevents certain things from rendering");
        INSTANCE = this;
    }

    // **************************** overlays ****************************

    public static Setting<Boolean> overlays = new Setting<>("Overlays", true)
            .setDescription("Prevents overlays from rendering");

    public static Setting<Boolean> overlayFire = new Setting<>("FireOverlay", true)
            .setDescription("Prevents fire overlay from rendering")
            .setVisible(() -> overlays.getValue());

    public static Setting<Boolean> overlayLiquid = new Setting<>("LiquidOverlay", true)
            .setDescription("Prevents liquid overlay from rendering")
            .setVisible(() -> overlays.getValue());

    public static Setting<Boolean> overlayBlock = new Setting<>("BlockOverlay", true)
            .setDescription("Prevents block overlay from rendering")
            .setVisible(() -> overlays.getValue());

    public static Setting<Boolean> overlayBoss = new Setting<>("BossOverlay", true)
            .setDescription("Prevents boss bar overlay from rendering")
            .setVisible(() -> overlays.getValue());

    // **************************** fog ****************************

    public static Setting<Boolean> fog = new Setting<>("Fog", true)
            .setAlias("NoFog", "LiquidVision")
            .setDescription("Prevents fog from rendering");

    // **************************** other ****************************

    public static Setting<Boolean> armor = new Setting<>("Armor", true)
            .setAlias("NoArmor")
            .setDescription("Prevents armor from rendering");

    public static Setting<Boolean> items = new Setting<>("Items", false)
            .setAlias("NoItems")
            .setDescription("Prevents dropped items from rendering");

    public static Setting<Boolean> tileEntities = new Setting<>("TileEntities", false)
            .setDescription("Prevents tile entity effects (enchantment table books, beacon beams, etc.) from rendering");

    public static Setting<Boolean> barrier = new Setting<>("Barrier", true)
            .setDescription("Prevents barrier block signs from rendering");

    public static Setting<Boolean> maps = new Setting<>("Maps", false)
            .setDescription("Prevents maps from rendering");

    public static Setting<Boolean> totemAnimation = new Setting<>("TotemAnimation", false)
            .setAlias("PopAnimation")
            .setDescription("Removes the totem pop animation");

    public static Setting<Boolean> hurtCamera = new Setting<>("HurtCamera", true)
            .setDescription("Removes the hurt camera effect");

    public static Setting<Boolean> witherSkull = new Setting<>("WitherSkull", true)
            .setDescription("Prevents flying wither skulls from rendering");

    public static Setting<Boolean> potion = new Setting<>("Potion", false)
            .setDescription("Removes certain potion effects");

    public static Setting<Boolean> fov = new Setting<>("FOV", true)
            .setDescription("Removes the FOV modifier effect");

    public static Setting<Boolean> swing = new Setting<>("Swing", false)
            .setDescription("Prevents other player's swing animations from rendering");

    public static Setting<Boolean> noBob = new Setting<>("NoBob", true)
            .setDescription("Let's you have view bobbing on without the crosshair bobbing as well");

    @SubscribeEvent
    public void onRenderItem(RenderItemEvent event) {

        // prevent dropped items from rendering
        if (items.getValue()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPotion(PotionEffectEvent.PotionAdd event) {

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
    public void onRenderFog(RenderFogEvent event) {

        // prevent fog from rendering
        if (fog.getValue()) {
            event.setCanceled(true);
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
    public void onRenderParticle(RenderParticleEvent event) {

        // cancels barrier particles from rendering
        if (barrier.getValue() && event.getParticleType().equals(EnumParticleTypes.BARRIER)) {
            event.setCanceled(true);
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

    @SubscribeEvent
    public void onRenderItemActivation(RenderItemActivationEvent event) {

        // prevent the totem pop animation
        if (totemAnimation.getValue()) {
            event.setCanceled(true);
        }
    }
}