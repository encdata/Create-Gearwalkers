package com.encdata.gearwalkers.content;

import com.encdata.gearwalkers.registry.GearwalkersBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BallJointBlock extends BaseJointBlock {
    public BallJointBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<BallJointBlockEntity> getJointType() {
        return GearwalkersBlockEntities.BALL_JOINT.get();
    }

    @Override
    public BallJointBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return GearwalkersBlockEntities.BALL_JOINT.get().create(pos, state);
    }
}
