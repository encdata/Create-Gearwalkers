package com.encdata.gearwalkers.network;

import com.encdata.gearwalkers.Gearwalkers;
import com.encdata.gearwalkers.content.MechCommand;
import com.encdata.gearwalkers.content.MechCommandFrequency;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record MechFrequencySavePayload(BlockPos pos, List<MechCommandFrequency> entries) implements CustomPacketPayload {
    public static final Type<MechFrequencySavePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Gearwalkers.MOD_ID, "save_mech_frequencies"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MechFrequencySavePayload> STREAM_CODEC = StreamCodec.of(
            MechFrequencySavePayload::write, MechFrequencySavePayload::read);

    private static void write(RegistryFriendlyByteBuf buffer, MechFrequencySavePayload payload) {
        BlockPos.STREAM_CODEC.encode(buffer, payload.pos());
        buffer.writeVarInt(payload.entries().size());
        for (MechCommandFrequency entry : payload.entries()) {
            buffer.writeVarInt(entry.command().ordinal());
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, entry.first().getStack());
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, entry.second().getStack());
        }
    }

    private static MechFrequencySavePayload read(RegistryFriendlyByteBuf buffer) {
        BlockPos pos = BlockPos.STREAM_CODEC.decode(buffer);
        int size = buffer.readVarInt();
        if (size < 0 || size > MechCommand.values().length) throw new IllegalArgumentException("invalid mech command count: " + size);
        List<MechCommandFrequency> entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            int commandId = buffer.readVarInt();
            if (commandId < 0 || commandId >= MechCommand.values().length) throw new IllegalArgumentException("invalid mech command: " + commandId);
            ItemStack first = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
            ItemStack second = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
            entries.add(new MechCommandFrequency(MechCommand.values()[commandId],
                    RedstoneLinkNetworkHandler.Frequency.of(first), RedstoneLinkNetworkHandler.Frequency.of(second)));
        }
        return new MechFrequencySavePayload(pos, entries);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
