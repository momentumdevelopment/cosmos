package cope.cosmos.client.features.modules.miscellaneous;

import cope.cosmos.asm.mixins.accessor.IMinecraft;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.InventoryManager.Switch;
import cope.cosmos.util.player.InventoryUtil;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips
 * @since 10/12/2022
 */
public class AutoFishModule extends Module {
    public static AutoFishModule INSTANCE;

    public AutoFishModule() {
        super("AutoFish", Category.MISCELLANEOUS, "Automatically casts and reels a fishing rod");
        INSTANCE = this;
    }

    // **************************** general ****************************

    public static Setting<Boolean> inventory = new Setting<>("Inventory", true)
            .setAlias("OpenInventory")
            .setDescription("Allows you to fish while in the inventory");

    public static Setting<Double> castTick = new Setting<>("CastTicks",  0.0, 10.0, 20.0, 1)
            .setDescription("Ticks to wait before re-casting");

    public static Setting<Double> maxSoundDistance = new Setting<>("MaxSoundDistance",  0.1, 2.0, 20.0, 1)
            .setAlias("SoundRange")
            .setDescription("Maximum distance for the bob sound");

    // reel trackers
    private boolean reel;
    private int reelTicks;

    @Override
    public void onTick() {

        // cant reel while in a screen
        if (mc.currentScreen != null && !inventory.getValue()) {
            return;
        }

        // check if holding fishing rod
        if (!InventoryUtil.isHolding(Items.FISHING_ROD)) {

            // switch to fishing rod
            getCosmos().getInventoryManager().switchToItem(Items.FISHING_ROD, Switch.NORMAL);
        }

        // fishing rod hook
        EntityFishHook fish = mc.player.fishEntity;

        // re-cast
        if (fish == null) {

            // reel
            ((IMinecraft) mc).hookRightClickMouse();
            return;
        }

        // caught a fish
        if (mc.player.fishEntity.caughtEntity != null) {

            // reel
            ((IMinecraft) mc).hookRightClickMouse();
        }

        // we need to reel
        if (reel) {

            // reel delay
            if (reelTicks > 4) {

                // reel
                ((IMinecraft) mc).hookRightClickMouse();
                reel = false;
            }

            else {
                reelTicks++;
            }
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {

        // packet for water bobbing sound effects
        if (event.getPacket() instanceof SPacketSoundEffect && ((SPacketSoundEffect) event.getPacket()).getSound().equals(SoundEvents.ENTITY_BOBBER_SPLASH)) {

            // hooked fish
            EntityFishHook fish = mc.player.fishEntity;

            // check if player has caught a fish
            if (fish != null && fish.getAngler().equals(mc.player)) {

                // distance to bob sound
                double distance = fish.getPositionVector().distanceTo(new Vec3d(((SPacketSoundEffect) event.getPacket()).getX(), ((SPacketSoundEffect) event.getPacket()).getY(), ((SPacketSoundEffect) event.getPacket()).getZ()));

                // make sure sound is close enough to caught fish
                if (distance <= maxSoundDistance.getValue()) {

                    // check if the player is holding a fishing rod
                    if (InventoryUtil.isHolding(Items.FISHING_ROD)) {

                        // must reel
                        reel = true;
                        reelTicks = 0;
                    }
                }
            }
        }
    }
}
