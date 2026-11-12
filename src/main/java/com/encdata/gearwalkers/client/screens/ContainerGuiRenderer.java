package com.encdata.gearwalkers.client.screens;

import com.encdata.gearwalkers.content.ContainerGuiTheme;
import net.minecraft.client.gui.GuiGraphics;

public final class ContainerGuiRenderer {
    private static final int SLOT_SIZE = 18;

    private ContainerGuiRenderer() {}

    public static void renderWindow(GuiGraphics graphics, int x, int y, int width, int height, ContainerGuiTheme theme) {
        graphics.fill(x, y, x + width, y + height, theme.outerBorder());
        graphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, theme.windowFace());
        graphics.fill(x + 3, y + 3, x + width - 3, y + 4, theme.windowHighlight());
        graphics.fill(x + 3, y + height - 4, x + width - 3, y + height - 3, theme.windowShadow());
        graphics.fill(x + 3, y + 4, x + 4, y + height - 4, theme.windowHighlight());
        graphics.fill(x + width - 4, y + 4, x + width - 3, y + height - 4, theme.windowShadow());
        graphics.fill(x + 1, y + 1, x + width - 1, y + 2, theme.windowOutline());
        graphics.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, theme.windowOutline());
        graphics.fill(x + 1, y + 2, x + 2, y + height - 2, theme.windowOutline());
        graphics.fill(x + width - 2, y + 2, x + width - 1, y + height - 2, theme.windowOutline());
    }

    public static void renderSlot(GuiGraphics graphics, int x, int y, ContainerGuiTheme theme) {
        graphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, theme.slotBorder());
        graphics.fill(x + 1, y + 1, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, theme.slotFace());
        graphics.fill(x + 2, y + 2, x + SLOT_SIZE - 2, y + 3, theme.slotHighlight());
        graphics.fill(x + 2, y + 3, x + 3, y + SLOT_SIZE - 2, theme.slotHighlight());
        graphics.fill(x + 2, y + SLOT_SIZE - 3, x + SLOT_SIZE - 2, y + SLOT_SIZE - 2, theme.slotShadow());
        graphics.fill(x + SLOT_SIZE - 3, y + 3, x + SLOT_SIZE - 2, y + SLOT_SIZE - 2, theme.slotShadow());
    }

    public static void renderFrequencySlot(GuiGraphics graphics, int x, int y, boolean first, ContainerGuiTheme theme) {
        renderSlot(graphics, x, y, theme);
        graphics.fill(x + 4, y + 4, x + SLOT_SIZE - 4, y + SLOT_SIZE - 4,
                first ? theme.firstFrequency() : theme.secondFrequency());
    }

    public static void renderMachinePanel(GuiGraphics graphics, int x, int y, int width, int height, ContainerGuiTheme theme) {
        graphics.fill(x, y, x + width, y + height, theme.panelBorder());
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, theme.panelFace());
        if (theme.woodGrain()) {
            for (int stripeY = y + 5; stripeY < y + height - 2; stripeY += 12)
                graphics.fill(x + 2, stripeY, x + width - 2, stripeY + 1, theme.windowShadow());
        }
    }

    public static void renderButton(GuiGraphics graphics, int x, int y, int width, int height, boolean hovered,
                                    ContainerGuiTheme theme) {
        graphics.fill(x, y, x + width, y + height, theme.buttonBorder());
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, hovered ? theme.buttonHover() : theme.buttonFace());
        graphics.fill(x + 2, y + 2, x + width - 2, y + 3, theme.windowHighlight());
        graphics.fill(x + 2, y + height - 3, x + width - 2, y + height - 2, theme.buttonShadow());
    }

    public static void renderPlayerInventorySlots(GuiGraphics graphics, int x, int y, ContainerGuiTheme theme) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                renderSlot(graphics, x + 7 + column * SLOT_SIZE, y + 93 + row * SLOT_SIZE, theme);
            }
        }
        for (int column = 0; column < 9; column++) {
            renderSlot(graphics, x + 7 + column * SLOT_SIZE, y + 151, theme);
        }
    }

    public static void renderProgressBar(GuiGraphics graphics, int x, int y, int progress, int capacity, ContainerGuiTheme theme) {
        int width = Math.round(79.0F * Math.clamp(progress / (float) capacity, 0.0F, 1.0F));
        graphics.fill(x, y, x + 81, y + 9, theme.windowOutline());
        graphics.fill(x + 1, y + 1, x + 80, y + 8, theme.progressFace());
        graphics.fill(x + 2, y + 2, x + 2 + width, y + 7, theme.progressShadow());
        graphics.fill(x + 2, y + 2, x + 2 + width, y + 3, theme.progressHighlight());
        graphics.fill(x + 3, y + 3, x + 2 + width, y + 6, theme.progressFill());
    }
}
