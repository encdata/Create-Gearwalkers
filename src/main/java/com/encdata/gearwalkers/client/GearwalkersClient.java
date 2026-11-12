package com.encdata.gearwalkers.client;

import com.encdata.gearwalkers.client.screens.MechFrequencyControllerScreen;
import com.encdata.gearwalkers.registry.GearwalkersMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = "gearwalkers", value = Dist.CLIENT)
public final class GearwalkersClient {
    private GearwalkersClient() {}

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(GearwalkersMenus.MECH_FREQUENCY_CONTROLLER.get(), MechFrequencyControllerScreen::new);
    }
}
