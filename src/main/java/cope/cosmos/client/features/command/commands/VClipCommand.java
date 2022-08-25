package cope.cosmos.client.features.command.commands;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.features.command.Command;

/**
 * @author linustouchtips
 * @since 08/24/2022
 */
public class VClipCommand extends Command {
    public static VClipCommand INSTANCE;

    public VClipCommand() {
        super("VClip", "Teleports the player vertically");
        INSTANCE = this;
    }

    @Override
    public void onExecute(String[] args) {

        if (args.length == 1) {
            try {

                // teleport distance
                double teleport = Double.parseDouble(args[0]);

                // teleport riding entity
                if (mc.player.isRiding()) {
                    mc.player.getRidingEntity().setPosition(mc.player.getRidingEntity().posX, mc.player.getRidingEntity().posY + teleport, mc.player.getRidingEntity().posZ);
                }

                // teleport player
                else {
                    mc.player.setPosition(mc.player.posX, mc.player.posY + teleport, mc.player.posZ);
                }

            } catch (NumberFormatException | NullPointerException exception) {

                // unrecognized value exception
                getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Value!", ChatFormatting.RED + "Please enter a teleport distance.");
            }
        }

        else {

            // unrecognized arguments exception
            getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Arguments!", ChatFormatting.RED + "Please enter the correct arguments for this command.");
        }
    }

    @Override
    public String getUseCase() {
        return "<distance>";
    }

    @Override
    public int getArgSize() {
        return 1;
    }
}
