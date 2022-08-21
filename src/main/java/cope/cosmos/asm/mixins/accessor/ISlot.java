package cope.cosmos.asm.mixins.accessor;

import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Slot.class)
public interface ISlot {

    @Invoker("onSwapCraft")
    void hookOnSwapCraft(int count);
}
