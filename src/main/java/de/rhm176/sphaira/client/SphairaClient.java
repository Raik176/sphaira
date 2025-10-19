package de.rhm176.sphaira.client;


import de.rhm176.sphaira.client.api.ScreenUtils;
import de.rhm176.sphaira.mixin.client.accessor.MenuScreensAccessor;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class SphairaClient {
    public static BaseImpl IMPL;

    public static void init(BaseImpl impl) {
        IMPL = impl;
    }

    public interface BaseImpl {
        default <T extends AbstractContainerMenu> void registerScreen(MenuType<T> type, ScreenUtils.MenuScreenFactory<T> factory) {
            MenuScreensAccessor.callRegister(type, factory::create);
        }
    }
}
