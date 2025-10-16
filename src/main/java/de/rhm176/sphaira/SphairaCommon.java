package de.rhm176.sphaira;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
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

        <T> Set<T> getPlugins(String id, Class<T> entrypointClass, Class<? extends Annotation> annotationClass);

        <T> Supplier<T> register(Registry<T> registry, ResourceLocation id, T element);
    }
}
