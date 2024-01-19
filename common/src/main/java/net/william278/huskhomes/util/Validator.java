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

public class Validator {

    public static final int MAX_NAME_LENGTH = 16;
    public static final int MIN_NAME_LENGTH = 1;
    public static final int MAX_DESCRIPTION_LENGTH = 256;

    private final HuskHomes plugin;

    public Validator(@NotNull HuskHomes plugin) {
        this.plugin = plugin;
    }

    /**
     * Validate home and warp names.
     *
     * @param name The name to validate
     * @throws ValidationException If the name is invalid
     */
    public void validateName(@NotNull String name) throws ValidationException {
        if (!isValidNameCharacters(name)) {
            throw new ValidationException(ValidationException.Type.NAME_INVALID_CHARACTERS);
        }
        if (!isValidNameLength(name)) {
            throw new ValidationException(ValidationException.Type.NAME_INVALID_LENGTH);
        }
    }

    /**
     * Validate home and warp descriptions.
     *
     * @param description The description to validate
     * @throws ValidationException If the description is invalid
     */
    public void validateDescription(@NotNull String description) throws ValidationException {
        if (!isValidDescriptionCharacters(description)) {
            throw new ValidationException(ValidationException.Type.DESCRIPTION_INVALID_CHARACTERS);
        }
        if (!isValidDescriptionLength(description)) {
            throw new ValidationException(ValidationException.Type.DESCRIPTION_INVALID_LENGTH);
        }
    }

    // Check a home/warp name contains only valid characters
    private boolean isValidNameCharacters(@NotNull String name) {
        return (name.matches(plugin.getSettings().getNameRegex()) || !plugin.getSettings().doRestrictNames())
                && !name.contains("\u0000")
                && !containsWhitespace(name)
                && !name.contains(Home.IDENTIFIER_DELIMITER);
    }

    // Check a home/warp name is of a valid length
    private boolean isValidNameLength(@NotNull String name) {
        return name.length() <= MAX_NAME_LENGTH && name.length() >= MIN_NAME_LENGTH;
    }

    // Check a home/warp description contains only valid characters
    private boolean isValidDescriptionCharacters(@NotNull String description) {
        return (description.matches(plugin.getSettings().getDescriptionRegex())
                || !plugin.getSettings().doRestrictDescriptions())
                && !description.contains("\u0000");
    }

    // Check a home/warp description is of a valid length
    private boolean isValidDescriptionLength(@NotNull String description) {
        return description.length() <= MAX_DESCRIPTION_LENGTH;
    }

    // Check if a string contains whitespace
    private boolean containsWhitespace(@NotNull String string) {
        return string.matches(".*\\s.*");
    }

}
