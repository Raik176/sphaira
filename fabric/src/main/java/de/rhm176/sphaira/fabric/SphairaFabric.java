package de.rhm176.sphaira.fabric;

import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import net.fabricmc.api.ModInitializer;
import de.rhm176.sphaira.SphairaCommon;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Entrypoint
public class SphairaFabric implements ModInitializer {
	@Override
	public void onInitialize() {
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
