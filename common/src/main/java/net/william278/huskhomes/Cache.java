package net.william278.huskhomes;

import de.themoep.minedown.adventure.MineDown;
import net.william278.huskhomes.command.CommandBase;
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.database.Database;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.User;
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

    private final HuskHomes plugin;
    private final Map<UUID, List<String>> homes = new HashMap<>();
    private final Map<String, List<String>> publicHomes = new HashMap<>();
    private final List<String> warps = new ArrayList<>();
    private final Set<String> players = new HashSet<>();
    private final Map<String, PaginatedList> privateHomeLists = new HashMap<>();
    private final Map<UUID, PaginatedList> publicHomeLists = new HashMap<>();
    private final Map<UUID, PaginatedList> warpLists = new HashMap<>();
    private final Set<UUID> currentlyOnWarmup = new HashSet<>();

    /**
     * Create a new cache
     */
    public Cache(@NotNull HuskHomes plugin) {
        this.plugin = plugin;
        this.initialize();
    }

    /**
     * Initialize the cache with public home and warp names
     */
    public void initialize() {
        final Database database = plugin.getDatabase();
        plugin.runAsync(() -> {
            database.getPublicHomes().forEach(home -> {
                this.getPublicHomes().putIfAbsent(home.getOwner().getUsername(), new ArrayList<>());
                this.getPublicHomes().get(home.getOwner().getUsername()).add(home.getMeta().getName());
            });
            database.getWarps().forEach(warp -> this.getWarps().add(warp.getMeta().getName()));
        });
    }

    /**
     * Updates the cached list of online players and returns it
     *
     * @param plugin the implementing plugin
     */
    public CompletableFuture<Set<String>> updatePlayerListCache(@NotNull HuskHomes plugin, @NotNull OnlineUser requester) {
        getPlayers().clear();
        getPlayers().addAll(plugin.getOnlineUsers()
                .stream()
                .filter(player -> !player.isVanished())
                .map(onlineUser -> onlineUser.getUsername())
                .toList());

        if (plugin.getSettings().isCrossServer()) {
            return plugin.getMessenger()
                    .getOnlinePlayerNames(requester)
                    .thenApply(networkedPlayers -> {
                        getPlayers().addAll(Set.of(networkedPlayers));
                        return getPlayers();
                    });
        }
        return CompletableFuture.completedFuture(getPlayers());
    }

    /**
     * Returns if the given user is currently warming up to teleport to a home.
     *
     * @param userUuid The user to check.
     * @return If the user is currently warming up.
     * @since 3.1
     */
    public boolean isWarmingUp(@NotNull UUID userUuid) {
        return this.getCurrentlyOnWarmup().contains(userUuid);
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
        if (plugin.getEventDispatcher().dispatchViewHomeListEvent(homes, onlineUser, false).join().isCancelled()) {
            return Optional.empty();
        }
        final String homeListArguments = !onlineUser.equals(homeOwner) ? " " + homeOwner.getUsername() : "";
        final PaginatedList homeList = PaginatedList.of(homes.stream().map(home ->
                locales.getRawLocale("home_list_item",
                                Locales.escapeMineDown(home.getMeta().getName()),
                                Locales.escapeMineDown(home.getOwner().getUsername() + "." + home.getMeta().getName()),
                                Locales.escapeMineDown(locales.formatDescription(home.getMeta().getDescription())))
                        .orElse(home.getMeta().getName())).sorted().collect(Collectors.toList()), getBaseList(locales, itemsPerPage)
                .setHeaderFormat(locales.getRawLocale("home_list_page_title",
                        homeOwner.getUsername(), "%first_item_on_page_index%",
                        "%last_item_on_page_index%", "%total_items%").orElse(""))
                .setCommand("/huskhomes:homelist" + homeListArguments).build());
        this.getPrivateHomeLists().put(homeOwner.getUsername(), homeList);
        return Optional.of(homeList.getNearestValidPage(page));
    }

    @NotNull
    public Optional<MineDown> getPublicHomeList(@NotNull OnlineUser onlineUser, @NotNull Locales locales,
                                                @NotNull List<Home> publicHomes, final int itemsPerPage, final int page) {
        if (plugin.getEventDispatcher().dispatchViewHomeListEvent(publicHomes, onlineUser, true).join().isCancelled()) {
            return Optional.empty();
        }
        final PaginatedList publicHomeList = PaginatedList.of(publicHomes.stream().map(home ->
                locales.getRawLocale("public_home_list_item",
                                Locales.escapeMineDown(home.getMeta().getName()),
                                Locales.escapeMineDown(home.getOwner().getUsername() + "." + home.getMeta().getName()),
                                Locales.escapeMineDown(home.getOwner().getUsername()),
                                Locales.escapeMineDown(locales.formatDescription(home.getMeta().getDescription())))
                        .orElse(home.getMeta().getName())).sorted().collect(Collectors.toList()), getBaseList(locales, itemsPerPage)
                .setHeaderFormat(locales.getRawLocale("public_home_list_page_title",
                        "%first_item_on_page_index%", "%last_item_on_page_index%",
                        "%total_items%").orElse(""))
                .setCommand("/huskhomes:publichomelist").build());
        getPublicHomeLists().put(onlineUser.getUuid(), publicHomeList);
        return Optional.of(publicHomeList.getNearestValidPage(page));
    }

    @NotNull
    public Optional<MineDown> getWarpList(@NotNull OnlineUser onlineUser, @NotNull Locales locales,
                                          @NotNull List<Warp> warps, final int itemsPerPage, final int page) {
        if (plugin.getEventDispatcher().dispatchViewWarpListEvent(warps, onlineUser).join().isCancelled()) {
            return Optional.empty();
        }
        final PaginatedList warpList = PaginatedList.of(warps.stream()
                .map(warp -> locales.getRawLocale("warp_list_item",
                                Locales.escapeMineDown(warp.getMeta().getName()),
                                Locales.escapeMineDown(locales.formatDescription(warp.getMeta().getDescription())))
                        .orElse(warp.getMeta().getName())).sorted().collect(Collectors.toList()), getBaseList(locales, itemsPerPage)
                .setHeaderFormat(locales.getRawLocale("warp_list_page_title",
                        "%first_item_on_page_index%", "%last_item_on_page_index%",
                        "%total_items%").orElse(""))
                .setCommand("/huskhomes:warplist").build());
        getWarpLists().put(onlineUser.getUuid(), warpList);
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

    /**
     * Cached home names - maps a {@link UUID} to a list of their homes
     */
    public Map<UUID, List<String>> getHomes() {
        return homes;
    }

    /**
     * Cached home names - maps a username to a list of their public homes
     */
    public Map<String, List<String>> getPublicHomes() {
        return publicHomes;
    }

    /**
     * Cached warp names
     */
    public List<String> getWarps() {
        return warps;
    }

    /**
     * Cached player list
     */
    public Set<String> getPlayers() {
        return players;
    }

    /**
     * Cached lists of private homes for pagination, mapped to the username of the home owner
     */
    public Map<String, PaginatedList> getPrivateHomeLists() {
        return privateHomeLists;
    }

    /**
     * Cached lists of public homes for pagination
     */
    public Map<UUID, PaginatedList> getPublicHomeLists() {
        return publicHomeLists;
    }

    /**
     * Cached lists of warps for pagination
     */
    public Map<UUID, PaginatedList> getWarpLists() {
        return warpLists;
    }

    /**
     * Cached user UUIDs currently on warmup countdowns for {@link TimedTeleport}s
     */
    public Set<UUID> getCurrentlyOnWarmup() {
        return currentlyOnWarmup;
    }
}
