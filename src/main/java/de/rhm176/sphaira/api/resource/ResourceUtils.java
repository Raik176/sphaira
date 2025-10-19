package de.rhm176.sphaira.api.resource;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import de.rhm176.sphaira.SphairaCommon;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.InputStreamReader;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ResourceUtils {
    private static final Set<RegisteredListener<?>> registeredListeners = new HashSet<>();

    public static <T> void registerResourceListener(Codec<T> codec, String pathPrefix, Function<String, Boolean> pathValidator, Consumer<List<T>> reloadConsumer) {
        registeredListeners.add(new RegisteredListener<>(pathPrefix, pathValidator, codec, reloadConsumer));
    }

    public static void reload(MinecraftServer server) {
        registeredListeners.forEach(listener -> listener.reload(server.getResourceManager()));
    }

    record RegisteredListener<T>(String pathPrefix, Function<String, Boolean> pathValidator, Codec<T> codec, Consumer<List<T>> reloadConsumer) {
        private static final BiConsumer<String, Exception> EXCEPTION_HANDLER = (resource, e) -> SphairaCommon.LOGGER.warn(
                "Could not load resource: \"{}\"",
                resource,
                e
        );
        private static final Gson GSON = new Gson();

        public void reload(ResourceManager manager) {
            List<T> contents = new ArrayList<>();

            //? if >=1.19 {
            for (Map.Entry<ResourceLocation, Resource> resource : manager.listResources(pathPrefix(), (path) -> pathValidator().apply(path.getPath())).entrySet()) {
                handleResource(resource.getKey(), resource.getValue(), contents);
            }
            //?} else {
            /*manager.listResources(pathPrefix(), pathValidator()::apply).stream().map(path -> {
                try {
                    return manager.getResource(path);
                } catch (Exception e) {
                    EXCEPTION_HANDLER.accept(path.toString(), e);
                    return null;
                }
            }).filter(Objects::nonNull).forEach(resource -> handleResource(resource.getLocation(), resource, contents));
            *///?}

            reloadConsumer().accept(contents);
        }

        private void handleResource(ResourceLocation path, Resource resource, List<T> contents) {
            try {
                //? if >=1.19 {
                JsonElement element = GSON.fromJson(resource.openAsReader(), JsonElement.class);
                //?} else {
                /*JsonElement element = GSON.fromJson(new InputStreamReader(resource.getInputStream()), JsonElement.class);
                *///?}
                DataResult<Pair<T, JsonElement>> result = codec().decode(JsonOps.INSTANCE, element);

                result.error().ifPresent(e -> SphairaCommon.LOGGER.warn(
                        "Could not load resource: \"{}\", because:{}",
                        path,
                        e.message()
                ));
                result.result().ifPresent((pair) -> contents.add(pair.getFirst()));
            } catch (Exception e) {
                EXCEPTION_HANDLER.accept(path.toString(), e);
            }
        }
    }
}