package cope.cosmos.client.manager.managers;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.features.command.Command;
import cope.cosmos.client.features.command.commands.Drawn;
import cope.cosmos.client.features.command.commands.Friend;
import cope.cosmos.client.features.command.commands.Help;
import cope.cosmos.client.features.command.commands.Preset;
import cope.cosmos.client.manager.Manager;

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
	private final List<Command> commands;

	public CommandManager() {
		super("CommandManager", "Manages client commands");

		// add all commands
		commands = Arrays.asList(
				new Friend(),
				new Preset(),
				new Help(),
				new Drawn()
		);

		// register commands to dispatcher
		getAllCommands().forEach(command -> {
			Cosmos.INSTANCE.getCommandDispatcher().register(command.getCommand());
			Cosmos.INSTANCE.getCommandDispatcher().register(Command.redirectBuilder(command.getName(), command.getCommand().build()));
		});
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
