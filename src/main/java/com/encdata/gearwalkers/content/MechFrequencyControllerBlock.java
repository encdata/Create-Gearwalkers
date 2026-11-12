package com.encdata.gearwalkers.content;

import com.encdata.gearwalkers.registry.GearwalkersBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.server.level.ServerPlayer;

public class MechFrequencyControllerBlock extends Block implements EntityBlock {
    public MechFrequencyControllerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return GearwalkersBlockEntities.MECH_FREQUENCY_CONTROLLER.get().create(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof MechFrequencyControllerBlockEntity controller) {
            serverPlayer.openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable("block.gearwalkers.mech_frequency_controller");
                }

                @Override
                public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id, net.minecraft.world.entity.player.Inventory inventory, Player player) {
                    return new MechFrequencyControllerMenu(id, inventory, controller);
                }
            }, buffer -> {
                buffer.writeBlockPos(pos);
                buffer.writeNbt(controller.saveForMenu(buffer.registryAccess()));
            });
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
