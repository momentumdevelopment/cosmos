package cope.cosmos.client.features.modules.player;

import cope.cosmos.asm.mixins.accessor.ICPacketPlayerTryUseItemOnBlock;
import cope.cosmos.client.events.block.LiquidInteractEvent;
import cope.cosmos.client.events.entity.hitbox.HitboxInteractEvent;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.events.client.SettingUpdateEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.combat.AutoCrystalModule;
import cope.cosmos.client.features.modules.combat.BurrowModule;
import cope.cosmos.client.features.modules.combat.HoleFillModule;
import cope.cosmos.client.features.modules.combat.SurroundModule;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.InventoryUtil;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;

/**
 * This module attempts to put all the bloat modules (i.e. Swing, LiquidInteract, NoHeightLimit) in one module
 * @author linustouchtips, Wolfsurge
 * @since 06/08/2021
 */
public class InteractModule extends Module {
    public static InteractModule INSTANCE;

    public InteractModule() {
        super("Interact", Category.PLAYER, "Changes how you interact with blocks & entities");
        INSTANCE = this;
    }

    // **************************** hand interactions ****************************

    public static Setting<Hand> hand = new Setting<>("Hand", Hand.MAINHAND)
            .setDescription("Swinging hand");

    public static Setting<Boolean> ghostHand = new Setting<>("GhostHand", false)
            .setDescription("Allows you to interact with blocks through walls");

    public static Setting<Boolean> noSwing = new Setting<>("NoSwing", false)
            .setDescription("Cancels the server side animation for swinging");

    // **************************** block interactions ****************************

    public static Setting<Boolean> ignoreContainer = new Setting<>("IgnoreContainers", false)
            .setDescription("Ignores containers");

    // **************************** illegal interactions ****************************

    public static Setting<Boolean> liquid = new Setting<>("Liquid", false)
            .setDescription("Allows you to place blocks on liquid");

    public static Setting<Boolean> heightLimit = new Setting<>("HeightLimit", true)
            .setDescription("Allows you to interact with blocks at height limit");

    public static Setting<Boolean> worldBorder = new Setting<>("WorldBorder", false)
            .setDescription("Allows you to interact with blocks at the world border");

    // **************************** No Entity Trace *********************************

    public static Setting<Boolean> noEntityTrace = new Setting<>("NoEntityTrace", false)
            .setDescription("Allows you to interact with blocks through an entity");

    public static Setting<Boolean> pickaxe = new Setting<>("NoTracePickaxe", true)
            .setDescription("Apply NoEntityTrace when holding a pickaxe in your main hand").setVisible(() -> noEntityTrace.getValue());

    public static Setting<Boolean> blocks = new Setting<>("NoTraceBlocks", false)
            .setDescription("Apply NoEntityTrace when holding a block").setVisible(() -> noEntityTrace.getValue());

    public static Setting<Boolean> all = new Setting<>("NoTraceAll", false)
            .setDescription("Always apply NoEntityTrace").setVisible(() -> noEntityTrace.getValue());

    public static Setting<Boolean> excludeSword = new Setting<>("NoTraceAllExcludeSword", true)
            .setDescription("Do not apply NoEntityTrace when holding a sword").setVisible(() -> noEntityTrace.getValue() && all.getValue());

    public static Setting<Boolean> holdingCrystals = new Setting<>("NoTraceHoldingCrystals", false)
            .setDescription("Apply NoEntityTrace when holding crystals").setVisible(() -> noEntityTrace.getValue());

    @SubscribeEvent
    public void onSettingUpdate(SettingUpdateEvent event) {
        if (event.getSetting().equals(hand)) {

            // update the player's swinging hand
            switch (hand.getValue()) {
                case OFFHAND:
                    mc.player.swingingHand = EnumHand.OFF_HAND;
                    break;
                case MAINHAND:
                    mc.player.swingingHand = EnumHand.MAIN_HAND;
                    break;
            }
        }
    }

    @SubscribeEvent
    public void onLiquidInteract(LiquidInteractEvent event) {

        // allow interactions with liquids
        if (liquid.getValue() || event.getLiquidLevel() && event.getBlockState().getValue(BlockLiquid.LEVEL) == 0) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {

        // packet for placing on blocks
        if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {

            // position where we need to limit the face
            BlockPos limitPosition = ((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getPos();

            // click on the the opposite face at height borders
            if (heightLimit.getValue() && limitPosition.getY() >= (mc.world.getHeight() - 1) && ((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getDirection().equals(EnumFacing.UP)) {
                ((ICPacketPlayerTryUseItemOnBlock) event.getPacket()).setDirection(EnumFacing.DOWN);
            }

            // click on the the opposite face at world borders
            if (worldBorder.getValue() && mc.world.getWorldBorder().contains(limitPosition)) {

                // opposite face
                EnumFacing oppositeFace = ((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getDirection().getOpposite();

                // update packet
                ((ICPacketPlayerTryUseItemOnBlock) event.getPacket()).setDirection(oppositeFace);
            }

            // cancel the right-click packet if we're interacting with a container
            if (ignoreContainer.getValue()) {
                if (getCosmos().getInteractionManager().getSneakBlocks().contains(mc.world.getBlockState(limitPosition).getBlock())) {
                    event.setCanceled(true);
                }
            }

            if (ghostHand.getValue()) {

                // check if we are auto-placing, we can't attempt to open containers during an auto-place process
                if (AutoCrystalModule.INSTANCE.isActive() || HoleFillModule.INSTANCE.isActive() || BurrowModule.INSTANCE.isEnabled() || SurroundModule.INSTANCE.isActive()) {
                    return;
                }

                // iterate through all containers in the world
                new ArrayList<>(mc.world.loadedTileEntityList).forEach(tileEntity -> {

                    // if we clicked the container then we don't need to cancel the click
                    if (limitPosition.equals(tileEntity.getPos())) {
                        return;
                    }

                    // cancel the click and click the container instead
                    if (limitPosition.getDistance(tileEntity.getPos().getX(), tileEntity.getPos().getY(), tileEntity.getPos().getZ()) <= 3) {
                        mc.playerController.processRightClickBlock(mc.player, mc.world, tileEntity.getPos(), EnumFacing.UP, new Vec3d(0, 0, 0), EnumHand.MAIN_HAND);
                    }
                });
            }
        }

        // cancel the swing animation packet
        if (event.getPacket() instanceof CPacketAnimation && noSwing.getValue()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onHitboxInteract(HitboxInteractEvent event) {
        if (noEntityTrace.getValue()) {
            // Only check if we are holding it in our main hand, we can't use it in our offhand
            if (pickaxe.getValue() && mc.player.getHeldItemMainhand().getItem() instanceof ItemPickaxe) {
                event.setCanceled(true);
            }

            // Check we are holding blocks
            if (blocks.getValue() && InventoryUtil.isHolding(ItemBlock.class)) {
                event.setCanceled(true);
            }

            // Cancel if we are holding crystals
            if (holdingCrystals.getValue() && InventoryUtil.isHolding(Items.END_CRYSTAL)) {
                event.setCanceled(true);
            }

            // Always cancel it
            if (all.getValue()) {
                // We don't want to cancel it if we are holding a sword
                if (excludeSword.getValue() && mc.player.getHeldItemMainhand().getItem() instanceof ItemSword) {
                    return;
                }

                event.setCanceled(true);
            }
        }
    }

    public enum Hand {

        /**
         * Swings the mainhand
         */
        MAINHAND,

        /**
         * Swings the offhand
         */
        OFFHAND
    }
}
