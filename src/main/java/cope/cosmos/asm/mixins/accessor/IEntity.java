package cope.cosmos.asm.mixins.accessor;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface IEntity {

    @Accessor("inPortal")
    boolean getInPortal();

    @Accessor("isInWeb")
    boolean getInWeb();

    @Accessor("isInWeb")
    void setInWeb(boolean isInWeb);

    @Accessor("inPortal")
    void setInPortal(boolean inPortal);

    @Invoker("setSize")
    void setEntitySize(float width, float height);
}
