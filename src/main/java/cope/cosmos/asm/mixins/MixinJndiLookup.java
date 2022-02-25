package cope.cosmos.asm.mixins;

import org.apache.logging.log4j.core.LogEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "org.apache.logging.log4j.core.lookup.JndiLookup", remap = false)
public class MixinJndiLookup {

    @Inject(method = "lookup(Lorg/apache/logging/log4j/core/LogEvent;Ljava/lang/String;)Ljava/lang/String;", at = @At("HEAD"), cancellable = true)
    public void onLookup(LogEvent event, String key, CallbackInfoReturnable<String> info) {
        info.setReturnValue(key);
        info.cancel();
    }
}
