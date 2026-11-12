package com.encdata.gearwalkers.client.screens;

import net.minecraft.client.gui.GuiGraphics;

public final class ContainerGuiRenderer {
    private static final int SLOT_SIZE = 18;

    private ContainerGuiRenderer() {}

    public static void renderWindow(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x + 3, y + 3, x + width - 3, y + height - 3, 0xFFC6C6C6);
        graphics.fill(x + 2, y, x + width - 3, y + 3, 0xFFFFFFFF);
        graphics.fill(x, y + 2, x + 3, y + height - 3, 0xFFFFFFFF);
        graphics.fill(x + 3, y + height - 3, x + width - 2, y + height, 0xFF555555);
        graphics.fill(x + width - 3, y + 3, x + width, y + height - 3, 0xFF555555);
        graphics.fill(x + width - 3, y + 2, x + width - 2, y + 3, 0xFFC6C6C6);
        graphics.fill(x + 2, y + height - 3, x + 3, y + height - 2, 0xFFC6C6C6);

        graphics.fill(x + 2, y, x + width - 3, y + 1, 0xFF000000);
        graphics.fill(x + width - 1, y + 3, x + width, y + height - 3, 0xFF000000);
        graphics.fill(x + 3, y + height - 1, x + width - 3, y + height, 0xFF000000);
        graphics.fill(x, y + 2, x + 1, y + height - 3, 0xFF000000);
        graphics.fill(x + 1, y + 1, x + 2, y + 2, 0xFF000000);
        graphics.fill(x + width - 3, y + 1, x + width - 2, y + 2, 0xFF000000);
        graphics.fill(x + width - 2, y + 2, x + width - 1, y + 3, 0xFF000000);
        graphics.fill(x + width - 2, y + height - 3, x + width - 1, y + height - 2, 0xFF000000);
        graphics.fill(x + width - 3, y + height - 2, x + width - 2, y + height - 1, 0xFF000000);
        graphics.fill(x + 1, y + height - 3, x + 2, y + height - 2, 0xFF000000);
        graphics.fill(x + 2, y + height - 2, x + 3, y + height - 1, 0xFF000000);
    }

    public static void renderSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF8B8B8B);
        graphics.fill(x, y, x + SLOT_SIZE, y + 1, 0xFF373737);
        graphics.fill(x, y + 1, x + 1, y + SLOT_SIZE, 0xFF373737);
        graphics.fill(x + SLOT_SIZE - 1, y, x + SLOT_SIZE, y + SLOT_SIZE, 0xFFFFFFFF);
        graphics.fill(x, y + SLOT_SIZE - 1, x + SLOT_SIZE - 1, y + SLOT_SIZE, 0xFFFFFFFF);
        graphics.fill(x + SLOT_SIZE - 1, y, x + SLOT_SIZE, y + 1, 0xFF8C8C8C);
        graphics.fill(x, y + SLOT_SIZE - 1, x + 1, y + SLOT_SIZE, 0xFF8C8C8C);
    }

    public static void renderMachinePanel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, 0xFF8B8B8B);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFFE0E0E0);
    }

    public static void renderPlayerInventorySlots(GuiGraphics graphics, int x, int y) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                renderSlot(graphics, x + 7 + column * SLOT_SIZE, y + 93 + row * SLOT_SIZE);
            }
        }
        for (int column = 0; column < 9; column++) {
            renderSlot(graphics, x + 7 + column * SLOT_SIZE, y + 151);
        }
    }

    public static void renderProgressBar(GuiGraphics graphics, int x, int y, int progress, int capacity) {
        int width = Math.round(79.0F * Math.clamp(progress / (float) capacity, 0.0F, 1.0F));
        graphics.fill(x, y, x + 81, y + 9, 0xFF000000);
        graphics.fill(x + 1, y + 1, x + 80, y + 8, 0xFF373737);
        graphics.fill(x + 2, y + 2, x + 2 + width, y + 7, 0xFF6F6F6F);
        graphics.fill(x + 2, y + 2, x + 2 + width, y + 3, 0xFFB0B0B0);
        graphics.fill(x + 2, y + 6, x + 2 + width, y + 7, 0xFF4B4B4B);
        graphics.fill(x + 3, y + 3, x + 2 + width, y + 6, 0xFF4C8AC4);
    }
}
