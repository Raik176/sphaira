package de.rhm176.sphaira.neoforge;

import de.rhm176.sphaira.SphairaCommon;
import de.rhm176.sphaira.api.networking.PacketContext;
import de.rhm176.sphaira.api.networking.Side;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.minecraft.core.Registry;
import de.rhm176.sphaira.api.networking.PacketContainer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.function.Supplier;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import java.lang.annotation.Annotation;
import java.nio.file.Path;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.client.gui.screens.Screen;
import java.util.function.Function;
import de.rhm176.sphaira.api.ConfigUtils;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforgespi.language.ModFileScanData;
import java.util.stream.Collectors;

//? if >=1.20.6 {
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jetbrains.annotations.NotNull;
import net.minecraft.client.Minecraft;
//?} else
/*import net.neoforged.neoforge.client.ConfigScreenHandler;*/

import org.objectweb.asm.Type;

@Mod(SphairaCommon.MOD_ID)
public class SphairaNeoForge {
    //? if >=1.20.4 {
    private record PacketRegistrationContainer<T>(PacketContainer<T> container) {
        //? if >=1.20.5 {
        void register(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent event) {
            var registrar = event.registrar(container.channel().getNamespace()).optional();

            switch (container.stage()) {
                case CONFIGURATION ->
                        registrar.configurationBidirectional(container.type(), container.streamCodec(), (payload, ctx) -> {
                            if (ctx.flow().getReceptionSide().isClient()) {
                                container.handler().accept(new PacketContext<>(null, payload.packet(), Side.CLIENT));
                            } else if (ctx.player() instanceof ServerPlayer serverPlayer) {
                                container.handler().accept(new PacketContext<>(serverPlayer, payload.packet(), Side.SERVER));
                            }
                        });
                case PLAY ->
                        registrar.playBidirectional(container.type(), container.streamCodec(), (payload, ctx) -> {
                            if (ctx.flow().getReceptionSide().isClient()) {
                                container.handler().accept(new PacketContext<>(null, payload.packet(), Side.CLIENT));
                            } else if (ctx.player() instanceof ServerPlayer serverPlayer) {
                                container.handler().accept(new PacketContext<>(serverPlayer, payload.packet(), Side.SERVER));
                            }
                        });
            }
        //?} else {
        /*void register(net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent event) {
            var registrar = event.registrar(container.channel().getNamespace()).optional();

            switch (container.stage()) {
                case CONFIGURATION ->
                        registrar.configuration(container.channel(), friendlyByteBuf -> new de.rhm176.sphaira.api.networking.PayloadPacketWrapper<>(
                                container.channel(),
                                container.serializer().encodeConsumer(),
                                container.serializer().decodeFunction().apply(friendlyByteBuf)
                        ), (payload, ctx) -> {
                            if (ctx.flow().getReceptionSide().isClient()) {
                                container.handler().accept(new PacketContext<>(null, payload.packet(), Side.CLIENT));
                            } else if (ctx.player().isPresent() && ctx.player().get() instanceof ServerPlayer serverPlayer) {
                                container.handler().accept(new PacketContext<>(serverPlayer, payload.packet(), Side.SERVER));
                            }
                        });
                case PLAY ->
                        registrar.play(container.channel(), friendlyByteBuf -> new de.rhm176.sphaira.api.networking.PayloadPacketWrapper<>(
                                container.channel(),
                                container.serializer().encodeConsumer(),
                                container.serializer().decodeFunction().apply(friendlyByteBuf)
                        ), (payload, ctx) -> {
                            if (ctx.flow().getReceptionSide().isClient()) {
                                container.handler().accept(new PacketContext<>(null, payload.packet(), Side.CLIENT));
                            } else if (ctx.player().isPresent() && ctx.player().get() instanceof ServerPlayer serverPlayer) {
                                container.handler().accept(new PacketContext<>(serverPlayer, payload.packet(), Side.SERVER));
                            }
                        });
            }
        *///?}
        }
    }
    private static final Set<PacketRegistrationContainer<?>> REGISTERED_PACKETS = new HashSet<>();
    //?} else {
    /*private static final Map<Class<?>, net.neoforged.neoforge.network.simple.SimpleChannel> CHANNELS = new HashMap<>();
    *///?}

    public SphairaNeoForge(IEventBus eventBus, ModContainer container) {
        Impl impl = new Impl();
        SphairaCommon.init(impl);

        impl.registerRegistries(eventBus);

        Map<String, Function<Screen, Screen>> configFactories = ConfigUtils.apply();
        for (String modId : configFactories.keySet()) {
            ModList.get().getModContainerById(modId).ifPresent(container2 -> {
                //? if >=1.20.6 {
                container2.registerExtensionPoint(IConfigScreenFactory.class, (Supplier<IConfigScreenFactory>) () -> new IConfigScreenFactory() {
                    // neoforge changed the signature some time, no idea when but this should support both

                    public @NotNull Screen createScreen(@NotNull ModContainer container3, @NotNull Screen parent) {
                        return configFactories.get(modId).apply(parent);
                    }
                    public @NotNull Screen createScreen(@NotNull Minecraft client, @NotNull Screen parent) {
                        return configFactories.get(modId).apply(parent);
                    }
                });
                //?} else {
                /*container2.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (client, parent) -> configFactories.get(modId).apply(parent)
                ));
                *///?}
            });
        }
	}

    //? if >=1.20.5 {
    @SubscribeEvent
    public void registerPackets(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent event) {
        REGISTERED_PACKETS.forEach(c -> c.register(event));
    }
    //?}

    private static class Impl implements SphairaCommon.BaseImpl {
        private final Map<String, Map<Registry<?>, DeferredRegister<?>>> REGISTRY_MAP = new HashMap<>();

