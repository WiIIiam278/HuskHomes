package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.CommandUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public abstract class Command extends Node {

    private final String usage;

    protected Command(@NotNull String name, @NotNull List<String> aliases, @NotNull String usage, @NotNull HuskHomes plugin) {
        super(name, aliases, plugin);
        this.usage = usage;
    }

    @Override
    public final void onExecuted(@NotNull CommandUser executor, @NotNull String[] args) {
        if (!executor.hasPermission(getPermission())) {
            plugin.getLocales().getLocale("error_no_permission")
                    .ifPresent(executor::sendMessage);
            return;
        }
        plugin.runAsync(() -> this.execute(executor, args));
    }

    public abstract void execute(@NotNull CommandUser executor, @NotNull String[] args);

    @NotNull
    protected String[] removeFirstArg(@NotNull String[] args) {
        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, args.length - 1);
        return newArgs;
    }

    @NotNull
    public final String getUsage() {
        return "/" + getName() + " " + usage;
    }

    @NotNull
    public String getDescription() {
        return plugin.getLocales().getRawLocale(getName() + "_command_description")
                .map(description -> plugin.getLocales().truncateText(description, 40))
                .orElse(getUsage());
    }

    @NotNull
    public Map<String, Boolean> getAdditionalPermissions() {
        return Map.of();
    }

    @NotNull
    public final HuskHomes getPlugin() {
        return plugin;
    }

}