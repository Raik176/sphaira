package de.rhm176.sphaira.api.networking;

import net.minecraft.network.FriendlyByteBuf;

import java.util.function.BiConsumer;
import java.util.function.Function;

public record PayloadSerializer<S extends FriendlyByteBuf, T>(Function<S, T> decodeFunction, BiConsumer<S, T> encodeConsumer) {

}