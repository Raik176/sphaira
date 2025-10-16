package de.rhm176.sphaira;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

//? if >=1.19.3
/*import net.minecraft.core.registries.BuiltInRegistries;*/

public class RegistryUtils {
    //? if >=1.19.3 {
    /*public static final Registry<Item> ITEM_REGISTRY = BuiltInRegistries.ITEM;
    public static final Registry<Block> BLOCK_REGISTRY = BuiltInRegistries.BLOCK;
    public static final Registry<BlockEntityType<?>> BLOCK_ENTITY_TYPE_REGISTRY = BuiltInRegistries.BLOCK_ENTITY_TYPE;
    public static final Registry<EntityType<?>> ENTITY_TYPE_REGISTRY = BuiltInRegistries.ENTITY_TYPE;
    public static final Registry<RecipeType<?>> RECIPE_TYPE_REGISTRY = BuiltInRegistries.RECIPE_TYPE;
    public static final Registry<RecipeSerializer<?>> RECIPE_SERIALIZER_REGISTRY = BuiltInRegistries.RECIPE_SERIALIZER;
    //? if >=1.20.5
    /^public static final Registry<net.minecraft.core.component.DataComponentType<?>> DATA_COMPONENT_TYPE_REGISTRY = BuiltInRegistries.DATA_COMPONENT_TYPE;^/
    *///?} else {
    public static final Registry<Item> ITEM_REGISTRY = Registry.ITEM;
    public static final Registry<Block> BLOCK_REGISTRY = Registry.BLOCK;
    public static final Registry<BlockEntityType<?>> BLOCK_ENTITY_TYPE_REGISTRY = Registry.BLOCK_ENTITY_TYPE;
    public static final Registry<EntityType<?>> ENTITY_TYPE_REGISTRY = Registry.ENTITY_TYPE;
    public static final Registry<RecipeType<?>> RECIPE_TYPE_REGISTRY = Registry.RECIPE_TYPE;
    public static final Registry<RecipeSerializer<?>> RECIPE_SERIALIZER_REGISTRY = Registry.RECIPE_SERIALIZER;
    //?}

    public static class WrappedRegistry<T> {
        private final String namespace;
        private final Registry<T> registry;
        private final Set<Supplier<T>> elements;

        public WrappedRegistry(@NotNull Registry<T> registry, @NotNull String namespace) {
            this.namespace = namespace;
            this.registry = registry;
            this.elements = new HashSet<>();
        }

        public @NotNull Supplier<@NotNull T> register(@NotNull String id, @NotNull T element) {
            Supplier<T> registeredElement = SphairaCommon.IMPL.register(registry, IdUtils.id(namespace, id), element);

            elements.add(registeredElement);

            return registeredElement;
        }

        public @NotNull Set<Supplier<@NotNull T>> getElements() {
            return ImmutableSet.copyOf(elements);
        }
    }
}
