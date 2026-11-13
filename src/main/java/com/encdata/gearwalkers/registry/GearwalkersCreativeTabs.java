package com.encdata.gearwalkers.registry;

import com.encdata.gearwalkers.Gearwalkers;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import net.minecraft.resources.ResourceLocation;

public final class GearwalkersCreativeTabs {
    private static final SimulatedRegistrate REGISTRATE = new SimulatedRegistrate(
            id("mechs"), Gearwalkers.MOD_ID);

    private GearwalkersCreativeTabs() {}

    public static void register() {
        REGISTRATE.addExtraItem(id("mech_frequency_controller"));
        REGISTRATE.addExtraItem(id("hinge"));
        REGISTRATE.addExtraItem(id("ball_joint"));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Gearwalkers.MOD_ID, path);
    }
}
