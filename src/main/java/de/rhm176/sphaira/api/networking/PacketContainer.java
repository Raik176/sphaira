package de.rhm176.sphaira.api.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class PacketContainer<T> {
    private final ResourceLocation channel;
    //? if >=1.20.5 {
    /*private final net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<PayloadPacketWrapper<T>> type;
    private final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, PayloadPacketWrapper<T>> streamCodec;
    *///?}
    private final Consumer<PacketContext<T>> handler;
    private final PayloadSerializer<FriendlyByteBuf, T> serializer;
    private final NetworkingStage stage;
    private final Class<T> clazz;

    public PacketContainer(ResourceLocation channel, Class<T> clazz, PayloadSerializer<FriendlyByteBuf, T> serializer, Consumer<PacketContext<T>> handler, NetworkingStage stage) {
        this.channel = channel;
        this.serializer = serializer;
        this.handler = handler;
        this.stage = stage;
        this.clazz = clazz;

        //? if >=1.20.5 {
        /*this.type = new net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<>(channel());
        this.streamCodec = net.minecraft.network.protocol.common.custom.CustomPacketPayload.codec(
                (packet, buf) -> serializer.encodeConsumer().accept(buf, packet.packet()),
                (buf) -> new PayloadPacketWrapper<>(type, serializer.decodeFunction().apply(buf))
        );
        *///?}
    }

    //? if >=1.20.5 {
    /*public net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<PayloadPacketWrapper<T>> type() {
        return type;
    }
    public net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, PayloadPacketWrapper<T>> streamCodec() {
        return streamCodec;
    }
    *///?} else {
    public PayloadSerializer<FriendlyByteBuf, T> serializer() {
        return serializer;
    }
    //?}

    public @NotNull ResourceLocation channel() {
        return channel;
    }

    public @NotNull Consumer<PacketContext<T>> handler() {
        return handler;
    }

    public NetworkingStage stage() {
        return stage;
    }

    public Class<T> clazz() {
        return clazz;
    }
}
