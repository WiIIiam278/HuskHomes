package net.william278.huskhomes.command;

import de.themoep.minedown.MineDown;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.Player;
import net.william278.huskhomes.player.User;
import net.william278.huskhomes.position.PositionMeta;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

public class SetWarpCommand extends CommandBase {

    public SetWarpCommand(@NotNull HuskHomes implementor) {
        super("setwarp", Permission.COMMAND_SET_WARP, implementor);
    }

    @Override
    public void onExecute(@NotNull Player player, @NotNull String[] args) {
        final User user = new User(player);
        switch (args.length) {
            case 0 -> plugin.getDatabase().getHomes(user).thenAccept(homes -> {
                plugin.getLocales().getLocale("error_invalid_syntax", "/setwarp <name>")
                        .ifPresent(player::sendMessage);
            });
            case 1 -> setWarp(player, args[0]);
            default -> plugin.getLocales().getLocale("error_invalid_syntax", "/setwarp <name>")
                    .ifPresent(player::sendMessage);
        }
    }

    private void setWarp(@NotNull Player player, @NotNull String warpName) {
        player.getPosition().thenAccept(position -> plugin.getSavedPositionManager().setWarp(new PositionMeta(warpName,
                        plugin.getLocales().getRawLocale("warp_default_description").orElse("")),
                position).thenAccept(setResult ->
                player.sendMessage(switch (setResult.resultType()) {
                    case SUCCESS -> {
                        assert setResult.setPosition() != null;
                        yield plugin.getLocales().getLocale("set_warp_success", setResult.setPosition().meta.name)
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
