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

import net.kyori.adventure.text.Component;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.SpongeHuskHomes;
import net.william278.huskhomes.teleport.TeleportRequest;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.SpongeUser;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.Command.Raw;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.util.Tristate;

import java.util.*;
import java.util.function.Function;

public class SpongeCommand implements Raw {

    private final Command command;
    private final SpongeHuskHomes plugin;

    public SpongeCommand(@NotNull Command command, @NotNull SpongeHuskHomes plugin) {
        this.command = command;
        this.plugin = plugin;
    }

    // Register command
    public void registerCommand(@NotNull RegisterCommandEvent<Raw> event) {
        event.register(
                plugin.getPluginContainer(),
                this,
                command.getName(),
                command.getAliases().toArray(String[]::new)
        );
    }

    // Register command permissions
    public void registerPermissions() {
        final Map<String, Boolean> permissionDescriptions = new HashMap<>(command.getAdditionalPermissions());
        permissionDescriptions.put(command.getPermission(), command.isOperatorCommand());

        for (Map.Entry<String, Boolean> node : permissionDescriptions.entrySet()) {
            final PermissionDescription.Builder builder = plugin.getGame().server()
                    .serviceProvider().permissionService()
                    .newDescriptionBuilder(plugin.getPluginContainer())
                    .id(node.getKey());

            // Set default access level
            if (node.getValue()) {
                builder.defaultValue(Tristate.UNDEFINED)
                        .assign(PermissionDescription.ROLE_ADMIN, true);
            } else {
                builder.defaultValue(Tristate.TRUE)
                        .assign(PermissionDescription.ROLE_USER, true);
            }
            builder.register();
        }
    }

    @NotNull
    public Command getCommand() {
        return command;
    }

    @Override
    @NotNull
    public CommandResult process(@NotNull CommandCause cause, @NotNull ArgumentReader.Mutable arguments) {
        command.onExecuted(resolveExecutor(cause), assimilateArguments(arguments));
        return CommandResult.success();
    }

    @Override
    @NotNull
    public List<CommandCompletion> complete(@NotNull CommandCause cause, @NotNull ArgumentReader.Mutable arguments) {
        if (this.command instanceof TabCompletable provider) {
            return provider.getSuggestions(resolveExecutor(cause), assimilateArguments(arguments)).stream()
                    .map(CommandCompletion::of)
                    .toList();
        }
        return List.of();
    }

    @NotNull
    private CommandUser resolveExecutor(@NotNull CommandCause cause) {
        if (cause.root() instanceof ServerPlayer player) {
            return SpongeUser.adapt(player, plugin);
        }
        return plugin.getConsole();
    }

    /**
     * Assimilate the arguments from a {@link ArgumentReader} into a spaced {@link String} array.
     *
     * @param arguments the {@link ArgumentReader} to assimilate
     * @return the assimilated {@link String} array
     */
    @NotNull
    private String[] assimilateArguments(@NotNull ArgumentReader.Mutable arguments) {
        final String argumentString = arguments.immutable().remaining();
        if (argumentString.isBlank()) {
            return new String[0];
        }
        return argumentString.split(" ");
    }

    @Override
    public boolean canExecute(@NotNull CommandCause cause) {
        return resolveExecutor(cause).hasPermission(command.getPermission());
    }

    @Override
    @NotNull
    public Optional<Component> shortDescription(@NotNull CommandCause cause) {
        return Optional.of(Component.text(command.getDescription()));
    }

    @Override
    @NotNull
    public Optional<Component> extendedDescription(@NotNull CommandCause cause) {
        return shortDescription(cause);
    }

    @Override
    @NotNull
    public Component usage(@NotNull CommandCause cause) {
        return Component.text(command.getUsage());
    }

    /**
     * Commands available on the Sponge HuskHomes implementation.
     */
    public enum Type {
        HOME_COMMAND(PrivateHomeCommand::new),
        SET_HOME_COMMAND(SetHomeCommand::new),
        HOME_LIST_COMMAND(HomeListCommand::new),
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
        TPA_COMMAND((plugin) -> new TpRequestCommand(plugin, TeleportRequest.Type.TPA)),
        TPA_HERE_COMMAND((plugin) -> new TpRequestCommand(plugin, TeleportRequest.Type.TPA_HERE)),
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
        public static List<Command> getCommands(@NotNull SpongeHuskHomes plugin) {
            return Arrays.stream(values())
                    .map(type -> type.createCommand(plugin))
                    .map(command -> plugin.getSettings().isCommandDisabled(command) ? null : command)
                    .filter(Objects::nonNull)
                    .toList();
        }

    }

}
