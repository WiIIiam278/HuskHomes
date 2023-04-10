package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.CommandUser;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Command extends Node {

    private final String usage;
    private final Map<String, Boolean> additionalPermissions;

    protected Command(@NotNull String name, @NotNull List<String> aliases, @NotNull String usage, @NotNull HuskHomes plugin) {
        super(name, aliases, plugin);
        this.usage = usage;
        this.additionalPermissions = new HashMap<>();
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
        if (args.length <= 1) {
            return new String[0];
        }
        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, args.length - 1);
        return newArgs;
    }

    @NotNull
    public final String getRawUsage() {
        return usage;
    }

    @NotNull
    public final String getUsage() {
        return "/" + getName() + " " + getRawUsage();
    }

    public final void addAdditionalPermissions(@NotNull Map<String, Boolean> additionalPermissions) {
        additionalPermissions.forEach((permission, value) -> this.additionalPermissions.put(getPermission(permission), value));
    }

    @NotNull
    public final Map<String, Boolean> getAdditionalPermissions() {
        return additionalPermissions;
    }

    @NotNull
    public String getDescription() {
        return plugin.getLocales().getRawLocale(getName() + "_command_description")
                .orElse(getUsage());
    }

    @NotNull
    public final HuskHomes getPlugin() {
        return plugin;
    }

}