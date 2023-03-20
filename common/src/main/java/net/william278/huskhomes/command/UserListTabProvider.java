package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.CommandUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public interface UserListTabProvider extends TabProvider {

    @Override
    @Nullable
    default List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
        return args.length <= 1 ? getPlugin().getCache().getPlayers().stream()
                .filter(s -> s.toLowerCase().startsWith(args.length == 1 ? args[0].toLowerCase() : ""))
                .sorted().collect(Collectors.toList()) : Collections.emptyList();
    }

    @NotNull
    HuskHomes getPlugin();

}
