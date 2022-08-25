package cope.cosmos.client.features.command.commands;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.features.command.Command;

/**
 * @author linustouchtips
 * @since 08/24/2022
 */
public class HClipCommand extends Command {
    public static HClipCommand INSTANCE;

    public HClipCommand() {
        super("HClip", "Teleports the player horizontally");
        INSTANCE = this;
    }

    @Override
    public void onExecute(String[] args) {

        if (args.length == 1) {
            try {

                // teleport distance
                double teleport = Double.parseDouble(args[0]);

                // horizontal movement
                double motionX = Math.cos(Math.toRadians(mc.player.rotationYaw + 90));
                double motionZ = Math.sin(Math.toRadians(mc.player.rotationYaw + 90));

                // teleport riding entity
                if (mc.player.isRiding()) {
                    mc.player.getRidingEntity().setPosition(mc.player.getRidingEntity().posX + (motionX * teleport), mc.player.getRidingEntity().posY, mc.player.getRidingEntity().posZ + (motionZ * teleport));
                }

                // teleport player
                else {
                    mc.player.setPosition(mc.player.posX + (motionX * teleport), mc.player.posY, mc.player.posZ + (motionZ * teleport));
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
