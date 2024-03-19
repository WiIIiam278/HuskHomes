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
import net.william278.huskhomes.user.CommandUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

import static net.william278.huskhomes.position.Home.IDENTIFIER_DELIMITER;

public class PublicHomeCommand extends HomeCommand implements TabProvider {

    protected PublicHomeCommand(@NotNull HuskHomes plugin) {
        super("phome", List.of("publichome"), plugin);
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
        // Display the public home list if no arguments are provided
        if (args.length == 0) {
            plugin.getCommand(PublicHomeListCommand.class)
                    .ifPresent(command -> command.showPublicHomeList(executor, 1));
            return;
        }
        if(plugin.getSettings().getGeneral().getNames().isReversePHomeNaming()) {
            final Optional<String> name = parseStringArg(args, 0);
            if (name.isPresent()) {
                if (name.get().contains(IDENTIFIER_DELIMITER)) {
                    String home = name.get().substring(0, name.get().indexOf(IDENTIFIER_DELIMITER));
                    String owner = name.get().substring(name.get().indexOf(IDENTIFIER_DELIMITER) + 1);
                    args[0] = owner + IDENTIFIER_DELIMITER + home;
                }
            }
        }
        super.execute(executor, args);
    }

    @Override
    @NotNull
    public List<String> suggest(@NotNull CommandUser executor, @NotNull String[] args) {
        if (args.length <= 2) {
            if(!plugin.getSettings().getGeneral().getNames().isReversePHomeNaming()){
                return filter(plugin.getManager().homes().getPublicHomeIdentifiers(), args);
            }
            else{
                return filter(plugin.getManager().homes().getPublicHomeIdentifiersReverse(), args);
            }
        }
        return List.of();
    }

}