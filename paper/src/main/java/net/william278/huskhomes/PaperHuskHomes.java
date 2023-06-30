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

package net.william278.huskhomes;

import net.william278.huskhomes.command.BukkitCommand;
import net.william278.huskhomes.command.Command;
import net.william278.huskhomes.command.DisabledCommand;
import net.william278.huskhomes.command.PaperCommand;
import net.william278.huskhomes.hook.Pl3xMapHook;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class PaperHuskHomes extends BukkitHuskHomes {

    @Override
    public void registerHooks() {
        super.registerHooks();

        if (getMapHook().isEmpty() && isDependencyLoaded("Pl3xMap")) {
            getHooks().add(new Pl3xMapHook(this));
        }
    }

    @NotNull
    @Override
    public List<Command> registerCommands() {
        return Arrays.stream(BukkitCommand.Type.values())
                .map((type) -> {
                    Command command = type.createCommand(this);
                    if (this.getSettings().isCommandDisabled(command)) {
                        command = new DisabledCommand(command.getName(), this);
                    }
                    new PaperCommand(command, this).register();
                    return command;
                })
                .filter((command) -> !(command instanceof DisabledCommand))
                .toList();
    }

}
