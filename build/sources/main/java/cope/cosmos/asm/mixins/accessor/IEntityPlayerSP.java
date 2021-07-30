package cope.cosmos.asm.mixins.accessor;

import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityPlayerSP.class)
public interface IEntityPlayerSP {

    @Accessor("serverSprintState")
    boolean getServerSprintState();

    @Accessor("serverSneakState")
    boolean getServerSneakState();

    @Accessor("positionUpdateTicks")
    int getPositionUpdateTicks();

    @Accessor("lastReportedPosX")
    double getLastReportedPosX();

    @Accessor("lastReportedPosY")
    double getLastReportedPosY();

    @Accessor("lastReportedPosZ")
    double getLastReportedPosZ();

    @Accessor("lastReportedYaw")
    float getLastReportedYaw();

    @Accessor("lastReportedPitch")
    float getLastReportedPitch();

    @Accessor("prevOnGround")
    boolean getPreviousOnGround();

    @Accessor("serverSprintState")
    void setServerSprintState(boolean serverSprintState);

    @Accessor("serverSneakState")
    void setServerSneakState(boolean serverSneakState);

    @Accessor("positionUpdateTicks")
    void setPositionUpdateTicks(int positionUpdateTicks);

    @Accessor("lastReportedPosX")
    void setLastReportedPosX(double lastReportedPosX);

    @Accessor("lastReportedPosY")
    void setLastReportedPosY(double lastReportedPosY);

    @Accessor("lastReportedPosZ")
    void setLastReportedPosZ(double lastReportedPosZ);

    @Accessor("lastReportedYaw")
    void setLastReportedYaw(float lastReportedYaw);

    @Accessor("lastReportedPitch")
    void setLastReportedPitch(float lastReportedPitch);

    @Accessor("prevOnGround")
    void setPreviousOnGround(boolean previousOnGround);
}
