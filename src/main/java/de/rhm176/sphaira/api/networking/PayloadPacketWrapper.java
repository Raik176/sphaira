package de.rhm176.sphaira.api.networking;

//? if >=1.20.4 {
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

//? if >=1.20.5 {
public record PayloadPacketWrapper<T>(Type<? extends CustomPacketPayload> type, T packet) implements CustomPacketPayload {

}
//?} else {
/*public record PayloadPacketWrapper<T>(ResourceLocation channel, BiConsumer<FriendlyByteBuf, T> encoder, T packet) implements CustomPacketPayload {
    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        encoder.accept(friendlyByteBuf, this.packet);
    }

    @Override
    public @NotNull ResourceLocation id() {
        return channel;
    }
}
*///?}
//?}