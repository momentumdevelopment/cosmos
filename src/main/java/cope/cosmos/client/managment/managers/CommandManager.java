package cope.cosmos.client.managment.managers;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.features.command.Command;
import cope.cosmos.client.features.command.commands.Drawn;
import cope.cosmos.client.features.command.commands.Friend;
import cope.cosmos.client.features.command.commands.Preset;
import cope.cosmos.client.managment.Manager;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CommandManager extends Manager {
	public CommandManager() {
		super("CommandManager", "Manages client commands");
	}

	private static final List<Command> commands = Arrays.asList(
			new Friend(),
			new Preset(),
			new Drawn()
	);

	public static void registerCommands() {
		for (Command command : getAllCommands()) {
			Cosmos.INSTANCE.getCommandDispatcher().register(command.getCommand());
			Cosmos.INSTANCE.getCommandDispatcher().register(Command.redirectBuilder(command.getName(), command.getCommand().build()));
		}
	}
		
	public static List<Command> getAllCommands() {
		return CommandManager.commands;
	}
		
	public static List<Command> getCommands(Predicate<? super Command> predicate) {
		return CommandManager.commands.stream().filter(predicate).collect(Collectors.toList());
	}
		
	public static Command getCommand(Predicate<? super Command> predicate) {
		return CommandManager.commands.stream().filter(predicate).findFirst().orElse(null);
	}
}
