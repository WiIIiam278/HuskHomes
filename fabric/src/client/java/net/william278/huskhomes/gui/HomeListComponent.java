package net.william278.huskhomes.gui;

import io.wispforest.owo.ui.component.TextBoxComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;

public class HomeListComponent extends AlwaysSelectedEntryListWidget<HomeEntry> {

    TextBoxComponent searchBox;

    public HomeListComponent(MinecraftClient client, int width, int height, int y, int itemHeight, TextBoxComponent searchBox) {
        super(client, width, height, y, itemHeight);
        this.searchBox = searchBox;
    }

}
