package de.rhm176.sphaira;

import de.rhm176.sphaira.api.networking.PacketContainer;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Supplier;

public class SphairaCommon {
	public static final String MOD_ID = "sphaira";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static BaseImpl IMPL;

	public static void init(BaseImpl impl) {
        IMPL = impl;
	}

    public interface BaseImpl {
        boolean isModLoaded(String id);

        Path getConfigDir();
        Path getGameDir();

        boolean isDevelopmentEnvironment();

        <T> void registerPayload(PacketContainer<T> container);
        //? if >=1.20.5 {
        /*void sendToServer(net.minecraft.network.protocol.common.custom.CustomPacketPayload payload);
        void sendToPlayer(net.minecraft.network.protocol.common.custom.CustomPacketPayload payload, ServerPlayer player);
        *///?} else {
        void sendToServer(ResourceLocation channel, FriendlyByteBuf payload);
        void sendToPlayer(ResourceLocation channel, FriendlyByteBuf payload, ServerPlayer player);
        //?}

        <T> Set<T> getPlugins(String id, Class<T> entrypointClass, Class<? extends Annotation> annotationClass);

        <T> Supplier<T> register(Registry<T> registry, ResourceLocation id, T element);
    }
}
