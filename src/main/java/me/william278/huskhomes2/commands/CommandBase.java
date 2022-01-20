package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.util.MessageManager;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class CommandBase implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player) {
            onCommand((Player) sender, command, label, args);
            return true;
        }
        return false;
    }

    protected abstract void onCommand(Player player, Command command, String label, String[] args);

    /**
     * Register base for bukkit command
     *
     * @param command Command for registration
     */
    public PluginCommand register(PluginCommand command) {
        Objects.requireNonNull(command);

        // Set command executor
        command.setExecutor(this);

        // Set the appropriate tab completer
        if (this instanceof TabCompleter) {
            command.setTabCompleter((TabCompleter) this);
        }

        // Get localised command description
        command.setDescription(MessageManager.getRawMessage(command.getName() + "_command_description"));
        return command;
    }

    public static class EmptyTab implements TabCompleter {
        @Override
        public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] strings) {
            return Collections.emptyList();
        }
    }
}
