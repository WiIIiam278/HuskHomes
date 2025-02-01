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

import net.william278.huskhomes.BukkitHuskHomes;
import net.william278.huskhomes.user.CommandUser;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BukkitCommand extends org.bukkit.command.Command {

    private final BukkitHuskHomes plugin;
    private final Command command;

    public BukkitCommand(@NotNull Command command, @NotNull BukkitHuskHomes plugin) {
        super(command.getName(), command.getDescription(), command.getUsage(), command.getAliases());
        this.setPermission(command.getPermission());

        this.command = command;
        this.plugin = plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        this.command.onExecuted(sender instanceof Player p ? plugin.getOnlineUser(p) : plugin.getConsole(), args);
        return true;
    }

    @NotNull
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias,
                                    @NotNull String[] args) throws IllegalArgumentException {
        if (!(this.command instanceof TabCompletable completable)) {
            return List.of();
        }
        final CommandUser user = sender instanceof Player p ? plugin.getOnlineUser(p) : plugin.getConsole();
        return completable.getSuggestions(user, args);
    }

    public void register() {
        // Register with bukkit
        plugin.getMorePaperLib().commandRegistration().getServerCommandMap()
                .register("huskhomes", this);

        // Register permissions
        BukkitCommand.addPermission(
                plugin,
                command.getPermission(),
                command.getUsage(),
                BukkitCommand.getPermissionDefault(command.isOperatorCommand())
        );
        this.setPermission(command.getPermission());
        final List<Permission> childNodes = command.getAdditionalPermissions()
                .entrySet().stream()
                .map((entry) -> BukkitCommand.addPermission(
                        plugin,
                        entry.getKey(),
                        "",
                        BukkitCommand.getPermissionDefault(entry.getValue()))
                )
                .filter(Objects::nonNull)
                .toList();
        if (!childNodes.isEmpty()) {
            BukkitCommand.addPermission(
                    plugin,
                    command.getPermission("*"),
                    command.getUsage(),
                    PermissionDefault.FALSE,
                    childNodes.toArray(new Permission[0])
            );
        }
    }

    @Nullable
    protected static Permission addPermission(@NotNull BukkitHuskHomes plugin, @NotNull String node,
                                              @NotNull String description, @NotNull PermissionDefault permissionDefault,
                                              @NotNull Permission... children) {
        final Map<String, Boolean> childNodes = Arrays.stream(children)
                .map(Permission::getName)
                .collect(HashMap::new, (map, child) -> map.put(child, true), HashMap::putAll);

        final PluginManager manager = plugin.getServer().getPluginManager();
        if (manager.getPermission(node) != null) {
            return null;
        }

        final Permission permission;
        if (description.isEmpty()) {
            permission = new Permission(node, permissionDefault, childNodes);
        } else {
            permission = new Permission(node, description, permissionDefault, childNodes);
        }
        manager.addPermission(permission);

        return permission;
    }

    @NotNull
    protected static PermissionDefault getPermissionDefault(boolean isOperatorCommand) {
        return isOperatorCommand ? PermissionDefault.OP : PermissionDefault.TRUE;
    }

}