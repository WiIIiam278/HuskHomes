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

import com.google.common.collect.Lists;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.teleport.TeleportRequest;
import net.william278.huskhomes.user.CommandUser;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public interface CommandProvider {


    /**
     * Get the list of registered commands
     *
     * @return the list of commands
     * @since 4.8
     */
    @NotNull
    List<Command> getCommands();

    /**
     * Get a command by its class
     *
     * @param commandClass the class of the command to get
     * @param <T>          the type of command
     * @return the command, or null if not found
     */
    default <T extends Command> Optional<T> getCommand(@NotNull Class<T> commandClass) {
        return getCommands().stream()
                .filter(commandClass::isInstance)
                .map(commandClass::cast)
                .findFirst();
    }

    /**
     * Returns whether a user can use a command
     *
     * @param commandClass the class of the command to check
     * @param user         the user to check
     * @param nodes        the nodes to check
     * @param <T>          the type of command
     * @return whether the user can use the command
     */
    default <T extends Command> boolean canUseCommand(@NotNull Class<T> commandClass, @NotNull CommandUser user,
                                                      @NotNull String... nodes) {
        return getCommand(commandClass).map(command -> command.hasPermission(user, nodes)).orElse(false);
    }

    /**
     * Register a batch of command with the platform implementation
     *
     * @param commands the list of commands to register
     * @since 4.8
     */
    void registerCommands(@NotNull List<Command> commands);

    /**
     * Registers all plugin commands
     *
     * @since 4.8
     */
    default void loadCommands() {
        final List<Command> commands = Lists.newArrayList();

        // Instantiate commands
        commands.add(new PrivateHomeCommand(getPlugin()));
        commands.add(new SetHomeCommand(getPlugin()));
        commands.add(new HomeListCommand(getPlugin()));
        commands.add(new DelHomeCommand(getPlugin()));
        commands.add(new EditHomeCommand(getPlugin()));
        commands.add(new PublicHomeCommand(getPlugin()));
        commands.add(new PublicHomeListCommand(getPlugin()));
        commands.add(new WarpCommand(getPlugin()));
        commands.add(new SetWarpCommand(getPlugin()));
        commands.add(new WarpListCommand(getPlugin()));
        commands.add(new DelWarpCommand(getPlugin()));
        commands.add(new EditWarpCommand(getPlugin()));
        commands.add(new TpCommand(getPlugin()));
        commands.add(new TpHereCommand(getPlugin()));
        commands.add(new TpRequestCommand(getPlugin(), TeleportRequest.Type.TPA));
        commands.add(new TpRequestCommand(getPlugin(), TeleportRequest.Type.TPA_HERE));
        commands.add(new TpRespondCommand(getPlugin(), true));
        commands.add(new TpRespondCommand(getPlugin(), false));
        commands.add(new TpaAllCommand(getPlugin()));
        commands.add(new RtpCommand(getPlugin()));
        commands.add(new TpIgnoreCommand(getPlugin()));
        commands.add(new TpOfflineCommand(getPlugin()));
        commands.add(new TpAllCommand(getPlugin()));
        commands.add(new SpawnCommand(getPlugin()));
        commands.add(new SetSpawnCommand(getPlugin()));
        commands.add(new BackCommand(getPlugin()));
        commands.add(new HuskHomesCommand(getPlugin()));
        commands.add(new SaveBackLocationCommand(getPlugin()));

        // Filter, sort, and register
        registerCommands(commands.stream()
                .filter((command) -> !getPlugin().getSettings().isCommandDisabled(command))
                .sorted(Comparator.comparing(Command::getName))
                .toList());
    }

    @NotNull
    HuskHomes getPlugin();

}
