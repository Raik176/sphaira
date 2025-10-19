package de.rhm176.sphaira.client.api;

import de.rhm176.sphaira.client.SphairaClient;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class ScreenUtils {
    public static <T extends AbstractContainerMenu> void registerMenuScreen(MenuType<T> type, MenuScreenFactory<T> factory) {
        SphairaClient.IMPL.registerScreen(type, factory);
    }

    @FunctionalInterface
    public interface MenuScreenFactory<T extends AbstractContainerMenu> {
        <S extends AbstractContainerScreen<T>> S create(T menu, Inventory inventory, Component title);
    }
}
