package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.onixary.shapeShifterCurseFabric.client.ClientPlayerStateManager;
import net.onixary.shapeShifterCurseFabric.mixin.accessor.EntityAirDataAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientAirSyncMixin {
    @Inject(method = "handleSetEntityData", at = @At("TAIL"))
    private void ssc$rememberServerAir(ClientboundSetEntityDataPacket packet, CallbackInfo ci) {
        var player = Minecraft.getInstance().player;
        if (player == null || player.getId() != packet.id()) return;
        int airId = EntityAirDataAccessor.ssc$getAirData().id();
        for (var value : packet.packedItems()) {
            if (value.id() == airId) ClientPlayerStateManager.lastServerAir = (Integer)value.value();
        }
    }
}
