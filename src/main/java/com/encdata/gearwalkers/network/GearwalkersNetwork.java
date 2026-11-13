package com.encdata.gearwalkers.network;

import com.encdata.gearwalkers.Gearwalkers;
import com.encdata.gearwalkers.content.MechControlRegistry;
import com.encdata.gearwalkers.content.MechFrequencyControllerBlockEntity;
import com.encdata.gearwalkers.content.MechFrequencyControllerMenu;
import com.encdata.gearwalkers.content.MotorizedJointBlockEntity;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.util.HashSet;
import java.util.Set;

public final class GearwalkersNetwork {
    private GearwalkersNetwork() {}

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(Gearwalkers.MOD_ID);
        registrar.playToServer(
                MechFrequencySavePayload.TYPE,
                MechFrequencySavePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer player)) return;
                    if (!(player.level().getBlockEntity(payload.pos()) instanceof MechFrequencyControllerBlockEntity controller)) return;
                    if (!(player.containerMenu instanceof MechFrequencyControllerMenu menu)
                            || menu.contentHolder != controller || !menu.stillValid(player) || !player.mayBuild()) return;

                    Set<String> allowedControlIds = new HashSet<>();
                    for (MechControlRegistry.Control control : MechControlRegistry.getControls(
                            MotorizedJointBlockEntity.getLinkedControlIds(controller))) {
                        allowedControlIds.add(control.id());
                    }
                    var entries = payload.entries().stream()
                            .filter(entry -> allowedControlIds.contains(entry.controlId()))
                            .toList();
                    controller.replaceCommands(entries);
                }));
    }
}
