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
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class SaveBackLocationCommand extends Command {

    protected SaveBackLocationCommand(@NotNull HuskHomes plugin) {
        super(List.of("savebacklocation"), "<player> <x> <y> <z> <pitch> <yaw> <world> <server>", plugin);
        setOperatorCommand(true); // Only operators can use it
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
        if (args.length != 8) {
            plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(executor::sendMessage);
            return;
        }

        final String playerName = args[0];

        // Parse numerical arguments safely
        try {
            double x = Double.parseDouble(args[1]);
            double y = Double.parseDouble(args[2]);
            double z = Double.parseDouble(args[3]);
            float pitch = Float.parseFloat(args[4]);
            float yaw = Float.parseFloat(args[5]);
            String worldName = args[6];
            String serverName = args[7];

            // Find user
            Optional<User> userOpt = plugin.getUserList().stream()
                    .filter(u -> u.getName().equalsIgnoreCase(playerName))
                    .findFirst();
            if (userOpt.isEmpty()) {
                plugin.getLocales().getLocale("error_player_not_found", playerName)
                        .ifPresent(executor::sendMessage);
                return;
            }

            User user = userOpt.get();
            Position position = Position.at(x, y, z, yaw, pitch,
                    World.from(worldName),
                    serverName
            );

            plugin.getDatabase().setLastPosition(user, position);

            plugin.getLocales().getLocale("teleporting_back_location_saved")
                    .ifPresent(executor::sendMessage);

        } catch (NumberFormatException e) {
            plugin.getLocales().getLocale("error_invalid_syntax")
                    .ifPresent(executor::sendMessage);
        }
    }
}
