package com.encdata.gearwalkers.content;

import com.encdata.gearwalkers.registry.GearwalkersBlockEntities;
import dev.ryanhcode.sable.api.physics.constraint.ConstraintJointAxis;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.util.EnumSet;

public class HingeBlockEntity extends MotorizedJointBlockEntity {
    public HingeBlockEntity(BlockPos pos, BlockState state) {
        super(GearwalkersBlockEntities.HINGE.get(), pos, state);
    }

    @Override
    protected String getControlType() {
        return "hinge";
    }

    @Override
    protected String getControlTypeName() {
        return "Hinge";
    }

    @Override
    protected EnumSet<JointControlDirection> getSupportedDirections() {
        return EnumSet.of(JointControlDirection.FORWARD, JointControlDirection.BACKWARD);
    }

    @Override
    protected Vector3d getJointAxis() {
        Direction facing = getBlockState().getValue(BaseJointBlock.FACING);
        Vector3d axis = JOMLConversion.toJOML(Vec3.atLowerCornerOf(facing.getNormal()));
        if (facing.getAxis().isHorizontal()) axis.set(axis.z, 0, -axis.x);
        return axis;
    }

    @Override
    protected EnumSet<ConstraintJointAxis> getLockedAxes() {
        return EnumSet.of(ConstraintJointAxis.LINEAR_X, ConstraintJointAxis.LINEAR_Y, ConstraintJointAxis.LINEAR_Z,
                ConstraintJointAxis.ANGULAR_Y, ConstraintJointAxis.ANGULAR_Z);
    }

    @Override
    protected boolean hasMotorLimit() {
        return true;
    }

    @Override
    protected double getMotorTargetAngle() {
        return Math.toRadians(90);
    }

    @Override
    protected boolean holdsPoseWhenIdle() {
        return true;
    }
}
