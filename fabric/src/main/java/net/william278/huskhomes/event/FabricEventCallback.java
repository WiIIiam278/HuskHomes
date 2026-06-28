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

package net.william278.huskhomes.event;

//#if MC>=260102
import net.minecraft.world.InteractionResult;
//#else
//$$ import net.minecraft.util.ActionResult;
//#endif
import org.jetbrains.annotations.NotNull;

public interface FabricEventCallback<E extends Event> {
    //#if MC>=260102
    static <C extends Cancellable> InteractionResult invokeEvents(FabricEventCallback<C>[] listeners, C event) {
    //#else
    //$$ static <C extends Cancellable> ActionResult invokeEvents(FabricEventCallback<C>[] listeners, C event) {
    //#endif
        for (FabricEventCallback<C> listener : listeners) {
            //#if MC>=260102
            final InteractionResult result = listener.invoke(event);
            if (event.isCancelled()) {
                return InteractionResult.CONSUME;
            } else if (result != InteractionResult.PASS) {
                event.setCancelled(true);
                return result;
            }
            //#else
            //$$ final ActionResult result = listener.invoke(event);
            //$$ if (event.isCancelled()) {
            //$$    return ActionResult.CONSUME;
            //$$ } else if (result != ActionResult.PASS) {
            //$$    event.setCancelled(true);
            //$$    return result;
            //$$ }
            //#endif
        }

        //#if MC>=260102
        return InteractionResult.PASS;
        //#else
        //$$ return ActionResult.PASS;
        //#endif
    }

    @NotNull
    //#if MC>=260102
    InteractionResult invoke(@NotNull E event);
    //#else
    //$$ ActionResult invoke(@NotNull E event);
    //#endif
}
