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

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.TeleportBuilder;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TpParallelCommand extends Command implements TabProvider {

    protected TpParallelCommand(@NotNull HuskHomes plugin) {
        super("ptp", List.of("paralleltp"), "<server> [player]", plugin);
        setOperatorCommand(true);
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
        switch (args.length) {
            case 1 -> {
                if (!(executor instanceof OnlineUser user)) {
                    plugin.getLocales().getLocale("error_in_game_only")
                            .ifPresent(executor::sendMessage);
                    return;
                }

                this.execute(executor, user, args[0], args);
            }
            case 2 -> plugin.getOnlineUser(args[1]).ifPresentOrElse(teleportable ->
                            this.execute(executor, teleportable, args[0], args),
                    () -> plugin.getLocales().getLocale("error_player_not_found", args[1])
                            .ifPresent(executor::sendMessage));
        }
    }

    // Execute a teleport
    private void execute(@NotNull CommandUser executor, @NotNull OnlineUser teleportedPlayer, @NotNull String serverName,
                         @NotNull String[] args) {

        final Position position = teleportedPlayer.getPosition();
        position.setServer(serverName);

        // Build and execute the teleport
        final TeleportBuilder builder = Teleport.builder(plugin)
                .teleporter(teleportedPlayer)
                .target(position);
        if (executor instanceof OnlineUser user) {
            builder.executor(user);
        }
        builder.buildAndComplete(false, args);

        // Display a teleport completion message
        plugin.getLocales().getLocale("teleporting_other_complete_position", teleportedPlayer.getUsername(),
                        Integer.toString((int) position.getX()), Integer.toString((int) position.getY()),
                        Integer.toString((int) position.getZ()))
                .ifPresent(executor::sendMessage);
    }

    @Override
    @NotNull
    public final List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
        if (args.length == 2) {
            return plugin.getPlayerList(false);
        }
        return List.of();
    }

}
