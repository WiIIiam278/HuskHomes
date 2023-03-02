package net.william278.huskhomes.manager;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.ValidationException;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.PositionMeta;
import net.william278.huskhomes.position.Warp;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class WarpsManager {
    private final HuskHomes plugin;
    protected WarpsManager(@NotNull HuskHomes plugin) {
        this.plugin = plugin;
    }

    public void createWarp(@NotNull String name, @NotNull Position position, boolean overwrite) {
        final Optional<Warp> existingWarp = plugin.getDatabase().getWarp(name);
        if (existingWarp.isPresent() && !overwrite) {
            throw new ValidationException(ValidationException.ValidationError.NAME_TAKEN);
        }

        if (!plugin.getValidator().isValidName(name)) {
            throw new ValidationException(ValidationException.ValidationError.NAME_INVALID);
        }

        final Warp warp = existingWarp
                .map(existing -> {
                    existing.setX(position.getX());
                    existing.setY(position.getY());
                    existing.setZ(position.getZ());
                    existing.setWorld(position.getWorld());
                    existing.setServer(position.getServer());
                    existing.setYaw(position.getYaw());
                    existing.setPitch(position.getPitch());
                    return existing;
                })
                .orElse(new Warp(position, new PositionMeta(name, "")));
        plugin.getDatabase().saveWarp(warp);
    }

    public void deleteWarp(@NotNull String name) {
        final Optional<Warp> warp = plugin.getDatabase().getWarp(name);
        if (warp.isEmpty()) {
            throw new ValidationException(ValidationException.ValidationError.NOT_FOUND);
        }
        plugin.getDatabase().deleteWarp(warp.get().getUuid());
    }

    public void relocateWarp(@NotNull String name, @NotNull Position position) {
        createWarp(name, position, true);
    }

    public void renameHome(@NotNull String name, @NotNull String newName) throws ValidationException {
        final Optional<Warp> optionalWarp = plugin.getDatabase().getWarp(name);
        if (optionalWarp.isEmpty()) {
            throw new ValidationException(ValidationException.ValidationError.NOT_FOUND);
        }

        if (!plugin.getValidator().isValidName(newName)) {
            throw new ValidationException(ValidationException.ValidationError.NAME_INVALID);
        }

        final Warp warp = optionalWarp.get();
        warp.getMeta().setName(newName);
        plugin.getDatabase().saveWarp(warp);
    }

    public void setWarpDescription(@NotNull String name, @NotNull String description) {
        final Optional<Warp> optionalWarp = plugin.getDatabase().getWarp(name);
        if (optionalWarp.isEmpty()) {
            throw new ValidationException(ValidationException.ValidationError.NOT_FOUND);
        }

        final Warp warp = optionalWarp.get();
        warp.getMeta().setDescription(description);
        plugin.getDatabase().saveWarp(warp);
    }

}
