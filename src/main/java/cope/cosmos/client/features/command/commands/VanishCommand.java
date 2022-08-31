package cope.cosmos.client.features.command.commands;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.features.command.Command;
import net.minecraft.entity.Entity;

/**
 * @author linustouchtips
 * @since 08/26/2022
 */
public class VanishCommand extends Command {
    public static VanishCommand INSTANCE;

    public VanishCommand() {
        super("Vanish", new String[]{"Godmode", "EntityDesync"}, "Desyncs the riding entity");
        INSTANCE = this;
    }

    // riding entity
    private Entity vanishEntity;

    @Override
    public void onExecute(String[] args) {

        if (args.length == 1) {

            // remove entity from world
            if (args[0].equalsIgnoreCase("Dismount") || args[0].equalsIgnoreCase("D") || args[0].equalsIgnoreCase("Dis")) {

                // check if player is riding an entity
                if (mc.player.isRiding() && mc.player.getRidingEntity() != null) {

                    // check if already in vanish
                    if (vanishEntity == null) {

                        // entity to remove
                        vanishEntity = mc.player.getRidingEntity();

                        // vanish entity
                        mc.player.dismountRidingEntity();
                        mc.world.removeEntityDangerously(vanishEntity);
                    }

                    else {

                        // vanished exception
                        getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Already Vanished!", ChatFormatting.RED + "You must be remount the vanished entity before dismounting.");
                    }
                }

                else {

                    // not riding exception
                    getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Not Riding Entity!", ChatFormatting.RED + "You must be riding an entity to use this command.");
                }
            }

            // re-add entity to world
            else if (args[0].equalsIgnoreCase("Remount") || args[0].equalsIgnoreCase("R") || args[0].equalsIgnoreCase("Re")) {

                // check if already in vanish
                if (vanishEntity != null) {

                    // re-add entity to world
                    vanishEntity.isDead = false;
                    mc.world.addEntityToWorld(vanishEntity.getEntityId(), vanishEntity);

                    // remount
                    mc.player.startRiding(vanishEntity, true);
                    vanishEntity = null;
                }

                else {

                    // vanished exception
                    getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "No Vanished Entity!", ChatFormatting.RED + "You must be vanish an entity before attempting to remount.");
                }
            }
        }

        else {

            // unrecognized arguments exception
            getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Arguments!", ChatFormatting.RED + "Please enter the correct arguments for this command.");
        }
    }

    @Override
    public String getUseCase() {
        return "<dismount/remount>";
    }

    @Override
    public int getArgSize() {
        return 1;
    }
}
