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

package net.william278.huskhomes.database;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.config.Server;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.SavedPosition;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.SavedUser;
import net.william278.huskhomes.user.User;
import net.william278.huskhomes.util.TransactionResolver;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * An abstract representation of the plugin database, storing home, warp and player data.
 *
 * <p>Implemented by different database platforms - MySQL, SQLite, etc. - as configured by the administrator.
 */
public abstract class Database {

    protected final HuskHomes plugin;

    /**
     * Loads SQL table creation schema statements from a resource file as a string array.
     *
     * @param schemaFileName database script resource file to load from
     * @return Array of string-formatted table creation schema statements
     * @throws IOException if the resource could not be read
     */
    protected final String[] getSchemaStatements(@NotNull String schemaFileName) throws IOException {
        return formatStatementTables(
                new String(Objects.requireNonNull(plugin.getResource(schemaFileName)).readAllBytes(),
                        StandardCharsets.UTF_8))
                .split(";");
    }

    /**
     * Format all table name placeholder strings in an SQL statement.
     *
     * @param sql the SQL statement with unformatted table name placeholders
     * @return the formatted statement, with table placeholders replaced with the correct names
     */
    protected final String formatStatementTables(@NotNull String sql) {
        return sql
                .replaceAll("%positions_table%", plugin.getSettings()
                        .getTableName(Table.POSITION_DATA))
                .replaceAll("%players_table%", plugin.getSettings()
                        .getTableName(Table.PLAYER_DATA))
                .replaceAll("%cooldowns_table%", plugin.getSettings()
                        .getTableName(Table.PLAYER_COOLDOWNS_DATA))
                .replaceAll("%teleports_table%", plugin.getSettings()
                        .getTableName(Table.TELEPORT_DATA))
                .replaceAll("%saved_positions_table%", plugin.getSettings()
                        .getTableName(Table.SAVED_POSITION_DATA))
                .replaceAll("%homes_table%", plugin.getSettings()
                        .getTableName(Table.HOME_DATA))
                .replaceAll("%warps_table%", plugin.getSettings()
                        .getTableName(Table.WARP_DATA));
    }

    /**
     * Create a database instance, pulling table names from the plugin config.
     *
     * @param plugin the implementing plugin instance
     */
    protected Database(@NotNull HuskHomes plugin) {
        this.plugin = plugin;
    }

    /**
     * Initialize the database and ensure tables are present; create tables if they do not exist.
     */
    public abstract void initialize() throws IllegalStateException;

    /**
     * <b>(Internal use only)</b> - Sets a position to the position table in the database.
     *
     * @param position   The {@link Position} to set
     * @param connection SQL connection
     * @return The newly inserted row ID
     * @throws SQLException if an SQL exception occurs doing this
     */
    protected abstract int setPosition(@NotNull Position position, @NotNull Connection connection) throws SQLException;

    /**
     * <b>(Internal use only)</b> - Updates position data.
     *
     * @param positionId ID of the position to update
     * @param position   the Position.at
     * @param connection SQL connection
     * @throws SQLException if an SQL exception occurs doing this
     */
    @ApiStatus.Internal
    protected abstract void updatePosition(int positionId, @NotNull Position position,
                                           @NotNull Connection connection) throws SQLException;

    /**
     * <b>(Internal use only)</b> - Sets a {@link SavedPosition} to the database.
     *
     * @param position   The {@link SavedPosition} to set
     * @param connection SQL connection
     * @return The newly inserted row ID
     * @throws SQLException if an SQL exception occurs doing this
     */
    @ApiStatus.Internal
    protected abstract int setSavedPosition(@NotNull SavedPosition position,
                                            @NotNull Connection connection) throws SQLException;

    /**
     * <b>(Internal use only)</b> - Updates a saved position metadata.
     *
     * @param savedPositionId ID of the metadata to update
     * @param savedPosition   the new saved position
     * @param connection      SQL connection
     * @throws SQLException if an SQL exception occurs doing this
     */
    @ApiStatus.Internal
    protected abstract void updateSavedPosition(int savedPositionId, @NotNull SavedPosition savedPosition,
                                                @NotNull Connection connection) throws SQLException;

    /**
     * Ensure a {@link User} has a {@link SavedUser} entry in the database and that their username is up-to-date.
     *
     * @param user The {@link User} to ensure
     */
    public abstract void ensureUser(@NotNull User user);

    /**
     * Get {@link SavedUser} for a user by their Minecraft username (<i>case-insensitive</i>).
     *
     * @param name Username of the {@link SavedUser} to get (<i>case-insensitive</i>)
     * @return An optional with the {@link SavedUser} present if they exist
     */
    public abstract Optional<SavedUser> getUserDataByName(@NotNull String name);

    /**
     * Get {@link SavedUser} for a user by their Minecraft account {@link UUID}.
     *
     * @param uuid Minecraft account {@link UUID} of the {@link SavedUser} to get
     * @return An optional with the {@link SavedUser} present if they exist
     */
    public abstract Optional<SavedUser> getUserData(@NotNull UUID uuid);

