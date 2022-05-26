package net.william278.huskhomes.util;

import net.william278.huskhomes.HuskHomes;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Optional;

/**
 * Utility for matching a username based on an input name
 */
public final class MatcherUtil {

    /**
     * Match a player's username from a user-supplied input name
     *
     * @param inputName the user input name
     * @param plugin    the implementing plugin
     * @return an optional, containing a matched username if one was found
     */
    public static Optional<String> matchPlayerName(@NotNull String inputName, @NotNull HuskHomes plugin) {
        // Return an exact match if there is one present
        final Optional<String> exactPlayer = plugin.getCache().players.stream().filter(username ->
                username.equalsIgnoreCase(inputName)).findFirst();
        if (exactPlayer.isPresent()) {
            return exactPlayer;
        }

        // Return most likely match, if present
        return plugin.getCache().players.stream().filter(username -> username.toLowerCase()
                .startsWith(inputName.toLowerCase())).min(Comparator.comparingInt(username ->
                username.length() - inputName.toLowerCase().length()));
    }

}
