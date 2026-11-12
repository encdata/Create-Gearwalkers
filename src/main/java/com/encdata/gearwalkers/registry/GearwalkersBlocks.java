package com.encdata.gearwalkers.registry;

import com.encdata.gearwalkers.Gearwalkers;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;

public final class GearwalkersBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Gearwalkers.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Gearwalkers.MOD_ID);

    public static final DeferredBlock<Block> MECH_FREQUENCY_CONTROLLER = BLOCKS.registerSimpleBlock(
            "mech_frequency_controller", BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final DeferredItem<BlockItem> MECH_FREQUENCY_CONTROLLER_ITEM = ITEMS.registerSimpleBlockItem(
            "mech_frequency_controller", MECH_FREQUENCY_CONTROLLER);

    private GearwalkersBlocks() {}

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }
}
