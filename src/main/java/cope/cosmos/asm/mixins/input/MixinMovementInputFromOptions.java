package cope.cosmos.asm.mixins.input;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.input.UpdateMoveStateEvent;
import net.minecraft.util.MovementInputFromOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MovementInputFromOptions.class)
public class MixinMovementInputFromOptions {

    @Inject(method = "updatePlayerMoveState", at = @At("RETURN"))
    public void onUpdatePlayerMoveState(CallbackInfo info) {
        UpdateMoveStateEvent updateMoveStateEvent = new UpdateMoveStateEvent();
        Cosmos.EVENT_BUS.post(updateMoveStateEvent);
    }
}
