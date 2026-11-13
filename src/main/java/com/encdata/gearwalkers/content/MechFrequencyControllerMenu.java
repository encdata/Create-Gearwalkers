package com.encdata.gearwalkers.content;

import com.encdata.gearwalkers.registry.GearwalkersMenus;
import com.simibubi.create.foundation.gui.menu.GhostItemMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MechFrequencyControllerMenu extends GhostItemMenu<MechFrequencyControllerBlockEntity> {
    public static final int CLEAR_CONTROL = Integer.MIN_VALUE;
    public boolean showSlots;
    private ContainerGuiTheme theme = ContainerGuiTheme.DARK_OAK;
    private final List<MechControlRegistry.Control> controls = new ArrayList<>();

    public MechFrequencyControllerMenu(int id, Inventory inventory, RegistryFriendlyByteBuf data) {
        super(GearwalkersMenus.MECH_FREQUENCY_CONTROLLER.get(), id, inventory, data);
        loadControls();
    }

    public MechFrequencyControllerMenu(int id, Inventory inventory, MechFrequencyControllerBlockEntity controller) {
        super(GearwalkersMenus.MECH_FREQUENCY_CONTROLLER.get(), id, inventory, controller);
        loadControls();
    }

    private void loadControls() {
        if (contentHolder != null) controls.addAll(MechControlRegistry.getControls(contentHolder.getMenuControlIds()));
        else controls.addAll(MechControlRegistry.getControls(List.of()));
    }

    public List<MechControlRegistry.Control> getControls() {
        return controls;
    }

    public int getRemoveButtonId(int index) {
        return -(index + 1);
    }

    public int getEditButtonId(int index) {
        return -(controls.size() + index + 1);
    }

    public ContainerGuiTheme getTheme() {
        return theme;
    }

    public void setTheme(ContainerGuiTheme theme) {
        this.theme = Objects.requireNonNull(theme);
    }

    @Override
    protected ItemStackHandler createGhostInventory() {
        return new ItemStackHandler(2);
    }

    @Override
    protected boolean allowRepeats() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected MechFrequencyControllerBlockEntity createOnClient(RegistryFriendlyByteBuf data) {
        ClientLevel level = Minecraft.getInstance().level;
        BlockPos pos = data.readBlockPos();
        CompoundTag savedData = data.readNbt();
        if (level == null) return null;
        MechFrequencyControllerBlockEntity controller;
        if (level.getBlockEntity(pos) instanceof MechFrequencyControllerBlockEntity loadedController) {
            controller = loadedController;
        } else {
            controller = new MechFrequencyControllerBlockEntity(pos, level.getBlockState(pos));
        }
        if (savedData != null) controller.loadForMenu(savedData, data.registryAccess());
        return controller;
    }

    @Override
    protected void addSlots() {
        addPlayerSlots(15, 117);
        addSlot(new FrequencySlot(ghostInventory, 0, 81, 69));
        addSlot(new FrequencySlot(ghostInventory, 1, 104, 69));
    }

    @Override
    protected void addPlayerSlots(int x, int y) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new InventorySlot(playerInventory, column + row * 9 + 9, x - 1 + column * 18, y + 5 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new InventorySlot(playerInventory, column, x - 1 + column * 18, y + 63));
        }
    }

    @Override
    protected void saveData(MechFrequencyControllerBlockEntity controller) {}

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (contentHolder == null || !stillValid(player) || !player.mayBuild()) return false;

        if (id == CLEAR_CONTROL) {
            ghostInventory.setStackInSlot(0, ItemStack.EMPTY);
            ghostInventory.setStackInSlot(1, ItemStack.EMPTY);
            return true;
        }
        if (id >= 0 && id < controls.size()) {
            contentHolder.setCommand(controls.get(id).id(), ghostInventory.getStackInSlot(0), ghostInventory.getStackInSlot(1));
            return true;
        }
        long encoded = -(long) id - 1;
        if (id < 0 && encoded < controls.size()) {
            contentHolder.removeCommand(controls.get((int) encoded).id());
            return true;
        }
        if (id < 0 && encoded >= controls.size() && encoded < controls.size() * 2L) {
            var control = controls.get((int) (encoded - controls.size()));
            var entry = contentHolder.getCommands().stream().filter(row -> row.controlId().equals(control.id())).findFirst().orElse(null);
            if (entry == null) return false;
            ghostInventory.setStackInSlot(0, entry.first().getStack());
            ghostInventory.setStackInSlot(1, entry.second().getStack());
            return true;
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return contentHolder != null && player.distanceToSqr(contentHolder.getBlockPos().getCenter()) <= 64;
    }

    private class InventorySlot extends Slot {
        private InventorySlot(Container inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean isActive() {
            return showSlots;
        }
    }

    private class FrequencySlot extends SlotItemHandler {
        private FrequencySlot(IItemHandler inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean isActive() {
            return showSlots;
        }

        @Override
        public boolean isFake() {
            return true;
        }
    }
}
