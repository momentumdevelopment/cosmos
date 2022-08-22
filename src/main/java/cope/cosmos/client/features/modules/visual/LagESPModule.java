package cope.cosmos.client.features.modules.visual;

import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.math.Timer;
import cope.cosmos.util.string.ColorUtil;
import net.minecraft.init.Items;
import net.minecraft.item.ItemChorusFruit;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


import java.util.LinkedList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glPopMatrix;

/**
 * @author oragejuice
 * @since 22/08/22
 */

public class LagESPModule extends Module {

    public static LagESPModule INSTANCE;

    public LagESPModule() {
        super("LagESP", Category.VISUAL, "Shows a path from where you got rubber banded");
        INSTANCE = this;
    }

    public static Setting<Float> fadeSpeed = new Setting<>("FadeSpeed", 1F, 2F, 10F, 1)
            .setDescription("How long the rubber band should show for");

    public static Setting<Float> width = new Setting<>("Width", 0.1F, 2F, 5F, 1)
            .setDescription("The width of the lines");

    public static Setting<Boolean> reverse = new Setting<>("Inverse", false)
            .setDescription("Direction of fade");

    public static Setting<Fade> fadeSetting = new Setting<Fade>("Fade", Fade.LINEAR)
            .setDescription("Which mode of fade");

    private final LinkedList<RubberBand> list = new LinkedList<>();
    private Timer lastChorus = new Timer();


    @SubscribeEvent
    public void onPacket(PacketEvent event){
        /*  when we get rubber-banded */
        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            SPacketPlayerPosLook p = (SPacketPlayerPosLook) event.getPacket();

            // if we have eaten a chorus recently, then the teleport is likely that of the chorus
            if(!lastChorus.passedTime(400, Timer.Format.MILLISECONDS)) return;

            //if the teleport (x and z values only) is telporting you more than 8 blocks
            // then it is likely not a rubberband
            if(mc.player.getPositionVector().distanceTo(new Vec3d(p.getX(), mc.player.posY, p.getZ())) > 8) return;

                /* register that a rubberband happened */
                list.add(new RubberBand(
                    mc.player.getPositionVector(),
                    new Vec3d(p.getX(), p.getY(), p.getZ()
                    )));

        }

        if (event.getPacket() instanceof CPacketPlayerTryUseItem) {
            if(mc.player.getHeldItemMainhand().getItem() instanceof ItemChorusFruit) {
                this.lastChorus.resetTime();
            }
        }

    }

    /* stolen from breadcrumbs.. */
    @Override
    public void onRender3D() {


        // Render positions
        list.forEach(r -> {

            glPushMatrix();
            glDisable(GL_TEXTURE_2D);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glEnable(GL_LINE_SMOOTH);
            glEnable(GL_BLEND);
            glDisable(GL_DEPTH_TEST);
            glLineWidth(width.getValue());

            // disable render lighting
            mc.entityRenderer.disableLightmap();

            glBegin(GL_LINE_STRIP);



            // Set line colour
            //starting position should always be clear
            glColor4f(ColorUtil.getPrimaryColor().getRed() / 255F,
                    ColorUtil.getPrimaryColor().getGreen() / 255F,
                    ColorUtil.getPrimaryColor().getBlue() / 255F,
                    reverse.getValue() ? 1F : 0.05F
            );

            // draw line from starting inital position to intermediary point
            glVertex3d(
                    r.from.x - mc.getRenderManager().viewerPosX,
                    r.from.y - mc.getRenderManager().viewerPosY,
                    r.from.z - mc.getRenderManager().viewerPosZ
            );

            //set the intermadiary point colour
            glColor4f(ColorUtil.getPrimaryColor().getRed() / 255F,
                    ColorUtil.getPrimaryColor().getGreen() / 255F,
                    ColorUtil.getPrimaryColor().getBlue() / 255F,
                    reverse.getValue() ? 1F : 0.05F

            );

            // calculate intermediary point
            r.calculateIntermediary();


            //render to the intermadiary point
            // draw line from starting inital position to intermediary point
            glVertex3d(
                    r.intermediary.x - mc.getRenderManager().viewerPosX,
                    r.intermediary.y - mc.getRenderManager().viewerPosY,
                    r.intermediary.z - mc.getRenderManager().viewerPosZ
            );

            // render to the final position
            glColor4f(ColorUtil.getPrimaryColor().getRed() / 255F,
                    ColorUtil.getPrimaryColor().getGreen() / 255F,
                    ColorUtil.getPrimaryColor().getBlue() / 255F,
                    reverse.getValue() ? 0.05F : 1F

            );
            glVertex3d(
                    r.to.x - mc.getRenderManager().viewerPosX,
                    r.to.y - mc.getRenderManager().viewerPosY,
                    r.to.z - mc.getRenderManager().viewerPosZ
            );

            if (System.currentTimeMillis() - r.time >= fadeSpeed.getValue() * 1000){
                list.remove(r);
            }

            // Reset colour
            glColor4d(1, 1, 1, 1);

            glEnd();
            glEnable(GL_DEPTH_TEST);
            glDisable(GL_LINE_SMOOTH);
            glDisable(GL_BLEND);
            glEnable(GL_TEXTURE_2D);
            glPopMatrix();

        });


    }


    private class RubberBand {
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
            if (reverse.getValue()){

                //calculate the difference
                Vec3d d = from.subtract(to);

                //set the position to be an interpolated value from `from` to `to`
                if (fadeSetting.getValue() == Fade.LINEAR) {
                    intermediary = to.addVector(
                            d.x * ((timeDelta) / (fadeSpeed.getValue() * 1000)),
                            d.y * ((timeDelta) / (fadeSpeed.getValue() * 1000)),
                            d.z * ((timeDelta) / (fadeSpeed.getValue() * 1000))
                    );
                }
                if (fadeSetting.getValue() == Fade.DYNAMIC) {
                    double distance = from.distanceTo(to);
                    intermediary = to.addVector(
                            d.x * ((timeDelta / 1000F) * (fadeSpeed.getValue() / 10F) / distance),
                            d.y * ((timeDelta / 1000F) * (fadeSpeed.getValue() / 10F) / distance),
                            d.z * ((timeDelta / 1000F) * (fadeSpeed.getValue() / 10F) / distance)
                    );
                }
            } else {
                //calculate the difference
                Vec3d d = to.subtract(from);

                //set the position to be an interpolated value from `from` to `to`
                if (fadeSetting.getValue().equals(Fade.LINEAR)) {
                    intermediary = from.addVector(
                            d.x * ((timeDelta) / (fadeSpeed.getValue() * 1000)),
                            d.y * ((timeDelta) / (fadeSpeed.getValue() * 1000)),
                            d.z * ((timeDelta) / (fadeSpeed.getValue() * 1000))
                    );
                }
                if (fadeSetting.getValue().equals(Fade.DYNAMIC)) {
                    double distance = from.distanceTo(to);
                    intermediary = from.addVector(
                            d.x * ((timeDelta / 1000F) * (fadeSpeed.getValue() / 10F) / distance),
                            d.y * ((timeDelta / 1000F) * (fadeSpeed.getValue() / 10F) / distance),
                            d.z * ((timeDelta / 1000F) * (fadeSpeed.getValue() / 10F) / distance)
                    );
                }
            }
        }

    }

    private enum Fade {
        LINEAR,
        DYNAMIC
    }


}

