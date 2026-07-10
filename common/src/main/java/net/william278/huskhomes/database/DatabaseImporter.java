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
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.SavedUser;
import net.william278.huskhomes.user.User;
import net.william278.huskhomes.util.TransactionResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * Utility class for importing data between different database types.
 */
public class DatabaseImporter {

    private final HuskHomes plugin;
    private final Database sourceDatabase;
    private final Database targetDatabase;
    private final CommandUser executor;

    public DatabaseImporter(@NotNull HuskHomes plugin, @NotNull Database sourceDatabase, @NotNull Database targetDatabase) {
        this.plugin = plugin;
        this.sourceDatabase = sourceDatabase;
        this.targetDatabase = targetDatabase;
        this.executor = null;
    }

    public DatabaseImporter(@NotNull HuskHomes plugin, @NotNull Database sourceDatabase, @NotNull Database targetDatabase, @NotNull CommandUser executor) {
        this.plugin = plugin;
        this.sourceDatabase = sourceDatabase;
        this.targetDatabase = targetDatabase;
        this.executor = executor;
    }

    /**
     * Import all data from the source database to the target database.
     *
     * @return A CompletableFuture that completes when the import is finished
     */
    public CompletableFuture<ImportResult> importAllData() {
        return CompletableFuture.supplyAsync(() -> {
            plugin.log(Level.INFO, "Starting database import from " +
                sourceDatabase.getClass().getSimpleName() + " to " +
                targetDatabase.getClass().getSimpleName());

            ImportResult result = new ImportResult();

            try {
                // Discover all users first
                sendProgressMessage("database_import_discovering_users");
                Set<User> allUsers = discoverAllUsers();
                plugin.log(Level.INFO, "Discovered " + allUsers.size() + " users in source database");
                sendProgressMessage("database_import_discovered_users", Integer.toString(allUsers.size()));

                // Import users first (required for homes)
                sendProgressMessage("database_import_importing_users");
                result.usersImported = importUsers(allUsers);
                plugin.log(Level.INFO, "Imported " + result.usersImported + " users");
                sendProgressMessage("database_import_imported_users", Integer.toString(result.usersImported));

                // Import warps
                sendProgressMessage("database_import_importing_warps");
                result.warpsImported = importWarps();
                plugin.log(Level.INFO, "Imported " + result.warpsImported + " warps");
                sendProgressMessage("database_import_imported_warps", Integer.toString(result.warpsImported));

                // Import homes
                sendProgressMessage("database_import_importing_homes");
                result.homesImported = importHomes(allUsers);
                plugin.log(Level.INFO, "Imported " + result.homesImported + " homes");
                sendProgressMessage("database_import_imported_homes", Integer.toString(result.homesImported));

                // Import user positions
                sendProgressMessage("database_import_importing_positions");
                result.positionsImported = importUserPositions(allUsers);
                plugin.log(Level.INFO, "Imported " + result.positionsImported + " user positions");
                sendProgressMessage("database_import_imported_positions", Integer.toString(result.positionsImported));

                // Import cooldowns
                sendProgressMessage("database_import_importing_cooldowns");
                result.cooldownsImported = importCooldowns(allUsers);
                plugin.log(Level.INFO, "Imported " + result.cooldownsImported + " cooldowns");
                sendProgressMessage("database_import_imported_cooldowns", Integer.toString(result.cooldownsImported));

                result.success = true;
                plugin.log(Level.INFO, "Database import completed successfully!");

            } catch (Exception e) {
                plugin.log(Level.SEVERE, "Database import failed", e);
                result.success = false;
                result.errorMessage = e.getMessage();
            }

            return result;
        });
    }

    /**
     * Sends a progress message to the executor if available
     */
    private void sendProgressMessage(@NotNull String localeKey, @NotNull String... args) {
        if (executor != null) {
            plugin.getLocales().getLocale(localeKey, args).ifPresent(executor::sendMessage);
        }
    }

