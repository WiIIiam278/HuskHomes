package net.william278.huskhomes.command;

import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.william278.huskhomes.SpongeHuskHomes;
import net.william278.huskhomes.player.SpongePlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.plugin.PluginContainer;

import java.util.ArrayList;
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

    public SpongeCommand(@NotNull CommandBase command, @NotNull SpongeHuskHomes implementor) {
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
    public CommandResult process(CommandCause cause, ArgumentReader.Mutable arguments) throws CommandException {
        if (cause.root() instanceof Player player) {
            this.command.onExecute(SpongePlayer.adapt(player), assimilateArguments(arguments));
        } else {
            if (this.command instanceof ConsoleExecutable consoleExecutable) {
                consoleExecutable.onConsoleExecute(assimilateArguments(arguments));
            } else {
                plugin.getLocales().getRawLocale("error_in_game_only").
                        ifPresent(locale -> cause.sendMessage(Identity.nil(), Component.text(locale)));
            }
        }
        return CommandResult.builder()
                .result(200)
                .build();
    }

    @Override
    public List<CommandCompletion> complete(CommandCause cause, ArgumentReader.Mutable arguments) throws CommandException {
        if (this.command instanceof TabCompletable tabCompletable) {
            final SpongePlayer spongePlayer = cause.root() instanceof Player player ? SpongePlayer.adapt(player) : null;
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
        return arguments.immutable().remaining().split(" ");
    }

    @Override
    public boolean canExecute(CommandCause cause) { // todo disabled commands
        if (cause instanceof Player) {
            return cause.hasPermission(command.permission);
        } else {
            return this.command instanceof ConsoleExecutable;
        }
    }

    @Override
    public Optional<Component> shortDescription(CommandCause cause) {
        return Optional.of(Component.text(command.getDescription()));
    }

    @Override
    public Optional<Component> extendedDescription(CommandCause cause) {
        return shortDescription(cause);
    }

    @Override
    public Component usage(CommandCause cause) {
        return Component.text("");
    }
}
