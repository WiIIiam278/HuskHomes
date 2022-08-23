package net.william278.huskhomes.command;

import de.themoep.minedown.MineDown;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.player.UserData;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.PositionMeta;
import net.william278.huskhomes.position.SavedPositionManager;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class SetHomeCommand extends CommandBase {

    protected SetHomeCommand(@NotNull HuskHomes implementor) {
        super("sethome", Permission.COMMAND_SET_HOME, implementor);
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        plugin.getDatabase().getHomes(onlineUser).thenAccept(homes -> {
            switch (args.length) {
                case 0 -> {
                    if (homes.isEmpty()) {
                        setHome(onlineUser, "home", homes);
                    } else {
                        plugin.getLocales().getLocale("error_invalid_syntax", "/sethome <name>")
                                .ifPresent(onlineUser::sendMessage);
                    }
                }
                case 1 -> setHome(onlineUser, args[0], homes);
                default -> plugin.getLocales().getLocale("error_invalid_syntax", "/sethome <name>")
                        .ifPresent(onlineUser::sendMessage);
            }
        });
    }

    /**
     * Attempts to set a home by given name for the {@link OnlineUser}.
     * <p>
     * A number of validation checks will take place before the home is set. If these checks fail, the home won't be set.
     * <ul>
     *     <li>The user's currentHomes must not exceed the permissive maximum home limit</li>
     *     <li>If economy features are on and the user does not have enough home slots, they must have sufficient funds to buy another</li>
     *     <li>The home name must not already exist</li>
     *     <li>The home name must meet the length and character criteria</li>
     * </ul>
     *
     * @param onlineUser   The {@link OnlineUser} to set the home for
     * @param homeName     The name of the home to set
     * @param currentHomes The current homes of the {@link OnlineUser}
     */
    private void setHome(@NotNull OnlineUser onlineUser, @NotNull String homeName, @NotNull List<Home> currentHomes) {
        // Check against maximum homes
        final int maxHomes = onlineUser.getMaxHomes(plugin.getSettings().maxHomes, plugin.getSettings().stackPermissionLimits);
        if (currentHomes.size() >= maxHomes) {
            plugin.getLocales().getLocale("error_set_home_maximum_homes", Integer.toString(maxHomes))
                    .ifPresent(onlineUser::sendMessage);
            return;
        }

        // Check against economy if needed
        boolean newSlotNeeded = false;
        UserData userDataToUpdate = null;
        if (plugin.getSettings().economy) {
            final int freeHomes = onlineUser.getFreeHomes(plugin.getSettings().freeHomeSlots, plugin.getSettings().stackPermissionLimits);
            final Optional<UserData> fetchedData = plugin.getDatabase().getUserData(onlineUser.uuid).join();
            if (fetchedData.isPresent()) {
                newSlotNeeded = (currentHomes.size() + 1) > (freeHomes + fetchedData.get().homeSlots());
            }

            // If a new slot is needed, validate the user has enough funds to purchase one
            if (newSlotNeeded) {
                if (!plugin.validateEconomyCheck(onlineUser, Settings.EconomyAction.ADDITIONAL_HOME_SLOT)) {
                    return;
                }
                userDataToUpdate = new UserData(onlineUser, (currentHomes.size() + 1) - freeHomes,
                        fetchedData.get().ignoringTeleports(), fetchedData.get().rtpCooldown());
            }
        }

        // Set the home in the saved position manager
        final SavedPositionManager.SaveResult setResult = plugin.getSavedPositionManager()
                .setHome(new PositionMeta(homeName, ""), onlineUser, onlineUser.getPosition()).join();

        // Display feedback of the result of the set operation
        (switch (setResult.resultType()) {
            case SUCCESS -> {
                assert setResult.savedPosition().isPresent();

                // If the user needed to buy a new slot, perform the transaction and update their data
                if (newSlotNeeded) {
                    plugin.performEconomyTransaction(onlineUser, Settings.EconomyAction.ADDITIONAL_HOME_SLOT);
                    plugin.getDatabase().updateUserData(userDataToUpdate).join();
                }
                yield plugin.getLocales().getLocale("set_home_success", setResult.savedPosition().get().meta.name);
            }
            case FAILED_DUPLICATE -> plugin.getLocales().getLocale("error_home_name_taken");
            case FAILED_NAME_LENGTH -> plugin.getLocales().getLocale("error_home_name_length");
            case FAILED_NAME_CHARACTERS -> plugin.getLocales().getLocale("error_home_name_characters");
            default -> Optional.of(new MineDown(""));
        }).ifPresent(onlineUser::sendMessage);
    }

}