    /**
     * Discovers all users in the source database using the getAllUsers method
     */
    private Set<User> discoverAllUsers() {
        Set<User> users = new HashSet<>();

        // Use the new getAllUsers method to get all users directly from the database
        List<SavedUser> allSavedUsers = sourceDatabase.getAllUsers();
        allSavedUsers.forEach(savedUser -> users.add(savedUser.getUser()));

        return users;
    }

    private int importUsers(Set<User> users) {
        AtomicInteger count = new AtomicInteger(0);

        // Import each unique user
        users.forEach(user -> {
            Optional<SavedUser> savedUser = sourceDatabase.getUser(user.getUuid());
            if (savedUser.isPresent()) {
                targetDatabase.ensureUser(user);
                targetDatabase.updateUserData(savedUser.get());
                count.incrementAndGet();
            }
        });

        return count.get();
    }

    private int importWarps() {
        List<Warp> warps = sourceDatabase.getWarps();
        warps.forEach(targetDatabase::saveWarp);
        return warps.size();
    }

    private int importHomes(Set<User> users) {
        AtomicInteger count = new AtomicInteger(0);
        Set<UUID> processedHomes = new HashSet<>();

        // Import all homes for each user
        users.forEach(user -> {
            List<Home> userHomes = sourceDatabase.getHomes(user);
            userHomes.forEach(home -> {
                // Avoid duplicates by checking UUID
                if (!processedHomes.contains(home.getUuid())) {
                    targetDatabase.saveHome(home);
                    processedHomes.add(home.getUuid());
                    count.incrementAndGet();
                }
            });
        });

        return count.get();
    }

    private int importUserPositions(Set<User> users) {
        AtomicInteger count = new AtomicInteger(0);

        // Import last positions, offline positions, and respawn positions
        users.forEach(user -> {
            // Import last position
            Optional<Position> lastPosition = sourceDatabase.getLastPosition(user);
            if (lastPosition.isPresent()) {
                targetDatabase.setLastPosition(user, lastPosition.get());
                count.incrementAndGet();
            }

            // Import offline position
            Optional<Position> offlinePosition = sourceDatabase.getOfflinePosition(user);
            if (offlinePosition.isPresent()) {
                targetDatabase.setOfflinePosition(user, offlinePosition.get());
                count.incrementAndGet();
            }

            // Import respawn position
            Optional<Position> respawnPosition = sourceDatabase.getRespawnPosition(user);
            if (respawnPosition.isPresent()) {
                targetDatabase.setRespawnPosition(user, respawnPosition.get());
                count.incrementAndGet();
            }
        });

        return count.get();
    }

    private int importCooldowns(Set<User> users) {
        AtomicInteger count = new AtomicInteger(0);

        // Import cooldowns for all actions and users
        users.forEach(user -> {
            for (TransactionResolver.Action action : TransactionResolver.Action.values()) {
                Optional<Instant> cooldown = sourceDatabase.getCooldown(action, user);
                if (cooldown.isPresent()) {
                    targetDatabase.setCooldown(action, user, cooldown.get());
                    count.incrementAndGet();
                }
            }
        });

        return count.get();
    }

    /**
     * Result of a database import operation.
     */
    public static class ImportResult {
        public boolean success = false;
        public String errorMessage = "";
        public int usersImported = 0;
        public int homesImported = 0;
        public int warpsImported = 0;
        public int positionsImported = 0;
        public int cooldownsImported = 0;

        public int getTotalImported() {
            return usersImported + homesImported + warpsImported + positionsImported + cooldownsImported;
        }

        @Override
        public String toString() {
            if (!success) {
                return "Import failed: " + errorMessage;
            }
            return String.format("Import successful! Users: %d, Homes: %d, Warps: %d, Positions: %d, Cooldowns: %d (Total: %d)",
                usersImported, homesImported, warpsImported, positionsImported, cooldownsImported, getTotalImported());
        }
    }
}
