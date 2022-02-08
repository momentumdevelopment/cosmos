package cope.cosmos.asm.mixins.render;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.render.entity.ShaderColorEvent;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
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
        Cosmos.EVENT_BUS.post(shaderColorEvent);

        if (shaderColorEvent.isCanceled()) {
            info.cancel();
            info.setReturnValue(shaderColorEvent.getColor().getRGB());
        }
    }
}
