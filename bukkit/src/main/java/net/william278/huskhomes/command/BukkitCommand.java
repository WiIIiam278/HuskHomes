package net.william278.huskhomes.command;

import me.lucko.commodore.CommodoreProvider;
import net.william278.huskhomes.BukkitHuskHomes;
import net.william278.huskhomes.user.BukkitUser;
import org.bukkit.command.*;
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
        this.command.execute(sender instanceof Player player ? BukkitUser.adapt(player) : plugin.getConsole(), args);
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        return this.command.suggest(sender instanceof Player player ? BukkitUser.adapt(player) : plugin.getConsole(), args);
    }

    public void register() {
        // Register with bukkit
        final PluginCommand pluginCommand = Objects.requireNonNull(plugin.getCommand(command.getName()));
        pluginCommand.setExecutor(this);
        pluginCommand.setTabCompleter(this);

        // Register commodore TAB completion
        if (CommodoreProvider.isSupported() && plugin.getSettings().doBrigadierTabCompletion()) {
            BrigadierUtil.registerCommodore(plugin, pluginCommand, command);
        }

        // Register permissions
        final PluginManager manager = plugin.getServer().getPluginManager();
        command.getChildren()
                .stream().map(child -> new Permission(child.getPermission(), child.getUsage(),
                        child.isOperatorCommand() ? PermissionDefault.OP : PermissionDefault.TRUE))
                .forEach(manager::addPermission);
        manager.addPermission(new Permission(command.getPermission(), "/" + command.getName(),
                command.isOperatorCommand() ? PermissionDefault.OP : PermissionDefault.TRUE));

        // Register master permission
        final Map<String, Boolean> childNodes = new HashMap<>();
        command.getChildren().forEach(child -> childNodes.put(child.getPermission(), true));
        manager.addPermission(new Permission(command.getPermission() + ".*", command.getUsage(),
                PermissionDefault.FALSE, childNodes));
    }

    /**
     * Commands available on the Bukkit HuskHomes implementation
     */
    public enum Type {
        HOME_COMMAND(new HomeCommand(BukkitHuskHomes.getInstance())),
        SET_HOME_COMMAND(new SetHomeCommand(BukkitHuskHomes.getInstance())),
        HOME_LIST_COMMAND(new HomeListCommand(BukkitHuskHomes.getInstance())),
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
        TPA_COMMAND(new TpaCommand(BukkitHuskHomes.getInstance())),
        TPA_HERE_COMMAND(new TpaHereCommand(BukkitHuskHomes.getInstance())),
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