package cope.cosmos.client.features.command;

import cope.cosmos.client.features.Feature;
import cope.cosmos.util.Wrapper;

/**
 * @author Milse113, linustouchtips
 * @since 06/08/2021
 */
public abstract class Command extends Feature implements Wrapper {

	public Command(String name, String description) {
		super(name, description);
	}
	
	public Command(String name, String[] aliases, String description) {
		super(name, description);
		setAliases(aliases);
	}

	/**
	 * Runs after command is executed
	 */
	public abstract void onExecute(String[] args);

	/**
	 * Gets a correct use case
	 * @return The correct use case
	 */
	public abstract String getUseCase();

	/**
	 * Gets the maximum argument size
	 * @return The maximum argument size
	 */
	public abstract int getArgSize();
}
