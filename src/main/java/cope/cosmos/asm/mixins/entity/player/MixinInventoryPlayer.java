package cope.cosmos.asm.mixins.entity.player;

import cope.cosmos.client.Cosmos;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryPlayer.class)
public abstract class MixinInventoryPlayer {

    @Shadow @Final public NonNullList<ItemStack> mainInventory;

    @Shadow public int currentItem;

    @Shadow public EntityPlayer player;

    @Inject(method = "getCurrentItem", at = @At("HEAD"), cancellable = true)
    public void getCurrentItem(CallbackInfoReturnable<ItemStack> info) {
        Cosmos cosmos = Cosmos.INSTANCE;
        if (cosmos != null && cosmos.getInventoryManager() != null && player.equals(Minecraft.getMinecraft().player)) {
            int slot = cosmos.getInventoryManager().getServerSlot();
            if (slot == -1) {
                slot = currentItem;
            }

            info.setReturnValue(InventoryPlayer.isHotbar(slot) ? mainInventory.get(slot) : ItemStack.EMPTY);
        }
    }
}