package cope.cosmos.client.features.modules.client;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.util.math.MathUtil;
import cope.cosmos.util.string.StringFormatter;
import net.minecraft.util.EnumFacing;

import javax.swing.*;
import java.awt.Font;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author aesthetical
 * @since 1/26/2022
 */
public class StreamerModeModule extends Module {
    public static StreamerModeModule INSTANCE;

    public StreamerModeModule() {
        super("StreamerMode", Category.CLIENT, "Opens a separate window that shows your coordinates");
        INSTANCE = this;
    }

    // window
    private JFrame frame;
    private JLabel facing, coordinates, netherCoordinates;

    @Override
    public void onEnable() {
        super.onEnable();

        // god do i hate how this code looks
        frame = new JFrame("Cosmos Streamer Mode");

        frame.setSize(1000, 350);

        // set everything to go vertical (y axis)
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.getContentPane().setBackground(Color.BLACK);

        // add our text labels we'll be editing with the data
        // we also set the font for all of these labels too

        facing = new JLabel("Facing: ");
        facing.setFont(new Font("Verdana", Font.PLAIN, 26));

        coordinates = new JLabel("XYZ: ");
        coordinates.setFont(new Font("Verdana", Font.PLAIN, 26));

        netherCoordinates = new JLabel("XYZ [Nether]: ");
        netherCoordinates.setFont(new Font("Verdana", Font.PLAIN, 26));

        // add to frame
        frame.add(facing);
        frame.add(coordinates);
        frame.add(netherCoordinates);

        // add a close listener for when the X is pressed. it'll toggle off the module.
        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent event) {
                disable(true);
            }
        });

        // pack the elements, and set the frame to visible
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // cleanup
        if (frame != null) {
            frame.setVisible(false);
            frame.dispose();

            facing = null;
            coordinates = null;
            netherCoordinates = null;
            frame = null;
        }
    }

    @Override
    public void onUpdate() {

        // facing stuff
        EnumFacing direction = mc.player.getHorizontalFacing();
        EnumFacing.AxisDirection axisDirection = direction.getAxisDirection();

        facing.setText("Facing: " + direction + " (" + StringFormatter.formatEnum(direction.getAxis()) + (axisDirection.equals(EnumFacing.AxisDirection.POSITIVE) ? "+" : "-") + ")");

        // coordinates
        String overWorldCoordinate = mc.player.dimension != -1 ? "XYZ " + MathUtil.roundFloat(mc.player.posX, 1) + " " + MathUtil.roundFloat(mc.player.posY, 1) + " " + MathUtil.roundFloat(mc.player.posZ, 1) : "XYZ " + MathUtil.roundFloat(mc.player.posX * 8, 1) + " " + MathUtil.roundFloat(mc.player.posY * 8, 1) + " " + MathUtil.roundFloat(mc.player.posZ * 8, 1);
        String netherCoordinate = mc.player.dimension == -1 ? "XYZ " + MathUtil.roundFloat(mc.player.posX, 1) + " " + MathUtil.roundFloat(mc.player.posY, 1) + " " + MathUtil.roundFloat(mc.player.posZ, 1) : "XYZ " + MathUtil.roundFloat(mc.player.posX / 8, 1) + " " + MathUtil.roundFloat(mc.player.posY / 8, 1) + " " + MathUtil.roundFloat(mc.player.posZ / 8, 1);

        coordinates.setText(overWorldCoordinate);
        netherCoordinates.setText("[Nether] " + netherCoordinate);
    }
}
