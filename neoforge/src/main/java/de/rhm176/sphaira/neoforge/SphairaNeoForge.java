package de.rhm176.sphaira.neoforge;

import de.rhm176.sphaira.SphairaCommon;
import net.neoforged.fml.common.Mod;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import java.util.function.Supplier;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import java.lang.annotation.Annotation;
import java.nio.file.Path;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import java.util.Map;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.Set;
import net.minecraft.client.gui.screens.Screen;
import java.util.function.Function;
import de.rhm176.sphaira.ConfigUtils;
import java.util.Objects;
import net.neoforged.fml.loading.FMLLoader;
import java.util.HashMap;
import org.objectweb.asm.Type;
import net.neoforged.neoforgespi.language.ModFileScanData;
import java.util.stream.Collectors;

//? if >=1.20.6 {
/*import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jetbrains.annotations.NotNull;
import net.minecraft.client.Minecraft;
*///?} else
import net.neoforged.neoforge.client.ConfigScreenHandler;

@Mod(SphairaCommon.MOD_ID)
public class SphairaNeoForge {
	public SphairaNeoForge(IEventBus eventBus, ModContainer container) {
        Impl impl = new Impl();
        SphairaCommon.init(impl);

        impl.registerRegistries(eventBus);

        Map<String, Function<Screen, Screen>> configFactories = ConfigUtils.apply();
        for (String modId : configFactories.keySet()) {
            ModList.get().getModContainerById(modId).ifPresent(container2 -> {
                //? if >=1.20.6 {
                /*container2.registerExtensionPoint(IConfigScreenFactory.class, (Supplier<IConfigScreenFactory>) () -> new IConfigScreenFactory() {
                    // neoforge changed the signature some time, no idea when but this should support both

                    public @NotNull Screen createScreen(@NotNull ModContainer container3, @NotNull Screen parent) {
                        return configFactories.get(modId).apply(parent);
                    }
                    public @NotNull Screen createScreen(@NotNull Minecraft client, @NotNull Screen parent) {
                        return configFactories.get(modId).apply(parent);
                    }
                });
                *///?} else {
                container2.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (client, parent) -> configFactories.get(modId).apply(parent)
                ));
                //?}
            });
        }
	}

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
            return false;
            //return !FMLLoader.isProduction(); //TODO: can't refernce from static context, wait for stonecutter to fix project bug
        }

        @Override
        public <T> void registerPayload(PacketContainer<T> container) {

        }

        @Override
        public void sendToServer(CustomPacketPayload payload) {

        }

        @Override
        public void sendToPlayer(CustomPacketPayload payload, ServerPlayer player) {

        }

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
