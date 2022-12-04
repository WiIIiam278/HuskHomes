package net.william278.huskhomes;

import de.themoep.minedown.adventure.MineDown;
import net.william278.huskhomes.command.CommandBase;
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.database.Database;
import net.william278.huskhomes.event.EventDispatcher;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.player.User;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.teleport.TimedTeleport;
import net.william278.paginedown.ListOptions;
import net.william278.paginedown.PaginatedList;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * A cache used to hold frequently accessed data (i.e. TAB-completed homes and warps)
 */
public class Cache {

    /**
     * Cached home names - maps a {@link UUID} to a list of their homes
     */
    public final HashMap<UUID, List<String>> homes;

    /**
     * Cached home names - maps a username to a list of their public homes
     */
    public final HashMap<String, List<String>> publicHomes;

    /**
     * Cached warp names
     */
    public final List<String> warps;

    /**
     * Cached player list
     */
    public final List<String> players;

    /**
     * Cached lists of private homes for pagination, mapped to the username of the home owner
     */
    public final HashMap<String, PaginatedList> privateHomeLists;

    /**
     * Cached lists of public homes for pagination
     */
    public final HashMap<UUID, PaginatedList> publicHomeLists;

    /**
     * Cached lists of warps for pagination
     */
    public final HashMap<UUID, PaginatedList> warpLists;

    /**
     * Cached user UUIDs currently on warmup countdowns for {@link TimedTeleport}s
     */
    public final HashSet<UUID> currentlyOnWarmup;

    /**
     * Plugin event dispatcher
     */
    private final EventDispatcher eventDispatcher;

    /**
     * Create a new cache
     */
    public Cache(@NotNull EventDispatcher eventDispatcher) {
        this.homes = new HashMap<>();
        this.publicHomes = new HashMap<>();
        this.warps = new ArrayList<>();
        this.players = new ArrayList<>();
        this.privateHomeLists = new HashMap<>();
        this.publicHomeLists = new HashMap<>();
        this.warpLists = new HashMap<>();
        this.currentlyOnWarmup = new HashSet<>();
        this.eventDispatcher = eventDispatcher;
    }

    /**
     * Initialize the cache, request basic data to load into memory
     *
     * @param database the database to load data from
     */
    public void initialize(@NotNull Database database) {
        CompletableFuture.runAsync(() -> {
            database.getPublicHomes().thenAccept(publicHomeList -> publicHomeList.forEach(home -> {
                this.publicHomes.putIfAbsent(home.owner.username, new ArrayList<>());
                this.publicHomes.get(home.owner.username).add(home.meta.name);
            }));
            database.getWarps().thenAccept(warpsList -> warpsList.forEach(warp ->
                    this.warps.add(warp.meta.name)));
        });
    }

    /**
     * Updates the cached list of online players and returns it
     *
     * @param plugin the implementing plugin
     */
    public CompletableFuture<List<String>> updatePlayerListCache(@NotNull HuskHomes plugin, @NotNull OnlineUser requester) {
        if (plugin.getSettings().crossServer) {
            return plugin.getMessenger().getOnlinePlayerNames(requester).thenApply(returnedPlayerList -> {
                players.clear();
                players.addAll(List.of(returnedPlayerList));
                return players;
            });
        } else {
            players.clear();
            players.addAll(plugin.getOnlinePlayers()
                    .stream()
                    .filter(player -> !player.isVanished())
                    .map(onlineUser -> onlineUser.username)
                    .toList());
            return CompletableFuture.completedFuture(players);
        }
    }

    /**
     * Returns if the given user is currently warming up to teleport to a home.
     *
     * @param userUuid The user to check.
     * @return If the user is currently warming up.
     * @since 3.1
     */
    public boolean isWarmingUp(@NotNull UUID userUuid) {
        return this.currentlyOnWarmup.contains(userUuid);
    }

    @NotNull
    private ListOptions.Builder getBaseList(@NotNull Locales locales, int itemsPerPage) {
        return new ListOptions.Builder().setFooterFormat(locales.getRawLocale("list_footer",
                        "%previous_page_button%", "%current_page%",
                        "%total_pages%", "%next_page_button%", "%page_jumpers%").orElse(""))
                .setNextButtonFormat(locales.getRawLocale("list_next_page_button",
                        "%next_page_index%", "%command%").orElse(""))
                .setPreviousButtonFormat(locales.getRawLocale("list_previous_page_button",
                        "%previous_page_index%", "%command%").orElse(""))
                .setPageJumpersFormat(locales.getRawLocale("list_page_jumpers",
                        "%page_jump_buttons%").orElse(""))
                .setPageJumperPageFormat(locales.getRawLocale("list_page_jumper_button",
                        "%target_page_index%", "%command%").orElse(""))
                .setPageJumperCurrentPageFormat(locales.getRawLocale("list_page_jumper_current_page",
                        "%current_page%").orElse(""))
                .setPageJumperPageSeparator(locales.getRawLocale("list_page_jumper_separator").orElse(""))
                .setPageJumperGroupSeparator(locales.getRawLocale("list_page_jumper_group_separator").orElse(""))
                .setItemSeparator(locales.getRawLocale("list_item_divider").orElse(" "))
                .setItemsPerPage(itemsPerPage)
                .setEscapeItemsMineDown(false)
                .setSpaceAfterHeader(false)
                .setSpaceBeforeFooter(false);
    }

