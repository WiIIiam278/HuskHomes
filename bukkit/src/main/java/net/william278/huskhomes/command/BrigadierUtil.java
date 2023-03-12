package net.william278.huskhomes.command;

import me.lucko.commodore.CommodoreProvider;
import me.lucko.commodore.file.CommodoreFileReader;
import net.william278.huskhomes.BukkitHuskHomes;
import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

public class BrigadierUtil {

    /**
     * Uses commodore to register command completions
     *
     * @param plugin        instance of the registering Bukkit plugin
     * @param pluginCommand the Bukkit PluginCommand to register completions for
     * @param command       the {@link Command} to register completions for
     */
    protected static void registerCommodore(@NotNull BukkitHuskHomes plugin, @NotNull PluginCommand pluginCommand,
                                            @NotNull Command command) {
        final InputStream commandCommodore = plugin.getResource("commodore/" + pluginCommand.getName() + ".commodore");
        if (commandCommodore == null) {
            return;
        }
        try {
            CommodoreProvider.getCommodore(plugin).register(pluginCommand,
                    CommodoreFileReader.INSTANCE.parse(commandCommodore),
                    player -> player.hasPermission(command.getPermission()));
        } catch (IOException e) {
            plugin.log(Level.SEVERE, "Failed to read command commodore completions for "
                                                         + pluginCommand.getName(), e);
        }
    }

}
