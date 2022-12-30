package cope.cosmos.asm.mixins.accessor;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.Session;
import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface IMinecraft {

	@Accessor("session")
	void setSession(Session session);

	@Invoker("rightClickMouse")
	void hookRightClickMouse();

	@Accessor("rightClickDelayTimer")
	void setRightClickDelayTimer(int rightClickDelayTimer);

	@Accessor("renderViewEntity")
	void hookSetRenderViewEntity(Entity renderViewEntity);

	@Accessor("timer")
	Timer getTimer();
}
