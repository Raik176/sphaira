package de.rhm176.sphaira;

import net.minecraft.client.gui.screens.Screen;
import oshi.util.tuples.Pair;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

public class ConfigUtils {
    private static final Map<String, Pair<BooleanSupplier, Class<? extends ConfigUtils.Factory>>> CONFIG_FACTORIES = new HashMap<>();

    public static <T extends Factory> void registerScreen(String id, BooleanSupplier condition, Class<T> factoryClass) {
        try {
            factoryClass.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Factory class " + factoryClass.getName() + " must have a no-arg constructor", e);
        }

        CONFIG_FACTORIES.put(id, new Pair<>(condition, factoryClass));
    }

    public static Map<String, Function<Screen, Screen>> apply() {
        Map<String, Function<Screen, Screen>> configScreenFactories = new HashMap<>();

        for (String modId : CONFIG_FACTORIES.keySet()) {
            Pair<BooleanSupplier, Class<? extends ConfigUtils.Factory>> pair =  CONFIG_FACTORIES.get(modId);

            if (pair.getA().getAsBoolean()) {
                try {
                    configScreenFactories.put(modId, pair.getB().getDeclaredConstructor().newInstance()::createConfig);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return configScreenFactories;
    }

    @FunctionalInterface
    public interface Factory {
        Screen createConfig(Screen parent);
    }
}
