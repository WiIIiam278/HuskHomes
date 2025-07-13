package net.william278.huskhomes.pluginmessage;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;

public abstract class PluginMessageAPI {

    @NotNull
    @Blocking
    public String getUserHomesJson(@NotNull OnlineUser owner) {
        if (getPlugin().getDatabase().getUser(owner.getUuid()).isEmpty()) {
            return getErrorResponse(ErrorResponse.Type.USER_NOT_FOUND);
        }
        return getPlugin().getGson().toJson(getPlugin().getDatabase().getHomes(owner));
    }

    private String getErrorResponse(@NotNull ErrorResponse.Type type, @NotNull String... message) {
        return getPlugin().getGson().toJson(new ErrorResponse(type, String.join(", ", message)));
    }



    protected abstract HuskHomes getPlugin();

}
