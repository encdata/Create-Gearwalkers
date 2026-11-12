package com.encdata.gearwalkers.network;

import com.encdata.gearwalkers.Gearwalkers;
import com.encdata.gearwalkers.content.MechFrequencyControllerBlockEntity;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class GearwalkersNetwork {
    private GearwalkersNetwork() {}

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(Gearwalkers.MOD_ID);
        registrar.playToServer(
                MechFrequencySavePayload.TYPE,
                MechFrequencySavePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer player)) return;
                    if (player.distanceToSqr(payload.pos().getCenter()) > 64) return;
                    if (!(player.level().getBlockEntity(payload.pos()) instanceof MechFrequencyControllerBlockEntity controller)) return;
                    controller.replaceCommands(payload.entries());
                }));
    }
}
