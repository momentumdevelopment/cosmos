package cope.cosmos.client.features.command.commands;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.features.command.Command;
import cope.cosmos.client.manager.managers.WaypointManager.*;
import cope.cosmos.util.world.WorldUtil;
import net.minecraft.util.math.Vec3d;

/**
 * @author linustouchtips
 * @since 08/25/2022
 */
public class WaypointCommand extends Command {
    public static WaypointCommand INSTANCE;

    public WaypointCommand() {
        super("Waypoint", "Adds or removes a waypoint to the client's waypoints");
        INSTANCE = this;
    }

    @Override
    public void onExecute(String[] args) {

        // add args
        if (args.length == 5) {
            try {

                // coordinates
                double x = Double.parseDouble(args[1]);
                double y = Double.parseDouble(args[2]);
                double z = Double.parseDouble(args[3]);

                // add
                if (args[0].equalsIgnoreCase("Add")) {

                    // add waypoint
                    getCosmos().getWaypointManager().addWaypoint(args[4], new Waypoint(new Vec3d(x, y ,z), WorldUtil.getWorldName(), Format.COORDINATE));
                    getCosmos().getChatManager().sendClientMessage("Added waypoint " + ChatFormatting.GRAY + args[4]);
                }

                else {

                    // unrecognized action
                    getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Action!", ChatFormatting.RED + "Please use a valid action.");
                }

            } catch (NumberFormatException | NullPointerException exception) {

                // unrecognized value exception
                getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Coordinates!", ChatFormatting.RED + "Please enter valid coordinates.");
            }
        }

        // remove args
        else if (args.length == 2) {

            // delete
            if (args[0].equalsIgnoreCase("Remove") || args[0].equalsIgnoreCase("Delete") || args[0].equalsIgnoreCase("Del")) {

                // remove waypoint
                getCosmos().getWaypointManager().removeWaypoint(args[1]);
                getCosmos().getChatManager().sendClientMessage("Removed waypoint " + ChatFormatting.GRAY + args[1]);
            }

            else {

                // unrecognized action
                getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Action!", ChatFormatting.RED + "Please use a valid action.");
            }
        }

        else {

            // unrecognized arguments exception
            getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Arguments!", ChatFormatting.RED + "Please enter the correct arguments for this command.");
        }
    }

    @Override
    public String getUseCase() {
        return "<add/remove> <optional:x> <optional:y> <optional:z> <name>";
    }

    @Override
    public int getArgSize() {
        return 5;
    }
}
