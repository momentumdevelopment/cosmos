package cope.cosmos.client.features.modules.world;

import cope.cosmos.asm.mixins.accessor.IPlayerControllerMP;
import cope.cosmos.client.events.block.BlockResetEvent;
import cope.cosmos.client.events.block.LeftClickBlockEvent;
import cope.cosmos.client.events.client.SettingUpdateEvent;
import cope.cosmos.client.events.entity.player.RotationUpdateEvent;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.combat.AuraModule;
import cope.cosmos.client.features.modules.combat.AutoCrystalModule;
import cope.cosmos.client.features.modules.visual.FreecamModule;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.InventoryManager.InventoryRegion;
import cope.cosmos.client.manager.managers.InventoryManager.Switch;
import cope.cosmos.util.holder.Rotation;
import cope.cosmos.util.holder.Rotation.Rotate;
import cope.cosmos.util.math.MathUtil;
import cope.cosmos.util.player.AngleUtil;
import cope.cosmos.util.render.RenderBuilder;
import cope.cosmos.util.render.RenderBuilder.Box;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.string.ColorUtil;
import cope.cosmos.util.string.StringFormatter;
import cope.cosmos.util.world.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.*;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

/**
 * @author linustouchtips
 * @since 12/02/2021
 */
public class SpeedMineModule extends Module {
    public static SpeedMineModule INSTANCE;

    public SpeedMineModule() {
        super("SpeedMine", new String[] {"PacketMine", "SpeedyGonzales", "AutoMine"}, Category.WORLD, "Mines faster", () -> mode.getValue().equals(Mode.PACKET) ? "" + MathHelper.clamp(MathUtil.roundFloat(mineDamage, 2), 0, 1) : "");
        INSTANCE = this;
    }

