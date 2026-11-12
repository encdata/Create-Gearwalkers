package com.encdata.gearwalkers.content;

import net.minecraft.network.chat.Component;

import java.util.Locale;

public enum MechCommand {
    WALK_FORWARD,
    WALK_BACKWARD,
    WALK_LEFT,
    WALK_RIGHT,
    JUMP;

    public Component getName() {
        return Component.translatable("gearwalkers.command." + name().toLowerCase(Locale.ROOT));
    }
}
