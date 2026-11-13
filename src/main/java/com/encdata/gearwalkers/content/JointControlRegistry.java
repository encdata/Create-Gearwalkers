package com.encdata.gearwalkers.content;

import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class JointControlRegistry {
    private static final Map<ServerLevel, Map<UUID, Map<Integer, MotorizedJointBlockEntity>>> CONTROLS = new IdentityHashMap<>();

    private JointControlRegistry() {}

    public static int register(ServerSubLevel ship, MotorizedJointBlockEntity joint, int savedId) {
        Map<UUID, Map<Integer, MotorizedJointBlockEntity>> ships = CONTROLS.computeIfAbsent(
                ship.getLevel(), $ -> new LinkedHashMap<>());
        UUID oldShipId = null;
        Integer oldId = null;
        for (var shipEntry : ships.entrySet()) {
            for (var controlEntry : shipEntry.getValue().entrySet()) {
                if (controlEntry.getValue() == joint) {
                    oldShipId = shipEntry.getKey();
                    oldId = controlEntry.getKey();
                    break;
                }
            }
            if (oldId != null) break;
        }
        if (ship.getUniqueId().equals(oldShipId) && oldId == savedId) return savedId;
        if (oldShipId != null) ships.get(oldShipId).remove(oldId);

        Set<Integer> usedIds = new HashSet<>();
        for (SubLevel connected : SubLevelHelper.getConnectedChain(ship)) {
            Map<Integer, MotorizedJointBlockEntity> controls = ships.get(connected.getUniqueId());
            if (controls != null) usedIds.addAll(controls.keySet());
        }
        int id = savedId;
        if (id < 1 || usedIds.contains(id)) {
            id = 1;
            while (usedIds.contains(id)) id++;
        }

        ships.computeIfAbsent(ship.getUniqueId(), $ -> new LinkedHashMap<>()).put(id, joint);
        return id;
    }

    public static void unregister(MotorizedJointBlockEntity joint) {
        for (var levelIterator = CONTROLS.entrySet().iterator(); levelIterator.hasNext();) {
            var levelEntry = levelIterator.next();
            for (var shipIterator = levelEntry.getValue().entrySet().iterator(); shipIterator.hasNext();) {
                Map<Integer, MotorizedJointBlockEntity> controls = shipIterator.next().getValue();
                controls.values().removeIf(registered -> registered == joint);
                if (controls.isEmpty()) shipIterator.remove();
            }
            if (levelEntry.getValue().isEmpty()) levelIterator.remove();
        }
    }

    public static List<MotorizedJointBlockEntity> getControls(ServerSubLevel ship) {
        Map<UUID, Map<Integer, MotorizedJointBlockEntity>> ships = CONTROLS.get(ship.getLevel());
        if (ships == null) return List.of();
        Set<MotorizedJointBlockEntity> controls = Collections.newSetFromMap(new IdentityHashMap<>());
        for (SubLevel connected : SubLevelHelper.getConnectedChain(ship)) {
            Map<Integer, MotorizedJointBlockEntity> connectedControls = ships.get(connected.getUniqueId());
            if (connectedControls != null) controls.addAll(connectedControls.values());
        }
        return new ArrayList<>(controls);
    }
}
