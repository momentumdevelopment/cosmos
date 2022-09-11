package cope.cosmos.util.player;

import cope.cosmos.client.features.modules.visual.ESPModule;
import net.minecraft.util.math.Vec3d;

public class RubberBand {

    public Vec3d getFrom() {
        return from;
    }

    public Vec3d getTo() {
        return to;
    }

    public Vec3d getIntermediary() {
        return intermediary;
    }


    public long getTime() {
        return time;
    }


    //where the player was initially
    private final Vec3d from;
    //where they got rubberbanded to
    private final Vec3d to;

    private final long time;

    private Vec3d intermediary;


    public RubberBand(Vec3d from, Vec3d to) {
        this.from = from;
        this.to = to;
        this.time = System.currentTimeMillis();
        intermediary = from;
    }

    //calculate, update then return the intermedarity position for rendering
    public void calculateIntermediary(){
        // the difference in time between the creation and now
        long timeDelta = System.currentTimeMillis() - time;
        timeDelta = timeDelta == 0 ? 1 : timeDelta;


            /*
            so normally i would use a higher order function for calcuting the interp,
            but im lazy and nobody will ever look at this. so i wont
             */
        if (ESPModule.reverse.getValue()){

            //calculate the difference
            Vec3d d = from.subtract(to);

            //set the position to be an interpolated value from `from` to `to`
            intermediary = to.addVector(
                    d.x * ((timeDelta) / (ESPModule.fadeSpeed.getValue() * 1000)),
                    d.y * ((timeDelta) / (ESPModule.fadeSpeed.getValue() * 1000)),
                    d.z * ((timeDelta) / (ESPModule.fadeSpeed.getValue() * 1000))
            );

        } else {
            //calculate the difference
            Vec3d d = to.subtract(from);

            //set the position to be an interpolated value from `from` to `to`
            intermediary = from.addVector(
                        d.x * ((timeDelta) / (ESPModule.fadeSpeed.getValue() * 1000)),
                        d.y * ((timeDelta) / (ESPModule.fadeSpeed.getValue() * 1000)),
                        d.z * ((timeDelta) / (ESPModule.fadeSpeed.getValue() * 1000))
            );

        }
    }

}