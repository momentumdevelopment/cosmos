package cope.cosmos.asm.mixins.render;

import cope.cosmos.client.features.modules.visual.WallhackModule;
import net.minecraft.client.renderer.BufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.IntBuffer;

@Mixin(BufferBuilder.class)
public class MixinBufferBuilder {
    @Redirect(method = "putColorMultiplier", at = @At(value = "INVOKE", target = "java/nio/IntBuffer.put(II)Ljava/nio/IntBuffer;", remap = false))
    private IntBuffer putColorMultiplier(IntBuffer buffer, int i, int j) {
        return buffer.put(i, WallhackModule.INSTANCE.isEnabled() ? j & 0x00ffffff | WallhackModule.opacity.getValue().intValue() << 24 : j);
    }
}
