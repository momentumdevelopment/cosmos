package cope.cosmos.client.features.command;

import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import cope.cosmos.client.features.Feature;
import cope.cosmos.utility.IUtility;

import java.util.function.Predicate;

public class Command extends Feature implements IUtility {

	private final LiteralArgumentBuilder<Object> command;
	
	public Command(String name, String description, LiteralArgumentBuilder<Object> command) {
		super(name, description);
		this.command = command;
	}

	public LiteralArgumentBuilder<Object> getCommand() {
		return this.command;
	}

	@SuppressWarnings("unchecked")
	public static LiteralArgumentBuilder<Object> redirectBuilder(String alias, LiteralCommandNode<?> destination) {
		LiteralArgumentBuilder<Object> literalArgumentBuilder = LiteralArgumentBuilder.literal(alias.toLowerCase()).requires((Predicate<Object>) destination.getRequirement()).forward((CommandNode<Object>) destination.getRedirect(), (RedirectModifier<Object>) destination.getRedirectModifier(), destination.isFork()).executes((com.mojang.brigadier.Command<Object>) destination.getCommand());

		for (CommandNode<?> child : destination.getChildren()) {
			literalArgumentBuilder.then((CommandNode<Object>) child);
		}

		return literalArgumentBuilder;
	}
}
