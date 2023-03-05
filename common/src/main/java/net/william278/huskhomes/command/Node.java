package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.ConsoleUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

public abstract class Node implements Executable {

    protected static final String PERMISSION_PREFIX = "huskhomes.command";

    protected final HuskHomes plugin;
    private final String name;
    private final List<String> aliases;
    private boolean consoleExecutable = false;
    private boolean operatorCommand = false;

    protected Node(@NotNull String name, @NotNull List<String> aliases, @NotNull HuskHomes plugin) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("Command name cannot be blank");
        }
        this.name = name;
        this.aliases = aliases;
        this.plugin = plugin;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public List<String> getAliases() {
        return aliases;
    }

    public final boolean matchesInput(@NotNull String input) {
        return input.equalsIgnoreCase(getName()) || getAliases().contains(input.toLowerCase());
    }

    @NotNull
    public String getPermission() {
        return new StringJoiner(".")
                .add(PERMISSION_PREFIX)
                .add(getName()).toString();
    }

    public boolean canPerform(@NotNull CommandUser user) {
        if (user instanceof ConsoleUser) {
            return isConsoleExecutable();
        }
        return user.hasPermission(getPermission());
    }

    public boolean isConsoleExecutable() {
        return consoleExecutable;
    }

    public void setConsoleExecutable(boolean consoleExecutable) {
        this.consoleExecutable = consoleExecutable;
    }

    public boolean isOperatorCommand() {
        return operatorCommand;
    }

    public void setOperatorCommand(boolean operatorCommand) {
        this.operatorCommand = operatorCommand;
    }

    protected Optional<Integer> parseIntArg(@NotNull String[] args, int index) {
        try {
            if (args.length > index) {
                return Optional.of(Integer.parseInt(args[index]));
            }
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    protected Optional<Double> parseDoubleArg(@NotNull String[] args, int index) {
        try {
            if (args.length > index) {
                return Optional.of(Double.parseDouble(args[index]));
            }
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    protected Optional<String> parseStringArg(@NotNull String[] args, int index) {
        if (args.length > index) {
            return Optional.of(args[index]);
        }
        return Optional.empty();
    }

    protected Optional<String> parseGreedyString(@NotNull String[] args, int startIndex) {
        if (args.length > startIndex) {
            final StringJoiner sentence = new StringJoiner(" ");
            for (int i = startIndex; i < args.length; i++) {
                sentence.add(args[i]);
            }
            return Optional.of(sentence.toString().trim());
        }
        return Optional.empty();
    }

}