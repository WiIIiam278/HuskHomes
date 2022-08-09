package net.william278.huskhomes.command;

import de.themoep.minedown.MineDown;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.player.User;
import net.william278.huskhomes.position.PositionMeta;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

public class SetHomeCommand extends CommandBase {

    public SetHomeCommand(@NotNull HuskHomes implementor) {
        super("sethome", Permission.COMMAND_SET_HOME, implementor);
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        
        switch (args.length) {
            case 0 -> plugin.getDatabase().getHomes(onlineUser).thenAccept(homes -> {
                if (homes.isEmpty()) {
                    setHome(onlineUser, onlineUser, "home");
                } else {
                    plugin.getLocales().getLocale("error_invalid_syntax", "/sethome <name>")
                            .ifPresent(onlineUser::sendMessage);
                }
            });
            case 1 -> setHome(onlineUser, onlineUser, args[0]);
            default -> plugin.getLocales().getLocale("error_invalid_syntax", "/sethome <name>")
                    .ifPresent(onlineUser::sendMessage);
        }
    }

    private void setHome(@NotNull OnlineUser onlineUser, @NotNull User user, @NotNull String homeName) {
        onlineUser.getPosition().thenAccept(position -> plugin.getSavedPositionManager().setHome(new PositionMeta(homeName,
                        plugin.getLocales().getRawLocale("home_default_description", user.username).orElse("")),
                user, position).thenAccept(setResult ->
                onlineUser.sendMessage(switch (setResult.resultType()) {
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
                })));
    }

}
