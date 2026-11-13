package com.encdata.gearwalkers.content;

import com.encdata.gearwalkers.registry.GearwalkersBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class HingeBlock extends BaseJointBlock {
    public HingeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<HingeBlockEntity> getJointType() {
        return GearwalkersBlockEntities.HINGE.get();
    }

    @Override
    public HingeBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return GearwalkersBlockEntities.HINGE.get().create(pos, state);
    }
}
