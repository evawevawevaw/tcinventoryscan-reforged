package tcinventoryscan.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import tcinventoryscan.TcInventoryScanning;

public record ScanSelfPayload() implements CustomPacketPayload {
    public static final Type<ScanSelfPayload> TYPE = new Type<>(TcInventoryScanning.id("scan_self"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ScanSelfPayload> STREAM_CODEC = StreamCodec.unit(new ScanSelfPayload());

    @Override
    public Type<ScanSelfPayload> type() {
        return TYPE;
    }
}
