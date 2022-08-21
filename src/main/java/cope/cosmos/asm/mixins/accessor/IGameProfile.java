package cope.cosmos.asm.mixins.accessor;

import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameProfile.class)
public interface IGameProfile {

    @Accessor("name")
    void setName(String name);
}
