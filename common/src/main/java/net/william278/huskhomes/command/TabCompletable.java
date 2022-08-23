package net.william278.huskhomes.command;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Interface providing tab completions for a command
 */
public interface TabCompletable {

    /**
     * What should be returned when the player attempts to TAB-complete the command
     *
     * @param args Current command arguments
     * @return List of String arguments to offer TAB suggestions
     */
    @NotNull
    List<String> onTabComplete(@NotNull String[] args);

}
