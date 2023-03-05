package net.william278.huskhomes.user;

import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public interface CommandUser {

    @NotNull
    Audience getAudience();

    boolean hasPermission(@NotNull String permission);

    default void sendMessage(@NotNull Component component) {
        getAudience().sendMessage(component);
    }

    default void sendMessage(@NotNull MineDown mineDown) {
        this.sendMessage(mineDown.toComponent());
    }

}