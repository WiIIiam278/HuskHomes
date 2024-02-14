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
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.teleport.TeleportRequest;
import net.william278.huskhomes.user.BukkitUser;
import net.william278.huskhomes.user.CommandUser;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class BukkitCommand extends org.bukkit.command.Command {

    private final BukkitHuskHomes plugin;
    private final Command command;

    public BukkitCommand(@NotNull Command command, @NotNull BukkitHuskHomes plugin) {
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
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias,
                                    @NotNull String[] args) throws IllegalArgumentException {
        if (!(this.command instanceof TabProvider provider)) {
            return List.of();
        }
        final CommandUser user = sender instanceof Player p ? BukkitUser.adapt(p, plugin) : plugin.getConsole();
        return provider.getSuggestions(user, args);
    }

    public void register() {
        // Register with bukkit
        plugin.getCommandRegistrar().getServerCommandMap().register("huskhomes", this);

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
        if (CommodoreProvider.isSupported() && plugin.getSettings().getGeneral().isBrigadierTabCompletion()) {
            BrigadierUtil.registerCommodore(plugin, this, command);
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
     * Commands available on the Bukkit HuskHomes implementation.
     */
    public enum Type {
        HOME_COMMAND(PrivateHomeCommand::new),
        SET_HOME_COMMAND(SetHomeCommand::new),
        HOME_LIST_COMMAND(PrivateHomeListCommand::new),
        DEL_HOME_COMMAND(DelHomeCommand::new),
        EDIT_HOME_COMMAND(EditHomeCommand::new),
        PUBLIC_HOME_COMMAND(PublicHomeCommand::new),
        PUBLIC_HOME_LIST_COMMAND(PublicHomeListCommand::new),
        WARP_COMMAND(WarpCommand::new),
        SET_WARP_COMMAND(SetWarpCommand::new),
        WARP_LIST_COMMAND(WarpListCommand::new),
        DEL_WARP_COMMAND(DelWarpCommand::new),
        EDIT_WARP_COMMAND(EditWarpCommand::new),
        TP_COMMAND(TpCommand::new),
        TP_HERE_COMMAND(TpHereCommand::new),
        TPA_COMMAND((plugin) -> new TeleportRequestCommand(plugin, TeleportRequest.Type.TPA)),
        TPA_HERE_COMMAND((plugin) -> new TeleportRequestCommand(plugin, TeleportRequest.Type.TPA_HERE)),
        TPACCEPT_COMMAND((plugin) -> new TpRespondCommand(plugin, true)),
        TPDECLINE_COMMAND((plugin) -> new TpRespondCommand(plugin, false)),
        RTP_COMMAND(RtpCommand::new),
        TP_IGNORE_COMMAND(TpIgnoreCommand::new),
        TP_OFFLINE_COMMAND(TpOfflineCommand::new),
        TP_ALL_COMMAND(TpAllCommand::new),
        TPA_ALL_COMMAND(TpaAllCommand::new),
        TP_PARALLEL(TpParallelCommand::new),
        SPAWN_COMMAND(SpawnCommand::new),
        SET_SPAWN_COMMAND(SetSpawnCommand::new),
        BACK_COMMAND(BackCommand::new),
        HUSKHOMES_COMMAND(HuskHomesCommand::new);

        private final Function<HuskHomes, Command> supplier;

        Type(@NotNull Function<HuskHomes, Command> supplier) {
            this.supplier = supplier;
        }

        @NotNull
        public Command createCommand(@NotNull HuskHomes plugin) {
            return supplier.apply(plugin);
        }

        @NotNull
        public static List<Command> getCommands(@NotNull BukkitHuskHomes plugin) {
            return Arrays.stream(values())
                    .map((type) -> type.createCommand(plugin))
                    .filter((command) -> !plugin.getSettings().isCommandDisabled(command))
                    .peek((command) -> new BukkitCommand(command, plugin).register())
                    .toList();
        }

    }

}