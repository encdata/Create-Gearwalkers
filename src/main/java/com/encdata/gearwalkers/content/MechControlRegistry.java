package com.encdata.gearwalkers.content;

import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class MechControlRegistry {
    private MechControlRegistry() {}

    public static List<Control> getControls(List<String> jointControlIds) {
        List<Control> controls = new ArrayList<>();
        for (MechCommand command : MechCommand.values()) {
            controls.add(new Control(command.name().toLowerCase(Locale.ROOT), command.getName()));
        }
        jointControlIds.stream().distinct().sorted().forEach(id -> controls.add(new Control(id, getName(id))));
        return controls;
    }

    public static String jointControlId(String type, int number, JointControlDirection direction) {
        return type + "_" + number + "_" + direction.id();
    }

    public static Component getName(String id) {
        for (MechCommand command : MechCommand.values()) {
            if (command.name().equalsIgnoreCase(id)) return command.getName();
        }
        String[] parts = id.split("_");
        if (parts.length >= 3) {
            String direction = switch (parts[parts.length - 1]) {
                case "forward" -> "Forward";
                case "backward" -> "Backward";
                case "left" -> "Left";
                case "right" -> "Right";
                default -> null;
            };
            try {
                int number = Integer.parseInt(parts[parts.length - 2]);
                String type = String.join("_", Arrays.copyOf(parts, parts.length - 2));
                if (direction != null) return jointName(type, number, direction);
            } catch (NumberFormatException ignored) {
                // keep unknown saved controls visible in the editor
            }
        }
        return Component.literal(id);
    }

    private static Component jointName(String type, int number, String direction) {
        String name = Arrays.stream(type.split("_"))
                .map(part -> part.isEmpty() ? part : Character.toUpperCase(part.charAt(0)) + part.substring(1))
                .collect(Collectors.joining(" "));
        return Component.literal(name + " " + number + " " + direction);
    }

    public record Control(String id, Component name) {}
}
