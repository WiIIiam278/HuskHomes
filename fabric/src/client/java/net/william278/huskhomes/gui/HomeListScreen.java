package net.william278.huskhomes.gui;

import com.nimbusds.jose.util.Container;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class HomeListScreen extends BaseOwoScreen<FlowLayout> {

    private static final Identifier HOME_FILTER_BUTTON = Identifier.of("huskhomes", "textures/gui/home_filter_button.png");

    private final HomeProvider provider;

    public HomeListScreen(HomeProvider provider) {
        this.provider = provider;
    }

    // UI
    private TextBoxComponent searchBox;
    private ButtonComponent filterButton;
    private ScrollContainer<FlowLayout> list;

    @Override
    @NotNull
    protected OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout root) {
        root.surface(Surface.optionsBackground())
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);
        root.child(
                Containers.verticalFlow(Sizing.fill(), Sizing.content())
                        .child(getTitleLabel())
                        .child(getSearchBar())
                        .child(getHomeList())
                        .child(getDoneButton())
                        .gap(10)
                        .verticalAlignment(VerticalAlignment.CENTER)
                        .horizontalAlignment(HorizontalAlignment.CENTER)
        );
    }

    private Component getTitleLabel() {
        return Components.label(Text.literal("Home List"))
                .color(Color.ofRgb(0xffffff))
                .shadow(true)
                .horizontalTextAlignment(HorizontalAlignment.CENTER)
                .verticalTextAlignment(VerticalAlignment.CENTER);
    }

    private Component getSearchBar() {
        this.searchBox = Components.textBox(Sizing.fixed(200), "");
        searchBox.setSuggestion("Filter Homes...");
        searchBox.onChanged();

        this.filterButton = Components.button(Text.empty(), (button) -> {
            System.out.println("FILTER");
        });
        this.filterButton.renderer(ButtonComponent.Renderer.texture(
                HOME_FILTER_BUTTON, 0, 0, 32, 64
        ));
        this.filterButton.setWidth(20);

        return Containers.horizontalFlow(Sizing.content(), Sizing.content())
                .child(searchBox)
                .child(filterButton)
                .gap(5)
                .verticalAlignment(VerticalAlignment.CENTER)
                .horizontalAlignment(HorizontalAlignment.CENTER);
    }

    private Component getHomeList() {
        final FlowLayout flow = Containers.verticalFlow(Sizing.content(), Sizing.content());
        flow.child(new HomeListComponent(client, 500, 300, 0, 30, searchBox));
        return flow;
    }

    private Component getDoneButton() {
        final ButtonComponent button = Components.button(ScreenTexts.DONE, (btn) -> this.close());
        button.setWidth(200);
        return button;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

}
