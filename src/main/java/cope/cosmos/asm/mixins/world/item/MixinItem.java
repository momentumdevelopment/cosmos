package cope.cosmos.asm.mixins.world.item;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.item.ItemUseFinishEvent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class MixinItem {

    @Inject(method = "onItemUseFinish", at = @At("HEAD"), cancellable = true)
    public void onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving, CallbackInfoReturnable<ItemStack> info) {
        ItemUseFinishEvent itemUseFinishEvent = new ItemUseFinishEvent(stack);
        Cosmos.EVENT_BUS.post(itemUseFinishEvent);

        if (itemUseFinishEvent.isCanceled()) {
            info.cancel();
            info.setReturnValue(stack);
        }
    }
}
