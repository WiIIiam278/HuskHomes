package net.william278.huskhomes.util;

import de.themoep.minedown.MineDown;
import net.william278.huskhomes.config.Locales;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AboutMenu {

    @NotNull
    private final String title;

    @Nullable
    private Version version;

    @Nullable
    private String description;

    @NotNull
    private final Map<String, List<Credit>> attributions;

    @NotNull
    private final List<Link> buttons;

    private AboutMenu(@NotNull String title) {
        this.title = title;
        this.buttons = new ArrayList<>();
        this.attributions = new LinkedHashMap<>();
    }

    @NotNull
    public static AboutMenu create(@NotNull String title) {
        return new AboutMenu(title);
    }

    @NotNull
    public AboutMenu withDescription(@Nullable String description) {
        this.description = description;
        return this;
    }

    @NotNull
    public AboutMenu withVersion(@NotNull Version version) {
        this.version = version;
        return this;
    }

    @NotNull
    public AboutMenu addAttribution(@NotNull String category, @NotNull Credit... credits) {
        final List<Credit> creditList = new ArrayList<>(Arrays.asList(credits));
        attributions.putIfAbsent(category, new ArrayList<>());
        attributions.get(category).addAll(creditList);
        return this;
    }

    @NotNull
    public AboutMenu addButtons(@NotNull Link... links) {
        buttons.addAll(Arrays.asList(links));
        return this;
    }

    @NotNull
    public MineDown toMineDown() {
        final StringJoiner menu = new StringJoiner("\n")
                .add("[" + Locales.escapeMineDown(title) + "](#00fb9a bold)"
                     + (version != null ? " [| Version " + Locales.escapeMineDown(version.toString()) + "](#00fb9a)" : ""));
        if (description != null) {
            menu.add("[" + Locales.escapeMineDown(description) + "](gray)");
        }

        if (!attributions.isEmpty()) {
            menu.add("");
        }
        for (Map.Entry<String, List<Credit>> entry : attributions.entrySet()) {
            final StringJoiner creditJoiner = new StringJoiner(", ");
            for (final Credit credit : entry.getValue()) {
                creditJoiner.add("[" + credit.name + "](" + credit.color +
                                 (credit.description != null ? " show_text=&7" + Locales.escapeMineDown(credit.description) : "") +
                                 (credit.url != null ? " open_url=" + Locales.escapeMineDown(credit.url) : "")
                                 + ")");
            }

            if (!creditJoiner.toString().isBlank()) {
                menu.add("[• " + entry.getKey() + ":](white) " + creditJoiner);
            }
        }

        if (!buttons.isEmpty()) {
            final StringJoiner buttonsJoiner = new StringJoiner("   ");
            for (final Link link : buttons) {
                buttonsJoiner.add("[[" + (link.icon != null ? link.icon + " " : "")
                                  + Locales.escapeMineDown(link.text) + "…]](" + link.color + " "
                                  + "show_text=&7Click to open link open_url=" + Locales.escapeMineDown(link.url) + ")");
            }
            menu.add("").add("[Links:](gray) " + buttonsJoiner);
        }

        return new MineDown(menu.toString()).replace();
    }

    @NotNull
    public String toConsoleString() {
        final StringJoiner menu = new StringJoiner("\n")
                .add(title + (version != null ? " | Version " + version : ""));
        if (description != null) {
            menu.add(description);
        }

        if (!attributions.entrySet().isEmpty()) {
            menu.add("━━━━━━━━━━━━━━━━━━━━━━━━");
        }
        for (final Map.Entry<String, List<Credit>> entry : attributions.entrySet()) {
            final StringJoiner creditJoiner = new StringJoiner(", ");
            for (final Credit credit : entry.getValue()) {
                creditJoiner.add(credit.name + (credit.description != null ? " (" + credit.description + ")" : ""));
            }

            if (!creditJoiner.toString().isBlank()) {
                menu.add("- " + entry.getKey() + ": " + creditJoiner);
            }
        }

        if (!buttons.isEmpty()) {
            menu.add("━━━━━━━━━━━━━━━━━━━━━━━━");
            for (final Link link : buttons) {
                menu.add("- " + link.text + ": " + link.url);
            }
        }

        return menu.toString();
    }

    public static class Link {

        @NotNull
        private String text = "Link";

        @NotNull
        private String color = "#00fb9a";

        @Nullable
        private String icon;

        @NotNull
        private final String url;

        private Link(@NotNull String url) {
            this.url = url;
        }

        public static Link of(@NotNull String url) {
            return new Link(url);
        }

        public Link withText(@NotNull String text) {
            this.text = text;
            return this;
        }

        public Link withIcon(@NotNull String icon) {
            this.icon = icon;
            return this;
        }

        public Link withColor(@NotNull String color) {
            this.color = color;
            return this;
        }

    }

    public static class Credit {
        @NotNull
        private final String name;

        @Nullable
        private String description;

        @Nullable
        private String url;

        @NotNull
        private String color = "gray";

        private Credit(@NotNull String name) {
            this.name = name;
        }

        public static Credit of(@NotNull String name) {
            return new Credit(name);
        }

        public Credit withDescription(@Nullable String description) {
            this.description = description;
            return this;
        }

        public Credit withUrl(@Nullable String url) {
            this.url = url;
            return this;
        }

        public Credit withColor(@NotNull String color) {
            this.color = color;
            return this;
        }

    }

}
