package com.encdata.gearwalkers.content;

import dev.ryanhcode.sable.api.block.BlockSubLevelAssemblyListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

public abstract class BaseJointBlock extends Block implements EntityBlock, BlockSubLevelAssemblyListener {
    public static final BooleanProperty ASSEMBLED = BooleanProperty.create("assembled");
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    protected BaseJointBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(ASSEMBLED, false));
    }

    protected abstract BlockEntityType<? extends BaseJointBlockEntity> getJointType();

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ASSEMBLED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getNearestLookingDirection());
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != getJointType()) return null;
        return (tickLevel, pos, blockState, blockEntity) -> BaseJointBlockEntity.tick(
                tickLevel, pos, blockState, (BaseJointBlockEntity) blockEntity);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide
                && level.getBlockEntity(pos) instanceof BaseJointBlockEntity joint) {
            if (joint instanceof MotorizedJointBlockEntity motorized) motorized.removeControlsFromController();
            joint.removeFromWorld();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof BaseJointBlockEntity joint) {
            if (joint instanceof MotorizedJointBlockEntity motorized) motorized.removeControlsFromController();
            joint.removeFromWorld();
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)
                || !(level.getBlockEntity(pos) instanceof BaseJointBlockEntity joint)) return InteractionResult.PASS;
        if (joint.isAssembled()) {
            player.displayClientMessage(Component.translatable("gearwalkers.joint.already_attached"), true);
            return InteractionResult.SUCCESS;
        }
        JointInteractionHandler.select(serverPlayer, pos);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void afterMove(ServerLevel originLevel, ServerLevel resultingLevel, BlockState newState, BlockPos oldPos, BlockPos newPos) {
        if (resultingLevel.getBlockEntity(newPos) instanceof BaseJointBlockEntity joint) joint.afterAssemblyMove();
    }

    @Override
    public void beforeMove(ServerLevel originLevel, ServerLevel resultingLevel, BlockState newState, BlockPos oldPos, BlockPos newPos) {
        if (originLevel.getBlockEntity(oldPos) instanceof BaseJointBlockEntity joint) joint.beforeAssemblyMove();
    }
}
