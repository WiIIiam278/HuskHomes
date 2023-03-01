package net.william278.huskhomes.manager;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.ValidationException;
import net.william278.huskhomes.player.User;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.PositionMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class HomesManager {
    private final HuskHomes plugin;

    protected HomesManager(@NotNull HuskHomes plugin) {
        this.plugin = plugin;
    }

    public void createHome(@NotNull User owner, @NotNull String name, @NotNull Position position, boolean overwrite) throws ValidationException {
        final Optional<Home> existingHome = plugin.getDatabase().getHome(owner, name);
        if (existingHome.isPresent() && !overwrite) {
            throw new ValidationException(ValidationException.ValidationError.NAME_TAKEN);
        }

        if (!plugin.getValidator().isValidName(name)) {
            throw new ValidationException(ValidationException.ValidationError.NAME_INVALID);
        }

        final Home home = existingHome
                .map(existing -> {
                    existing.x = position.x;
                    existing.y = position.y;
                    existing.z = position.z;
                    existing.world = position.world;
                    existing.server = position.server;
                    existing.yaw = position.yaw;
                    existing.pitch = position.pitch;
                    return existing;
                })
                .orElse(new Home(position, new PositionMeta(name, ""), owner));
        plugin.getDatabase().saveHome(home);
    }

    public void deleteHome(@NotNull User owner, @NotNull String name) throws ValidationException {
        final Optional<Home> home = plugin.getDatabase().getHome(owner, name);
        if (home.isEmpty()) {
            throw new ValidationException(ValidationException.ValidationError.NOT_FOUND);
        }
        plugin.getDatabase().deleteHome(home.get().uuid);
    }

    public void relocateHome(@NotNull User owner, @NotNull String name, @NotNull Position position) throws ValidationException {
        createHome(owner, name, position, true);
    }

    public void renameHome(@NotNull User owner, @NotNull String name, @NotNull String newName) throws ValidationException {
        final Optional<Home> optionalHome = plugin.getDatabase().getHome(owner, name);
        if (optionalHome.isEmpty()) {
            throw new ValidationException(ValidationException.ValidationError.NOT_FOUND);
        }

        if (!plugin.getValidator().isValidName(newName)) {
            throw new ValidationException(ValidationException.ValidationError.NAME_INVALID);
        }

        final Home home = optionalHome.get();
        home.meta.name = newName;
        plugin.getDatabase().saveHome(home);
    }

    public void updateHomeDescription(@NotNull User owner, @NotNull String name, @NotNull String description) throws ValidationException {
        final Optional<Home> optionalHome = plugin.getDatabase().getHome(owner, name);
        if (optionalHome.isEmpty()) {
            throw new ValidationException(ValidationException.ValidationError.NOT_FOUND);
        }

        if (!plugin.getValidator().isValidDescription(description)) {
            throw new ValidationException(ValidationException.ValidationError.DESCRIPTION_INVALID);
        }

        final Home home = optionalHome.get();
        home.meta.description = description;
        plugin.getDatabase().saveHome(home);
    }

    public void updateHomePrivacy(@NotNull User owner, @NotNull String name, boolean isPublic) throws ValidationException {
        final Optional<Home> optionalHome = plugin.getDatabase().getHome(owner, name);
        if (optionalHome.isEmpty()) {
            throw new ValidationException(ValidationException.ValidationError.NOT_FOUND);
        }

        final Home home = optionalHome.get();
        home.isPublic = isPublic;
        plugin.getDatabase().saveHome(home);
    }

}
