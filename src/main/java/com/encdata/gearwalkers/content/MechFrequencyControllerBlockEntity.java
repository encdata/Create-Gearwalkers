package com.encdata.gearwalkers.content;

import com.encdata.gearwalkers.registry.GearwalkersBlockEntities;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MechFrequencyControllerBlockEntity extends BlockEntity {
    private static final Map<Level, Set<MechFrequencyControllerBlockEntity>> LOADED_CONTROLLERS = new IdentityHashMap<>();
    private final Map<String, MechCommandFrequency> commands = new LinkedHashMap<>();
    @Nullable private UUID controlId;
    @Nullable private List<String> menuControlIds;

    public MechFrequencyControllerBlockEntity(BlockPos pos, BlockState state) {
        super(GearwalkersBlockEntities.MECH_FREQUENCY_CONTROLLER.get(), pos, state);
    }

    public List<MechCommandFrequency> getCommands() {
        return new ArrayList<>(commands.values());
    }

    public MechCommandFrequency getCommand(String controlId) {
        return commands.get(controlId);
    }

    public List<String> getMenuControlIds() {
        return menuControlIds == null ? MotorizedJointBlockEntity.getLinkedControlIds(this) : menuControlIds;
    }

    public UUID getControlId() {
        if (controlId == null) {
            controlId = UUID.randomUUID();
            setChanged();
        }
        return controlId;
    }

    public static MechFrequencyControllerBlockEntity findLoadedController(Level level, UUID controlId) {
        ResourceKey<Level> dimension = level.dimension();
        for (Set<MechFrequencyControllerBlockEntity> controllers : LOADED_CONTROLLERS.values()) {
            for (MechFrequencyControllerBlockEntity controller : controllers) {
                if (controller.level != null && controller.level.dimension().equals(dimension)
                        && controller.getControlId().equals(controlId)) return controller;
            }
        }
        return null;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level == null || level.isClientSide) return;
        LOADED_CONTROLLERS.computeIfAbsent(level, $ -> Collections.newSetFromMap(new IdentityHashMap<>())).add(this);
        MotorizedJointBlockEntity.refreshControls(level);
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide) {
            Set<MechFrequencyControllerBlockEntity> controllers = LOADED_CONTROLLERS.get(level);
            if (controllers != null) {
                controllers.remove(this);
                if (controllers.isEmpty()) LOADED_CONTROLLERS.remove(level);
            }
            MotorizedJointBlockEntity.refreshControls(level);
        }
        super.setRemoved();
    }

    public CompoundTag saveForMenu(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        ListTag registeredControls = new ListTag();
        for (String controlId : MotorizedJointBlockEntity.getLinkedControlIds(this)) {
            registeredControls.add(StringTag.valueOf(controlId));
        }
        tag.put("RegisteredControls", registeredControls);
        return tag;
    }

    public void loadForMenu(CompoundTag tag, HolderLookup.Provider registries) {
        loadAdditional(tag, registries);
        ListTag registeredControls = tag.getList("RegisteredControls", CompoundTag.TAG_STRING);
        menuControlIds = new ArrayList<>(registeredControls.size());
        for (int i = 0; i < registeredControls.size(); i++) menuControlIds.add(registeredControls.getString(i));
    }

    public void setCommand(String controlId, ItemStack first, ItemStack second) {
        commands.put(controlId, new MechCommandFrequency(controlId,
                RedstoneLinkNetworkHandler.Frequency.of(single(first)),
                RedstoneLinkNetworkHandler.Frequency.of(single(second))));
        setChanged();
        syncBlockEntity();
        refreshJoints();
    }

    public void removeCommand(String controlId) {
        if (commands.remove(controlId) == null) return;
        setChanged();
        syncBlockEntity();
        refreshJoints();
    }

    public void replaceCommands(List<MechCommandFrequency> entries) {
        commands.clear();
        for (MechCommandFrequency entry : entries) {
            if (entry.controlId() == null || commands.containsKey(entry.controlId())) continue;
            commands.put(entry.controlId(), new MechCommandFrequency(entry.controlId(),
                    RedstoneLinkNetworkHandler.Frequency.of(single(entry.first().getStack())),
                    RedstoneLinkNetworkHandler.Frequency.of(single(entry.second().getStack()))));
        }
        setChanged();
        syncBlockEntity();
        refreshJoints();
    }

    private void refreshJoints() {
        if (level != null && !level.isClientSide) MotorizedJointBlockEntity.refreshControls(level);
    }

    private static ItemStack single(ItemStack stack) {
        ItemStack copy = stack.copy();
        if (!copy.isEmpty()) copy.setCount(1);
        return copy;
    }

    private void syncBlockEntity() {
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (controlId != null) tag.putUUID("ControlId", controlId);
        ListTag entries = new ListTag();
        for (MechCommandFrequency entry : commands.values()) {
            CompoundTag row = new CompoundTag();
            row.putString("controlId", entry.controlId());
            row.put("first", entry.first().getStack().saveOptional(registries));
            row.put("second", entry.second().getStack().saveOptional(registries));
            entries.add(row);
        }
        tag.put("commands", entries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        controlId = tag.hasUUID("ControlId") ? tag.getUUID("ControlId") : null;
        commands.clear();
        ListTag entries = tag.getList("commands", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag row = entries.getCompound(i);
            try {
                String controlId = row.contains("controlId") ? row.getString("controlId")
                        : MechCommand.valueOf(row.getString("command")).name().toLowerCase(Locale.ROOT);
                ItemStack first = ItemStack.parseOptional(registries, row.getCompound("first"));
                ItemStack second = ItemStack.parseOptional(registries, row.getCompound("second"));
                commands.put(controlId, new MechCommandFrequency(controlId,
                        RedstoneLinkNetworkHandler.Frequency.of(first),
                        RedstoneLinkNetworkHandler.Frequency.of(second)));
            } catch (IllegalArgumentException ignored) {
                // ignore entries written by command names removed from the mod
            }
        }
    }
}
