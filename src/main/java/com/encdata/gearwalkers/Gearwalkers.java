package com.encdata.gearwalkers;

import com.encdata.gearwalkers.content.JointInteractionHandler;
import com.encdata.gearwalkers.network.GearwalkersNetwork;
import com.encdata.gearwalkers.registry.GearwalkersBlockEntities;
import com.encdata.gearwalkers.registry.GearwalkersBlocks;
import com.encdata.gearwalkers.registry.GearwalkersCreativeTabs;
import com.encdata.gearwalkers.registry.GearwalkersMenus;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Gearwalkers.MOD_ID)
public final class Gearwalkers {
    public static final String MOD_ID = "gearwalkers";

    public Gearwalkers(IEventBus modEventBus) {
        GearwalkersBlocks.register(modEventBus);
        GearwalkersBlockEntities.TYPES.register(modEventBus);
        GearwalkersMenus.TYPES.register(modEventBus);
        modEventBus.addListener(GearwalkersNetwork::registerPayloads);
        GearwalkersCreativeTabs.register();
        NeoForge.EVENT_BUS.addListener(JointInteractionHandler::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(JointInteractionHandler::onPlayerLogout);
    }
}
