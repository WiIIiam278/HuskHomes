package net.william278.huskhomes.util;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * A provider for the plugin user list, tracking online users across the network.
 *
 * @since 4.8
 */
public interface UserListProvider {

    @NotNull
    Map<String, List<User>> getGlobalUserList();

    @NotNull
    default List<User> getUserList() {
        return Stream.concat(
                getGlobalUserList().values().stream().flatMap(Collection::stream),
                getPlugin().getOnlineUsers().stream().filter(o -> !o.isVanished())
        ).distinct().sorted().toList();
    }

    default void setUserList(@NotNull String server, @NotNull List<User> players) {
        getGlobalUserList().values().forEach(list -> {
            list.removeAll(players);
            list.removeAll(getPlugin().getOnlineUsers());
        });
        getGlobalUserList().put(server, players);
    }

    default boolean isUserOnline(@NotNull User user) {
        return getUserList().contains(user);
    }

    @NotNull
    HuskHomes getPlugin();

}
