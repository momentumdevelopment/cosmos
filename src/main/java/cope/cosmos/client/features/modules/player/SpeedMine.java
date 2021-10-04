package cope.cosmos.client.features.modules.player;

import cope.cosmos.asm.mixins.accessor.IPlayerControllerMP;
import cope.cosmos.client.events.BlockResetEvent;
import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.InventoryUtil;
import cope.cosmos.util.player.InventoryUtil.Switch;
import cope.cosmos.util.render.RenderBuilder;
import cope.cosmos.util.render.RenderBuilder.Box;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.system.Timer;
import cope.cosmos.util.system.Timer.Format;
import cope.cosmos.util.world.BlockUtil;
import cope.cosmos.util.world.BlockUtil.BlockResistance;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.Color;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unused")
public class SpeedMine extends Module {
    public static SpeedMine INSTANCE;

    public SpeedMine() {
        super("SpeedMine", Category.PLAYER, "Mines faster", () -> Setting.formatEnum(mode.getValue()));
        INSTANCE = this;
    }

    public static Setting<Mine> mode = new Setting<>("Mode", "Mode for SpeedMine", Mine.PACKET);
    public static Setting<Double> damage = new Setting<>(() -> mode.getValue().equals(Mine.DAMAGE), "Damage", "Instant block damage", 0.0, 1.0, 1.0, 1);
    public static Setting<Switch> mineSwitch = new Setting<>("Switch", "Mode when switching to a pickaxe", Switch.NONE);
    public static Setting<Boolean> animation = new Setting<>("Animation", "Cancels swinging packets", false);
    public static Setting<Boolean> reset = new Setting<>("Reset", "Doesn't allow block break progress to be reset", false);
    public static Setting<Boolean> doubleBreak = new Setting<>("DoubleBreak", "Breaks blocks above the one you are mining", false);
    public static Setting<Boolean> onlyPick = new Setting<>("OnlyPickaxe", "Only applies speed mine when using a pickaxe", false);
    public static Setting<Boolean> kickBack = new Setting<>("KickBack", "Syncs client break progress to server break progress", false);

    public static Setting<Boolean> render = new Setting<>("Render", "Renders a visual over current mining block", true);
    public static Setting<Box> renderMode = new Setting<>("Mode", "Style for the visual", Box.CLAW).setParent(render);
    public static Setting<Color> renderMine = new Setting<>("MineColor", "Color for the mining block", new Color(255, 0, 0, 50)).setParent(render);
    public static Setting<Color> renderAir = new Setting<>("AirColor", "Color for the predicted broken block", new Color(0, 255, 0, 45)).setParent(render);

    private final Timer mineTimer = new Timer();
    private final Timer switchTimer = new Timer();

    private BlockPos minePosition = BlockPos.ORIGIN;

    private int previousSlot = -1;

    @Override
    public void onUpdate() {
        for (Map.Entry<Potion, PotionEffect> potion : mc.player.getActivePotionMap().entrySet()) {
            if (potion.getKey().getName().equals("SpeedMine")) {
                mc.player.removePotionEffect(potion.getKey());
            }
        }

        if (Objects.equals(BlockUtil.getBlockResistance(minePosition), BlockResistance.BLANK))
            minePosition = BlockPos.ORIGIN;
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        try {
            if ((Objects.equals(BlockUtil.getBlockResistance(event.getPos()), BlockResistance.RESISTANT) || Objects.equals(BlockUtil.getBlockResistance(event.getPos()), BlockResistance.BREAKABLE))) {
                BlockPos mineBlock = event.getPos();
                EnumFacing mineFacing = event.getFace();

                if (onlyPick.getValue() && !InventoryUtil.isHolding(Items.DIAMOND_PICKAXE))
                    return;

                switch (mode.getValue()) {
                    case PACKET:
                        mineTimer.reset();
                        switchTimer.reset();
                        mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, mineBlock, mineFacing));
                        mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, mineBlock, mineFacing));

                        if (kickBack.getValue())
                            mc.playerController.onPlayerDestroyBlock(mineBlock);

                        break;
                    case DAMAGE:
                        ((IPlayerControllerMP) mc.playerController).setCurrentBlockDamage((float) ((double) damage.getValue()));

                        if (kickBack.getValue())
                            mc.playerController.onPlayerDestroyBlock(mineBlock);

                        break;
                    case VANILLA:
                        ((IPlayerControllerMP) mc.playerController).setBlockHitDelay(0);
                        mc.player.addPotionEffect(new PotionEffect(MobEffects.HASTE.setPotionName("SpeedMine"), 80950, 1, false, false));
                        break;
                    case FAKE:
                        if (kickBack.getValue())
                            mc.playerController.onPlayerDestroyBlock(mineBlock);