    @NotNull
    public Optional<MineDown> getHomeList(@NotNull OnlineUser onlineUser, @NotNull User homeOwner, @NotNull Locales locales,
                                          @NotNull List<Home> homes, final int itemsPerPage, final int page) {
        if (eventDispatcher.dispatchViewHomeListEvent(homes, onlineUser, false).join().isCancelled()) {
            return Optional.empty();
        }
        final String homeListArguments = !onlineUser.equals(homeOwner) ? " " + homeOwner.username : "";
        final PaginatedList homeList = PaginatedList.of(homes.stream().map(home ->
                locales.getRawLocale("home_list_item",
                                Locales.escapeMineDown(home.meta.name),
                                Locales.escapeMineDown(home.owner.username + "." + home.meta.name),
                                Locales.escapeMineDown(locales.formatDescription(home.meta.description)))
                        .orElse(home.meta.name)).sorted().collect(Collectors.toList()), getBaseList(locales, itemsPerPage)
                .setHeaderFormat(locales.getRawLocale("home_list_page_title",
                        homeOwner.username, "%first_item_on_page_index%",
                        "%last_item_on_page_index%", "%total_items%").orElse(""))
                .setCommand("/huskhomes:homelist" + homeListArguments).build());
        this.privateHomeLists.put(homeOwner.username, homeList);
        return Optional.of(homeList.getNearestValidPage(page));
    }

    @NotNull
    public Optional<MineDown> getPublicHomeList(@NotNull OnlineUser onlineUser, @NotNull Locales locales,
                                                @NotNull List<Home> publicHomes, final int itemsPerPage, final int page) {
        if (eventDispatcher.dispatchViewHomeListEvent(publicHomes, onlineUser, true).join().isCancelled()) {
            return Optional.empty();
        }
        final PaginatedList publicHomeList = PaginatedList.of(publicHomes.stream().map(home ->
                locales.getRawLocale("public_home_list_item",
                                Locales.escapeMineDown(home.meta.name),
                                Locales.escapeMineDown(home.owner.username + "." + home.meta.name),
                                Locales.escapeMineDown(home.owner.username),
                                Locales.escapeMineDown(locales.formatDescription(home.meta.description)))
                        .orElse(home.meta.name)).sorted().collect(Collectors.toList()), getBaseList(locales, itemsPerPage)
                .setHeaderFormat(locales.getRawLocale("public_home_list_page_title",
                        "%first_item_on_page_index%", "%last_item_on_page_index%",
                        "%total_items%").orElse(""))
                .setCommand("/huskhomes:publichomelist").build());
        publicHomeLists.put(onlineUser.uuid, publicHomeList);
        return Optional.of(publicHomeList.getNearestValidPage(page));
    }

    @NotNull
    public Optional<MineDown> getWarpList(@NotNull OnlineUser onlineUser, @NotNull Locales locales,
                                          @NotNull List<Warp> warps, final int itemsPerPage, final int page) {
        if (eventDispatcher.dispatchViewWarpListEvent(warps, onlineUser).join().isCancelled()) {
            return Optional.empty();
        }
        final PaginatedList warpList = PaginatedList.of(warps.stream()
                .map(warp -> locales.getRawLocale("warp_list_item",
                                Locales.escapeMineDown(warp.meta.name),
                                Locales.escapeMineDown(locales.formatDescription(warp.meta.description)))
                        .orElse(warp.meta.name)).sorted().collect(Collectors.toList()), getBaseList(locales, itemsPerPage)
                .setHeaderFormat(locales.getRawLocale("warp_list_page_title",
                        "%first_item_on_page_index%", "%last_item_on_page_index%",
                        "%total_items%").orElse(""))
                .setCommand("/huskhomes:warplist").build());
        warpLists.put(onlineUser.uuid, warpList);
        return Optional.of(warpList.getNearestValidPage(page));
    }

    @NotNull
    public MineDown getCommandList(@NotNull OnlineUser onlineUser, @NotNull Locales locales,
                                   @NotNull List<CommandBase> commands, final int itemsPerPage, final int page) {
        return PaginatedList.of(commands.stream()
                                .filter(command -> onlineUser.hasPermission(command.permission))
                                .map(command -> locales.getRawLocale("command_list_item",
                                                Locales.escapeMineDown(command.command),
                                                Locales.escapeMineDown(command.getDescription().length() > 50
                                                        ? command.getDescription().substring(0, 49).trim() + "…"
                                                        : command.getDescription()),
                                                Locales.escapeMineDown(locales.formatDescription(command.getDescription())))
                                        .orElse(command.command))
                                .collect(Collectors.toList()),
                        getBaseList(locales, Math.min(itemsPerPage, 6))
                                .setHeaderFormat(locales.getRawLocale("command_list_title").orElse(""))
                                .setItemSeparator("\n").setCommand("/huskhomes:huskhomes help").build())
                .getNearestValidPage(page);
    }

}
