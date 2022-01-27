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
public class StreamerMode extends Module {
    public static StreamerMode INSTANCE;

    public StreamerMode() {
        super("StreamerMode", Category.CLIENT, "Opens a separate window that shows your coordinates");
        INSTANCE = this;
    }

    private JFrame frame;
    private JLabel facing, coords, netherCoords;

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

        coords = new JLabel("XYZ: ");
        coords.setFont(new Font("Verdana", Font.PLAIN, 26));

        netherCoords = new JLabel("XYZ [Nether]: ");
        netherCoords.setFont(new Font("Verdana", Font.PLAIN, 26));

        // add to frame
        frame.add(facing);
        frame.add(coords);
        frame.add(netherCoords);

        // add a close listener for when the X is pressed. it'll toggle off the module.
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                toggle();
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
            coords = null;
            netherCoords = null;
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
        String overWorldCoordinates = mc.player.dimension != -1 ? "XYZ " + MathUtil.roundFloat(mc.player.posX, 1) + " " + MathUtil.roundFloat(mc.player.posY, 1) + " " + MathUtil.roundFloat(mc.player.posZ, 1) : "XYZ " + MathUtil.roundFloat(mc.player.posX * 8, 1) + " " + MathUtil.roundFloat(mc.player.posY * 8, 1) + " " + MathUtil.roundFloat(mc.player.posZ * 8, 1);
        String netherCoordinates = mc.player.dimension == -1 ? "XYZ " + MathUtil.roundFloat(mc.player.posX, 1) + " " + MathUtil.roundFloat(mc.player.posY, 1) + " " + MathUtil.roundFloat(mc.player.posZ, 1) : "XYZ " + MathUtil.roundFloat(mc.player.posX / 8, 1) + " " + MathUtil.roundFloat(mc.player.posY / 8, 1) + " " + MathUtil.roundFloat(mc.player.posZ / 8, 1);

        coords.setText(overWorldCoordinates);
        netherCoords.setText("[Nether] " + netherCoordinates);
    }
}
