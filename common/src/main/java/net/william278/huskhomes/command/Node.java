package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.teleport.Teleportable;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.ConsoleUser;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;

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
    public String getPermission(@NotNull String... child) {
        return new StringJoiner(".")
                .add(PERMISSION_PREFIX)
                .add(getName())
                .add(String.join(".", child))
                .toString();
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

    protected Optional<Float> parseFloatArg(@NotNull String[] args, int index) {
        try {
            if (args.length > index) {
                return Optional.of(Float.parseFloat(args[index]));
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

    protected Optional<Double> parseCoordinateArg(@NotNull String[] args, int index, double relativeTo) {
        try {
            if (args.length > index) {
                final String arg = args[index];
                if (arg.startsWith("~")) {
                    final String coordinate = arg.substring(1);
                    if (coordinate.isBlank()) {
                        return Optional.of(relativeTo);
                    }
                    return Optional.of(relativeTo + Double.parseDouble(coordinate));
                }
                return Optional.of(Double.parseDouble(arg));
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

    protected Optional<World> parseWorldArg(@NotNull String[] args, int index) {
        if (args.length > index) {
            final String worldName = args[index];
            final World.Environment environment = worldName.endsWith("_the_end") ? World.Environment.THE_END :
                    worldName.endsWith("nether") ? World.Environment.NETHER : World.Environment.OVERWORLD;
            return Optional.of(plugin.getWorlds().stream()
                    .filter(world -> world.getName().equalsIgnoreCase(worldName))
                    .findFirst()
                    .orElse(new World(worldName, new UUID(0, 0), environment)));
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

    protected Position getBasePosition(@NotNull CommandUser executor) {
        return executor instanceof OnlineUser user ? user.getPosition() : plugin.getSpawn()
                .orElse(new Position(0, 0, 0, 0, 0, plugin.getWorlds().get(0), plugin.getServerName()));
    }

    protected Optional<Position> parsePositionArgs(@NotNull Position basePosition, @NotNull String[] args, int startIndex) {
        // Parse x, y, and z
        final Optional<Double> x = parseCoordinateArg(args, startIndex, basePosition.getX());
        final Optional<Double> y = parseCoordinateArg(args, startIndex + 1, basePosition.getY());
        final Optional<Double> z = parseCoordinateArg(args, startIndex + 2, basePosition.getZ());
        if (x.isEmpty() || y.isEmpty() || z.isEmpty()) {
            return Optional.empty();
        }

        // Parse world and server name
        final Optional<World> world = parseWorldArg(args, startIndex + 3);
        final Optional<String> server = parseStringArg(args, startIndex + 4);

        // Parse yaw, pitch
        int angleStartIndex = (world.isEmpty() ? 3 : server.isEmpty() ? 4 : 5);
        final Optional<Float> yaw = parseFloatArg(args, startIndex + angleStartIndex);
        final Optional<Float> pitch = parseFloatArg(args, startIndex + angleStartIndex + 1);

        return Optional.of(new Position(x.get(), y.get(), z.get(),
                yaw.orElse(basePosition.getYaw()), pitch.orElse(basePosition.getPitch()),
                world.orElse(basePosition.getWorld()), server.orElse(basePosition.getServer())));
    }


    protected Optional<Teleportable> resolveTeleportable(@NotNull CommandUser executor, @NotNull String[] args) {
        return parseStringArg(args, 0).map(Teleportable::username)
                .or(() -> executor instanceof Teleportable ? Optional.of((Teleportable) executor) : Optional.empty());
    }


}