package tcinventoryscan.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import tcinventoryscan.TcInventoryScanning;

public record ScanSlotPayload(int containerId, int slotIndex) implements CustomPacketPayload {
    public static final Type<ScanSlotPayload> TYPE = new Type<>(TcInventoryScanning.id("scan_slot"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ScanSlotPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, ScanSlotPayload::containerId,
        ByteBufCodecs.VAR_INT, ScanSlotPayload::slotIndex,
        ScanSlotPayload::new
    );

    @Override
    public Type<ScanSlotPayload> type() {
        return TYPE;
    }
}
