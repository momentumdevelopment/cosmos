package cope.cosmos.client.features.modules.client;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.util.system.MathUtil;
import net.minecraft.util.EnumFacing;

import javax.swing.*;
import java.awt.Font;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class StreamerMode extends Module {
    public StreamerMode() {
        super("StreamerMode", Category.CLIENT, "Opens a separate window that shows your coords etc. Helpful for streaming");
    }

    private JFrame frame;
    private JLabel facing, coords, netherCoords;

    @Override
    public void onEnable() {
        if (!nullCheck()) {
            toggle();
            return;
        }

        // god do i hate how this code looks
        frame = new JFrame("Cosmos Streamer Mode");

        frame.setSize(1000, 350);
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

        frame.getContentPane().setBackground(Color.BLACK);

        facing = new JLabel("Facing: ");
        facing.setFont(new Font("Verdana", Font.PLAIN, 26));

        coords = new JLabel("XYZ: ");
        coords.setFont(new Font("Verdana", Font.PLAIN, 26));

        netherCoords = new JLabel("XYZ [Nether]: ");
        netherCoords.setFont(new Font("Verdana", Font.PLAIN, 26));

        frame.add(facing);
        frame.add(coords);
        frame.add(netherCoords);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                toggle();
            }
        });

        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public void onDisable() {
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

        facing.setText("Facing: " + direction + " (" + direction.getAxis().name() + (axisDirection.equals(EnumFacing.AxisDirection.POSITIVE) ? "+" : "-") + ")");

        // coordinates
        String overWorldCoordinates = mc.player.dimension != -1 ? "XYZ " + MathUtil.roundFloat(mc.player.posX, 1) + " " + MathUtil.roundFloat(mc.player.posY, 1) + " " + MathUtil.roundFloat(mc.player.posZ, 1) : "XYZ " + MathUtil.roundFloat(mc.player.posX * 8, 1) + " " + MathUtil.roundFloat(mc.player.posY * 8, 1) + " " + MathUtil.roundFloat(mc.player.posZ * 8, 1);
        String netherCoordinates = mc.player.dimension == -1 ? "XYZ " + MathUtil.roundFloat(mc.player.posX, 1) + " " + MathUtil.roundFloat(mc.player.posY, 1) + " " + MathUtil.roundFloat(mc.player.posZ, 1) : "XYZ " + MathUtil.roundFloat(mc.player.posX / 8, 1) + " " + MathUtil.roundFloat(mc.player.posY / 8, 1) + " " + MathUtil.roundFloat(mc.player.posZ / 8, 1);

        coords.setText(overWorldCoordinates);
        netherCoords.setText("[Nether] " + netherCoordinates);
    }
}
