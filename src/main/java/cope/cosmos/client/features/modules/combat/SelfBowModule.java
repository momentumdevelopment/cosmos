package cope.cosmos.client.features.modules.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.InventoryManager.InventoryRegion;
import cope.cosmos.client.manager.managers.InventoryManager.Switch;
import cope.cosmos.util.player.InventoryUtil;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTippedArrow;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author linustouchtips
 * @since 09/05/2022
 */
public class SelfBowModule extends Module {
    public static SelfBowModule INSTANCE;

    public SelfBowModule() {
        super("SelfBow", new String[] {"Quiver"}, Category.COMBAT, "Shoots the player with positive tipped arrows");
        INSTANCE = this;
    }

    public static Setting<Boolean> hotbar = new Setting<>("Hotbar", false)
            .setAlias("Recursive")
            .setDescription("Allow hotbar arrows to be used");

    // positive potion effects
    private final List<Potion> potionEffects = Arrays.asList(
            MobEffects.ABSORPTION,
            MobEffects.REGENERATION,
            MobEffects.STRENGTH,
            MobEffects.SPEED,
            MobEffects.JUMP_BOOST,
            MobEffects.LUCK,
            MobEffects.HASTE,
            MobEffects.INVISIBILITY,
            MobEffects.NIGHT_VISION,
            MobEffects.RESISTANCE,
            MobEffects.FIRE_RESISTANCE,
            MobEffects.WATER_BREATHING,
            MobEffects.INSTANT_HEALTH
    );

    // list of arrows to shoot
    private List<Integer> arrows;

    @Override
    public void onUpdate() {

        // update arrows only if the arrows are already shot
        if (arrows.isEmpty()) {

            // update list of arrows
            arrows = new CopyOnWriteArrayList<>();

            // search inventory
            for (int i = 9; i < (hotbar.getValue() ? 45 : 36); i++) {

                // item at slot
                ItemStack item = mc.player.inventoryContainer.getSlot(i).getStack();

                // check if tipped arrow
                if (item.getItem() instanceof ItemTippedArrow) {

                    // potion associated with the tipped arrow
                    PotionType potion = PotionUtils.getPotionFromItem(item);

                    // check effects
                    for (PotionEffect effect : potion.getEffects()) {

                        // check if the effect is already active
                        if (mc.player.isPotionActive(effect.getPotion())) {
                            continue;
                        }

                        // is positive potion effect
                        if (potionEffects.contains(effect.getPotion())) {

                            // add to list of arrows
                            arrows.add(i);
                        }
                    }
                }
            }
        }

        // found arrows
        if (!arrows.isEmpty()) {

            // slot of first arrow
            int swapSlot = arrows.stream().min(Integer::compareTo).orElse(-1);

            // swap slot exists
            if (swapSlot != -1) {

                // slot to switch to
                int bowSlot = getCosmos().getInventoryManager().searchSlot(Items.BOW, InventoryRegion.HOTBAR);

                // switch to bow
                if (bowSlot != -1) {
                    getCosmos().getInventoryManager().switchToSlot(bowSlot, Switch.NORMAL);
                }

                else {
                    getCosmos().getChatManager().sendClientMessage(ChatFormatting.RED + "No bow found!", -201);
                    return;
                }

                // check if holding bow
                if (InventoryUtil.isHolding(Items.BOW)) {

                    // hold down keybind for use item
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), true);

                    // make sure we've held it for at least a minimum of specified ticks
                    if (mc.player.getItemInUseMaxCount() > AutoBowReleaseModule.ticks.getValue()) {

                        // release bow packets
                        mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                        // mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(mc.player.getActiveHand()));
                        mc.player.stopActiveHand();

                        // remove arrow
                        arrows.remove(swapSlot);
                    }
                }
            }
        }

        // disable if no arrows
        else {
            disable(true);
        }
    }
}
