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

import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * Options, including placeholder strings, used to generate a {@link PaginatedList} of items.
 */
public class ListOptions {

    @NotNull
    protected String headerFormat = "<%color%>Viewing %topic%</%color%> "
            + "<%color%>(%first_item_on_page_index%-%last_item_on_page_index% of</%color%> "
            + "<%color%><bold>%total_items%</bold></%color%><%color%>)</%color%>";
    @NotNull
    protected String footerFormat = "%previous_page_button%Page <%color%>%current_page%</%color%>/"
            + "<%color%>%total_pages%</%color%>%next_page_button%   %page_jumpers%";
    @NotNull
    protected String previousButtonFormat = "<white><hover:show_text:'View previous page (%previous_page_index%)'>"
            + "<click:run_command:'/%command% %previous_page_index%'>◁</click></hover></white> ";
    @NotNull
    protected String nextButtonFormat = " <white><hover:show_text:'View next page (%next_page_index%)'>"
            + "<click:run_command:'/%command% %next_page_index%'>▷</click></hover></white>";
    @NotNull
    protected String pageJumpersFormat = "(%page_jump_buttons%)";
    @NotNull
    protected String pageJumperPageSeparator = "|";
    @NotNull
    protected String pageJumperGroupSeparator = "…";
    @NotNull
    protected String pageJumperCurrentPageFormat = "<%color%>%current_page%</%color%>";
    @NotNull
    protected String pageJumperPageFormat = "<hover:show_text:'Jump to page %target_page_index%'>"
            + "<click:run_command:'/%command% %target_page_index%'>%target_page_index%</click></hover>";
    @NotNull
    protected String topic = "List";
    @NotNull
    protected String command = "example";
    @NotNull
    protected Color themeColor = new Color(0x00fb9a);
    protected boolean spaceAfterHeader = true;
    protected boolean spaceBeforeFooter = true;
    protected boolean escapeItems = true;
    @NotNull
    protected String itemSeparator = "\n";
    protected int itemsPerPage = 10;

    protected int pageJumperStartButtons = 3;

    protected int pageJumperEndButtons = 3;

    private ListOptions() {
    }

    @SuppressWarnings("unused")
    public static class Builder {
        @NotNull
        private final ListOptions options = new ListOptions();

        @NotNull
        public Builder setHeaderFormat(@NotNull String headerFormat) {
            options.headerFormat = headerFormat;
            return this;
        }

        @NotNull
        public Builder setFooterFormat(@NotNull String footerFormat) {
            options.footerFormat = footerFormat;
            return this;
        }

        @NotNull
        public Builder setItemSeparator(@NotNull String itemSeparator) {
            options.itemSeparator = itemSeparator;
            return this;
        }

        @NotNull
        public Builder setThemeColor(@NotNull Color themeColor) {
            options.themeColor = themeColor;
            return this;
        }

        @NotNull
        public Builder setSpaceAfterHeader(final boolean spaceAfterHeader) {
            options.spaceAfterHeader = spaceAfterHeader;
            return this;
        }

        @NotNull
        public Builder setSpaceBeforeFooter(final boolean spaceBeforeFooter) {
            options.spaceBeforeFooter = spaceBeforeFooter;
            return this;
        }

        @NotNull
        public Builder setItemsPerPage(final int itemsPerPage) {
            options.itemsPerPage = itemsPerPage;
            return this;
        }

        @NotNull
        public Builder setTopic(@NotNull String topic) {
            options.topic = topic;
            return this;
        }

        @NotNull
        public Builder setCommand(@NotNull String command) {
            options.command = command;
            return this;
        }

        @NotNull
        public Builder setEscapeItems(final boolean escapeItems) {
            options.escapeItems = escapeItems;
            return this;
        }

        @NotNull
        public Builder setPageJumpersFormat(@NotNull String pageJumpersFormat) {
            options.pageJumpersFormat = pageJumpersFormat;
            return this;
        }

        @NotNull
        public Builder setPageJumperPageSeparator(@NotNull String pageJumperPageSeparator) {
            options.pageJumperPageSeparator = pageJumperPageSeparator;
            return this;
        }

        @NotNull
        public Builder setPageJumperPageFormat(@NotNull String pageJumperPageFormat) {
            options.pageJumperPageFormat = pageJumperPageFormat;
            return this;
        }

        @NotNull
        public Builder setPageJumperGroupSeparator(@NotNull String pageJumperGroupSeparator) {
            options.pageJumperGroupSeparator = pageJumperGroupSeparator;
            return this;
        }

        @NotNull
        public Builder setPageJumperCurrentPageFormat(@NotNull String pageJumperCurrentPageFormat) {
            options.pageJumperCurrentPageFormat = pageJumperCurrentPageFormat;
            return this;
        }

        @NotNull
        public Builder setPreviousButtonFormat(@NotNull String previousButtonFormat) {
            options.previousButtonFormat = previousButtonFormat;
            return this;
        }

        @NotNull
        public Builder setNextButtonFormat(@NotNull String nextButtonFormat) {
            options.nextButtonFormat = nextButtonFormat;
            return this;
        }

        @NotNull
        public Builder setPageJumperStartButtons(final int pageJumperStartButtons) {
            options.pageJumperStartButtons = pageJumperStartButtons;
            return this;
        }

        @NotNull
        public Builder setPageJumperEndButtons(final int pageJumperEndButtons) {
            options.pageJumperEndButtons = pageJumperEndButtons;
            return this;
        }

        @NotNull
        public ListOptions build() {
            return options;
        }
    }
}
