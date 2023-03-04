package net.william278.huskhomes.teleport;

import org.jetbrains.annotations.NotNull;

public interface Target {

    @NotNull
    static Target username(@NotNull String target) {
        return new Username(target);
    }

}
