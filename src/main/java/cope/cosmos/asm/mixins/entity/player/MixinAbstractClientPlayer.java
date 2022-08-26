package cope.cosmos.asm.mixins.entity.player;

import com.mojang.authlib.GameProfile;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.render.other.CapeLocationEvent;
import cope.cosmos.client.events.render.player.ModifyFOVEvent;
import cope.cosmos.client.events.render.other.SkinLocationEvent;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(AbstractClientPlayer.class)
public abstract class MixinAbstractClientPlayer extends EntityPlayer {

    public MixinAbstractClientPlayer(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }

    @Inject(method = "getLocationSkin()Lnet/minecraft/util/ResourceLocation;", at = @At("HEAD"), cancellable = true)
    public void getLocationSkin(CallbackInfoReturnable<ResourceLocation> info) {
        SkinLocationEvent skinLocationEvent = new SkinLocationEvent();
        Cosmos.EVENT_BUS.post(skinLocationEvent);
    }

    @Inject(method = "getLocationCape", at = @At("HEAD"), cancellable = true)
    public void getLocationCape(CallbackInfoReturnable<ResourceLocation> info) {
        CapeLocationEvent event = new CapeLocationEvent(getName());
        Cosmos.EVENT_BUS.post(event);

        if (event.getLocation() != null) {
            info.setReturnValue(new ResourceLocation(Cosmos.MOD_ID, event.getLocation()));
        }
    }

    @Inject(method = "getFovModifier", at = @At("HEAD"), cancellable = true)
    public void getFOVModifier(CallbackInfoReturnable<Float> info) {
        ModifyFOVEvent modifyFOVEvent = new ModifyFOVEvent();
        Cosmos.EVENT_BUS.post(modifyFOVEvent);

        if (modifyFOVEvent.isCanceled()) {
            info.cancel();
            info.setReturnValue(1.0F);
        }
    }
}
