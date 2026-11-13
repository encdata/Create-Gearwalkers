package com.encdata.gearwalkers.client.screens;

import com.encdata.gearwalkers.content.ContainerGuiTheme;
import com.encdata.gearwalkers.content.MechCommandFrequency;
import com.encdata.gearwalkers.content.MechControlRegistry;
import com.encdata.gearwalkers.content.MechFrequencyControllerMenu;
import com.encdata.gearwalkers.network.MechFrequencySavePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class MechFrequencyControllerScreen extends AbstractContainerScreen<MechFrequencyControllerMenu> {
    private static final int ROW_HEIGHT = 34;
    private final List<MechCommandFrequency> entries = new ArrayList<>();
    private final List<RowButtons> rowButtons = new ArrayList<>();

    private Button addButton;
    private Button doneButton;
    private Button confirmButton;
    private Button cancelButton;
    private MechControlRegistry.Control editingControl;
    private MechControlRegistry.Control selectedControl;
    private double scroll;

    public MechFrequencyControllerScreen(MechFrequencyControllerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 190;
        imageHeight = 205;
        titleLabelY = 7;
        inventoryLabelY = 10000;
        if (menu.contentHolder != null) {
            for (MechCommandFrequency entry : menu.contentHolder.getCommands()) {
                if (controlIndex(entry.controlId()) >= 0) entries.add(entry);
            }
        }
    }

    public ContainerGuiTheme getTheme() {
        return menu.getTheme();
    }

    public void setTheme(ContainerGuiTheme theme) {
        menu.setTheme(theme);
    }

    @Override
    protected void init() {
        super.init();
        rowButtons.clear();
        addButton = addRenderableWidget(createButton(Component.literal("+"), button -> startAdding(),
                leftPos + 110, topPos + 177, 30, 20));
        doneButton = addRenderableWidget(createButton(Component.translatable("gui.done"), button -> onClose(),
                leftPos + 144, topPos + 177, 38, 20));

        for (MechCommandFrequency entry : entries) {
            Button edit = addRenderableWidget(createButton(Component.translatable("gearwalkers.gui.edit"), button -> startEditing(entry), 0, 0, 32, 18));
            Button remove = addRenderableWidget(createButton(Component.literal("×"), button -> remove(entry), 0, 0, 20, 18));
            rowButtons.add(new RowButtons(entry, edit, remove));
        }
        addButton.active = firstAvailableControl() != null;

        confirmButton = addRenderableWidget(createButton(Component.translatable("gui.done"), button -> confirmEdit(),
                leftPos + 48, topPos + 87, 58, 20));
        cancelButton = addRenderableWidget(createButton(Component.translatable("gui.cancel"), button -> closeEditor(),
                leftPos + 112, topPos + 87, 58, 20));
        setEditorVisible(false);
    }

    private void startAdding() {
        editingControl = null;
        selectedControl = firstAvailableControl();
        if (selectedControl == null) return;
        menu.ghostInventory.setStackInSlot(0, ItemStack.EMPTY);
        menu.ghostInventory.setStackInSlot(1, ItemStack.EMPTY);
        clickMenuButton(MechFrequencyControllerMenu.CLEAR_CONTROL);
        menu.showSlots = true;
        setEditorVisible(true);
    }

    private void startEditing(MechCommandFrequency entry) {
        int controlIndex = controlIndex(entry.controlId());
        if (controlIndex < 0) return;
        editingControl = menu.getControls().get(controlIndex);
        selectedControl = editingControl;
        menu.ghostInventory.setStackInSlot(0, entry.first().getStack());
        menu.ghostInventory.setStackInSlot(1, entry.second().getStack());
        clickMenuButton(menu.getEditButtonId(controlIndex));
        menu.showSlots = true;
        setEditorVisible(true);
    }

    private void confirmEdit() {
        if (selectedControl == null) return;
        ItemStack first = menu.ghostInventory.getStackInSlot(0).copy();
        ItemStack second = menu.ghostInventory.getStackInSlot(1).copy();
        if (editingControl != null && !editingControl.id().equals(selectedControl.id())) {
            int editingIndex = controlIndex(editingControl.id());
            if (editingIndex >= 0) clickMenuButton(menu.getRemoveButtonId(editingIndex));
            entries.removeIf(entry -> entry.controlId().equals(editingControl.id()));
        }
        int selectedIndex = controlIndex(selectedControl.id());
        if (selectedIndex < 0) return;
        clickMenuButton(selectedIndex);
        entries.removeIf(entry -> entry.controlId().equals(selectedControl.id()));
        entries.add(new MechCommandFrequency(selectedControl.id(),
                com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency.of(first),
                com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency.of(second)));
        sendSave();
        rebuildRowButtons();
        closeEditor();
    }

    private void remove(MechCommandFrequency entry) {
        int index = controlIndex(entry.controlId());
        if (index >= 0) clickMenuButton(menu.getRemoveButtonId(index));
        entries.removeIf(current -> current.controlId().equals(entry.controlId()));
        sendSave();
        rebuildRowButtons();
    }

    private void rebuildRowButtons() {
        for (RowButtons row : rowButtons) {
            removeWidget(row.edit());
            removeWidget(row.remove());
        }
        rowButtons.clear();
        for (MechCommandFrequency entry : entries) {
            Button edit = addRenderableWidget(createButton(Component.translatable("gearwalkers.gui.edit"), button -> startEditing(entry), 0, 0, 32, 18));
            Button remove = addRenderableWidget(createButton(Component.literal("×"), button -> remove(entry), 0, 0, 20, 18));
            rowButtons.add(new RowButtons(entry, edit, remove));
        }
        addButton.active = firstAvailableControl() != null;
    }

    private MechControlRegistry.Control firstAvailableControl() {
        for (MechControlRegistry.Control control : menu.getControls()) {
            if (entries.stream().noneMatch(entry -> entry.controlId().equals(control.id()))) return control;
        }
        return null;
    }

    private List<MechControlRegistry.Control> availableControls() {
        List<MechControlRegistry.Control> available = new ArrayList<>();
        for (MechControlRegistry.Control control : menu.getControls()) {
            if ((editingControl != null && editingControl.id().equals(control.id()))
                    || entries.stream().noneMatch(entry -> entry.controlId().equals(control.id()))) available.add(control);
        }
        return available;
    }

    private void cycleCommand(int direction) {
        List<MechControlRegistry.Control> available = availableControls();
        if (available.isEmpty()) return;
        int index = available.indexOf(selectedControl);
        selectedControl = available.get(Math.floorMod(index + direction, available.size()));
    }

    private int controlIndex(String id) {
        for (int i = 0; i < menu.getControls().size(); i++) {
            if (menu.getControls().get(i).id().equals(id)) return i;
        }
        return -1;
    }

    private void closeEditor() {
        editingControl = null;
        selectedControl = null;
        menu.showSlots = false;
        setEditorVisible(false);
    }

    private void setEditorVisible(boolean visible) {
        addButton.visible = !visible;
        doneButton.visible = !visible;
        confirmButton.visible = visible;
        cancelButton.visible = visible;
        confirmButton.active = visible;
        cancelButton.active = visible;
        for (RowButtons row : rowButtons) {
            row.edit().visible = !visible;
            row.remove().visible = !visible;
            row.edit().active = !visible;
            row.remove().active = !visible;
        }
    }

    private void clickMenuButton(int id) {
        if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        ContainerGuiRenderer.renderWindow(graphics, leftPos, topPos, imageWidth, imageHeight, getTheme());
        ContainerGuiRenderer.renderMachinePanel(graphics, leftPos + 7, topPos + 20, imageWidth - 14, imageHeight - 27, getTheme());

        if (selectedControl != null) {
            renderEditor(graphics);
            return;
        }

        int viewportTop = topPos + 28;
        int viewportBottom = topPos + 169;
        graphics.enableScissor(leftPos + 8, viewportTop, leftPos + imageWidth - 8, viewportBottom);
        for (int i = 0; i < entries.size(); i++) {
            MechCommandFrequency entry = entries.get(i);
            int rowY = (int) (viewportTop + i * ROW_HEIGHT - scroll);
            ContainerGuiRenderer.renderMachinePanel(graphics, leftPos + 10, rowY, imageWidth - 20, 30, getTheme());
            graphics.enableScissor(leftPos + 16, rowY + 5, leftPos + 72, rowY + 25);
            graphics.drawScrollingString(font, entry.getName(), leftPos + 16, leftPos + 72, rowY + 10, getTheme().text());
            graphics.disableScissor();
            drawFrequencyItem(graphics, entry.first().getStack(), leftPos + 76, rowY + 6);
            drawFrequencyItem(graphics, entry.second().getStack(), leftPos + 99, rowY + 6);
        }
        graphics.disableScissor();
        positionRowButtons(viewportTop, viewportBottom);
        graphics.drawString(font, Component.translatable("block.gearwalkers.mech_frequency_controller"), leftPos + 12, topPos + 7, getTheme().text(), false);
    }

    private void renderEditor(GuiGraphics graphics) {
        ContainerGuiRenderer.renderMachinePanel(graphics, leftPos + 10, topPos + 23, imageWidth - 20, 86, getTheme());
        graphics.drawString(font, Component.translatable("gearwalkers.gui.command"), leftPos + 15, topPos + 31, getTheme().text(), false);
        ContainerGuiRenderer.renderMachinePanel(graphics, leftPos + 70, topPos + 25, 104, 20, getTheme());
        graphics.enableScissor(leftPos + 72, topPos + 25, leftPos + 172, topPos + 45);
        graphics.drawScrollingString(font, selectedControl.name(), leftPos + 72, leftPos + 172, topPos + 31, getTheme().text());
        graphics.disableScissor();
        graphics.drawString(font, Component.translatable("gearwalkers.gui.create_frequency"), leftPos + 15, topPos + 53, getTheme().text(), false);
        ContainerGuiRenderer.renderFrequencySlot(graphics, leftPos + 80, topPos + 68, true, getTheme());
        ContainerGuiRenderer.renderFrequencySlot(graphics, leftPos + 103, topPos + 68, false, getTheme());
        graphics.drawString(font, Component.translatable("container.inventory"), leftPos + 14, topPos + 113, getTheme().text(), false);
        ContainerGuiRenderer.renderPlayerInventorySlots(graphics, leftPos + 7, topPos + 29, getTheme());
    }

    private void drawFrequencyItem(GuiGraphics graphics, ItemStack stack, int x, int y) {
        if (!stack.isEmpty()) graphics.renderItem(stack, x, y);
    }

    private void positionRowButtons(int viewportTop, int viewportBottom) {
        for (int i = 0; i < rowButtons.size(); i++) {
            RowButtons row = rowButtons.get(i);
            int y = (int) (viewportTop + i * ROW_HEIGHT - scroll + 6);
            boolean visible = y + 18 > viewportTop && y < viewportBottom;
            row.edit().setPosition(leftPos + 126, y);
            row.remove().setPosition(leftPos + 160, y);
            row.edit().visible = visible;
            row.remove().visible = visible;
            row.edit().active = visible;
            row.remove().active = visible;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (selectedControl != null && mouseX >= leftPos + 70 && mouseX <= leftPos + 174
                && mouseY >= topPos + 25 && mouseY <= topPos + 45) {
            if (scrollY != 0) cycleCommand(scrollY > 0 ? -1 : 1);
            return true;
        }
        int top = topPos + 28;
        int bottom = topPos + 169;
        if (selectedControl == null && mouseX >= leftPos && mouseX <= leftPos + imageWidth && mouseY >= top && mouseY <= bottom) {
            double maxScroll = Math.max(0, entries.size() * ROW_HEIGHT - (bottom - top));
            scroll = Math.clamp(scroll - scrollY * 20, 0, maxScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (selectedControl != null && button == 0 && mouseX >= leftPos + 70 && mouseX <= leftPos + 174
                && mouseY >= topPos + 25 && mouseY <= topPos + 45) {
            cycleCommand(1);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {}

    private void sendSave() {
        if (menu.contentHolder != null && minecraft != null && minecraft.player != null) {
            PacketDistributor.sendToServer(new MechFrequencySavePayload(menu.contentHolder.getBlockPos(), List.copyOf(entries)));
        }
    }

    @Override
    public void onClose() {
        sendSave();
        super.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private Button createButton(Component message, Button.OnPress onPress, int x, int y, int width, int height) {
        return new ThemedButton(x, y, width, height, message, onPress);
    }

    private final class ThemedButton extends Button {
        private ThemedButton(int x, int y, int width, int height, Component message, Button.OnPress onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            ContainerGuiRenderer.renderButton(graphics, getX(), getY(), getWidth(), getHeight(), isHoveredOrFocused(), getTheme());
            graphics.drawCenteredString(font, getMessage(), getX() + getWidth() / 2,
                    getY() + (getHeight() - font.lineHeight) / 2 + 1, getTheme().text());
        }
    }

    private record RowButtons(MechCommandFrequency entry, Button edit, Button remove) {}
}
