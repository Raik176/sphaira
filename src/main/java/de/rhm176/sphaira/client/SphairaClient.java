package de.rhm176.sphaira.client;


import de.rhm176.sphaira.client.api.ScreenUtils;
import de.rhm176.sphaira.mixin.client.accessor.MenuScreensAccessor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class SphairaClient {
    public static BaseImpl IMPL;

    public static void init(BaseImpl impl) {
        IMPL = impl;
    }

    public interface BaseImpl {
        default <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void registerScreen(MenuType<? extends M> type, ScreenUtils.MenuScreenFactory<M, U> factory) {
            MenuScreensAccessor.callRegister(type, factory::create);
        }
    }
}
