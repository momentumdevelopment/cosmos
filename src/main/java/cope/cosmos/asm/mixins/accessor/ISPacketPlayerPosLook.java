package cope.cosmos.asm.mixins.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.network.play.server.SPacketPlayerPosLook;

@Mixin(SPacketPlayerPosLook.class)
public interface ISPacketPlayerPosLook {

	@Accessor("yaw")
	void setYaw(float yaw);
	
	@Accessor("yaw")
	float getYaw();
	
	@Accessor("pitch")
	void setPitch(float pitch);
	
	@Accessor("pitch")
	float getPitch();
}
