package net.william278.huskhomes.hook;

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.BukkitUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.SavedUser;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlaceholderAPIHook extends Hook {

    public PlaceholderAPIHook(@NotNull HuskHomes plugin) {
        super(plugin, "PlaceholderAPI");
    }

    @Override
    public void initialize() {
        new HuskHomesExpansion(plugin).register();
    }

    public static class HuskHomesExpansion extends PlaceholderExpansion {

        @NotNull
        private final HuskHomes plugin;

        private HuskHomesExpansion(@NotNull HuskHomes plugin) {
            this.plugin = plugin;
        }

        @Override
        @Nullable
        public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
            if (offlinePlayer == null || !offlinePlayer.isOnline() || offlinePlayer.getPlayer() == null) {
                return "Player not online";
            }

            // Return the requested data
            final OnlineUser player = BukkitUser.adapt(offlinePlayer.getPlayer());
            return switch (params) {
                case "homes_count" -> String.valueOf(plugin.getManager().homes()
                        .getUserHomes()
                        .getOrDefault(player.getUsername(), List.of()).size());
                case "max_homes" -> String.valueOf(player.getMaxHomes(
                        plugin.getSettings().getMaxHomes(),
                        plugin.getSettings().doStackPermissionLimits()
                ));
                case "max_public_homes" -> String.valueOf(player.getMaxPublicHomes(
                        plugin.getSettings().getMaxPublicHomes(),
                        plugin.getSettings().doStackPermissionLimits()
                ));
                case "free_home_slots" -> String.valueOf(player.getFreeHomes(
                        plugin.getSettings().getFreeHomeSlots(),
                        plugin.getSettings().doStackPermissionLimits()
                ));
                case "home_slots" -> String.valueOf(plugin.getSavedUser(player)
                        .map(SavedUser::getHomeSlots)
                        .orElse(0));
                case "homes_list" -> String.join(", ", plugin.getManager().homes()
                        .getUserHomes()
                        .getOrDefault(player.getUsername(), List.of()));
                case "public_homes_count" -> String.valueOf(plugin.getManager().homes()
                        .getPublicHomes()
                        .getOrDefault(player.getUsername(), List.of()).size());
                case "public_homes_list" -> String.join(", ", plugin.getManager().homes()
                        .getPublicHomes()
                        .getOrDefault(player.getUsername(), List.of()));
                case "ignoring_tp_requests" -> getBooleanValue(plugin.getManager().requests()
                        .isIgnoringRequests(player));
                default -> null;
            };
        }

        @Override
        public boolean persist() {
            return true;
        }

        @Override
        @NotNull
        public String getIdentifier() {
            return "huskhomes";
        }

        @Override
        @NotNull
        public String getAuthor() {
            return "William278";
        }

        @Override
        @NotNull
        public String getVersion() {
            return plugin.getVersion().toStringWithoutMetadata();
        }

        @NotNull
        private String getBooleanValue(final boolean bool) {
            return bool ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
        }

    }

}
