package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.player.UserData;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.PositionMeta;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SetHomeCommand extends CommandBase {

    protected SetHomeCommand(@NotNull HuskHomes implementor) {
        super("sethome", "[name]", Permission.COMMAND_SET_HOME, implementor);
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        plugin.getDatabase().getHomes(onlineUser).thenAcceptAsync(homes -> {
            switch (args.length) {
                case 0 -> {
                    if (homes.isEmpty()) {
                        setHome(onlineUser, "home", homes);
                        return;
                    }
                    onlineUser.sendMessage(getSyntaxErrorMessage());
                }
                case 1 -> setHome(onlineUser, args[0], homes);
                default -> onlineUser.sendMessage(getSyntaxErrorMessage());
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

        // Get their user data
        plugin.getDatabase().getUserData(onlineUser.uuid).thenAccept(fetchedData -> {
            // Check against economy if needed
            final AtomicBoolean newSlotNeeded = new AtomicBoolean(false);
            final AtomicReference<UserData> userDataToUpdate = new AtomicReference<>(null);
            if (plugin.getSettings().economy) {
                final int freeHomes = onlineUser.getFreeHomes(plugin.getSettings().freeHomeSlots,
                        plugin.getSettings().stackPermissionLimits);
                if (fetchedData.isPresent()) {
                    newSlotNeeded.set((currentHomes.size() + 1) > (freeHomes + fetchedData.get().homeSlots()));

                    // If a new slot is needed, validate the user has enough funds to purchase one
                    if (newSlotNeeded.get()) {
                        if (!plugin.validateEconomyCheck(onlineUser, Settings.EconomyAction.ADDITIONAL_HOME_SLOT)) {
                            return;
                        }
                        userDataToUpdate.set(new UserData(onlineUser, (currentHomes.size() + 1) - freeHomes,
                                fetchedData.get().ignoringTeleports(), fetchedData.get().rtpCooldown()));
                    } else {
                        if (currentHomes.size() == freeHomes) {
                            plugin.getLocales().getLocale("set_home_used_free_slots", Integer.toString(freeHomes))
                                    .ifPresent(onlineUser::sendMessage);
                        }
                    }
                }
            }

            // Set the home in the saved position manager
            plugin.getSavedPositionManager()
                    .setHome(new PositionMeta(homeName, ""), onlineUser, onlineUser.getPosition())
                    .thenAccept(setResult -> {
                        // Display feedback of the result of the set operation
                        (switch (setResult.resultType()) {
                            case SUCCESS -> {
                                assert setResult.savedPosition().isPresent();

                                // If the user needed to buy a new slot, perform the transaction and update their data
                                if (newSlotNeeded.get()) {
                                    plugin.performEconomyTransaction(onlineUser, Settings.EconomyAction.ADDITIONAL_HOME_SLOT);
                                    plugin.getDatabase().updateUserData(userDataToUpdate.get());
                                }
                                yield plugin.getLocales().getLocale("set_home_success", setResult.savedPosition().get().meta.name);
                            }
                            case SUCCESS_OVERWRITTEN -> {
                                assert setResult.savedPosition().isPresent();
                                yield plugin.getLocales().getLocale("edit_home_update_location",
                                        setResult.savedPosition().get().meta.name);
                            }
                            case FAILED_DUPLICATE -> plugin.getLocales().getLocale("error_home_name_taken");
                            case FAILED_NAME_LENGTH -> plugin.getLocales().getLocale("error_home_name_length");
                            case FAILED_NAME_CHARACTERS -> plugin.getLocales().getLocale("error_home_name_characters");
                            default -> plugin.getLocales().getLocale("error_home_description_characters");
                        }).ifPresent(onlineUser::sendMessage);
                    });


        });
    }

}
