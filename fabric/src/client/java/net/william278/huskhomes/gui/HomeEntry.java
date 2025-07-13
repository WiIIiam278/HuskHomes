package net.william278.huskhomes.gui;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.Text;
import net.william278.huskhomes.position.Home;

@RequiredArgsConstructor
public class HomeEntry extends AlwaysSelectedEntryListWidget.Entry<HomeEntry> {

    private final Home wrapped;

    @Override
    public Text getNarration() {
        return Text.literal(wrapped.getIdentifier());
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight,
                       int mouseX, int mouseY, boolean hovered, float tickProgress) {
    }
}
