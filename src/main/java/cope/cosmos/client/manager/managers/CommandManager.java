package cope.cosmos.client.manager.managers;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.asm.mixins.accessor.IGuiChat;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.render.gui.RenderChatTextEvent;
import cope.cosmos.client.features.command.Command;
import cope.cosmos.client.features.command.commands.*;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.client.FontModule;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.util.render.FontUtil;
import net.minecraft.client.gui.GuiChat;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author linustouchtips
 * @since 06/08/2021
 */
public class CommandManager extends Manager {

	// list of commands
	private final List<Command> commands = new ArrayList<>();

	public CommandManager() {
		super("CommandManager", "Manages client commands");

		// add all commands
		commands.add(new HelpCommand());
		commands.add(new BindCommand());
		commands.add(new ConfigCommand());
		commands.add(new HClipCommand());
		commands.add(new VClipCommand());
		commands.add(new FriendCommand());
		commands.add(new ReloadSoundCommand());
		commands.add(new FolderCommand());
		commands.add(new DrawnCommand());
		commands.add(new PrefixCommand());
		commands.add(new ToggleCommand());
		commands.add(new GamemodeCommand());
		commands.add(new FontCommand());
		commands.add(new WaypointCommand());
		commands.add(new VanishCommand());

		// add setting commands for each module
		for (Module module : getCosmos().getModuleManager().getAllModules()) {

			// add setting command
			commands.add(new SettingCommand(module));
		}

		Cosmos.EVENT_BUS.register(this);
	}

	// suggestion
	StringBuilder suggestionBuilder = new StringBuilder();

	@Override
	public void onUpdate() {

		// reset suggestion builder
		suggestionBuilder = new StringBuilder();

		// player is in gui
		if (mc.currentScreen != null) {

			// player is in chat
			if (mc.currentScreen instanceof GuiChat) {

				// chat input
				String input = ((IGuiChat) mc.currentScreen).getInputField().getText();

				// argument inputs
				String[] args = input.split(" ");

				// event the user sends a command
				if (input.startsWith(Cosmos.PREFIX)) {

					// command
					String[] finalArgs = args;
					String command = finalArgs[0].substring(1);

					// suggestion
					Command suggestion = getCosmos().getCommandManager().getCommand(command1 -> command1.startsWith(command) != -1);

					// args without command
					args = Arrays.copyOfRange(args, 1, args.length);

					// found a suggestion
					if (suggestion != null) {

						// use cases
						String[] cases = suggestion.getUseCase().split(" ");

						// index of the suggestion
						int index = suggestion.startsWith(command);

						// must match
						if (index != -1) {

							// args size
							for (int i = 0; i < suggestion.getArgSize(); i++) {
								if (i < args.length) {

									// sync input
									cases[i] = args[i];
								}
							}
						}

						suggestionBuilder.append(Cosmos.PREFIX);

						// show suggestion
						if (index >= 1000) {
							suggestionBuilder.append(suggestion.getName().toLowerCase());
							suggestionBuilder.append(" ");

							// add cases
							for (String useCase : cases) {
								suggestionBuilder.append(useCase);
								suggestionBuilder.append(" ");
							}
						}

						else {
							suggestionBuilder.append(suggestion.getAliases()[index].toLowerCase());
							suggestionBuilder.append(" ");

							// add cases
							for (String useCase : cases) {
								suggestionBuilder.append(useCase);
								suggestionBuilder.append(" ");
							}
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onChatInput(ClientChatEvent event) {

		// player message
		String message = event.getMessage().trim();

		// event the user sends a command
		if (message.startsWith(Cosmos.PREFIX)) {

			// prevent rendering
			event.setCanceled(true);
			mc.ingameGUI.getChatGUI().addToSentMessages(event.getMessage());

			// remove prefix
			message = message.substring(1);

			// passed arguments
			String[] args = message.split(" ");

			// given command
			String command = args[0];

			// remove command from args
			args = Arrays.copyOfRange(args, 1, args.length);

			// executable command
			Command executable = getCosmos().getCommandManager().getCommand(command1 -> command1.equals(command));

			// execute command
			if (executable != null) {

				// execute
				executable.onExecute(args);
			}

			else {

				// unrecognized command exception
				getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Command!", ChatFormatting.RED + "Please enter a valid command.");
			}
		}
	}

	@SubscribeEvent
	public void onRenderChatText(RenderChatTextEvent event) {

		// suggest
		if (FontModule.INSTANCE.isEnabled() && FontModule.vanilla.getValue()) {
			FontUtil.drawStringWithShadow(suggestionBuilder.toString(), event.getX(), event.getY(), Color.GRAY.getRGB());
		}

		else {
			mc.fontRenderer.drawStringWithShadow(suggestionBuilder.toString(), event.getX(), event.getY(), Color.GRAY.getRGB());
		}
	}

	/**
	 * Gets a list of all the client's commands
	 * @return List of all the client's commands
	 */
	public List<Command> getAllCommands() {
		return commands;
	}

	/**
	 * Gets a list of all the client's commands that fulfill a specified condition
	 * @param predicate The specified condition
	 * @return List of all the client's commands that fulfill the specified condition
	 */
	public List<Command> getCommands(Predicate<? super Command> predicate) {
		return commands.stream()
				.filter(predicate)
				.collect(Collectors.toList());
	}

	/**
	 * Gets the first command that fulfills a specified condition
	 * @param predicate The specified condition
	 * @return The first command that fulfills the specified condition
	 */
	public Command getCommand(Predicate<? super Command> predicate) {
		return commands.stream()
				.filter(predicate)
				.findFirst()
				.orElse(null);
	}
}
