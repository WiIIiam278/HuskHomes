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

package net.william278.huskhomes.util;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Home;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class Validator {

    public static final int MAX_NAME_LENGTH = 16;
    public static final int MIN_NAME_LENGTH = 1;
    public static final int MAX_DESCRIPTION_LENGTH = 256;

    private final HuskHomes plugin;

    public Validator(@NotNull HuskHomes plugin) {
        this.plugin = plugin;
    }

    /**
     * Check if a name is valid
     *
     * @param name The name to check
     * @return True if the name is valid as per the plugin settings, false otherwise
     */
    public boolean isValidName(@NotNull String name) {
        return (isAsciiOnly(name) || plugin.getSettings().doAllowUnicodeNames())
                && !containsWhitespace(name) && !name.contains(Home.IDENTIFIER_DELIMITER)
                && name.length() <= MAX_NAME_LENGTH && name.length() >= MIN_NAME_LENGTH;
    }

    /**
     * Validate home and warp descriptions
     *
     * @param description The meta to validate
     * @return Whether the meta is valid against the plugin settings
     */
    public boolean isValidDescription(@NotNull String description) {
        return (isAsciiOnly(description) || plugin.getSettings().doAllowUnicodeDescriptions())
                && description.length() <= MAX_DESCRIPTION_LENGTH;
    }

    // Check if a string contains only ASCII characters
    private static boolean isAsciiOnly(@NotNull String string) {
        return string.matches("\\A\\p{ASCII}*\\z") && !string.contains("\u0000");
    }

    // Check if a string contains whitespace
    private static boolean containsWhitespace(@NotNull String string) {
        return string.matches(".*\\s.*");
    }

}
