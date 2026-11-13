package com.encdata.gearwalkers.content;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class JointInteractionHandler {
    private static final Map<UUID, Selection> SELECTIONS = new HashMap<>();

    private JointInteractionHandler() {}

    public static void select(ServerPlayer player, BlockPos jointPos) {
        SELECTIONS.put(player.getUUID(), new Selection(player.level().dimension(), jointPos.immutable()));
        player.displayClientMessage(Component.translatable("gearwalkers.joint.select_block"), true);
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (JointControllerLinkHandler.onRightClickBlock(event)) return;
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof ServerPlayer player)) return;
        Selection selection = SELECTIONS.get(player.getUUID());
        if (selection == null) return;

        if (!selection.dimension().equals(player.level().dimension())) {
            SELECTIONS.remove(player.getUUID());
            return;
        }

        BlockPos clickedPos = event.getPos();
        event.setCanceled(true);
        if (clickedPos.equals(selection.jointPos())) {
            SELECTIONS.remove(player.getUUID());
            player.displayClientMessage(Component.translatable("gearwalkers.joint.selection_cancelled"), true);
            return;
        }

        if (!(player.level().getBlockEntity(selection.jointPos()) instanceof BaseJointBlockEntity joint)) {
            SELECTIONS.remove(player.getUUID());
            return;
        }

        if (joint.assemble(player, clickedPos)) SELECTIONS.remove(player.getUUID());
    }

    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        SELECTIONS.remove(event.getEntity().getUUID());
        JointControllerLinkHandler.onPlayerLogout(event);
    }

    private record Selection(ResourceKey<Level> dimension, BlockPos jointPos) {}
}
