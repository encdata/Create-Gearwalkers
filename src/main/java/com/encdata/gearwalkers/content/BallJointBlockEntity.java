package com.encdata.gearwalkers.content;

import com.encdata.gearwalkers.registry.GearwalkersBlockEntities;
import dev.ryanhcode.sable.api.physics.constraint.ConstraintJointAxis;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3d;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.EnumSet;

public class BallJointBlockEntity extends MotorizedJointBlockEntity {
    public BallJointBlockEntity(BlockPos pos, BlockState state) {
        super(GearwalkersBlockEntities.BALL_JOINT.get(), pos, state);
    }

    @Override
    protected Vector3d getJointAxis() {
        Direction connectionDirection = attachmentAnchor == null
                ? getBlockState().getValue(BaseJointBlock.FACING)
                : Direction.getNearest(attachmentAnchor.getX() - worldPosition.getX(),
                        attachmentAnchor.getY() - worldPosition.getY(), attachmentAnchor.getZ() - worldPosition.getZ());
        return JOMLConversion.toJOML(Vec3.atLowerCornerOf(connectionDirection.getNormal()));
    }

    @Override
    protected Quaterniond getJointFrame() {
        Vector3d axis = getJointAxis().normalize();
        Vector3d xAxis = JOMLConversion.toJOML(Vec3.atLowerCornerOf(
                getBlockState().getValue(BaseJointBlock.FACING).getNormal()));
        xAxis.fma(-xAxis.dot(axis), axis);

        if (xAxis.lengthSquared() < 1.0E-8) {
            Direction fallback = axis.x != 0 ? Direction.UP : Direction.EAST;
            xAxis.set(fallback.getStepX(), fallback.getStepY(), fallback.getStepZ());
            xAxis.fma(-xAxis.dot(axis), axis);
        }

        xAxis.normalize();
        Vector3d zAxis = xAxis.cross(axis, new Vector3d()).normalize();
        return new Matrix3d()
                .setColumn(0, xAxis)
                .setColumn(1, axis)
                .setColumn(2, zAxis)
                .getNormalizedRotation(new Quaterniond());
    }

    @Override
    protected EnumSet<ConstraintJointAxis> getLockedAxes() {
        return EnumSet.of(ConstraintJointAxis.LINEAR_X, ConstraintJointAxis.LINEAR_Y, ConstraintJointAxis.LINEAR_Z,
                ConstraintJointAxis.ANGULAR_Y);
    }

    @Override
    protected String getControlType() {
        return "ball_joint";
    }

    @Override
    protected String getControlTypeName() {
        return "Ball Joint";
    }

    @Override
    protected EnumSet<JointControlDirection> getSupportedDirections() {
        return EnumSet.allOf(JointControlDirection.class);
    }

    @Override
    protected boolean holdsPoseWhenIdle() {
        return true;
    }

}
