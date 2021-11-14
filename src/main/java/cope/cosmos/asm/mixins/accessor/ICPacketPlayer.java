package cope.cosmos.asm.mixins.accessor;

import net.minecraft.network.play.client.CPacketPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CPacketPlayer.class)
public interface ICPacketPlayer {

    @Accessor("rotating")
    boolean isRotating();

    @Accessor("moving")
    boolean isMoving();

    @Accessor("x")
    void setX(double x);

    @Accessor("y")
    void setY(double y);

    @Accessor("z")
    void setZ(double z);

    @Accessor("yaw")
    void setYaw(float yaw);

    @Accessor("pitch")
    void setPitch(float pitch);

    @Accessor("onGround")
    void setOnGround(boolean onGround);
}
