package com.encdata.gearwalkers;

import com.encdata.gearwalkers.registry.GearwalkersBlocks;
import com.encdata.gearwalkers.registry.GearwalkersCreativeTabs;
import com.encdata.gearwalkers.registry.GearwalkersBlockEntities;
import com.encdata.gearwalkers.registry.GearwalkersMenus;
import com.encdata.gearwalkers.network.GearwalkersNetwork;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Gearwalkers.MOD_ID)
public final class Gearwalkers {
    public static final String MOD_ID = "gearwalkers";

    public Gearwalkers(IEventBus modEventBus) {
        GearwalkersBlocks.register(modEventBus);
        GearwalkersBlockEntities.TYPES.register(modEventBus);
        GearwalkersMenus.TYPES.register(modEventBus);
        modEventBus.addListener(GearwalkersNetwork::registerPayloads);
        GearwalkersCreativeTabs.register();
    }
}
