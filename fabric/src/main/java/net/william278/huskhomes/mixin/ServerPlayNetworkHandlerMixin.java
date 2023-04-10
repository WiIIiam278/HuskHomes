package net.william278.huskhomes.mixin;

import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.william278.huskhomes.util.CustomPayloadCallback;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
 * Mixin work taken from Awakened Redstone's work on PAPIProxyBridge (Licensed Under Apache-2.0)
 */
@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Inject(at = @At("HEAD"), method = "onCustomPayload(Lnet/minecraft/network/packet/c2s/play/CustomPayloadC2SPacket;)V")
    private void huskHomes$handlePluginMessage(@NotNull CustomPayloadC2SPacket packet, @NotNull CallbackInfo ci) {
        CustomPayloadCallback.EVENT.invoker().invoke(packet.getChannel().toString(), packet.getData());
    }

}
