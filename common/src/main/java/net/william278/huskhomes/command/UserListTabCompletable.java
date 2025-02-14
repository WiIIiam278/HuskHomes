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
import net.william278.huskhomes.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface UserListTabCompletable extends TabCompletable {

    @Override
    @Nullable
    default List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
        return args.length < 2 ? getUsernameList() : null;
    }

    @NotNull
    default List<String> getUsernameList() {
        return getPlugin().getUserList().stream().map(User::getName).toList();
    }

    @NotNull
    HuskHomes getPlugin();

}