    /**
     * Get the currently active cooldown of a {@link User} for a specific {@link TransactionResolver.Action}.
     *
     * @return An optional with the {@link Instant} the cooldown expires, or empty if the {@link User} has no cooldown
     */
    public abstract Optional<Instant> getCooldown(@NotNull TransactionResolver.Action action, @NotNull User user);

    /**
     * Set the cooldown of a {@link User} for a specific {@link TransactionResolver.Action}.
     *
     * @param action         The {@link TransactionResolver.Action} to set the cooldown for
     * @param user           The {@link User} to set the cooldown for
     * @param cooldownExpiry The {@link Instant} the cooldown expires at
     */
    public abstract void setCooldown(@NotNull TransactionResolver.Action action, @NotNull User user,
                                     @NotNull Instant cooldownExpiry);

    /**
     * Remove the cooldown of a {@link User} for a specific {@link TransactionResolver.Action}.
     *
     * @param action The {@link TransactionResolver.Action} to remove the cooldown for
     * @param user   The {@link User} to remove the cooldown for
     */
    public abstract void removeCooldown(@NotNull TransactionResolver.Action action, @NotNull User user);

    /**
     * Get a list of {@link Home}s set by a {@link User}.
     *
     * @param user {@link User} to get the homes of
     * @return A future returning void when complete
     */
    public abstract List<Home> getHomes(@NotNull User user);

    /**
     * Get a list of all {@link Warp}s that have been set.
     *
     * @return A list containing all {@link Warp}s
     */
    public abstract List<Warp> getWarps();

    /**
     * Get a list of publicly-set {@link Warp}s on <i>this {@link Server server}</i>.
     *
     * @param plugin The plugin instance
     * @return A list containing all {@link Warp}s set on this server
     */
    @NotNull
    public final List<Warp> getLocalWarps(@NotNull HuskHomes plugin) {
        return getWarps().stream()
                .filter(warp -> warp.getServer().equals(plugin.getServerName()))
                .collect(Collectors.toList());
    }

    /**
     * Get a list of all publicly-set {@link Home}s.
     *
     * @return A list containing all publicly-set {@link Home}s
     */
    public abstract List<Home> getPublicHomes();

    /**
     * Get a list of publicly-set {@link Home}s on <i>this {@link Server server}</i>.
     *
     * @param plugin The plugin instance
     * @return A list containing all publicly-set {@link Home}s on this server
     */
    @NotNull
    public final List<Home> getLocalPublicHomes(@NotNull HuskHomes plugin) {
        return getPublicHomes().stream()
                .filter(home -> home.getServer().equals(plugin.getServerName()))
                .collect(Collectors.toList());
    }

    /**
     * Get a {@link Home} with the given name, set by the given {@link User}.
     *
     * @param user     The {@link User} who set the home
     * @param homeName The name of the home to get
     * @return An optional with the {@link Home} present if it exists
     * @apiNote Whether lookup is case-sensitive is determined by the {@code general.case_insensitive_names} setting
     */
    public final Optional<Home> getHome(@NotNull User user, @NotNull String homeName) {
        return this.getHome(user, homeName, plugin.getSettings().caseInsensitiveNames());
    }

    /**
     * Get a {@link Home} with the given name, set by the given {@link User}.
     *
     * @param user            The {@link User} who set the home
     * @param homeName        The name of the home to get
     * @param caseInsensitive Whether the name lookup query should be case-insensitive
     * @return An optional with the {@link Home} present if it exists
     */
    public abstract Optional<Home> getHome(@NotNull User user, @NotNull String homeName, boolean caseInsensitive);

    /**
     * Get a {@link Home} by its {@link UUID unique ID}.
     *
     * @param uuid the {@link UUID} of the home to get
     * @return An optional with the {@link Home} present if it exists
     */
    public abstract Optional<Home> getHome(@NotNull UUID uuid);

    /**
     * Get a {@link Warp} with the given name.
     *
     * @param warpName The name of the warp to get
     * @return An optional with the {@link Warp} present if it exists
     * @apiNote Whether lookup is case-insensitive is determined by the {@code general.case_insensitive_names} setting
     */
    public final Optional<Warp> getWarp(@NotNull String warpName) {
        return getWarp(warpName, plugin.getSettings().caseInsensitiveNames());
    }

    /**
     * Get a {@link Warp} with the given name.
     *
     * @param warpName        The name of the warp to get
     * @param caseInsensitive Whether the search should be case-insensitive
     * @return An optional with the {@link Warp} present if it exists
     */
    public abstract Optional<Warp> getWarp(@NotNull String warpName, boolean caseInsensitive);

    /**
     * Get a {@link Warp} by its {@link UUID unique ID}.
     *
     * @param uuid the {@link UUID} of the warp to get
     * @return An optional with the {@link Warp} present if it exists
     */
    public abstract Optional<Warp> getWarp(@NotNull UUID uuid);

    /**
     * Get the current {@link Teleport} being executed by the specified {@link OnlineUser}.
     *
     * @param onlineUser The {@link OnlineUser} to check
     * @return An optional with the {@link Teleport} present if they are teleporting cross-server
     */
    public abstract Optional<Teleport> getCurrentTeleport(@NotNull OnlineUser onlineUser);

