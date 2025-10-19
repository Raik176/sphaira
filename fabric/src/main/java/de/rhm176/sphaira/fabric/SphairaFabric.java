package de.rhm176.sphaira.fabric;

import de.rhm176.sphaira.api.networking.*;
import de.rhm176.sphaira.api.resource.IdUtils;
import de.rhm176.sphaira.api.resource.ResourceUtils;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import de.rhm176.sphaira.SphairaCommon;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Entrypoint
public class SphairaFabric implements ModInitializer {
	@Override
	public void onInitialize() {
        ResourceLocation earlyReload = IdUtils.id(SphairaCommon.MOD_ID, "early_reload");
        ServerLifecycleEvents.SERVER_STARTED.register(earlyReload, ResourceUtils::reload);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(earlyReload, (
                server,
                closeableResourceManager,
                bool) -> ResourceUtils.reload(server)
        );

        SphairaCommon.init(new SphairaCommon.BaseImpl() {
            @Override
            public boolean isModLoaded(String id) {
                return FabricLoader.getInstance().isModLoaded(id);
            }

            @Override
            public Path getConfigDir() {
                return FabricLoader.getInstance().getConfigDir();
            }

            @Override
            public Path getGameDir() {
                return FabricLoader.getInstance().getGameDir();
            }

            @Override
            public boolean isDevelopmentEnvironment() {
                return FabricLoader.getInstance().isDevelopmentEnvironment();
            }

            //? if >=1.20.5 {
            /*@Override
            public <T> void registerPayload(PacketContainer<T> container) {
                switch (container.stage()) {
                    case CONFIGURATION -> {
                        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.configurationS2C().register(
                                container.type(),
                                container.streamCodec()
                        );
                        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.configurationC2S().register(
                                container.type(),
                                container.streamCodec()
                        );

                        ServerLoginNetworking.registerGlobalReceiver(
                                container.channel(),
                                (server, handler, understood, buf, synchronizer, responseSender) -> server.execute(() ->
                                        container.handler().accept(new PacketContext<>(null, container.streamCodec().decode(buf).packet(), Side.SERVER))
                                )
                        );
                    }
                    case PLAY -> {
                        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(
                                container.type(),
                                container.streamCodec()
                        );
                        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S().register(
                                container.type(),
                                container.streamCodec()
                        );

                        ServerPlayNetworking.registerGlobalReceiver(
                                container.type(),
                                (payload, context) -> context.server().execute(() ->
                                        container.handler().accept(new PacketContext<>(null, payload.packet(), Side.SERVER))
                                )
                        );
                    }
                }

                // maybe (probably) cursed
                if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                    switch (container.stage()) {
                        case CONFIGURATION -> net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking.registerGlobalReceiver(
                                container.type(),
                                (payload, context) -> context.client().execute(() ->
                                        container.handler().accept(new PacketContext<>(null, payload.packet(), Side.SERVER))
                                )
                        );
                        case PLAY -> ClientPlayNetworking.registerGlobalReceiver(
                                container.type(),
                                (payload, context) -> context.client().execute(() ->
                                        container.handler().accept(new PacketContext<>(null, payload.packet(), Side.SERVER))
                                )
                        );
                    }
                }
            }
            *///?} else {
            @Override
            public <T> void registerPayload(PacketContainer<T> container) {
                switch (container.stage()) {
                    case CONFIGURATION -> {
                        ServerLoginNetworking.registerGlobalReceiver(
                                container.channel(),
                                (server, handler, understood, buf, synchronizer, responseSender) -> server.execute(() ->
                                        container.handler().accept(new PacketContext<>(null, container.serializer().decodeFunction().apply(buf), Side.SERVER))
                                )
                        );
                    }
                    case PLAY -> {
                        ServerPlayNetworking.registerGlobalReceiver(
                                container.channel(),
                                (server, player, handler, buf, sender) ->
                                        container.handler().accept(new PacketContext<>(player, container.serializer().decodeFunction().apply(buf), Side.SERVER))
                        );
                    }
                }

                // maybe (probably) cursed
                if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                    switch (container.stage()) {
                        case CONFIGURATION -> ClientLoginNetworking.registerGlobalReceiver(
                                container.channel(),
                                (client, handler, buf, responseSender) -> {
                                    client.execute(() ->
                                            container.handler().accept(new PacketContext<>(null, container.serializer().decodeFunction().apply(buf), Side.SERVER))
                                    );
                                    return CompletableFuture.completedFuture(null);
                                }
                        );
                        case PLAY -> ClientPlayNetworking.registerGlobalReceiver(
                                container.channel(),
                                (client, handler, buf, responseSender) -> client.execute(() ->
                                        container.handler().accept(new PacketContext<>(null, container.serializer().decodeFunction().apply(buf), Side.SERVER))
                                )
                        );
                    }
                }
            }
            //?}

            //? if >=1.20.5 {
            /*@Override
            public void sendToServer(net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) {
                ClientPlayNetworking.send(payload);
            }

            @Override
            public void sendToPlayer(net.minecraft.network.protocol.common.custom.CustomPacketPayload payload, ServerPlayer player) {
                ServerPlayNetworking.send(player, payload);
            }
            *///?} else {
            @Override
            public void sendToServer(ResourceLocation channel, FriendlyByteBuf payload) {
                ClientPlayNetworking.send(channel, payload);
            }

            @Override
            public void sendToPlayer(ResourceLocation channel, FriendlyByteBuf payload, ServerPlayer player) {
                ServerPlayNetworking.send(player, channel, payload);
            }
            //?}

            @Override
            public <T> Set<T> getPlugins(String id, Class<T> entrypointClass, Class<? extends Annotation> annotationClass) {
                return FabricLoader.getInstance()
                        .getEntrypoints(id, entrypointClass)
                        .stream()
                        .filter(plugin -> {
                            if (!plugin.getClass().isAnnotationPresent(annotationClass)) {
                                SphairaCommon.LOGGER.warn(
                                        "Class {} implements {} but is not annotated with @{}. Ignoring.",
                                        plugin.getClass().getName(), entrypointClass.getSimpleName(), annotationClass.getSimpleName()
                                );
                                return false;
                            }
                            return true;
                        })
                        .collect(Collectors.toSet());
            }

            @Override
            public <T> Supplier<T> register(Registry<T> registry, ResourceLocation id, T element) {
                T registered = Registry.register(registry, id, element);

                return () -> registered;
            }
        });
	}
}
