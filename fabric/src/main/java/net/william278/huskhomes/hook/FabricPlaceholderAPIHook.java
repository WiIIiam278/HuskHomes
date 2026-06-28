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

package net.william278.huskhomes.hook;

import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
//#if MC>=260102
import net.minecraft.resources.Identifier;
//#else
//$$ import net.minecraft.util.Identifier;
//#endif
import net.william278.huskhomes.FabricHuskHomes;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.SavedUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@PluginHook(
        name = "Fabric PlaceholderAPI",
        register = PluginHook.Register.ON_ENABLE
)
public class FabricPlaceholderAPIHook extends Hook {

    public FabricPlaceholderAPIHook(@NotNull FabricHuskHomes plugin) {
        super(plugin);
    }

    @SuppressWarnings("SameParameterValue")
    @NotNull
    private Identifier createIdentifier(@NotNull String name) {
        //#if MC>=260102
        return Identifier.fromNamespaceAndPath("huskhomes", name);
        //#else
        //$$ return Identifier.of("huskhomes", name);
        //#endif
    }

    @Override
    public void load() {
        //#if MC>=260102
        Placeholders.registerServer(createIdentifier("player"), (ctx, arg) -> {
        //#else
        //$$ Placeholders.register(createIdentifier("player"), (ctx, arg) -> {
        //#endif
            if (!ctx.hasPlayer()) {
                return PlaceholderResult.invalid("No player!");
            }

            if (arg == null) {
                return PlaceholderResult.invalid("No argument for player!");
            }

            assert ctx.player() != null;
            //#if MC>=260000
            final OnlineUser player = ((FabricHuskHomes) plugin).getOnlineUser(ctx.player().getUUID());
            //#else
            //$$ final OnlineUser player = ((FabricHuskHomes) plugin).getOnlineUser(ctx.player());
            //#endif

            final String response = switch (arg) {
                case "homes_count" -> String.valueOf(plugin.getManager().homes()
                        .getUserHomes()
                        .getOrDefault(player.getName(), List.of()).size());
                case "max_homes" -> String.valueOf(plugin.getManager().homes().getMaxHomes(player));
                case "max_public_homes" -> String.valueOf(plugin.getManager().homes().getMaxPublicHomes(player));
                case "free_home_slots" -> String.valueOf(plugin.getManager().homes().getFreeHomes(player));
                case "home_slots" -> String.valueOf(plugin.getSavedUser(player)
                        .map(SavedUser::getHomeSlots).orElse(0));
                case "homes_list" -> String.join(", ", plugin.getManager().homes()
                        .getUserHomes()
                        .getOrDefault(player.getName(), List.of()));
                case "public_homes_count" -> String.valueOf(plugin.getManager().homes()
                        .getPublicHomes()
                        .getOrDefault(player.getName(), List.of()).size());
                case "public_homes_list" -> String.join(", ", plugin.getManager().homes()
                        .getPublicHomes()
                        .getOrDefault(player.getName(), List.of()));
                case "ignoring_tp_requests" -> String.valueOf(plugin.getManager().requests()
                        .isIgnoringRequests(player));
                default -> null;
            };

            if (response == null) {
                return PlaceholderResult.invalid("Invalid argument for player!");
            }

            return PlaceholderResult.value(response);
        });
    }

    @Override
    public void unload() {
        //#if MC>=260102
        // Placeholder API 3.0.0+26.1 does not expose unregister methods.
        //#else
        //$$ Placeholders.remove(createIdentifier("player"));
        //#endif
    }

}
