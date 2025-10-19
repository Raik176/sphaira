package de.rhm176.sphaira.api.networking;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PacketContext<T>(@Nullable ServerPlayer sender, @NotNull T payload, @NotNull Side side) {

}