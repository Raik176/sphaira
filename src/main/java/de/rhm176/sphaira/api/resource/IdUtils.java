package de.rhm176.sphaira.api.resource;

import net.minecraft.resources.ResourceLocation;

public class IdUtils {
    public static ResourceLocation id(String ns, String id) {
        //? if >= 1.21 {
        /*return ResourceLocation.fromNamespaceAndPath(ns, id);
        *///?} else {
        return new ResourceLocation(ns, id);
         //?}
    }

    public static ResourceLocation withPrefix(ResourceLocation rl, String prefix) {
        return id(rl.getNamespace(), prefix + rl.getPath());
    }

    public static ResourceLocation withSuffix(ResourceLocation rl, String suffix) {
        return id(rl.getNamespace(), rl.getPath() + suffix);
    }
}