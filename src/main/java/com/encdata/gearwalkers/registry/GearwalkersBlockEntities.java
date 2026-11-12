package com.encdata.gearwalkers.registry;

import com.encdata.gearwalkers.Gearwalkers;
import com.encdata.gearwalkers.content.MechFrequencyControllerBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class GearwalkersBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Gearwalkers.MOD_ID);
    public static final Supplier<BlockEntityType<MechFrequencyControllerBlockEntity>> MECH_FREQUENCY_CONTROLLER = TYPES.register(
            "mech_frequency_controller", () -> BlockEntityType.Builder.of(MechFrequencyControllerBlockEntity::new,
                    GearwalkersBlocks.MECH_FREQUENCY_CONTROLLER.get()).build(null));

    private GearwalkersBlockEntities() {}
}
