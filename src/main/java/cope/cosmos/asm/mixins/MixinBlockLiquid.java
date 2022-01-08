package cope.cosmos.asm.mixins;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.LiquidInteractEvent;
import cope.cosmos.client.features.modules.movement.Jesus;
import cope.cosmos.util.Wrapper;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
@Mixin(BlockLiquid.class)
public class MixinBlockLiquid implements Wrapper {

    @Inject(method = "canCollideCheck", at = @At("HEAD"), cancellable = true)
    public void canCollideCheck(IBlockState blockState, boolean liquidLevel, CallbackInfoReturnable<Boolean> info) {
        LiquidInteractEvent liquidInteractEvent = new LiquidInteractEvent(blockState, liquidLevel);
        Cosmos.EVENT_BUS.post(liquidInteractEvent);

        if (liquidInteractEvent.isCanceled()) {
            info.cancel();
            info.setReturnValue(true);
        }
    }

    /**
     * @author Wolfsurge
     */
    @Overwrite
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        try {
            return nullCheck() && Jesus.INSTANCE.isEnabled() && Jesus.mode.getValue() == Jesus.Mode.SOLID && !mc.player.isInWater() && !mc.gameSettings.keyBindSneak.isKeyDown() ?
                    BlockLiquid.FULL_BLOCK_AABB : BlockLiquid.NULL_AABB;
        } catch (NullPointerException ignored) {return BlockLiquid.NULL_AABB;} // produces an error for some reason, this was the easiest fix i found
    }

}
