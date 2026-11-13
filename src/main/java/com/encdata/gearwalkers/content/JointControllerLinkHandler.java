package com.encdata.gearwalkers.content;

import com.simibubi.create.AllItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class JointControllerLinkHandler {
    private static final Map<UUID, Selection> SELECTIONS = new HashMap<>();

    private JointControllerLinkHandler() {}

    public static boolean onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof ServerPlayer player)
                || !AllItems.WRENCH.isIn(event.getItemStack())) return false;

        BlockPos pos = event.getPos();
        if (player.isShiftKeyDown() && event.getLevel().getBlockEntity(pos) instanceof MotorizedJointBlockEntity joint) {
            joint.unlinkController();
            player.displayClientMessage(Component.translatable("gearwalkers.joint.control_unlinked"), true);
            consume(event);
            return true;
        }

        BlockEntity target = event.getLevel().getBlockEntity(pos);
        boolean isJoint = target instanceof MotorizedJointBlockEntity;
        boolean isController = target instanceof MechFrequencyControllerBlockEntity;
        if (!isJoint && !isController) return false;

        Selection selection = SELECTIONS.get(player.getUUID());
        if (selection == null || !selection.dimension().equals(player.level().dimension())) {
            SELECTIONS.put(player.getUUID(), new Selection(player.level().dimension(), pos.immutable(), isJoint));
            player.displayClientMessage(Component.translatable(
                    isJoint ? "gearwalkers.joint.control_select_controller" : "gearwalkers.joint.control_select_joint"), true);
            consume(event);
            return true;
        }

        if (selection.pos().equals(pos)) {
            SELECTIONS.remove(player.getUUID());
            player.displayClientMessage(Component.translatable("gearwalkers.joint.control_cancelled"), true);
            consume(event);
            return true;
        }

        if (selection.joint() == isJoint) {
            SELECTIONS.put(player.getUUID(), new Selection(player.level().dimension(), pos.immutable(), isJoint));
            player.displayClientMessage(Component.translatable(
                    isJoint ? "gearwalkers.joint.control_select_controller" : "gearwalkers.joint.control_select_joint"), true);
            consume(event);
            return true;
        }

        BlockEntity selected = event.getLevel().getBlockEntity(selection.pos());
        MotorizedJointBlockEntity joint;
        MechFrequencyControllerBlockEntity controller;
        if (selection.joint()) {
            joint = selected instanceof MotorizedJointBlockEntity selectedJoint ? selectedJoint : null;
            controller = (MechFrequencyControllerBlockEntity) target;
        } else {
            joint = (MotorizedJointBlockEntity) target;
            controller = selected instanceof MechFrequencyControllerBlockEntity selectedController ? selectedController : null;
        }
        if (joint != null && controller != null) {
            joint.linkController(controller);
            player.displayClientMessage(Component.translatable("gearwalkers.joint.control_linked", joint.getControlName()), true);
        }
        SELECTIONS.remove(player.getUUID());
        consume(event);
        return true;
    }

    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        SELECTIONS.remove(event.getEntity().getUUID());
    }

    private static void consume(PlayerInteractEvent.RightClickBlock event) {
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    private record Selection(ResourceKey<Level> dimension, BlockPos pos, boolean joint) {}
}
