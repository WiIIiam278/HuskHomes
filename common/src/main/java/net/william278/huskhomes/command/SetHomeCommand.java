package net.william278.huskhomes.command;

import de.themoep.minedown.MineDown;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.list.PrivateHomeList;
import net.william278.huskhomes.player.Player;
import net.william278.huskhomes.player.User;
import net.william278.huskhomes.position.PositionMeta;
import net.william278.huskhomes.util.RegexUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class SetHomeCommand extends CommandBase {

    private final static String PERMISSION = "huskhomes.command.sethome";

    public SetHomeCommand(@NotNull HuskHomes implementor) {
        super("sethome", PERMISSION, implementor);
    }

    @Override
    public void onExecute(@NotNull Player player, @NotNull String[] args) {
        final User user = new User(player);
        switch (args.length) {
            case 0 -> plugin.getDatabase().getHomes(user).thenAccept(homes -> {
                if (homes.isEmpty()) {
                    setHome(player, user, "home");
                } else {
                    plugin.getLocales().getLocale("error_invalid_syntax", "/sethome <name>")
                            .ifPresent(player::sendMessage);
                }
            });
            case 1 -> setHome(player, user, args[0]);
            default -> plugin.getLocales().getLocale("error_invalid_syntax", "/sethome <name>")
                    .ifPresent(player::sendMessage);
        }
    }

    private void setHome(@NotNull Player player, @NotNull User user, @NotNull String homeName) {
        player.getPosition().thenAccept(position -> plugin.getSettingManager().setHome(new PositionMeta(homeName,
                        plugin.getLocales().getRawLocale("home_default_description").orElse("")),
                user, position).thenAcceptAsync(setResult -> {
            player.sendMessage(switch (setResult.resultType()) {
                case SUCCESS -> {
                    assert setResult.setPosition() != null;
                    yield plugin.getLocales().getLocale("set_home_success", setResult.setPosition().meta.name)
                            .orElse(new MineDown(""));
                }
                case FAILED_DUPLICATE -> plugin.getLocales().getLocale("error_set_home_name_taken")
                        .orElse(new MineDown(""));
                case FAILED_NAME_LENGTH -> plugin.getLocales().getLocale("error_set_home_invalid_length")
                        .orElse(new MineDown(""));
                case FAILED_NAME_CHARACTERS -> plugin.getLocales().getLocale("error_set_home_invalid_characters")
                        .orElse(new MineDown(""));
                default -> new MineDown("");
            });
        }));
    }

}