    // **************************** general ****************************

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.PACKET)
            .setDescription("Mode for SpeedMine");

    public static Setting<Switch> mineSwitch = new Setting<>("Switch", Switch.PACKET)
            .setAlias("AutoSwitch", "Swap", "AutoSwap", "MineSwitch")
            .setDescription( "Mode when switching to a pickaxe")
            .setVisible(() -> mode.getValue().equals(Mode.PACKET));

    public static Setting<Double> damage = new Setting<>("Damage", 0.0, 0.8, 1.0, 1)
            .setAlias("MineDamage")
            .setDescription("Instant block damage")
            .setVisible(() -> mode.getValue().equals(Mode.DAMAGE));

    // **************************** anticheat ****************************

    public static Setting<Rotate> rotate = new Setting<>("Rotation", Rotate.NONE)
            .setAlias("Rotate")
            .setDescription("How to rotate to the mine");

    public static Setting<Boolean> strict = new Setting<>("Strict", true)
            .setAlias("AlternateSwitch", "AlternateSwap")
            .setDescription("Mines on the correct direction")
            .setVisible(() -> mode.getValue().equals(Mode.PACKET));

    public static Setting<Boolean> strictReMine = new Setting<>("StrictBreak", true)
            .setAlias("Limit")
            .setDescription("Limits re-mines")
            .setVisible(() -> mode.getValue().equals(Mode.PACKET));

    public static Setting<Boolean> reset = new Setting<>("Stabilize", false)
            .setAlias("NoReset", "NoMineReset")
            .setDescription("Doesn't allow block break progress to be reset");

    // **************************** general ****************************

    public static Setting<Double> range = new Setting<>("Range", 0.0, 5.0, 6.0, 1)
            .setDescription("Range to mine blocks")
            .setVisible(() -> mode.getValue().equals(Mode.PACKET));

    // **************************** render ****************************

    public static Setting<Boolean> render = new Setting<>("Render", true)
            .setDescription("Renders a visual over current mining block");

    public static Setting<Box> renderMode = new Setting<>("RenderMode", Box.BOTH)
            .setDescription("Style for the visual")
            .setExclusion(Box.GLOW, Box.REVERSE, Box.NONE)
            .setVisible(() -> render.getValue());

    // mine info
    private BlockPos minePosition;
    private EnumFacing mineFacing;

    // mine damage
    private static float mineDamage;
    private int mineBreaks;

    // potion info
    private int previousHaste;

    @Override
    public void onUpdate() {

        // no reason to speedmine in creative mode, blocks break instantly
        if (!mc.player.capabilities.isCreativeMode) {
            if (minePosition != null) {

                // distance to mine position
                double mineDistance = BlockUtil.getDistanceToCenter(mc.player, minePosition);

                // limit re-mines
                if (mineBreaks >= 2 && strictReMine.getValue() || mineDistance > range.getValue()) {

                    // reset our block info
                    minePosition = null;
                    mineFacing = null;
                    mineDamage = 0;
                    mineBreaks = 0;
                }
            }

            /*
             * Idea behind this mode is that blocks often are considered broken on NCP before the damage is
             * greater than 1, so by breaking them at an earlier time we can break the block slightly faster
             *  (working on NCP)
             */
            if (mode.getValue().equals(Mode.DAMAGE)) {

                // if the damage is greater than our specified damage, set the block to full damage
                if (((IPlayerControllerMP) mc.playerController).getCurrentBlockDamage() > damage.getValue().floatValue()) {
                    ((IPlayerControllerMP) mc.playerController).setCurrentBlockDamage(1);

                    // destroy the block
                    mc.playerController.onPlayerDestroyBlock(minePosition);
                }
            }

            else if (mode.getValue().equals(Mode.PACKET)) {
                if (minePosition != null && !mc.world.isAirBlock(minePosition)) {

                    // if the block is broken
                    if (mineDamage >= 1) {

                        // make sure combat modules aren't active
                        if (!AutoCrystalModule.INSTANCE.isActive() && !AuraModule.INSTANCE.isActive()) {

                            // previous slot
                            int previousSlot = mc.player.inventory.currentItem;

                            // slot of item (based on slot ids from : https://c4k3.github.io/wiki.vg/images/1/13/Inventory-slots.png)
                            int swapSlot = getCosmos().getInventoryManager().searchSlot(getEfficientItem(mc.world.getBlockState(minePosition)).getItem(), InventoryRegion.HOTBAR) + 36;

                            // swap with window clicks
                            if (strict.getValue()) {

                                // transaction id
                                short nextTransactionID = mc.player.openContainer.getNextTransactionID(mc.player.inventory);

                                // window click
                                ItemStack itemstack = mc.player.openContainer.slotClick(swapSlot, mc.player.inventory.currentItem, ClickType.SWAP, mc.player);
                                mc.player.connection.sendPacket(new CPacketClickWindow(mc.player.inventoryContainer.windowId, swapSlot, mc.player.inventory.currentItem, ClickType.SWAP, itemstack, nextTransactionID));
                            }

                            else {

                                // switch to the most efficient item
                                getCosmos().getInventoryManager().switchToItem(getEfficientItem(mc.world.getBlockState(minePosition)).getItem(), mineSwitch.getValue());
                            }

                            // break the block
                            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, minePosition, mineFacing));
                            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, minePosition, EnumFacing.UP));

                            // FAST SPEED FAST SPEED
                            if (strict.getValue()) {
                                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, minePosition, mineFacing));
                            }

                            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, minePosition, mineFacing));

                            // save our current slot
                            if (previousSlot != -1) {

                                // swap with window clicks
                                if (strict.getValue()) {

                                    // slot of item (based on slot ids from : https://c4k3.github.io/wiki.vg/images/1/13/Inventory-slots.png)
                                    // int swapSlot = getCosmos().getInventoryManager().searchSlot(getEfficientItem(mc.world.getBlockState(minePosition)).getItem(), InventoryRegion.HOTBAR) + 36;

                                    // transaction id
                                    short nextTransactionID = mc.player.openContainer.getNextTransactionID(mc.player.inventory);

                                    // window click
                                    ItemStack itemstack = mc.player.openContainer.slotClick(swapSlot, mc.player.inventory.currentItem, ClickType.SWAP, mc.player);
                                    mc.player.connection.sendPacket(new CPacketClickWindow(mc.player.inventoryContainer.windowId, swapSlot, mc.player.inventory.currentItem, ClickType.SWAP, itemstack, nextTransactionID));

                                    // confirm packets
                                    mc.player.connection.sendPacket(new CPacketConfirmTransaction(mc.player.inventoryContainer.windowId, nextTransactionID, true));
                                }

                                else {

                                    // switch to our previous slot
                                    getCosmos().getInventoryManager().switchToSlot(previousSlot, Switch.PACKET);
                                }
                            }

                            // reset don't want this position to be re-mined without delay
                            mineDamage = 0;
                            mineBreaks++;
                        }
                    }

                    // update block damage
                    mineDamage += getBlockStrength(mc.world.getBlockState(minePosition), minePosition);
                }

                else {
                    mineDamage = 0; // not currently mining
                }
            }

            else if (mode.getValue().equals(Mode.VANILLA)) {

                // add haste and set the block hit delay to 0
                ((IPlayerControllerMP) mc.playerController).setBlockHitDelay(0);
                mc.player.addPotionEffect(new PotionEffect(MobEffects.HASTE.setPotionName("SpeedMine"), 80950, 1, false, false));
            }
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();

        // save old haste
        if (mc.player.isPotionActive(MobEffects.HASTE)) {
            previousHaste = mc.player.getActivePotionEffect(MobEffects.HASTE).getDuration();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // remove haste effect
        if (mc.player.isPotionActive(MobEffects.HASTE)) {
            mc.player.removePotionEffect(MobEffects.HASTE);
        }

        if (previousHaste > 0) {

            // reapply old haste
            mc.player.addPotionEffect(new PotionEffect(MobEffects.HASTE, previousHaste));
        }

        // reset our block info
        minePosition = null;
        mineFacing = null;
        mineDamage = 0;
        mineBreaks = 0;
    }

    @Override
    public void onRender3D() {

        // render the current mining block
        if (mode.getValue().equals(Mode.PACKET) && !mc.player.capabilities.isCreativeMode) {
            if (minePosition != null && !mc.world.isAirBlock(minePosition)) {

                // box of the mine
                AxisAlignedBB mineBox = mc.world.getBlockState(minePosition).getSelectedBoundingBox(mc.world, minePosition);

                // center of the box
                Vec3d mineCenter = mineBox.getCenter();

                // shrink
                AxisAlignedBB shrunkMineBox = new AxisAlignedBB(mineCenter.x, mineCenter.y, mineCenter.z, mineCenter.x, mineCenter.y, mineCenter.z);

                // draw box
                RenderUtil.drawBox(new RenderBuilder()
                        .position(shrunkMineBox.grow(((mineBox.minX - mineBox.maxX) * 0.5) * MathHelper.clamp(mineDamage, 0, 1), ((mineBox.minY - mineBox.maxY) * 0.5) * MathHelper.clamp(mineDamage, 0, 1), ((mineBox.minZ - mineBox.maxZ) * 0.5) * MathHelper.clamp(mineDamage, 0, 1)))
                        .color(mineDamage >= 0.95 ? ColorUtil.getPrimaryAlphaColor(120) : new Color(255, 0, 0, 120))
                        .box(renderMode.getValue())
                        .setup()
                        .line(1.5F)
                        .cull(renderMode.getValue().equals(Box.GLOW) || renderMode.getValue().equals(Box.REVERSE))
                        .shade(renderMode.getValue().equals(Box.GLOW) || renderMode.getValue().equals(Box.REVERSE))
                        .alpha(renderMode.getValue().equals(Box.GLOW) || renderMode.getValue().equals(Box.REVERSE))
                        .depth(true)
                        .blend()
                        .texture()
                );
            }
        }
    }

    @Override
    public boolean isActive() {
        return isEnabled() && minePosition != null && !mc.world.isAirBlock(minePosition) && mineDamage > 0;
    }

    @SubscribeEvent
    public void onLeftClickBlock(LeftClickBlockEvent event) {

        // make sure the block is breakable
        if (BlockUtil.isBreakable(event.getPos()) && !mc.player.capabilities.isCreativeMode) {
            if (mode.getValue().equals(Mode.CREATIVE)) {

                // instantly break the block and set the block to air
                mc.playerController.onPlayerDestroyBlock(event.getPos());
                mc.world.setBlockToAir(event.getPos());
            }

            if (mode.getValue().equals(Mode.PACKET)) {

                // left click block info
                if (!event.getPos().equals(minePosition)) {

                    // new mine info
                    minePosition = event.getPos();
                    mineFacing = event.getFace();
                    mineDamage = 0;
                    mineBreaks = 0;

                    if (minePosition != null && mineFacing != null) {

                        // send the packets to mine the position
                        mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, minePosition, mineFacing));
                        mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, minePosition, EnumFacing.UP));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onRotationUpdate(RotationUpdateEvent event) {

        if (isActive() && nullCheck()) {

            // server-side update our rotations
            if (!rotate.getValue().equals(Rotate.NONE)) {

                // incompatibilities
                if (FreecamModule.INSTANCE.isInteracting()) {
                    return;
                }

                // mine is complete
                if (mineDamage > 0.95) {

                    // cancel vanilla rotations, we'll send our own
                    event.setCanceled(true);

                    // check if mine position exists
                    if (minePosition != null) {

                        // angles to block
                        Rotation mineRotation = AngleUtil.calculateAngles(minePosition.add(0.5, 0.5, 0.5));

                        // update rots
                        if (rotate.getValue().equals(Rotate.CLIENT)) {
                            mc.player.rotationYaw = mineRotation.getYaw();
                            mc.player.rotationYawHead = mineRotation.getYaw();
                            mc.player.rotationPitch = mineRotation.getPitch();
                        }

                        // send rotations
                        getCosmos().getRotationManager().setRotation(mineRotation);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onBlockReset(BlockResetEvent event) {

        // don't allow block break progress to be reset
        if (reset.getValue()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onSettingChange(SettingUpdateEvent event) {

        // clear haste effect on mode change
        if (event.getSetting().equals(mode) && !event.getSetting().getValue().equals(Mode.VANILLA)) {
            if (mc.player.isPotionActive(MobEffects.HASTE)) {
                mc.player.removePotionEffect(MobEffects.HASTE);
            }

            if (previousHaste > 0) {

                // reapply old haste
                mc.player.addPotionEffect(new PotionEffect(MobEffects.HASTE, previousHaste));
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {

        // packet for switching held item
        if (event.getPacket() instanceof CPacketHeldItemChange) {

            // reset our mine time
            if (strict.getValue()) {
                mineDamage = 0;
            }
        }
    }

    // vanilla break speed methods

    /**
     * Searches the most efficient item for a specified position
     * @param state The {@link IBlockState} position to find the most efficient item for
     * @return The most efficient item for the specified position
     */
    public ItemStack getEfficientItem(IBlockState state) {

        // the efficient slot
        int bestSlot = -1;

        // find the most efficient item
        double bestBreakSpeed = 0;

        // iterate through item in the hotbar
        for (int i = 0; i < 9; i++) {
            if (!mc.player.inventory.getStackInSlot(i).isEmpty()) {
                float breakSpeed = mc.player.inventory.getStackInSlot(i).getDestroySpeed(state);

                // make sure the block is breakable
                if (breakSpeed > 1) {

                    // scale by efficiency enchantment
                    if (EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, mc.player.inventory.getStackInSlot(i)) > 0) {
                        breakSpeed += StrictMath.pow(EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, mc.player.inventory.getStackInSlot(i)), 2) + 1;
                    }

                    // if it's greater than our best break speed, mc.player our new most efficient item
                    if (breakSpeed > bestBreakSpeed) {
                        bestBreakSpeed = breakSpeed;
                        bestSlot = i;
                    }
                }
            }
        }

        // return the most efficient item
        if (bestSlot != -1) {
            return mc.player.inventory.getStackInSlot(bestSlot);
        }

        return mc.player.inventory.getStackInSlot(mc.player.inventory.currentItem);
    }

    /**
     * Finds the block strength of a specified block
     * @param state The {@link IBlockState} block state of the specified block
     * @param position The {@link BlockPos} position of the specified block
     * @return The block strength of the specified block
     */
    public float getBlockStrength(IBlockState state, BlockPos position) {

        // the block's hardness
        float hardness = state.getBlockHardness(mc.world, position);

        // if the block is air, it has no strength
        if (hardness < 0) {
            return 0;
        }

        // verify if the player can harvest the block
        if (!canHarvestBlock(state.getBlock(), position)) {
            return getDigSpeed(state) / hardness / 100F;
        }

        // find the dig speed if the player can't harvest the block
        else {
            return getDigSpeed(state) / hardness / 30F;
        }
    }

    /**
     * Check whether or not a specified block can be harvested
     * @param block The {@link Block} block to check
     * @param position The {@link BlockPos} position of the block to check
     * @return Whether or not the block can be harvested
     */
    @SuppressWarnings("deprecation")
    public boolean canHarvestBlock(Block block, BlockPos position) {

        // get the state of the block
        IBlockState worldState = mc.world.getBlockState(position);
        IBlockState state = worldState.getBlock().getActualState(worldState, mc.world, position);

        // if a tool is not required to harvest the block then we don't need to find the item
        if (state.getMaterial().isToolNotRequired()) {
            return true;
        }

        // find the item and get it's harvest tool
        ItemStack stack = getEfficientItem(state);
        String tool = block.getHarvestTool(state);

        // if the tool exists, then verify if the player can harvest the block
        if (stack.isEmpty() || tool == null) {
            return mc.player.canHarvestBlock(state);
        }

        // find the tool's harvest level
        int toolLevel = stack.getItem().getHarvestLevel(stack, tool, mc.player, state);
        if (toolLevel < 0) {
            return mc.player.canHarvestBlock(state);
        }

        // verify if the tool's harvest level is greater than the block's harvest level
        return toolLevel >= block.getHarvestLevel(state);
    }

    /**
     * Finds the dig speed of a specified block
     * @param state {@link IBlockState} The block state of the specified block
     * @return The dig speed of the specified block
     */
    @SuppressWarnings("all")
    public float getDigSpeed(IBlockState state) {

        // base dig speed
        float digSpeed = getDestroySpeed(state);

        if (digSpeed > 1) {
            ItemStack itemstack = getEfficientItem(state);
            
            // efficiency level
            int efficiencyModifier = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, itemstack);

            // scale by efficiency level
            if (efficiencyModifier > 0 && !itemstack.isEmpty()) {
                digSpeed += StrictMath.pow(efficiencyModifier, 2) + 1;
            }
        }

        // scaled based on haste effect level
        if (mc.player.isPotionActive(MobEffects.HASTE)) {
            digSpeed *= 1 + (mc.player.getActivePotionEffect(MobEffects.HASTE).getAmplifier() + 1) * 0.2F;
        }

        if (mc.player.isPotionActive(MobEffects.MINING_FATIGUE)) {

            // scale based on fatigue effect level
            float fatigueScale;
            switch (mc.player.getActivePotionEffect(MobEffects.MINING_FATIGUE).getAmplifier()) {
                case 0:
                    fatigueScale = 0.3F;
                    break;
                case 1:
                    fatigueScale = 0.09F;
                    break;
                case 2:
                    fatigueScale = 0.0027F;
                    break;
                case 3:
                default:
                    fatigueScale = 8.1E-4F;
            }

            digSpeed *= fatigueScale;
        }

        // reduce dig speed if the player is in water
        if (mc.player.isInsideOfMaterial(Material.WATER) && !EnchantmentHelper.getAquaAffinityModifier(mc.player)) {
            digSpeed /= 5;
        }

        // reduce dig speed if the player is not on the ground
        if (!mc.player.onGround) {
            digSpeed /= 5;
        }

        return (digSpeed < 0 ? 0 : digSpeed);
    }

    /**
     * Finds the destroy speed of a specified position
     * @param state {@link IBlockState} The position to get the destroy speed for
     * @return The destroy speed of the specified position
     */
    public float getDestroySpeed(IBlockState state) {

        // base destroy speed
        float destroySpeed = 1;

        // scale by the item's destroy speed
        if (getEfficientItem(state) != null && !getEfficientItem(state).isEmpty()) {
            destroySpeed *= getEfficientItem(state).getDestroySpeed(state);
        }

        return destroySpeed;
    }

    /**
     * Gets the current mine position
     * @return The current mine position
     */
    public BlockPos getMinePosition() {
        return minePosition;
    }

    public enum Mode {

        /**
         * Mines the block with packets, so the block breaking animation shouldn't be visible
         */
        PACKET,

        /**
         * Sets the block damage when block breaking animation is nearly complete
         */
        DAMAGE,

        /**
         * Adds the {@link net.minecraft.init.MobEffects} Haste potion effect
         */
        VANILLA,

        /**
         * Sets the block to air
         */
        CREATIVE
    }
}