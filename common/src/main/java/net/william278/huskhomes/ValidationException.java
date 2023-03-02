package net.william278.huskhomes;

import org.jetbrains.annotations.NotNull;

public class ValidationException extends IllegalArgumentException {

    public ValidationException(@NotNull ValidationError error) {
        super("Error validating position: " + error.name());
    }

    public enum ValidationError {
        NOT_FOUND,
        NAME_TAKEN,
        NAME_INVALID,
        DESCRIPTION_INVALID
    }

}
