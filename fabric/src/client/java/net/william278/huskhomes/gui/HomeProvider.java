package net.william278.huskhomes.gui;

import lombok.Getter;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.pluginmessage.ErrorResponse;
import net.william278.huskhomes.position.Home;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface HomeProvider {

    @NotNull
    Response<List<Home>> getUserHomes();

    @NotNull
    HuskHomes plugin();

    final class Response<T> {

        private @Nullable T response;
        @Getter
        private @Nullable ErrorResponse error;

        private Response(@Nullable T response) {
            this.response = response;
        }

        private Response(@Nullable ErrorResponse error) {
            this.error = error;
        }

        public Optional<T> getResponse() {
            return Optional.ofNullable(response);
        }

        public static <T> Response<T> error(@NotNull ErrorResponse.Type type, @NotNull String... message) {
            return new Response<>(new ErrorResponse(type, String.join(", ", message)));
        }

        public static <T> Response<T> success(@NotNull T response) {
            return new Response<>(response);
        }

    }

}
