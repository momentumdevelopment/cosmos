package cope.cosmos.client.features.command.commands;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.asm.mixins.accessor.ISoundHandler;
import cope.cosmos.client.features.command.Command;

/**
 * @author linustouchtips
 * @since 08/24/2022
 */
public class ReloadSoundCommand extends Command {
    public static ReloadSoundCommand INSTANCE;

    public ReloadSoundCommand() {
        super("ReloadSound", new String[]{"SoundReload"}, "Reloads the world sound");
        INSTANCE = this;
    }

    @Override
    public void onExecute(String[] args) {

        if (args.length == 0) {

            // reload sound
            ((ISoundHandler) mc.getSoundHandler()).getSoundManager().reloadSoundSystem();

            // notify
            getCosmos().getChatManager().sendClientMessage("Sound Reloaded");
        }

        else {

            // unrecognized arguments exception
            getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Arguments!", ChatFormatting.RED + "Please enter the correct arguments for this command.");
        }
    }

    @Override
    public String getUseCase() {
        return "";
    }

    @Override
    public int getArgSize() {
        return 0;
    }
}
