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
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.fabric.api.permissions.v0.PermissionCheckEvent;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.william278.huskhomes.FabricHuskHomes;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.teleport.TeleportRequest;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.FabricUser;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class FabricCommand {

    private final FabricHuskHomes plugin;
    private final Command command;

    public FabricCommand(@NotNull Command command, @NotNull FabricHuskHomes plugin) {
        this.command = command;
        this.plugin = plugin;
    }

    public void register(@NotNull CommandDispatcher<ServerCommandSource> dispatcher) {
        // Register brigadier command
        final Predicate<ServerCommandSource> predicate = Permissions
                .require(command.getPermission(), command.isOperatorCommand() ? 3 : 0);
        final LiteralArgumentBuilder<ServerCommandSource> builder = literal(command.getName())
                .requires(predicate).executes(getBrigadierExecutor());
        plugin.getPermissions().put(command.getPermission(), command.isOperatorCommand());
        if (!command.getRawUsage().isBlank()) {
            builder.then(argument(command.getRawUsage().replaceAll("[<>\\[\\]]", ""), greedyString())
                    .executes(getBrigadierExecutor())
                    .suggests(getBrigadierSuggester()));
        }

        // Register additional permissions
        final Map<String, Boolean> permissions = command.getAdditionalPermissions();
        permissions.forEach((permission, isOp) -> plugin.getPermissions().put(permission, isOp));
        PermissionCheckEvent.EVENT.register((player, node) -> {
            if (permissions.containsKey(node) && permissions.get(node) && player.hasPermissionLevel(3)) {
                return TriState.TRUE;
            }
            return TriState.DEFAULT;
        });

        // Register aliases
        final LiteralCommandNode<ServerCommandSource> node = dispatcher.register(builder);
        dispatcher.register(literal("huskhomes:" + command.getName())
                .requires(predicate).executes(getBrigadierExecutor()).redirect(node));
        command.getAliases().forEach(alias -> dispatcher.register(literal(alias)
                .requires(predicate).executes(getBrigadierExecutor()).redirect(node)));
    }

    private com.mojang.brigadier.Command<ServerCommandSource> getBrigadierExecutor() {
        return (context) -> {
            command.onExecuted(
                    resolveExecutor(context.getSource()),
                    command.removeFirstArg(context.getInput().split(" "))
            );
            return 1;
        };
    }

    private com.mojang.brigadier.suggestion.SuggestionProvider<ServerCommandSource> getBrigadierSuggester() {
        if (!(command instanceof TabProvider provider)) {
            return (context, builder) -> com.mojang.brigadier.suggestion.Suggestions.empty();
        }
        return (context, builder) -> {
            final String[] args = command.removeFirstArg(context.getInput().split(" "));
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

    private CommandUser resolveExecutor(@NotNull ServerCommandSource source) {
        if (source.getEntity() instanceof ServerPlayerEntity player) {
            return FabricUser.adapt(player, plugin);
        }
        return plugin.getConsole();
    }


    /**
     * Commands available on the Fabric HuskHomes implementation
     */
    public enum Type {
        HOME_COMMAND(PrivateHomeCommand::new),
        SET_HOME_COMMAND(SetHomeCommand::new),
        HOME_LIST_COMMAND(PrivateHomeListCommand::new),
        DEL_HOME_COMMAND(DelHomeCommand::new),
        EDIT_HOME_COMMAND(EditHomeCommand::new),
        PUBLIC_HOME_COMMAND(PublicHomeCommand::new),
        PUBLIC_HOME_LIST_COMMAND(PublicHomeListCommand::new),
        WARP_COMMAND(WarpCommand::new),
        SET_WARP_COMMAND(SetWarpCommand::new),
        WARP_LIST_COMMAND(WarpListCommand::new),
        DEL_WARP_COMMAND(DelWarpCommand::new),
        EDIT_WARP_COMMAND(EditWarpCommand::new),
        TP_COMMAND(TpCommand::new),
        TP_HERE_COMMAND(TpHereCommand::new),
        TPA_COMMAND((plugin) -> new TeleportRequestCommand(plugin, TeleportRequest.Type.TPA)),
        TPA_HERE_COMMAND((plugin) -> new TeleportRequestCommand(plugin, TeleportRequest.Type.TPA_HERE)),
        TPACCEPT_COMMAND((plugin) -> new TpRespondCommand(plugin, true)),
        TPDECLINE_COMMAND((plugin) -> new TpRespondCommand(plugin, false)),
        RTP_COMMAND(RtpCommand::new),
        TP_IGNORE_COMMAND(TpIgnoreCommand::new),
        TP_OFFLINE_COMMAND(TpOfflineCommand::new),
        TP_ALL_COMMAND(TpAllCommand::new),
        TPA_ALL_COMMAND(TpaAllCommand::new),
        SPAWN_COMMAND(SpawnCommand::new),
        SET_SPAWN_COMMAND(SetSpawnCommand::new),
        BACK_COMMAND(BackCommand::new),
        HUSKHOMES_COMMAND(HuskHomesCommand::new);

        private final Function<HuskHomes, Command> supplier;

        Type(@NotNull Function<HuskHomes, Command> supplier) {
            this.supplier = supplier;
        }

        @NotNull
        public Command createCommand(@NotNull HuskHomes plugin) {
            return supplier.apply(plugin);
        }

        @NotNull
        public static List<Command> getCommands(@NotNull FabricHuskHomes plugin) {
            return Arrays.stream(values())
                    .map(type -> type.createCommand(plugin))
                    .map(command -> plugin.getSettings().isCommandDisabled(command) ? null : command)
                    .filter(Objects::nonNull)
                    .toList();
        }

    }

}
