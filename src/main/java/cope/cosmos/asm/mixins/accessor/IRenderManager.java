package cope.cosmos.asm.mixins.accessor;

import net.minecraft.client.renderer.entity.RenderManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderManager.class)
public interface IRenderManager {

    @Accessor("renderPosX")
    double getRenderX();

    @Accessor("renderPosY")
    double getRenderY();

    @Accessor("renderPosZ")
    double getRenderZ();
}
