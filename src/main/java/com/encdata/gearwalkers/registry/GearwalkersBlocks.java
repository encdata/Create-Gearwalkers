package com.encdata.gearwalkers.registry;

import com.encdata.gearwalkers.Gearwalkers;
import com.encdata.gearwalkers.content.BallJointBlock;
import com.encdata.gearwalkers.content.HingeBlock;
import com.encdata.gearwalkers.content.MechFrequencyControllerBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GearwalkersBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Gearwalkers.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Gearwalkers.MOD_ID);

    public static final DeferredBlock<MechFrequencyControllerBlock> MECH_FREQUENCY_CONTROLLER = BLOCKS.registerBlock(
            "mech_frequency_controller", MechFrequencyControllerBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final DeferredItem<BlockItem> MECH_FREQUENCY_CONTROLLER_ITEM = ITEMS.registerSimpleBlockItem(
            "mech_frequency_controller", MECH_FREQUENCY_CONTROLLER);
    public static final DeferredBlock<HingeBlock> HINGE = BLOCKS.registerBlock(
            "hinge", HingeBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final DeferredItem<BlockItem> HINGE_ITEM = ITEMS.registerSimpleBlockItem("hinge", HINGE);
    public static final DeferredBlock<BallJointBlock> BALL_JOINT = BLOCKS.registerBlock(
            "ball_joint", BallJointBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final DeferredItem<BlockItem> BALL_JOINT_ITEM = ITEMS.registerSimpleBlockItem("ball_joint", BALL_JOINT);

    private GearwalkersBlocks() {}

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }
}
