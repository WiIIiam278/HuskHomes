package net.william278.huskhomes.user;

import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

public final class ConsoleUser implements CommandUser {

    @NotNull
    private final Audience audience;

    public ConsoleUser(@NotNull Audience console) {
        this.audience = console;
    }

    @Override
    @NotNull
    public Audience getAudience() {
        return audience;
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return true;
    }
}