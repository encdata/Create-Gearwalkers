package com.encdata.gearwalkers.content;

import com.simibubi.create.content.contraptions.AssemblyException;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.api.physics.constraint.ConstraintJointAxis;
import dev.ryanhcode.sable.api.physics.constraint.GenericConstraintConfiguration;
import dev.ryanhcode.sable.api.physics.constraint.GenericConstraintHandle;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.util.SimAssemblyHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class BaseJointBlockEntity extends BlockEntity implements BlockEntitySubLevelActor {
    @Nullable protected UUID movingSubLevelId;
    @Nullable protected BlockPos movingAnchor;
    @Nullable protected BlockPos attachmentAnchor;
    @Nullable protected Vector3d movingJointAnchor;
    @Nullable protected GenericConstraintHandle constraint;
    protected boolean assembling;

    protected BaseJointBlockEntity(BlockEntityType<? extends BaseJointBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BaseJointBlockEntity joint) {
        if (level.isClientSide || joint.movingSubLevelId == null) return;
        if (joint.constraint != null && !joint.constraint.isValid()) joint.constraint = null;
        if (joint.constraint == null) joint.refreshConstraint();
        joint.tickJoint(level.getGameTime());
    }

    @Override
    public void sable$tick(ServerSubLevel subLevel) {}

    protected void tickJoint(long gameTime) {}

    public boolean assemble(Player player, BlockPos attachedBlock) {
        if (movingSubLevelId != null || !(level instanceof ServerLevel)) return false;

        Direction attachmentFace = Direction.getNearest(attachedBlock.getX() - worldPosition.getX(),
                attachedBlock.getY() - worldPosition.getY(), attachedBlock.getZ() - worldPosition.getZ());
        if (!worldPosition.relative(attachmentFace).equals(attachedBlock)) {
            player.displayClientMessage(Component.translatable("gearwalkers.joint.place_against_block"), true);
            return false;
        }

        try {
            SimAssemblyHelper.AssemblyResult result = SimAssemblyHelper.assembleFromSingleBlock(level,
                    worldPosition, attachedBlock, false, false);
            if (result == null) return false;

            ServerSubLevel movingSubLevel = (ServerSubLevel) result.subLevel();
            attachmentAnchor = attachedBlock;
            movingSubLevelId = movingSubLevel.getUniqueId();
            movingAnchor = attachedBlock.offset(result.offset());
            Vector3d attachmentNormal = JOMLConversion.toJOML(Vec3.atLowerCornerOf(attachmentFace.getNormal()));
            movingJointAnchor = JOMLConversion.toJOML(attachedBlock.getCenter())
                    .fma(-0.5, attachmentNormal)
                    .add(result.offset().getX(), result.offset().getY(), result.offset().getZ());
            level.setBlock(worldPosition, getBlockState().setValue(BaseJointBlock.ASSEMBLED, true), 3);
            onAssemblyMoved(movingSubLevel);
            setChanged();
            refreshConstraint();
            return true;
        } catch (AssemblyException exception) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(Component.literal(exception.getMessage()), true);
            }
            return false;
        }
    }

    public boolean isAssembled() {
        return movingSubLevelId != null;
    }

    public void refreshConstraint() {
        if (level == null || level.isClientSide || movingSubLevelId == null || attachmentAnchor == null || movingJointAnchor == null) return;
        SubLevelContainer subLevels = SubLevelContainer.getContainer(level);
        if (!(subLevels instanceof ServerSubLevelContainer container)) return;
        SubLevel moving = container.getSubLevel(movingSubLevelId);
        if (!(moving instanceof ServerSubLevel movingSubLevel)) return;

        SubLevel containing = Sable.HELPER.getContaining(this);
        if (!(containing instanceof ServerSubLevel base) || base == movingSubLevel) return;
        if (constraint != null) constraint.remove();

        Direction attachmentFace = Direction.getNearest(attachmentAnchor.getX() - worldPosition.getX(),
                attachmentAnchor.getY() - worldPosition.getY(), attachmentAnchor.getZ() - worldPosition.getZ());
        Vector3d attachmentNormal = JOMLConversion.toJOML(Vec3.atLowerCornerOf(attachmentFace.getNormal()));
        Vector3d baseAnchor = JOMLConversion.toJOML(worldPosition.getCenter()).fma(0.5, attachmentNormal);
        Quaterniond baseFrame = getJointFrameIn(base, base);
        Quaterniond movingFrame = getJointFrameIn(base, movingSubLevel);
        constraint = container.physicsSystem().getPipeline().addConstraint(base, movingSubLevel,
                new GenericConstraintConfiguration(baseAnchor, movingJointAnchor, baseFrame, movingFrame, getLockedAxes()));
        constraint.setContactsEnabled(false);
        onConstraintCreated(base, movingSubLevel);
    }

    protected abstract Vector3d getJointAxis();

    protected Quaterniond getJointFrame() {
        return new Quaterniond().rotationTo(new Vector3d(1, 0, 0), getJointAxis());
    }

    protected Quaterniond getJointFrameIn(ServerSubLevel reference, ServerSubLevel body) {
        Quaterniond worldFrame = new Quaterniond(reference.logicalPose().orientation()).mul(getJointFrame());
        return new Quaterniond(body.logicalPose().orientation()).conjugate().mul(worldFrame).normalize();
    }

    protected abstract EnumSet<ConstraintJointAxis> getLockedAxes();

    protected void onConstraintCreated(ServerSubLevel base, ServerSubLevel moving) {}

    protected void onAssemblyMoved(@Nullable ServerSubLevel movingSubLevel) {}

    public void beforeAssemblyMove() {
        assembling = true;
    }

    public void afterAssemblyMove() {
        assembling = false;
        SubLevelContainer container = level == null ? null : SubLevelContainer.getContainer(level);
        SubLevel moving = container == null || movingSubLevelId == null ? null : container.getSubLevel(movingSubLevelId);
        onAssemblyMoved(moving instanceof ServerSubLevel serverMoving ? serverMoving : null);
        refreshConstraint();
    }

    public void removeFromWorld() {
        if (assembling || level == null || level.isClientSide || movingSubLevelId == null) return;
        SubLevelContainer container = SubLevelContainer.getContainer(level);
        SubLevel moving = container == null ? null : container.getSubLevel(movingSubLevelId);
        if (moving != null && movingAnchor != null && level instanceof ServerLevel serverLevel) {
            BlockPos destination = attachmentAnchor == null ? worldPosition : attachmentAnchor;
            if (constraint != null) constraint.remove();
            constraint = null;
            SimAssemblyHelper.disassembleSubLevel(serverLevel, moving, movingAnchor, destination, Rotation.NONE, false);
        }
        movingSubLevelId = null;
        movingAnchor = null;
        attachmentAnchor = null;
        movingJointAnchor = null;
        onJointRemoved();
        setChanged();
    }

    protected void onJointRemoved() {}

    @Override
    public void setRemoved() {
        if (constraint != null) constraint.remove();
        constraint = null;
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (movingSubLevelId != null) tag.putUUID("MovingSubLevel", movingSubLevelId);
        if (movingAnchor != null) tag.putLong("MovingAnchor", movingAnchor.asLong());
        if (attachmentAnchor != null) tag.putLong("AttachmentAnchor", attachmentAnchor.asLong());
        if (movingJointAnchor != null) {
            tag.putDouble("MovingJointAnchorX", movingJointAnchor.x);
            tag.putDouble("MovingJointAnchorY", movingJointAnchor.y);
            tag.putDouble("MovingJointAnchorZ", movingJointAnchor.z);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        movingSubLevelId = tag.hasUUID("MovingSubLevel") ? tag.getUUID("MovingSubLevel") : null;
        movingAnchor = tag.contains("MovingAnchor") ? BlockPos.of(tag.getLong("MovingAnchor")) : null;
        attachmentAnchor = tag.contains("AttachmentAnchor") ? BlockPos.of(tag.getLong("AttachmentAnchor")) : null;
        movingJointAnchor = tag.contains("MovingJointAnchorX")
                ? new Vector3d(tag.getDouble("MovingJointAnchorX"), tag.getDouble("MovingJointAnchorY"), tag.getDouble("MovingJointAnchorZ"))
                : null;
    }

    @Override
    public @Nullable Iterable<@NotNull SubLevel> sable$getConnectionDependencies() {
        if (level == null || movingSubLevelId == null) return null;
        SubLevelContainer container = SubLevelContainer.getContainer(level);
        SubLevel moving = container == null ? null : container.getSubLevel(movingSubLevelId);
        return moving == null ? null : List.of(moving);
    }
}
