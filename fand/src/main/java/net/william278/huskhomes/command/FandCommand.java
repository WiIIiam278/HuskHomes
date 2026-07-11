/*
 * This file is part of HuskHomes, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 */

package net.william278.huskhomes.command;

import io.fand.api.command.Arguments;
import io.fand.api.command.CommandContext;
import io.fand.api.command.CommandSender;
import io.fand.api.entity.Player;
import net.william278.huskhomes.FandHuskHomes;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.ConsoleUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class FandCommand {

    private final Command command;
    private final FandHuskHomes plugin;

    public FandCommand(@NotNull Command command, @NotNull FandHuskHomes plugin) {
        this.command = command;
        this.plugin = plugin;
    }

    public void register() {
        plugin.getContext().commands().register(command.getName(), builder -> {
            builder.namespace("huskhomes")
                    .permission(command.getPermission())
                    .executes(this::execute);
            final List<String> aliases = command.getAliases().stream()
                    .filter(alias -> !alias.equals(command.getName()))
                    .toList();
            if (!aliases.isEmpty()) {
                builder.aliases(aliases);
            }
            if (!command.getRawUsage().isBlank()) {
                builder.argument("arguments", Arguments.greedyString(), argument -> {
                    argument.executes(this::execute);
                    if (command instanceof TabCompletable) {
                        argument.suggests(this::suggest);
                    }
                });
            }
        });
    }

    private void execute(@NotNull CommandContext context) {
        command.onExecuted(resolve(context.sender()), context.args().toArray(String[]::new));
    }

    @NotNull
    private List<String> suggest(@NotNull CommandContext context) {
        if (!(command instanceof TabCompletable completable)) {
            return List.of();
        }
        return completable.getSuggestions(resolve(context.sender()), context.args().toArray(String[]::new));
    }

    @NotNull
    private CommandUser resolve(@NotNull CommandSender sender) {
        return sender instanceof Player player ? plugin.getOnlineUser(player) : ConsoleUser.wrap(sender);
    }
}
