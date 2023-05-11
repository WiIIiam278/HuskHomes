package net.william278.huskhomes.client;

import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.SavedPosition;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PositionScreen extends CottonClientScreen {

    protected PositionScreen(@NotNull HuskHomesClient client) {
        super(new PositionGuiDescription(client));
    }

    private static class PositionGuiDescription extends LightweightGuiDescription {

        private PositionGuiDescription(@NotNull HuskHomesClient client) {
            final WPlainPanel root = new WPlainPanel();
            setRootPanel(root);
            root.setSize(256, 240);
            root.setInsets(Insets.ROOT_PANEL);

            final WLabel statusLabel = new WLabel(Text.literal("Loading homes..."));
            client.sendQuery(
                    ClientQuery.builder()
                            .setType(ClientQuery.Type.GET_PRIVATE_HOMES)
                            .build(),
                    (response -> {
                        statusLabel.setText(Text.literal(
                                "Homes: " + response.getPayload().getHomeList()
                                        .orElse(List.of())
                                        .stream().map(SavedPosition::getName)
                                        .collect(Collectors.joining(", ")))
                        );
                        statusLabel.validate(this);
                    }),
                    () -> {
                        statusLabel.setText(Text.literal("Could not retrieve homes :-("));
                        statusLabel.validate(this);
                    }
            );
            root.add(statusLabel, 5, 5);

            root.validate(this);
        }

    }

    private static class HomeListContainer extends WPlainPanel {

        private HomeListContainer() {
            final WPlainPanel root = new WPlainPanel();
            root.setSize(196, 224);
            root.setInsets(new Insets(4));
        }

    }

    private static class PositionItem<T extends SavedPosition> extends WPlainPanel {

        private PositionItem(@NotNull T position) {
            // Add icon
            add(getIcon(position), 5, 5, 16, 16);

            // Add labels
            final List<WLabel> labels = getLabels(position);
            for (int i = 0; i < labels.size(); i++) {
                final WLabel label = labels.get(i);
                add(label, 5, 5 + (i * 10));
            }
        }

        @NotNull
        private WSprite getIcon(@NotNull T position) {
            return new WSprite(Objects.requireNonNull(Identifier.of(
                    "huskhomes",
                    "textures/gui/icons/" + (position instanceof Home ? "player_home" : "server_warp") + ".png"
            )));
        }

        @NotNull
        private List<WLabel> getLabels(@NotNull T position) {
            final List<WLabel> labels = new ArrayList<>();
            labels.add(new WLabel(Text.of(position.getName())));

            if (!position.getMeta().getDescription().isBlank()) {
                final Text description = Text.of(Locales.truncateText(
                        position.getMeta().getDescription(), 24)
                );
                description.getStyle().withColor(TextColor.fromRgb(0xAAAAAA));
                labels.add(new WLabel(description));
            }
            return labels;
        }

    }

}
