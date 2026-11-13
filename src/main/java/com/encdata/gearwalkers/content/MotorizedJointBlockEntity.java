package com.encdata.gearwalkers.content;

import com.simibubi.create.Create;
import com.simibubi.create.content.redstone.link.IRedstoneLinkable;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;
import com.simibubi.create.infrastructure.config.AllConfigs;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.constraint.ConstraintJointAxis;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.service.SimConfigService;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public abstract class MotorizedJointBlockEntity extends BaseJointBlockEntity {
    private static final double MOTOR_TARGET_ANGLE = Math.toRadians(100);
    private static final double MOTOR_SPEED = 2.0;
    private static final double IDLE_MOTOR_DAMPING = 1.5;
    private static final Map<Level, Set<MotorizedJointBlockEntity>> LOADED_JOINTS = new IdentityHashMap<>();

    private final EnumMap<JointControlDirection, Integer> strengths = new EnumMap<>(JointControlDirection.class);
    private final EnumMap<JointControlDirection, FrequencyReceiver> receivers = new EnumMap<>(JointControlDirection.class);
    private final EnumMap<ConstraintJointAxis, Double> motorTargets = new EnumMap<>(ConstraintJointAxis.class);
    private final EnumMap<ConstraintJointAxis, Double> motorStiffness = new EnumMap<>(ConstraintJointAxis.class);
    private final EnumMap<ConstraintJointAxis, Double> motorDamping = new EnumMap<>(ConstraintJointAxis.class);

    @Nullable private UUID registeredSubLevelId;
    @Nullable private UUID linkedControllerId;
    private int jointNumber;
    private long lastMotorAdvanceTick = Long.MIN_VALUE;

    protected MotorizedJointBlockEntity(BlockEntityType<? extends MotorizedJointBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected abstract String getControlType();

    protected abstract String getControlTypeName();

    protected abstract EnumSet<JointControlDirection> getSupportedDirections();

    protected ConstraintJointAxis getMotorAxis(JointControlDirection direction) {
        return switch (direction) {
            case FORWARD, BACKWARD -> ConstraintJointAxis.ANGULAR_X;
            case LEFT, RIGHT -> ConstraintJointAxis.ANGULAR_Z;
        };
    }

    protected boolean hasMotorLimit() {
        return false;
    }

    protected double getMotorTargetAngle() {
        return MOTOR_TARGET_ANGLE;
    }

    protected EnumSet<ConstraintJointAxis> getStabilizedAxes() {
        return EnumSet.noneOf(ConstraintJointAxis.class);
    }

    protected boolean holdsPoseWhenIdle() {
        return false;
    }

    public String getControlName() {
        return getControlTypeName() + " " + jointNumber;
    }

    public int getJointNumber() {
        return jointNumber;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level == null || level.isClientSide) return;
        LOADED_JOINTS.computeIfAbsent(level, $ -> Collections.newSetFromMap(new IdentityHashMap<>())).add(this);
        registerControl((ServerSubLevel) Sable.HELPER.getContaining(this));
        refreshFrequencyInputs();
    }

    @Override
    public void sable$tick(ServerSubLevel subLevel) {
        registerControl(subLevel);
        advanceMotors(subLevel.getLevel().getGameTime());
    }

    @Override
    protected void tickJoint(long gameTime) {
        advanceMotors(gameTime);
        if (gameTime % 5 == 0) {
            for (FrequencyReceiver receiver : receivers.values()) receiver.refreshReceivedStrength();
        }
    }

    private void advanceMotors(long gameTime) {
        if (gameTime == lastMotorAdvanceTick) return;
        lastMotorAdvanceTick = gameTime;
        for (JointControlDirection direction : getSupportedDirections()) {
            if (direction == JointControlDirection.BACKWARD || direction == JointControlDirection.RIGHT) continue;
            int strength = getStrength(direction);
            if (strength == 0) continue;
            ConstraintJointAxis axis = getMotorAxis(direction);
            double target = motorTargets.getOrDefault(axis, 0.0)
                    + strength / 15.0 * MOTOR_SPEED / 20.0;
            double targetLimit = getMotorTargetAngle();
            double nextTarget = Math.clamp(target, -targetLimit, targetLimit);
            if (nextTarget == motorTargets.getOrDefault(axis, 0.0)) continue;
            motorTargets.put(axis, nextTarget);
            applyMotor(axis);
        }
    }

    private int getStrength(JointControlDirection direction) {
        return strengths.getOrDefault(direction, 0) - strengths.getOrDefault(opposite(direction), 0);
    }

    private JointControlDirection opposite(JointControlDirection direction) {
        return switch (direction) {
            case FORWARD -> JointControlDirection.BACKWARD;
            case BACKWARD -> JointControlDirection.FORWARD;
            case LEFT -> JointControlDirection.RIGHT;
            case RIGHT -> JointControlDirection.LEFT;
        };
    }

    private void registerControl(@Nullable ServerSubLevel subLevel) {
        if (subLevel == null) {
            JointControlRegistry.unregister(this);
            registeredSubLevelId = null;
            return;
        }
        if (subLevel.getUniqueId().equals(registeredSubLevelId)) return;
        int registeredId = JointControlRegistry.register(subLevel, this, jointNumber);
        if (jointNumber != registeredId) {
            jointNumber = registeredId;
            setChanged();
            refreshFrequencyInputs();
        }
        registeredSubLevelId = subLevel.getUniqueId();
    }

    public static void refreshControls(Level level) {
        for (var entry : new ArrayList<>(LOADED_JOINTS.entrySet())) {
            if (!entry.getKey().dimension().equals(level.dimension())) continue;
            for (MotorizedJointBlockEntity joint : new ArrayList<>(entry.getValue())) joint.refreshFrequencyInputs();
        }
    }

    public static List<String> getLinkedControlIds(MechFrequencyControllerBlockEntity controller) {
        List<String> controlIds = new ArrayList<>();
        UUID controllerId = controller.getControlId();
        ResourceKey<Level> dimension = controller.getLevel().dimension();
        for (Set<MotorizedJointBlockEntity> joints : LOADED_JOINTS.values()) {
            for (MotorizedJointBlockEntity joint : joints) {
                if (joint.level == null || !joint.level.dimension().equals(dimension)
                        || !controllerId.equals(joint.linkedControllerId) || joint.jointNumber < 1) continue;
                for (JointControlDirection direction : joint.getSupportedDirections()) {
                    controlIds.add(MechControlRegistry.jointControlId(joint.getControlType(), joint.jointNumber, direction));
                }
            }
        }
        return controlIds.stream().distinct().sorted().toList();
    }

    public void linkController(MechFrequencyControllerBlockEntity controller) {
        if (linkedControllerId != null && !linkedControllerId.equals(controller.getControlId())) removeControlsFromController();
        linkedControllerId = controller.getControlId();
        setChanged();
        refreshFrequencyInputs();
    }

    public void unlinkController() {
        removeControlsFromController();
        linkedControllerId = null;
        setChanged();
        refreshFrequencyInputs();
    }

    public void removeControlsFromController() {
        if (assembling || level == null || linkedControllerId == null) return;
        MechFrequencyControllerBlockEntity controller = MechFrequencyControllerBlockEntity.findLoadedController(level, linkedControllerId);
        if (controller == null) return;
        for (JointControlDirection direction : getSupportedDirections()) {
            controller.removeCommand(MechControlRegistry.jointControlId(getControlType(), jointNumber, direction));
        }
    }

    private void refreshFrequencyInputs() {
        if (level == null || level.isClientSide) return;
        MechFrequencyControllerBlockEntity controller = linkedControllerId == null ? null
                : MechFrequencyControllerBlockEntity.findLoadedController(level, linkedControllerId);
        for (JointControlDirection direction : getSupportedDirections()) {
            String controlId = MechControlRegistry.jointControlId(getControlType(), jointNumber, direction);
            MechCommandFrequency command = controller == null || jointNumber < 1 ? null : controller.getCommand(controlId);
            FrequencyReceiver receiver = receivers.computeIfAbsent(direction, key -> new FrequencyReceiver(this, key));
            if (command == null || (command.first().getStack().isEmpty() && command.second().getStack().isEmpty())) {
                receiver.setFrequency(null, null, Frequency.EMPTY, Frequency.EMPTY);
            } else {
                receiver.setFrequency(level, worldPosition, command.first(), command.second());
            }
        }
    }

    private void setCommandStrength(JointControlDirection direction, int strength) {
        if (strengths.getOrDefault(direction, 0) == strength) return;
        strengths.put(direction, strength);
        applyMotor(getMotorAxis(direction));
    }

    private void applyMotor(ConstraintJointAxis axis) {
        if (constraint == null || !constraint.isValid()) return;
        boolean active = false;
        for (JointControlDirection direction : getSupportedDirections()) {
            if (getMotorAxis(direction) == axis && getStrength(direction) != 0) {
                active = true;
                break;
            }
        }
        if (!active && !holdsPoseWhenIdle()) {
            constraint.setMotor(axis, 0.0, 0.0, IDLE_MOTOR_DAMPING, false, 0.0);
            return;
        }
        wakeConnectedBodies();
        constraint.setMotor(axis, motorTargets.getOrDefault(axis, 0.0),
                motorStiffness.getOrDefault(axis, 0.0), motorDamping.getOrDefault(axis, 0.0), false, 0.0);
    }

    private void wakeConnectedBodies() {
        if (level == null || movingSubLevelId == null) return;
        SubLevelContainer subLevels = SubLevelContainer.getContainer(level);
        if (!(subLevels instanceof ServerSubLevelContainer container)) return;
        SubLevel moving = container.getSubLevel(movingSubLevelId);
        if (!(moving instanceof ServerSubLevel movingSubLevel)) return;
        var pipeline = container.physicsSystem().getPipeline();
        pipeline.wakeUp(movingSubLevel);
        SubLevel base = Sable.HELPER.getContaining(this);
        if (base instanceof ServerSubLevel baseSubLevel) pipeline.wakeUp(baseSubLevel);
    }

    private static double inertiaAlong(@Nullable SubLevel subLevel, Vector3d axis) {
        if (!(subLevel instanceof ServerSubLevel serverSubLevel)) return 0.0;
        Vector3d transformedAxis = new Vector3d();
        serverSubLevel.getMassTracker().getInertiaTensor().transform(axis, transformedAxis);
        return transformedAxis.dot(axis);
    }

    @Override
    protected void onConstraintCreated(ServerSubLevel base, ServerSubLevel moving) {
        var config = SimConfigService.INSTANCE.server().physics;
        Quaterniond baseFrame = getJointFrameIn(base, base);
        Quaterniond movingFrame = getJointFrameIn(base, moving);
        EnumSet<ConstraintJointAxis> motorAxes = getStabilizedAxes();
        for (JointControlDirection direction : getSupportedDirections()) {
            motorAxes.add(getMotorAxis(direction));
        }
        for (ConstraintJointAxis axis : motorAxes) {
            Vector3d localAxis = getLocalAxis(axis);
            double inertia = Math.max(10.0, Math.max(
                    inertiaAlong(base, baseFrame.transform(new Vector3d(localAxis))),
                    inertiaAlong(moving, movingFrame.transform(new Vector3d(localAxis)))));
            motorStiffness.put(axis, config.swivelBearingStiffness.get() * inertia);
            motorDamping.put(axis, config.swivelBearingDamping.get() * inertia);
        }
        if (hasMotorLimit()) {
            constraint.setLimit(ConstraintJointAxis.ANGULAR_X, -Math.toRadians(90), Math.toRadians(90));
        }
        for (ConstraintJointAxis axis : motorStiffness.keySet()) applyMotor(axis);
    }

    private Vector3d getLocalAxis(ConstraintJointAxis axis) {
        return switch (axis) {
            case ANGULAR_X -> new Vector3d(1, 0, 0);
            case ANGULAR_Y -> new Vector3d(0, 1, 0);
            case ANGULAR_Z -> new Vector3d(0, 0, 1);
            default -> throw new IllegalArgumentException("motor axis must be angular");
        };
    }

    @Override
    protected void onAssemblyMoved(@Nullable ServerSubLevel movingSubLevel) {
        SubLevel containing = Sable.HELPER.getContaining(this);
        registerControl(containing instanceof ServerSubLevel base ? base : movingSubLevel);
        refreshFrequencyInputs();
    }

    @Override
    protected void onJointRemoved() {
        JointControlRegistry.unregister(this);
        registeredSubLevelId = null;
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide) {
            for (FrequencyReceiver receiver : receivers.values()) receiver.remove();
            JointControlRegistry.unregister(this);
            registeredSubLevelId = null;
            Set<MotorizedJointBlockEntity> joints = LOADED_JOINTS.get(level);
            if (joints != null) {
                joints.remove(this);
                if (joints.isEmpty()) LOADED_JOINTS.remove(level);
            }
        }
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("JointNumber", jointNumber);
        if (linkedControllerId != null) tag.putUUID("LinkedController", linkedControllerId);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        jointNumber = tag.getInt("JointNumber");
        linkedControllerId = tag.hasUUID("LinkedController") ? tag.getUUID("LinkedController") : null;
    }

    private static final class FrequencyReceiver implements IRedstoneLinkable {
        private final MotorizedJointBlockEntity joint;
        private final JointControlDirection direction;
        private Frequency first = Frequency.EMPTY;
        private Frequency second = Frequency.EMPTY;
        @Nullable private Level networkLevel;
        @Nullable private BlockPos location;
        private boolean registered;

        private FrequencyReceiver(MotorizedJointBlockEntity joint, JointControlDirection direction) {
            this.joint = joint;
            this.direction = direction;
        }

        private void setFrequency(@Nullable Level level, @Nullable BlockPos pos, Frequency first, Frequency second) {
            if (registered) remove();
            this.first = first;
            this.second = second;
            networkLevel = level;
            location = pos == null ? null : pos.immutable();
            if (level == null) {
                joint.setCommandStrength(direction, 0);
                return;
            }
            Create.REDSTONE_LINK_NETWORK_HANDLER.addToNetwork(level, this);
            registered = true;
            refreshReceivedStrength();
        }

        private void refreshReceivedStrength() {
            if (!registered || networkLevel == null) return;
            Set<IRedstoneLinkable> network = Create.REDSTONE_LINK_NETWORK_HANDLER.getNetworkOf(networkLevel, this);
            int strength = 0;
            for (IRedstoneLinkable other : network) {
                if (other == this || !other.isAlive() || !withinSableLinkRange(other)) continue;
                strength = Math.max(strength, other.getTransmittedStrength());
            }
            joint.setCommandStrength(direction, strength);
        }

        private boolean withinSableLinkRange(IRedstoneLinkable other) {
            if (networkLevel == null) return false;
            Vector3d receiverPos = JOMLConversion.atCenterOf(getLocation());
            Vector3d transmitterPos = JOMLConversion.atCenterOf(other.getLocation());
            SubLevel receiverSubLevel = Sable.HELPER.getContaining(networkLevel, receiverPos);
            SubLevel transmitterSubLevel = Sable.HELPER.getContaining(networkLevel, transmitterPos);
            if (receiverSubLevel != null) receiverSubLevel.logicalPose().transformPosition(receiverPos);
            if (transmitterSubLevel != null) transmitterSubLevel.logicalPose().transformPosition(transmitterPos);
            int linkRange = AllConfigs.server().logistics.linkRange.get();
            return receiverPos.distanceSquared(transmitterPos) < (double) linkRange * linkRange;
        }

        private void remove() {
            if (!registered || networkLevel == null) return;
            Create.REDSTONE_LINK_NETWORK_HANDLER.removeFromNetwork(networkLevel, this);
            registered = false;
        }

        @Override
        public int getTransmittedStrength() {
            return 0;
        }

        @Override
        public void setReceivedStrength(int power) {
            joint.setCommandStrength(direction, power);
        }

        @Override
        public boolean isListening() {
            return true;
        }

        @Override
        public boolean isAlive() {
            return joint.level != null && !joint.isRemoved() && joint.level.isLoaded(joint.worldPosition)
                    && joint.level.getBlockEntity(joint.worldPosition) == joint;
        }

        @Override
        public Couple<Frequency> getNetworkKey() {
            return Couple.create(first, second);
        }

        @Override
        public BlockPos getLocation() {
            if (location != null) return location;
            return joint.attachmentAnchor == null ? joint.worldPosition : joint.attachmentAnchor;
        }
    }
}
