package cope.cosmos.client.features.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import cope.cosmos.client.features.command.Command;
import cope.cosmos.client.features.modules.misc.FakePlayerModule;

/**
 * Placeholder, going to be added onto
 *
 * @author aesthetical
 */
public class FakePlayerCommand extends Command {
    public FakePlayerCommand() {
        super("fakeplayer", "Manages the fakeplayer", LiteralArgumentBuilder.literal("fakeplayer")
                .executes((ctx) -> {
                    FakePlayerModule.INSTANCE.toggle();
                    return 0;
                }));
    }
}
