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
        switch (error) {
            case NOT_FOUND -> plugin.getLocales()
                    .getLocale(other ? "error_home_invalid_other" : "error_home_invalid", args)
                    .ifPresent(viewer::sendMessage);
            case NAME_TAKEN -> plugin.getLocales()
                    .getLocale("error_home_name_taken")
                    .ifPresent(viewer::sendMessage);
            case NAME_INVALID -> plugin.getLocales()
                    .getLocale("error_home_name_characters", args)
                    .ifPresent(viewer::sendMessage);
            case NOT_ENOUGH_HOME_SLOTS, REACHED_MAX_HOMES -> plugin.getLocales()
                    .getLocale("error_set_home_maximum_homes", args)
                    .ifPresent(viewer::sendMessage);
            case REACHED_MAX_PUBLIC_HOMES -> plugin.getLocales()
                    .getLocale("error_edit_home_maximum_public_homes", args)
                    .ifPresent(viewer::sendMessage);
            case DESCRIPTION_INVALID -> plugin.getLocales()
                    .getLocale("error_home_description_characters", args)
                    .ifPresent(viewer::sendMessage);
        }
    }

    public void dispatchWarpError(@NotNull CommandUser viewer, @NotNull HuskHomes plugin, @NotNull String... args) {
        switch (error) {
            case NOT_FOUND -> plugin.getLocales()
                    .getLocale("error_warp_invalid", args)
                    .ifPresent(viewer::sendMessage);
            case NAME_TAKEN -> plugin.getLocales()
                    .getLocale("error_warp_name_taken", args)
                    .ifPresent(viewer::sendMessage);
            case NAME_INVALID -> plugin.getLocales()
                    .getLocale("error_warp_name_characters", args)
                    .ifPresent(viewer::sendMessage);
            case DESCRIPTION_INVALID -> plugin.getLocales()
                    .getLocale("error_warp_description_characters", args)
                    .ifPresent(viewer::sendMessage);
        }
    }

    public enum Type {
        NOT_FOUND,
        NAME_TAKEN,
        NAME_INVALID,
        REACHED_MAX_HOMES,
        NOT_ENOUGH_HOME_SLOTS,
        REACHED_MAX_PUBLIC_HOMES,
        NOT_ENOUGH_MONEY,
        DESCRIPTION_INVALID
    }

}