        public void registerRegistries(IEventBus eventBus) {
            for (Map<Registry<?>, DeferredRegister<?>> value : REGISTRY_MAP.values()) {
                for (DeferredRegister<?> deferredRegister : value.values()) {
                    deferredRegister.register(eventBus);
                }
            }
        }

        @Override
        public boolean isModLoaded(String id) {
            return ModList.get().isLoaded(id);
        }

        @Override
        public Path getConfigDir() {
            return FMLPaths.CONFIGDIR.get();
        }

        @Override
        public Path getGameDir() {
            return FMLPaths.GAMEDIR.get();
        }

        @Override
        public boolean isDevelopmentEnvironment() {
            //? if >=1.21.9 {
            /*return !FMLLoader.getCurrent().isProduction();
            *///?} else {
            return !FMLLoader.isProduction();
            //?}
        }

        //? if >=1.20.5 {
        @Override
        public <T> void registerPayload(PacketContainer<T> container) {
            REGISTERED_PACKETS.add(new PacketRegistrationContainer<>(container));
        }

        @Override
        public void sendToServer(net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) {
            var listener = Minecraft.getInstance().getConnection();

            if (listener != null) {
                listener.getConnection().send(new net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket(payload));
            }
        }

        @Override
        public void sendToPlayer(net.minecraft.network.protocol.common.custom.CustomPacketPayload payload, ServerPlayer player) {
            player.connection.send(new net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket(payload));
        }
        //?} else {
        /*@Override
        public <T> void registerPayload(PacketContainer<T> container) {
            //? if >=1.20.4 {
            REGISTERED_PACKETS.add(new PacketRegistrationContainer<>(container));
            //?} else {
            /^var channel = net.neoforged.neoforge.network.NetworkRegistry.ChannelBuilder
                    .named(container.channel())
                    .clientAcceptedVersions((s) -> true)
                    .serverAcceptedVersions((s) -> true)
                    .networkProtocolVersion(() -> "1")
                    .simpleChannel();

            channel.registerMessage(
                    0,
                    container.clazz(),
                    (packet, buf) -> container.serializer().encodeConsumer().accept(buf, packet),
                    (buf) -> container.serializer().decodeFunction().apply(buf),
                    (packet, ctx) -> {
                        if (ctx.getDirection().getReceptionSide().isClient()) {
                            container.handler().accept(new PacketContext<>(null, packet, Side.CLIENT));
                        } else {
                            container.handler().accept(new PacketContext<>(ctx.getSender(), packet, Side.SERVER));
                        }
                    }
            );

            CHANNELS.put(container.clazz(), channel);
            ^///?}
        }

        @Override
        public <T> void sendToServer(PacketContainer<T> container, T packet) {
            //? if >=1.20.4 {
            PacketDistributor.SERVER.noArg().send(new de.rhm176.sphaira.api.networking.PayloadPacketWrapper<>(
                    container.channel(),
                    container.serializer().encodeConsumer(),
                    packet
            ));
            //?} else {
            /^CHANNELS.get(container.clazz()).sendToServer(packet);
            ^///?}
        }

        @Override
        public <T> void sendToPlayer(ServerPlayer player, PacketContainer<T> container, T packet) {
            //? if >=1.20.4 {
            PacketDistributor.PLAYER.with(player).send(new de.rhm176.sphaira.api.networking.PayloadPacketWrapper<>(
                    container.channel(),
                    container.serializer().encodeConsumer(),
                    packet
            ));
            //?} else {
            /^FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.EMPTY_BUFFER);
            container.serializer().encodeConsumer().accept(buf, packet);

            PacketDistributor.PLAYER.with(() -> player).send(new ClientboundCustomPayloadPacket(buf));
            ^///?}
        }
        *///?}

        @Override
        public <T> Set<T> getPlugins(String id, Class<T> entrypointClass, Class<? extends Annotation> annotationClass) {
            return ModList.get().getAllScanData().stream()
                    .flatMap(scanData -> scanData.getAnnotations().stream())
                    .filter(a -> Objects.equals(a.annotationType(), Type.getType(annotationClass)))
                    .map(ModFileScanData.AnnotationData::memberName)
                    .distinct()
                    .map(className -> {
                        try {
                            Class<?> clazz = Class.forName(className);
                            if (!entrypointClass.isAssignableFrom(clazz)) {
                                SphairaCommon.LOGGER.warn(
                                        "Class {} is annotated with @{} but does not implement {}. Ignoring.",
                                        className, annotationClass.getSimpleName(), entrypointClass.getSimpleName()
                                );
                                return null;
                            }

                            try {
                                clazz.getDeclaredConstructor();
                            } catch (NoSuchMethodException e) {
                                throw new IllegalArgumentException("Plugin class " + className + " must have a no-arg constructor", e);
                            }

                            return clazz.asSubclass(entrypointClass)
                                    .getDeclaredConstructor()
                                    .newInstance();
                        } catch (ReflectiveOperationException | LinkageError e) {
                            SphairaCommon.LOGGER.error("Failed to load: {}", className, e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        public <T> Supplier<T> register(Registry<T> registry, ResourceLocation id, T element) {
            Map<Registry<?>, DeferredRegister<?>> map = REGISTRY_MAP.computeIfAbsent(
                    id.getNamespace(),
                    k -> new HashMap<>()
            );
            DeferredRegister deferredRegister = map.computeIfAbsent(
                    registry,
                    r -> DeferredRegister.create(registry.key(), id.getNamespace())
            );

            return deferredRegister.register(id.getPath(), () -> element);
        }
    }
}
