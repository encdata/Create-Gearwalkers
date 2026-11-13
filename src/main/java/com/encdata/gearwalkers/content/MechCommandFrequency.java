package com.encdata.gearwalkers.content;

import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;
import net.minecraft.network.chat.Component;

public record MechCommandFrequency(String controlId, Frequency first, Frequency second) {
    public Component getName() {
        return MechControlRegistry.getName(controlId);
    }
}
