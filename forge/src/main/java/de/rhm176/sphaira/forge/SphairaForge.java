package de.rhm176.sphaira.forge;

import de.rhm176.sphaira.ConfigUtils;
import de.rhm176.sphaira.SphairaCommon;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
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
}