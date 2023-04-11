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
import net.william278.huskhomes.teleport.TeleportRequest;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.FabricUser;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

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
        final LiteralArgumentBuilder<ServerCommandSource> builder = literal(command.getName())
                .requires(Permissions.require(command.getPermission(), command.isOperatorCommand() ? 3 : 0))
                .executes(getBrigadierExecutor());
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
        dispatcher.register(literal("huskhomes:" + command.getName()).executes(getBrigadierExecutor()).redirect(node));
        command.getAliases().forEach(alias -> dispatcher.register(literal(alias).executes(getBrigadierExecutor()).redirect(node)));
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
            return FabricUser.adapt(plugin, player);
        }
        return plugin.getConsole();
    }


    /**
     * Commands available on the Fabric HuskHomes implementation
     */
    public enum Type {
        HOME_COMMAND(new PrivateHomeCommand(FabricHuskHomes.getInstance())),
        SET_HOME_COMMAND(new SetHomeCommand(FabricHuskHomes.getInstance())),
        HOME_LIST_COMMAND(new PrivateHomeListCommand(FabricHuskHomes.getInstance())),
        DEL_HOME_COMMAND(new DelHomeCommand(FabricHuskHomes.getInstance())),
        EDIT_HOME_COMMAND(new EditHomeCommand(FabricHuskHomes.getInstance())),
        PUBLIC_HOME_COMMAND(new PublicHomeCommand(FabricHuskHomes.getInstance())),
        PUBLIC_HOME_LIST_COMMAND(new PublicHomeListCommand(FabricHuskHomes.getInstance())),
        WARP_COMMAND(new WarpCommand(FabricHuskHomes.getInstance())),
        SET_WARP_COMMAND(new SetWarpCommand(FabricHuskHomes.getInstance())),
        WARP_LIST_COMMAND(new WarpListCommand(FabricHuskHomes.getInstance())),
        DEL_WARP_COMMAND(new DelWarpCommand(FabricHuskHomes.getInstance())),
        EDIT_WARP_COMMAND(new EditWarpCommand(FabricHuskHomes.getInstance())),
        TP_COMMAND(new TpCommand(FabricHuskHomes.getInstance())),
        TP_HERE_COMMAND(new TpHereCommand(FabricHuskHomes.getInstance())),
        TPA_COMMAND(new TeleportRequestCommand(FabricHuskHomes.getInstance(), TeleportRequest.Type.TPA)),
        TPA_HERE_COMMAND(new TeleportRequestCommand(FabricHuskHomes.getInstance(), TeleportRequest.Type.TPA_HERE)),
        TPACCEPT_COMMAND(new TpRespondCommand(FabricHuskHomes.getInstance(), true)),
        TPDECLINE_COMMAND(new TpRespondCommand(FabricHuskHomes.getInstance(), false)),
        RTP_COMMAND(new RtpCommand(FabricHuskHomes.getInstance())),
        TP_IGNORE_COMMAND(new TpIgnoreCommand(FabricHuskHomes.getInstance())),
        TP_OFFLINE_COMMAND(new TpOfflineCommand(FabricHuskHomes.getInstance())),
        TP_ALL_COMMAND(new TpAllCommand(FabricHuskHomes.getInstance())),
        TPA_ALL_COMMAND(new TpaAllCommand(FabricHuskHomes.getInstance())),
        SPAWN_COMMAND(new SpawnCommand(FabricHuskHomes.getInstance())),
        SET_SPAWN_COMMAND(new SetSpawnCommand(FabricHuskHomes.getInstance())),
        BACK_COMMAND(new BackCommand(FabricHuskHomes.getInstance())),
        HUSKHOMES_COMMAND(new HuskHomesCommand(FabricHuskHomes.getInstance()));

        private final Command command;

        Type(@NotNull Command command) {
            this.command = command;
        }

        @NotNull
        public Command getCommand() {
            return command;
        }
    }

}