                        mc.world.setBlockToAir(mineBlock);
                        break;
                }

                minePosition = mineBlock;

                if (doubleBreak.getValue() && Objects.equals(BlockUtil.getBlockResistance(mineBlock.up()), BlockResistance.BREAKABLE) || Objects.equals(BlockUtil.getBlockResistance(mineBlock.up()), BlockResistance.RESISTANT)) {
                    switch (mode.getValue()) {
                        case PACKET:
                            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, mineBlock.up(), mineFacing));
                            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, mineBlock.up(), mineFacing));

                            if (kickBack.getValue())
                                mc.playerController.onPlayerDestroyBlock(mineBlock);

                            break;
                        case FAKE:
                            if (kickBack.getValue())
                                mc.playerController.onPlayerDestroyBlock(mineBlock);

                            mc.world.setBlockToAir(mineBlock.up());
                            break;
                        case DAMAGE:
                        case VANILLA:
                            break;
                    }
                }

                if (mode.getValue().equals(Mine.PACKET)) {
                    previousSlot = mc.player.inventory.currentItem;

                    /*
                    long breakDelay = (long) ((1 - getDigSpeed(mc.world.getBlockState(minePosition), minePosition)) * 2000);
                    if (mineTimer.passed(breakDelay + 50, Format.SYSTEM) && !switchTimer.passed(breakDelay + 200, Format.SYSTEM))
                        InventoryUtil.switchToSlot(getBestItem(mc.world.getBlockState(minePosition)).getItem(), mineSwitch.getValue());

                    if (switchTimer.passed(breakDelay + 400, Format.SYSTEM) || Objects.equals(BlockUtil.getBlockResistance(minePosition), BlockResistance.BLANK)) {
                        InventoryUtil.switchToSlot(previousSlot, Switch.NORMAL);
                        minePosition = BlockPos.ORIGIN;
                    }

                     */
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public ItemStack getBestItem(IBlockState blockState) {
        int bestSlot = -1;
        double max = 0;

        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getStackInSlot(i).isEmpty())
                continue;

            float speed = mc.player.inventory.getStackInSlot(i).getDestroySpeed(blockState);
            int eff;

            if (speed > 1) {
                speed += ((eff = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, mc.player.inventory.getStackInSlot(i))) > 0 ? (Math.pow(eff, 2) + 1) : 0);

                if (speed > max) {
                    max = speed;
                    bestSlot = i;
                }
            }
        }

        if (bestSlot != -1)
            return mc.player.inventory.getStackInSlot(bestSlot);

        return null;
    }

    public float getDigSpeed(IBlockState state, BlockPos blockPos) {
        float baseDestroySpeed = getDestroySpeed(state);

        if (baseDestroySpeed > 1) {
            int efficiencyModifier = EnchantmentHelper.getEfficiencyModifier(mc.player);

            if (efficiencyModifier > 0 && !getBestItem(state).isEmpty()) {
                baseDestroySpeed += (float) (Math.pow(efficiencyModifier, 2) + 1);
            }
        }

        if (mc.player.isPotionActive(MobEffects.HASTE)) {
            baseDestroySpeed *= 1 + (float) (Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.HASTE)).getAmplifier() + 1) * 0.2F;
        }

        if (mc.player.isPotionActive(MobEffects.MINING_FATIGUE)) {
            float fatigueScale;

            switch (Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.MINING_FATIGUE)).getAmplifier()) {
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

            baseDestroySpeed *= fatigueScale;
        }

        if (mc.player.isInsideOfMaterial(Material.WATER) && !EnchantmentHelper.getAquaAffinityModifier(mc.player)) {
            baseDestroySpeed /= 5;
        }

        if (!mc.player.onGround) {
            baseDestroySpeed /= 5;
        }

        baseDestroySpeed = ForgeEventFactory.getBreakSpeed(mc.player, state, baseDestroySpeed, blockPos);
        return (baseDestroySpeed < 0 ? 0 : baseDestroySpeed);
    }

    public float getDestroySpeed(IBlockState state) {
        float destroySpeed = 1;

        if (!getBestItem(state).isEmpty())
            destroySpeed *= getBestItem(state).getDestroySpeed(state);

        return destroySpeed;
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        event.setCanceled(nullCheck() && event.getPacket() instanceof CPacketAnimation && animation.getValue());
    }

    @SubscribeEvent
    public void onBlockReset(BlockResetEvent event) {
        event.setCanceled(nullCheck() && reset.getValue());
    }

    @Override
    public void onDisable() {
        super.onDisable();
        mc.player.removePotionEffect(MobEffects.HASTE);
        minePosition = BlockPos.ORIGIN;
    }

    @Override
    public void onRender3D() {
        if (minePosition != BlockPos.ORIGIN && render.getValue())
            RenderUtil.drawBox(new RenderBuilder().position(minePosition).color(mineTimer.passed((long) ((1 - getDigSpeed(mc.world.getBlockState(minePosition), minePosition)) * 2000), Format.SYSTEM) ? renderAir.getValue() : renderMine.getValue()).box(renderMode.getValue()).setup().line(1.5F).cull(renderMode.getValue().equals(Box.GLOW) || renderMode.getValue().equals(Box.REVERSE)).shade(renderMode.getValue().equals(Box.GLOW) || renderMode.getValue().equals(Box.REVERSE)).alpha(renderMode.getValue().equals(Box.GLOW) || renderMode.getValue().equals(Box.REVERSE)).depth(true).blend().texture());
    }

    public enum Mine {
        PACKET, DAMAGE, VANILLA, FAKE
    }
}
