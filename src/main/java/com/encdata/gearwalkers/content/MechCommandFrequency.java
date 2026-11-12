package com.encdata.gearwalkers.content;

import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;

public record MechCommandFrequency(MechCommand command, Frequency first, Frequency second) {}
