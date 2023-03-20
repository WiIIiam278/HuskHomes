package net.william278.huskhomes.teleport;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.CommandUser;
import org.jetbrains.annotations.NotNull;

public class TeleportationException extends IllegalArgumentException {

    public TeleportationException(@NotNull Type error) {
        super("Error during teleport operation: " + error.name());
    }

    public enum Type {
        TELEPORTER_NOT_FOUND,
        TARGET_NOT_FOUND,
        ALREADY_WARMING_UP,
        ECONOMY_ACTION_FAILED,
        WARMUP_ALREADY_MOVING,
        WORLD_NOT_FOUND,
        ILLEGAL_TARGET_COORDINATES,
        CANNOT_TELEPORT_TO_SELF
    }

    public void displayMessage(@NotNull CommandUser user, @NotNull HuskHomes plugin, @NotNull String[] args) {
        switch (Type.valueOf(getMessage().split(": ")[1])) {
            case TELEPORTER_NOT_FOUND, TARGET_NOT_FOUND -> plugin.getLocales()
                    .getLocale("error_player_not_found", args)
                    .ifPresent(user::sendMessage);
            case ALREADY_WARMING_UP -> plugin.getLocales()
                    .getLocale("error_already_teleporting")
                    .ifPresent(user::sendMessage);
            case WARMUP_ALREADY_MOVING -> plugin.getLocales()
                    .getLocale("error_teleport_warmup_stand_still")
                    .ifPresent(user::sendMessage);
            case CANNOT_TELEPORT_TO_SELF -> plugin.getLocales()
                    .getLocale("error_teleport_request_self")
                    .ifPresent(user::sendMessage);
            case ILLEGAL_TARGET_COORDINATES -> plugin.getLocales()
                    .getLocale("error_illegal_target_coordinates")
                    .ifPresent(user::sendMessage);
            case WORLD_NOT_FOUND -> plugin.getLocales()
                    .getLocale("error_invalid_world")
                    .ifPresent(user::sendMessage);
        }
    }
}
