package com.encdata.gearwalkers.registry;

import com.encdata.gearwalkers.Gearwalkers;
import com.encdata.gearwalkers.content.MechFrequencyControllerMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class GearwalkersMenus {
    public static final DeferredRegister<MenuType<?>> TYPES = DeferredRegister.create(Registries.MENU, Gearwalkers.MOD_ID);
    public static final Supplier<MenuType<MechFrequencyControllerMenu>> MECH_FREQUENCY_CONTROLLER = TYPES.register(
            "mech_frequency_controller", () -> IMenuTypeExtension.create(MechFrequencyControllerMenu::new));

    private GearwalkersMenus() {}
}
