/*
 * This file is part of HuskHomes, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.huskhomes.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.fabric.api.permissions.v0.PermissionCheckEvent;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
//? if >=1.21.11 {
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
//?}
import net.minecraft.server.level.ServerPlayer;
import net.william278.huskhomes.FabricHuskHomes;
import net.william278.huskhomes.user.CommandUser;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Predicate;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
//? if >=26.1.2 {
import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;
//?} else {
/*import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
*///?}

public class FabricCommand {

    private final FabricHuskHomes plugin;
    private final Command command;

    public FabricCommand(@NotNull Command command, @NotNull FabricHuskHomes plugin) {
        this.command = command;
        this.plugin = plugin;
    }

    public void register(
            @NotNull CommandDispatcher<CommandSourceStack> dispatcher
    ) {
        // Register brigadier command
        final int permissionLevel = command.isOperatorCommand() ? 3 : 0;
        final Predicate<CommandSourceStack> predicate = Permissions
                .require(
                        command.getPermission(),
                        //? if >=1.21.11 {
                        PermissionLevel.byId(permissionLevel)
                        //?} else {
                        /*permissionLevel
                        *///?}
                );
        final RequiredArgumentBuilder<CommandSourceStack, String> requiredArgumentBuilder = argument(
                command.getRawUsage().replaceAll("[<>\\[\\]]", ""), greedyString()
        );
        final LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(command.getName())
                .requires(predicate).executes(getBrigadierExecutor());
        plugin.getPermissions().put(command.getPermission(), command.isOperatorCommand());
        if (!command.getRawUsage().isBlank()) {
            builder.then(requiredArgumentBuilder.executes(getBrigadierExecutor()).suggests(getBrigadierSuggester()));
        }

        // Register additional permissions
        final Map<String, Boolean> permissions = command.getAdditionalPermissions();
        permissions.forEach((permission, isOp) -> plugin.getPermissions().put(permission, isOp));
        PermissionCheckEvent.EVENT.register((player, node) -> {
            if (permissions.containsKey(node) && permissions.get(node) &&
                    (!(player instanceof CommandSourceStack source) || hasPermissionLevel(source, 3))
            ) {
                return TriState.TRUE;
            }
            return TriState.DEFAULT;
        });

        // Register aliases
        final LiteralCommandNode<CommandSourceStack> node = dispatcher.register(builder);
        final LiteralArgumentBuilder<CommandSourceStack> subcommandBuilder = literal("huskhomes:%s"
                .formatted(command.getName()));
        dispatcher.register(subcommandBuilder.requires(predicate).executes(getBrigadierExecutor()).redirect(node));
        command.getAliases().forEach(alias -> {
            LiteralArgumentBuilder<CommandSourceStack> aliasBuilder = literal(alias);
            LiteralArgumentBuilder<CommandSourceStack> subcommandAliasBuilder = literal("huskhomes:%s".formatted(alias));
            dispatcher.register(aliasBuilder.requires(predicate)
                    .executes(getBrigadierExecutor()).redirect(node));
            dispatcher.register(subcommandAliasBuilder.requires(predicate)
                    .executes(getBrigadierExecutor()).redirect(node));
        });
    }

    private boolean hasPermissionLevel(
            CommandSourceStack source,
            int level
    ) {
        //? if >=1.21.11 {
        return source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(level)));
        //?} else {
        /*return source.hasPermission(level);
        *///?}
    }

    private com.mojang.brigadier.Command<CommandSourceStack> getBrigadierExecutor() {
        return (context) -> {
            command.onExecuted(
                    resolveExecutor(context.getSource()),
                    context.getNodes().stream()
                            .filter(node -> node.getNode() instanceof ArgumentCommandNode<?, ?>)
                            .map(node -> context.getInput().substring(node.getRange().getStart(),
                                    node.getRange().getEnd()).split(" "))
                            .findFirst().orElseGet(() -> new String[0])
            );
            return 1;
        };
    }

    private com.mojang.brigadier.suggestion.SuggestionProvider<CommandSourceStack> getBrigadierSuggester() {
        if (!(command instanceof TabCompletable provider)) {
            return (context, builder) -> com.mojang.brigadier.suggestion.Suggestions.empty();
        }
        return (context, builder) -> {
            final String[] args = builder.getRemaining().split(" ", -1);
            provider.getSuggestions(resolveExecutor(context.getSource()), args).stream()
                    .map(suggestion -> {
                        final String completedArgs = String.join(" ", args);
                        int lastIndex = completedArgs.lastIndexOf(" ");
                        if (lastIndex == -1) {
                            return suggestion;
                        }
                        return completedArgs.substring(0, lastIndex + 1) + suggestion;
                    })
                    .forEach(builder::suggest);
            return builder.buildFuture();
        };
    }

    private CommandUser resolveExecutor(
            @NotNull CommandSourceStack source
    ) {
        if (source.getEntity() instanceof ServerPlayer player) {
            return plugin.getOnlineUser(player);
        }
        return plugin.getConsole();
    }

}