    /**
     * Updates a user in the database with new {@link SavedUser}.
     *
     * @param savedUser The {@link SavedUser} to update
     */
    public abstract void updateUserData(@NotNull SavedUser savedUser);

    /**
     * Sets or clears the current {@link Teleport} being executed by a {@link User}.
     *
     * @param user     The {@link User} to set the current teleport of.
     *                 Pass as {@code null} to clear the player's current teleport
     * @param teleport The {@link Teleport} to set as their current cross-server teleport
     */
    public abstract void setCurrentTeleport(@NotNull User user, @Nullable Teleport teleport);

    /**
     * Clears the current {@link Teleport} being executed by a {@link User}.
     *
     * @param user The {@link User} to clear the current teleport of
     */
    public final void clearCurrentTeleport(@NotNull User user) {
        this.setCurrentTeleport(user, null);
    }

    /**
     * Get the last teleport {@link Position} of a specified {@link User}.
     *
     * @param user The {@link User} to check
     * @return An optional with the {@link Position} present if it has been set
     */
    public abstract Optional<Position> getLastPosition(@NotNull User user);

    /**
     * Sets the last teleport {@link Position} of a {@link User}.
     *
     * @param user     The {@link User} to set the last position of
     * @param position The {@link Position} to set as their last position
     */
    public abstract void setLastPosition(@NotNull User user, @NotNull Position position);

    /**
     * Get the offline {@link Position} of a specified {@link User}.
     *
     * @param user The {@link User} to check
     * @return An optional with the {@link Position} present if it has been set
     */
    public abstract Optional<Position> getOfflinePosition(@NotNull User user);

    /**
     * Sets the offline {@link Position} of a {@link User}.
     *
     * @param user     The {@link User} to set the offline position of
     * @param position The {@link Position} to set as their offline position
     */
    public abstract void setOfflinePosition(@NotNull User user, @NotNull Position position);

    /**
     * Get the respawn {@link Position} of a specified {@link User}.
     *
     * @param user The {@link User} to check
     * @return An optional with the {@link Position} present if it has been set
     */
    public abstract Optional<Position> getRespawnPosition(@NotNull User user);

    /**
     * Sets or clears the respawn {@link Position} of a {@link User}.
     *
     * @param user     The {@link User} to set the respawn position of
     * @param position The {@link Position} to set as their respawn position
     *                 Pass as {@code null} to clear the player's current respawn position.
     */
    public abstract void setRespawnPosition(@NotNull User user, @Nullable Position position);

    /**
     * Sets or updates a {@link Home} into the home data table on the database.
     *
     * @param home The {@link Home} to set - or update - in the database.
     */
    public abstract void saveHome(@NotNull Home home);

    /**
     * Sets or updates a {@link Warp} into the warp data table on the database.
     *
     * @param warp The {@link Warp} to set - or update - in the database.
     */
    public abstract void saveWarp(@NotNull Warp warp);

    /**
     * Deletes a {@link Home} by the given unique id from the home table on the database.
     *
     * @param uuid {@link UUID} of the home to delete
     */
    public abstract void deleteHome(@NotNull UUID uuid);

    /**
     * Deletes all {@link Home}s of a {@link User} from the home table on the database.
     *
     * @param user The {@link User} to delete all homes of
     * @return An integer; the number of deleted homes
     */
    public abstract int deleteAllHomes(@NotNull User user);

    /**
     * Deletes a {@link Warp} by the given unique id from the warp table on the database.
     *
     * @param uuid {@link UUID} of the warp to delete
     */
    public abstract void deleteWarp(@NotNull UUID uuid);

    /**
     * Deletes all {@link Warp}s set on the database table.
     *
     * @return An integer; the number of deleted warps
     */
    public abstract int deleteAllWarps();

    /**
     * Close any remaining connection to the database source.
     */
    public abstract void terminate();


    /**
     * Identifies types of databases.
     */
    public enum Type {
        MYSQL("MySQL"),
        MARIADB("MariaDB"),
        SQLITE("SQLite"),
        H2("H2");

        private final String displayName;

        Type(@NotNull String displayName) {
            this.displayName = displayName;
        }

        @NotNull
        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Represents the names of tables in the database.
     */
    public enum Table {
        PLAYER_DATA("huskhomes_users"),
        PLAYER_COOLDOWNS_DATA("huskhomes_user_cooldowns"),
        POSITION_DATA("huskhomes_position_data"),
        SAVED_POSITION_DATA("huskhomes_saved_positions"),
        HOME_DATA("huskhomes_homes"),
        WARP_DATA("huskhomes_warps"),
        TELEPORT_DATA("huskhomes_teleports");

        private final String defaultName;

        Table(@NotNull String defaultName) {
            this.defaultName = defaultName;
        }

        @NotNull
        public String getDefaultName() {
            return defaultName;
        }

        @NotNull
        public static Map<String, String> getConfigMap() {
            return Arrays.stream(values()).collect(Collectors.toMap(
                    table -> table.name().toLowerCase(Locale.ENGLISH),
                    Table::getDefaultName
            ));
        }

    }
}
