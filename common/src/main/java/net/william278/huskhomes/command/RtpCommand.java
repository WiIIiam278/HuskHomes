package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.hook.EconomyHook;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.TeleportBuilder;
import net.william278.huskhomes.teleport.TeleportationException;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.SavedUser;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RtpCommand extends Command {

    protected RtpCommand(@NotNull HuskHomes plugin) {
        super("rtp", List.of(), "[player]", plugin);
        addAdditionalPermissions(Map.of(
                "other", true,
                "bypass_cooldown", true
        ));
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
        final Optional<OnlineUser> optionalTeleporter = args.length >= 1 ? plugin.findOnlinePlayer(args[0])
                : executor instanceof OnlineUser ? Optional.of((OnlineUser) executor) : Optional.empty();
        if (optionalTeleporter.isEmpty()) {
            if (args.length == 0) {
                plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                        .ifPresent(executor::sendMessage);
                return;
            }

            plugin.getLocales().getLocale("error_player_not_found", args[0])
                    .ifPresent(executor::sendMessage);
            return;
        }

        final OnlineUser teleporter = optionalTeleporter.get();
        if (!executor.equals(teleporter) && !executor.hasPermission(getPermission("other"))) {
            plugin.getLocales().getLocale("error_no_permission")
                    .ifPresent(executor::sendMessage);
            return;
        }

        this.executeRtp(teleporter, executor, args);
    }

    private void executeRtp(@NotNull OnlineUser teleporter, @NotNull CommandUser executor, @NotNull String[] args) {
        if (!plugin.validateEconomyCheck(teleporter, EconomyHook.Action.RANDOM_TELEPORT)) {
            return;
        }

        if (plugin.getSettings().getRtpRestrictedWorlds().stream()
                .anyMatch(worldName -> worldName.equals(teleporter.getPosition().getWorld().getName()))) {
            plugin.getLocales().getLocale("error_rtp_restricted_world")
                    .ifPresent(executor::sendMessage);
            return;
        }

        final SavedUser user = plugin.getDatabase().getUserData(teleporter.getUuid())
                .orElseThrow(() -> new IllegalStateException("No user data found for " + teleporter.getUsername()));
        final Instant currentTime = Instant.now();
        if (executor.equals(teleporter) && !currentTime.isAfter(user.getRtpCooldown()) &&
            !executor.hasPermission(getPermission("bypass_cooldown"))) {
            plugin.getLocales().getLocale("error_rtp_cooldown",
                            Long.toString(currentTime.until(user.getRtpCooldown(), ChronoUnit.MINUTES) + 1))
                    .ifPresent(executor::sendMessage);
            return;
        }

        // Generate a random position
        plugin.getLocales().getLocale("teleporting_random_generation")
                .ifPresent(teleporter::sendMessage);
        final Optional<Position> position = plugin.getRandomTeleportEngine()
                .getRandomPosition(teleporter.getPosition().getWorld(), args.length > 1 ? removeFirstArg(args) : args);
        if (position.isEmpty()) {
            plugin.getLocales().getLocale("error_rtp_randomization_timeout")
                    .ifPresent(executor::sendMessage);
            return;
        }

        final TeleportBuilder builder = Teleport.builder(plugin)
                .teleporter(teleporter)
                .target(position.get());
        try {
            if (executor.equals(teleporter)) {
                builder.toTimedTeleport().execute();
            } else {
                builder.toTeleport().execute();
            }
        } catch (TeleportationException e) {
            e.displayMessage(executor, plugin, args);
            return;
        }

        user.setRtpCooldown(Instant.now().plus(plugin.getSettings().getRtpCooldownLength(), ChronoUnit.MINUTES));
        plugin.getDatabase().updateUserData(user);
    }

}
