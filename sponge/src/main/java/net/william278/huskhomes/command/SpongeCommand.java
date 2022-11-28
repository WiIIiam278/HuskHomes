package net.william278.huskhomes.command;

import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.william278.huskhomes.SpongeHuskHomes;
import net.william278.huskhomes.player.SpongePlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.plugin.PluginContainer;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SpongeCommand implements Command.Raw {

    /**
     * The {@link CommandBase} that will be executed
     */
    private final CommandBase command;

    /**
     * The implementing plugin
     */
    private final SpongeHuskHomes plugin;

    public SpongeCommand(@NotNull CommandBase command,
                         @NotNull SpongeHuskHomes implementor) {
        this.command = command;
        this.plugin = implementor;
    }

    /**
     * Registers a command to this implementation
     *
     * @param event to register the command on
     */
    public void register(@NotNull RegisterCommandEvent<Command.Raw> event, @NotNull PluginContainer container) {
        event.register(container, this, this.command.command, this.command.aliases);
    }

    @Override
    public CommandResult process(@NotNull CommandCause cause, @NotNull ArgumentReader.Mutable arguments) {
        if (cause.root() instanceof ServerPlayer player) {
            this.command.onExecute(SpongePlayer.adapt(player), assimilateArguments(arguments));
        } else {
            if (this.command instanceof ConsoleExecutable consoleExecutable) {
                consoleExecutable.onConsoleExecute(assimilateArguments(arguments));
            } else {
                return CommandResult.error(plugin.getLocales()
                        .getLocale("error_in_game_only")
                        .orElse(new MineDown("Error: That command can only be run in-game."))
                        .toComponent());
            }
        }
        return CommandResult.success();
    }

    @Override
    public List<CommandCompletion> complete(CommandCause cause, ArgumentReader.Mutable arguments) {
        if (this.command instanceof TabCompletable tabCompletable) {
            final SpongePlayer spongePlayer = cause.root() instanceof ServerPlayer player ? SpongePlayer.adapt(player) : null;
            return tabCompletable.onTabComplete(assimilateArguments(arguments), spongePlayer).stream()
                    .map(CommandCompletion::of)
                    .toList();
        }
        return Collections.emptyList();
    }

    /**
     * Assimilate the arguments from a {@link ArgumentReader} into a {@link String} array
     *
     * @param arguments the {@link ArgumentReader} to assimilate
     * @return the assimilated {@link String} array
     */
    private String[] assimilateArguments(@NotNull ArgumentReader.Mutable arguments) {
        final String argumentString = arguments.immutable().remaining();
        if (argumentString.isBlank()) {
            return new String[0];
        }
        return argumentString.split(" ");
    }

    @Override
    public boolean canExecute(@NotNull CommandCause cause) {
        if (cause.root() instanceof ServerPlayer player) {
            return player.hasPermission(command.permission);
        }
        cause.sendMessage(Identity.nil(), Component.text("Error: console executable check???."));
        return this.command instanceof ConsoleExecutable;
    }

    @Override
    public Optional<Component> shortDescription(@NotNull CommandCause cause) {
        return Optional.of(Component.text(command.getDescription()));
    }

    @Override
    public Optional<Component> extendedDescription(@NotNull CommandCause cause) {
        return shortDescription(cause);
    }

    @Override
    public Component usage(@NotNull CommandCause cause) {
        return Component.text(command.getUsage());
    }
}
