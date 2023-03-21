package net.william278.huskhomes.manager;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.PositionMeta;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.util.ValidationException;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class WarpsManager {
    private final HuskHomes plugin;

    protected WarpsManager(@NotNull HuskHomes plugin) {
        this.plugin = plugin;
    }

    public void createWarp(@NotNull String name, @NotNull Position position, boolean overwrite) throws ValidationException {
        final Optional<Warp> existingWarp = plugin.getDatabase().getWarp(name);
        if (existingWarp.isPresent() && !overwrite) {
            throw new ValidationException(ValidationException.Type.NAME_TAKEN);
        }

        if (!plugin.getValidator().isValidName(name)) {
            throw new ValidationException(ValidationException.Type.NAME_INVALID);
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

    public void createWarp(@NotNull String name, @NotNull Position position) throws ValidationException {
        this.createWarp(name, position, plugin.getSettings().doOverwriteExistingHomesWarps());
    }

    public void deleteWarp(@NotNull String name) throws ValidationException {
        final Optional<Warp> warp = plugin.getDatabase().getWarp(name);
        if (warp.isEmpty()) {
            throw new ValidationException(ValidationException.Type.NOT_FOUND);
        }

        this.deleteWarp(warp.get());
    }

    public void deleteWarp(@NotNull Warp warp) {
        plugin.getDatabase().deleteWarp(warp.getUuid());
    }

    public int deleteAllWarps() {
        return plugin.getDatabase().deleteAllWarps();
    }

    public void relocateWarp(@NotNull String name, @NotNull Position position) throws ValidationException {
        final Optional<Warp> optionalWarp = plugin.getDatabase().getWarp(name);
        if (optionalWarp.isEmpty()) {
            throw new ValidationException(ValidationException.Type.NOT_FOUND);
        }

        this.relocateWarp(optionalWarp.get(), position);
    }

    public void relocateWarp(@NotNull Warp warp, @NotNull Position position) {
        warp.update(position);
        plugin.getDatabase().saveWarp(warp);
    }

    public void renameWarp(@NotNull String name, @NotNull String newName) throws ValidationException {
        final Optional<Warp> optionalWarp = plugin.getDatabase().getWarp(name);
        if (optionalWarp.isEmpty()) {
            throw new ValidationException(ValidationException.Type.NOT_FOUND);
        }

        this.renameWarp(optionalWarp.get(), newName);
    }

    public void renameWarp(@NotNull Warp warp, @NotNull String newName) throws ValidationException {
        if (!plugin.getValidator().isValidName(newName)) {
            throw new ValidationException(ValidationException.Type.NAME_INVALID);
        }

        warp.getMeta().setName(newName);
        plugin.getDatabase().saveWarp(warp);
    }

    public void setWarpDescription(@NotNull String name, @NotNull String description) throws ValidationException {
        final Optional<Warp> optionalWarp = plugin.getDatabase().getWarp(name);
        if (optionalWarp.isEmpty()) {
            throw new ValidationException(ValidationException.Type.NOT_FOUND);
        }

        this.setWarpDescription(optionalWarp.get(), description);
    }

    public void setWarpDescription(@NotNull Warp warp, @NotNull String description) {
        if (!plugin.getValidator().isValidDescription(description)) {
            throw new ValidationException(ValidationException.Type.DESCRIPTION_INVALID);
        }

        warp.getMeta().setDescription(description);
        plugin.getDatabase().saveWarp(warp);
    }

}
