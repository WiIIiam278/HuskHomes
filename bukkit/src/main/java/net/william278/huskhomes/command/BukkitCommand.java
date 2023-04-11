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
import net.william278.huskhomes.BukkitHuskHomes;
import net.william278.huskhomes.teleport.TeleportRequest;
import net.william278.huskhomes.user.BukkitUser;
import net.william278.huskhomes.user.CommandUser;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BukkitCommand implements CommandExecutor, TabCompleter {

    private final BukkitHuskHomes plugin;
    private final Command command;

    public BukkitCommand(@NotNull Command command, @NotNull BukkitHuskHomes plugin) {
        this.command = command;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command,
                             @NotNull String label, @NotNull String[] args) {
        this.command.onExecuted(sender instanceof Player player ? BukkitUser.adapt(player) : plugin.getConsole(), args);
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (!(this.command instanceof TabProvider provider)) {
            return List.of();
        }
        final CommandUser user = sender instanceof Player player ? BukkitUser.adapt(player) : plugin.getConsole();
        return provider.getSuggestions(user, args);
    }

    public void register() {
        // Register with bukkit
        final PluginCommand pluginCommand = plugin.getCommand(command.getName());
        if (pluginCommand == null) {
            throw new IllegalStateException("Command " + command.getName() + " not found in plugin.yml");
        }

        // Register permissions
        addPermission(plugin, command.getPermission(), command.getUsage(), getPermissionDefault(command.isOperatorCommand()));
        final List<Permission> childNodes = command.getAdditionalPermissions()
                .entrySet().stream()
                .map((entry) -> addPermission(plugin, entry.getKey(), "", getPermissionDefault(entry.getValue())))
                .filter(Objects::nonNull)
                .toList();
        if (!childNodes.isEmpty()) {
            addPermission(plugin, command.getPermission("*"), command.getUsage(), PermissionDefault.FALSE,
                    childNodes.toArray(new Permission[0]));
        }

        // Set command parameters
        pluginCommand.setExecutor(this);
        pluginCommand.setTabCompleter(this);
        pluginCommand.setDescription(command.getDescription());
        pluginCommand.setPermission(command.getPermission());

        // Register commodore TAB completion
        if (CommodoreProvider.isSupported() && plugin.getSettings().doBrigadierTabCompletion()) {
            BrigadierUtil.registerCommodore(plugin, pluginCommand, command);
        }
    }

    @Nullable
    protected static Permission addPermission(@NotNull BukkitHuskHomes plugin, @NotNull String node, @NotNull String description,
                                              @NotNull PermissionDefault permissionDefault, @NotNull Permission... children) {
        final Map<String, Boolean> childNodes = Arrays.stream(children)
                .map(Permission::getName)
                .collect(HashMap::new, (map, child) -> map.put(child, true), HashMap::putAll);

        final PluginManager manager = plugin.getServer().getPluginManager();
        if (manager.getPermission(node) != null) {
            return null;
        }

        Permission permission;
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

    /**
     * Commands available on the Bukkit HuskHomes implementation
     */
    public enum Type {
        HOME_COMMAND(new PrivateHomeCommand(BukkitHuskHomes.getInstance())),
        SET_HOME_COMMAND(new SetHomeCommand(BukkitHuskHomes.getInstance())),
        HOME_LIST_COMMAND(new PrivateHomeListCommand(BukkitHuskHomes.getInstance())),
        DEL_HOME_COMMAND(new DelHomeCommand(BukkitHuskHomes.getInstance())),
        EDIT_HOME_COMMAND(new EditHomeCommand(BukkitHuskHomes.getInstance())),
        PUBLIC_HOME_COMMAND(new PublicHomeCommand(BukkitHuskHomes.getInstance())),
        PUBLIC_HOME_LIST_COMMAND(new PublicHomeListCommand(BukkitHuskHomes.getInstance())),
        WARP_COMMAND(new WarpCommand(BukkitHuskHomes.getInstance())),
        SET_WARP_COMMAND(new SetWarpCommand(BukkitHuskHomes.getInstance())),
        WARP_LIST_COMMAND(new WarpListCommand(BukkitHuskHomes.getInstance())),
        DEL_WARP_COMMAND(new DelWarpCommand(BukkitHuskHomes.getInstance())),
        EDIT_WARP_COMMAND(new EditWarpCommand(BukkitHuskHomes.getInstance())),
        TP_COMMAND(new TpCommand(BukkitHuskHomes.getInstance())),
        TP_HERE_COMMAND(new TpHereCommand(BukkitHuskHomes.getInstance())),
        TPA_COMMAND(new TeleportRequestCommand(BukkitHuskHomes.getInstance(), TeleportRequest.Type.TPA)),
        TPA_HERE_COMMAND(new TeleportRequestCommand(BukkitHuskHomes.getInstance(), TeleportRequest.Type.TPA_HERE)),
        TPACCEPT_COMMAND(new TpRespondCommand(BukkitHuskHomes.getInstance(), true)),
        TPDECLINE_COMMAND(new TpRespondCommand(BukkitHuskHomes.getInstance(), false)),
        RTP_COMMAND(new RtpCommand(BukkitHuskHomes.getInstance())),
        TP_IGNORE_COMMAND(new TpIgnoreCommand(BukkitHuskHomes.getInstance())),
        TP_OFFLINE_COMMAND(new TpOfflineCommand(BukkitHuskHomes.getInstance())),
        TP_ALL_COMMAND(new TpAllCommand(BukkitHuskHomes.getInstance())),
        TPA_ALL_COMMAND(new TpaAllCommand(BukkitHuskHomes.getInstance())),
        SPAWN_COMMAND(new SpawnCommand(BukkitHuskHomes.getInstance())),
        SET_SPAWN_COMMAND(new SetSpawnCommand(BukkitHuskHomes.getInstance())),
        BACK_COMMAND(new BackCommand(BukkitHuskHomes.getInstance())),
        HUSKHOMES_COMMAND(new HuskHomesCommand(BukkitHuskHomes.getInstance()));

        private final Command command;

        Type(@NotNull Command command) {
            this.command = command;
        }

        @NotNull
        public Command getCommand() {
            return command;
        }
    }
}