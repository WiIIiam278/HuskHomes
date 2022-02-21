package me.william278.huskhomes2.commands;

import de.themoep.minedown.MineDown;
import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.util.MessageManager;
import me.william278.huskhomes2.teleport.TeleportManager;
import me.william278.huskhomes2.teleport.points.TeleportationPoint;
import me.william278.huskhomes2.util.NameAutoCompleter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TpCommand extends CommandBase {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    private static final HashSet<TpCommandInputHandler> inputHandlers = new HashSet<>();

    public TpCommand() {
        inputHandlers.add(new TpCommandInputHandler(false, "<target>"));
        inputHandlers.add(new TpCommandInputHandler(true, "<player>", "<target>"));
        inputHandlers.add(new TpCommandInputHandler(false, "<x>", "<y>", "<z>"));
        inputHandlers.add(new TpCommandInputHandler(false, "<x>", "<y>", "<z>", "<world>"));
        inputHandlers.add(new TpCommandInputHandler(false, "<x>", "<y>", "<z>", "<yaw>", "<pitch>"));
        inputHandlers.add(new TpCommandInputHandler(false, "<x>", "<y>", "<z>", "<yaw>", "<pitch>", "<world>"));
        inputHandlers.add(new TpCommandInputHandler(true, "<player>", "<x>", "<y>", "<z>"));
        inputHandlers.add(new TpCommandInputHandler(true, "<player>", "<x>", "<y>", "<z>", "<world>"));
        inputHandlers.add(new TpCommandInputHandler(true, "<player>", "<x>", "<y>", "<z>", "<yaw>", "<pitch>"));
        inputHandlers.add(new TpCommandInputHandler(true, "<player>", "<x>", "<y>", "<z>", "<yaw>", "<pitch>", "<world>"));

        if (HuskHomes.getSettings().doBungee()) {
            inputHandlers.add(new TpCommandInputHandler(false, "<x>", "<y>", "<z>", "<world>", "<server>"));
            inputHandlers.add(new TpCommandInputHandler(true, "<player>", "<x>", "<y>", "<z>", "<world>", "<server>"));
            inputHandlers.add(new TpCommandInputHandler(true, "<player>", "<x>", "<y>", "<z>", "<yaw>", "<pitch>", "<world>", "<server>"));
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        TpCommandInputHandler.HandlerResponse response = null;
        for (TpCommandInputHandler inputHandler : inputHandlers) {
            response = inputHandler.handleInput(args, sender);
            switch (response) {
                case CAN_HANDLE -> {
                    inputHandler.executeTeleport(sender);
                    return true;
                }
                case CANNOT_HANDLE_HANDLED_BY_TP_HERE -> {
                    return true;
                }
            }
        }
        assert response != null;
        switch (response) {
            case CANNOT_HANDLE_BY_CONSOLE -> {
                sender.spigot().sendMessage(new MineDown(MessageManager.getRawMessage("error_invalid_syntax",
                        "/tp " + getClosestUsage(true, sender, args))).toComponent());
                return true;
            }
            case CANNOT_HANDLE_INVALID_WORLD -> {
                sender.spigot().sendMessage(new MineDown(MessageManager.getRawMessage("error_tp_invalid_world")).toComponent());
                return true;
            }
            case CANNOT_HANDLE_INVALID_PLAYER -> {
                sender.spigot().sendMessage(new MineDown(MessageManager.getRawMessage("error_invalid_player")).toComponent());
                return true;
            }
        }
        sender.spigot().sendMessage(new MineDown(MessageManager.getRawMessage("error_invalid_syntax",
                "/tp " + getClosestUsage((!(sender instanceof Player)), sender, args))).toComponent());
        return true;
    }

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        // Accept console input
    }

    public static class Tab implements TabCompleter {
        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
            String[] closestArgs = getClosestArgs(false, args);
            final int currentArgIndex = Math.max(args.length - 1, 0);
            if (currentArgIndex >= closestArgs.length) {
                return Collections.emptyList();
            }

            final ArrayList<String> possibleTabCompletions = new ArrayList<>();
            final String argToReturn = closestArgs[currentArgIndex];

            switch (argToReturn) {
                case "<x>", "<y>", "<z>" -> possibleTabCompletions.add("~");
                case "<yaw>", "<pitch>" -> {
                    for (int i = -180; i <= 180; i += 45) {
                        possibleTabCompletions.add(Integer.toString(i));
                    }
                }
                case "<player>" -> {
                    final ArrayList<String> onlinePlayers = new ArrayList<>();
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        onlinePlayers.add(player.getName());
                    }
                    StringUtil.copyPartialMatches(args[currentArgIndex], onlinePlayers, possibleTabCompletions);
                }
                case "<target>" -> {
                    final ArrayList<String> globalPlayers = new ArrayList<>(HuskHomes.getPlayerList().getPlayers());
                    StringUtil.copyPartialMatches(args[currentArgIndex], globalPlayers, possibleTabCompletions);
                }
                case "<world>" -> {
                    final ArrayList<String> worlds = new ArrayList<>();
                    for (World world : Bukkit.getWorlds()) {
                        worlds.add(world.getName());
                    }
                    StringUtil.copyPartialMatches(args[currentArgIndex], worlds, possibleTabCompletions);
                }
                default -> possibleTabCompletions.add(argToReturn);
            }

            Collections.sort(possibleTabCompletions);
            return possibleTabCompletions;
        }
    }

    public static String getClosestUsage(boolean filterOnlyConsoleExecutable, CommandSender sender, String... inputArgs) {
        StringJoiner joiner = new StringJoiner(" ");
        for (String argument : getClosestArgs(filterOnlyConsoleExecutable, inputArgs)) {
            joiner.add(argument);
        }
        return joiner.toString();
    }

    public static String[] getClosestArgs(boolean filterOnlyConsoleExecutable, String... inputArgs) {
        String[] closestArgs = new String[]{"[player] [<x>|<target>] [<y>] [<z>] [yaw] [pitch] [world] [server]"};
        int difference = Integer.MAX_VALUE;
        for (TpCommandInputHandler handler : inputHandlers) {
            if (filterOnlyConsoleExecutable) {
                if (!handler.consoleExecutable) {
                    continue;
                }
            }
            int argLengthDifference = handler.arguments.length - inputArgs.length;
            if (argLengthDifference < 0) {
                continue;
            }
            if (argLengthDifference < difference) {
                closestArgs = handler.arguments;
            }
        }
        return closestArgs;
    }

    public static class TpCommandInputHandler {

        private String target;
        private Player player;
        private Double x;
        private Double y;
        private Double z;
        private Float yaw;
        private Float pitch;
        private String worldName;
        private String serverId;

        private String tpHereTarget;

        public final String[] arguments;
        public final boolean consoleExecutable;

        public TpCommandInputHandler(boolean consoleExecutable, String... arguments) {
            this.consoleExecutable = consoleExecutable;
            this.arguments = arguments;
        }

        public HandlerResponse handleInput(String[] inputArguments, CommandSender inputExecutor) {
            if (!(inputExecutor instanceof Player) && !consoleExecutable) {
                return HandlerResponse.CANNOT_HANDLE_BY_CONSOLE;
            }
            if (inputArguments.length == arguments.length) {
                return getHandleable(inputArguments, inputExecutor);
            }
            return HandlerResponse.CANNOT_HANDLE_SYNTAX_LENGTH_MISMATCH;
        }

        private HandlerResponse getHandleable(String[] inputArguments, CommandSender inputExecutor) {
            int argumentIndex = 0;
            for (String inputArgument : inputArguments) {
                if (argumentIndex >= arguments.length) break;
                final HandlerResponse argumentResponse = handleArg(inputArgument, argumentIndex, inputExecutor);
                if (argumentResponse != HandlerResponse.CAN_HANDLE) {
                    return argumentResponse;
                }
                argumentIndex++;
            }
            if (player == null) {
                if (inputExecutor instanceof Player commandDispatcher) {
                    player = commandDispatcher;
                } else {
                    return HandlerResponse.CANNOT_HANDLE_BY_CONSOLE;
                }
            }
            return HandlerResponse.CAN_HANDLE;
        }

        public void executeTeleport(CommandSender sender) {
            // Handle teleportation to a target player
            if (target != null) {
                final String targetName = NameAutoCompleter.getAutoCompletedName(target);
                TeleportManager.teleportPlayer(player, targetName);
                if (sender instanceof Player sendingPlayer) {
                    if (!sendingPlayer.getUniqueId().equals(player.getUniqueId())) {
                        sender.spigot().sendMessage(new MineDown(MessageManager.getRawMessage("teleporting_complete")).toComponent());
                    }
                } else {
                    sender.spigot().sendMessage(new MineDown(MessageManager.getRawMessage("teleporting_complete")).toComponent());
                    player.spigot().sendMessage(new MineDown(MessageManager.getRawMessage("teleporting_complete_console", targetName)).toComponent());
                }
                return;
            }

            // Handle teleportation
            final TeleportationPoint teleportationPoint = new TeleportationPoint(
                    (worldName != null ? worldName : player.getWorld().getName()),
                    (x != null ? x : player.getLocation().getX()),
                    (y != null ? y : player.getLocation().getY()),
                    (z != null ? z : player.getLocation().getZ()),
                    (yaw != null ? yaw : player.getLocation().getYaw()),
                    (pitch != null ? pitch : player.getLocation().getPitch()),
                    serverId != null || HuskHomes.getSettings().doBungee() ? serverId : HuskHomes.getSettings().getServerID());

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                TeleportManager.teleportPlayer(player, teleportationPoint);
                if (sender instanceof Player sendingPlayer) {
                    if (!sendingPlayer.getUniqueId().equals(player.getUniqueId())) {
                        sender.spigot().sendMessage(new MineDown(MessageManager.getRawMessage("teleporting_complete")).toComponent());
                    }
                } else {
                    sender.spigot().sendMessage(new MineDown(MessageManager.getRawMessage("teleporting_complete")).toComponent());
                    player.spigot().sendMessage(new MineDown(MessageManager.getRawMessage("teleporting_complete_console", teleportationPoint.toString())).toComponent());
                }
            });
        }

        private boolean isCrossServerInput() {
            boolean isCrossServer = false;
            for (String argument : arguments) {
                if (argument.equalsIgnoreCase("<server>")) {
                    isCrossServer = true;
                    break;
                }
            }
            return isCrossServer;
        }

        public HandlerResponse handleArg(String inputArgument, int argumentIndex, CommandSender inputExecutor) {
            final String expectedArgument = arguments[argumentIndex];
            switch (expectedArgument) {
                case "<x>", "<y>", "<z>" -> {
                    try {
                        double value;
                        if (inputArgument.startsWith("~")) {
                            if (inputExecutor instanceof Player player) {
                                value = switch (expectedArgument) {
                                    case "<x>" -> player.getLocation().getX();
                                    case "<y>" -> player.getLocation().getY();
                                    case "<z>" -> player.getLocation().getZ();
                                    default -> 0;
                                };
                            } else {
                                return HandlerResponse.CANNOT_HANDLE_BY_CONSOLE;
                            }
                            if (inputArgument.length() > 1) {
                                inputArgument = inputArgument.substring(1);
                                value += Double.parseDouble(inputArgument);
                            }
                        } else {
                            value = Double.parseDouble(inputArgument);
                        }
                        switch (expectedArgument) {
                            case "<x>" -> x = value;
                            case "<y>" -> y = value;
                            case "<z>" -> z = value;
                        }
                        return HandlerResponse.CAN_HANDLE;
                    } catch (NumberFormatException e) {
                        return HandlerResponse.CANNOT_HANDLE_SYNTAX_MISMATCH;
                    }
                }
                case "<yaw>", "<pitch>" -> {
                    try {
                        float value;
                        if (inputArgument.startsWith("~")) {
                            if (inputExecutor instanceof Player player) {
                                value = switch (expectedArgument) {
                                    case "<yaw>" -> player.getLocation().getYaw();
                                    case "<pitch>" -> player.getLocation().getPitch();
                                    default -> 0;
                                };
                            } else {
                                return HandlerResponse.CANNOT_HANDLE_BY_CONSOLE;
                            }
                            if (inputArgument.length() > 1) {
                                inputArgument = inputArgument.substring(1);
                                value += Double.parseDouble(inputArgument);
                            }
                        } else {
                            value = Float.parseFloat(inputArgument);
                        }
                        switch (expectedArgument) {
                            case "<yaw>" -> yaw = value;
                            case "<pitch>" -> pitch = value;
                        }
                        return HandlerResponse.CAN_HANDLE;
                    } catch (NumberFormatException e) {
                        return HandlerResponse.CANNOT_HANDLE_SYNTAX_MISMATCH;
                    }
                }
                case "<world>" -> {
                    if (isCrossServerInput()) {
                        worldName = inputArgument;
                        return HandlerResponse.CAN_HANDLE;
                    } else {
                        if (Bukkit.getWorld(inputArgument) != null) {
                            worldName = inputArgument;
                            return HandlerResponse.CAN_HANDLE;
                        }
                        return HandlerResponse.CANNOT_HANDLE_INVALID_WORLD;
                    }
                }
                case "<server>" -> {
                    serverId = inputArgument;
                    return HandlerResponse.CAN_HANDLE;
                }
                case "<target>" -> {
                    target = inputArgument;
                    if (arguments.length == 2) {
                        if (inputExecutor instanceof Player playerExecutor) {
                            if (NameAutoCompleter.getAutoCompletedName(target).equalsIgnoreCase(playerExecutor.getName()) && tpHereTarget != null) {
                                TpHereCommand.executeTpHere(playerExecutor, tpHereTarget);
                                return HandlerResponse.CANNOT_HANDLE_HANDLED_BY_TP_HERE;
                            }
                        }
                    }
                    return HandlerResponse.CAN_HANDLE;
                }
                case "<player>" -> {
                    Player teleportingPlayer = Bukkit.getPlayerExact(inputArgument);
                    if (teleportingPlayer != null) {
                        player = teleportingPlayer;
                        return HandlerResponse.CAN_HANDLE;
                    }
                    if (arguments.length == 2) {
                        if (arguments[1].equalsIgnoreCase("<target>")) {
                            tpHereTarget = inputArgument;
                            return HandlerResponse.CAN_HANDLE;
                        }
                    }
                    return HandlerResponse.CANNOT_HANDLE_INVALID_PLAYER;
                }
            }
            return HandlerResponse.CANNOT_HANDLE_INVALID_EXPECTED_ARGUMENT;
        }

        public enum HandlerResponse {
            CAN_HANDLE,
            CANNOT_HANDLE_HANDLED_BY_TP_HERE,
            CANNOT_HANDLE_INVALID_WORLD,
            CANNOT_HANDLE_INVALID_PLAYER,
            CANNOT_HANDLE_BY_CONSOLE,
            CANNOT_HANDLE_SYNTAX_MISMATCH,
            CANNOT_HANDLE_SYNTAX_LENGTH_MISMATCH,
            CANNOT_HANDLE_INVALID_EXPECTED_ARGUMENT
        }
    }
}