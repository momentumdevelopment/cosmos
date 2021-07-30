package cope.cosmos.asm.mixins.accessor;

import net.minecraft.util.text.TextComponentString;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TextComponentString.class)
public interface ITextComponentString {

    @Accessor("text")
    void setText(String text);
}
