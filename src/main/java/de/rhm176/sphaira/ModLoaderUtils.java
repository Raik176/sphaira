package de.rhm176.sphaira;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.Set;

public class ModLoaderUtils {
    public static boolean isModLoaded(@NotNull String id) {
        return SphairaCommon.IMPL.isModLoaded(id);
    }

    public static Path getConfigDir() {
        return SphairaCommon.IMPL.getConfigDir();
    }

    public static Path getGameDir() {
        return SphairaCommon.IMPL.getGameDir();
    }

    public static boolean isDevelopmentEnvironment() {
        return SphairaCommon.IMPL.isDevelopmentEnvironment();
    }

    public static <T> Set<T> getPlugins(String id, Class<T> entrypointClass, Class<? extends Annotation> annotationClass) {
        return SphairaCommon.IMPL.getPlugins(id, entrypointClass, annotationClass);
    }
}
