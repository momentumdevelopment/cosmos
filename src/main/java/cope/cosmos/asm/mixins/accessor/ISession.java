package cope.cosmos.asm.mixins.accessor;

import net.minecraft.util.Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Session.class)
public interface ISession {

	@Accessor("username")
	void setUsername(String username);
}
