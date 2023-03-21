package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.CommandUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface UserListTabProvider extends TabProvider {

    @Override
    @Nullable
    default List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
        return args.length < 2 ? getPlugin().getPlayerList() : List.of();
    }

    @NotNull
    HuskHomes getPlugin();

}
