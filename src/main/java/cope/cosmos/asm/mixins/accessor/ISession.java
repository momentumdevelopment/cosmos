package cope.cosmos.asm.mixins.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.util.Session;

@Mixin(Session.class)
public interface ISession {

	@Accessor("username")
	void setUsername(String username);
}
