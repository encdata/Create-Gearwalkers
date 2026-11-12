package com.encdata.gearwalkers.content;

public record ContainerGuiTheme(
        int outerBorder,
        int windowFace,
        int windowHighlight,
        int windowShadow,
        int windowOutline,
        int slotBorder,
        int slotFace,
        int slotShadow,
        int slotHighlight,
        int panelBorder,
        int panelFace,
        int buttonBorder,
        int buttonFace,
        int buttonHover,
        int buttonShadow,
        int firstFrequency,
        int secondFrequency,
        int progressFace,
        int progressHighlight,
        int progressShadow,
        int progressFill,
        int text,
        boolean woodGrain
) {
    public static final ContainerGuiTheme MINECRAFT = new ContainerGuiTheme(
            0xFF000000, 0xFFC6C6C6, 0xFFFFFFFF, 0xFF555555, 0xFF000000,
            0xFF373737, 0xFF8B8B8B, 0xFF373737, 0xFFFFFFFF,
            0xFF8B8B8B, 0xFFE0E0E0,
            0xFF373737, 0xFF8B8B8B, 0xFF9A9A9A, 0xFF555555,
            0xFF9F4852, 0xFF4C6FA8,
            0xFF373737, 0xFFB0B0B0, 0xFF4B4B4B, 0xFF4C8AC4,
            0xFF202020, false
    );

    public static final ContainerGuiTheme DARK_OAK = new ContainerGuiTheme(
            0xFFE2B44F, 0xFF39251A, 0xFF795135, 0xFF24170F, 0xFF1B110B,
            0xFFD6A448, 0xFF4B3020, 0xFF291A12, 0xFF9A7042,
            0xFF67452F, 0xFF4B3020,
            0xFFD6A448, 0xFF553722, 0xFF714A2C, 0xFF291A12,
            0xFF9F4852, 0xFF4C6FA8,
            0xFF291A12, 0xFFB98A4B, 0xFF39251A, 0xFFC38A3D,
            0xFFFFE2A3, true
    );
}
