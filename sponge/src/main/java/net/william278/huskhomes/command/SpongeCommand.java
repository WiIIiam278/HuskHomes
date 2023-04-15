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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        if (this.command instanceof TabProvider provider) {
            return provider.getSuggestions(resolveExecutor(cause), assimilateArguments(arguments)).stream()
                    .map(CommandCompletion::of)
                    .toList();
        }
        return List.of();
    }

    @NotNull
    private CommandUser resolveExecutor(@NotNull CommandCause cause) {
        if (cause.root() instanceof ServerPlayer player) {
            return SpongeUser.adapt(player);
        }
        return plugin.getConsole();
    }

    /**
     * Assimilate the arguments from a {@link ArgumentReader} into a spaced {@link String} array
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
     * Commands available on the Sponge HuskHomes implementation
     */
    public enum Type {
        HOME_COMMAND(new PrivateHomeCommand(SpongeHuskHomes.getInstance())),
        SET_HOME_COMMAND(new SetHomeCommand(SpongeHuskHomes.getInstance())),
        HOME_LIST_COMMAND(new PrivateHomeListCommand(SpongeHuskHomes.getInstance())),
        DEL_HOME_COMMAND(new DelHomeCommand(SpongeHuskHomes.getInstance())),
        EDIT_HOME_COMMAND(new EditHomeCommand(SpongeHuskHomes.getInstance())),
        PUBLIC_HOME_COMMAND(new PublicHomeCommand(SpongeHuskHomes.getInstance())),
        PUBLIC_HOME_LIST_COMMAND(new PublicHomeListCommand(SpongeHuskHomes.getInstance())),
        WARP_COMMAND(new WarpCommand(SpongeHuskHomes.getInstance())),
        SET_WARP_COMMAND(new SetWarpCommand(SpongeHuskHomes.getInstance())),
        WARP_LIST_COMMAND(new WarpListCommand(SpongeHuskHomes.getInstance())),
        DEL_WARP_COMMAND(new DelWarpCommand(SpongeHuskHomes.getInstance())),
        EDIT_WARP_COMMAND(new EditWarpCommand(SpongeHuskHomes.getInstance())),
        TP_COMMAND(new TpCommand(SpongeHuskHomes.getInstance())),
        TP_HERE_COMMAND(new TpHereCommand(SpongeHuskHomes.getInstance())),
        TPA_COMMAND(new TeleportRequestCommand(SpongeHuskHomes.getInstance(), TeleportRequest.Type.TPA)),
        TPA_HERE_COMMAND(new TeleportRequestCommand(SpongeHuskHomes.getInstance(), TeleportRequest.Type.TPA_HERE)),
        TPACCEPT_COMMAND(new TpRespondCommand(SpongeHuskHomes.getInstance(), true)),
        TPDECLINE_COMMAND(new TpRespondCommand(SpongeHuskHomes.getInstance(), false)),
        RTP_COMMAND(new RtpCommand(SpongeHuskHomes.getInstance())),
        TP_IGNORE_COMMAND(new TpIgnoreCommand(SpongeHuskHomes.getInstance())),
        TP_OFFLINE_COMMAND(new TpOfflineCommand(SpongeHuskHomes.getInstance())),
        TP_ALL_COMMAND(new TpAllCommand(SpongeHuskHomes.getInstance())),
        TPA_ALL_COMMAND(new TpaAllCommand(SpongeHuskHomes.getInstance())),
        SPAWN_COMMAND(new SpawnCommand(SpongeHuskHomes.getInstance())),
        SET_SPAWN_COMMAND(new SetSpawnCommand(SpongeHuskHomes.getInstance())),
        BACK_COMMAND(new BackCommand(SpongeHuskHomes.getInstance())),
        HUSKHOMES_COMMAND(new HuskHomesCommand(SpongeHuskHomes.getInstance()));

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
