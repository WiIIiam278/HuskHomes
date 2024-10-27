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
import org.jetbrains.annotations.NotNull;

public interface DatabaseProvider {

    @NotNull
    Database getDatabase();

    void setDatabase(@NotNull Database database);

    void closeDatabase();

    default void loadDatabase() throws IllegalStateException {
        // Create database instance
        final Database database = createDatabase();

        // Initialize database
        database.initialize();
        if (!database.isLoaded()) {
            throw new IllegalStateException("Failed to initialize database");
        }

        // Set database
        setDatabase(database);
    }

    @NotNull
    private Database createDatabase() {
        final Database.Type type = getPlugin().getSettings().getDatabase().getType();
        switch (type) {
            case MYSQL, MARIADB -> {
                return new MySqlDatabase(getPlugin());
            }
            case POSTGRESQL -> {
                return new PostgreSqlDatabase(getPlugin());
            }
            case SQLITE -> {
                return new SqLiteDatabase(getPlugin());
            }
            case H2 -> {
                return new H2Database(getPlugin());
            }
            default -> throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    @NotNull
    HuskHomes getPlugin();

}
