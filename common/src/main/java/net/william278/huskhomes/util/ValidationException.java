package net.william278.huskhomes.util;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.CommandUser;
import org.jetbrains.annotations.NotNull;

public class ValidationException extends IllegalArgumentException {

    private final Type error;

    public ValidationException(@NotNull ValidationException.Type error) {
        super("Error validating position: " + error.name());
        this.error = error;
    }

    public void dispatchHomeError(@NotNull CommandUser viewer, boolean other, @NotNull HuskHomes plugin, @NotNull String... args) {
        plugin.getLocales()
                .getLocale(switch (error) {
                    case NOT_FOUND -> other ? "error_home_invalid_other" : "error_home_invalid";
                    case NAME_TAKEN -> "error_home_name_taken";
                    case NAME_INVALID -> "error_home_name_characters";
                    case DESCRIPTION_INVALID -> "error_home_description_characters";
                }, args)
                .ifPresent(viewer::sendMessage);
    }

    public void dispatchWarpError(@NotNull CommandUser viewer, @NotNull HuskHomes plugin) {
        plugin.getLocales()
                .getLocale(switch (error) {
                    case NOT_FOUND -> "error_warp_invalid";
                    case NAME_TAKEN -> "error_warp_name_taken";
                    case NAME_INVALID -> "error_warp_name_characters";
                    case DESCRIPTION_INVALID -> "error_warp_description_characters";
                })
                .ifPresent(viewer::sendMessage);
    }

    public enum Type {
        NOT_FOUND,
        NAME_TAKEN,
        NAME_INVALID,
        REACHED_MAX_HOMES, NOT_ENOUGH_HOME_SLOTS, REACHED_MAX_PUBLIC_HOMES, DESCRIPTION_INVALID
    }

}
