package net.william278.huskhomes.command;

import de.themoep.minedown.MineDown;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.PositionMeta;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

public class SetWarpCommand extends CommandBase {

    public SetWarpCommand(@NotNull HuskHomes implementor) {
        super("setwarp", Permission.COMMAND_SET_WARP, implementor);
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        if (args.length == 1) {
            setWarp(onlineUser, args[0]);
        } else {
            plugin.getLocales().getLocale("error_invalid_syntax", "/setwarp <name>")
                    .ifPresent(onlineUser::sendMessage);
        }
    }

    private void setWarp(@NotNull OnlineUser onlineUser, @NotNull String warpName) {
        onlineUser.getPosition().thenAccept(position -> plugin.getSavedPositionManager().setWarp(new PositionMeta(warpName,
                        plugin.getLocales().getRawLocale("warp_default_description").orElse("")),
                position).thenAccept(setResult ->
                onlineUser.sendMessage(switch (setResult.resultType()) {
                    case SUCCESS -> {
                        assert setResult.savedPosition().isPresent();
                        yield plugin.getLocales().getLocale("set_warp_success", setResult.savedPosition().get().meta.name)
                                .orElse(new MineDown(""));
                    }
                    case FAILED_DUPLICATE -> plugin.getLocales().getLocale("error_set_warp_name_taken")
                            .orElse(new MineDown(""));
                    case FAILED_NAME_LENGTH -> plugin.getLocales().getLocale("error_set_warp_invalid_length")
                            .orElse(new MineDown(""));
                    case FAILED_NAME_CHARACTERS -> plugin.getLocales().getLocale("error_set_warp_invalid_characters")
                            .orElse(new MineDown(""));
                    default -> new MineDown("");
                })));
    }

}
