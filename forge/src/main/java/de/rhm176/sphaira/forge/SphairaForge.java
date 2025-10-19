package de.rhm176.sphaira.forge;

import de.rhm176.sphaira.api.ConfigUtils;
import de.rhm176.sphaira.SphairaCommon;
import de.rhm176.sphaira.api.networking.*;
import de.rhm176.sphaira.client.SphairaClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Mod(SphairaCommon.MOD_ID)
public class SphairaForge {
    @SuppressWarnings("removal")
    public SphairaForge() { // fix some problems on god knows what forge version
        this(FMLJavaModLoadingContext.get());
    }

	public SphairaForge(FMLJavaModLoadingContext context) {
        Impl impl = new Impl();
		SphairaCommon.init(impl);

        //? if >=1.21.6 {
        /*impl.registerRegistries(context.getModBusGroup());
        *///?} else {
        impl.registerRegistries(context.getModEventBus());
        //?}

        Map<String, Function<Screen, Screen>> configFactories = ConfigUtils.apply();
        for (String modId : configFactories.keySet()) {
            //? if >= 1.19 {
            ModList.get().getModContainerById(modId).ifPresent(container ->
                    container.registerExtensionPoint(net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory.class,
                            () -> new net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory((mc, parent) ->
                                    configFactories.get(modId).apply(parent)
                            )
                    ));
            //?} elif >= 1.18 {
            /*ModList.get().getModContainerById(modId).ifPresent(container ->
                    container.registerExtensionPoint(net.minecraftforge.client.ConfigGuiHandler.ConfigGuiFactory.class,
                            () -> new net.minecraftforge.client.ConfigGuiHandler.ConfigGuiFactory((mc, parent) ->
                                    configFactories.get(modId).apply(parent)
                            )
                    ));
            *///?} else {
            /*ModList.get().getModContainerById(modId).ifPresent(container ->
                    container.registerExtensionPoint(net.minecraftforge.fmlclient.ConfigGuiHandler.ConfigGuiFactory.class,
                    () -> new net.minecraftforge.fmlclient.ConfigGuiHandler.ConfigGuiFactory((mc, parent) ->
                            configFactories.get(modId).apply(parent)
                    )
            ));
            *///?}
        }
    }

    private static class Impl implements SphairaCommon.BaseImpl {
        private final Map<String, Map<Registry<?>, DeferredRegister<?>>> REGISTRY_MAP = new HashMap<>();

        //? if >=1.21.6 {
        /*public void registerRegistries(net.minecraftforge.eventbus.api.bus.BusGroup eventBus) {
        *///?} else {
        public void registerRegistries(net.minecraftforge.eventbus.api.IEventBus eventBus) {
        //?}
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
            return !FMLLoader.isProduction();
        }

        //? if >=1.20.5 {
        /*@Override
        public <T> void registerPayload(PacketContainer<T> container) {
            net.minecraftforge.network.payload.PayloadConnection<net.minecraft.network.protocol.common.custom.CustomPacketPayload> builder =
                net.minecraftforge.network.ChannelBuilder.named(container.channel()).optional().payloadChannel();
            switch (container.stage()) {
                case CONFIGURATION ->
                        builder.configuration().bidirectional().addMain(container.type(), container.streamCodec(), (msg, ctx) -> {
                            ctx.setPacketHandled(true);
                            ctx.enqueueWork(() -> container.handler().accept(new PacketContext<>(
                                    ctx.getSender(),
                                    msg.packet(),
                                    ctx.isClientSide() ? Side.CLIENT : Side.SERVER
                            )));
                        }).build();
                case PLAY ->
                        builder.play().bidirectional().addMain(container.type(), container.streamCodec().cast(), (msg, ctx) -> {
                            ctx.setPacketHandled(true);
                            ctx.enqueueWork(() -> container.handler().accept(new PacketContext<>(
                                    ctx.getSender(),
                                    msg.packet(),
                                    ctx.isClientSide() ? Side.CLIENT : Side.SERVER
                            )));
                        }).build();
                default -> throw new IllegalStateException("Unknown networking stage");
            }
        }

        @Override
        public void sendToServer(net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) {
            ClientPacketListener listener = Minecraft.getInstance().getConnection();

            if (listener != null) {
                listener.getConnection().send(new net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket(payload));
            }
        }

        @Override
        public void sendToPlayer(net.minecraft.network.protocol.common.custom.CustomPacketPayload payload, ServerPlayer player) {
            player.connection.send(new net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket(payload));
        }
        *///?} else {
        @Override
        public <T> void registerPayload(PacketContainer<T> container) {

        }

        @Override
        public void sendToServer(ResourceLocation channel, FriendlyByteBuf payload) {

        }

        @Override
        public void sendToPlayer(ResourceLocation channel, FriendlyByteBuf payload, ServerPlayer player) {

        }
        //?}

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
                    //? if >= 1.18.2 {
                    r -> DeferredRegister.create(registry.key(), id.getNamespace())
                    //?} else {
                    /*r -> DeferredRegister.create((IForgeRegistry<?>) registry, id.getNamespace())
                    *///?}
            );

            return deferredRegister.register(id.getPath(), () -> element);
        }
    }

    @Mod.EventBusSubscriber(modid = SphairaCommon.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            SphairaClient.init(new SphairaClient.BaseImpl() {

            });
        }
    }
}