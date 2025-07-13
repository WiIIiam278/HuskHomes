package net.william278.huskhomes.pluginmessage;

import org.jetbrains.annotations.NotNull;

public record ErrorResponse(@NotNull Type type, @NotNull String message) {

    public enum Type {
        USER_NOT_FOUND,
        NO_ENTRIES
    }

}
