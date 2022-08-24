package net.william278.huskhomes;

import de.themoep.minedown.MineDown;
import net.william278.huskhomes.command.CommandBase;
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.database.Database;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.player.User;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Warp;
import net.william278.paginedown.ListOptions;
import net.william278.paginedown.PaginatedList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
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
     * Cached lists of private homes for pagination
     */
    public final HashMap<UUID, PaginatedList> privateHomeLists;

    /**
     * Cached lists of public homes for pagination
     */
    public final HashMap<UUID, PaginatedList> publicHomeLists;

    /**
     * Cached lists of warps for pagination
     */
    public final HashMap<UUID, PaginatedList> warpLists;

    /**
     * Create a new cache
     */
    public Cache() {
        this.homes = new HashMap<>();
        this.publicHomes = new HashMap<>();
        this.warps = new ArrayList<>();
        this.players = new ArrayList<>();
        this.privateHomeLists = new HashMap<>();
        this.publicHomeLists = new HashMap<>();
        this.warpLists = new HashMap<>();
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
     * Updates the cached list of online players
     *
     * @param plugin the implementing plugin
     */
    public void updateOnlinePlayerList(@NotNull HuskHomes plugin, @NotNull OnlineUser requester) {
        if (plugin.getSettings().crossServer) {
            assert plugin.getNetworkMessenger() != null;
            plugin.getNetworkMessenger().getOnlinePlayerNames(requester).thenAccept(returnedPlayerList -> {
                players.clear();
                players.addAll(List.of(returnedPlayerList));
            });
        } else {
            players.clear();
            players.addAll(plugin.getOnlinePlayers().stream().map(onlineUser -> onlineUser.username).toList());
        }
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
                .setEscapeItemsMineDown(false);
    }

    @NotNull
    public MineDown getHomeList(@NotNull OnlineUser onlineUser, @NotNull User listOwner, @NotNull Locales locales,
                                @NotNull List<Home> homes, final int itemsPerPage, final int page) {
        final PaginatedList homeList = PaginatedList.of(homes.stream().map(home ->
                locales.getRawLocale("home_list_item",
                                Locales.escapeMineDown(home.meta.name),
                                Locales.escapeMineDown(home.owner.username + "." + home.meta.name),
                                Locales.escapeMineDown(locales.formatDescription(home.meta.description)))
                        .orElse(home.meta.name)).sorted().collect(Collectors.toList()), getBaseList(locales, itemsPerPage)
                .setHeaderFormat(locales.getRawLocale("home_list_page_title",
                        listOwner.username, "%first_item_on_page_index%",
                        "%last_item_on_page_index%", "%total_items%").orElse(""))
                .setCommand("/huskhomes:homelist").build());
        this.privateHomeLists.put(onlineUser.uuid, homeList);
        return homeList.getNearestValidPage(page);
    }

    @NotNull
    public MineDown getCommandList(@NotNull Locales locales, @NotNull List<CommandBase> commands,
                                   final int itemsPerPage, final int page) {
        return PaginatedList.of(commands.stream().map(command -> locales.getRawLocale("command_list_item",
                                                Locales.escapeMineDown(command.command),
                                                Locales.escapeMineDown(command.getDescription().length() > 50
                                                        ? command.getDescription().substring(0, 49).trim() + "…"
                                                        : command.getDescription()),
                                                Locales.escapeMineDown(locales.formatDescription(command.getDescription())))
                                        .orElse(command.command))
                                .collect(Collectors.toList()),
                        getBaseList(locales, itemsPerPage)
                                .setHeaderFormat(locales.getRawLocale("command_list_title").orElse(""))
                                .setItemSeparator("\n").setCommand("/huskhomes:huskhomes help").build())
                .getNearestValidPage(page);
    }

    @NotNull
    public MineDown getPublicHomeList(@NotNull OnlineUser onlineUser, @NotNull Locales locales,
                                      @NotNull List<Home> publicHomes, final int itemsPerPage, final int page) {
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
        return publicHomeList.getNearestValidPage(page);
    }

    @NotNull
    public MineDown getWarpList(@NotNull OnlineUser onlineUser, @NotNull Locales locales,
                                @NotNull List<Warp> warps, final boolean permissionRestrictWarps,
                                final int itemsPerPage, final int page) {
        final PaginatedList warpList = PaginatedList.of(warps.stream()
                .filter(warp -> !permissionRestrictWarps || onlineUser.hasPermission(warp.getPermissionNode()))
                .map(warp ->
                        locales.getRawLocale("warp_list_item",
                                        Locales.escapeMineDown(warp.meta.name),
                                        Locales.escapeMineDown(locales.formatDescription(warp.meta.description)))
                                .orElse(warp.meta.name)).sorted().collect(Collectors.toList()), getBaseList(locales, itemsPerPage)
                .setHeaderFormat(locales.getRawLocale("warp_list_page_title",
                        "%first_item_on_page_index%", "%last_item_on_page_index%",
                        "%total_items%").orElse(""))
                .setCommand("/huskhomes:warplist").build());
        warpLists.put(onlineUser.uuid, warpList);
        return warpList.getNearestValidPage(page);
    }

}
