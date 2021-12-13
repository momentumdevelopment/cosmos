package cope.cosmos.client.features.modules.player;

import cope.cosmos.asm.mixins.accessor.IPlayerControllerMP;
import cope.cosmos.client.events.BlockResetEvent;
import cope.cosmos.client.events.LeftClickBlockEvent;
import cope.cosmos.client.events.SettingUpdateEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.client.ColorUtil;
import cope.cosmos.util.client.StringFormatter;
import cope.cosmos.util.player.InventoryUtil;
import cope.cosmos.util.player.InventoryUtil.Inventory;
import cope.cosmos.util.player.InventoryUtil.Switch;
import cope.cosmos.util.render.RenderBuilder;
import cope.cosmos.util.render.RenderBuilder.Box;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.world.BlockUtil;
import cope.cosmos.util.world.BlockUtil.Resistance;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

/**
 * @author linustouchtips
 * @since 12/02/2021
 */
@SuppressWarnings({"unused", "deprecation"})
public class SpeedMine extends Module {
    public static SpeedMine INSTANCE;

    public SpeedMine() {
        super("SpeedMine", Category.PLAYER, "Mines faster", () -> StringFormatter.formatEnum(mode.getValue()));
        INSTANCE = this;
    }

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.PACKET).setDescription("Mode for SpeedMine");
    public static Setting<Switch> mineSwitch = new Setting<>("Switch", Switch.NORMAL).setDescription( "Mode when switching to a pickaxe").setVisible(() -> mode.getValue().equals(Mode.PACKET));
    public static Setting<Double> damage = new Setting<>("Damage", 0.0, 0.8, 1.0, 1).setDescription("Instant block damage").setVisible(() -> mode.getValue().equals(Mode.DAMAGE));
    public static Setting<Boolean> strict = new Setting<>("Strict", false).setDescription("Mines on the opposite face").setVisible(() -> mode.getValue().equals(Mode.PACKET));
    public static Setting<Boolean> swing = new Setting<>("NoSwing", false).setDescription("Cancels swinging packets").setVisible(() -> mode.getValue().equals(Mode.PACKET));
    public static Setting<Boolean> reset = new Setting<>("Reset", false).setDescription("Doesn't allow block break progress to be reset");

    public static Setting<Boolean> render = new Setting<>("Render", true).setDescription("Renders a visual over current mining block");
    public static Setting<Box> renderMode = new Setting<>("Mode", Box.BOTH).setParent(render).setDescription("Style for the visual");

    // mine info
    private BlockPos minePosition;
    private EnumFacing mineFacing;

    // mine damage
    private float mineDamage;

    // switch info
    private int previousSlot = -1;

    // potion info
    private int previousHaste;

    @Override
    public void onUpdate() {
        // no reason to speedmine in creative mode, blocks break instantly
        if (!mc.player.capabilities.isCreativeMode) {

            if (mode.getValue().equals(Mode.DAMAGE)) {
                // if the damage is greater than our specified damage, set the block to full damage
                if (((IPlayerControllerMP) mc.playerController).getCurrentBlockDamage() > damage.getValue().floatValue()) {
                    ((IPlayerControllerMP) mc.playerController).setCurrentBlockDamage(1);

                    // destroy the block
                    mc.playerController.onPlayerDestroyBlock(minePosition);
                }
            }

            else if (mode.getValue().equals(Mode.PACKET)) {
                if (minePosition != null) {
                    // if the block is broken
                    if (mineDamage >= 1) {

                        // break block
                        mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, minePosition, mineFacing));

                        // save our current slot
                        if (previousSlot != -1) {
                            // switch to our previous slot
                            mc.player.connection.sendPacket(new CPacketHeldItemChange(previousSlot));

                            // reset previous slot
                            previousSlot = -1;
                        }
                    }

                    // update block damage
                    mineDamage += getBlockStrength(mc.world.getBlockState(minePosition), minePosition);
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
    }

    @Override
    public void onRender3D() {
        // render the current mining block
        if (mode.getValue().equals(Mode.PACKET) && !mc.player.capabilities.isCreativeMode) {
            if (minePosition != null && !mc.world.isAirBlock(minePosition)) {
                RenderUtil.drawBox(new RenderBuilder()
                        .position(minePosition)
                        .color(mineDamage >= 1 ? ColorUtil.getPrimaryAlphaColor(120) : new Color(255, 0, 0, 120))
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
                // save our current slot
                previousSlot = mc.player.inventory.currentItem;

                // re-click
                if (event.getPos().equals(minePosition)) {
                    if (mode.getValue().equals(Mode.PACKET)) {
                        // if our damage is enough to destroy the block then we switch to the best item
                        if (mineDamage >= 1) {
                            if (BlockUtil.getResistance(minePosition).equals(Resistance.RESISTANT)) {
                                // switch to the most efficient item
                                int switchSlot = InventoryUtil.getItemSlot(getEfficientItem(mc.world.getBlockState(minePosition)).getItem(), Inventory.HOTBAR);

                                mc.player.connection.sendPacket(new CPacketHeldItemChange(switchSlot));

                                // break the block
                                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, minePosition, mineFacing));

                                // save our current slot
                                if (previousSlot != -1) {
                                    // switch to our previous slot
                                    mc.player.connection.sendPacket(new CPacketHeldItemChange(previousSlot));

                                    // reset previous slot
                                    previousSlot = -1;
                                }
                            }
                        }
                    }
                }

                // left click block info
                else if (!event.getPos().equals(minePosition)) {
                    // new mine info
                    minePosition = event.getPos();
                    mineFacing = event.getFace();
                    mineDamage = 0;

                    if (minePosition != null && mineFacing != null) {

                        // send the packets to mine the position
                        mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, minePosition, mineFacing));

                        // mine on the opposite facing
                        if (strict.getValue()) {
                            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, minePosition, mineFacing.getOpposite()));
                        }

                        // cancel the swinging animation
                        if (swing.getValue()) {
                            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, minePosition, mineFacing.getOpposite()));
                        }

                        if (!BlockUtil.getResistance(minePosition).equals(Resistance.RESISTANT)) {
                            // switch to the most efficient item
                            int switchSlot = InventoryUtil.getItemSlot(getEfficientItem(mc.world.getBlockState(minePosition)).getItem(), Inventory.HOTBAR);

                            mc.player.connection.sendPacket(new CPacketHeldItemChange(switchSlot));
                        }
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
                        breakSpeed += Math.pow(EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, mc.player.inventory.getStackInSlot(i)), 2) + 1;
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

        // if the block is air, it has no strenght
        if (hardness < 0) {
            return 0;
        }

        // verify if the player can harvest the block
        if (!canHarvestBlock(state.getBlock(), position)) {
            return getDigSpeed(state, position) / hardness / 100F;
        }

        // find the dig speed if the player can't harvest the block
        else {
            return getDigSpeed(state, position) / hardness / 30F;
        }
    }

    /**
     * Check whether or not a specified block can be harvested
     * @param block The {@link Block} block to check
     * @param position The {@link BlockPos} position of the block to check
     * @return Whether or not the block can be harvested
     */
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
     * @param position {@link BlockPos} The position of the specified block
     * @return The dig speed of the specified block
     */
    public float getDigSpeed(IBlockState state, BlockPos position) {
        // base dig speed
        float digSpeed = getDestroySpeed(state);

        if (digSpeed > 1) {
            ItemStack itemstack = getEfficientItem(state);
            
            // efficiency level
            int efficiencyModifier = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, itemstack);

            // scale by efficiency level
            if (efficiencyModifier > 0 && !itemstack.isEmpty()) {
                digSpeed += Math.pow(efficiencyModifier, 2) + 1;
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