package cope.cosmos.client.events.motion.movement;

import net.minecraft.entity.MoverType;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class MotionEvent extends Event {

    private MoverType type;
    private double x;
    private double y;
    private double z;

    public MotionEvent(MoverType type, double x, double y, double z) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public MoverType getType() {
        return type;
    }

    public void setType(MoverType in) {
        type = in;
    }

    public double getX() {
        return x;
    }

    public void setX(double in) {
        x = in;
    }

    public double getY() {
        return y;
    }

    public void setY(double in) {
        y = in;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double in) {
        z = in;
    }
}
