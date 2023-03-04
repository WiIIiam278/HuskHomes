package net.william278.huskhomes.util;

import org.jetbrains.annotations.NotNull;

public class ValidationException extends IllegalArgumentException {

    public ValidationException(@NotNull ValidationException.Type error) {
        super("Error validating position: " + error.name());
    }

    public enum Type {
        NOT_FOUND,
        NAME_TAKEN,
        NAME_INVALID,
        DESCRIPTION_INVALID
    }

}
