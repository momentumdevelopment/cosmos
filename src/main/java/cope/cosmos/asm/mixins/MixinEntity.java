package cope.cosmos.asm.mixins;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.EntityHitboxSizeEvent;
import cope.cosmos.client.events.StepEvent;
import cope.cosmos.util.Wrapper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@SuppressWarnings("unused")
@Mixin(Entity.class)
public class MixinEntity implements Wrapper {

    @Shadow
    public float stepHeight;

    @Inject(method = "move", at = @At(value = "HEAD"))
    public void move(MoverType type, double tx, double ty, double tz, CallbackInfo info) {
        if (mc.getCurrentServerData() != null) {
            double x = tx;
            double y = ty;
            double z = tz;

            if (info.isCancelled())
                return;

            AxisAlignedBB axisAlignedBB = mc.player.getEntityBoundingBox();

            if (!mc.player.noClip) {
                if (type.equals(MoverType.PISTON))
                    return;

                mc.world.profiler.startSection("move");

                double d2 = x;
                double d3 = y;
                double d4 = z;

                if ((type == MoverType.SELF || type == MoverType.PLAYER) && mc.player.onGround && mc.player.isSneaking()) {
                    for (double d5 = 0.05; x != 0 && mc.world.getCollisionBoxes(mc.player, axisAlignedBB.offset(x, -mc.player.stepHeight, 0)).isEmpty(); d2 = x) {
                        if (x < 0.05 && x >= -0.05) {
                            x = 0;
                        } 
                        
                        else if (x > 0) {
                            x -= 0.05;
                        } 
                        
                        else {
                            x += 0.05;
                        }
                    }

                    for (; z != 0 && mc.world.getCollisionBoxes(mc.player, axisAlignedBB.offset(0, -mc.player.stepHeight, z)).isEmpty(); d4 = z) {
                        if (z < 0.05 && z >= -0.05) {
                            z = 0;
                        } 
                        
                        else if (z > 0) {
                            z -= 0.05;
                        } 
                        
                        else {
                            z += 0.05;
                        }
                    }

                    for (; x != 0 && z != 0 && mc.world.getCollisionBoxes(mc.player, axisAlignedBB.offset(x, -mc.player.stepHeight, z)).isEmpty(); d4 = z) {
                        if (x < 0.05 && x >= -0.05) {
                            x = 0;
                        } 
                        
                        else if (x > 0) {
                            x -= 0.05;
                        } 
                        
                        else {
                            x += 0.05;
                        }

                        d2 = x;

                        if (z < 0.05 && z >= -0.05) {
                            z = 0;
                        }

                        else if (z > 0) {
                            z -= 0.05;
                        }

                        else {
                            z += 0.05;
                        }
                    }
                }

                List<AxisAlignedBB> list1 = mc.world.getCollisionBoxes(mc.player, axisAlignedBB.expand(x, y, z));

                if (y != 0) {
                    int k = 0;

                    for (int l = list1.size(); k < l; ++k) {
                        y = list1.get(k).calculateYOffset(axisAlignedBB, y);
                    }

                    axisAlignedBB = (axisAlignedBB.offset(0, y, 0));
                }

                if (x != 0) {
                    int j5 = 0;

                    for (int l5 = list1.size(); j5 < l5; ++j5) {
                        x = list1.get(j5).calculateXOffset(axisAlignedBB, x);
                    }

                    if (x != 0) {
                        axisAlignedBB = (axisAlignedBB.offset(x, 0, 0));
                    }
                }

                if (z != 0) {
                    int k5 = 0;

                    for (int i6 = list1.size(); k5 < i6; ++k5) {
                        z = list1.get(k5).calculateZOffset(axisAlignedBB, z);
                    }

                    if (z != 0) {
                        axisAlignedBB = (axisAlignedBB.offset(0, 0, z));
                    }
                }

                boolean flag = mc.player.onGround || d3 != y && d3 < 0;

                if (mc.player.stepHeight > 0.0F && flag && (d2 != x || d4 != z)) {
                    double d14 = x;
                    double d6 = y;
                    double d7 = z;

                    y = mc.player.stepHeight;
                    List<AxisAlignedBB> list = mc.world.getCollisionBoxes(mc.player, axisAlignedBB.expand(d2, y, d4));
                    AxisAlignedBB axisalignedbb2 = axisAlignedBB;
                    AxisAlignedBB axisalignedbb3 = axisalignedbb2.expand(d2, 0, d4);

                    double d8 = y;
                    int j1 = 0;

                    for (int k1 = list.size(); j1 < k1; ++j1) {
                        d8 = list.get(j1).calculateYOffset(axisalignedbb3, d8);
                    }

                    axisalignedbb2 = axisalignedbb2.offset(0, d8, 0);
                    double d18 = d2;
                    int l1 = 0;

                    for (int i2 = list.size(); l1 < i2; ++l1) {
                        d18 = list.get(l1).calculateXOffset(axisalignedbb2, d18);
                    }

                    axisalignedbb2 = axisalignedbb2.offset(d18, 0, 0);
                    double d19 = d4;
                    int j2 = 0;

                    for (int k2 = list.size(); j2 < k2; ++j2) {
                        d19 = list.get(j2).calculateZOffset(axisalignedbb2, d19);
                    }

                    axisalignedbb2 = axisalignedbb2.offset(0, 0, d19);
                    AxisAlignedBB axisalignedbb4 = axisAlignedBB;
                    double d20 = y;
                    int l2 = 0;

                    for (int i3 = list.size(); l2 < i3; ++l2) {
                        d20 = list.get(l2).calculateYOffset(axisalignedbb4, d20);
                    }

                    axisalignedbb4 = axisalignedbb4.offset(0, d20, 0);
                    double d21 = d2;
                    int j3 = 0;

                    for (int k3 = list.size(); j3 < k3; ++j3) {
                        d21 = list.get(j3).calculateXOffset(axisalignedbb4, d21);
                    }

                    axisalignedbb4 = axisalignedbb4.offset(d21, 0, 0);
                    double d22 = d4;
                    int l3 = 0;

                    for (int i4 = list.size(); l3 < i4; ++l3) {
                        d22 = list.get(l3).calculateZOffset(axisalignedbb4, d22);
                    }

                    axisalignedbb4 = axisalignedbb4.offset(0, 0, d22);
                    double d23 = d18 * d18 + d19 * d19;
                    double d9 = d21 * d21 + d22 * d22;

                    if (d23 > d9) {
                        x = d18;
                        z = d19;
                        y = -d8;
                        axisAlignedBB = (axisalignedbb2);
                    } 
                    
                    else {
                        x = d21;
                        z = d22;
                        y = -d20;
                        axisAlignedBB = (axisalignedbb4);
                    }

                    int j4 = 0;

                    for (int k4 = list.size(); j4 < k4; ++j4) {
                        y = list.get(j4).calculateYOffset(axisAlignedBB, y);
                    }

                    axisAlignedBB = (axisAlignedBB.offset(0, y, 0));

                    if (!(d14 * d14 + d7 * d7 >= x * x + z * z)) {
                        StepEvent event = new StepEvent(axisAlignedBB, stepHeight);
                        Cosmos.EVENT_BUS.post(event);

                        mc.player.stepHeight = event.getHeight();
                    }
                }
            }
        }
    }

    @Inject(method = "getCollisionBorderSize", at = @At("HEAD"), cancellable = true)
    public void getCollisionBorderSize(CallbackInfoReturnable<Float> info) {
        EntityHitboxSizeEvent entityHitboxSizeEvent = new EntityHitboxSizeEvent();
        Cosmos.EVENT_BUS.post(entityHitboxSizeEvent);

        if (entityHitboxSizeEvent.isCanceled()) {
            info.cancel();
            info.setReturnValue(entityHitboxSizeEvent.getHitboxSize());
        }
    }
}
