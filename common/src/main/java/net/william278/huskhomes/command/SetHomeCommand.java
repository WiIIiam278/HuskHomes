package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.util.ValidationException;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class SetHomeCommand extends SetPositionCommand {

    protected SetHomeCommand(@NotNull HuskHomes plugin) {
        super("sethome", plugin);
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
        if (executor instanceof OnlineUser user && args.length == 0 && createDefaultHome(user)) {
            return;
        }
        super.execute(executor, args);
    }

    @Override
    protected void execute(@NotNull OnlineUser setter, @NotNull String name) {
        plugin.fireEvent(plugin.getHomeCreateEvent(setter, name, setter.getPosition(), setter), (event) -> {
            try {
                plugin.getManager().homes().createHome(setter, event.getName(), event.getPosition());
            } catch (ValidationException e) {
                e.dispatchHomeError(setter, false, plugin, event.getName());
                return;
            }
            plugin.getLocales().getLocale("set_home_success", event.getName())
                    .ifPresent(setter::sendMessage);
        });
    }

    private boolean createDefaultHome(@NotNull OnlineUser user) {
        final List<Home> homes = plugin.getDatabase().getHomes(user);
        final Optional<String> name = homes.isEmpty() ? Optional.of("home") :
                (homes.size() == 1 && plugin.getSettings().doOverwriteExistingHomesWarps())
                        ? Optional.of(homes.get(0).getName()) : Optional.empty();
        if (name.isPresent()) {
            this.execute(user, "home");
            return true;
        }
        return false;
    }

}
