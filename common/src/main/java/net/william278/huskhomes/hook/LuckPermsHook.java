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

package net.william278.huskhomes.hook;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

@PluginHook(
        name = "LuckPerms",
        register = PluginHook.Register.ON_ENABLE
)
public class LuckPermsHook extends Hook {

    private LuckPerms api;

    public LuckPermsHook(@NotNull HuskHomes plugin) {
        super(plugin);
    }

    @Override
    public void load() {
        this.api = LuckPermsProvider.get();
    }

    @Override
    public void unload() {
        this.api = null;
    }

    @NotNull
    public List<Integer> getNumericalPermissions(@NotNull OnlineUser online, @NotNull String nodePrefix) {
        final User user = api.getUserManager().getUser(online.getUuid());
        if (user == null) {
            return List.of();
        }
        return user.getNodes().stream().filter(Node::getValue).filter(n -> n.getKey().startsWith(nodePrefix))
                .filter(perm -> canParse(perm, nodePrefix))
                .map(perm -> Integer.parseInt(perm.getKey().substring(nodePrefix.length())))
                .sorted(Collections.reverseOrder()).toList();

    }

    // Remove node prefix from the permission and parse as an integer
    private static boolean canParse(@NotNull Node permission, @NotNull String nodePrefix) {
        try {
            Integer.parseInt(permission.getKey().substring(nodePrefix.length()));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
