package net.william278.huskhomes.gui;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.pluginmessage.ErrorResponse;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.user.SavedUser;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record IntegratedServerHomeProvider(@Getter HuskHomes plugin) implements HomeProvider {

    @Override
    @NotNull
    @Blocking
    public Response<List<Home>> getUserHomes() {
        final Optional<SavedUser> user = plugin.getDatabase().getUser(getUserUuid());
        if (user.isEmpty()) {
            return Response.error(ErrorResponse.Type.USER_NOT_FOUND);
        }
        final List<Home> homes = plugin.getDatabase().getHomes(user.get().getUser());
        if (homes.isEmpty()) {
            return Response.error(ErrorResponse.Type.NO_ENTRIES);
        }
        return Response.success(homes);
    }

    @NotNull
    private UUID getUserUuid() {
        return MinecraftClient.getInstance().getGameProfile().getId();
    }

}
