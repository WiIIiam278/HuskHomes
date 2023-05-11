package net.william278.huskhomes.client;

import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.SavedPosition;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PositionScreen extends CottonClientScreen {

    private static final int WIDTH = 270;
    private static final int HEIGHT = 190;

    protected PositionScreen(@NotNull HuskHomesClient client) {
        super(new PositionGuiDescription(client));
    }

    private static class PositionGuiDescription extends LightweightGuiDescription {

        private PositionGuiDescription(@NotNull HuskHomesClient client) {
            final WTabPanel root = new WTabPanel();
            setRootPanel(root);

            // Add tabs
            final LinkedHashMap<ListContainer, String> tabs = new LinkedHashMap<>(Map.of(
                    new PrivateHomeListContainer(client), "gui.huskhomes.homes.title",
                    new PublicHomeListContainer(client), "gui.huskhomes.public_homes.title",
                    new WarpListContainer(client), "gui.huskhomes.warps.title"
            ));
            tabs.forEach((key, value) -> {
                root.add(key.getList(), builder -> builder.title(Text.translatable(value)));
                key.getList().setHost(this);
                key.getList().validate(this);
            });

            root.setSize(WIDTH, HEIGHT);
            root.setSelectedIndex(0);
            root.setBackgroundPainter((matrices, left, top, panel) -> {
            });
            root.validate(this);
        }

    }

    public static final class PrivateHomeListContainer extends ListContainer {

        private PrivateHomeListContainer(@NotNull HuskHomesClient client) {
            super(client, ClientQuery.Type.GET_PRIVATE_HOMES, "homes");
        }

    }

    public static final class PublicHomeListContainer extends ListContainer {

        private PublicHomeListContainer(@NotNull HuskHomesClient client) {
            super(client, ClientQuery.Type.GET_PUBLIC_HOMES, "public_homes");
        }

    }

    public static final class WarpListContainer extends ListContainer {

        private WarpListContainer(@NotNull HuskHomesClient client) {
            super(client, ClientQuery.Type.GET_WARPS, "warps");
        }

    }

    private static abstract class ListContainer {

        private final WListPanel<SavedPosition, PositionItem> list;
        private final List<SavedPosition> positions = new ArrayList<>();
        private final WLabel statusLabel = new WLabel(Text.empty());

        private ListContainer(@NotNull HuskHomesClient client, @NotNull ClientQuery.Type queryType, @NotNull String title) {
            list = new WListPanel<>(
                    positions,
                    PositionItem::new,
                    (position, display) -> display.setFrom(position)
            );
            list.setListItemHeight(2 * 18);
            list.setLocation(5, 0);
            list.setSize(WIDTH - 10, HEIGHT);
            if (!isDisabled(client)) {
                sendQuery(client, queryType, title);
            }
        }

        protected void sendQuery(@NotNull HuskHomesClient client, @NotNull ClientQuery.Type queryType, @NotNull String title) {
            client.sendQuery(
                    ClientQuery.builder().type(queryType).build(),
                    (response -> {
                        statusLabel.setText(Text.empty());
                        if (queryType == ClientQuery.Type.GET_WARPS) {
                            positions.addAll(response.getPayload().getWarpList().orElseThrow());
                        } else {
                            positions.addAll(response.getPayload().getHomeList().orElseThrow());
                        }
                    }),
                    () -> statusLabel.setText(Text.translatable("gui.huskhomes." + title + ".timeout"))
            );
        }

        protected boolean isDisabled(@NotNull HuskHomesClient client) {
            if (!client.isCurrentlyEnabled()) {
                statusLabel.setText(Text.translatable("gui.huskhomes.disabled"));
                return true;
            }
            return false;
        }

        @NotNull
        public WListPanel<SavedPosition, PositionItem> getList() {
            return list;
        }

    }

    private static class PositionItem extends WPlainPanel {

        private static final int WIDTH = 120;
        private final WSprite icon;
        private final WLabel name;
        private final WLabel description;

        private PositionItem() {
            icon = new WSprite(new Identifier("huskhomes", "textures/gui/icons/player_home.png"));
            name = new WLabel(Text.translatable("gui.huskhomes.loading"));
            description = new WLabel(Text.empty());

            this.add(icon, 0, 0, 18, 18);
            this.add(name, 20, 0, WIDTH, 18);
            this.add(description, 20, 20, WIDTH, 18);
        }

        public void setFrom(@NotNull SavedPosition position) {
            // Add icon
            icon.setImage(new Identifier(
                    "huskhomes",
                    "textures/gui/icons/" + (position instanceof Home ? "player_home" : "server_warp") + ".png"
            ));

            // Add name
            name.setText(Text.of(position.getName()));
            if (!position.getMeta().getDescription().isBlank()) {
                description.setText(Text.of(Locales.truncateText(position.getMeta().getDescription(), 24)));
            }
        }


    }

}
