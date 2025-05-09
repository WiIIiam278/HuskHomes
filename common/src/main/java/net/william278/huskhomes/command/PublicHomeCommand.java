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
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.user.CommandUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PublicHomeCommand extends HomeCommand implements TabCompletable {

    protected PublicHomeCommand(@NotNull HuskHomes plugin) {
        super(
                List.of("phome", "publichome"),
                PositionCommandType.PUBLIC_HOME,
                plugin
        );
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
        // Display the public home list if no arguments are provided
        if (args.length == 0) {
            plugin.getCommand(PublicHomeListCommand.class).ifPresent(cmd -> cmd
                    .showPublicHomeList(executor, null, 1));
            return;
        }
        super.execute(executor, args);
    }

    @Override
    @NotNull
    public List<String> suggest(@NotNull CommandUser executor, @NotNull String[] args) {
        if (args.length <= 2) {
            if (args.length >= 1 && args[0].contains(Home.getDelimiter())) {
                return plugin.getManager().homes().getPublicHomeIdentifierNames();
            }
            return plugin.getManager().homes().getPublicHomeNames();
        }
        return List.of();
    }

}