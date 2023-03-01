package net.william278.huskhomes;

import org.jetbrains.annotations.NotNull;

public class ValidationException extends IllegalArgumentException {

    public ValidationException(@NotNull ValidationError error) {
        super("Error validating position: " + error.toString());
    }

    public enum ValidationError {
        NAME_LENGTH,
        NAME_TAKEN,
        NAME_CHARACTERS,
        NAME_RESERVED,
        DESCRIPTION_LENGTH,
        DESCRIPTION_CHARACTERS
    }
}
