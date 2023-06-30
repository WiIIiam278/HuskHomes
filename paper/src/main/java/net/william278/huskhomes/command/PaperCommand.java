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

import me.lucko.commodore.CommodoreProvider;
import net.william278.huskhomes.PaperHuskHomes;
import net.william278.huskhomes.user.BukkitUser;
import net.william278.huskhomes.user.CommandUser;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class PaperCommand extends org.bukkit.command.Command {

    private final PaperHuskHomes plugin;
    private final Command command;

    public PaperCommand(@NotNull Command command, @NotNull PaperHuskHomes plugin) {
        super(command.getName(), command.getDescription(), command.getUsage(), command.getAliases());
        this.command = command;
        this.plugin = plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        this.command.onExecuted(sender instanceof Player p ? BukkitUser.adapt(p, plugin) : plugin.getConsole(), args);
        return true;
    }

    @NotNull
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if (!(this.command instanceof TabProvider provider)) {
            return List.of();
        }
        final CommandUser user = sender instanceof Player p ? BukkitUser.adapt(p, plugin) : plugin.getConsole();
        return provider.getSuggestions(user, args);
    }

    public void register() {
        // Register with bukkit
        plugin.getServer().getCommandMap().register("huskhomes", this);

        // Register permissions
        BukkitCommand.addPermission(
                plugin,
                command.getPermission(),
                command.getUsage(),
                BukkitCommand.getPermissionDefault(command.isOperatorCommand())
        );
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

        // Register commodore TAB completion
        if (CommodoreProvider.isSupported() && plugin.getSettings().doBrigadierTabCompletion()) {
            BrigadierUtil.registerCommodore(plugin, this, command);
        }
    }

}
