package cope.cosmos.client.events;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.eventhandler.Event;

public class StepEvent extends Event {

    AxisAlignedBB bb;
    float height;


    public StepEvent(AxisAlignedBB box, float stepHeight) {
        bb = box;
        height = stepHeight;
    }

    public AxisAlignedBB getBB() {
        return bb;
    }

    public void setHeight(float height) {
        this.height = height;
    }


    public float getHeight() {
        return height;
    }

}
