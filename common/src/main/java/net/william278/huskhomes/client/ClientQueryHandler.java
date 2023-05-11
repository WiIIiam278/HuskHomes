package net.william278.huskhomes.client;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.network.Payload;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public interface ClientQueryHandler {

    String CLIENT_MESSAGE_CHANNEL = "huskhomes:client_query";

    default void handleClientQuery(@NotNull OnlineUser user, byte[] message) {
        try {
            handleQuery(user, getPlugin().getGson().fromJson(new String(message, StandardCharsets.UTF_8), ClientQuery.class));
        } catch (Throwable e) {
            getPlugin().log(Level.SEVERE, "Failed to fully read client query plugin message", e);
        }
    }

    private void handleQuery(@NotNull OnlineUser user, @NotNull ClientQuery query) {
        getPlugin().runAsync(() -> {
            switch (query.getType()) {
                case HANDSHAKE -> reply(user, query, Payload.empty());
                case GET_WARPS -> reply(user, query, Payload.withWarpList(
                        new ArrayList<>(getWarpsForUser(user))
                ));
                case GET_PRIVATE_HOMES -> reply(user, query, Payload.withHomeList(
                        new ArrayList<>(getPlugin().getDatabase().getHomes(user))
                ));
                case GET_PUBLIC_HOMES -> reply(user, query, Payload.withHomeList(
                        new ArrayList<>(getPlugin().getDatabase().getPublicHomes())
                ));
            }
        });
    }

    @NotNull
    private List<Warp> getWarpsForUser(@NotNull OnlineUser user) {
        return getPlugin().getDatabase().getWarps().stream()
                .filter(warp -> {
                    if (getPlugin().getSettings().doPermissionRestrictWarps()) {
                        return user.hasPermission(warp.getPermission()) || user.hasPermission(Warp.getWildcardPermission());
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    private void reply(@NotNull OnlineUser user, @NotNull ClientQuery query, @NotNull Payload newPayload) {
        query.setPayload(newPayload);
        user.sendPluginMessage(
                CLIENT_MESSAGE_CHANNEL,
                getPlugin().getGson().toJson(query).getBytes(StandardCharsets.UTF_8)
        );
    }

    @NotNull
    HuskHomes getPlugin();

}
