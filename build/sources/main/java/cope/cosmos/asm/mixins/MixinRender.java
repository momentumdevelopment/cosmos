package cope.cosmos.asm.mixins;

import cope.cosmos.client.events.ShaderColorEvent;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Mixin(Render.class)
public class MixinRender<T extends Entity> {

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    public void getTeamColor(T entity, CallbackInfoReturnable<Integer> info) {
        ShaderColorEvent shaderColorEvent = new ShaderColorEvent(entity);
        MinecraftForge.EVENT_BUS.post(shaderColorEvent);

        if (shaderColorEvent.isCanceled()) {
            info.cancel();
            info.setReturnValue(shaderColorEvent.getColor().getRGB());
        }
    }
}
