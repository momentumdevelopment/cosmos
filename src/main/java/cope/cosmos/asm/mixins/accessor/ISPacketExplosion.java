package cope.cosmos.asm.mixins.accessor;

import net.minecraft.network.play.server.SPacketExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SPacketExplosion.class)
public interface ISPacketExplosion {

	@Accessor("motionX")
	float getMotionX();
	
	@Accessor("motionY")
	float getMotionY();
	
	@Accessor("motionZ")
	float getMotionZ();
	
	@Accessor("motionX")
	void setMotionX(float x);
	
	@Accessor("motionY")
	void setMotionY(float y);
	
	@Accessor("motionZ")
	void setMotionZ(float z);
}
