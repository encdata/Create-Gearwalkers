package com.encdata.gearwalkers.content;

import com.encdata.gearwalkers.registry.GearwalkersBlockEntities;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MechFrequencyControllerBlockEntity extends BlockEntity {
    private final Map<MechCommand, MechCommandFrequency> commands = new LinkedHashMap<>();

    public MechFrequencyControllerBlockEntity(BlockPos pos, BlockState state) {
        super(GearwalkersBlockEntities.MECH_FREQUENCY_CONTROLLER.get(), pos, state);
    }

    public List<MechCommandFrequency> getCommands() {
        return new ArrayList<>(commands.values());
    }

    public CompoundTag saveForMenu(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    public void loadForMenu(CompoundTag tag, HolderLookup.Provider registries) {
        loadAdditional(tag, registries);
    }

    public void setCommand(MechCommand command, ItemStack first, ItemStack second) {
        commands.put(command, new MechCommandFrequency(command,
                RedstoneLinkNetworkHandler.Frequency.of(single(first)),
                RedstoneLinkNetworkHandler.Frequency.of(single(second))));
        setChanged();
        syncBlockEntity();
    }

    public void removeCommand(MechCommand command) {
        if (commands.remove(command) == null) return;
        setChanged();
        syncBlockEntity();
    }

    public void replaceCommands(List<MechCommandFrequency> entries) {
        commands.clear();
        for (MechCommandFrequency entry : entries) {
            if (entry.command() == null || commands.containsKey(entry.command())) continue;
            commands.put(entry.command(), new MechCommandFrequency(entry.command(),
                    RedstoneLinkNetworkHandler.Frequency.of(single(entry.first().getStack())),
                    RedstoneLinkNetworkHandler.Frequency.of(single(entry.second().getStack()))));
        }
        setChanged();
        syncBlockEntity();
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
        ListTag entries = new ListTag();
        for (MechCommandFrequency entry : commands.values()) {
            CompoundTag row = new CompoundTag();
            row.putString("command", entry.command().name());
            row.put("first", entry.first().getStack().saveOptional(registries));
            row.put("second", entry.second().getStack().saveOptional(registries));
            entries.add(row);
        }
        tag.put("commands", entries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        commands.clear();
        ListTag entries = tag.getList("commands", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag row = entries.getCompound(i);
            try {
                MechCommand command = MechCommand.valueOf(row.getString("command"));
                ItemStack first = ItemStack.parseOptional(registries, row.getCompound("first"));
                ItemStack second = ItemStack.parseOptional(registries, row.getCompound("second"));
                commands.put(command, new MechCommandFrequency(command,
                        RedstoneLinkNetworkHandler.Frequency.of(first),
                        RedstoneLinkNetworkHandler.Frequency.of(second)));
            } catch (IllegalArgumentException ignored) {
                // ignore entries written by command names removed from the mod
            }
        }
    }
}
