package cope.cosmos.asm.mixins.accessor;

import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyBinding.class)
public interface IKeybinding {

    @Accessor("pressed")
    void setPressed(boolean pressed);
}
