package de.rhm176.sphaira.api.networking;

import de.rhm176.sphaira.SphairaCommon;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class NetworkingUtils {
    private static final Map<Class<?>, PacketContainer<?>> REGISTERED_PAYLOADS = new HashMap<>();

    public static <T> void registerPayload(
            @NotNull ResourceLocation channel, @NotNull PayloadSerializer<FriendlyByteBuf, T> serializer,
            @NotNull Consumer<PacketContext<T>> handler,  NetworkingStage stage, Class<T> packet
    ) {
        if (REGISTERED_PAYLOADS.containsKey(packet))
            return;

        PacketContainer<T> container = new PacketContainer<>(channel, packet, serializer, handler, stage);
        SphairaCommon.IMPL.registerPayload(container);

        REGISTERED_PAYLOADS.put(packet, container);
    }

    public static <T> void sendToServer(T packet) {
        @SuppressWarnings("unchecked")
        PacketContainer<T> container = (PacketContainer<T>) REGISTERED_PAYLOADS.get(packet.getClass());

        if (container == null) {
            throw new IllegalStateException(packet.getClass().getName() + " is not registered");
        }

        //? if >=1.20.5 {
        /*SphairaCommon.IMPL.sendToServer(new PayloadPacketWrapper<>(REGISTERED_PAYLOADS.get(packet.getClass()).type(), packet));
        *///?} else {
        FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.EMPTY_BUFFER);
        container.serializer().encodeConsumer().accept(buf, packet);
        SphairaCommon.IMPL.sendToServer(container.channel(), buf);
        //?}
    }

    public static <T> void sendToPlayer(T packet, ServerPlayer player) {
        @SuppressWarnings("unchecked")
        PacketContainer<T> container = (PacketContainer<T>) REGISTERED_PAYLOADS.get(packet.getClass());

        if (container == null) {
            throw new IllegalStateException(packet.getClass().getName() + " is not registered");
        }

        //? if >=1.20.5 {
        /*SphairaCommon.IMPL.sendToPlayer(
                new PayloadPacketWrapper<>(REGISTERED_PAYLOADS.get(packet.getClass()).type(), packet),
                player
        );
        *///?} else {
        FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.EMPTY_BUFFER);
        container.serializer().encodeConsumer().accept(buf, packet);
        SphairaCommon.IMPL.sendToPlayer(container.channel(), buf, player);
        //?}
    }

    public static void broadcast(Object packet, MinecraftServer server) {
        if (REGISTERED_PAYLOADS.get(packet.getClass()) == null) {
            throw new IllegalStateException(packet.getClass().getName() + " is not registered");
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendToPlayer(packet, player);
        }
    }
}
