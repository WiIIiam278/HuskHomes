/*
 * This file is part of HuskHomes, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.huskhomes.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.william278.huskhomes.config.Locales;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Generates MiniMessage-formatted chat menus of paginated list items.
 */
@SuppressWarnings("unused")
public class PaginatedList {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    @NotNull
    private final ListOptions options;

    @NotNull
    private final List<String> items;

    private PaginatedList(@NotNull List<String> items, @NotNull ListOptions options) {
        this.items = items;
        this.options = options;
    }

    @NotNull
    public static PaginatedList of(@NotNull List<String> items) {
        return new PaginatedList(items, new ListOptions.Builder().build());
    }

    @NotNull
    public static PaginatedList of(@NotNull List<String> items, @NotNull ListOptions options) {
        return new PaginatedList(items, options);
    }

    @NotNull
    public Component getNearestValidPage(final int page) {
        return getPage(Math.max(1, Math.min(getTotalPages(), page)));
    }

    @NotNull
    public Component getPage(final int page) throws PaginationException {
        return MINI_MESSAGE.deserialize(getRawPage(page));
    }

    @NotNull
    public String getRawPage(final int page) throws PaginationException {
        if (page < 1) {
            throw new PaginationException("Page index must be >= 1");
        }
        if (page > getTotalPages()) {
            throw new PaginationException("Page index must be <= the total number of pages (" + getTotalPages() + ")");
        }

        final StringJoiner menuJoiner = new StringJoiner("\n");
        if (!options.headerFormat.isBlank()) {
            menuJoiner.add(formatPageString(options.headerFormat, page));
            if (options.spaceAfterHeader) {
                menuJoiner.add("");
            }
        }

        if (options.escapeItems) {
            menuJoiner.add(getItemsForPage(page).stream().map(Locales::escapeText)
                    .collect(Collectors.joining(options.itemSeparator)));
        } else {
            menuJoiner.add(String.join(options.itemSeparator, getItemsForPage(page)));
        }

        if (!options.footerFormat.isBlank()) {
            if (options.spaceBeforeFooter) {
                menuJoiner.add("");
            }
            menuJoiner.add(formatPageString(options.footerFormat, page));
        }
        return menuJoiner.toString();
    }

    public int getTotalPages() {
        return (int) Math.ceil((double) items.size() / options.itemsPerPage);
    }

    @NotNull
    private List<String> getItemsForPage(final int page) {
        return items.subList((page - 1) * options.itemsPerPage, Math.min(items.size(), page * options.itemsPerPage));
    }

    @NotNull
    private String formatPageString(@NotNull String format, int page) {
        final StringBuilder convertedFormat = new StringBuilder();
        StringBuilder currentPlaceholder = new StringBuilder();
        boolean readingPlaceholder = false;
        for (char c : format.toCharArray()) {
            if (c == '%') {
                if (readingPlaceholder) {
                    switch (currentPlaceholder.toString().toLowerCase()) {
                        case "topic" -> convertedFormat.append(formatPageString(options.topic, page));
                        case "color" -> convertedFormat.append(String.format("#%02x%02x%02x",
                                options.themeColor.getRed(), options.themeColor.getGreen(), options.themeColor.getBlue()));
                        case "first_item_on_page_index" ->
                                convertedFormat.append(((page - 1) * options.itemsPerPage) + 1);
                        case "last_item_on_page_index" ->
                                convertedFormat.append(((page - 1) * options.itemsPerPage) + getItemsForPage(page).size());
                        case "total_items" -> convertedFormat.append(items.size());
                        case "current_page" -> convertedFormat.append(page);
                        case "total_pages" -> convertedFormat.append(getTotalPages());
                        case "previous_page_button" -> {
                            if (page > 1) {
                                convertedFormat.append(formatPageString(options.previousButtonFormat, page));
                            }
                        }
                        case "next_page_button" -> {
                            if (page < getTotalPages()) {
                                convertedFormat.append(formatPageString(options.nextButtonFormat, page));
                            }
                        }
                        case "next_page_index" -> convertedFormat.append(page + 1);
                        case "previous_page_index" -> convertedFormat.append(page - 1);
                        case "command" -> convertedFormat.append(options.command);
                        case "page_jumpers" -> {
                            if (getTotalPages() > 2) {
                                convertedFormat.append(formatPageString(options.pageJumpersFormat, page));
                            }
                        }
                        case "page_jump_buttons" -> convertedFormat.append(getPageJumperButtons(page));
                    }
                } else {
                    currentPlaceholder = new StringBuilder();
                }
                readingPlaceholder = !readingPlaceholder;
                continue;
            }
            if (readingPlaceholder) {
                currentPlaceholder.append(c);
            } else {
                convertedFormat.append(c);
            }
        }
        return convertedFormat.toString();
    }

    @NotNull
    private String formatPageJumper(final int page) {
        return formatPageString(options.pageJumperPageFormat.replaceAll("%target_page_index%",
                Integer.toString(page)), page);
    }

    @NotNull
    protected String getPageJumperButtons(final int page) {
        final StringJoiner pageGroups = new StringJoiner(options.pageJumperGroupSeparator);
        StringJoiner pages = new StringJoiner(options.pageJumperPageSeparator);
        int lastPage = 1;
        for (int i = 1; i <= getTotalPages(); i++) {
            if (i <= options.pageJumperStartButtons || i > getTotalPages() - options.pageJumperEndButtons || page == i) {
                if (i - lastPage > 1) {
                    pageGroups.add(pages.toString());
                    pages = new StringJoiner(options.pageJumperPageSeparator);
                }
                if (page == i) {
                    pages.add(formatPageString(options.pageJumperCurrentPageFormat, i));
                } else {
                    pages.add(formatPageString(formatPageJumper(i), i));
                }
                lastPage = i;
            }
        }
        if (!pages.toString().isBlank()) {
            pageGroups.add(pages.toString());
        }
        return pageGroups.toString();
    }

}
