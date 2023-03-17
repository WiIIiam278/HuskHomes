package net.william278.huskhomes.command;

import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.ConsoleUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface TabProvider {

    @Nullable
    List<String> suggest(@NotNull CommandUser user, @NotNull String[] args);

    @NotNull
    default List<String> getSuggestions(@NotNull CommandUser user, @NotNull String[] args) {
        List<String> suggestions = suggest(user, args);
        if (suggestions == null) {
            suggestions = List.of();
        }
        return filter(suggestions, args);
    }

    @NotNull
    default List<String> filter(@NotNull List<String> suggestions, @NotNull String[] args) {
        return suggestions.stream()
                .filter(suggestion -> args.length == 0 || suggestion.toLowerCase()
                        .startsWith(args[args.length - 1].toLowerCase().trim()))
                .toList();
    }

    @NotNull
    static List<String> getMatchingNames(@Nullable String argument, @NotNull CommandUser user,
                                         @NotNull List<? extends Node> providers) {
        return providers.stream()
                .filter(command -> !(user instanceof ConsoleUser) || command.isConsoleExecutable())
                .map(Node::getName)
                .filter(commandName -> argument == null || argument.isBlank() || commandName.toLowerCase()
                        .startsWith(argument.toLowerCase().trim()))
                .toList();
    }

}